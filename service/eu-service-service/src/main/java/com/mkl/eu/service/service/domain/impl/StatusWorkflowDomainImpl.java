package com.mkl.eu.service.service.domain.impl;

import com.mkl.eu.client.service.util.GameUtil;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.service.service.domain.ICounterDomain;
import com.mkl.eu.service.service.domain.IStatusWorkflowDomain;
import com.mkl.eu.service.service.persistence.diff.IDiffDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryOrderEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.WarEntity;
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
    /** Diff DAO. */
    @Autowired
    private IDiffDao diffDao;

    /** {@inheritDoc} */
    @Override
    public List<DiffEntity> computeEndAdministrativeActions(GameEntity game) {
        // FIXME check minors at war
        return computeEndMinorLogistics(game);
    }

    /** {@inheritDoc} */
    @Override
    public List<DiffEntity> computeEndMinorLogistics(GameEntity game) {
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

        fusion(alliances);

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

        DiffEntity diff = new DiffEntity();
        diff.setIdGame(game.getId());
        diff.setVersionGame(game.getVersion());
        diff.setType(DiffTypeEnum.INVALIDATE);
        diff.setTypeObject(DiffTypeObjectEnum.TURN_ORDER);
        DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.STATUS);
        diffAttributes.setValue(GameStatusEnum.MILITARY_MOVE.name());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);
        diffs.add(diff);

        diffDao.create(diff);

        // FIXME when leaders implemented, it will be MILITARY_HIERARCHY phase
        game.setStatus(GameStatusEnum.MILITARY_MOVE);

        diff = new DiffEntity();
        diff.setIdGame(game.getId());
        diff.setVersionGame(game.getVersion());
        diff.setType(DiffTypeEnum.MODIFY);
        diff.setTypeObject(DiffTypeObjectEnum.STATUS);
        diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.STATUS);
        // FIXME when leaders implemented, it will be MILITARY_HIERARCHY phase
        diffAttributes.setValue(GameStatusEnum.MILITARY_MOVE.name());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);
        diffs.add(diff);

        diffDao.create(diff);

        diffs.addAll(nextRound(game, true));

        return diffs;
    }

    /**
     * Fusion alliances base on countries.
     * Fusion is transitive.
     *
     * @param alliances to fusion.
     */
    protected void fusion(List<Alliance> alliances) {
        List<Alliance> allianceToDelete = new ArrayList<>();

        for (Alliance alliance : alliances) {
            if (allianceToDelete.contains(alliance)) {
                continue;
            }
            for (Alliance allianceNext : alliances) {
                if (alliance == allianceNext) {
                    continue;
                }

                if (alliance.fusion(allianceNext)) {
                    allianceToDelete.add(allianceNext);
                }
            }
        }

        if (!allianceToDelete.isEmpty()) {
            alliances.removeAll(allianceToDelete);
            fusion(alliances);
        }
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
                .forEach(stack -> stack.setMovePhase(MovePhaseEnum.NOT_MOVED));

        DiffEntity diff = new DiffEntity();
        diff.setIdGame(game.getId());
        diff.setVersionGame(game.getVersion());
        diff.setType(DiffTypeEnum.MODIFY);
        diff.setTypeObject(DiffTypeObjectEnum.STACK);
        DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.MOVE_PHASE);
        diffAttributes.setValue(MovePhaseEnum.NOT_MOVED.name());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);

        diffDao.create(diff);
        diffs.add(diff);

        // Invalidate (set ready to false) to all orders
        game.getOrders().stream()
                .filter(o -> o.getGameStatus() == GameStatusEnum.MILITARY_MOVE)
                .forEach(o -> o.setReady(false));
        diff = new DiffEntity();
        diff.setIdGame(game.getId());
        diff.setVersionGame(game.getVersion());
        diff.setType(DiffTypeEnum.INVALIDATE);
        diff.setTypeObject(DiffTypeObjectEnum.TURN_ORDER);
        diff.setIdObject(null);
        diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.STATUS);
        diffAttributes.setValue(GameStatusEnum.MILITARY_MOVE.name());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);

        diffDao.create(diff);
        diffs.add(diff);

        // set the order of position 0 active
        game.getOrders().stream()
                .filter(o -> o.getGameStatus() == GameStatusEnum.MILITARY_MOVE)
                .forEach(o -> o.setActive(false));
        game.getOrders().stream()
                .filter(o -> o.getGameStatus() == GameStatusEnum.MILITARY_MOVE &&
                        o.getPosition() == 0)
                .forEach(o -> o.setActive(true));
        diff = new DiffEntity();
        diff.setIdGame(game.getId());
        diff.setVersionGame(game.getVersion());
        diff.setType(DiffTypeEnum.MODIFY);
        diff.setTypeObject(DiffTypeObjectEnum.TURN_ORDER);
        diff.setIdObject(null);
        diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.ACTIVE);
        diffAttributes.setValue("0");
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);

        diffDao.create(diff);
        diffs.add(diff);

        return diffs;
    }

    protected List<DiffEntity> endRound(GameEntity game) {
        List<DiffEntity> diffs = new ArrayList<>();

        diffs.add(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.GOOD_WEATHER, null, "B_MR_End", game));
        // FIXME Redeployment phase

        return diffs;
    }

    /**
     * Alliance of countries that will move at the same segment.
     */
    protected static class Alliance {
        /** Countries part of the alliance. */
        private List<PlayableCountryEntity> countries;
        /** Initiative of the alliance. */
        private int initiative;

        public Alliance(List<PlayableCountryEntity> countries, int initiative) {
            this.countries = countries;
            this.initiative = initiative;
        }

        /** @return the countries. */
        public List<PlayableCountryEntity> getCountries() {
            return countries;
        }

        /** @return the initiative. */
        public int getInitiative() {
            return initiative;
        }

        /**
         * Try to fusion this with alliance.
         * If this and alliance shares a country, then countries are merged into
         * this and minimum initiative of both.
         * Do nothing if no country is shared.
         *
         * @param alliance to fusion with.
         * @return <code>true</code> if fusion succeeded, <code>false</code> otherwise.
         */
        private boolean fusion(Alliance alliance) {
            boolean fusion = false;

            for (PlayableCountryEntity country : alliance.getCountries()) {
                if (countries.contains(country)) {
                    fusion = true;
                    break;
                }
            }

            if (fusion) {
                for (PlayableCountryEntity country : alliance.getCountries()) {
                    if (!countries.contains(country)) {
                        countries.add(country);
                    }
                }

                initiative = Math.min(initiative, alliance.getInitiative());
            }

            return fusion;
        }
    }
}
