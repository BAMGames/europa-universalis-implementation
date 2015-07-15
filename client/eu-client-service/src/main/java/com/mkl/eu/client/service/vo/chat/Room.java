package com.mkl.eu.client.service.vo.chat;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.country.PlayableCountry;

import java.util.ArrayList;
import java.util.List;

/**
 * Room where people talk.
 *
 * @author MKL.
 */
public class Room extends EuObject {
    /** Name of the global room. */
    public final static String NAME_GLOBAL = "global";
    /** Name of the room. Can't be NAME_GLOBAL. */
    private String name;
    /** Country who created the room. <code>null</code> for global. */
    private PlayableCountry owner;
    /** List of countries in the room. Empty for global. */
    private List<PlayableCountry> countries = new ArrayList<>();
    /** Flag saying that the room is visible in the interface. Always <code>true</code> for global. */
    private boolean visible;
    /** Flag saying that the player is still in the room and can speak in it. */
    private boolean present;
    /** Messages said in the room. */
    private List<Message> messages = new ArrayList<>();

    /** @return the name. */
    public String getName() {
        return name;
    }

    /** @param name the name to set. */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the owner. */
    public PlayableCountry getOwner() {
        return owner;
    }

    /** @param owner the owner to set. */
    public void setOwner(PlayableCountry owner) {
        this.owner = owner;
    }

    /** @return the countries. */
    public List<PlayableCountry> getCountries() {
        return countries;
    }

    /** @param countries the countries to set. */
    public void setCountries(List<PlayableCountry> countries) {
        this.countries = countries;
    }

    /** @return the visible. */
    public boolean isVisible() {
        return visible;
    }

    /** @param visible the visible to set. */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /** @return the present. */
    public boolean isPresent() {
        return present;
    }

    /** @param present the present to set. */
    public void setPresent(boolean present) {
        this.present = present;
    }

    /** @return the messages. */
    public List<Message> getMessages() {
        return messages;
    }

    /** @param messages the messages to set. */
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
