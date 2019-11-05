package com.mkl.eu.service.service.domain.impl;

import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.util.GameUtil;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.service.service.domain.ICounterDomain;
import com.mkl.eu.service.service.domain.IStatusWorkflowDomain;
import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryInWarEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryOrderEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.WarEntity;
import com.mkl.eu.service.service.persistence.oe.military.BattleEntity;
import com.mkl.eu.service.service.persistence.oe.military.SiegeEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.util.DiffUtil;
import com.mkl.eu.service.service.util.IOEUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Domain for cross service methods around status workflow.
 *
 * @author MKL.
 */
@Component
public class StatusWorkflowDomainImpl implements IStatusWorkflowDomain {
    /** Counter domain. */
    @Autowired
    private ICounterDomain counterDomain;
    /** OeUtil. */
    @Autowired
    private IOEUtil oeUtil;
    /** Province Dao. */
    @Autowired
    private IProvinceDao provinceDao;
    /** Game DAO only for flush purpose because Hibernate poorly handles non technical ids and also to retrieve ids of created entities. */
    @Autowired
    private IGameDao gameDao;

    /** {@inheritDoc} */
    @Override
    public List<DiffEntity> computeEndAdministrativeActions(GameEntity game) {
        // TODO TG-13 check minors at war
        return initMilitaryPhase(game);
    }

    /** {@inheritDoc} */
    @Override
    public List<DiffEntity> computeEndMinorLogistics(GameEntity game) {
        return initMilitaryPhase(game);
    }

    /**
     * Initialize the military phase.
     *
     * @param game the game.
     * @return the List of Diff.
     */
    private List<DiffEntity> initMilitaryPhase(GameEntity game) {
        List<DiffEntity> diffs = new ArrayList<>();

        Map<PlayableCountryEntity, Integer> initiatives = game.getCountries().stream()
                .filter(country -> StringUtils.isNotEmpty(country.getUsername()))
                .collect(Collectors.toMap(Function.identity(), oeUtil::getInitiative));

        /**
         * First: we extract all groups (alliance in war = group) that will
         * move at the same initiative.
         */
        List<Alliance> alliances = new ArrayList<>();
        for (WarEntity war : game.getWars()) {
            List<PlayableCountryEntity> offCountries = new ArrayList<>();
            List<PlayableCountryEntity> defCountries = new ArrayList<>();
            war.getCountries().stream()
                    .filter(countryWar -> countryWar.getImplication() == WarImplicationEnum.FULL)
                    .forEach(countryWar -> {
                        PlayableCountryEntity country = game.getCountries().stream()
                                .filter(c -> StringUtils.equals(c.getName(), countryWar.getCountry().getName()))
                                .findFirst()
                                .orElse(null);
                        if (country != null) {
                            if (countryWar.isOffensive()) {
                                offCountries.add(country);
                            } else {
                                defCountries.add(country);
                            }
                        }
                    });


            Alliance offAlliance = new Alliance(offCountries, offCountries.stream()
                    .map(initiatives::get)
                    .min(Comparator.naturalOrder())
                    .orElse(0));
            alliances.add(offAlliance);
            Alliance defAlliance = new Alliance(defCountries, defCountries.stream()
                    .map(initiatives::get)
                    .min(Comparator.naturalOrder())
                    .orElse(0));
            alliances.add(defAlliance);
        }

        Alliance.fusion(alliances);

        /**
         * Then we add the countries that are not in war.
         */
        game.getCountries().stream()
                .filter(country -> StringUtils.isNotEmpty(country.getUsername()))
                .forEach(country -> {
                    Alliance alliance = alliances.stream()
                            .filter(all -> all.getCountries().contains(country))
                            .findFirst()
                            .orElse(null);
                    if (alliance == null) {
                        alliance = new Alliance(Collections.singletonList(country), initiatives.get(country));
                        alliances.add(alliance);
                    }
                });

        /**
         * Finally, previous order is removed.
         */
        game.getOrders().clear();

        /**
         * Hibernate will delete old values after inserting new one.
         * And since the PK is often the same, it will fail.
         * We need to flush so that the old values are deleted before.
         */
        this.gameDao.flush();

        /**
         * And the alliances are transformed into CountryOrder.
         */
        Collections.sort(alliances, Comparator.comparing(Alliance::getInitiative).reversed());
        for (int i = 0; i < alliances.size(); i++) {
            for (PlayableCountryEntity country : alliances.get(i).getCountries()) {
                CountryOrderEntity order = new CountryOrderEntity();
                order.setGame(game);
                order.setCountry(country);
                order.setPosition(i);
                game.getOrders().add(order);
            }
        }

        diffs.addAll(nextRound(game, true));

        return diffs;
    }

