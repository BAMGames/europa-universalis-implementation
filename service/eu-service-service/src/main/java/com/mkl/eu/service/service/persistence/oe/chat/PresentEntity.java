package com.mkl.eu.service.service.persistence.oe.chat;

import com.mkl.eu.service.service.persistence.oe.IEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity that describes if a player is or was in a room.
 *
 * @author MKL.
 */
@Entity
@Table(name = "C_PRESENT")
public class PresentEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Room. */
    private RoomEntity room;
    /** Country. */
    private PlayableCountryEntity country;
    /** Flag saying that the country is present in the room. */
    private Boolean present;
    /** Flag saying that the country wants to see this room in his interface. */
    private Boolean visible;

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

    /** @return the country. */
    @ManyToOne
    @JoinColumn(name = "ID_COUNTRY")
    public PlayableCountryEntity getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(PlayableCountryEntity country) {
        this.country = country;
    }

    /** @return the present. */
    @Column(name = "PRESENT", columnDefinition = "BIT")
    public Boolean isPresent() {
        return present;
    }

    /** @param present the present to set. */
    public void setPresent(Boolean present) {
        this.present = present;
    }

    /** @return the visible. */
    @Column(name = "VISIBLE", columnDefinition = "BIT")
    public Boolean isVisible() {
        return visible;
    }

    /** @param visible the visible to set. */
    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
}
