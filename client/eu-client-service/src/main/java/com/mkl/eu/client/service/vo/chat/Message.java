package com.mkl.eu.client.service.vo.chat;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import org.joda.time.DateTime;

/**
 * Message sent/received in a room.
 *
 * @author MKL.
 */
public class Message extends EuObject {
    /** Sender of the message. */
    private PlayableCountry sender;
    /** Date sent of the message. */
    private DateTime dateSent;
    /** Date read of the message. Always <code>null</code> for global messages. */
    private DateTime dateRead;
    /** Value of the message. */
    private String message;

    /** @return the sender. */
    public PlayableCountry getSender() {
        return sender;
    }

    /** @param sender the sender to set. */
    public void setSender(PlayableCountry sender) {
        this.sender = sender;
    }

    /** @return the dateSent. */
    public DateTime getDateSent() {
        return dateSent;
    }

    /** @param dateSent the dateSent to set. */
    public void setDateSent(DateTime dateSent) {
        this.dateSent = dateSent;
    }

    /** @return the dateRead. */
    public DateTime getDateRead() {
        return dateRead;
    }

    /** @param dateRead the dateRead to set. */
    public void setDateRead(DateTime dateRead) {
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
}
