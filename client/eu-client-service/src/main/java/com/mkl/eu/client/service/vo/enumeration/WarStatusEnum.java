package com.mkl.eu.client.service.vo.enumeration;

/**
 * Enum to know in which war status a country is.
 *
 * @author MKL.
 */
public enum WarStatusEnum {
    /** In Peace. */
    PEACE(false, false),
    /** In a classic war. */
    CLASSIC_WAR(true, true);

    /** To know if the country can use war maintenance. */
    private boolean warMaintenance;
    /** To know if the country can levy exceptional taxes. */
    private boolean taxes;
    // TODO notion of army levies
    // TODO notion of bonus at exchequer test
    // TODO notion of blocked trade (maybe)

    /**
     * Constructor.
     *
     * @param warMaintenance the warMaintenance to set.
     * @param taxes          the taxes to set.
     */
    WarStatusEnum(boolean warMaintenance, boolean taxes) {
        this.warMaintenance = warMaintenance;
        this.taxes = taxes;
    }

    /** @return the warMaintenance. */
    public boolean canWarMaintenance() {
        return warMaintenance;
    }

    /** @return the taxes. */
    public boolean canTaxes() {
        return taxes;
    }
}
