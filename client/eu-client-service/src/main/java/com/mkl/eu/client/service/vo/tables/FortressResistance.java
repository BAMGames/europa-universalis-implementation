package com.mkl.eu.client.service.vo.tables;

import com.mkl.eu.client.service.vo.EuObject;

/**
 * VO for the fortress resistance table.
 *
 * @author MKL.
 */
public class FortressResistance extends EuObject {
    /** Level of the fortress under siege. */
    private Integer fortress;
    /** Round resistance of the fortress. */
    private Integer round;
    /** Third resistance of the fortress. */
    private Integer third;
    /** If the fortress is breached. */
    private boolean breach;

    /** @return the fortress. */
    public Integer getFortress() {
        return fortress;
    }

    /** @param fortress the fortress to set. */
    public void setFortress(Integer fortress) {
        this.fortress = fortress;
    }

    /** @return the round. */
    public Integer getRound() {
        return round;
    }

    /** @param round the round to set. */
    public void setRound(Integer round) {
        this.round = round;
    }

    /** @return the third. */
    public Integer getThird() {
        return third;
    }

    /** @param third the third to set. */
    public void setThird(Integer third) {
        this.third = third;
    }

    /** @return the breach. */
    public boolean isBreach() {
        return breach;
    }

    /** @param breach the breach to set. */
    public void setBreach(boolean breach) {
        this.breach = breach;
    }
}
