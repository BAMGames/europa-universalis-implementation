package com.mkl.eu.front.client.map;

/**
 * Configuration of the map.
 *
 * @author MKL
 */
public class MapConfiguration {
    /** Color mode. */
    private static boolean withColor = false;

    /** Switch the color mode. */
    public static void switchColor() {
        withColor = !withColor;
    }

    /** @return the withColor. */
    public static boolean isWithColor() {
        return withColor;
    }
}
