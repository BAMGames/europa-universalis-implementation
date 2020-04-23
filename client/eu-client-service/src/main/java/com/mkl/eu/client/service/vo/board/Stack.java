package com.mkl.eu.client.service.vo.board;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.enumeration.MovePhaseEnum;

import javax.xml.bind.Unmarshaller;
import java.util.ArrayList;
import java.util.List;

/**
 * Stack of counters (regroupment).
 *
 * @author MKL
 */
public class Stack extends EuObject implements IStack {
    /** Province where the stack is located. */
    private String province;
    /** Phase of the move the stack is (has moved, is moving,..). */
    private MovePhaseEnum movePhase;
    /** Flag saying that the stack is being besieged. */
    private boolean besieged;
    /** Code of the leader controlling the stack. */
    private String leader;
    /** Country controlling the stack. */
    private String country;
    /** Number of movement points already done by this stack. */
    private int move;
    /** Counters of the stack. */
    private List<Counter> counters = new ArrayList<>();

    /** {@inheritDoc} */
    @Override
    public String getProvince() {
        return province;
    }

    /** {@inheritDoc} */
    @Override
    public void setProvince(String province) {
        this.province = province;
    }

    /** {@inheritDoc} */
    @Override
    public MovePhaseEnum getMovePhase() {
        return movePhase;
    }

    /** {@inheritDoc} */
    @Override
    public void setMovePhase(MovePhaseEnum movePhase) {
        this.movePhase = movePhase;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isBesieged() {
        return besieged;
    }

    /** {@inheritDoc} */
    @Override
    public void setBesieged(boolean besieged) {
        this.besieged = besieged;
    }

    /** {@inheritDoc} */
    @Override
    public String getLeader() {
        return leader;
    }

    /** {@inheritDoc} */
    @Override
    public void setLeader(String leader) {
        this.leader = leader;
    }

    /** {@inheritDoc} */
    @Override
    public String getCountry() {
        return country;
    }

    /** {@inheritDoc} */
    @Override
    public void setCountry(String country) {
        this.country = country;
    }

    /** {@inheritDoc} */
    @Override
    public int getMove() {
        return move;
    }

    /** {@inheritDoc} */
    @Override
    public void setMove(int move) {
        this.move = move;
    }

    /** {@inheritDoc} */
    @Override
    public List<Counter> getCounters() {
        return counters;
    }

    /** @param counters the counters to set. */
    public void setCounters(List<Counter> counters) {
        this.counters = counters;
    }

    /**
     * This method is called after all the properties (except IDREF) are unmarshalled for this object,
     * but before this object is set to the parent object.
     *
     * @param unmarshaller the unmarshaller.
     * @param parent       the parent object.
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        if (this.counters != null) {
            for (Counter counter : counters) {
                counter.setOwner(this);
            }
        }
    }
}
