package com.mkl.eu.client.service.vo;

import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.client.service.vo.chat.Chat;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.country.Relation;
import com.mkl.eu.client.service.vo.diplo.CountryOrder;
import com.mkl.eu.client.service.vo.enumeration.GameStatusEnum;
import com.mkl.eu.client.service.vo.event.PoliticalEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Instance of EU game.
 *
 * @author MKL
 */
public class Game extends EuObject {
    /** Stacks of countries of the game. */
    private List<PlayableCountry> countries = new ArrayList<>();
    /** Relations between the countries. */
    private List<Relation> relations = new ArrayList<>();
    /** Events that have occured in the game. */
    private List<PoliticalEvent> events = new ArrayList<>();
    /** Stacks of counters of the game. */
    private List<Stack> stacks = new ArrayList<>();
    /** Orders of countries in phases. */
    private List<CountryOrder> orders = new ArrayList<>();
    /** All the chat-related stuff. */
    private Chat chat;
    /** Turn of the game. */
    private Integer turn;
    /** Status of the game. */
    private GameStatusEnum status;
    /** Version of the game. */
    private long version;

    /** @return the countries. */
    public List<PlayableCountry> getCountries() {
        return countries;
    }

    /** @param countries the countries to set. */
    public void setCountries(List<PlayableCountry> countries) {
        this.countries = countries;
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

    /** @return the orders. */
    public List<CountryOrder> getOrders() {
        return orders;
    }

    /** @param orders the orders to set. */
    public void setOrders(List<CountryOrder> orders) {
        this.orders = orders;
    }

    /** @return the chat. */
    public Chat getChat() {
        return chat;
    }

    /** @param chat the chat to set. */
    public void setChat(Chat chat) {
        this.chat = chat;
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

    /** @return the version. */
    public long getVersion() {
        return version;
    }

    /** @param version the version to set. */
    public void setVersion(long version) {
        this.version = version;
    }
}
