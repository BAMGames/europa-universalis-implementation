package com.mkl.eu.client.service.util;

import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.CultureEnum;
import com.mkl.eu.client.service.vo.enumeration.ForceTypeEnum;

/**
 * Utility around counters.
 *
 * @author MKL.
 */
public final class CounterUtil {

    /**
     * Private constructor.
     */
    private CounterUtil() {

    }

    /**
     * Returns the military size of a counter.
     *
     * @param face the face.
     * @return the military size.
     */
    public static int getSizeFromType(CounterFaceTypeEnum face) {
        int size = 0;

        if (face != null) {
            switch (face) {
                case FLEET_PLUS:
                case ARMY_PLUS:
                    size = 4;
                    break;
                case FLEET_MINUS:
                case ARMY_MINUS:
                    size = 2;
                    break;
                case LAND_DETACHMENT:
                case LAND_DETACHMENT_TIMAR:
                case LAND_DETACHMENT_KOZAK:
                case NAVAL_DETACHMENT:
                case NAVAL_TRANSPORT:
                case NAVAL_GALLEY:
                    size = 1;
                    break;
                default:
                    break;
            }
        }

        return size;
    }

    /**
     * Returns the fortress level of a counter.
     *
     * @param face the face.
     * @return the fortress level.
     */
    public static int getFortressLevelFromType(CounterFaceTypeEnum face) {
        int level = 0;

        if (face != null) {
            switch (face) {
                case FORTRESS_5:
                case ARSENAL_5_ST_PETER:
                    level = 5;
                    break;
                case FORTRESS_4:
                case ARSENAL_4:
                case ARSENAL_4_ST_PETER:
                    level = 4;
                    break;
                case FORTRESS_3:
                case ARSENAL_3:
                case ARSENAL_3_GIBRALTAR:
                case ARSENAL_3_SEBASTOPOL:
                case ARSENAL_3_ST_PETER:
                    level = 3;
                    break;
                case FORTRESS_2:
                case ARSENAL_2:
                case ARSENAL_2_GIBRALTAR:
                case ARSENAL_2_SEBASTOPOL:
                case ARSENAL_2_ST_PETER:
                    level = 2;
                    break;
                case FORTRESS_1:
                case MISSION:
                case ARSENAL_1_ST_PETER:
                    level = 1;
                    break;
                case FORT:
                case ARSENAL_0_ST_PETER:
                    level = 0;
                    break;
                default:
                    break;
            }
        }

        return level;
    }

    /**
     * @param face the face.
     * @return <code>true</code> if the counter face type is a fortress, <code>false</code> otherwise.
     */
    public static boolean isFortress(CounterFaceTypeEnum face) {
        boolean fortress = false;

        if (face != null) {
            switch (face) {
                case FORTRESS_5:
                case ARSENAL_5_ST_PETER:
                case FORTRESS_4:
                case ARSENAL_4:
                case ARSENAL_4_ST_PETER:
                case FORTRESS_3:
                case ARSENAL_3:
                case ARSENAL_3_GIBRALTAR:
                case ARSENAL_3_SEBASTOPOL:
                case ARSENAL_3_ST_PETER:
                case FORTRESS_2:
                case ARSENAL_2:
                case ARSENAL_2_GIBRALTAR:
                case ARSENAL_2_SEBASTOPOL:
                case ARSENAL_2_ST_PETER:
                case FORTRESS_1:
                case MISSION:
                case ARSENAL_1_ST_PETER:
                case FORT:
                case ARSENAL_0_ST_PETER:
                    fortress = true;
                    break;
                default:
                    break;
            }
        }

        return fortress;
    }

    /**
     * @param face the face.
     * @return <code>true</code> if the counter face type is an army (land or naval), <code>false</code> otherwise.
     */
    public static boolean isArmy(CounterFaceTypeEnum face) {
        boolean army = false;

        if (face != null) {
            switch (face) {
                case ARMY_PLUS:
                case ARMY_TIMAR_PLUS:
                case ARMY_MINUS:
                case ARMY_TIMAR_MINUS:
                case LAND_DETACHMENT:
                case LAND_DETACHMENT_EXPLORATION:
                case LAND_DETACHMENT_TIMAR:
                case LAND_DETACHMENT_KOZAK:
                case LAND_DETACHMENT_EXPLORATION_KOZAK:
                case LAND_INDIAN:
                case LAND_SEPOY:
                case LAND_INDIAN_EXPLORATION:
                case LAND_SEPOY_EXPLORATION:
                case FLEET_PLUS:
                case FLEET_MINUS:
                case FLEET_TRANSPORT_PLUS:
                case FLEET_TRANSPORT_MINUS:
                case NAVAL_DETACHMENT:
                case NAVAL_DETACHMENT_EXPLORATION:
                case NAVAL_GALLEY:
                case NAVAL_TRANSPORT:
                    army = true;
                    break;
                default:
                    break;
            }
        }

        return army;
    }

