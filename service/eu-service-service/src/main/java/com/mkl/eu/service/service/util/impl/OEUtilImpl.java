package com.mkl.eu.service.service.util.impl;

import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.service.util.GameUtil;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.TerrainEnum;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.BorderEntity;
import com.mkl.eu.service.service.util.IOEUtil;
import com.mkl.eu.service.service.util.SavableRandom;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Utility for OE class.
 *
 * @author MKL.
 */
@Component
public final class OEUtilImpl implements IOEUtil {

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAdministrativeValue(PlayableCountryEntity country) {
        int adm = 3;
        if (country != null && country.getMonarch() != null && country.getMonarch().getAdministrative() != null) {
            adm = country.getMonarch().getAdministrative();
        }
        return adm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMilitaryValue(PlayableCountryEntity country) {
        int mil = 3;
        if (country != null && country.getMonarch() != null && country.getMonarch().getMilitary() != null) {
            mil = country.getMonarch().getMilitary();
        }
        return mil;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStability(GameEntity game, String country) {
        int stab = 0;
        if (game != null) {
            CounterEntity stabCounter = CommonUtil.findFirst(game.getStacks().stream().filter(stack -> GameUtil.isStabilityBox(stack.getProvince()))
                            .flatMap(stack -> stack.getCounters().stream()),
                    counter -> StringUtils.equals(country, counter.getCountry()) && counter.getType() == CounterFaceTypeEnum.STABILITY);
            if (stabCounter != null) {
                String box = stabCounter.getOwner().getProvince();
                stab = GameUtil.getStability(box);
            }
        }
        return stab;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canSettle(AbstractProvinceEntity province, List<String> discoveries, List<String> sources, List<String> friendlies) {
        return settleDistance(province, discoveries, sources, friendlies, 0) <= 12;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int rollDie(GameEntity game, String country) {
        PlayableCountryEntity countryEntity = CommonUtil.findFirst(game.getCountries(), c -> StringUtils.equals(country, c.getName()));
        return rollDie(game, countryEntity);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int rollDie(GameEntity game, PlayableCountryEntity country) {
        /**
         * For the moment, only one seed is stored in tha game.
         * But it will be easy to switch to a seed per player basis later on.
         */
        long seed = game.getSeed();

        SavableRandom rand = new SavableRandom();
        rand.setSeed(seed);
        int die = rand.nextInt(10) + 1;
        game.setSeed(rand.getSeed());

        return die;
    }

    /**
     * @param province    the province to settle.
     * @param discoveries the provinces that have been discovered.
     * @param sources     the sources (COL/TP/Owned european province) of supply of the settlement.
     * @param friendlies  the friendly terrains.
     * @param supply      the supply distance already covered.
     * @return the supply distance for a settlement.
     */
    protected int settleDistance(AbstractProvinceEntity province, List<String> discoveries, List<String> sources, List<String> friendlies, int supply) {
        int distance = -1;

        // TODO enemy forces are ignored, should it be the case ?
        if (province != null && discoveries != null && discoveries.contains(province.getName())) {
            if (sources != null && sources.contains(province.getName())) {
                distance = supply;
                if (province.getTerrain() == TerrainEnum.SWAMP) {
                    distance = supply + 2;
                }
            } else if (province.getBorders() != null) {
                int defaultSupply = 6;
                if (province.getTerrain() == TerrainEnum.PLAIN) {
                    if (friendlies != null && friendlies.contains(province.getName())) {
                        defaultSupply = 2;
                    } else {
                        defaultSupply = 4;
                    }
                } else if (province.getTerrain() == TerrainEnum.SWAMP) {
                    defaultSupply = 8;
                }
                if (supply + defaultSupply <= 12) {
                    for (BorderEntity border : province.getBorders()) {
                        int distanceTmp;
                        AbstractProvinceEntity other = border.getProvinceTo();
                        if (border.getType() == null) {
                            if (other.getTerrain() == TerrainEnum.SEA) {
                                if (supply == 0 && discoveries.contains(other.getName())) {
                                    distanceTmp = 0;
                                } else {
                                    distanceTmp = -1;
                                }
                            } else {
                                distanceTmp = settleDistance(other, discoveries, sources, friendlies, defaultSupply + supply);
                            }
                        } else {
                            switch (border.getType()) {
                                case BERING_STRAIT:
                                    if (supply == 0) {
                                        distanceTmp = 12;
                                    } else {
                                        distanceTmp = -1;
                                    }
                                    break;
                                case PASS:
                                case RIVER:
                                    distanceTmp = settleDistance(other, discoveries, sources, friendlies, defaultSupply + supply);
                                    if (distanceTmp != -1) {
                                        distanceTmp += 2;
                                    }
                                    break;
                                default:
                                    distanceTmp = -1;
                                    break;
                            }
                        }

                        if (distanceTmp != -1 && distanceTmp <= 12 && (distance == -1 || distanceTmp < distance)) {
                            distance = distanceTmp;
                        }
                    }
                }
            }
        }

        return distance;
    }
}
