package com.mkl.eu.client.service.vo;

import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.client.service.vo.chat.Chat;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.diplo.CountryOrder;
import com.mkl.eu.client.service.vo.diplo.War;
import com.mkl.eu.client.service.vo.eco.Competition;
import com.mkl.eu.client.service.vo.eco.TradeFleet;
import com.mkl.eu.client.service.vo.enumeration.GameStatusEnum;
import com.mkl.eu.client.service.vo.event.PoliticalEvent;
import com.mkl.eu.client.service.vo.military.Battle;
import com.mkl.eu.client.service.vo.military.Siege;

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
    /** Wars between the countries. */
    private List<War> wars = new ArrayList<>();
    /** Events that have occured in the game. */
    private List<PoliticalEvent> events = new ArrayList<>();
    /** Stacks of counters of the game. */
    private List<Stack> stacks = new ArrayList<>();
    /** Trade fleets of the game. */
    private List<TradeFleet> tradeFleets = new ArrayList<>();
    /** Automatic competitions that happened. */
    private List<Competition> competitions = new ArrayList<>();
    /** Orders of countries in phases. */
    private List<CountryOrder> orders = new ArrayList<>();
    /** Battles of the game. */
    private List<Battle> battles = new ArrayList<>();
    /** Sieges of the game. */
    private List<Siege> sieges = new ArrayList<>();
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

    /** @return the wars. */
    public List<War> getWars() {
        return wars;
    }

    /** @param wars the wars to set. */
    public void setWars(List<War> wars) {
        this.wars = wars;
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

    /** @return the tradeFleets. */
    public List<TradeFleet> getTradeFleets() {
        return tradeFleets;
    }

    /** @param tradeFleets the tradeFleets to set. */
    public void setTradeFleets(List<TradeFleet> tradeFleets) {
        this.tradeFleets = tradeFleets;
    }

    /** @return the competitions. */
    public List<Competition> getCompetitions() {
        return competitions;
    }

    /** @param competitions the competitions to set. */
    public void setCompetitions(List<Competition> competitions) {
        this.competitions = competitions;
    }

    /** @return the orders. */
    public List<CountryOrder> getOrders() {
        return orders;
    }

    /** @param orders the orders to set. */
    public void setOrders(List<CountryOrder> orders) {
        this.orders = orders;
    }

    /** @return the battles. */
    public List<Battle> getBattles() {
        return battles;
    }

    /** @param battles the battles to set. */
    public void setBattles(List<Battle> battles) {
        this.battles = battles;
    }

    /** @return the sieges. */
    public List<Siege> getSieges() {
        return sieges;
    }

    /** @param sieges the sieges to set. */
    public void setSieges(List<Siege> sieges) {
        this.sieges = sieges;
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
