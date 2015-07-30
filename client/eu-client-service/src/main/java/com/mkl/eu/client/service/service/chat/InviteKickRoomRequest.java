package com.mkl.eu.client.service.service.chat;

/**
 * Request for inviteKickRoom service.
 *
 * @author MKL.
 */
public class InviteKickRoomRequest {
    /** Id of the room to speak in. */
    private Long idRoom;
    /** Flag saying that the user wants to invite (<code>true</code>) or kick (<code>false</code>). */
    private Boolean invite;
    /** Id of the country to be invited/kicked. */
    private Long idCountry;

    /**
     * Constructor for jaxb.
     */
    public InviteKickRoomRequest() {

    }

    /**
     * Constructor.
     *
     * @param idRoom    the idRoom to set.
     * @param invite    the invite to set.
     * @param idCountry the idCountry to set.
     */
    public InviteKickRoomRequest(Long idRoom, Boolean invite, Long idCountry) {
        this.idRoom = idRoom;
        this.invite = invite;
        this.idCountry = idCountry;
    }

    /** @return the idRoom. */
    public Long getIdRoom() {
        return idRoom;
    }

    /** @param idRoom the idRoom to set. */
    public void setIdRoom(Long idRoom) {
        this.idRoom = idRoom;
    }

    /** @return the invite. */
    public Boolean isInvite() {
        return invite;
    }

    /** @param invite the invite to set. */
    public void setInvite(Boolean invite) {
        this.invite = invite;
    }

    /** @return the idCountry. */
    public Long getIdCountry() {
        return idCountry;
    }

    /** @param idCountry the idCountry to set. */
    public void setIdCountry(Long idCountry) {
        this.idCountry = idCountry;
    }
}