    /**
     * @param face the face.
     * @return <code>true</code> if the counter face type is an army (land, not naval), <code>false</code> otherwise.
     */
    public static boolean isLandArmy(CounterFaceTypeEnum face) {
        boolean army = false;

        if (face != null) {
            switch (face) {
                case ARMY_PLUS:
                case ARMY_TIMAR_PLUS:
                case ARMY_MINUS:
                case ARMY_TIMAR_MINUS:
                case LAND_DETACHMENT:
                case LAND_DETACHMENT_EXPLORATION:
                case LAND_DETACHMENT_TIMAR:
                case LAND_DETACHMENT_KOZAK:
                case LAND_DETACHMENT_EXPLORATION_KOZAK:
                case LAND_INDIAN:
                case LAND_SEPOY:
                case LAND_INDIAN_EXPLORATION:
                case LAND_SEPOY_EXPLORATION:
                    army = true;
                    break;
                default:
                    break;
            }
        }

        return army;
    }

    /**
     * @param face the face.
     * @return <code>true</code> if the counter face type is an army (naval, not land), <code>false</code> otherwise.
     */
    public static boolean isNavalArmy(CounterFaceTypeEnum face) {
        boolean army = false;

        if (face != null) {
            switch (face) {
                case FLEET_PLUS:
                case FLEET_MINUS:
                case FLEET_TRANSPORT_PLUS:
                case FLEET_TRANSPORT_MINUS:
                case NAVAL_DETACHMENT:
                case NAVAL_DETACHMENT_EXPLORATION:
                case NAVAL_GALLEY:
                case NAVAL_TRANSPORT:
                    army = true;
                    break;
                default:
                    break;
            }
        }

        return army;
    }

    /**
     * Transform a force to a size.
     *
     * @param force to transform in size.
     * @return the size.
     */
    public static Double getSizeFromForce(ForceTypeEnum force) {
        Double size = null;

        if (force != null) {
            switch (force) {
                case ARMY_PLUS:
                case ARMY_TIMAR_PLUS:
                case FLEET_PLUS:
                    size = 4d;
                    break;
                case ARMY_MINUS:
                case ARMY_TIMAR_MINUS:
                case FLEET_MINUS:
                    size = 2d;
                    break;
                case LD:
                case LDT:
                case ND:
                case LDND:
                    size = 1d;
                    break;
                case LDE:
                case DE:
                    size = 0.5;
                    break;
                default:
                    break;
            }
        }

        return size;
    }

    /**
     * @param type of the counter to test.
     * @return <code>true</code> if the counter face type is the one of a manufacture, <code>false</code> otherwise.
     */
    public static boolean isManufacture(CounterFaceTypeEnum type) {
        boolean mnu = false;

        if (type != null) {
            switch (type) {
                case MNU_ART_MINUS:
                case MNU_ART_PLUS:
                case MNU_CEREALS_MINUS:
                case MNU_CEREALS_PLUS:
                case MNU_CLOTHES_MINUS:
                case MNU_CLOTHES_PLUS:
                case MNU_FISH_MINUS:
                case MNU_FISH_PLUS:
                case MNU_INSTRUMENTS_MINUS:
                case MNU_INSTRUMENTS_PLUS:
                case MNU_METAL_MINUS:
                case MNU_METAL_PLUS:
                case MNU_METAL_SCHLESIEN_MINUS:
                case MNU_METAL_SCHLESIEN_PLUS:
                case MNU_SALT_MINUS:
                case MNU_SALT_PLUS:
                case MNU_WINE_MINUS:
                case MNU_WINE_PLUS:
                case MNU_WOOD_MINUS:
                case MNU_WOOD_PLUS:
                    mnu = true;
                    break;
                default:
                    break;
            }
        }

        return mnu;
    }

