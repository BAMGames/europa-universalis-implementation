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
    /** Flag saying that the attacker has selected his forces. */
    private Boolean attackerForces;
    /** Flag saying that the defender has selected his forces. */
    private Boolean defenderForces;
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

    /** @return the attackerForces. */
    @Column(name = "ATTACKER_FORCES")
    public Boolean isAttackerForces() {
        return attackerForces;
    }

    /** @param attackerForces the attackerForces to set. */
    public void setAttackerForces(Boolean attackerForces) {
        this.attackerForces = attackerForces;
    }

    /** @return the defenderForces. */
    @Column(name = "DEFENDER_FORCES")
    public Boolean isDefenderForces() {
        return defenderForces;
    }

    /** @param defenderForces the defenderForces to set. */
    public void setDefenderForces(Boolean defenderForces) {
        this.defenderForces = defenderForces;
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
