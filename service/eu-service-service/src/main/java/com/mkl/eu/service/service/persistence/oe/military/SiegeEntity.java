package com.mkl.eu.service.service.persistence.oe.military;

import com.mkl.eu.client.service.vo.enumeration.SiegeStatusEnum;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity for a siege.
 *
 * @author MKL.
 */
@Entity
@Table(name = "SIEGE")
public class SiegeEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Status. */
    private SiegeStatusEnum status;
    /** Province where the competition occurs. */
    private String province;
    /** Turn of the competition. */
    private Integer turn;
    /** Counters involved in the battle. */
    private Set<SiegeCounterEntity> counters = new HashSet<>();
    /** Game in which the competition occurs. */
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

    /** @return the status. */
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    public SiegeStatusEnum getStatus() {
        return status;
    }

    /** @param status the status to set. */
    public void setStatus(SiegeStatusEnum status) {
        this.status = status;
    }

    /** @return the province. */
    @Column(name = "R_PROVINCE")
    public String getProvince() {
        return province;
    }

    /** @param province the province to set. */
    public void setProvince(String province) {
        this.province = province;
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

    /** @return the counters. */
    @OneToMany(mappedBy = "id.siege", cascade = CascadeType.ALL)
    public Set<SiegeCounterEntity> getCounters() {
        return counters;
    }

    /** @param counters the counters to set. */
    public void setCounters(Set<SiegeCounterEntity> counters) {
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
