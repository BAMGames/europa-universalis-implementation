package com.mkl.eu.client.service.util;

import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.tables.Leader;

import java.util.ArrayList;
import java.util.List;

import static com.mkl.eu.client.common.util.CommonUtil.THIRD;

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
    public static double getSizeFromType(CounterFaceTypeEnum face) {
        double size = 0;

        if (face != null) {
            switch (face) {
                case FLEET_PLUS:
                case ARMY_PLUS:
                    size = 4;
                    break;
                case PACHA_3:
                    size = 3;
                    break;
                case FLEET_MINUS:
                case ARMY_MINUS:
                case PACHA_2:
                    size = 2;
                    break;
                case LAND_DETACHMENT:
                case LAND_DETACHMENT_TIMAR:
                case LAND_DETACHMENT_KOZAK:
                case NAVAL_DETACHMENT:
                case NAVAL_TRANSPORT:
                case NAVAL_GALLEY:
                case PACHA_1:
                    size = 1;
                    break;
                case NAVAL_DETACHMENT_EXPLORATION:
                case LAND_DETACHMENT_EXPLORATION:
                case LAND_DETACHMENT_EXPLORATION_KOZAK:
                case LAND_INDIAN_EXPLORATION:
                case LAND_SEPOY_EXPLORATION:
                    size = THIRD;
                    break;
                default:
                    break;
            }
        }

        return size;
    }

    /**
     * @param face the face.
     * @return an face counter equivalent to the face but of size 2.
     */
    public static CounterFaceTypeEnum getSize2FromType(CounterFaceTypeEnum face) {
        CounterFaceTypeEnum face2 = null;

        if (face != null) {
            switch (face) {
                case ARMY_PLUS:
                case ARMY_MINUS:
                case LAND_DETACHMENT:
                case LAND_DETACHMENT_EXPLORATION:
                    face2 = CounterFaceTypeEnum.ARMY_MINUS;
                    break;
                case ARMY_TIMAR_PLUS:
                case ARMY_TIMAR_MINUS:
                case LAND_DETACHMENT_TIMAR:
                    face2 = CounterFaceTypeEnum.ARMY_TIMAR_MINUS;
                    break;
                case LAND_DETACHMENT_KOZAK:
                case LAND_DETACHMENT_EXPLORATION_KOZAK:
                case LAND_INDIAN:
                case LAND_INDIAN_EXPLORATION:
                case LAND_SEPOY:
                case LAND_SEPOY_EXPLORATION:
                    // No counter of size 2 for them
                    break;
                case FLEET_PLUS:
                case FLEET_MINUS:
                case NAVAL_DETACHMENT:
                case NAVAL_DETACHMENT_EXPLORATION:
                    face2 = CounterFaceTypeEnum.FLEET_MINUS;
                    break;
                case FLEET_TRANSPORT_PLUS:
                case FLEET_TRANSPORT_MINUS:
                case NAVAL_TRANSPORT:
                    face2 = CounterFaceTypeEnum.FLEET_TRANSPORT_MINUS;
                    break;
                case NAVAL_GALLEY:
                    break;
            }
        }

        return face2;
    }

    /**
     * @param face the face.
     * @return an face counter equivalent to the face but of size 1.
     */
    public static CounterFaceTypeEnum getSize1FromType(CounterFaceTypeEnum face) {
        CounterFaceTypeEnum face2 = null;

        if (face != null) {
            switch (face) {
                case ARMY_PLUS:
                case ARMY_MINUS:
                case LAND_DETACHMENT:
                case LAND_DETACHMENT_EXPLORATION:
                    face2 = CounterFaceTypeEnum.LAND_DETACHMENT;
                    break;
                case ARMY_TIMAR_PLUS:
                case ARMY_TIMAR_MINUS:
                case LAND_DETACHMENT_TIMAR:
                    face2 = CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR;
                    break;
                case LAND_DETACHMENT_KOZAK:
                case LAND_DETACHMENT_EXPLORATION_KOZAK:
                    face2 = CounterFaceTypeEnum.LAND_DETACHMENT_KOZAK;
                    break;
                case LAND_INDIAN:
                case LAND_INDIAN_EXPLORATION:
                    face2 = CounterFaceTypeEnum.LAND_INDIAN;
                    break;
                case LAND_SEPOY:
                case LAND_SEPOY_EXPLORATION:
                    face2 = CounterFaceTypeEnum.LAND_SEPOY;
                    break;
                case FLEET_PLUS:
                case FLEET_MINUS:
                case NAVAL_DETACHMENT:
                case NAVAL_DETACHMENT_EXPLORATION:
                    face2 = CounterFaceTypeEnum.NAVAL_DETACHMENT;
                    break;
                case FLEET_TRANSPORT_PLUS:
                case FLEET_TRANSPORT_MINUS:
                case NAVAL_TRANSPORT:
                    face2 = CounterFaceTypeEnum.NAVAL_TRANSPORT;
                    break;
                case NAVAL_GALLEY:
                    face2 = CounterFaceTypeEnum.NAVAL_GALLEY;
                    break;
            }
        }

        return face2;
    }

    /**
     * @param face the face.
     * @return an face counter equivalent to the face but of size third (exploration).
     */
    public static CounterFaceTypeEnum getSizeThirdFromType(CounterFaceTypeEnum face) {
        CounterFaceTypeEnum face2 = null;

        if (face != null) {
            switch (face) {
                case ARMY_PLUS:
                case ARMY_MINUS:
                case LAND_DETACHMENT:
                case LAND_DETACHMENT_EXPLORATION:
                    face2 = CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION;
                    break;
                case ARMY_TIMAR_PLUS:
                case ARMY_TIMAR_MINUS:
                case LAND_DETACHMENT_TIMAR:
                    face2 = CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION;
                    break;
                case LAND_DETACHMENT_KOZAK:
                case LAND_DETACHMENT_EXPLORATION_KOZAK:
                    face2 = CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION_KOZAK;
                    break;
                case LAND_INDIAN:
                case LAND_INDIAN_EXPLORATION:
                    face2 = CounterFaceTypeEnum.LAND_INDIAN_EXPLORATION;
                    break;
                case LAND_SEPOY:
                case LAND_SEPOY_EXPLORATION:
                    face2 = CounterFaceTypeEnum.LAND_SEPOY_EXPLORATION;
                    break;
                case FLEET_PLUS:
                case FLEET_MINUS:
                case NAVAL_DETACHMENT:
                case NAVAL_DETACHMENT_EXPLORATION:
                    face2 = CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION;
                    break;
                case FLEET_TRANSPORT_PLUS:
                case FLEET_TRANSPORT_MINUS:
                case NAVAL_TRANSPORT:
                case NAVAL_GALLEY:
                    // No counter of size third for them
                    break;
            }
        }

        return face2;
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
     * @param level of the fortresses.
     * @return the List of counter types of fortresses given a level.
     */
    public static List<CounterFaceTypeEnum> getFortressesFromLevel(int level) {
        List<CounterFaceTypeEnum> fortresses = new ArrayList<>();

        switch (level) {
            case 0:
                fortresses.add(CounterFaceTypeEnum.FORT);
                break;
            case 1:
                fortresses.add(CounterFaceTypeEnum.FORTRESS_1);
                break;
            case 2:
                fortresses.add(CounterFaceTypeEnum.FORTRESS_2);
                fortresses.add(CounterFaceTypeEnum.ARSENAL_2);
                break;
            case 3:
                fortresses.add(CounterFaceTypeEnum.FORTRESS_3);
                fortresses.add(CounterFaceTypeEnum.ARSENAL_3);
                break;
            case 4:
                fortresses.add(CounterFaceTypeEnum.FORTRESS_4);
                fortresses.add(CounterFaceTypeEnum.ARSENAL_4);
                break;
            case 5:
                fortresses.add(CounterFaceTypeEnum.FORTRESS_5);
                break;

        }

        return fortresses;
    }

    /**
     * @param level   of the fortresses.
     * @param arsenal if this is an arsenal
     * @return the counter types of the fortress or arsenal given a level.
     */
    public static CounterFaceTypeEnum getFortressesFromLevel(int level, boolean arsenal) {
        CounterFaceTypeEnum fortress = null;

        switch (level) {
            case 0:
                fortress = CounterFaceTypeEnum.FORT;
                break;
            case 1:
                fortress = CounterFaceTypeEnum.FORTRESS_1;
                break;
            case 2:
                if (arsenal) {
                    fortress = CounterFaceTypeEnum.ARSENAL_2;
                } else {
                    fortress = CounterFaceTypeEnum.FORTRESS_2;
                }
                break;
            case 3:
                if (arsenal) {
                    fortress = CounterFaceTypeEnum.ARSENAL_3;
                } else {
                    fortress = CounterFaceTypeEnum.FORTRESS_3;
                }
                break;
            case 4:
                if (arsenal) {
                    fortress = CounterFaceTypeEnum.ARSENAL_4;
                } else {
                    fortress = CounterFaceTypeEnum.FORTRESS_4;
                }
                break;
            case 5:
                fortress = CounterFaceTypeEnum.FORTRESS_5;
                break;

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
     * @param stack the stack.
     * @return <code>true</code> if the stack is an army (land, not naval), <code>false</code> otherwise.
     */
    public static boolean isLandArmy(Stack stack) {
        return !(stack == null || stack.getCounters().isEmpty()) &&
                stack.getCounters().stream().filter(c -> !CounterUtil.isLandArmy(c.getType())).count() == 0;
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
     * @param face the face.
     * @return <code>true</code> if the counter face type is an army counter(A- or A+), <code>false</code> otherwise.
     */
    public static boolean isArmyCounter(CounterFaceTypeEnum face) {
        boolean army = false;

        if (face != null) {
            switch (face) {
                case ARMY_PLUS:
                case ARMY_TIMAR_PLUS:
                case ARMY_MINUS:
                case ARMY_TIMAR_MINUS:
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
     * @return <code>true</code> if the counter face type is the one of a manufacture, <code>false</code> otherwise.
     */
    public static boolean isManufacture(CounterTypeEnum type) {
        boolean mnu = false;

        if (type != null) {
            switch (type) {
                case MNU_ART:
                case MNU_CEREALS:
                case MNU_CLOTHES:
                case MNU_FISH:
                case MNU_INSTRUMENTS:
                case MNU_METAL:
                case MNU_METAL_SCHLESIEN:
                case MNU_SALT:
                case MNU_WINE:
                case MNU_WOOD:
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
     * @return The CounterFaceTypeEnum of the manufacture minus corresponding to the counter type.
     */
    public static CounterFaceTypeEnum getManufactureFace(CounterTypeEnum type) {
        CounterFaceTypeEnum mnu = null;

        if (type != null) {
            switch (type) {
                case MNU_ART:
                    mnu = CounterFaceTypeEnum.MNU_ART_MINUS;
                    break;
                case MNU_CEREALS:
                    mnu = CounterFaceTypeEnum.MNU_CEREALS_MINUS;
                    break;
                case MNU_CLOTHES:
                    mnu = CounterFaceTypeEnum.MNU_CLOTHES_MINUS;
                    break;
                case MNU_FISH:
                    mnu = CounterFaceTypeEnum.MNU_FISH_MINUS;
                    break;
                case MNU_INSTRUMENTS:
                    mnu = CounterFaceTypeEnum.MNU_INSTRUMENTS_MINUS;
                    break;
                case MNU_METAL:
                    mnu = CounterFaceTypeEnum.MNU_METAL_MINUS;
                    break;
                case MNU_METAL_SCHLESIEN:
                    mnu = CounterFaceTypeEnum.MNU_METAL_SCHLESIEN_MINUS;
                    break;
                case MNU_SALT:
                    mnu = CounterFaceTypeEnum.MNU_SALT_MINUS;
                    break;
                case MNU_WINE:
                    mnu = CounterFaceTypeEnum.MNU_WINE_MINUS;
                    break;
                case MNU_WOOD:
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
     * @return The CounterTypeEnum of the manufacture corresponding to the counter face.
     */
    public static CounterTypeEnum getManufactureCounter(CounterFaceTypeEnum type) {
        CounterTypeEnum mnu = null;

        if (type != null) {
            switch (type) {
                case MNU_ART_MINUS:
                case MNU_ART_PLUS:
                    mnu = CounterTypeEnum.MNU_ART;
                    break;
                case MNU_CEREALS_MINUS:
                case MNU_CEREALS_PLUS:
                    mnu = CounterTypeEnum.MNU_CEREALS;
                    break;
                case MNU_CLOTHES_MINUS:
                case MNU_CLOTHES_PLUS:
                    mnu = CounterTypeEnum.MNU_CLOTHES;
                    break;
                case MNU_FISH_MINUS:
                case MNU_FISH_PLUS:
                    mnu = CounterTypeEnum.MNU_FISH;
                    break;
                case MNU_INSTRUMENTS_MINUS:
                case MNU_INSTRUMENTS_PLUS:
                    mnu = CounterTypeEnum.MNU_INSTRUMENTS;
                    break;
                case MNU_METAL_MINUS:
                case MNU_METAL_PLUS:
                    mnu = CounterTypeEnum.MNU_METAL;
                    break;
                case MNU_METAL_SCHLESIEN_MINUS:
                case MNU_METAL_SCHLESIEN_PLUS:
                    mnu = CounterTypeEnum.MNU_METAL_SCHLESIEN;
                    break;
                case MNU_SALT_MINUS:
                case MNU_SALT_PLUS:
                    mnu = CounterTypeEnum.MNU_SALT;
                    break;
                case MNU_WINE_MINUS:
                case MNU_WINE_PLUS:
                    mnu = CounterTypeEnum.MNU_WINE;
                    break;
                case MNU_WOOD_MINUS:
                case MNU_WOOD_PLUS:
                    mnu = CounterTypeEnum.MNU_WOOD;
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
     * @param type       of the counter to test.
     * @param land       if we want to know if the counter can stack with a land technology counter (naval technology counter if <code>false</code>).
     * @param forNeutral <code>true</code> if the counter we are testing is a neutral technology, <code>false</code> otherwise.
     * @return <code>true</code> if the counter face type can be stack with a technology counter of the given type, <code>false</code> otherwise.
     */
    public static boolean canTechnologyStack(CounterFaceTypeEnum type, boolean land, boolean forNeutral) {
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
                /** Major and minor land technologies cannot stack with a neutral land technology counter. */
                case TECH_LAND:
                case TECH_LAND_ORTHODOX:
                case TECH_LAND_LATIN:
                case TECH_LAND_ISLAM:
                case TECH_LAND_ASIA:
                    stack = !forNeutral || !land;
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
                /** Major and minor naval technologies cannot stack with a neutral land technology counter. */
                case TECH_NAVAL:
                case TECH_NAVAL_ORTHODOX:
                case TECH_NAVAL_LATIN:
                case TECH_NAVAL_ISLAM:
                case TECH_NAVAL_ASIA:
                    stack = !forNeutral || land;
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

    /**
     * @param type of the counter to test.
     * @return the type of establishment given the type of counter face.
     */
    public static EstablishmentTypeEnum getEstablishmentType(CounterFaceTypeEnum type) {
        EstablishmentTypeEnum estType = null;

        if (type != null) {
            switch (type) {
                case TRADING_POST_MINUS:
                case TRADING_POST_PLUS:
                    estType = EstablishmentTypeEnum.TRADING_POST;
                    break;
                case COLONY_MINUS:
                case COLONY_PLUS:
                    estType = EstablishmentTypeEnum.COLONY;
                    break;
                case MINOR_ESTABLISHMENT_MINUS:
                case MINOR_ESTABLISHMENT_PLUS:
                    estType = EstablishmentTypeEnum.MINOR_ESTABLISHMENT;
                    break;
                default:
                    break;
            }
        }

        return estType;
    }

    /**
     * @param type of the admin action to test.
     * @return the type of counter face of an establishment given the admin type.
     */
    public static CounterFaceTypeEnum getEstablishmentType(AdminActionTypeEnum type) {
        CounterFaceTypeEnum estType = null;

        if (type != null) {
            switch (type) {
                case TP:
                    estType = CounterFaceTypeEnum.TRADING_POST_MINUS;
                    break;
                case COL:
                    estType = CounterFaceTypeEnum.COLONY_MINUS;
                    break;
                default:
                    break;
            }
        }

        return estType;
    }

    /**
     * @param type of the counter to test.
     * @return <code>true</code> if the counter face type is a trading fleet, <code>false</code> otherwise.
     */
    public static boolean isTradingFleet(CounterFaceTypeEnum type) {
        boolean tf = false;

        if (type != null) {
            switch (type) {
                case TRADING_FLEET_MINUS:
                case TRADING_FLEET_PLUS:
                    tf = true;
                    break;
                default:
                    break;
            }
        }

        return tf;
    }

    /**
     * @param type face type which we want the face plus.
     * @return the face plus of a face type.
     */
    public static CounterFaceTypeEnum getFacePlus(CounterFaceTypeEnum type) {
        CounterFaceTypeEnum facePlus = null;

        if (type != null) {
            switch (type) {
                case TRADING_POST_MINUS:
                case TRADING_POST_PLUS:
                    facePlus = CounterFaceTypeEnum.TRADING_POST_PLUS;
                    break;
                case COLONY_MINUS:
                case COLONY_PLUS:
                    facePlus = CounterFaceTypeEnum.COLONY_PLUS;
                    break;
                default:
                    break;
            }
        }

        return facePlus;
    }

    /**
     * @param type face type which we want the face minus.
     * @return the face minus of a face type.
     */
    public static CounterFaceTypeEnum getFaceMinus(CounterFaceTypeEnum type) {
        CounterFaceTypeEnum facePlus = null;

        if (type != null) {
            switch (type) {
                case TRADING_POST_MINUS:
                case TRADING_POST_PLUS:
                    facePlus = CounterFaceTypeEnum.TRADING_POST_MINUS;
                    break;
                case COLONY_MINUS:
                case COLONY_PLUS:
                    facePlus = CounterFaceTypeEnum.COLONY_MINUS;
                    break;
                default:
                    break;
            }
        }

        return facePlus;
    }

    /**
     * @param type of the counter.
     * @param land or naval, or <code>null</code> for both.
     * @return the upgrade cost of a counter type (technology upgrade).
     */
    public static int getUpgradeCost(CounterFaceTypeEnum type, Boolean land) {
        int cost = 0;

        if (type != null) {
            switch (type) {
                case ARMY_PLUS:
                case ARMY_TIMAR_PLUS:
                    if (land == null || land) {
                        cost = 10;
                    }
                    break;
                case ARMY_MINUS:
                case ARMY_TIMAR_MINUS:
                    if (land == null || land) {
                        cost = 5;
                    }
                    break;
                case LAND_DETACHMENT:
                case LAND_DETACHMENT_EXPLORATION:
                case LAND_DETACHMENT_TIMAR:
                case LAND_DETACHMENT_KOZAK:
                case LAND_DETACHMENT_EXPLORATION_KOZAK:
                case LAND_INDIAN:
                case LAND_SEPOY:
                case LAND_INDIAN_EXPLORATION:
                case LAND_SEPOY_EXPLORATION:
                    if (land == null || land) {
                        cost = 1;
                    }
                    break;
                case FLEET_PLUS:
                case FLEET_TRANSPORT_PLUS:
                    if (land == null || !land) {
                        cost = 10;
                    }
                    break;
                case FLEET_MINUS:
                case FLEET_TRANSPORT_MINUS:
                    if (land == null || !land) {
                        cost = 5;
                    }
                    break;
                case NAVAL_DETACHMENT:
                case NAVAL_DETACHMENT_EXPLORATION:
                case NAVAL_TRANSPORT:
                    if (land == null || !land) {
                        cost = 1;
                    }
                    break;
                case NAVAL_GALLEY:
                default:
                    break;
            }
        }

        return cost;
    }

    /**
     * @param face the face.
     * @return <code>true</code> if the counter face type is mobile (can be moved), <code>false</code> otherwise.
     */
    public static boolean isMobile(CounterFaceTypeEnum face) {
        return isArmy(face) || isLeader(face);
    }

    /**
     * @param stack the stack.
     * @return <code>true</code> if the stack is mobile (can be moved), <code>false</code> otherwise.
     */
    public static boolean isMobile(Stack stack) {
        return !(stack == null || stack.getCounters().isEmpty()) && !stack.isBesieged() &&
                stack.getCounters().stream().filter(c -> !CounterUtil.isMobile(c.getType())).count() == 0;
    }

    /**
     * @param face the face.
     * @return <code>true</code> if the counter face type is exploration, <code>false</code> otherwise.
     */
    public static boolean isExploration(CounterFaceTypeEnum face) {
        boolean explo = false;

        if (face != null) {
            switch (face) {
                case LAND_DETACHMENT_EXPLORATION:
                case LAND_DETACHMENT_EXPLORATION_KOZAK:
                case LAND_INDIAN_EXPLORATION:
                case LAND_SEPOY_EXPLORATION:
                case NAVAL_DETACHMENT_EXPLORATION:
                    explo = true;
                    break;
                default:
            }
        }

        return explo;
    }

    /**
     * @param face the face.
     * @return <code>true</code> if the counter face type is a pacha, <code>false</code> otherwise.
     */
    public static boolean isPacha(CounterFaceTypeEnum face) {
        boolean pacha = false;

        if (face != null) {
            switch (face) {
                case PACHA_1:
                case PACHA_2:
                case PACHA_3:
                    pacha = true;
                    break;
            }
        }

        return pacha;
    }

    /**
     * @param face the face.
     * @return <code>true</code> if the counter face type is a pacha, <code>false</code> otherwise.
     */
    public static boolean isPacha(String face) {
        return face != null && face.startsWith("PACHA");
    }

    /**
     * @param face the face.
     * @return <code>true</code> if the counter face type is a leader, <code>false</code> otherwise.
     */
    public static boolean isLeader(CounterFaceTypeEnum face) {
        boolean leader = false;

        if (face != null) {
            switch (face) {
                case LEADER:
                case PACHA_1:
                case PACHA_2:
                case PACHA_3:
                    leader = true;
                    break;
            }
        }

        return leader;
    }

    /**
     * @param leader the leader.
     * @return the type of the counter corresponding to this leader.
     */
    public static CounterFaceTypeEnum getLeaderType(Leader leader) {
        CounterFaceTypeEnum type = CounterFaceTypeEnum.LEADER;

        if (leader != null && leader.getType() == LeaderTypeEnum.PACHA) {
            switch (leader.getSize()) {
                case 1:
                    type = CounterFaceTypeEnum.PACHA_1;
                    break;
                case 2:
                    type = CounterFaceTypeEnum.PACHA_2;
                    break;
                case 3:
                    type = CounterFaceTypeEnum.PACHA_3;
                    break;
            }
        }

        return type;
    }

    /**
     * @param type of limit.
     * @return if the type of the country limit affects a leader.
     */
    public static boolean isLeaderType(LimitTypeEnum type) {
        boolean leader = false;

        if (type != null) {
            switch (type) {
                case LEADER_GENERAL:
                case LEADER_GENERAL_AMERICA:
                case LEADER_ADMIRAL:
                case LEADER_CONQUISTADOR:
                case LEADER_CONQUISTADOR_INDIA:
                case LEADER_EXPLORER:
                case LEADER_GOVERNOR:
                    leader = true;
                    break;
            }
        }

        return leader;
    }

    /**
     * @param type of limit.
     * @return the type of leader corresponding to a type of limit that concerns a leader.
     */
    public static LeaderTypeEnum getLeaderType(LimitTypeEnum type) {
        LeaderTypeEnum leaderType = null;

        if (type != null) {
            switch (type) {
                case LEADER_GENERAL:
                    leaderType = LeaderTypeEnum.GENERAL;
                    break;
                case LEADER_GENERAL_AMERICA:
                    leaderType = LeaderTypeEnum.GENERAL;
                    break;
                case LEADER_ADMIRAL:
                    leaderType = LeaderTypeEnum.ADMIRAL;
                    break;
                case LEADER_CONQUISTADOR:
                    leaderType = LeaderTypeEnum.CONQUISTADOR;
                    break;
                case LEADER_CONQUISTADOR_INDIA:
                    leaderType = LeaderTypeEnum.CONQUISTADOR;
                    break;
                case LEADER_EXPLORER:
                    leaderType = LeaderTypeEnum.EXPLORER;
                    break;
                case LEADER_GOVERNOR:
                    leaderType = LeaderTypeEnum.GOVERNOR;
                    break;
            }
        }

        return leaderType;
    }
}
