package com.mkl.eu.client.service.vo.util;

import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.ForceTypeEnum;
import com.mkl.eu.client.service.vo.tables.IBasicForce;
import com.mkl.eu.client.service.vo.tables.IUnit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility to compute maintenance.
 *
 * @author MKL.
 */
public final class MaintenanceUtil {
    /** Constant for LAND. */
    private static final String LAND = "LAND";
    /** Constant for NAVAL. */
    private static final String NAVAL = "NAVAL";
    /** Constant for TIMAR. */
    private static final String TIMAR = "TIMAR";
    /** Constant for JOKER. */
    private static final String JOKER = "JOKER";

    /**
     * Constructor.
     */
    private MaintenanceUtil() {

    }

    /**
     * Compute the maintenance given the forces, the free maintenance and the price of each unit.
     *
     * @param forces      to maintain.
     * @param basicForces free maintenance.
     * @param units       price of maintenance for each unit.
     * @return the total maintenance fee.
     */
    public static int computeMaintenance(Map<CounterFaceTypeEnum, Integer> forces, List<? extends IBasicForce> basicForces, List<? extends IUnit> units) {
        int total = 0;

        if (forces != null && units != null) {
            Map<CounterFaceTypeEnum, Integer> forcesLeft = new HashMap<>(forces);


            Map<String, Double> maintenance = new HashMap<>();
            if (basicForces != null) {
                for (IBasicForce basicForce : basicForces) {
                    String type = getTypeFromForce(basicForce.getType());
                    Double number = basicForce.getNumber() * getSizeFromForce(basicForce.getType());
                    if (!maintenance.containsKey(type)) {
                        maintenance.put(type, number);
                    } else {
                        maintenance.put(type, maintenance.get(type) + number);
                    }
                }
            }

            SubtractMaintenanceForces(maintenance.get(LAND),
                    new MaintenanceInfo(new CounterFaceTypeEnum[]{CounterFaceTypeEnum.ARMY_PLUS}, 4, getPrice(units, CounterFaceTypeEnum.ARMY_PLUS)),
                    new MaintenanceInfo(new CounterFaceTypeEnum[]{CounterFaceTypeEnum.ARMY_MINUS}, 2, getPrice(units, CounterFaceTypeEnum.ARMY_MINUS)),
                    new MaintenanceInfo(new CounterFaceTypeEnum[]{CounterFaceTypeEnum.LAND_DETACHMENT}, 1, getPrice(units, CounterFaceTypeEnum.LAND_DETACHMENT)),
                    new MaintenanceInfo(new CounterFaceTypeEnum[]{CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION}, 0.5, null),
                    forcesLeft);

            SubtractMaintenanceForces(maintenance.get(NAVAL),
                    new MaintenanceInfo(new CounterFaceTypeEnum[]{CounterFaceTypeEnum.FLEET_PLUS}, 4, getPrice(units, CounterFaceTypeEnum.FLEET_PLUS)),
                    new MaintenanceInfo(new CounterFaceTypeEnum[]{CounterFaceTypeEnum.FLEET_MINUS}, 2, getPrice(units, CounterFaceTypeEnum.FLEET_MINUS)),
                    new MaintenanceInfo(new CounterFaceTypeEnum[]{CounterFaceTypeEnum.NAVAL_DETACHMENT, CounterFaceTypeEnum.NAVAL_GALLEY, CounterFaceTypeEnum.NAVAL_TRANSPORT}, 1, getPrice(units, CounterFaceTypeEnum.NAVAL_DETACHMENT)),
                    new MaintenanceInfo(new CounterFaceTypeEnum[]{CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION}, 0.5, null),
                    forcesLeft);

            SubtractMaintenanceForces(maintenance.get(TIMAR),
                    new MaintenanceInfo(new CounterFaceTypeEnum[]{CounterFaceTypeEnum.ARMY_TIMAR_PLUS}, 4, getPrice(units, CounterFaceTypeEnum.ARMY_PLUS)),
                    new MaintenanceInfo(new CounterFaceTypeEnum[]{CounterFaceTypeEnum.ARMY_TIMAR_MINUS}, 2, getPrice(units, CounterFaceTypeEnum.ARMY_MINUS)),
                    new MaintenanceInfo(new CounterFaceTypeEnum[]{CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR}, 1, getPrice(units, CounterFaceTypeEnum.LAND_DETACHMENT)),
                    new MaintenanceInfo(null, 0.5, null),
                    forcesLeft);

            for (CounterFaceTypeEnum face : forcesLeft.keySet()) {
                Integer number = forcesLeft.get(face);
                if (number != null) {
                    IUnit unit = CommonUtil.findFirst(units, iUnit -> iUnit.getType() == getForceFromFace(face));
                    if (unit != null && unit.getPrice() != null) {
                        total += unit.getPrice() * number;
                    } else if (face == CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION ||
                            face == CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION_KOZAK) {
                        unit = CommonUtil.findFirst(units, iUnit -> iUnit.getType() == ForceTypeEnum.LD);
                        if (unit != null && unit.getPrice() != null) {
                            total += ((unit.getPrice() + 1) / 2) * number;
                        }
                    } else if (face == CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION) {
                        unit = CommonUtil.findFirst(units, iUnit -> iUnit.getType() == ForceTypeEnum.ND);
                        if (unit != null && unit.getPrice() != null) {
                            total += ((unit.getPrice() + 1) / 2) * number;
                        }
                    }
                }
            }
        }

        return total;
    }

