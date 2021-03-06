package com.mkl.eu.client.service.service.chat;

/**
 * Request for speakInRoom service.
 *
 * @author MKL.
 */
public class SpeakInRoomRequest {
    /** Id of the room to speak in. */
    private Long idRoom;
    /** Message to send. */
    private String message;

    /**
     * Constructor for jaxb.
     */
    public SpeakInRoomRequest() {

    }

    /**
     * Constructor.
     *
     * @param idRoom    the idRoom to set.
     * @param message   the message to set.
     */
    public SpeakInRoomRequest(Long idRoom, String message) {
        this.idRoom = idRoom;
        this.message = message;
    }

    /** @return the idRoom. */
    public Long getIdRoom() {
        return idRoom;
    }

    /** @param idRoom the idRoom to set. */
    public void setIdRoom(Long idRoom) {
        this.idRoom = idRoom;
    }

    /** @return the message. */
    public String getMessage() {
        return message;
    }

    /** @param message the message to set. */
    public void setMessage(String message) {
        this.message = message;
    }
}
