package com.mkl.eu.service.service.persistence.oe.chat;

import com.mkl.eu.service.service.persistence.oe.IEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * Abstract entity that holds the common column of various message entities.
 * The ids are shared between the MessageEntity and MessageGlobalEntity and it
 * is intended to do so. Do not insert a MessageEntity and a MessageGlobalEntity
 * with the same id or the mapping will fail.
 *
 * @author MKL.
 */
@MappedSuperclass
public class AbstractMessageEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Sender of the message. */
    private PlayableCountryEntity sender;
    /** Date when the message was sent. */
    private ZonedDateTime dateSent;
    /** Message sent. */
    private String message;

    /** @return the id. */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    /** @param id the id to set. */

    public void setId(Long id) {
        this.id = id;
    }

    /** @return the sender. */
    @ManyToOne
    @JoinColumn(name = "ID_COUNTRY")
    public PlayableCountryEntity getSender() {
        return sender;
    }

    /** @param sender the sender to set. */
    public void setSender(PlayableCountryEntity sender) {
        this.sender = sender;
    }

    /** @return the dateSent. */
    @Column(name = "DATE_SENT")
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentZonedDateTime")
    public ZonedDateTime getDateSent() {
        return dateSent;
    }

    /** @param dateSent the dateSent to set. */
    public void setDateSent(ZonedDateTime dateSent) {
        this.dateSent = dateSent;
    }

    /** @return the message. */
    @Column(name = "MESSAGE")
    public String getMessage() {
        return message;
    }

    /** @param message the message to set. */
    public void setMessage(String message) {
        this.message = message;
    }
}
