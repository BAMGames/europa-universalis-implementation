package com.mkl.eu.service.service.domain.impl;

import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.util.GameUtil;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.tables.Exchequer;
import com.mkl.eu.client.service.vo.tables.Result;
import com.mkl.eu.client.service.vo.tables.TradeIncome;
import com.mkl.eu.service.service.domain.ICounterDomain;
import com.mkl.eu.service.service.domain.IStatusWorkflowDomain;
import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.eco.IEconomicalSheetDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryInWarEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryOrderEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.WarEntity;
import com.mkl.eu.service.service.persistence.oe.eco.EconomicalSheetEntity;
import com.mkl.eu.service.service.persistence.oe.military.BattleEntity;
import com.mkl.eu.service.service.persistence.oe.military.SiegeEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.service.impl.AbstractBack;
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
public class StatusWorkflowDomainImpl extends AbstractBack implements IStatusWorkflowDomain {
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
    /** Counter Dao. */
    @Autowired
    private ICounterDao counterDao;
    /** EconomicalSheet DAO. */
    @Autowired
    private IEconomicalSheetDao economicalSheetDao;

    /** {@inheritDoc} */
    @Override
    public List<DiffEntity> computeEndAdministrativeActions(GameEntity game) {
        // TODO TG-18 check minors at war
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

            diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.GAME,
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
                    diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.GAME,
                            DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, GameStatusEnum.MILITARY_MOVE)));

                }
                diffs.add(changeActivePlayers(next, game));
            }
            return diffs;
        } else {
            // There is no other country. If we are in move phase, check siege
            if (game.getStatus() == GameStatusEnum.MILITARY_MOVE || game.getStatus() == GameStatusEnum.MILITARY_BATTLES) {
                List<String> provincesAtSiege = game.getStacks().stream()
                        .filter(s -> s.getMovePhase() != null && s.getMovePhase().isBesieging())
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
                                .filter(s -> s.getMovePhase() != null && s.getMovePhase().isBesieging() && StringUtils.equals(province, s.getProvince()))
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

                    diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.GAME,
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

        // TODO TG-58 when hierarchy implemented, it will be MILITARY_HIERARCHY phase
        game.setStatus(GameStatusEnum.MILITARY_MOVE);

        diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.GAME,
                // TODO TG-58 when hierarchy implemented, it will be MILITARY_HIERARCHY phase
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
        diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.GAME,
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
            diffs.addAll(adjustSiegeworks(game));
            diffs.addAll(updateEcoSheet(game));
            game.setStatus(GameStatusEnum.EXCHEQUER);
            diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.GAME,
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, GameStatusEnum.EXCHEQUER),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ACTIVE, false)));
        }

        return diffs;
    }

    /**
     * Only one siegework minus can remain if the province is still besieged.
     *
     * @param game the game.
     * @return the diffs involved.
     */
    private List<DiffEntity> adjustSiegeworks(GameEntity game) {
        Map<String, List<CounterEntity>> siegeworksPerProvince = game.getStacks().stream()
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> counter.getType() == CounterFaceTypeEnum.SIEGEWORK_MINUS || counter.getType() == CounterFaceTypeEnum.SIEGEWORK_PLUS)
                .collect(Collectors.groupingBy(counter -> counter.getOwner().getProvince()));

        List<DiffEntity> diffs = new ArrayList<>();
        for (String province : siegeworksPerProvince.keySet()) {
            List<CounterEntity> siegeworks = siegeworksPerProvince.get(province);
            boolean besieged = game.getStacks().stream()
                    .anyMatch(stack -> stack.getMovePhase() != null && stack.getMovePhase().isBesieging()
                            && StringUtils.equals(province, stack.getProvince()));
            if (!besieged) {
                siegeworks.stream()
                        .forEach(siegework -> diffs.add(counterDomain.removeCounter(siegework.getId(), game)));
            } else {
                CounterEntity siegeworkRemain = siegeworks.stream()
                        .filter(counter -> counter.getType() == CounterFaceTypeEnum.SIEGEWORK_MINUS)
                        .findAny()
                        .orElse(siegeworks.stream()
                                .filter(counter -> counter.getType() == CounterFaceTypeEnum.SIEGEWORK_PLUS)
                                .findAny()
                                .orElse(null));
                siegeworks.stream()
                        .filter(siegework -> !Objects.equals(siegeworkRemain.getId(), siegework.getId()))
                        .forEach(siegework -> diffs.add(counterDomain.removeCounter(siegework.getId(), game)));
                if (siegeworkRemain.getType() == CounterFaceTypeEnum.SIEGEWORK_PLUS) {
                    diffs.add(counterDomain.switchCounter(siegeworkRemain.getId(), CounterFaceTypeEnum.SIEGEWORK_MINUS, null, game));
                }
            }
        }

        return diffs;
    }

    /**
     * Compute the exceptional taxes and the exchequer test for each country.
     *
     * @param game the game.
     * @return the diffs involved.
     */
    private List<DiffEntity> updateEcoSheet(GameEntity game) {
        List<DiffEntity> diffs = new ArrayList<>();

        for (PlayableCountryEntity country : game.getCountries()) {
            EconomicalSheetEntity sheet = country.getEconomicalSheets().stream()
                    .filter(es -> Objects.equals(game.getTurn(), es.getTurn()))
                    .findAny()
                    .orElse(null);

            if (sheet != null) {
                List<DiffAttributesEntity> attributes = new ArrayList<>();
                attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ID_COUNTRY, country.getId()));
                attributes.addAll(computeExceptionalTaxes(sheet, country, game));
                attributes.addAll(computeExchequer(sheet, country, game));

                diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.ECO_SHEET, sheet.getId(),
                        attributes.toArray(new DiffAttributesEntity[attributes.size()])));

                country.setReady(false);
            }
        }

        return diffs;
    }

    /**
     * Compute the exceptional taxes for each country that has done one.
     *
     * @param sheet   the economical sheet.
     * @param country the country.
     * @param game    the game.
     * @return the diffs involved.
     */
    private List<DiffAttributesEntity> computeExceptionalTaxes(EconomicalSheetEntity sheet, PlayableCountryEntity country, GameEntity game) {
        List<DiffAttributesEntity> diffs = new ArrayList<>();

        if (sheet.getExcTaxesMod() != null) {
            Integer die = oeUtil.rollDie(game, country);
            sheet.setExcTaxes(10 * (die + sheet.getExcTaxesMod()));
            diffs.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.EXC_TAXES, sheet.getExcTaxes()));
        }

        return diffs;
    }

    /**
     * Compute the exchequer test for each country.
     *
     * @param sheet   the economical sheet.
     * @param country the country.
     * @param game    the game.
     * @return the diffs involved.
     */
    private List<DiffAttributesEntity> computeExchequer(EconomicalSheetEntity sheet, PlayableCountryEntity country, GameEntity game) {
        List<DiffAttributesEntity> diffs = new ArrayList<>();

        sheet.setRtBefExch(CommonUtil.add(0, sheet.getRtDiplo(), sheet.getPillages(), sheet.getGoldRotw(), sheet.getExcTaxes()));
        sheet.setExpenses(CommonUtil.add(0, sheet.getInterestExpense(), sheet.getMandRefundExpense(),
                sheet.getAdmTotalExpense(), sheet.getMilitaryExpense()));

        Integer stab = oeUtil.getStability(game, country.getName());
        Integer exchequerMod = 0;
        WarStatusEnum warStatus = oeUtil.getWarStatus(game, country);
        if (warStatus == WarStatusEnum.PEACE) {
            exchequerMod += 2;
        }
        // TODO TG-138 Loans and bankrupt
        // TODO TG-18 loan treaty broken

        Integer die = oeUtil.rollDie(game, country);
        sheet.setExchequerColumn(stab);
        sheet.setExchequerBonus(exchequerMod);
        sheet.setExchequerDie(die);

        int modifiedDie = Math.min(Math.max(die + exchequerMod, 1), 10);
        ResultEnum result = getTables().getResults().stream()
                .filter(res -> Objects.equals(stab, res.getColumn()) && Objects.equals(modifiedDie, res.getDie()))
                .map(Result::getResult)
                .findAny()
                .orElseThrow(createTechnicalExceptionSupplier(IConstantsCommonException.MISSING_TABLE, MSG_MISSING_TABLE, "results", "column:" + stab + ",die:" + modifiedDie));

        Exchequer exchequer = getTables().getExchequers().stream()
                .filter(exc -> exc.getResult() == result)
                .findAny()
                .orElseThrow(createTechnicalExceptionSupplier(IConstantsCommonException.MISSING_TABLE, MSG_MISSING_TABLE, "exchequer", result.name()));

        int grossIncome = sheet.getGrossIncome() != null ? sheet.getGrossIncome() : 0;
        int regular = grossIncome * exchequer.getRegular() / 100;
        int prestige = grossIncome * exchequer.getPrestige() / 100;
        int loanRatio = exchequer.getNatLoan();
        if (warStatus != WarStatusEnum.PEACE) {
            loanRatio += 10;
        }
        // TODO TG-131 Spain +10 if expulsion
        int loan = grossIncome * loanRatio / 100;
        sheet.setRegularIncome(regular);
        sheet.setPrestigeIncome(prestige);
        sheet.setMaxNatLoan(loan);
        // TODO TG-138 international loans

        sheet.setRemainingExpenses(sheet.getExpenses() - sheet.getRegularIncome());

        diffs.addAll(Arrays.asList(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.EXCHEQUER_ROYAL_TREASURE, sheet.getRtBefExch()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.EXPENSES, sheet.getExpenses()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.EXCHEQUER_COL, sheet.getExchequerColumn()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.EXCHEQUER_MOD, sheet.getExchequerBonus()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.EXCHEQUER_DIE, sheet.getExchequerDie()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.EXCHEQUER_REGULAR, sheet.getRegularIncome()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.EXCHEQUER_PRESTIGE, sheet.getPrestigeIncome()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.EXCHEQUER_MAX_NAT_LOAN, sheet.getMaxNatLoan()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.REMAINING_EXPENSES, sheet.getRemainingExpenses())));

        return diffs;
    }

    /** {@inheritDoc} */
    @Override
    public List<DiffEntity> endExchequerPhase(GameEntity game) {
        List<DiffEntity> diffs = new ArrayList<>();

        for (PlayableCountryEntity country : game.getCountries()) {
            EconomicalSheetEntity sheet = country.getEconomicalSheets().stream()
                    .filter(es -> Objects.equals(game.getTurn(), es.getTurn()))
                    .findAny()
                    .orElse(null);

            if (sheet != null) {
                int additionalIncomes = CommonUtil.add(0, sheet.getPrestigeSpent(), sheet.getNatLoan(), sheet.getInterLoan());
                sheet.setRtBalance(additionalIncomes - sheet.getRemainingExpenses());
                sheet.setRtAftExch(CommonUtil.add(sheet.getRtBefExch(), sheet.getRtBalance()));
                sheet.setPrestigeVP(CommonUtil.subtract(sheet.getPrestigeIncome(), sheet.getPrestigeSpent()));
                boolean firstTurnOfPeriod = getTables().getPeriods().stream()
                        .anyMatch(period -> Objects.equals(period.getBegin(), game.getTurn()));
                sheet.setWealth(CommonUtil.add(sheet.getGrossIncome(), sheet.getPrestigeVP()));
                if (firstTurnOfPeriod) {
                    sheet.setPeriodWealth(sheet.getWealth());
                } else {
                    int previousWealth = country.getEconomicalSheets().stream()
                            .filter(es -> Objects.equals(game.getTurn() - 1, es.getTurn()) && es.getPeriodWealth() != null)
                            .map(EconomicalSheetEntity::getPeriodWealth)
                            .findAny()
                            .orElse(0);

                    sheet.setPeriodWealth(CommonUtil.add(previousWealth, sheet.getWealth()));
                }

                sheet.setStabModifier(getStabilityModifier(country, game));

                diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.ECO_SHEET, sheet.getId(),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ID_COUNTRY, country.getId()),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ROYAL_TREASURE_BALANCE, sheet.getRtBalance()),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ROYAL_TREASURE_AFTER_EXCHEQUER, sheet.getRtAftExch()),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PRESTIGE_VPS, sheet.getPrestigeVP()),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.WEALTH, sheet.getWealth()),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PERIOD_WEALTH, sheet.getPeriodWealth()),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STAB_MODIFIER, sheet.getStabModifier())));

                country.setReady(false);
            }
        }

        game.setStatus(GameStatusEnum.STABILITY);
        diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.GAME,
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, GameStatusEnum.STABILITY.name()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ACTIVE, false)));

        return diffs;
    }

    /**
     * Get stability modifier without investment part.
     *
     * @param country the country.
     * @param game    the game.
     * @return the stability modifier.
     */
    private int getStabilityModifier(PlayableCountryEntity country, GameEntity game) {
        int modifier = oeUtil.getAdministrativeValue(country);
        // TODO TG-18 wars
        List<String> enemies = oeUtil.getEnemies(country, game);
        // TODO TG-18 limited intervention
        boolean warWithMajor = game.getCountries().stream()
                .anyMatch(major -> StringUtils.isNotEmpty(major.getUsername()) && enemies.contains(major.getName()));
        if (warWithMajor) {
            modifier -= 3;
        } else if (!enemies.isEmpty()) {
            modifier -= 2;
        }
        // TODO -5 if enemy army in owned national province
        if (!enemies.isEmpty()) {
            List<String> provinces = counterDao.getNationalTerritoriesUnderAttack(country.getName(), enemies, game.getId());
            if (!provinces.isEmpty()) {
                if (StringUtils.equals(PlayableCountry.SPAIN, country.getName())) {
                    // TODO TG-13 some events will restore spain malus to -5
                    modifier -= 3;
                } else {
                    modifier -= 5;
                }
            }
        }

        modifier += 3 * oeUtil.getProsperity(country, game);
        // TODO TG-13 Events
        return modifier;
    }

    /** {@inheritDoc} */
    @Override
    public List<DiffEntity> endStabilityPhase(GameEntity game) {
        List<DiffEntity> diffs = new ArrayList<>();

        int treshold = counterDao.getGoldExploitedRotw(game.getId()) >= 100 ? 3 : 7;
        int rollDie = oeUtil.rollDie(game);
        if (rollDie >= treshold) {
            Optional<DiffEntity> diff = counterDomain.increaseInflation(game);
            diff.ifPresent(diffs::add);
        }
        String inflationBox = oeUtil.getInflationBox(game);

        for (PlayableCountryEntity country : game.getCountries()) {
            EconomicalSheetEntity sheet = country.getEconomicalSheets().stream()
                    .filter(es -> Objects.equals(game.getTurn(), es.getTurn()))
                    .findAny()
                    .orElse(null);
            if (sheet != null) {
                sheet.setRtPeace(CommonUtil.toInt(sheet.getRtAftExch()) - CommonUtil.toInt(sheet.getStab()));

                int americanGold = counterDao.getGoldExploitedAmerica(country.getName(), game.getId());
                // TODO TG-131 Turkey before reforms has big inglation
                int inflation = GameUtil.getInflation(inflationBox, americanGold > 0);
                int computedInflation = (int) Math.ceil(((double) inflation * Math.abs(sheet.getRtPeace())) / 100);
                int minInflation = oeUtil.getMinimalInflation(inflation, country.getName(), getTables(), game);
                int actualInflation = Math.max(minInflation, computedInflation);
                sheet.setInflation(actualInflation);
                sheet.setRtEnd(sheet.getRtPeace() - actualInflation);

                diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.ECO_SHEET, sheet.getId(),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ID_COUNTRY, country.getId()),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ROYAL_TREASURE_PEACE, sheet.getRtPeace()),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.INFLATION, sheet.getInflation()),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ROYAL_TREASURE_END, sheet.getRtEnd())));

                EconomicalSheetEntity nextSheet = new EconomicalSheetEntity();
                nextSheet.setCountry(country);
                nextSheet.setTurn(game.getTurn() + 1);
                nextSheet.setRtStart(sheet.getRtEnd());
                // TODO TG-13 events
                nextSheet.setRtEvents(nextSheet.getRtStart());
                // TODO TG-18 diplo phase
                nextSheet.setRtDiplo(nextSheet.getRtStart());

                economicalSheetDao.create(nextSheet);

                country.getEconomicalSheets().add(nextSheet);

                diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.ADD, DiffTypeObjectEnum.ECO_SHEET, nextSheet.getId(),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ID_COUNTRY, country.getId()),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.TURN, nextSheet.getTurn()),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ROYAL_TREASURE_START, nextSheet.getRtStart())));

                country.setReady(false);
            }
        }
        game.setTurn(game.getTurn() + 1);
        diffs.add(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.TURN, null, GameUtil.getTurnBox(game.getTurn()), game));
        // TODO TG-13 events
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.GAME,
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ACTIVE, false),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.TURN, game.getTurn())));

        diffs.add(computeEconomicalSheets(game));

        return diffs;
    }

    /** {@inheritDoc} */
    public DiffEntity computeEconomicalSheets(GameEntity game) {
        Map<String, List<CounterFaceTypeEnum>> tradeCenters = economicalSheetDao.getTradeCenters(game.getId());
        for (PlayableCountryEntity country : game.getCountries()) {
            if (StringUtils.isEmpty(country.getUsername())) {
                continue;
            }
            EconomicalSheetEntity sheet = country.getEconomicalSheets().stream()
                    .filter(es -> Objects.equals(es.getTurn(), game.getTurn()))
                    .findAny()
                    .orElse(null);
            if (sheet == null) {
                sheet = new EconomicalSheetEntity();
                sheet.setCountry(country);
                sheet.setTurn(game.getTurn());

                economicalSheetDao.create(sheet);

                country.getEconomicalSheets().add(sheet);
            }

            String name = country.getName();

            Map<String, Integer> allProvinces = new HashMap<>();

            Map<String, Integer> provinces = economicalSheetDao.getOwnedAndControlledProvinces(name, game.getId());
            sheet.setProvincesIncome(provinces.values().stream().collect(Collectors.summingInt(value -> value)));

            Map<String, Integer> vassalProvinces = new HashMap<>();
            List<String> vassals = counterDao.getVassals(name, game.getId());
            for (String vassal : vassals) {
                vassalProvinces.putAll(economicalSheetDao.getOwnedAndControlledProvinces(vassal, game.getId()));
            }
            sheet.setVassalIncome(vassalProvinces.values().stream().collect(Collectors.summingInt(value -> value)));

            List<String> provinceNames = new ArrayList<>();
            provinceNames.addAll(provinces.keySet());
            provinceNames.addAll(vassalProvinces.keySet());
            List<String> pillagedProvinces = economicalSheetDao.getPillagedProvinces(provinceNames, game.getId());

            allProvinces.putAll(provinces);
            allProvinces.putAll(vassalProvinces);

            Integer pillagedIncome = pillagedProvinces.stream().collect(Collectors.summingInt(allProvinces::get));

            sheet.setLostIncome(pillagedIncome);

            Integer sum = CommonUtil.add(sheet.getProvincesIncome(), sheet.getVassalIncome(), sheet.getEventLandIncome());
            if (sheet.getLostIncome() != null) {
                sum -= sheet.getLostIncome();
            }
            sheet.setLandIncome(sum);

            sheet.setMnuIncome(economicalSheetDao.getMnuIncome(name, pillagedProvinces, game.getId()));

            List<String> provincesOwnedNotPillaged = provinces.keySet().stream().filter(s -> !pillagedProvinces.contains(s)).collect(Collectors.toList());
            sheet.setGoldIncome(economicalSheetDao.getGoldIncome(provincesOwnedNotPillaged, game.getId()));

            sheet.setIndustrialIncome(CommonUtil.add(sheet.getMnuIncome(), sheet.getGoldIncome()));

            final Integer valueDom = CommonUtil.add(sheet.getProvincesIncome(), sheet.getVassalIncome());
            TradeIncome tradeIncome = CommonUtil.findFirst(getTables().getDomesticTrades(), tradeIncome1 -> tradeIncome1.getCountryValue() == country.getDti()
                            && (tradeIncome1.getMinValue() == null || tradeIncome1.getMinValue() <= valueDom)
                            && (tradeIncome1.getMaxValue() == null || tradeIncome1.getMaxValue() >= valueDom)
            );
            if (tradeIncome != null) {
                sheet.setDomTradeIncome(tradeIncome.getValue());
            }

            // TODO needs War to know the blocked trade
            // TODO move elsewhere because we need to know the land income
            // of all countries.
            final Integer valueFor = 0;
            tradeIncome = CommonUtil.findFirst(getTables().getForeignTrades(), tradeIncome1 -> tradeIncome1.getCountryValue() == country.getFti()
                            && (tradeIncome1.getMinValue() == null || tradeIncome1.getMinValue() <= valueFor)
                            && (tradeIncome1.getMaxValue() == null || tradeIncome1.getMaxValue() >= valueFor)
            );
            if (tradeIncome != null) {
                sheet.setForTradeIncome(tradeIncome.getValue());
            }

            sheet.setFleetLevelIncome(economicalSheetDao.getFleetLevelIncome(name, game.getId()));

            sheet.setFleetMonopIncome(economicalSheetDao.getFleetLevelMonopoly(name, game.getId()));

            Integer tradeCentersIncome = 0;

            if (tradeCenters.get(name) != null) {
                for (CounterFaceTypeEnum tradeCenter : tradeCenters.get(name)) {
                    if (tradeCenter == CounterFaceTypeEnum.TRADE_CENTER_ATLANTIC) {
                        tradeCentersIncome += 100;
                    } else if (tradeCenter == CounterFaceTypeEnum.TRADE_CENTER_MEDITERRANEAN) {
                        tradeCentersIncome += 100;
                    } else if (tradeCenter == CounterFaceTypeEnum.TRADE_CENTER_INDIAN) {
                        tradeCentersIncome += 50;
                    }
                }
            }

            sheet.setTradeCenterIncome(tradeCentersIncome);

            sum = CommonUtil.add(sheet.getDomTradeIncome(), sheet.getForTradeIncome(), sheet.getFleetLevelIncome(), sheet.getFleetMonopIncome(), sheet.getTradeCenterIncome());
            if (sheet.getTradeCenterLoss() != null) {
                sum -= sheet.getTradeCenterLoss();
            }
            sheet.setTradeIncome(sum);

            Pair<Integer, Integer> colTpIncome = economicalSheetDao.getColTpIncome(name, game.getId());
            sheet.setColIncome(colTpIncome.getLeft());
            sheet.setTpIncome(colTpIncome.getRight());
            sheet.setExoResIncome(economicalSheetDao.getExoResIncome(name, game.getId()));

            sheet.setRotwIncome(CommonUtil.add(sheet.getColIncome(), sheet.getTpIncome(), sheet.getExoResIncome()));

            sheet.setIncome(CommonUtil.add(sheet.getLandIncome(), sheet.getIndustrialIncome(), sheet.getTradeIncome(), sheet.getRotwIncome(), sheet.getSpecialIncome()));

            sheet.setGrossIncome(CommonUtil.add(sheet.getIncome(), sheet.getEventIncome()));
        }

        return DiffUtil.createDiff(game, DiffTypeEnum.INVALIDATE, DiffTypeObjectEnum.ECO_SHEET,
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.TURN, game.getTurn()));
    }
}