    /**
     * Returns the maintenance price of a face.
     *
     * @param units List of maintenance costs.
     * @param face  the face.
     * @return the maintenance price of a face.
     */
    private static Integer getPrice(List<? extends IUnit> units, CounterFaceTypeEnum face) {
        Integer price = null;

        IUnit unit = CommonUtil.findFirst(units, iUnit -> iUnit.getType() == getForceFromFace(face));

        if (unit != null) {
            price = unit.getPrice();
        }

        return price;
    }

    /**
     * Subtract from the forces a type of maintenance given its faces that stand for 4, 2, 1 and a half D.
     * For Example, the LAND type would have a faceForFour of ARMY_PLUS, a feceForTwo of ARMY_MINUS,
     * a faceForOne of LAND_DETACHMENT and a faceForHalf of LAND_DETACHMENT_EXPLORATION.
     *
     * @param nbUnit      number of unit in D to subtract.
     * @param infoForFour the info on faces that stands for a unit of size 4.
     * @param infoForTwo  the info on faces that stands for a unit of size 2.
     * @param infoForOne  the info on faces that stands for a unit of size 1.
     * @param infoForHalf the info on faces that stands for a unit of size 1/2.
     * @param forces      to manage.
     */
    private static void SubtractMaintenanceForces(Double nbUnit, MaintenanceInfo infoForFour, MaintenanceInfo infoForTwo, MaintenanceInfo infoForOne, MaintenanceInfo infoForHalf, Map<CounterFaceTypeEnum, Integer> forces) {
        if (nbUnit != null) {
            int disc42 = subtract(infoForFour.getPrice(), infoForTwo.getPrice());
            int disc22 = toInt(infoForTwo.getPrice());
            int disc41 = subtract(infoForFour.getPrice(), infoForTwo.getPrice(), infoForOne.getPrice());
            int disc21 = subtract(infoForTwo.getPrice(), infoForOne.getPrice());
            int disc11 = toInt(infoForOne.getPrice());
            nbUnit = subtractMaintenanceForce(nbUnit, infoForFour, forces);
            if (nbUnit >= 2 && nbUnit < 4 && disc42 > disc22) {
                if (subtractMaintenanceForce(infoForFour, forces)) {
                    addOne(forces, infoForTwo.getFaces()[0]);
                    nbUnit -= infoForTwo.getSize();
                }
            } else if (nbUnit == 1 && disc41 > disc11 && disc41 > disc21) {
                if (subtractMaintenanceForce(infoForFour, forces)) {
                    addOne(forces, infoForTwo.getFaces()[0]);
                    addOne(forces, infoForOne.getFaces()[0]);
                    nbUnit -= infoForOne.getSize();
                }
            }
            nbUnit = subtractMaintenanceForce(nbUnit, infoForTwo, forces);
            if (nbUnit == 2) {
                if (subtractMaintenanceForce(infoForFour, forces)) {
                    addOne(forces, infoForTwo.getFaces()[0]);
                    nbUnit -= infoForTwo.getSize();
                }
            }
            if (nbUnit == 1 && disc21 > disc11) {
                if (subtractMaintenanceForce(infoForTwo, forces)) {
                    addOne(forces, infoForOne.getFaces()[0]);
                    nbUnit -= infoForOne.getSize();
                }
            }
            nbUnit = subtractMaintenanceForce(nbUnit, infoForOne, forces);
            if (nbUnit == 1) {
                if (disc41 > disc21) {
                    if (subtractMaintenanceForce(infoForFour, forces)) {
                        addOne(forces, infoForTwo.getFaces()[0]);
                        addOne(forces, infoForOne.getFaces()[0]);
                        nbUnit -= infoForOne.getSize();
                    } else if (subtractMaintenanceForce(infoForTwo, forces)) {
                        addOne(forces, infoForOne.getFaces()[0]);
                        nbUnit -= infoForOne.getSize();
                    }
                } else {
                    if (subtractMaintenanceForce(infoForTwo, forces)) {
                        addOne(forces, infoForOne.getFaces()[0]);
                        nbUnit -= infoForOne.getSize();
                    } else if (subtractMaintenanceForce(infoForFour, forces)) {
                        addOne(forces, infoForTwo.getFaces()[0]);
                        addOne(forces, infoForOne.getFaces()[0]);
                        nbUnit -= infoForOne.getSize();
                    }
                }
            }
            subtractMaintenanceForce(nbUnit, infoForHalf, forces);
        }
    }

