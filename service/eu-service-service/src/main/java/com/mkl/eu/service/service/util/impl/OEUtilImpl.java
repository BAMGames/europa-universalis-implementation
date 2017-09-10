package com.mkl.eu.service.service.util.impl;

import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.util.GameUtil;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.ref.Referential;
import com.mkl.eu.client.service.vo.ref.country.CountryReferential;
import com.mkl.eu.client.service.vo.tables.Period;
import com.mkl.eu.client.service.vo.tables.Tables;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryInWarEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.BorderEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.EuropeanProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.RotwProvinceEntity;
import com.mkl.eu.service.service.util.IOEUtil;
import com.mkl.eu.service.service.util.SavableRandom;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public int getDiplomaticValue(PlayableCountryEntity country) {
        int dip = 3;
        if (country != null && country.getMonarch() != null && country.getMonarch().getDiplomacy() != null) {
            dip = country.getMonarch().getDiplomacy();
        }
        return dip;
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
    public int getInitiative(PlayableCountryEntity country) {
        return getAdministrativeValue(country) + getDiplomaticValue(country)
                + getMilitaryValue(country);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFti(GameEntity game, Tables tables, String country) {
        // No country, no FTI
        if (StringUtils.isEmpty(country)) {
            return 0;
        }
        // Default value for fti is 2
        int fti = 2;
        // If the country is a major, then we take its own fti value
        PlayableCountryEntity major = CommonUtil.findFirst(game.getCountries(), c -> StringUtils.equals(c.getName(), country));
        if (major != null) {
            fti = major.getFti();
        } else {
            boolean commercialMinor = Arrays.binarySearch(MERCANTILE_COUNTRIES, country) >= 0;
            Period period = CommonUtil.findFirst(tables.getPeriods(), per -> per.getBegin() <= game.getTurn() && per.getEnd() >= game.getTurn());
            boolean period4OrMore = period != null && period.getName().compareTo(Period.PERIOD_IV) >= 0;
            // Mercantile minor countries have a FTI of 3 in period I-III then 4. The others have a FTI of 2 then 3
            if (commercialMinor) {
                fti++;
            }
            if (period4OrMore) {
                fti++;
            }
        }

        return fti;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDti(GameEntity game, Tables tables, String country) {
        // If the country is a major, then we take its own dti value
        PlayableCountryEntity major = CommonUtil.findFirst(game.getCountries(), c -> StringUtils.equals(c.getName(), country));
        if (major != null) {
            return major.getDti();
        } else {
            if (StringUtils.equals("provincesne", country) || StringUtils.equals("hollande", country)) {
                // Hollande has a DTI of 4
                return 4;
            } else {
                // Other minor countries have a DTI equals to their FTI
                return getFti(game, tables, country);
            }
        }
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
    public int getTechnologyAdvance(GameEntity game, String country, boolean land) {
        int tech = 0;
        CounterFaceTypeEnum face;
        if (land) {
            face = CounterFaceTypeEnum.TECH_LAND;
        } else {
            face = CounterFaceTypeEnum.TECH_NAVAL;
        }
        if (game != null) {
            CounterEntity techCounter = CommonUtil.findFirst(game.getStacks().stream().filter(stack -> GameUtil.isTechnologyBox(stack.getProvince()))
                            .flatMap(stack -> stack.getCounters().stream()),
                    counter -> StringUtils.equals(country, counter.getCountry()) && counter.getType() == face);
            if (techCounter != null) {
                String box = techCounter.getOwner().getProvince();
                tech = GameUtil.getTechnology(box);
            }
        }
        return tech;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canSettle(AbstractProvinceEntity province, List<String> discoveries, List<String> sources, List<String> friendlies) {
        return settleDistance(province, discoveries, sources, friendlies, 0) != -1;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<StackEntity> getStacksOnProvince(GameEntity game, String province) {
        return game.getStacks().stream().filter(s -> StringUtils.equals(s.getProvince(), province))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WarStatusEnum getWarStatus(GameEntity game, PlayableCountryEntity country) {
        if (game == null || country == null) {
            return null;
        }

        WarStatusEnum status = WarStatusEnum.PEACE;

        List<CountryInWarEntity> implications = game.getWars().stream()
                .flatMap(war -> war.getCountries().stream())
                .filter(countryInWar -> StringUtils.equals(country.getName(), countryInWar.getCountry().getName()))
                .collect(Collectors.toList());

        for (CountryInWarEntity implication : implications) {
            WarStatusEnum tmpStatus = null;
            if (implication.getImplication() != null) {
                switch (implication.getImplication()) {
                    case FOREIGN:
                        tmpStatus = WarStatusEnum.FOREIGN_INTERVENTION;
                        break;
                    case LIMITED:
                        tmpStatus = WarStatusEnum.LIMITED_INTERVENTION;
                        break;
                    case FULL:
                        tmpStatus = WarStatusEnum.CLASSIC_WAR;
                        break;
                    default:
                        break;
                }
            }

            if (tmpStatus == WarStatusEnum.CLASSIC_WAR) {
                if (implication.getWar().getType() != null) {
                    switch (implication.getWar().getType()) {
                        case CIVIL_WAR:
                            tmpStatus = WarStatusEnum.CIVIL_WAR;
                            break;
                        case RELIGIOUS_WAR:
                            tmpStatus = WarStatusEnum.RELIGIOUS_WAR;
                            break;
                        case CLASSIC_WAR:
                        default:
                            break;
                    }
                }
            }

            if (tmpStatus != null && tmpStatus.compareTo(status) > 0) {
                status = tmpStatus;
            }
        }

        return status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getEnemies(GameEntity game, PlayableCountryEntity country, boolean includeInterventions) {
        List<String> enemies = new ArrayList<>();

        if (game == null || country == null) {
            return enemies;
        }

        List<CountryInWarEntity> implications = game.getWars().stream()
                .flatMap(war -> war.getCountries().stream())
                .filter(countryInWar -> StringUtils.equals(country.getName(), countryInWar.getCountry().getName()) &&
                        countryInWar.getImplication() == WarImplicationEnum.FULL)
                .collect(Collectors.toList());

        for (CountryInWarEntity implication : implications) {
            implication.getWar().getCountries().stream()
                    .filter(countryInWar -> countryInWar.isOffensive() != implication.isOffensive() && !enemies.contains(countryInWar.getCountry().getName())
                            && (includeInterventions || countryInWar.getImplication() == WarImplicationEnum.FULL))
                    .forEach(countryInWar -> enemies.add(countryInWar.getCountry().getName()));
        }

        return enemies;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMobile(StackEntity stack) {
        return !(stack == null || stack.getCounters().isEmpty()) && !stack.isBesieged() &&
                stack.getCounters().stream().filter(c -> !CounterUtil.isMobile(c.getType())).count() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMovePoints(AbstractProvinceEntity provinceFrom, AbstractProvinceEntity provinceTo, boolean provinceToFriendly) {
        if (provinceFrom == provinceTo) {
            return 0;
        }

        boolean provinceFromOk = provinceFrom instanceof EuropeanProvinceEntity || provinceFrom instanceof RotwProvinceEntity;
        boolean provinceToOk = provinceTo instanceof EuropeanProvinceEntity || provinceTo instanceof RotwProvinceEntity;
        boolean provinceToRotw = provinceTo instanceof RotwProvinceEntity;

        if (!provinceFromOk || !provinceToOk) {
            return -1;
        }

        int move = 0;

        if (provinceFrom.getTerrain() == TerrainEnum.SWAMP) {
            if (provinceFrom instanceof RotwProvinceEntity) {
                move += 2;
            } else {
                move += 1;
            }
        }
        if (provinceTo.getTerrain() == TerrainEnum.SWAMP) {
            if (provinceToRotw) {
                move += 2;
            } else {
                move += 1;
            }
        }

        boolean found = false;
        for (BorderEntity border : provinceFrom.getBorders()) {
            if (border.getProvinceTo() == provinceTo) {
                found = true;
                switch (provinceTo.getTerrain()) {
                    case PLAIN:
                        if (provinceToRotw) {
                            if (provinceToFriendly) {
                                move += 2;
                            } else {
                                move += 4;
                            }
                        } else {
                            if (provinceToFriendly) {
                                move += 1;
                            } else {
                                move += 2;
                            }
                        }
                        break;
                    case MOUNTAIN:
                        if (!provinceToFriendly) {
                            if (provinceToRotw) {
                                move += 6;
                            } else {
                                move += 3;
                            }
                            break;
                        }
                    case SWAMP:
                    case DENSE_FOREST:
                    case DESERT:
                    case SPARSE_FOREST:
                        if (provinceToRotw) {
                            move += 6;
                        } else {
                            move += 2;
                        }
                        break;
                }

                if (border.getType() != null) {
                    switch (border.getType()) {
                        case STRAIT:
                        case PASS:
                        case RIVER:
                            if (provinceToRotw) {
                                move += 2;
                            } else {
                                move += 1;
                            }
                            break;
                        case BERING_STRAIT:
                            move = 12;
                            break;
                    }
                }
            }
        }

        if (!found) {
            move = -1;
        }

        return move;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getController(AbstractProvinceEntity province, GameEntity game) {
        String controller = null;

        // By default, the controller is the default owner.
        if (province instanceof EuropeanProvinceEntity) {
            controller = ((EuropeanProvinceEntity) province).getDefaultOwner();
        }

        List<CounterEntity> counters = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(province.getName(), stack.getProvince()))
                .flatMap(stack -> stack.getCounters().stream())
                .collect(Collectors.toList());

        // In ROTW, having an establishment means we owns the province.
        controller = counters.stream()
                .filter(counter -> CounterUtil.isEstablishment(counter.getType()))
                .map(CounterEntity::getCountry)
                .findFirst()
                .orElse(controller);

        // If the province is owned by another country, then it will be the controller.
        controller = counters.stream()
                .filter(counter -> counter.getType() == CounterFaceTypeEnum.OWN)
                .map(CounterEntity::getCountry)
                .findFirst()
                .orElse(controller);

        // Except if another country explicitly controls it.
        controller = counters.stream()
                .filter(counter -> counter.getType() == CounterFaceTypeEnum.CONTROL)
                .map(CounterEntity::getCountry)
                .findFirst()
                .orElse(controller);

        return controller;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAllies(PlayableCountryEntity country, GameEntity game) {
        List<String> allies = getCountries(country.getName(), game, true);
        if (!allies.contains(country.getName())) {
            allies.add(country.getName());
        }

        return allies;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getEnemies(PlayableCountryEntity country, GameEntity game) {
        return getCountries(country.getName(), game, false);
    }

    /**
     * @param name   of the asking country.
     * @param game   the game.
     * @param allies wether we want the allies or the enemies.
     * @return the List of countries allies or enemies with.
     */
    private List<String> getCountries(String name, GameEntity game, boolean allies) {
        List<String> countries = new ArrayList<>();

        game.getWars().stream()
                .flatMap(war -> war.getCountries().stream())
                .filter(warCountry -> warCountry.getImplication() == WarImplicationEnum.FULL &&
                        StringUtils.equals(warCountry.getCountry().getName(), name))
                .forEach(warCountry -> warCountry.getWar().getCountries().stream()
                        .filter(otherCountry -> otherCountry.getImplication() == WarImplicationEnum.FULL &&
                                (otherCountry.isOffensive() == warCountry.isOffensive() && allies ||
                                        otherCountry.isOffensive() != warCountry.isOffensive() && !allies))
                        .forEach(otherCountry -> {
                            if (!countries.contains(otherCountry.getCountry().getName())) {
                                countries.add(otherCountry.getCountry().getName());
                            }
                        }));

        return countries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFortressLevel(AbstractProvinceEntity province, GameEntity game) {
        int level = 0;
        if (province instanceof EuropeanProvinceEntity) {
            if (((EuropeanProvinceEntity) province).getFortress() != null) {
                level = ((EuropeanProvinceEntity) province).getFortress();
            }
        }
        List<StackEntity> stacks = getStacksOnProvince(game, province.getName());
        String controller = getController(province, game);
        CounterEntity fortressCounter = CommonUtil.findFirst(stacks.stream().flatMap(stack -> stack.getCounters().stream()),
                counter -> StringUtils.equals(controller, counter.getCountry()) && CounterUtil.isFortress(counter.getType()));
        if (fortressCounter != null) {
            level = CounterUtil.getFortressLevelFromType(fortressCounter.getType());
        }

        return level;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTechnology(String country, boolean land, Referential referential, GameEntity game) {
        String technology = game.getCountries().stream()
                .filter(c -> StringUtils.equals(country, c.getName()))
                .map(c -> land ? c.getLandTech() : c.getNavalTech())
                .findAny()
                .orElse(null);

        if (technology == null) {
            CultureEnum culture = referential.getCountries().stream()
                    .filter(c -> StringUtils.equals(country, c.getName()))
                    .map(CountryReferential::getCulture)
                    .findAny()
                    .orElse(null);

            Map<CultureEnum, String> techs;
            if (land) {
                techs = game.getMinorLandTechnologies();
            } else {
                techs = game.getMinorNavalTechnologies();
            }

            technology = techs.get(culture);
        }

        return technology;
    }
}
