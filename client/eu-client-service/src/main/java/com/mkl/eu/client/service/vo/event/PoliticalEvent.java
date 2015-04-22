package com.mkl.eu.client.service.vo.event;

import com.mkl.eu.client.service.vo.EuObject;

/**
 * Political event that have occured.
 *
 * @author MKL
 */
public class PoliticalEvent extends EuObject {
    /** Turn when the event occured. */
    private Integer turn;

    /** @return the turn. */
    public Integer getTurn() {
        return turn;
    }

    /** @param turn the turn to set. */
    public void setTurn(Integer turn) {
        this.turn = turn;
    }
}
