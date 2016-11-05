package com.mkl.eu.client.service.vo.enumeration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Enumeration for the cultural groups.
 *
 * @author MKL.
 */
public enum CultureEnum {
    /** Orthodox cultural group. */
    ORTHODOX(4, 3),
    /** Latin cultural group. */
    LATIN(2, 0, ORTHODOX),
    /** Islam cultural group. */
    ISLAM(5, 1),
    /** Medieval cultural group. */
    MEDIEVAL(100, 0),
    /** Rotw cultural group. */
    ROTW(10, 1);

    /**
     * Number of turns it takes for this Culture type to
     * gain on technology advance.
     * The formula to know if this culture should improve technology is:
     * gameTurn - technologyShift % technologyFrequency = 0;
     */
    private int technologyFrequency;
    /**
     * Shift to the number of turn.
     * The formula to know if this culture should improve technology is:
     * gameTurn - technologyShift % technologyFrequency = 0;
     */
    private int technologyShift;
    /**
     * Array of cultures of countries that will technology
     * help the Culture Group technology of this culture.
     */
    private List<CultureEnum> technologyCultures = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param technologyFrequency the technologyFrequency to set.
     * @param technologyShift     the technologyShift to set.
     * @param technologyCultures  the technologyCultures to set.
     */
    CultureEnum(int technologyFrequency, int technologyShift, CultureEnum... technologyCultures) {
        this.technologyFrequency = technologyFrequency;
        this.technologyShift = technologyShift;
        this.technologyCultures.addAll(Arrays.asList(technologyCultures));
        this.technologyCultures.add(this);
    }

    /** @return the technologyFrequency. */
    public int getTechnologyFrequency() {
        return technologyFrequency;
    }

    /** @return the technologyShift. */
    public int getTechnologyShift() {
        return technologyShift;
    }

    /** @return the technologyCultures. */
    public List<CultureEnum> getTechnologyCultures() {
        return technologyCultures;
    }
}
