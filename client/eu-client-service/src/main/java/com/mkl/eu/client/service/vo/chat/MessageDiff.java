package com.mkl.eu.client.service.vo.chat;

import com.mkl.eu.client.service.vo.EuObject;

import java.time.ZonedDateTime;

/**
 * Message received since last time information were asked on this game.
 *
 * @author MKL.
 */
public class MessageDiff extends EuObject {
    /** Date sent of the message. */
    private ZonedDateTime dateSent;
    /** Date read of the message. Always <code>null</code> for global messages. */
    private ZonedDateTime dateRead;
    /** Value of the message. */
    private String message;
    /** Id of the sender of the message. */
    private Long idSender;
    /** Id of the room of the message, <code>null</code> for global room. */
    private Long idRoom;

    /** @return the dateSent. */
    public ZonedDateTime getDateSent() {
        return dateSent;
    }

    /** @param dateSent the dateSent to set. */
    public void setDateSent(ZonedDateTime dateSent) {
        this.dateSent = dateSent;
    }

    /** @return the dateRead. */
    public ZonedDateTime getDateRead() {
        return dateRead;
    }

    /** @param dateRead the dateRead to set. */
    public void setDateRead(ZonedDateTime dateRead) {
        this.dateRead = dateRead;
    }

    /** @return the message. */
    public String getMessage() {
        return message;
    }

    /** @param message the message to set. */
    public void setMessage(String message) {
        this.message = message;
    }

    /** @return the idSender. */
    public Long getIdSender() {
        return idSender;
    }

    /** @param idSender the idSender to set. */
    public void setIdSender(Long idSender) {
        this.idSender = idSender;
    }

    /** @return the idRoom. */
    public Long getIdRoom() {
        return idRoom;
    }

    /** @param idRoom the idRoom to set. */
    public void setIdRoom(Long idRoom) {
        this.idRoom = idRoom;
    }
}
