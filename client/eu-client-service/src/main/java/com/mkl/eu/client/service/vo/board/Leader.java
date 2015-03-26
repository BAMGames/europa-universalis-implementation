package com.mkl.eu.client.service.vo.board;

import com.mkl.eu.client.service.vo.enumeration.LeaderRoleEnum;

/**
 * Counter of type Leader (can be general, admiral,...).
 *
 * @author MKL
 */
public class Leader extends Counter {
    /** Turn when the leader comes to play. */
    private Integer begin;
    /** Turn when the leader dies (he dies at the end of this turn). */
    private Integer end;
    /** Type of the leader. */
    private LeaderRoleEnum role;
    /** Manoeuvre value of the leader. */
    private Integer manoeuvre;
    /** Fire value of the leader. */
    private Integer fire;
    /** Shock value of the leader. */
    private Integer shock;
    /** Siege value of the leader (>= 0). */
    private Integer siege;

    /** @return the begin. */
    public Integer getBegin() {
        return begin;
    }

    /** @param begin the begin to set. */
    public void setBegin(Integer begin) {
        this.begin = begin;
    }

    /** @return the end. */
    public Integer getEnd() {
        return end;
    }

    /** @param end the end to set. */
    public void setEnd(Integer end) {
        this.end = end;
    }

    /** @return the role. */
    public LeaderRoleEnum getRole() {
        return role;
    }

    /** @param role the role to set. */
    public void setRole(LeaderRoleEnum role) {
        this.role = role;
    }

    /** @return the manoeuvre. */
    public Integer getManoeuvre() {
        return manoeuvre;
    }

    /** @param manoeuvre the manoeuvre to set. */
    public void setManoeuvre(Integer manoeuvre) {
        this.manoeuvre = manoeuvre;
    }

    /** @return the fire. */
    public Integer getFire() {
        return fire;
    }

    /** @param fire the fire to set. */
    public void setFire(Integer fire) {
        this.fire = fire;
    }

    /** @return the shock. */
    public Integer getShock() {
        return shock;
    }

    /** @param shock the shock to set. */
    public void setShock(Integer shock) {
        this.shock = shock;
    }

    /** @return the siege. */
    public Integer getSiege() {
        return siege;
    }

    /** @param siege the siege to set. */
    public void setSiege(Integer siege) {
        this.siege = siege;
    }
}
