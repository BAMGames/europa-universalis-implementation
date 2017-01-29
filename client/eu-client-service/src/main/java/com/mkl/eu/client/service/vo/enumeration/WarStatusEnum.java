package com.mkl.eu.client.service.vo.enumeration;

/**
 * Enum to know in which war status a country is.
 *
 * @author MKL.
 */
public enum WarStatusEnum {
    /** In Peace. */
    PEACE(false, false, false),
    /** In a foreign intervention. */
    FOREIGN_INTERVENTION(false, false, false),
    /** In a limited intervention. */
    LIMITED_INTERVENTION(false, false, false),
    /** In a religious war. */
    RELIGIOUS_WAR(true, true, false),
    /** In a civil war. */
    CIVIL_WAR(true, true, false),
    /** In a classic war. */
    CLASSIC_WAR(true, true, true);

    /** To know if the country can use war maintenance. */
    private boolean warMaintenance;
    /** To know if the country can levy exceptional taxes. */
    private boolean taxes;
    /** To know if the country can levy exceptional taxes without stab loss. */
    private boolean taxesReduction;
    // TODO notion of army levies
    // TODO notion of bonus at exchequer test
    // TODO notion of blocked trade (maybe)

    /**
     * Constructor.
     *
     * @param warMaintenance the warMaintenance to set.
     * @param taxes          the taxes to set.
     * @param taxesReduction the taxesReduction to set.
     */
    WarStatusEnum(boolean warMaintenance, boolean taxes, boolean taxesReduction) {
        this.warMaintenance = warMaintenance;
        this.taxes = taxes;
        this.taxesReduction = taxesReduction;
    }

    /** @return the warMaintenance. */
    public boolean canWarMaintenance() {
        return warMaintenance;
    }

    /** @return the taxes. */
    public boolean canTaxes() {
        return taxes;
    }

    /** @return the taxesReduction. */
    public boolean hasTaxesReduction() {
        return taxesReduction;
    }
}
