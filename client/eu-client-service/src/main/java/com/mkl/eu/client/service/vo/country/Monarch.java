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
    private Country country;
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
}
