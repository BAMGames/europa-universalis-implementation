package com.mkl.eu.service.service.persistence.oe.player;

import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity for a player. A player manages a country.
 *
 * @author MKL.
 */
@Entity
@Table(name = "PLAYER")
public class PlayerEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** The country being managed by the player. */
    private PlayableCountryEntity country;
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

    /** @return the country. */
    @OneToOne
    @JoinColumn(name = "ID_COUNTRY")
    public PlayableCountryEntity getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(PlayableCountryEntity country) {
        this.country = country;
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
