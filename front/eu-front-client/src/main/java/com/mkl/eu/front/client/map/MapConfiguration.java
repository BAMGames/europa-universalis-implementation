package com.mkl.eu.front.client.map;

/**
 * Configuration of the maps.
 *
 * @author MKL
 */
public final class MapConfiguration {
    /** Color mode. */
    private static boolean withColor = false;
    /** See move phase of stacks. */
    private static boolean stacksMovePhase;

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
}
