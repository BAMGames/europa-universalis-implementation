package com.mkl.eu.service.service.persistence.oe.chat;

import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Room where people talk. Cannot be a global room.
 *
 * @author MKL.
 */
@Entity
@Table(name = "C_ROOM", uniqueConstraints = {@UniqueConstraint(columnNames = {"ID_GAME", "NAME"})})
public class RoomEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Game owner of the entity. */
    private GameEntity game;
    /** Owner of the room. */
    private PlayableCountryEntity owner;
    /** Name of the room. */
    private String name;
    /** List of attending countries. */
    private List<PresentEntity> presents = new ArrayList<>();
    /** List of messages sent in the room. */
    private List<ChatEntity> messages = new ArrayList<>();

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

    /** @return the game. */
    @ManyToOne
    @JoinColumn(name = "ID_GAME")
    public GameEntity getGame() {
        return game;
    }

    /** @param game the game to set. */
    public void setGame(GameEntity game) {
        this.game = game;
    }

    /** @return the owner. */
    @ManyToOne
    @JoinColumn(name = "ID_COUNTRY")
    public PlayableCountryEntity getOwner() {
        return owner;
    }

    /** @param country the owner to set. */
    public void setOwner(PlayableCountryEntity country) {
        this.owner = country;
    }

    /** @return the name. */
    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    /** @param name the name to set. */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the presents. */
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<PresentEntity> getPresents() {
        return presents;
    }

    /** @param presents the presents to set. */
    public void setPresents(List<PresentEntity> presents) {
        this.presents = presents;
    }

    /** @return the messages. */
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ChatEntity> getMessages() {
        return messages;
    }

    /** @param messages the messages to set. */
    public void setMessages(List<ChatEntity> messages) {
        this.messages = messages;
    }
}
