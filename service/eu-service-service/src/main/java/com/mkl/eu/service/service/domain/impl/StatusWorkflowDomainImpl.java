package com.mkl.eu.service.service.domain.impl;

import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.service.service.domain.IStatusWorkflowDomain;
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
    /** OeUtil. */
    @Autowired
    private IOEUtil oeUtil;

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
