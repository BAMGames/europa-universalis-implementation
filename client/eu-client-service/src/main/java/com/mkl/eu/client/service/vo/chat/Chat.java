package com.mkl.eu.client.service.vo.chat;

import java.util.ArrayList;
import java.util.List;

/**
 * Object containing all chat-related objects.
 *
 * @author MKL.
 */
public class Chat {
    /** Messages said in the global room. */
    private List<Message> globalMessages = new ArrayList<>();
    /** Rooms seen by the user and the messages in it. */
    private List<Room> rooms = new ArrayList<>();

    /** @return the globalMessages. */
    public List<Message> getGlobalMessages() {
        return globalMessages;
    }

    /** @param globalMessages the globalMessages to set. */
    public void setGlobalMessages(List<Message> globalMessages) {
        this.globalMessages = globalMessages;
    }

    /** @return the rooms. */
    public List<Room> getRooms() {
        return rooms;
    }

    /** @param rooms the rooms to set. */
    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }
}
