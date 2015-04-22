package com.mkl.eu.client.service.vo.player;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.enumeration.RelationTypeEnum;

/**
 * Relation between two players.
 *
 * @author MKL
 */
public class Relation extends EuObject {
    /** Owner of the relation (the one who responsible of it). */
    private Player first;
    /** The other player of the relation (may be multiple ?). */
    private Player second;
    /** Type of the relation. */
    private RelationTypeEnum type;

    /** @return the first. */
    public Player getFirst() {
        return first;
    }

    /** @param first the first to set. */
    public void setFirst(Player first) {
        this.first = first;
    }

    /** @return the second. */
    public Player getSecond() {
        return second;
    }

    /** @param second the second to set. */
    public void setSecond(Player second) {
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