package com.mkl.eu.service.service.persistence.oe.country;

import com.mkl.eu.client.service.vo.enumeration.RelationTypeEnum;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

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
    private PlayableCountryEntity first;
    /** The other player of the relation (may be multiple ?). */
    private PlayableCountryEntity second;
    /** Type of the relation. */
    private RelationTypeEnum type;
    /** Game of the entity. */
    private GameEntity game;

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

    /** @return the first. */
    @ManyToOne
    @JoinColumn(name = "ID_COUNTRY_FIRST", nullable = true)
    public PlayableCountryEntity getFirst() {
        return first;
    }

    /** @param first the first to set. */
    public void setFirst(PlayableCountryEntity first) {
        this.first = first;
    }

    /** @return the second. */
    @ManyToOne
    @JoinColumn(name = "ID_COUNTRY_SECOND", nullable = true)
    public PlayableCountryEntity getSecond() {
        return second;
    }

    /** @param second the second to set. */
    public void setSecond(PlayableCountryEntity second) {
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