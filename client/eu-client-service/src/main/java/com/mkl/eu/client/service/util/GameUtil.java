package com.mkl.eu.client.service.util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility around the game.
 *
 * @author MKL.
 */
public final class GameUtil {
    /**
     * Constructor.
     */
    private GameUtil() {

    }

    /**
     * Get the stability from the box (province) where the counter is.
     *
     * @param provinceBox name of the province/box where the stability counter is.
     * @return the stability.
     */
    public static Integer getStability(String provinceBox) {
        Integer stab = null;
        if (!StringUtils.isEmpty(provinceBox)) {
            Matcher matcher = Pattern.compile("B_STAB_(-?\\d)").matcher(provinceBox);
            if (matcher.matches()) {
                stab = Integer.parseInt(matcher.group(1));
            }
        }

        return stab;
    }

    /**
     * Filter to use to filter the provinces which can hold stability counters.
     *
     * @param provinceBox name of the province/box.
     * @return <code>true</code> if the province can hold a stability counter, <code>false</code> otherwise.
     */
    public static boolean isStabilityBox(String provinceBox) {
        return !StringUtils.isEmpty(provinceBox) && provinceBox.startsWith("B_STAB_");
    }

    /**
     * Get the stability box for a given stab.
     *
     * @param stab actual stability.
     * @return the stability box.
     */
    public static String getStabilityBox(int stab) {
        return "B_STAB_" + stab;
    }

    /**
     * Get the technology advance from the box (province) where the counter is.
     *
     * @param provinceBox name of the province/box where the technology counter is.
     * @return the technology advance.
     */
    public static Integer getTechnology(String provinceBox) {
        Integer tech = null;
        if (!StringUtils.isEmpty(provinceBox)) {
            Matcher matcher = Pattern.compile("B_TECH_(\\d*)").matcher(provinceBox);
            if (matcher.matches()) {
                tech = Integer.parseInt(matcher.group(1));
            }
        }

        return tech;
    }

    /**
     * Filter to use to filter the provinces which can hold technology counters.
     *
     * @param provinceBox name of the province/box.
     * @return <code>true</code> if the province can hold a technology counter, <code>false</code> otherwise.
     */
    public static boolean isTechnologyBox(String provinceBox) {
        return !StringUtils.isEmpty(provinceBox) && provinceBox.startsWith("B_TECH_");
    }

    /**
     * Get the technology box for a given advance.
     *
     * @param tech the technology advance.
     * @return the technology box.
     */
    public static String getTechnologyBox(int tech) {
        return "B_TECH_" + tech;
    }

    /**
     * Get the inflation from the box (province) where the counter is.
     *
     * @param provinceBox name of the province/box where the inflation counter is.
     * @param exploitGold flag saying that we want the inflation for those exploiting gold in America or not.
     * @return the inflation.
     */
    public static Integer getInflation(String provinceBox, boolean exploitGold) {
        int internalNumber = -1;
        if (!StringUtils.isEmpty(provinceBox)) {
            Matcher matcher = Pattern.compile("B_PB_(\\d)([DG])").matcher(provinceBox);
            if (matcher.matches()) {
                int number = Integer.parseInt(matcher.group(1));
                boolean right = StringUtils.equals("D", matcher.group(2));
                internalNumber = 2 * number + 1;
                if (right) {
                    internalNumber++;
                }
                if (!exploitGold) {
                    internalNumber--;
                }
            }
        }

        Integer inflation;
        switch (internalNumber) {
            case 10:
                inflation = 33;
                break;
            case 9:
                inflation = 25;
                break;
            case 8:
            case 7:
                inflation = 20;
                break;
            case 6:
            case 5:
            case 4:
                inflation = 10;
                break;
            case 3:
            case 2:
            case 1:
            case 0:
                inflation = 5;
                break;
            default:
                inflation = 0;
                break;
        }

        return inflation;
    }

    /**
     * Filter to use to filter the provinces which can hold inflation counter.
     *
     * @param provinceBox name of the province/box.
     * @return <code>true</code> if the province can hold a inflation counter, <code>false</code> otherwise.
     */
    public static boolean isInflationBox(String provinceBox) {
        return !StringUtils.isEmpty(provinceBox) && provinceBox.startsWith("B_PB_");
    }

    /**
     * Get the box number of a technology counter.
     *
     * @param techBox name of the province/box where the technology counter is.
     * @return the box number.
     */
    public static Integer getTechnologyBox(String techBox) {
        Integer tech = null;
        if (!StringUtils.isEmpty(techBox)) {
            Matcher matcher = Pattern.compile("B_TECH_(\\d*)").matcher(techBox);
            if (matcher.matches()) {
                tech = Integer.parseInt(matcher.group(1));
            }
        }

        return tech;
    }

    /**
     * @param province to test.
     * @return <code>true</code> if the province is in the rotw, <code>false</code> otherwise. Does not work for sea provinces.
     */
    public static boolean isRotwProvince(String province) {
        return StringUtils.isNotEmpty(province) && province.startsWith("r");
    }
}
