package com.mkl.eu.client.service.vo.board;

import com.mkl.eu.client.service.vo.enumeration.MovePhaseEnum;

import java.util.List;

/**
 * Description of the class.
 *
 * @author MKL.
 */
public interface IStack {
    /** @return the province. */
    String getProvince();

    /** @param province the province to set. */
    void setProvince(String province);

    /** @return the movePhase. */
    MovePhaseEnum getMovePhase();

    /** @param movePhase the movePhase to set. */
    void setMovePhase(MovePhaseEnum movePhase);

    /** @return the besieged. */
    boolean isBesieged();

    /** @param besieged the besieged to set. */
    void setBesieged(boolean besieged);

    /** @return the leader. */
    String getLeader();

    /** @param leader the leader to set. */
    void setLeader(String leader);

    /** @return the country. */
    String getCountry();

    /** @param country the country to set. */
    void setCountry(String country);

    /** @return the move. */
    int getMove();

    /** @param move the move to set. */
    void setMove(int move);

    /** @return the counters. */
    List<? extends ICounter> getCounters();
}
