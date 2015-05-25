package com.mkl.eu.service.service.persistence.oe.country;

import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import com.mkl.eu.service.service.persistence.oe.eco.EconomicalSheetEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Country (major or former major or future major one).
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
    /** Name of the player. External functional id. */
    private String username;
    /**
     * Economical sheet by turn of the country.
     */
    private List<EconomicalSheetEntity> economicalSheets;
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

    /** @return the username. */
    @Column(name = "USERNAME")
    public String getUsername() {
        return username;
    }

    /** @param username the username to set. */
    public void setUsername(String username) {
        this.username = username;
    }

    /** @return the economicalSheets. */
    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<EconomicalSheetEntity> getEconomicalSheets() {
        return economicalSheets;
    }

    /** @param economicalSheets the economicalSheets to set. */
    public void setEconomicalSheets(List<EconomicalSheetEntity> economicalSheets) {
        this.economicalSheets = economicalSheets;
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