    /**
     * @param type of the counter to test.
     * @return the level of the manufacture (0 if the type is not a manufacture).
     */
    public static int getManufactureLevel(CounterFaceTypeEnum type) {
        int level = 0;

        if (type != null) {
            switch (type) {
                case MNU_ART_MINUS:
                case MNU_CEREALS_MINUS:
                case MNU_CLOTHES_MINUS:
                case MNU_FISH_MINUS:
                case MNU_INSTRUMENTS_MINUS:
                case MNU_METAL_MINUS:
                case MNU_METAL_SCHLESIEN_MINUS:
                case MNU_SALT_MINUS:
                case MNU_WINE_MINUS:
                case MNU_WOOD_MINUS:
                    level = 1;
                    break;
                case MNU_ART_PLUS:
                case MNU_CEREALS_PLUS:
                case MNU_CLOTHES_PLUS:
                case MNU_FISH_PLUS:
                case MNU_INSTRUMENTS_PLUS:
                case MNU_METAL_PLUS:
                case MNU_METAL_SCHLESIEN_PLUS:
                case MNU_SALT_PLUS:
                case MNU_WINE_PLUS:
                case MNU_WOOD_PLUS:
                    level = 2;
                    break;
                default:
                    break;
            }
        }

        return level;
    }

    /**
     * @param type of the counter to test.
     * @return the level one of the manufacture, if the type is a manufacture.
     */
    public static CounterFaceTypeEnum getManufactureLevel1(CounterFaceTypeEnum type) {
        CounterFaceTypeEnum mnu = null;

        if (type != null) {
            switch (type) {
                case MNU_ART_MINUS:
                case MNU_ART_PLUS:
                    mnu = CounterFaceTypeEnum.MNU_ART_MINUS;
                    break;
                case MNU_CEREALS_MINUS:
                case MNU_CEREALS_PLUS:
                    mnu = CounterFaceTypeEnum.MNU_CEREALS_MINUS;
                    break;
                case MNU_CLOTHES_MINUS:
                case MNU_CLOTHES_PLUS:
                    mnu = CounterFaceTypeEnum.MNU_CLOTHES_MINUS;
                    break;
                case MNU_FISH_MINUS:
                case MNU_FISH_PLUS:
                    mnu = CounterFaceTypeEnum.MNU_FISH_MINUS;
                    break;
                case MNU_INSTRUMENTS_MINUS:
                case MNU_INSTRUMENTS_PLUS:
                    mnu = CounterFaceTypeEnum.MNU_INSTRUMENTS_MINUS;
                    break;
                case MNU_METAL_MINUS:
                case MNU_METAL_PLUS:
                    mnu = CounterFaceTypeEnum.MNU_METAL_MINUS;
                    break;
                case MNU_METAL_SCHLESIEN_MINUS:
                case MNU_METAL_SCHLESIEN_PLUS:
                    mnu = CounterFaceTypeEnum.MNU_METAL_SCHLESIEN_MINUS;
                    break;
                case MNU_SALT_MINUS:
                case MNU_SALT_PLUS:
                    mnu = CounterFaceTypeEnum.MNU_SALT_MINUS;
                    break;
                case MNU_WINE_MINUS:
                case MNU_WINE_PLUS:
                    mnu = CounterFaceTypeEnum.MNU_WINE_MINUS;
                    break;
                case MNU_WOOD_MINUS:
                case MNU_WOOD_PLUS:
                    mnu = CounterFaceTypeEnum.MNU_WOOD_MINUS;
                    break;
                default:
                    break;
            }
        }

        return mnu;
    }

