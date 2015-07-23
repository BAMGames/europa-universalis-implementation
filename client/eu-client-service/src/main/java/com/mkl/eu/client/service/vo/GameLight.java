package com.mkl.eu.client.service.vo;

import com.mkl.eu.client.service.vo.enumeration.GameStatusEnum;

/**
 * Light object for an EU game (used in List for example).
 *
 * @author MKL
 */
public class GameLight extends EuObject {
    /** Id of the country played (can be <code>null</code> given the search). */
    private Long idCountry;
    /** Name of the country played (can be <code>null</code> given the search). */
    private String country;
    /** Number of unread messages (not including global messages). */
    private long unreadMessages;
    /** Turn of the game. */
    private Integer turn;
    /** Status of the game. */
    private GameStatusEnum status;
    /** Flag saying that the game is waiting for the player action. */
    private boolean active;

    /** @return the idCountry. */
    public Long getIdCountry() {
        return idCountry;
    }

    /** @param idCountry the idCountry to set. */
    public void setIdCountry(Long idCountry) {
        this.idCountry = idCountry;
    }

    /** @return the country. */
    public String getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(String country) {
        this.country = country;
    }

    /** @return the unreadMessages. */
    public long getUnreadMessages() {
        return unreadMessages;
    }

    /** @param unreadMessages the unreadMessages to set. */
    public void setUnreadMessages(long unreadMessages) {
        this.unreadMessages = unreadMessages;
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

    /** @return the active. */
    public boolean isActive() {
        return active;
    }

    /** @param active the active to set. */
    public void setActive(boolean active) {
        this.active = active;
    }
}
