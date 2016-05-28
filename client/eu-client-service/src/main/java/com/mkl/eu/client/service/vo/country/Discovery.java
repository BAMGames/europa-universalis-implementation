package com.mkl.eu.client.service.vo.country;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.board.Stack;

/**
 * Discovery of a province of the ROTW.
 *
 * @author MKL
 */
public class Discovery extends EuObject {
    /** Country the discovery belongs to. */
    private PlayableCountry country;
    /** Province of the discovery. */
    private String province;
    /** Stack where the discovery is if being repatriated. */
    private Stack stack;
    /** Turn it was repatriated in a national province (<code>null</code> if on going). */
    private Integer turn;

    /**
     * @return the country.
     */
    public PlayableCountry getCountry() {
        return country;
    }

    /**
     * @param country the country to set.
     */
    public void setCountry(PlayableCountry country) {
        this.country = country;
    }

    /**
     * @return the province.
     */
    public String getProvince() {
        return province;
    }

    /**
     * @param province the province to set.
     */
    public void setProvince(String province) {
        this.province = province;
    }

    /**
     * @return the stack.
     */
    public Stack getStack() {
        return stack;
    }

    /**
     * @param stack the stack to set.
     */
    public void setStack(Stack stack) {
        this.stack = stack;
    }

    /**
     * @return the turn.
     */
    public Integer getTurn() {
        return turn;
    }

    /**
     * @param turn the turn to set.
     */
    public void setTurn(Integer turn) {
        this.turn = turn;
    }
}
