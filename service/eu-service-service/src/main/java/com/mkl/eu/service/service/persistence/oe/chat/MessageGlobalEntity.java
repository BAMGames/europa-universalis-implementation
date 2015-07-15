package com.mkl.eu.service.service.persistence.oe.chat;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Message sent in a global room.
 *
 * @author MKL.
 */
@Entity
@Table(name = "C_MESSAGE_GLOBAL")
public class MessageGlobalEntity extends AbstractMessageEntity {
    /** Global room where the message was sent. */
    private RoomGlobalEntity room;

    /** @return the room. */
    @ManyToOne
    @JoinColumn(name = "ID_C_ROOM_GLOBAL")
    public RoomGlobalEntity getRoom() {
        return room;
    }

    /** @param room the room to set. */
    public void setRoom(RoomGlobalEntity room) {
        this.room = room;
    }
}
