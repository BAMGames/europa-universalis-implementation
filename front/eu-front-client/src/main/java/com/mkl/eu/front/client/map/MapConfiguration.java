package com.mkl.eu.front.client.map;

/**
 * Configuration of the maps.
 *
 * @author MKL
 */
public final class MapConfiguration {
    /** Color mode. */
    private static boolean withColor;
    /** See move phase of stacks. */
    private static boolean stacksMovePhase;
    /** Hide mobile counters. */
    private static boolean hideArmies;
    /** Hide economical counters. */
    private static boolean hideInfrastructures;
    /** Hide all counters. */
    private static boolean hideAll;

    /**
     * No instantiation of an utility class.
     */
    private MapConfiguration() {

    }

    /** Switch the color mode. */
    public static void switchColor() {
        withColor = !withColor;
    }

    /** @return the withColor. */
    public static boolean isWithColor() {
        return withColor;
    }

    /** Switch the move phase of stacks mode. */
    public static void switchStacksMovePhase() {
        stacksMovePhase = !stacksMovePhase;
    }

    /** @return the stacksMovePhase. */
    public static boolean isStacksMovePhase() {
        return stacksMovePhase;
    }

    /** Switch the hideArmies mode. */
    public static void switchHideArmies() {
        hideArmies = !hideArmies;
    }

    /** @return the hideArmies. */
    public static boolean isHideArmies() {
        return hideArmies;
    }

    /** Switch the hideInfrastructures mode. */
    public static void switchHideInfrastructures() {
        hideInfrastructures = !hideInfrastructures;
    }

    /** @return the hideInfrastructures. */
    public static boolean isHideInfrastructures() {
        return hideInfrastructures;
    }

    /** Switch the hideAll mode. */
    public static void switchHideAll() {
        hideAll = !hideAll;
    }

    /** @return the hideAll. */
    public static boolean isHideAll() {
        return hideAll;
    }
}