    /**
     * @param type of the counter to test.
     * @return the level two of the manufacture, if the type is a manufacture.
     */
    public static CounterFaceTypeEnum getManufactureLevel2(CounterFaceTypeEnum type) {
        CounterFaceTypeEnum mnu = null;

        if (type != null) {
            switch (type) {
                case MNU_ART_MINUS:
                case MNU_ART_PLUS:
                    mnu = CounterFaceTypeEnum.MNU_ART_PLUS;
                    break;
                case MNU_CEREALS_MINUS:
                case MNU_CEREALS_PLUS:
                    mnu = CounterFaceTypeEnum.MNU_CEREALS_PLUS;
                    break;
                case MNU_CLOTHES_MINUS:
                case MNU_CLOTHES_PLUS:
                    mnu = CounterFaceTypeEnum.MNU_CLOTHES_PLUS;
                    break;
                case MNU_FISH_MINUS:
                case MNU_FISH_PLUS:
                    mnu = CounterFaceTypeEnum.MNU_FISH_PLUS;
                    break;
                case MNU_INSTRUMENTS_MINUS:
                case MNU_INSTRUMENTS_PLUS:
                    mnu = CounterFaceTypeEnum.MNU_INSTRUMENTS_PLUS;
                    break;
                case MNU_METAL_MINUS:
                case MNU_METAL_PLUS:
                    mnu = CounterFaceTypeEnum.MNU_METAL_PLUS;
                    break;
                case MNU_METAL_SCHLESIEN_MINUS:
                case MNU_METAL_SCHLESIEN_PLUS:
                    mnu = CounterFaceTypeEnum.MNU_METAL_SCHLESIEN_PLUS;
                    break;
                case MNU_SALT_MINUS:
                case MNU_SALT_PLUS:
                    mnu = CounterFaceTypeEnum.MNU_SALT_PLUS;
                    break;
                case MNU_WINE_MINUS:
                case MNU_WINE_PLUS:
                    mnu = CounterFaceTypeEnum.MNU_WINE_PLUS;
                    break;
                case MNU_WOOD_MINUS:
                case MNU_WOOD_PLUS:
                    mnu = CounterFaceTypeEnum.MNU_WOOD_PLUS;
                    break;
                default:
                    break;
            }
        }

        return mnu;
    }

    /**
     * @param type of the counter to test.
     * @return <code>true</code> if the counter face type is the one of an arsenal, <code>false</code> otherwise.
     */
    public static boolean isArsenal(CounterFaceTypeEnum type) {
        boolean arsenal = false;

        if (type != null) {
            switch (type) {
                case ARSENAL_0_ST_PETER:
                case ARSENAL_1_ST_PETER:
                case ARSENAL_2:
                case ARSENAL_2_ST_PETER:
                case ARSENAL_2_SEBASTOPOL:
                case ARSENAL_2_GIBRALTAR:
                case ARSENAL_3:
                case ARSENAL_3_ST_PETER:
                case ARSENAL_3_SEBASTOPOL:
                case ARSENAL_3_GIBRALTAR:
                case ARSENAL_4:
                case ARSENAL_4_ST_PETER:
                case ARSENAL_5_ST_PETER:
                    arsenal = true;
                    break;
                default:
                    break;
            }
        }

        return arsenal;
    }

    /**
     * @param type of the counter to test.
     * @return <code>true</code> if the counter face type is the one of a force, <code>false</code> otherwise.
     */
    public static boolean isForce(CounterFaceTypeEnum type) {
        boolean force = false;

        if (type != null) {
            switch (type) {
                case FORT:
                case TRADING_POST_MINUS:
                case TRADING_POST_PLUS:
                case ARMY_MINUS:
                case ARMY_PLUS:
                case ARMY_TIMAR_MINUS:
                case ARMY_TIMAR_PLUS:
                case LAND_DETACHMENT:
                case LAND_DETACHMENT_KOZAK:
                case LAND_DETACHMENT_TIMAR:
                case LAND_DETACHMENT_EXPLORATION:
                case LAND_DETACHMENT_EXPLORATION_KOZAK:
                case LAND_INDIAN:
                case LAND_INDIAN_EXPLORATION:
                case LAND_SEPOY:
                case LAND_SEPOY_EXPLORATION:
                    force = true;
                    break;
                default:
                    break;
            }
        }

        return force;
    }

    /**
     * @param type of the counter to test.
     * @return <code>true</code> if the counter face type is the one of a neutral technology, <code>false</code> otherwise.
     */
    public static boolean isNeutralTechnology(CounterFaceTypeEnum type) {
        boolean tech = false;

        if (type != null) {
            switch (type) {
                /** Land technologies. */
                case TECH_RENAISSANCE:
                case TECH_ARQUEBUS:
                case TECH_MUSKET:
                case TECH_BAROQUE:
                case TECH_MANOEUVRE:
                case TECH_LACE_WAR:
                    /** Naval technologies. */
                case TECH_NAE_GALEON:
                case TECH_GALLEON_FLUYT:
                case TECH_BATTERY:
                case TECH_VESSEL:
                case TECH_THREE_DECKER:
                case TECH_SEVENTY_FOUR:
                    /** Special technologies. */
                case TECH_TERCIO:
                case TECH_GALLEASS:
                    tech = true;
                    break;
                default:
                    break;
            }
        }

        return tech;
    }

