package com.mkl.eu.service.service.persistence.oe.military;

import com.mkl.eu.client.service.vo.enumeration.BattleStatusEnum;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.IEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity for a battle (land or naval).
 *
 * @author MKL.
 */
@Entity
@Table(name = "BATTLE")
public class BattleEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Province where the battle occurs. */
    private String province;
    /** Turn of the game when the battle occurred. */
    private Integer turn;
    /** Status of the battle. */
    private BattleStatusEnum status;
    /** Flag saying that the phasing players have selected their forces. */
    private Boolean phasingForces;
    /** Flag saying that the non phasing players have selected their forces. */
    private Boolean nonPhasingForces;
    /** Counters involved in the battle. */
    private Set<BattleCounterEntity> counters = new HashSet<>();
    /** Game in which the battle occurs. */
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

    /** @return the status. */
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    public BattleStatusEnum getStatus() {
        return status;
    }

    /** @param status the status to set. */
    public void setStatus(BattleStatusEnum status) {
        this.status = status;
    }

    /** @return the phasingForces. */
    @Column(name = "PHASING_FORCES")
    public Boolean isPhasingForces() {
        return phasingForces;
    }

    /** @param phasingForces the phasingForces to set. */
    public void setPhasingForces(Boolean phasingForces) {
        this.phasingForces = phasingForces;
    }

    /** @return the nonPhasingForces. */
    @Column(name = "NON_PHASING_FORCES")
    public Boolean isNonPhasingForces() {
        return nonPhasingForces;
    }

    /** @param nonPhasingForces the nonPhasingForces to set. */
    public void setNonPhasingForces(Boolean nonPhasingForces) {
        this.nonPhasingForces = nonPhasingForces;
    }

    /** @return the counters. */
    @OneToMany(mappedBy = "id.battle", cascade = CascadeType.ALL)
    public Set<BattleCounterEntity> getCounters() {
        return counters;
    }

    /** @param counters the counters to set. */
    public void setCounters(Set<BattleCounterEntity> counters) {
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