    /**
     * Transform an Integer to an int (<code>null</code> will be transformed to <code>0</code>).
     *
     * @param i the Integer.
     * @return the int.
     */
    private static int toInt(Integer i) {
        int returnValue = 0;

        if (i != null) {
            returnValue = i;
        }

        return returnValue;
    }

    /**
     * Subtract the values to the origin. Result cannot be negative (will return <code>0</code>).
     *
     * @param origin beginning value.
     * @param values to subtract.
     * @return the subtraction.
     */
    private static int subtract(Integer origin, Integer... values) {
        int returnValue = toInt(origin);

        if (values != null) {
            for (Integer value : values) {
                returnValue -= toInt(value);
            }
        }

        if (returnValue < 0) {
            returnValue = 0;
        }

        return returnValue;
    }

    /**
     * Increment a Map of K->Integer for a given key.
     *
     * @param map the map.
     * @param key the key.
     * @param <K> the class of the key.
     */
    private static <K> void addOne(Map<K, Integer> map, K key) {
        if (map != null) {
            if (map.get(key) != null) {
                map.put(key, map.get(key) + 1);
            } else {
                map.put(key, 1);
            }
        }
    }

    /**
     * Subtract from the forces a particular face given a number of unit and its size.
     *
     * @param nbUnit number given in D.
     * @param info   Information containing the faces to subtract from the forces, the size of the face and the price of the face.
     * @param forces to manage.
     * @return the remaining number of D in the maintenance.
     */
    private static Double subtractMaintenanceForce(Double nbUnit, MaintenanceInfo info, Map<CounterFaceTypeEnum, Integer> forces) {
        if (info != null && info.getFaces() != null) {
            for (CounterFaceTypeEnum face : info.getFaces()) {
                if (nbUnit >= info.getSize()
                        && forces.containsKey(face)
                        && forces.get(face) != null
                        && forces.get(face) > 0) {
                    int number = (int) Math.min(nbUnit / info.getSize(), forces.get(face));
                    nbUnit -= info.getSize() * number;
                    if (forces.get(face) > number) {
                        forces.put(face, forces.get(face) - number);
                    } else {
                        forces.remove(face);
                    }
                }
            }
        }

        return nbUnit;
    }

