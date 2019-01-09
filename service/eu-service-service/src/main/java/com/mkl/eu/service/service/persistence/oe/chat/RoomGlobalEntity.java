package com.mkl.eu.service.service.persistence.oe.chat;

import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Room where people talk. It is a global room.
 *
 * @author MKL.
 */
@Entity
@Table(name = "C_ROOM_GLOBAL")
public class RoomGlobalEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Game owner of the entity. */
    private GameEntity game;
    /** List of messages sent in the room. */
    private List<MessageGlobalEntity> messages = new ArrayList<>();

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

    /** @return the messages. */
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<MessageGlobalEntity> getMessages() {
        return messages;
    }

    /** @param messages the messages to set. */
    public void setMessages(List<MessageGlobalEntity> messages) {
        this.messages = messages;
    }
}