    /**
     * @param tech counter face type of the technology counter.
     * @return the name of the technology counter given the counter face type.
     */
    public static String getTechnologyName(CounterFaceTypeEnum tech) {
        String name = null;

        if (isNeutralTechnology(tech)) {
            name = tech.name().substring(5);
        }

        return name;
    }

    /**
     * @param tech the name of the technology.
     * @return the counter face type of a technology counter given the technology name.
     */
    public static CounterFaceTypeEnum getTechnologyType(String tech) {
        CounterFaceTypeEnum type;
        try {
            type = CounterFaceTypeEnum.valueOf("TECH_" + tech);
        } catch (IllegalArgumentException e) {
            type = null;
        }

        return type;
    }

    /**
     * @param culture group.
     * @param land    <code>true</code> for land tech, <code>false</code> for naval tech.
     * @return the technology group from culture.
     */
    public static CounterFaceTypeEnum getTechnologyGroup(CultureEnum culture, boolean land) {
        CounterFaceTypeEnum tech = null;

        if (culture != null) {
            if (land) {
                switch (culture) {
                    case LATIN:
                        tech = CounterFaceTypeEnum.TECH_LAND_LATIN;
                        break;
                    case ISLAM:
                        tech = CounterFaceTypeEnum.TECH_LAND_ISLAM;
                        break;
                    case ORTHODOX:
                        tech = CounterFaceTypeEnum.TECH_LAND_ORTHODOX;
                        break;
                    case ROTW:
                        tech = CounterFaceTypeEnum.TECH_LAND_ASIA;
                        break;
                }
            } else {
                switch (culture) {
                    case LATIN:
                        tech = CounterFaceTypeEnum.TECH_NAVAL_LATIN;
                        break;
                    case ISLAM:
                        tech = CounterFaceTypeEnum.TECH_NAVAL_ISLAM;
                        break;
                    case ORTHODOX:
                        tech = CounterFaceTypeEnum.TECH_NAVAL_ORTHODOX;
                        break;
                    case ROTW:
                        tech = CounterFaceTypeEnum.TECH_NAVAL_ASIA;
                        break;
                }
            }
        }

        return tech;
    }

    /**
     * @param type of the counter to test.
     * @param land if we want to know if the counter can stack with a land technology counter (naval technology counter if <code>false</code>).
     * @return <code>true</code> if the counter face type can be stack with a technology counter of the given type, <code>false</code> otherwise.
     */
    public static boolean canTechnologyStack(CounterFaceTypeEnum type, boolean land) {
        boolean stack = true;

        if (type != null) {
            switch (type) {
                /** Land technologies cannot stack with land technology counters. */
                case TECH_RENAISSANCE:
                case TECH_ARQUEBUS:
                case TECH_MUSKET:
                case TECH_BAROQUE:
                case TECH_MANOEUVRE:
                case TECH_LACE_WAR:
                    stack = !land;
                    break;
                /** Naval technologies cannot stack with naval technology counters. */
                case TECH_NAE_GALEON:
                case TECH_GALLEON_FLUYT:
                case TECH_BATTERY:
                case TECH_VESSEL:
                case TECH_THREE_DECKER:
                case TECH_SEVENTY_FOUR:
                    stack = land;
                    break;
                /** Special technologies can stack with all. */
                case TECH_TERCIO:
                case TECH_GALLEASS:
                default:
                    break;
            }
        }

        return stack;
    }

    /**
     * @param type of the counter to test.
     * @return <code>true</code> if the counter face type is an establishment, <code>false</code> otherwise.
     */
    public static boolean isEstablishment(CounterFaceTypeEnum type) {
        boolean establishment = false;

        if (type != null) {
            switch (type) {
                case TRADING_POST_MINUS:
                case TRADING_POST_PLUS:
                case COLONY_MINUS:
                case COLONY_PLUS:
                case MINOR_ESTABLISHMENT_MINUS:
                case MINOR_ESTABLISHMENT_PLUS:
                    establishment = true;
                    break;
                default:
                    break;
            }
        }

        return establishment;
    }
}
