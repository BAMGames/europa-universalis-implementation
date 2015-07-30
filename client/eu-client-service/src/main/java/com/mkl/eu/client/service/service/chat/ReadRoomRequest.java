package com.mkl.eu.client.service.service.chat;

/**
 * Request for inviteKickRoom service.
 *
 * @author MKL.
 */
public class ReadRoomRequest {
    /** Id of the room to speak in. */
    private Long idRoom;
    /** Maximum id of messages in this room to mark as read. */
    private Long maxId;

    /**
     * Constructor for jaxb.
     */
    public ReadRoomRequest() {

    }

    /**
     * Constructor.
     *
     * @param idRoom the idRoom to set.
     * @param maxId  the maxId to set.
     */
    public ReadRoomRequest(Long idRoom, Long maxId) {
        this.idRoom = idRoom;
        this.maxId = maxId;
    }

    /** @return the idRoom. */
    public Long getIdRoom() {
        return idRoom;
    }

    /** @param idRoom the idRoom to set. */
    public void setIdRoom(Long idRoom) {
        this.idRoom = idRoom;
    }

    /** @return the maxId. */
    public Long getMaxId() {
        return maxId;
    }

    /** @param maxId the maxId to set. */
    public void setMaxId(Long maxId) {
        this.maxId = maxId;
    }
}