    /**
     * Subtract from the forces a particular face given a number of unit and its size.
     *
     * @param info   Information containing the faces to subtract from the forces, the size of the face and the price of the face.
     * @param forces to manage.
     * @return the remaining number of D in the maintenance.
     */
    private static boolean subtractMaintenanceForce(MaintenanceInfo info, Map<CounterFaceTypeEnum, Integer> forces) {
        boolean result = false;

        if (info != null && info.getFaces() != null) {
            for (CounterFaceTypeEnum face : info.getFaces()) {
                if (forces.containsKey(face)
                        && forces.get(face) != null
                        && forces.get(face) > 0) {
                    result = true;
                    if (forces.get(face) > 1) {
                        forces.put(face, forces.get(face) - 1);
                    } else {
                        forces.remove(face);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Transform a face to a force enum.
     *
     * @param face to transform in force.
     * @return the force.
     */
    private static ForceTypeEnum getForceFromFace(CounterFaceTypeEnum face) {
        ForceTypeEnum force = null;

        if (face != null) {
            switch (face) {
                case ARMY_MINUS:
                case ARMY_TIMAR_MINUS:
                    force = ForceTypeEnum.ARMY_MINUS;
                    break;
                case ARMY_PLUS:
                case ARMY_TIMAR_PLUS:
                    force = ForceTypeEnum.ARMY_PLUS;
                    break;
                case LAND_DETACHMENT:
                case LAND_DETACHMENT_KOZAK:
                case LAND_DETACHMENT_TIMAR:
                    force = ForceTypeEnum.LD;
                    break;
                case FLEET_MINUS:
                    force = ForceTypeEnum.FLEET_MINUS;
                    break;
                case FLEET_PLUS:
                    force = ForceTypeEnum.FLEET_PLUS;
                    break;
                case NAVAL_DETACHMENT:
                case NAVAL_GALLEY:
                case NAVAL_TRANSPORT:
                    force = ForceTypeEnum.ND;
                    break;
                default:
                    break;
            }
        }

        return force;
    }

    /**
     * Transform a force to a type.
     *
     * @param force to transform in type.
     * @return the type.
     */
    private static String getTypeFromForce(ForceTypeEnum force) {
        String type = null;

        if (force != null) {
            switch (force) {
                case ARMY_MINUS:
                case ARMY_PLUS:
                case LD:
                    type = LAND;
                    break;
                case ARMY_TIMAR_MINUS:
                case ARMY_TIMAR_PLUS:
                case LDT:
                    type = TIMAR;
                    break;
                case FLEET_MINUS:
                case FLEET_PLUS:
                case ND:
                    type = NAVAL;
                    break;
                case LDND:
                    type = JOKER;
                    break;
                default:
                    break;
            }
        }

        return type;
    }

    /**
     * Transform a force to a size.
     *
     * @param force to transform in size.
     * @return the size.
     */
    private static Double getSizeFromForce(ForceTypeEnum force) {
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

    private static class MaintenanceInfo {
        /** The faces corresponding to this type. */
        private CounterFaceTypeEnum[] faces;
        /** The size of this type. */
        private double size;
        /** The maintenance price of this type. */
        private Integer price;

        /**
         * Constructor.
         *
         * @param faces the faces to set.
         * @param size  the size to set.
         * @param price the price to set.
         */
        public MaintenanceInfo(CounterFaceTypeEnum[] faces, double size, Integer price) {
            this.faces = faces;
            this.size = size;
            this.price = price;
        }

        /** @return the faces. */
        public CounterFaceTypeEnum[] getFaces() {
            return faces;
        }

        /** @return the size. */
        public double getSize() {
            return size;
        }

        /** @return the price. */
        public Integer getPrice() {
            return price;
        }
    }
}