    /** {@inheritDoc} */
    @Override
    public List<DiffEntity> endMilitaryPhase(GameEntity game) {
        List<DiffEntity> diffs = new ArrayList<>();

        // If we are in battle phase, are there still some battle left ?
        if (game.getStatus() == GameStatusEnum.MILITARY_BATTLES &&
                game.getBattles().stream().anyMatch(battle -> Objects.equals(battle.getTurn(), game.getTurn())
                        && battle.getStatus() == BattleStatusEnum.NEW)) {
            return diffs;
        }
        int currentPosition = game.getOrders().stream()
                .filter(CountryOrderEntity::isActive)
                .map(CountryOrderEntity::getPosition)
                .findFirst()
                .orElse(Integer.MAX_VALUE);

        // If we are in siege phase, are there still some siege left ?
        if (game.getStatus() == GameStatusEnum.MILITARY_SIEGES &&
                game.getSieges().stream().anyMatch(siege -> Objects.equals(siege.getTurn(), game.getTurn())
                        && siege.getStatus() == SiegeStatusEnum.NEW)) {
            Integer activeSiege = game.getSieges().stream()
                    .filter(siege -> Objects.equals(siege.getTurn(), game.getTurn()) && siege.getStatus() == SiegeStatusEnum.NEW)
                    .map(siege -> getActiveOrder(siege.getWar(), siege.isBesiegingOffensive(), game))
                    .filter(Objects::nonNull)
                    .min(Comparator.<Integer>naturalOrder())
                    .orElse(null);

            if (activeSiege != currentPosition) {
                diffs.add(changeActivePlayers(activeSiege, game));
            }
            return diffs;
        }

        // Are there somme battles ?
        List<String> provincesAtWar = game.getStacks().stream()
                .filter(s -> s.getMovePhase() == MovePhaseEnum.FIGHTING)
                .map(StackEntity::getProvince)
                .distinct()
                .collect(Collectors.toList());

        if (!provincesAtWar.isEmpty()) {
            // Yes -> battle phase !
            game.setStatus(GameStatusEnum.MILITARY_BATTLES);

            diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STATUS,
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, GameStatusEnum.MILITARY_BATTLES)));

            for (String province : provincesAtWar) {
                BattleEntity battle = new BattleEntity();
                battle.setProvince(province);
                battle.setTurn(game.getTurn());
                battle.setStatus(BattleStatusEnum.NEW);
                battle.setGame(game);

                List<CounterEntity> phasingCounters = game.getStacks().stream()
                        .filter(s -> s.getMovePhase() == MovePhaseEnum.FIGHTING && StringUtils.equals(province, s.getProvince()))
                        .flatMap(s -> s.getCounters().stream())
                        .collect(Collectors.toList());
                List<CounterEntity> nonPhasingCounters = game.getStacks().stream()
                        .filter(s -> s.getMovePhase() != MovePhaseEnum.FIGHTING && StringUtils.equals(province, s.getProvince()))
                        .flatMap(s -> s.getCounters().stream())
                        .filter(c -> CounterUtil.isArmy(c.getType()))
                        .collect(Collectors.toList());
                Pair<WarEntity, Boolean> war = oeUtil.searchWar(phasingCounters, nonPhasingCounters, game);
                battle.setWar(war.getLeft());
                battle.setPhasingOffensive(war.getRight());
                gameDao.persist(battle);

                game.getBattles().add(battle);

                diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.ADD, DiffTypeObjectEnum.BATTLE, battle.getId(),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE, province),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.TURN, game.getTurn()),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, BattleStatusEnum.NEW),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ID_WAR, war.getLeft().getId()),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_OFFENSIVE, war.getRight())));
            }

            return diffs;
        }

        // Other cases: we check if there is another country in current phase of this round
        Integer next = game.getOrders().stream()
                .filter(o -> o.getPosition() > currentPosition)
                .map(CountryOrderEntity::getPosition)
                .min(Comparator.naturalOrder())
                .orElse(null);

        if (next != null) {
            if (game.getStatus() == GameStatusEnum.MILITARY_SIEGES) {
                Integer activeSiege = game.getSieges().stream()
                        .filter(siege -> Objects.equals(siege.getTurn(), game.getTurn()) && siege.getStatus() == SiegeStatusEnum.NEW)
                        .map(siege -> getActiveOrder(siege.getWar(), siege.isBesiegingOffensive(), game))
                        .filter(Objects::nonNull)
                        .min(Comparator.<Integer>naturalOrder())
                        .orElse(null);
                if (activeSiege != null) {
                    diffs.add(changeActivePlayers(activeSiege, game));
                } else {
                    diffs.addAll(nextRound(game));
                }
            } else {
                if (game.getStatus() == GameStatusEnum.MILITARY_BATTLES) {
                    game.setStatus(GameStatusEnum.MILITARY_MOVE);
                    diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STATUS,
                            DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, GameStatusEnum.MILITARY_MOVE)));

                }
                diffs.add(changeActivePlayers(next, game));
            }
            return diffs;
        } else {
            // There is no other country. If we are in move phase, check siege
            if (game.getStatus() == GameStatusEnum.MILITARY_MOVE || game.getStatus() == GameStatusEnum.MILITARY_BATTLES) {
                List<String> provincesAtSiege = game.getStacks().stream()
                        .filter(s -> s.getMovePhase().isBesieging())
                        .map(StackEntity::getProvince)
                        .distinct()
                        .collect(Collectors.toList());

                if (!provincesAtSiege.isEmpty()) {
                    // Yes -> siege phase !
                    game.setStatus(GameStatusEnum.MILITARY_SIEGES);

                    for (String province : provincesAtSiege) {
                        SiegeEntity siege = new SiegeEntity();
                        siege.setProvince(province);
                        siege.setTurn(game.getTurn());
                        siege.setStatus(SiegeStatusEnum.NEW);
                        siege.setBreach(game.getSieges().stream()
                                .anyMatch(sie -> sie.isBreach() && Objects.equals(sie.getTurn(), game.getTurn()) && StringUtils.equals(sie.getProvince(), province)));
                        siege.setGame(game);

                        List<CounterEntity> besiegingCounters = game.getStacks().stream()
                                .filter(s -> (s.getMovePhase().isBesieging()) && StringUtils.equals(province, s.getProvince()))
                                .flatMap(s -> s.getCounters().stream())
                                .collect(Collectors.toList());
                        List<CounterEntity> besiegedCounters = game.getStacks().stream()
                                .filter(s -> s.isBesieged() && StringUtils.equals(province, s.getProvince()))
                                .flatMap(s -> s.getCounters().stream())
                                .filter(c -> CounterUtil.isArmy(c.getType()))
                                .collect(Collectors.toList());
                        besiegedCounters.add(createFakeControl(province, game));
                        Pair<WarEntity, Boolean> war = oeUtil.searchWar(besiegingCounters, besiegedCounters, game);
                        siege.setWar(war.getLeft());
                        siege.setBesiegingOffensive(war.getRight());
                        gameDao.persist(siege);

                        diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.ADD, DiffTypeObjectEnum.SIEGE, siege.getId(),
                                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE, province),
                                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.TURN, game.getTurn()),
                                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, SiegeStatusEnum.NEW),
                                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ID_WAR, war.getLeft().getId()),
                                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_OFFENSIVE, war.getRight())));

                        game.getSieges().add(siege);
                    }
                    Integer activeSiege = game.getSieges().stream()
                            .filter(siege -> Objects.equals(siege.getTurn(), game.getTurn()) && siege.getStatus() == SiegeStatusEnum.NEW)
                            .map(siege -> getActiveOrder(siege.getWar(), siege.isBesiegingOffensive(), game))
                            .filter(Objects::nonNull)
                            .min(Comparator.<Integer>naturalOrder())
                            .orElse(null);

                    diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STATUS,
                            DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, GameStatusEnum.MILITARY_SIEGES)));

                    diffs.add(changeActivePlayers(activeSiege, game));

                    return diffs;
                }
            }
            // If no siege or last country of siege round, compute next round

            diffs.addAll(nextRound(game));
        }

        return diffs;
    }

    /**
     * Change the active player in the non simultaneous phase.
     *
     * @param position the new position of the active player in the non simultaneous phase.
     * @param game     the game.
     * @return the diff created.
     */
    private DiffEntity changeActivePlayers(int position, GameEntity game) {
        game.getOrders().stream()
                .forEach(o -> {
                    o.setActive(false);
                    o.setReady(false);
                });
        game.getOrders().stream()
                .filter(o -> o.getPosition() == position)
                .forEach(o -> o.setActive(true));

        return DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.TURN_ORDER,
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ACTIVE, position),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, GameStatusEnum.MILITARY_MOVE.name()));
    }

    /**
     * @param province the province.
     * @param game     the game.
     * @return a fake control counter owned by the controller of the province.
     */
    private CounterEntity createFakeControl(String province, GameEntity game) {
        AbstractProvinceEntity fullProvince = provinceDao.getProvinceByName(province);
        String controller = oeUtil.getController(fullProvince, game);
        CounterEntity fakeControlCounter = new CounterEntity();
        fakeControlCounter.setCountry(controller);
        fakeControlCounter.setType(CounterFaceTypeEnum.CONTROL);
        return fakeControlCounter;
    }

    /**
     * @param war       the war.
     * @param offensive the side.
     * @param game      the game.
     * @return the active order of the offensive side of the war in this turn.
     */
    private Integer getActiveOrder(WarEntity war, boolean offensive, GameEntity game) {
        Predicate<CountryInWarEntity> filterPhasing;
        if (offensive) {
            filterPhasing = country -> country.isOffensive() && country.getImplication() == WarImplicationEnum.FULL;
        } else {
            filterPhasing = country -> !country.isOffensive() && country.getImplication() == WarImplicationEnum.FULL;
        }
        Function<CountryInWarEntity, Integer> toActiveOrder = country -> game.getOrders().stream()
                .filter(order -> StringUtils.equals(country.getCountry().getName(), order.getCountry().getName()))
                .map(CountryOrderEntity::getPosition)
                .findAny()
                .orElse(null);

        return war.getCountries().stream()
                .filter(filterPhasing)
                .map(toActiveOrder)
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
    }

    /** {@inheritDoc} */
    @Override
    public List<DiffEntity> nextRound(GameEntity game) {
        return nextRound(game, false);
    }

    /**
     * Roll a die to go to next round (or first if init is <code>true</code>) and
     * creates the diffs ot it.
     * Does not handle good/bad weather at the moment.
     *
     * @param game to move to next round.
     * @param init to know if it is the first round of the turn or not.
     * @return the diffs representing the next round.
     */
    protected List<DiffEntity> nextRound(GameEntity game, boolean init) {
        List<DiffEntity> diffs = new ArrayList<>();

        int die = oeUtil.rollDie(game, (PlayableCountryEntity) null);

        if (init) {
            String round;
            switch (die) {
                case 1:
                    round = "B_MR_W0";
                    break;
                case 2:
                case 3:
                    round = "B_MR_S1";
                    break;
                case 4:
                case 5:
                    round = "B_MR_S2";
                    break;
                case 6:
                    round = "B_MR_W2";
                    break;
                case 7:
                case 8:
                case 9:
                case 10:
                default:
                    round = "B_MR_S3";
                    break;
            }
            diffs.addAll(initNewRound(round, game));
        } else {
            String round = game.getStacks().stream()
                    .filter(stack -> GameUtil.isRoundBox(stack.getProvince()))
                    .flatMap(stack -> stack.getCounters().stream())
                    .filter(counter -> counter.getType() == CounterFaceTypeEnum.GOOD_WEATHER || counter.getType() == CounterFaceTypeEnum.BAD_WEATHER)
                    .map(counter -> counter.getOwner().getProvince())
                    .findFirst()
                    .orElse(null);

            int roundNumber = GameUtil.getRoundBox(round);
            String nextRound;
            if (GameUtil.isWinterRoundBox(round)) {
                if (die <= 7) {
                    nextRound = "B_MR_S" + (roundNumber + 1);
                } else if (die == 8) {
                    nextRound = "B_MR_W" + (roundNumber + 1);
                } else {
                    nextRound = "B_MR_S" + (roundNumber + 2);
                }
            } else {
                if (die <= 5) {
                    nextRound = "B_MR_W" + roundNumber;
                } else {
                    nextRound = "B_MR_S" + (roundNumber + 1);
                }
            }
            switch (nextRound) {
                case "B_MR_S6":
                case "B_MR_W6":
                case "B_MR_S7":
                    diffs.addAll(endRound(game));
                    break;
                default:
                    diffs.addAll(initNewRound(nextRound, game));
                    break;
            }
        }

        return diffs;
    }

    /**
     * Computes the action at the end of a military round.
     *
     * @param nextRound the next season.
     * @param game      the game.
     * @return the diffs created.
     */
    private List<DiffEntity> initNewRound(String nextRound, GameEntity game) {
        List<DiffEntity> diffs = new ArrayList<>();
        diffs.add(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.GOOD_WEATHER, null, nextRound, game));

        // Stacks move phase reset
        game.getStacks().stream()
                .filter(stack -> stack.getMovePhase() != null)
                .forEach(stack -> {
                    stack.setMove(0);
                    if (stack.getMovePhase().isBesieging()) {
                        stack.setMovePhase(MovePhaseEnum.STILL_BESIEGING);
                    } else {
                        stack.setMovePhase(MovePhaseEnum.NOT_MOVED);
                    }
                });

        diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STACK,
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.MOVE_PHASE, MovePhaseEnum.NOT_MOVED)));

        // TODO TG-5 when leaders implemented, it will be MILITARY_HIERARCHY phase
        game.setStatus(GameStatusEnum.MILITARY_MOVE);

        diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STATUS,
                // TODO TG-5 when leaders implemented, it will be MILITARY_HIERARCHY phase
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, GameStatusEnum.MILITARY_MOVE)));

        // set the order of position 0 active
        diffs.add(changeActivePlayers(0, game));
        return diffs;
    }

    /**
     * Computes the action at the end of the military phase.
     *
     * @param game the game.
     * @return the diffs created.
     */
    protected List<DiffEntity> endRound(GameEntity game) {
        List<DiffEntity> diffs = new ArrayList<>();

        diffs.add(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.GOOD_WEATHER, null, "B_MR_End", game));
        game.setStatus(GameStatusEnum.REDEPLOYMENT);
        diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STATUS,
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, GameStatusEnum.REDEPLOYMENT)));
        // set the order of position 0 active
        diffs.add(changeActivePlayers(0, game));
        diffs.addAll(adjustPillages(game));

        return diffs;
    }

    /**
     * Remove one pillage from each province.
     *
     * @param game the game.
     * @return the diffs involved.
     */
    private List<DiffEntity> adjustPillages(GameEntity game) {
        Map<String, List<CounterEntity>> pillagesPerProvince = game.getStacks().stream()
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> counter.getType() == CounterFaceTypeEnum.PILLAGE_MINUS || counter.getType() == CounterFaceTypeEnum.PILLAGE_PLUS)
                .collect(Collectors.groupingBy(counter -> counter.getOwner().getProvince()));

        List<DiffEntity> diffs = new ArrayList<>();
        for (List<CounterEntity> pillages : pillagesPerProvince.values()) {
            CounterEntity pillageMinus = pillages.stream()
                    .filter(counter -> counter.getType() == CounterFaceTypeEnum.PILLAGE_MINUS)
                    .findAny()
                    .orElse(null);
            if (pillageMinus != null) {
                diffs.add(counterDomain.removeCounter(pillageMinus.getId(), game));
            } else {
                CounterEntity pillagePlus = pillages.stream()
                        .filter(counter -> counter.getType() == CounterFaceTypeEnum.PILLAGE_PLUS)
                        .findAny()
                        .orElse(null);
                if (pillagePlus != null) {
                    diffs.add(counterDomain.switchCounter(pillagePlus.getId(), CounterFaceTypeEnum.PILLAGE_MINUS, null, game));
                }
            }
        }

        return diffs;
    }

    /** {@inheritDoc} */
    @Override
    public List<DiffEntity> endRedeploymentPhase(GameEntity game) {
        List<DiffEntity> diffs = new ArrayList<>();

        int currentPosition = game.getOrders().stream()
                .filter(CountryOrderEntity::isActive)
                .map(CountryOrderEntity::getPosition)
                .findFirst()
                .orElse(Integer.MAX_VALUE);

        Integer next = game.getOrders().stream()
                .filter(o -> o.getPosition() > currentPosition)
                .map(CountryOrderEntity::getPosition)
                .min(Comparator.naturalOrder())
                .orElse(null);

        if (next != null) {
            diffs.add(changeActivePlayers(next, game));
        } else {
            // TODO exchequer
            game.setStatus(GameStatusEnum.EXCHEQUER);
            diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STATUS,
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, GameStatusEnum.EXCHEQUER)));
        }

        return diffs;
    }
}
