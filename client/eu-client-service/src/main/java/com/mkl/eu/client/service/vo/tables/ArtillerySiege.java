package com.mkl.eu.client.service.vo.tables;

import com.mkl.eu.client.service.vo.EuObject;

/**
 * VO for the artillery siege table.
 *
 * @author MKL.
 */
public class ArtillerySiege extends EuObject {
    /** Number of artillery of the besieging army. */
    private Integer artillery;
    /** Level of the fortress under siege. */
    private Integer fortress;
    /** Bonus of the artillery on the siege. */
    private Integer bonus;

    /** @return the size. */
    public Integer getArtillery() {
        return artillery;
    }

    /** @param artillery the size to set. */
    public void setArtillery(Integer artillery) {
        this.artillery = artillery;
    }

    /** @return the fortress. */
    public Integer getFortress() {
        return fortress;
    }

    /** @param fortress the fortress to set. */
    public void setFortress(Integer fortress) {
        this.fortress = fortress;
    }

    /** @return the bonus. */
    public Integer getBonus() {
        return bonus;
    }

    /** @param bonus the bonus to set. */
    public void setBonus(Integer bonus) {
        this.bonus = bonus;
    }
}
