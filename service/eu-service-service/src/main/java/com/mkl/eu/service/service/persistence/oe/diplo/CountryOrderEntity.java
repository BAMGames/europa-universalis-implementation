package com.mkl.eu.service.service.persistence.oe.diplo;

import com.mkl.eu.client.service.vo.enumeration.GameStatusEnum;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.ref.country.CountryEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Turn order of countries.
 *
 * @author MKL.
 */
@Entity
@Table(name = "COUNTRY_ORDER")
@IdClass(CountryOrderEntity.EmbeddedId.class)
public class CountryOrderEntity {
    /** Game concerned by the turn order. */
    private GameEntity game;
    /** Country concerned by the turn order. */
    // FIXME add a 'fake' playable country in case of minor alone in war
    private CountryEntity country;
    /** Phase of the game concerned by the turn order. For military phases, it is MILITARY_MOVE. */
    private GameStatusEnum gameStatus;
    /** Position of the country in the turn order. */
    private int position;
    /** Activity of this order segment (ie the one whose it is the turn). */
    private Boolean active;

    /** @return the game. */
    @Id
    @ManyToOne
    @JoinColumn(name = "ID_GAME")
    public GameEntity getGame() {
        return game;
    }

    /** @param game the game to set. */
    public void setGame(GameEntity game) {
        this.game = game;
    }

    /** @return the country. */
    @Id
    @ManyToOne
    @JoinColumn(name = "R_COUNTRY", referencedColumnName = "NAME")
    public CountryEntity getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(CountryEntity country) {
        this.country = country;
    }

    /** @return the gameStatus. */
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "GAME_STATUS")
    public GameStatusEnum getGameStatus() {
        return gameStatus;
    }

    /** @param gameStatus the gameStatus to set. */
    public void setGameStatus(GameStatusEnum gameStatus) {
        this.gameStatus = gameStatus;
    }

    /** @return the position. */
    @Column(name = "POSITION")
    public int getPosition() {
        return position;
    }

    /** @param position the position to set. */
    public void setPosition(int position) {
        this.position = position;
    }

    /** @return the active. */
    @Column(name = "ACTIVE")
    public Boolean isActive() {
        return active;
    }

    /** @param active the active to set. */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * Embedded class for JPA mapping.
     */
    public static class EmbeddedId implements Serializable {
        /** Composite id game. */
        private GameEntity game;
        /** Composite id country. */
        private CountryEntity country;
        /** Composite id status. */
        private GameStatusEnum gameStatus;

        /** @return the game. */
        public GameEntity getGame() {
            return game;
        }

        /** @param game the game to set. */
        public void setGame(GameEntity game) {
            this.game = game;
        }

        /** @return the country. */
        public CountryEntity getCountry() {
            return country;
        }

        /** @param country the country to set. */
        public void setCountry(CountryEntity country) {
            this.country = country;
        }

        /** @return the gameStatus. */
        public GameStatusEnum getGameStatus() {
            return gameStatus;
        }

        /** @param gameStatus the gameStatus to set. */
        public void setGameStatus(GameStatusEnum gameStatus) {
            this.gameStatus = gameStatus;
        }
    }
}
