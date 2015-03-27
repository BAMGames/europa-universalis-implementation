package com.mkl.eu.client.service.vo;

import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.country.Country;
import com.mkl.eu.client.service.vo.country.Relation;
import com.mkl.eu.client.service.vo.enumeration.GameStatusEnum;
import com.mkl.eu.client.service.vo.event.PoliticalEvent;

import java.util.List;
import java.util.Map;

/**
 * Instance of EU game.
 *
 * @author MKL
 */
public class Game extends EuObject {
    /** TODO active countries only or all ? */
    private List<Country> countries;
    /** Relations between the countries. */
    private List<Relation> relations;
    /** Events that have occured in the game. */
    private Map<Integer, List<PoliticalEvent>> events;
    /** counters of the game. */
    private List<Counter> counters;
    /** Turn of the game. */
    private Integer turn;
    /** Status of the game. */
    private GameStatusEnum status;
}
