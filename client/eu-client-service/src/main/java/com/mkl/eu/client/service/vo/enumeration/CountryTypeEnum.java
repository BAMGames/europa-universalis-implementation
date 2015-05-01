package com.mkl.eu.client.service.vo.enumeration;

/**
 * Enumeration for type of countries.
 *
 * @author MKL.
 */
public enum CountryTypeEnum {
    /** Major country. A minor that becomes major are represented by 2 countries: one minor/major and one major. */
    MAJOR,
    /** Minor country. */
    MINOR,
    /** Minor country that was major or that will be major. */
    MINORMAJOR,
    /** Minor country on the ROTW map (except the one created by the independence of colonies). */
    ROTW,
    /** Semi-independants kingdom of Hasbourgs (can be created either by HAB or SPA). */
    HAB,
    /** Countries created by revolts. */
    REVOLT,
    /** Virtual entities (natives, hre or events related). */
    VIRTUAL;
}
