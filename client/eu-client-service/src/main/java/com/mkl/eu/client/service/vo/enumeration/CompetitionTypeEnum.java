package com.mkl.eu.client.service.vo.enumeration;

/**
 * Enumeration for automatic competitions.
 *
 * @author MKL.
 */
public enum CompetitionTypeEnum {
    /** TF competition for total monopoly (either a single level 6 tf and no other tfs either no level 6 tf). */
    TF_6,
    /** TF competition for partial monopoly (a single tf of level 4 or more). */
    TF_4,
    /** Establishment competition (a single establishment counter per province). */
    ESTABLISHMENT,
    /** Exotic resources competition (exotic resources exploited by countries must be less or equal to the total exotic resources of the region). */
    EXOTIC_RESOURCES
}
