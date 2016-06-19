package com.mkl.eu.client.service.util;

import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
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
}
