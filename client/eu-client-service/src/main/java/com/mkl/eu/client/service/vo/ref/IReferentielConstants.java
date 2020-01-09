package com.mkl.eu.client.service.vo.ref;

/**
 * Constants for data in referentiel domain.
 *
 * @author MKL.
 */
public interface IReferentielConstants {
    // TODO replace these by GeoGroup geature
    /** Name of the caspian trading zone. */
    String TRADE_ZONE_CASPIAN = "ZMCaspienne";
    /** List of the names of the trade zones in Europe. */
    String[] TRADE_ZONES_EUROPE = {"ZMCaspienne", "ZMIonienne", "ZMLion", "ZMNoire W", "ZMNord",
            "ZPBaltique", "ZPangleterre", "ZPespagne", "ZPfrance", "ZPhollande", "ZPrussie",
            "ZPturquie", "ZPvenise"};
    /** List of the names of the trade zones in mediterranean trace center. */
    String[] TRADE_ZONES_MEDITERRANEAN = {"ZMCaspienne", "ZMIonienne", "ZMLion", "ZMNoire W",
            "ZPturquie", "ZPvenise"};
    /** List of the names of the trade zones in the atlantic trade center. */
    String[] TRADE_ZONES_ATLANTIC = {"ZMAmerique", "ZMCanarias", "ZMCaraibes", "ZMGuinee",
            "ZMNord", "ZMPatagonie", "ZMPerou", "ZMRecife",
            "ZPangleterre", "ZPBaltique", "ZPespagne", "ZPfrance", "ZPhollande", "ZPrussie"};
    /** List of the names of the trade zones in the indian trade center. */
    String[] TRADE_ZONES_INDIEN = {"ZMArabie", "ZMChine", "ZMIndien", "ZMTempetes"};
    /** List of the names of the trade zones that accept trade. */
    String[] TRADE_ZONES_TRADE = {"ZMArabie", "ZMCanarias", "ZMChine", "ZMGuinee", "ZMIndien"};


    /** Geo group of America. */
    String AMERICA = "AMERICA";
    /** Geo group of Asia. */
    String ASIA = "ASIA";
    /** Geo group of Mediterranean sea. */
    String MEDITERRANEAN_SEA = "MEDITERRANEAN SEA";
    /** Geo group of Europe (used only by seas for the moment). */
    String EUROPE = "EUROPE";
}