package com.mkl.eu.client.service.vo.tables;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.enumeration.ResultEnum;

/**
 * VO for the discovery table.
 *
 * @author MKL.
 */
public class DiscoveryTable extends EuObject {
    /** Result of the modified dice. */
    private Integer dice;
    /** If it is for land or sea. */
    private boolean land;
    /** If leader should check death. */
    private boolean checkLeader;
    /** If without troops, leader should check death. */
    private boolean checkLeaderNoTroops;
    /** Result of a discovery. */
    private ResultEnum result;

    /** @return the dice. */
    public Integer getDice() {
        return dice;
    }

    /** @param dice the dice to set. */
    public void setDice(Integer dice) {
        this.dice = dice;
    }

    /** @return the land. */
    public boolean isLand() {
        return land;
    }

    /** @param land the land to set. */
    public void setLand(boolean land) {
        this.land = land;
    }

    /** @return the checkLeader. */
    public boolean isCheckLeader() {
        return checkLeader;
    }

    /** @param checkLeader the checkLeader to set. */
    public void setCheckLeader(boolean checkLeader) {
        this.checkLeader = checkLeader;
    }

    /** @return the checkLeaderNoTroops. */
    public boolean isCheckLeaderNoTroops() {
        return checkLeaderNoTroops;
    }

    /** @param checkLeaderNoTroops the checkLeaderNoTroops to set. */
    public void setCheckLeaderNoTroops(boolean checkLeaderNoTroops) {
        this.checkLeaderNoTroops = checkLeaderNoTroops;
    }

    /** @return the result. */
    public ResultEnum getResult() {
        return result;
    }

    /** @param result the result to set. */
    public void setResult(ResultEnum result) {
        this.result = result;
    }
}
