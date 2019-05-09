package com.mkl.eu.service.service.domain.impl;

import com.mkl.eu.client.service.util.GameUtil;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.service.service.domain.ICounterDomain;
import com.mkl.eu.service.service.domain.IStatusWorkflowDomain;
import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryOrderEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.WarEntity;
import com.mkl.eu.service.service.persistence.oe.military.BattleEntity;
import com.mkl.eu.service.service.persistence.oe.military.SiegeEntity;
import com.mkl.eu.service.service.util.DiffUtil;
import com.mkl.eu.service.service.util.IOEUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
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
    /** Game DAO only for flush purpose because Hibernate poorly handles non technical ids. */
    @Autowired
    private IGameDao gameDao;

    /** {@inheritDoc} */
    @Override
    public List<DiffEntity> computeEndAdministrativeActions(GameEntity game) {
        // FIXME check minors at war
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
        game.getOrders().removeAll(game.getOrders().stream()
                .filter(order -> order.getGameStatus() == GameStatusEnum.MILITARY_MOVE)
                .collect(Collectors.toList()));

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
                order.setGameStatus(GameStatusEnum.MILITARY_MOVE);
                order.setCountry(country);
                order.setPosition(i);
                game.getOrders().add(order);
            }
        }

        // FIXME when leaders implemented, it will be MILITARY_HIERARCHY phase
        game.setStatus(GameStatusEnum.MILITARY_MOVE);

        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STATUS,
                // FIXME when leaders implemented, it will be MILITARY_HIERARCHY phase
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, GameStatusEnum.MILITARY_MOVE));
        diffs.add(diff);

        diffs.addAll(nextRound(game, true));

        return diffs;
    }

    /** {@inheritDoc} */
    @Override
    public List<DiffEntity> endMilitaryPhase(GameEntity game) {
        List<DiffEntity> diffs = new ArrayList<>();

        // If we are in battle phase, are there still some battle left ?
        if (game.getStatus() == GameStatusEnum.MILITARY_BATTLES && game.getBattles().stream().anyMatch(battle -> battle.getStatus() == BattleStatusEnum.NEW)) {
            return diffs;
        }

        // If we are in siege phase, are there still some siege left ?
        if (game.getStatus() == GameStatusEnum.MILITARY_SIEGES && game.getSieges().stream().anyMatch(siege -> siege.getStatus() == SiegeStatusEnum.NEW)) {
            return diffs;
        }

        // Are there somme battles ?
        List<String> provincesAtWar = game.getStacks().stream()
                .filter(s -> s.getMovePhase() == MovePhaseEnum.FIGHTING)
                .map(StackEntity::getProvince)
                .collect(Collectors.toList());

        if (!provincesAtWar.isEmpty()) {
            // Yes -> battle phase !
            game.setStatus(GameStatusEnum.MILITARY_BATTLES);

            DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STATUS,
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, GameStatusEnum.MILITARY_BATTLES));
            diffs.add(diff);

            for (String province : provincesAtWar) {
                BattleEntity battle = new BattleEntity();
                battle.setProvince(province);
                battle.setTurn(game.getTurn());
                battle.setStatus(BattleStatusEnum.NEW);
                battle.setGame(game);

                game.getBattles().add(battle);
            }

            diff = DiffUtil.createDiff(game, DiffTypeEnum.INVALIDATE, DiffTypeObjectEnum.BATTLE,
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.TURN, game.getTurn()));
            diffs.add(diff);
            return diffs;
        }

        // Other cases: we check if there is another country in current phase of this round
        int currentPosition = game.getOrders().stream()
                .filter(o -> o.isActive() && o.getGameStatus() == game.getStatus())
                .map(CountryOrderEntity::getPosition)
                .findFirst()
                .orElse(Integer.MAX_VALUE);

        Integer next = game.getOrders().stream()
                .filter(o -> o.getGameStatus() == game.getStatus() &&
                        o.getPosition() > currentPosition)
                .map(CountryOrderEntity::getPosition)
                .min(Comparator.naturalOrder())
                .orElse(null);

        if (next != null) {
            // There are other countries, proceed to next countries.
            game.getOrders().stream()
                    .filter(o -> o.getGameStatus() == game.getStatus())
                    .forEach(o -> o.setReady(false));

            DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.INVALIDATE, DiffTypeObjectEnum.TURN_ORDER,
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, game.getStatus()));

            diffs.add(diff);

            game.getOrders().stream()
                    .filter(o -> o.getGameStatus() == game.getStatus())
                    .forEach(o -> o.setActive(false));
            game.getOrders().stream()
                    .filter(o -> o.getGameStatus() == game.getStatus() &&
                            o.getPosition() == next)
                    .forEach(o -> o.setActive(true));

            diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.TURN_ORDER,
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ACTIVE, next));

            diffs.add(diff);

            return diffs;
        } else {
            // There is no other country. If we are in move phase, check siege
            if (game.getStatus() == GameStatusEnum.MILITARY_MOVE) {
                List<String> provincesAtSiege = game.getStacks().stream()
                        .filter(s -> s.getMovePhase() == MovePhaseEnum.BESIEGING || s.getMovePhase() == MovePhaseEnum.STILL_BESIEGING)
                        .map(StackEntity::getProvince)
                        .collect(Collectors.toList());

                if (!provincesAtSiege.isEmpty()) {
                    // Yes -> siege phase !
                    game.setStatus(GameStatusEnum.MILITARY_SIEGES);

                    DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STATUS,
                            DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, GameStatusEnum.MILITARY_SIEGES));
                    diffs.add(diff);


                    for (String province : provincesAtSiege) {
                        SiegeEntity siege = new SiegeEntity();
                        siege.setProvince(province);
                        siege.setTurn(game.getTurn());
                        siege.setBreach(game.getSieges().stream()
                                .anyMatch(sie -> sie.isBreach() && Objects.equals(sie.getTurn(), game.getTurn()) && StringUtils.equals(sie.getProvince(), province)));
                        siege.setGame(game);

                        game.getSieges().add(siege);
                    }

                    diff = DiffUtil.createDiff(game, DiffTypeEnum.INVALIDATE, DiffTypeObjectEnum.SIEGE,
                            DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.TURN, game.getTurn()));
                    diffs.add(diff);

                    return diffs;
                }
            }
            // If no siege or last country of siege round, compute next round

            diffs.addAll(nextRound(game));
        }

        return diffs;
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
            diffs.add(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.GOOD_WEATHER, null, round, game));
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
                    diffs.add(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.GOOD_WEATHER, null, nextRound, game));
                    break;
            }
        }
        // Stacks move phase reset
        game.getStacks().stream()
                .filter(stack -> stack.getMovePhase() == MovePhaseEnum.MOVED)
                .forEach(stack -> {
                    stack.setMove(0);
                    stack.setMovePhase(MovePhaseEnum.NOT_MOVED);
                });

        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STACK,
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.MOVE_PHASE, MovePhaseEnum.NOT_MOVED));

        diffs.add(diff);

        // Invalidate (set ready to false) to all orders
        game.getOrders().stream()
                .filter(o -> o.getGameStatus() == GameStatusEnum.MILITARY_MOVE)
                .forEach(o -> o.setReady(false));
        diff = DiffUtil.createDiff(game, DiffTypeEnum.INVALIDATE, DiffTypeObjectEnum.TURN_ORDER,
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, GameStatusEnum.MILITARY_MOVE));

        diffs.add(diff);

        // set the order of position 0 active
        game.getOrders().stream()
                .filter(o -> o.getGameStatus() == GameStatusEnum.MILITARY_MOVE)
                .forEach(o -> o.setActive(false));
        game.getOrders().stream()
                .filter(o -> o.getGameStatus() == GameStatusEnum.MILITARY_MOVE &&
                        o.getPosition() == 0)
                .forEach(o -> o.setActive(true));
        diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.TURN_ORDER,
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ACTIVE, "0"),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, GameStatusEnum.MILITARY_MOVE));

        diffs.add(diff);

        return diffs;
    }

    protected List<DiffEntity> endRound(GameEntity game) {
        List<DiffEntity> diffs = new ArrayList<>();

        diffs.add(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.GOOD_WEATHER, null, "B_MR_End", game));
        // FIXME Redeployment phase

        return diffs;
    }
}
