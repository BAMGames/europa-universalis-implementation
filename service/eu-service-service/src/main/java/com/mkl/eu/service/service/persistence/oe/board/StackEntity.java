package com.mkl.eu.service.service.persistence.oe.board;

import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.IEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Stack of counters (regroupment).
 *
 * @author MKL
 */
@Entity
@Table(name = "STACK")
public class StackEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Province where the stack is located (String or Province ?). */
    private AbstractProvinceEntity province;
    /** Counters of the stack. */
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

    /** @return the province. */
    @ManyToOne
    @JoinColumn(name = "ID_PROVINCE")
    public AbstractProvinceEntity getProvince() {
        return province;
    }

    /** @param province the province to set. */
    public void setProvince(AbstractProvinceEntity province) {
        this.province = province;
    }

    /** @return the counters. */
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<CounterEntity> getCounters() {
        return counters;
    }

    /** @param counters the counters to set. */
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
