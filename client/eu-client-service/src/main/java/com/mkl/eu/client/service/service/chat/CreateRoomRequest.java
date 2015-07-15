package com.mkl.eu.client.service.service.chat;

/**
 * Request for createRoom service.
 *
 * @author MKL.
 */
public class CreateRoomRequest {
    /** Name of the room to create. */
    private String name;

    /** @return the name. */
    public String getName() {
        return name;
    }

    /** @param name the name to set. */
    public void setName(String name) {
        this.name = name;
    }
}
