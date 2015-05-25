package com.mkl.eu.client.service.vo.country;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.enumeration.RelationTypeEnum;

/**
 * Relation between two players.
 *
 * @author MKL
 */
public class Relation extends EuObject {
    /** Owner of the relation (the one who responsible of it). */
    private PlayableCountry first;
    /** The other player of the relation (may be multiple ?). */
    private PlayableCountry second;
    /** Type of the relation. */
    private RelationTypeEnum type;

    /** @return the first. */
    public PlayableCountry getFirst() {
        return first;
    }

    /** @param first the first to set. */
    public void setFirst(PlayableCountry first) {
        this.first = first;
    }

    /** @return the second. */
    public PlayableCountry getSecond() {
        return second;
    }

    /** @param second the second to set. */
    public void setSecond(PlayableCountry second) {
        this.second = second;
    }

    /** @return the type. */
    public RelationTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(RelationTypeEnum type) {
        this.type = type;
    }
}