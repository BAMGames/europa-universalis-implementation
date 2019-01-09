package com.mkl.eu.service.service.persistence.oe.chat;

import com.mkl.eu.service.service.persistence.oe.IEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * Message sent in a classic room (not global).
 * A message is copied to each receiver.
 *
 * @author MKL.
 */
@Entity
@Table(name = "C_CHAT")
public class ChatEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Room where the message was sent. */
    private RoomEntity room;
    /** Receiver of the message. */
    private PlayableCountryEntity receiver;
    /** Date when the message was read. */
    private ZonedDateTime dateRead;
    /** Message sent. */
    private MessageEntity message;

    /** @return the id. */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    /** @param id the id to set. */

    public void setId(Long id) {
        this.id = id;
    }

    /** @return the room. */
    @ManyToOne
    @JoinColumn(name = "ID_C_ROOM", nullable = false)
    public RoomEntity getRoom() {
        return room;
    }

    /** @param room the room to set. */
    public void setRoom(RoomEntity room) {
        this.room = room;
    }

    /** @return the receiver. */
    @ManyToOne
    @JoinColumn(name = "ID_COUNTRY")
    public PlayableCountryEntity getReceiver() {
        return receiver;
    }

    /** @param receiver the receiver to set. */
    public void setReceiver(PlayableCountryEntity receiver) {
        this.receiver = receiver;
    }

    /** @return the dateRead. */
    @Column(name = "DATE_READ")
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentZonedDateTime")
    public ZonedDateTime getDateRead() {
        return dateRead;
    }

    /** @param dateRead the dateRead to set. */
    public void setDateRead(ZonedDateTime dateRead) {
        this.dateRead = dateRead;
    }

    /** @return the message. */
    @ManyToOne
    @JoinColumn(name = "ID_C_MESSAGE")
    @Cascade(CascadeType.SAVE_UPDATE)
    public MessageEntity getMessage() {
        return message;
    }

    /** @param message the message to set. */
    public void setMessage(MessageEntity message) {
        this.message = message;
    }
}
