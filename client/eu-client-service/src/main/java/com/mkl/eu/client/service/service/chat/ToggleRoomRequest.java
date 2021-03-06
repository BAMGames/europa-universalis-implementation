package com.mkl.eu.client.service.service.chat;

/**
 * Request for toggleRoom service.
 *
 * @author MKL.
 */
public class ToggleRoomRequest {
    /** Id of the room to speak in. */
    private Long idRoom;
    /** Flag saying that the user wants to set it visible or not. */
    private Boolean visible;

    /**
     * Constructor for jaxb.
     */
    public ToggleRoomRequest() {

    }

    /**
     * Constructor.
     *
     * @param idRoom    the idRoom to set.
     * @param visible   the visible to set.
     */
    public ToggleRoomRequest(Long idRoom, Boolean visible) {
        this.idRoom = idRoom;
        this.visible = visible;
    }

    /** @return the idRoom. */
    public Long getIdRoom() {
        return idRoom;
    }

    /** @param idRoom the idRoom to set. */
    public void setIdRoom(Long idRoom) {
        this.idRoom = idRoom;
    }

    /** @return the visible. */
    public Boolean isVisible() {
        return visible;
    }

    /** @param visible the visible to set. */
    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
}
