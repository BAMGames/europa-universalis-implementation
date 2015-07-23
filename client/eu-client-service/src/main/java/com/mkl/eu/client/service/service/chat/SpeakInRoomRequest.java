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
    /** Id of the country sending the message. */
    private Long idCountry;

    /**
     * Constructor.
     */
    public SpeakInRoomRequest() {

    }

    /**
     * Constructor.
     *
     * @param idRoom    the idRoom to set.
     * @param message   the message to set.
     * @param idCountry the idCountry to set.
     */
    public SpeakInRoomRequest(Long idRoom, String message, Long idCountry) {
        this.idRoom = idRoom;
        this.message = message;
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

    /** @return the message. */
    public String getMessage() {
        return message;
    }

    /** @param message the message to set. */
    public void setMessage(String message) {
        this.message = message;
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
