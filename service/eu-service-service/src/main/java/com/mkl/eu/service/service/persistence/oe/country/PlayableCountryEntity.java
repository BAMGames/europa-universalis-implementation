package com.mkl.eu.service.service.persistence.oe.country;

import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Country (can be minor or major).
 *
 * @author MKL
 */
@Entity
@Table(name = "COUNTRY")
public class PlayableCountryEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /**
     * Name of the country.
     */
    private String name;
    /**
     * Counters of the country.
     */
    private List<CounterEntity> counters = new ArrayList<>();
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

    /**
     * @return the name.
     */
    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the counters.
     */
    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<CounterEntity> getCounters() {
        return counters;
    }

    /**
     * @param counters the counters to set.
     */
    public void setCounters(List<CounterEntity> counters) {
        this.counters = counters;
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
