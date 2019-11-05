package com.mkl.eu.service.service.persistence.oe.diplo;

import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;

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
    // TODO TG-13 add a 'fake' playable country in case of minor alone in war
    private PlayableCountryEntity country;
    /** Position of the country in the turn order. */
    private int position;
    /** Activity of this order segment (ie the one whose it is the turn). */
    private boolean active;
    /** Flag saying that the country is ready for the next phase (currant phase ok). */
    private boolean ready;

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
    @JoinColumn(name = "ID_COUNTRY")
    public PlayableCountryEntity getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(PlayableCountryEntity country) {
        this.country = country;
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
    @Column(name = "ACTIVE", columnDefinition = "BIT default b'0'")
    public boolean isActive() {
        return active;
    }

    /** @param active the active to set. */
    public void setActive(boolean active) {
        this.active = active;
    }

    /** @return the ready. */
    @Column(name = "READY", columnDefinition = "BIT default b'0'")
    public boolean isReady() {
        return ready;
    }

    /** @param ready the ready to set. */
    public void setReady(boolean ready) {
        this.ready = ready;
    }

    /**
     * Embedded class for JPA mapping.
     */
    public static class EmbeddedId implements Serializable {
        /** Composite id game. */
        private GameEntity game;
        /** Composite id country. */
        private PlayableCountryEntity country;

        /** @return the game. */
        public GameEntity getGame() {
            return game;
        }

        /** @param game the game to set. */
        public void setGame(GameEntity game) {
            this.game = game;
        }

        /** @return the country. */
        public PlayableCountryEntity getCountry() {
            return country;
        }

        /** @param country the country to set. */
        public void setCountry(PlayableCountryEntity country) {
            this.country = country;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "EmbeddedId{" +
                    "game=" + game.getId() +
                    ", country=" + country.getName() +
                    '}';
        }
    }
}
