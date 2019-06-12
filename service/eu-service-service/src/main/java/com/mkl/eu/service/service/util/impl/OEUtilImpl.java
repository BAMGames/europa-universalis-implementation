package com.mkl.eu.service.service.util.impl;

import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.util.GameUtil;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.ref.Referential;
import com.mkl.eu.client.service.vo.ref.country.CountryReferential;
import com.mkl.eu.client.service.vo.tables.*;
import com.mkl.eu.service.service.persistence.oe.AbstractWithLossEntity;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryInWarEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.BorderEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.EuropeanProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.RotwProvinceEntity;
import com.mkl.eu.service.service.util.ArmyInfo;
import com.mkl.eu.service.service.util.IOEUtil;
import com.mkl.eu.service.service.util.SavableRandom;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import static com.mkl.eu.client.common.util.CommonUtil.EPSILON;
import static com.mkl.eu.client.common.util.CommonUtil.THIRD;

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
    public int rollDie(GameEntity game) {
        return rollDie(game, (PlayableCountryEntity) null);
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
    public List<String> getEnemies(PlayableCountryEntity country, GameEntity game, boolean includeInterventions) {
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
    public String getOwner(AbstractProvinceEntity province, GameEntity game) {
        String owner = null;

        // By default, the owner is the default owner.
        if (province instanceof EuropeanProvinceEntity) {
            owner = ((EuropeanProvinceEntity) province).getDefaultOwner();
        }

        List<CounterEntity> counters = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(province.getName(), stack.getProvince()))
                .flatMap(stack -> stack.getCounters().stream())
                .collect(Collectors.toList());

        // In ROTW, having an establishment means we owns the province.
        owner = counters.stream()
                .filter(counter -> CounterUtil.isEstablishment(counter.getType()))
                .map(CounterEntity::getCountry)
                .findAny()
                .orElse(owner);

        // If the province is owned by another country, then it will be the controller.
        owner = counters.stream()
                .filter(counter -> counter.getType() == CounterFaceTypeEnum.OWN)
                .map(CounterEntity::getCountry)
                .findAny()
                .orElse(owner);

        return owner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getController(AbstractProvinceEntity province, GameEntity game) {
        // By default, the controller is the owner.
        String controller = getOwner(province, game);

        // Except if another country explicitly controls it.
        controller = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(province.getName(), stack.getProvince()))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> counter.getType() == CounterFaceTypeEnum.CONTROL)
                .map(CounterEntity::getCountry)
                .findAny()
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
    public int getNaturalFortressLevel(AbstractProvinceEntity province, GameEntity game) {
        int level = 0;
        if (province instanceof EuropeanProvinceEntity) {
            if (((EuropeanProvinceEntity) province).getFortress() != null) {
                level = ((EuropeanProvinceEntity) province).getFortress();
            }
        } else if (province instanceof RotwProvinceEntity) {
            if (((RotwProvinceEntity) province).getFortress() != null) {
                level = ((RotwProvinceEntity) province).getFortress();
            }
        }

        return level;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFortressLevel(AbstractProvinceEntity province, GameEntity game) {
        int level = getNaturalFortressLevel(province, game);
        if (province instanceof EuropeanProvinceEntity) {
            if (((EuropeanProvinceEntity) province).getFortress() != null) {
                level = ((EuropeanProvinceEntity) province).getFortress();
            }
        } else if (province instanceof RotwProvinceEntity) {
            if (((RotwProvinceEntity) province).getFortress() != null) {
                level = ((RotwProvinceEntity) province).getFortress();
            }
        }
        List<StackEntity> stacks = getStacksOnProvince(game, province.getName());
        String controller = getController(province, game);
        CounterEntity fortressCounter = CommonUtil.findFirst(stacks.stream().flatMap(stack -> stack.getCounters().stream()),
                counter -> (StringUtils.equals(controller, counter.getCountry()) || counter.getCountry() == null) && CounterUtil.isFortress(counter.getType()));
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTechnology(List<CounterEntity> counters, boolean land, Referential referential, Tables tables, GameEntity game) {
        Map<Tech, Double> technoBySize = new TreeMap<>(Comparator.<Tech>reverseOrder());

        BigDecimal totalSize = new BigDecimal("0");
        for (CounterEntity counter : counters) {
            String tech = getTechnology(counter.getCountry(), land, referential, game);
            Tech technology = tables.getTechs().stream()
                    .filter(t -> StringUtils.equals(tech, t.getName()))
                    .findAny()
                    .orElse(null);
            double size = CounterUtil.getSizeFromType(counter.getType());
            CommonUtil.add(technoBySize, technology, size);
            totalSize = totalSize.add(new BigDecimal(size));
        }

        BigDecimal partialSize = new BigDecimal("0");
        String tech = null;
        for (Tech technology : technoBySize.keySet()) {
            tech = technology.getName();
            partialSize = partialSize.add(new BigDecimal(technoBySize.get(technology)));
            if (partialSize.subtract(totalSize.divide(new BigDecimal("2"), 3, BigDecimal.ROUND_CEILING)).doubleValue() > 0) {
                break;
            }
        }

        return tech;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getArtilleryBonus(List<CounterEntity> counters, Referential referential, Tables tables, GameEntity game) {
        return getArtilleryBonus(getArmyInfo(counters, referential), tables, game);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ArmyInfo> getArmyInfo(List<CounterEntity> counters, Referential referential) {
        return counters.stream()
                .map(counter -> {
                    ArmyInfo army = new ArmyInfo();
                    army.setType(counter.getType());
                    army.setCountry(counter.getCountry());
                    ArmyClassEnum armyClass = referential.getCountries().stream()
                            .filter(c -> StringUtils.equals(counter.getCountry(), c.getName()))
                            .map(CountryReferential::getArmyClass)
                            .findAny()
                            .orElse(null);
                    army.setArmyClass(armyClass);
                    return army;
                })
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getArtilleryBonus(List<ArmyInfo> counters, Tables tables, GameEntity game) {
        Period period = CommonUtil.findFirst(tables.getPeriods(), per -> per.getBegin() <= game.getTurn() && per.getEnd() >= game.getTurn());
        List<Integer> artilleries = counters.stream()
                .map(counter -> getNumberArtillery(counter, period, tables))
                .collect(Collectors.toList());

        Integer max = artilleries.stream()
                .max(Comparator.<Integer>naturalOrder())
                .orElse(null);

        if (max == null) {
            return 0;
        }

        artilleries.remove(max);
        int remain = artilleries.stream()
                .max(Comparator.<Integer>naturalOrder())
                .map(art -> art >= 2 ? 2 : art == 1 ? 1 : 0)
                .orElse(0);

        return max + remain;
    }

    /**
     * @param army   the counter.
     * @param period the period.
     * @param tables the tables.
     * @return the number of artillery for the given counter at the given period.
     */
    private Integer getNumberArtillery(ArmyInfo army, Period period, Tables tables) {
        int artillery = 0;
        int factor = 0;

        if (army.getType() == CounterFaceTypeEnum.ARMY_PLUS || army.getType() == CounterFaceTypeEnum.ARMY_TIMAR_PLUS) {
            factor = 1;
        } else if (army.getType() == CounterFaceTypeEnum.ARMY_MINUS || army.getType() == CounterFaceTypeEnum.ARMY_TIMAR_MINUS) {
            factor = 2;
        }

        if (factor != 0) {
            artillery = tables.getArmyArtilleries().stream()
                    .filter(art -> StringUtils.equals(period.getName(), art.getPeriod()) &&
                            StringUtils.equals(army.getCountry(), art.getCountry()))
                    .map(ArmyArtillery::getArtillery)
                    .findAny()
                    .orElse(0);

            if (artillery == 0) {
                artillery = tables.getArmyArtilleries().stream()
                        .filter(art -> StringUtils.equals(period.getName(), art.getPeriod()) &&
                                army.getArmyClass() == art.getArmyClass())
                        .map(ArmyArtillery::getArtillery)
                        .findAny()
                        .orElse(0);
            }

            artillery = artillery / factor;
        }

        return artillery;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getCavalryBonus(List<ArmyInfo> armies, TerrainEnum terrain, Tables tables, GameEntity game) {
        boolean bonus = false;

        Period PI = tables.getPeriods().stream()
                .filter(period -> StringUtils.equals(Period.PERIOD_I, period.getName()))
                .findAny()
                .orElse(null);
        Period PIII = tables.getPeriods().stream()
                .filter(period -> StringUtils.equals(Period.PERIOD_III, period.getName()))
                .findAny()
                .orElse(null);
        Period PIV = tables.getPeriods().stream()
                .filter(period -> StringUtils.equals(Period.PERIOD_IV, period.getName()))
                .findAny()
                .orElse(null);
        Period PV = tables.getPeriods().stream()
                .filter(period -> StringUtils.equals(Period.PERIOD_V, period.getName()))
                .findAny()
                .orElse(null);
        Period PVI = tables.getPeriods().stream()
                .filter(period -> StringUtils.equals(Period.PERIOD_VI, period.getName()))
                .findAny()
                .orElse(null);

        switch (terrain) {
            case PLAIN:
                bonus = between(game.getTurn(), PIII, PV) && armies.stream().anyMatch(army -> hasCavalry(army, ArmyClassEnum.IV));
                bonus |= between(game.getTurn(), PIV, PV) && armies.stream().anyMatch(army -> hasCavalry(army, ArmyClassEnum.IIIM));
                bonus |= between(game.getTurn(), PI, PIV) && armies.stream().anyMatch(army -> hasCavalry(army, ArmyClassEnum.IIM));
                bonus |= armies.stream().anyMatch(army -> hasCavalry(army, PlayableCountry.TURKEY));
                break;
            case DENSE_FOREST:
                bonus = between(game.getTurn(), PIV, PV) && armies.stream().anyMatch(army -> hasCavalry(army, ArmyClassEnum.IIIM));
                bonus |= between(game.getTurn(), PIII, PVI) && armies.stream().anyMatch(army -> hasCavalry(army, PlayableCountry.SWEDEN));
                break;
            case SPARSE_FOREST:
                bonus = between(game.getTurn(), PI, PIV) && armies.stream().anyMatch(army -> hasCavalry(army, ArmyClassEnum.IIM));
                break;
            case DESERT:
                bonus = armies.stream().anyMatch(army -> hasCavalry(army, PlayableCountry.TURKEY));
                break;
        }

        return bonus;
    }

    /**
     * @param turn of the game.
     * @param min  period.
     * @param max  period.
     * @return <code>true</code> if the turn is between the two periods.
     */
    private boolean between(int turn, Period min, Period max) {
        return turn >= min.getBegin() && turn <= max.getEnd();
    }

    /**
     * @param army    the army.
     * @param country the country.
     * @return <code>true</code> if the army has the right type and country.
     */
    private boolean hasCavalry(ArmyInfo army, String country) {
        return CounterUtil.isArmyCounter(army.getType()) && StringUtils.equals(country, army.getCountry());
    }

    /**
     * @param army      the army.
     * @param armyClass the class.
     * @return <code>true</code> if the army has the right type and class.
     */
    private boolean hasCavalry(ArmyInfo army, ArmyClassEnum armyClass) {
        return CounterUtil.isArmyCounter(army.getType()) && armyClass == army.getArmyClass();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getAssaultBonus(List<CounterEntity> armies, Tables tables, GameEntity game) {
        Period PI = tables.getPeriods().stream()
                .filter(period -> StringUtils.equals(Period.PERIOD_I, period.getName()))
                .findAny()
                .orElse(null);
        Period PII = tables.getPeriods().stream()
                .filter(period -> StringUtils.equals(Period.PERIOD_II, period.getName()))
                .findAny()
                .orElse(null);
        Period PIII = tables.getPeriods().stream()
                .filter(period -> StringUtils.equals(Period.PERIOD_III, period.getName()))
                .findAny()
                .orElse(null);

        // TODO reforms of turkey
        return between(game.getTurn(), PI, PII) && armies.stream().anyMatch(army -> hasAssault(army, PlayableCountry.POLAND)) ||
                between(game.getTurn(), PI, PIII) && armies.stream().anyMatch(army -> hasAssault(army, PlayableCountry.RUSSIA)) ||
                between(game.getTurn(), PI, PIII) && armies.stream().anyMatch(army -> hasAssault(army, PlayableCountry.TURKEY));
    }

    /**
     * @param army    the army.
     * @param country the country.
     * @return <code>true</code> if the army is an army counter (not timar) and is of the right country.
     */
    private boolean hasAssault(CounterEntity army, String country) {
        return (army.getType() == CounterFaceTypeEnum.ARMY_PLUS || army.getType() == CounterFaceTypeEnum.ARMY_MINUS) && StringUtils.equals(country, army.getCountry());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRetreat(AbstractProvinceEntity province, boolean inFortress, double stackSize, PlayableCountryEntity country, GameEntity game) {
        String controller = getController(province, game);
        List<String> allies = getAllies(country, game);
        boolean canRetreat = allies.contains(controller);

        if (canRetreat) {
            List<String> enemies = getEnemies(country, game);
            List<StackEntity> stacks = getStacksOnProvince(game, province.getName());
            if (inFortress) {
                int fortressLevel = getFortressLevel(province, game);
                canRetreat = stackSize <= fortressLevel;
            } else {
                canRetreat = !stacks.stream()
                        .anyMatch(s -> !s.isBesieged() && enemies.contains(s.getCountry()) && isMobile(s));
            }
        }

        return canRetreat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStackVeteran(List<CounterEntity> counters) {
        Double veterans = counters.stream()
                .filter(counter -> counter.getVeterans() != null)
                .collect(Collectors.summingDouble(CounterEntity::getVeterans));
        Double total = counters.stream()
                .collect(Collectors.summingDouble(counter -> CounterUtil.getSizeFromType(counter.getType())));

        // TODO pasha always conscript

        return veterans > total / 2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractWithLossEntity lossModificationSize(AbstractWithLossEntity losses, Integer sizeDiff) {
        int third;
        switch (sizeDiff) {
            case -1:
            case 0:
                return losses;
            case 1:
            case 2:
            case 3:
                third = losses.getTotalThird();

                third += (Math.pow(2, sizeDiff - 1) * (third) / 6) + sizeDiff / 2 * ((third % 3) + 1) / 2 + 1 - (third < 3 ? 1 : 0);

                return AbstractWithLossEntity.create(third);
            case -2:
                third = losses.getTotalThird();

                third -= third / 6 + ((third % 3) == 2 ? 1 : 0) + 1 - (third < 3 ? 1 : 0);

                return AbstractWithLossEntity.create(third);
            default:
                throw new TechnicalException(IConstantsCommonException.INVALID_PARAMETER, "size diff " + sizeDiff + " does not exits.", null, "lossModificationSize", "sizeDiff", sizeDiff);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double getArmySize(List<ArmyInfo> counters, Tables tables, GameEntity game) {
        Period period = CommonUtil.findFirst(tables.getPeriods(), per -> per.getBegin() <= game.getTurn() && per.getEnd() >= game.getTurn());
        ToDoubleFunction<ArmyInfo> sizeOfCounter = counter -> {
            ArmyClasse armyClasse = tables.getArmyClasses().stream()
                    .filter(ac -> counter.getArmyClass() == ac.getArmyClass() &&
                            StringUtils.equals(period.getName(), ac.getPeriod()))
                    .findAny()
                    .orElseThrow(null);
            return CounterUtil.getSizeFromType(counter.getType()) * armyClasse.getSize();
        };

        double size = counters.stream()
                .collect(Collectors.summingDouble(sizeOfCounter));

        double nbCounters = counters.stream()
                .collect(Collectors.summingDouble(counter -> CounterUtil.getSizeFromType(counter.getType())));

        return size / nbCounters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getSizeDiff(Double sizeFor, Double sizeAgainst) {
        return new BigDecimal(sizeFor).subtract(new BigDecimal(sizeAgainst)).divide(new BigDecimal("3"), BigDecimal.ROUND_HALF_DOWN).intValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractWithLossEntity lossesMitigation(Double size, boolean land, Supplier<Integer> rollDie) {
        AbstractWithLossEntity lossesReduction = null;
        if (land) {
            if (size >= 8) {
                lossesReduction = AbstractWithLossEntity.create(0);
            } else if (size >= 7) {
                boolean odd = rollDie == null || rollDie.get() % 2 == 0;
                lossesReduction = AbstractWithLossEntity.create(odd ? 1 : 0);
            } else if (size >= 6) {
                lossesReduction = AbstractWithLossEntity.create(1);
            } else if (size >= 4) {
                lossesReduction = AbstractWithLossEntity.create(2);
            } else if (size >= 3) {
                lossesReduction = AbstractWithLossEntity.create(3);
            } else if (size >= 2) {
                lossesReduction = AbstractWithLossEntity.create(4);
            } else if (size + EPSILON >= 1 + THIRD) {
                lossesReduction = AbstractWithLossEntity.create(5);
            } else if (Math.abs(1 - size) <= EPSILON) {
                lossesReduction = AbstractWithLossEntity.create(6);
            } else if (Math.abs(2 * THIRD - size) <= EPSILON) {
                lossesReduction = AbstractWithLossEntity.create(7);
            } else if (Math.abs(THIRD - size) <= EPSILON) {
                lossesReduction = AbstractWithLossEntity.create(9);
            }
        }
        return lossesReduction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractWithLossEntity retreat(Integer modifiedRollDie) {
        // cap die to [1-8)
        int die = Math.max(1, Math.min(8, modifiedRollDie));
        return AbstractWithLossEntity.create((die - 1) / 2);
    }
}
