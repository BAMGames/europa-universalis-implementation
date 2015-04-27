package com.mkl.eu.service.service.persistence.oe;

import com.mkl.eu.client.service.vo.enumeration.GameStatusEnum;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.CountryEntity;
import com.mkl.eu.service.service.persistence.oe.event.PoliticalEventEntity;
import com.mkl.eu.service.service.persistence.oe.player.PlayerEntity;
import com.mkl.eu.service.service.persistence.oe.player.RelationEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity for a Game.
 *
 * @author MKL.
 */
@Entity
@Table(name = "GAME")
public class GameEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Players of the game. */
    private List<PlayerEntity> players = new ArrayList<>();
    /** Countries of the game. */
    private List<CountryEntity> countries = new ArrayList<>();
    /** Relations between the countries. */
    private List<RelationEntity> relations = new ArrayList<>();
    /** Events that have occured in the game. */
    private List<PoliticalEventEntity> events = new ArrayList<>();
    /** Stacks of counters of the game. */
    private List<StackEntity> stacks = new ArrayList<>();
    /** Turn of the game. */
    private Integer turn;
    /** Status of the game. */
    private GameStatusEnum status;
    /** Version of the game (technical field). */
    private long version;

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

    /** @return the players. */
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<PlayerEntity> getPlayers() {
        return players;
    }

    /** @param players the players to set. */
    public void setPlayers(List<PlayerEntity> players) {
        this.players = players;
    }

    /** @return the countries. */
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<CountryEntity> getCountries() {
        return countries;
    }

    /** @param countries the countries to set. */
    public void setCountries(List<CountryEntity> countries) {
        this.countries = countries;
    }

    /** @return the relations. */
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<RelationEntity> getRelations() {
        return relations;
    }

    /** @param relations the relations to set. */
    public void setRelations(List<RelationEntity> relations) {
        this.relations = relations;
    }

    /** @return the events. */
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<PoliticalEventEntity> getEvents() {
        return events;
    }

    /** @param events the events to set. */
    public void setEvents(List<PoliticalEventEntity> events) {
        this.events = events;
    }

    /** @return the stacks. */
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<StackEntity> getStacks() {
        return stacks;
    }

    /** @param stacks the stacks to set. */
    public void setStacks(List<StackEntity> stacks) {
        this.stacks = stacks;
    }

    /** @return the turn. */
    @Column(name = "TURN")
    public Integer getTurn() {
        return turn;
    }

    /** @param turn the turn to set. */
    public void setTurn(Integer turn) {
        this.turn = turn;
    }

    /** @return the status. */
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    public GameStatusEnum getStatus() {
        return status;
    }

    /** @param status the status to set. */
    public void setStatus(GameStatusEnum status) {
        this.status = status;
    }

    /** @return the version. */
    @Version
    public long getVersion() {
        return version;
    }

    /** @param version the version to set. */
    public void setVersion(long version) {
        this.version = version;
    }
}
