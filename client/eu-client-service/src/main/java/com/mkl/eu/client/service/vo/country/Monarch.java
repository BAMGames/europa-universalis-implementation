package com.mkl.eu.client.service.vo.country;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.board.Leader;

/**
 * Monarch of a country.
 *
 * @author MKL
 */
public class Monarch extends EuObject {
    /** Country the monarch rules. */
    private PlayableCountry country;
    /** Turn when the leader comes to play. */
    private Integer begin;
    /** Turn when the leader dies (he dies at the start of this turn). */
    private Integer end;
    /** Administrative value. */
    private Integer administrative;
    /** Diplomatic value. */
    private Integer diplomacy;
    /** Military value. */
    private Integer military;
    /** Military average value for combats. */
    private Integer militaryAverage;
    /** Military values for combats. */
    private Leader militarySkills;

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
     * @return the begin.
     */
    public Integer getBegin() {
        return begin;
    }

    /**
     * @param begin the begin to set.
     */
    public void setBegin(Integer begin) {
        this.begin = begin;
    }

    /**
     * @return the end.
     */
    public Integer getEnd() {
        return end;
    }

    /**
     * @param end the end to set.
     */
    public void setEnd(Integer end) {
        this.end = end;
    }

    /**
     * @return the administrative.
     */
    public Integer getAdministrative() {
        return administrative;
    }

    /**
     * @param administrative the administrative to set.
     */
    public void setAdministrative(Integer administrative) {
        this.administrative = administrative;
    }

    /**
     * @return the diplomacy.
     */
    public Integer getDiplomacy() {
        return diplomacy;
    }

    /**
     * @param diplomacy the diplomacy to set.
     */
    public void setDiplomacy(Integer diplomacy) {
        this.diplomacy = diplomacy;
    }

    /**
     * @return the military.
     */
    public Integer getMilitary() {
        return military;
    }

    /**
     * @param military the military to set.
     */
    public void setMilitary(Integer military) {
        this.military = military;
    }

    /**
     * @return the militaryAverage.
     */
    public Integer getMilitaryAverage() {
        return militaryAverage;
    }

    /**
     * @param militaryAverage the militaryAverage to set.
     */
    public void setMilitaryAverage(Integer militaryAverage) {
        this.militaryAverage = militaryAverage;
    }

    /**
     * @return the militarySkills.
     */
    public Leader getMilitarySkills() {
        return militarySkills;
    }

    /**
     * @param militarySkills the militarySkills to set.
     */
    public void setMilitarySkills(Leader militarySkills) {
        this.militarySkills = militarySkills;
    }
}
