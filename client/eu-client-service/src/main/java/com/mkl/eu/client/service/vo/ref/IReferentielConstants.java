package com.mkl.eu.client.service.vo.ref;

/**
 * Constants for data in referentiel domain.
 *
 * @author MKL.
 */
public interface IReferentielConstants {
    /** Name of the caspian trading zone. */
    String TRADE_ZONE_CASPIAN = "ZMCaspienne";
    /** List of the names of the trade zones in Europe. */
    String[] TRADE_ZONES_EUROPE = {"ZMCaspienne", "ZMIonienne", "ZMLion", "ZMNoire W", "ZMNord",
            "ZPBaltique", "ZPangleterre", "ZPespagne", "ZPfrance", "ZPhollande", "ZPrussie",
            "ZPturquie", "ZPvenise"};
    /** List of the names of the trade zones in the atlantic trade center. */
    String[] TRADE_ZONES_ATLANTIC = {"ZMAmerique", "ZMCanarias", "ZMCaraibes", "ZMGuinee",
            "ZMNord", "ZMPatagonie", "ZMPerou", "ZMRecife", "ZMTempetes",
            "ZPangleterre", "ZPBaltique", "ZPespagne", "ZPfrance", "ZPhollande", "ZPrussie"};
    /** List of the names of the trade zones in the indian trade center. */
    String[] TRADE_ZONES_INDIEN = {"ZMArabie", "ZMChine", "ZMIndien"};
    /** List of the names of the trade zones that accept trade. */
    String[] TRADE_ZONES_TRADE = {"ZMArabie", "ZMCanarias", "ZMChine", "ZMGuinee", "ZMIndien"};
}