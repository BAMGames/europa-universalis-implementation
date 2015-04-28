package com.mkl.eu.front.client.map;

/**
 * Configuration of the map.
 *
 * @author MKL
 */
public final class MapConfiguration {
    /** Color mode. */
    private static boolean withColor = false;
    /** Id of the game. */
    private static Long idGame;
    /** Version of the game. */
    private static Long versionGame;

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

    /** @return the idGame. */
    public static Long getIdGame() {
        return idGame;
    }

    /** @param idGame the idGame to set. */
    public static void setIdGame(Long idGame) {
        MapConfiguration.idGame = idGame;
    }

    /** @return the versionGame. */
    public static Long getVersionGame() {
        return versionGame;
    }

    /** @param versionGame the versionGame to set. */
    public static void setVersionGame(Long versionGame) {
        MapConfiguration.versionGame = versionGame;
    }
}
