package com.mkl.eu.client.service.util;

import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.ForceTypeEnum;
import com.mkl.eu.client.service.vo.tables.IBasicForce;
import com.mkl.eu.client.service.vo.tables.IUnit;
import com.mkl.eu.client.service.vo.tables.Tech;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

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
    public static int computeUnitMaintenance(Map<CounterFaceTypeEnum, Long> forces, List<? extends IBasicForce> basicForces, List<? extends IUnit> units) {
        if (forces == null || units == null) {
            return 0;
        }

        Map<CounterFaceTypeEnum, Long> forcesLeft = new HashMap<>(forces);


        Map<String, Double> maintenance = maintenanceFromBasicForces(basicForces);

        // It is too complicated to test where the joker would be the most profitable
        // So we just use them in naval, and if some of them are not used, they are
        // then used in land and finally in special units.

        Double joker = maintenance.get(JOKER);

        Double remain = SubtractMaintenanceForces(CommonUtil.add(maintenance.get(NAVAL), joker),
                new MaintenanceInfo().addFace(CounterFaceTypeEnum.FLEET_PLUS).setSize(4).setPrice(getPrice(units, CounterFaceTypeEnum.FLEET_PLUS)),
                new MaintenanceInfo().addFace(CounterFaceTypeEnum.FLEET_MINUS).setSize(2).setPrice(getPrice(units, CounterFaceTypeEnum.FLEET_MINUS)),
                new MaintenanceInfo().addFace(CounterFaceTypeEnum.NAVAL_DETACHMENT).addFace(CounterFaceTypeEnum.NAVAL_GALLEY).addFace(CounterFaceTypeEnum.NAVAL_TRANSPORT)
                        .setSize(1).setPrice(getPrice(units, CounterFaceTypeEnum.NAVAL_DETACHMENT)),
                new MaintenanceInfo().addFace(CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION).setSize(0.5),
                forcesLeft);

        joker = CommonUtil.min(joker, remain);

        remain = SubtractMaintenanceForces(CommonUtil.add(maintenance.get(LAND), joker),
                new MaintenanceInfo().addFace(CounterFaceTypeEnum.ARMY_PLUS).setSize(4).setPrice(getPrice(units, CounterFaceTypeEnum.ARMY_PLUS)),
                new MaintenanceInfo().addFace(CounterFaceTypeEnum.ARMY_MINUS).setSize(2).setPrice(getPrice(units, CounterFaceTypeEnum.ARMY_MINUS)),
                new MaintenanceInfo().addFace(CounterFaceTypeEnum.LAND_DETACHMENT).setSize(1).setPrice(getPrice(units, CounterFaceTypeEnum.LAND_DETACHMENT)),
                new MaintenanceInfo().addFace(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION).setSize(0.5),
                forcesLeft);

        joker = CommonUtil.min(joker, remain);

        SubtractMaintenanceForces(CommonUtil.add(maintenance.get(TIMAR), joker),
                new MaintenanceInfo().addFace(CounterFaceTypeEnum.ARMY_TIMAR_PLUS).setSize(4).setPrice(getPrice(units, CounterFaceTypeEnum.ARMY_PLUS)),
                new MaintenanceInfo().addFace(CounterFaceTypeEnum.ARMY_TIMAR_MINUS).setSize(2).setPrice(getPrice(units, CounterFaceTypeEnum.ARMY_MINUS)),
                new MaintenanceInfo().addFace(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR).setSize(1).setPrice(getPrice(units, CounterFaceTypeEnum.LAND_DETACHMENT)),
                new MaintenanceInfo().setSize(0.5),
                forcesLeft);

        return forcesLeft.entrySet().stream()
                .map(entry -> {
                    CounterFaceTypeEnum face = entry.getKey();
                    Long number = Optional.ofNullable(entry.getValue()).orElse(0l);
                    Integer price = Optional.ofNullable(getPrice(units, face)).orElse(0);

                    return price * number;
                })
                .collect(Collectors.summingInt(Long::intValue));
    }

    /**
     * @param basicForces the basic forces.
     * @return a Map of maintenance for each type of unit given the basic forces.
     */
    private static Map<String, Double> maintenanceFromBasicForces(List<? extends IBasicForce> basicForces) {
        Map<String, Double> maintenance = new HashMap<>();
        if (basicForces != null) {
            for (IBasicForce basicForce : basicForces) {
                String type = getTypeFromForce(basicForce.getType());
                Double number = basicForce.getNumber() * CounterUtil.getSizeFromForce(basicForce.getType());
                if (!maintenance.containsKey(type)) {
                    maintenance.put(type, number);
                } else {
                    maintenance.put(type, maintenance.get(type) + number);
                }
            }
        }
        return maintenance;
    }

    /**
     * Returns the maintenance price of a face.
     *
     * @param units List of maintenance costs.
     * @param face  the face.
     * @return the maintenance price of a face.
     */
    private static <T extends IUnit> Integer getPrice(List<T> units, CounterFaceTypeEnum face) {
        Function<IUnit, Integer> getPrice = unit -> CounterUtil.isExploration(face) ? (unit.getPrice() + 1) / 2 : unit.getPrice();
        Predicate<T> isOfRightType = iUnit -> iUnit.getType() == getForceFromFace(face);
        return units.stream()
                .filter(isOfRightType)
                .map(getPrice)
                .findFirst()
                .orElse(null);
    }

    /**
     * Subtract from the forces a type of maintenance given its faces that stand for 4, 2, 1 and a half D.
     * For Example, the LAND type would have a faceForFour of ARMY_PLUS, a faceForTwo of ARMY_MINUS,
     * a faceForOne of LAND_DETACHMENT and a faceForHalf of LAND_DETACHMENT_EXPLORATION.
     *
     * @param nbUnit      number of unit in D to subtract.
     * @param infoForFour the info on faces that stands for a unit of size 4.
     * @param infoForTwo  the info on faces that stands for a unit of size 2.
     * @param infoForOne  the info on faces that stands for a unit of size 1.
     * @param infoForHalf the info on faces that stands for a unit of size 1/2.
     * @param forces      to manage.
     */
    private static Double SubtractMaintenanceForces(Double nbUnit, MaintenanceInfo infoForFour, MaintenanceInfo infoForTwo, MaintenanceInfo infoForOne, MaintenanceInfo infoForHalf, Map<CounterFaceTypeEnum, Long> forces) {
        if (nbUnit != null) {
            // The aim is to select the most profitable way of using the basic force (nbUnit) by
            // selecting the most expensive units.
            int disc42 = CommonUtil.subtract(infoForFour.getPrice(), infoForTwo.getPrice());
            int disc22 = CommonUtil.toInt(infoForTwo.getPrice());
            int disc41 = CommonUtil.subtract(infoForFour.getPrice(), infoForTwo.getPrice(), infoForOne.getPrice());
            int disc21 = CommonUtil.subtract(infoForTwo.getPrice(), infoForOne.getPrice());
            int disc11 = CommonUtil.toInt(infoForOne.getPrice());
            // The rules force to use basic force for complete counter if possible. So we first
            // use the basic force for the larger units (size 4).
            nbUnit = subtractMaintenanceForce(nbUnit, infoForFour, forces);
            // After that, only 0, 1, 2 or 3 can remain in dbUnit or there are no
            // large force (size 4) left in the forces
            if (nbUnit >= 2 && nbUnit < 4 && disc42 > disc22) {
                // If using 2 of basic force to partially maintain a large force (size 4)
                // is more profitable than maintaining a medium force (size 2)
                // then we remove a large force from the remaining forces but we add
                // a medium force (size 2).
                // If nbUnit is more than 4, then there are no more large force left.
                if (subtractMaintenanceForce(infoForFour, forces)) {
                    CommonUtil.addOneLong(forces, infoForTwo.getFaces().iterator().next());
                    nbUnit -= infoForTwo.getSize();
                }
            } else if (nbUnit == 1 && disc41 > disc11 && disc41 > disc21) {
                // If using 1 of basic force to partially maintain a large force (size 4)
                // is more profitable than partially maintaining a medium force (size 2)
                // or maintaining a small force (size 1)
                // then we remove a large force from the remaining forces but
                // we had a medium force (size 2) and a small force (size 1).
                // If nbUnit is different than 1, we must first try to maintain
                // fully a counter.
                if (subtractMaintenanceForce(infoForFour, forces)) {
                    CommonUtil.addOneLong(forces, infoForTwo.getFaces().iterator().next());
                    CommonUtil.addOneLong(forces, infoForOne.getFaces().iterator().next());
                    nbUnit -= infoForOne.getSize();
                }
            }
            // The rules force to use basic force for complete counter if possible.
            // So we now use basic force for medium units (size 2).
            // Now, either nbUnit is 0 or 1, either there are no medium force (size 2)
            // left in the forces.
            nbUnit = subtractMaintenanceForce(nbUnit, infoForTwo, forces);
            if (nbUnit >= 2 && disc42 > 2 * disc11) {
                // If using 2 of basic force to partially maintain a large force (size 4)
                // is more profitable than maintaining 2 small forces (size 1)
                // then we remove a large force from the remaining forces but we add
                // a medium force (size 2).
                // We can do that because if nbUnit is more than 2, then there are
                // no medium force (size 2) left in the forces.
                if (subtractMaintenanceForce(infoForFour, forces)) {
                    CommonUtil.addOneLong(forces, infoForTwo.getFaces().iterator().next());
                    nbUnit -= infoForTwo.getSize();
                }
            }
            // If using 1 of basic force to partially maintain a medium force (size 2)
            // is more profitable than maintaining a small force (size 1)
            // then we remove a medium force from the remaining forces but
            // we had a small force.
            // If nbUnit is more than 2, then there are no more medium force left.
            if (nbUnit == 1 && disc21 > disc11) {
                if (subtractMaintenanceForce(infoForTwo, forces)) {
                    CommonUtil.addOneLong(forces, infoForOne.getFaces().iterator().next());
                    nbUnit -= infoForOne.getSize();
                }
            }
            // The rules force to use basic force for complete counter if possible.
            // So we now use basic force for small units (size 1).
            // Now, either nbUnit is 0, either there are no small force (size 1)
            // left in the forces
            nbUnit = subtractMaintenanceForce(nbUnit, infoForOne, forces);
            // If it was profitable to maintain small units (size 1), then it is
            // to maintain tiny units (size 1/2) since they have the same cost-effectiveness.
            nbUnit = subtractMaintenanceForce(nbUnit, infoForHalf, forces);
            if (nbUnit > 0 && !forces.isEmpty()) {
                // if there are still large or medium forces in the remaining forces
                // then we search the most profitable path.
                if (disc41 > disc21) {
                    // In the case where there are still medium and large forces left,
                    // we test the most profitable between the two. We test for one force
                    // because there can't be 2 basic forces and medium force left.
                    if (subtractMaintenanceForce(infoForFour, forces)) {
                        nbUnit -= infoForFour.getSize();
                    } else if (subtractMaintenanceForce(infoForTwo, forces)) {
                        nbUnit -= infoForTwo.getSize();
                    }
                } else {
                    // If it was less profitable, we take the opposite path.
                    if (subtractMaintenanceForce(infoForTwo, forces)) {
                        nbUnit -= infoForTwo.getSize();
                    } else if (subtractMaintenanceForce(infoForFour, forces)) {
                        nbUnit -= infoForFour.getSize();
                    }
                }
            }
            if (nbUnit < 0) {
                // If there was not enough basic forces to fully maintain
                // the last unit, then we create equivalent of the remaining forces
                // to pay for maintenance.
                if (nbUnit <= -2) {
                    CommonUtil.addOneLong(forces, infoForTwo.getFaces().iterator().next());
                    nbUnit += 2;
                }
                if (nbUnit <= -1) {
                    CommonUtil.addOneLong(forces, infoForOne.getFaces().iterator().next());
                    nbUnit += 1;
                }
                if (nbUnit == -0.5) {
                    CommonUtil.addOneLong(forces, infoForHalf.getFaces().iterator().next());
                    nbUnit += 0.5;
                }
            }
        } else {
            nbUnit = 0d;
        }

        return nbUnit;
    }

    /**
     * Subtract from the forces a particular face given a number of unit and its size.
     *
     * @param nbUnit number given in D.
     * @param info   Information containing the faces to subtract from the forces, the size of the face and the price of the face.
     * @param forces to manage.
     * @return the remaining number of D in the maintenance.
     */
    private static Double subtractMaintenanceForce(Double nbUnit, MaintenanceInfo info, Map<CounterFaceTypeEnum, Long> forces) {
        Predicate<CounterFaceTypeEnum> isFaceInForces = face -> forces.containsKey(face)
                && forces.get(face) != null
                && forces.get(face) > 0;
        Function<CounterFaceTypeEnum, Stream<? extends CounterFaceTypeEnum>> multiplyFacesWithForces = face -> LongStream.range(0, forces.get(face))
                .mapToObj(i -> face);
        long maxUnit = (long) (nbUnit / info.getSize());
        Function<CounterFaceTypeEnum, CounterFaceTypeEnum> subtractFromForcesAndReturnFace = face -> {
            CommonUtil.subtractOneLong(forces, face);
            return face;
        };

        long number = info.getFaces().stream()
                .filter(isFaceInForces)
                .flatMap(multiplyFacesWithForces)
                .limit(maxUnit)
                .map(subtractFromForcesAndReturnFace)
                .count();

        return nbUnit - number * info.getSize();
    }

    /**
     * Subtract from the forces a particular face given a number of unit and its size.
     *
     * @param info   Information containing the faces to subtract from the forces, the size of the face and the price of the face.
     * @param forces to manage.
     * @return <code>true</code> if a force was removed.
     */
    private static boolean subtractMaintenanceForce(MaintenanceInfo info, Map<CounterFaceTypeEnum, Long> forces) {
        Function<CounterFaceTypeEnum, Boolean> subtractFromForcesAndReturnTrue = face -> {
            CommonUtil.subtractOneLong(forces, face);
            return true;
        };
        return info.getFaces().stream()
                .filter(forces::containsKey)
                .findAny()
                .map(subtractFromForcesAndReturnTrue)
                .orElse(false);
    }

    /**
     * Returns the real purchase price for a land army given the size of the already planned land purchase,
     * the limit purchase, the normal price and the size of the unit being purchased.
     *
     * @param alreadyPurchasedSize size in LD of the already planned land purchase.
     * @param maxPurchaseSize      size in LD of the limit the country can purchase land units at normal cost.
     * @param price                normal cost of the unit being purchased.
     * @param size                 size of the unit being purchased.
     * @return the real purchase price.
     */
    public static int getPurchasePrice(Integer alreadyPurchasedSize, Integer maxPurchaseSize, Integer price, Integer size) {
        alreadyPurchasedSize = Optional.ofNullable(alreadyPurchasedSize).orElse(0);
        size = Optional.ofNullable(size).orElse(1);
        maxPurchaseSize = Optional.ofNullable(maxPurchaseSize).orElse(alreadyPurchasedSize + size);

        return getInternalPurchasePrice(alreadyPurchasedSize, maxPurchaseSize, price, size, 0).invoke();
    }

    /**
     * Uses the trampoline pattern.
     *
     * @param alreadyPurchasedSize size in LD of the already planned land purchase.
     * @param maxPurchaseSize      size in LD of the limit the country can purchase land units at normal cost.
     * @param price                normal cost of the unit being purchased.
     * @param size                 size of the unit being purchased.
     * @param acc                  recursively accumulated price.
     * @return the real purchase price.
     */
    private static TailCall<Integer> getInternalPurchasePrice(Integer alreadyPurchasedSize, Integer maxPurchaseSize, Integer price, Integer size, Integer acc) {
        if (price == null) {
            return TailCall.done(acc);
        }
        int factor = 1 + alreadyPurchasedSize / maxPurchaseSize;
        int nbLdInFactor = maxPurchaseSize - alreadyPurchasedSize % maxPurchaseSize;

        if (nbLdInFactor >= size) {
            return TailCall.done(price * factor + acc);
        } else {
            int fractionPrice = price * nbLdInFactor / size;
            int remainingPrice = price - fractionPrice;
            return () -> getInternalPurchasePrice(alreadyPurchasedSize + nbLdInFactor, maxPurchaseSize, remainingPrice, size - nbLdInFactor, acc + fractionPrice * factor);
        }
    }

    /**
     * Compute the maintenance given the fortresses, whether they are in ROTW or not, and given the owner technology and the game turn.
     *
     * @param fortresses      the fortresses grouped by level and location (<code>true</code> if in ROTW) to maintain.
     * @param techs           List of all the land technologies.
     * @param ownerTechnology the land technology of the owner of the fortresses.
     * @param gameTurn        the turn of the game.
     * @return the total maintenance fee.
     */
    public static int computeFortressesMaintenance(Map<Pair<Integer, Boolean>, Integer> fortresses, List<Tech> techs, Tech ownerTechnology, Integer gameTurn) {
        if (fortresses == null) {
            return 0;
        }
        Supplier<Boolean> hasNotGoodTechForLevel3Fortress = () -> {
            boolean goodTech = false;
            if (techs != null) {
                Tech targetTech = CommonUtil.findFirst(techs, tech -> StringUtils.equals(tech.getName(), Tech.ARQUEBUS));
                goodTech = targetTech != null && ownerTechnology != null && ownerTechnology.getBeginTurn() >= targetTech.getBeginTurn();
            }
            return !goodTech;
        };
        Supplier<Boolean> isGameNotReadyForLevel4Fortress = () -> gameTurn == null || gameTurn < 40;

        return fortresses.keySet().stream()
                .map(key -> {
                    Function<Integer, Integer> positiveOrZero = i -> i < 0 ? 0 : i;
                    Integer level = Optional.ofNullable(key.getLeft()).map(positiveOrZero).orElse(0);
                    boolean rotw = Optional.ofNullable(key.getRight()).orElse(false);
                    Integer number = Optional.ofNullable(fortresses.get(key)).map(positiveOrZero).orElse(0);
                    return getFortressCost(level, rotw, number, hasNotGoodTechForLevel3Fortress, isGameNotReadyForLevel4Fortress);
                })
                .collect(Collectors.summingInt(value -> value));
    }

    /**
     * @param level                   of the fortresses.
     * @param rotw                    flag saying if the fortresses are in the rotw.
     * @param number                  of fortresses.
     * @param level3FortressExpensive method that says if level 3 fortresses are expensive.
     * @param level4FortressExpensive method that says if level 4 fortresses are expensive.
     * @return the cost of a set of fortresses which share some behaviors.
     */
    private static Integer getFortressCost(Integer level, boolean rotw, Integer number, Supplier<Boolean> level3FortressExpensive, Supplier<Boolean> level4FortressExpensive) {
        int cost = rotw ? Math.max(1, 2 * level) : level;

        if (level == 3 && level3FortressExpensive.get() || level == 4 && level4FortressExpensive.get()) {
            cost *= 2;
        }

        return cost * number;
    }

    /**
     * Transform a face to a force enum.
     *
     * @param face to transform in force.
     * @return the force.
     */
    public static ForceTypeEnum getPurchaseForceFromFace(CounterFaceTypeEnum face) {
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
                    force = ForceTypeEnum.NWD;
                    break;
                case NAVAL_GALLEY:
                    force = ForceTypeEnum.NGD;
                    break;
                case NAVAL_TRANSPORT:
                    force = ForceTypeEnum.NTD;
                    break;
                default:
                    break;
            }
        }

        return force;
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
                case LAND_DETACHMENT_EXPLORATION:
                case LAND_DETACHMENT_EXPLORATION_KOZAK:
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
                case NAVAL_DETACHMENT_EXPLORATION:
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

    private static class MaintenanceInfo {
        /** The faces corresponding to this type. */
        private List<CounterFaceTypeEnum> faces = new ArrayList<>();
        /** The size of this type. */
        private double size;
        /** The maintenance price of this type. */
        private Integer price;

        /**
         * Constructor.
         */
        public MaintenanceInfo() {
        }

        /** @param face the face to add. */
        public MaintenanceInfo addFace(CounterFaceTypeEnum face) {
            this.faces.add(face);
            return this;
        }

        /** @param size the size to set. */
        public MaintenanceInfo setSize(double size) {
            this.size = size;
            return this;
        }

        /** @param price the price to set. */
        public MaintenanceInfo setPrice(Integer price) {
            this.price = price;
            return this;
        }

        /** @return the faces. */
        public List<CounterFaceTypeEnum> getFaces() {
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
