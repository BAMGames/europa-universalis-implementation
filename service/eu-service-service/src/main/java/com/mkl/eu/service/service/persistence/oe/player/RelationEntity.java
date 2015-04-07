package com.mkl.eu.service.service.persistence.oe.player;

import com.mkl.eu.client.service.vo.enumeration.RelationTypeEnum;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.IEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity for a relation between two players.
 *
 * @author MKL
 */
@Entity
@Table(name = "RELATION")
public class RelationEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Owner of the relation (the one who responsible of it). */
    private PlayerEntity first;
    /** The other player of the relation (may be multiple ?). */
    private PlayerEntity second;
    /** Type of the relation. */
    private RelationTypeEnum type;
    /** Game of the entity. */
    private GameEntity game;

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

    /** @return the first. */
    @ManyToOne
    @JoinColumn(name = "ID_PLAYER_FIRST", nullable = true)
    public PlayerEntity getFirst() {
        return first;
    }

    /** @param first the first to set. */
    public void setFirst(PlayerEntity first) {
        this.first = first;
    }

    /** @return the second. */
    @ManyToOne
    @JoinColumn(name = "ID_PLAYER_SECOND", nullable = true)
    public PlayerEntity getSecond() {
        return second;
    }

    /** @param second the second to set. */
    public void setSecond(PlayerEntity second) {
        this.second = second;
    }

    /** @return the type. */
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE")
    public RelationTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(RelationTypeEnum type) {
        this.type = type;
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
}