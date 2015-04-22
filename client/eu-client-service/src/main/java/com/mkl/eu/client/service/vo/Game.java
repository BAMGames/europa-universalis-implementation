package com.mkl.eu.client.service.vo;

import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.client.service.vo.enumeration.GameStatusEnum;
import com.mkl.eu.client.service.vo.event.PoliticalEvent;
import com.mkl.eu.client.service.vo.player.Player;
import com.mkl.eu.client.service.vo.player.Relation;

import java.util.ArrayList;
import java.util.List;

/**
 * Instance of EU game.
 *
 * @author MKL
 */
public class Game extends EuObject {
    /** Players of the game. */
    private List<Player> players = new ArrayList<>();
    /** Relations between the countries. */
    private List<Relation> relations = new ArrayList<>();
    /** Events that have occured in the game. */
    private List<PoliticalEvent> events = new ArrayList<>();
    /** Stacks of counters of the game. */
    private List<Stack> stacks = new ArrayList<>();
    /** Turn of the game. */
    private Integer turn;
    /** Status of the game. */
    private GameStatusEnum status;

    /** @return the players. */
    public List<Player> getPlayers() {
        return players;
    }

    /** @param players the players to set. */
    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    /** @return the relations. */
    public List<Relation> getRelations() {
        return relations;
    }

    /** @param relations the relations to set. */
    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }

    /** @return the events. */
    public List<PoliticalEvent> getEvents() {
        return events;
    }

    /** @param events the events to set. */
    public void setEvents(List<PoliticalEvent> events) {
        this.events = events;
    }

    /** @return the stacks. */
    public List<Stack> getStacks() {
        return stacks;
    }

    /** @param stacks the stacks to set. */
    public void setStacks(List<Stack> stacks) {
        this.stacks = stacks;
    }

    /** @return the turn. */
    public Integer getTurn() {
        return turn;
    }

    /** @param turn the turn to set. */
    public void setTurn(Integer turn) {
        this.turn = turn;
    }

    /** @return the status. */
    public GameStatusEnum getStatus() {
        return status;
    }

    /** @param status the status to set. */
    public void setStatus(GameStatusEnum status) {
        this.status = status;
    }
}
