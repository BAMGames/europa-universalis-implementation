package com.mkl.eu.service.service.persistence.oe.military;

import com.mkl.eu.client.service.vo.enumeration.BattleEndEnum;
import com.mkl.eu.client.service.vo.enumeration.BattleStatusEnum;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

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
    /** Cause of battle end. */
    private BattleEndEnum end;
    /** Phasing side. */
    private BattleSideEntity phasing = new BattleSideEntity();
    /** Non phasing side. */
    private BattleSideEntity nonPhasing = new BattleSideEntity();
    /** Counters involved in the battle. */
    private Set<BattleCounterEntity> counters = new HashSet<>();
    /** Game in which the battle occurs. */
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

    /** @return the end. */
    @Enumerated(EnumType.STRING)
    @Column(name = "END")
    public BattleEndEnum getEnd() {
        return end;
    }

    /** @param end the end to set. */
    public void setEnd(BattleEndEnum end) {
        this.end = end;
    }

    /** @return the phasingSide. */
    @Embedded
    public BattleSideEntity getPhasing() {
        return phasing;
    }

    /** @param phasingSide the phasingSide to set. */
    public void setPhasing(BattleSideEntity phasingSide) {
        this.phasing = phasingSide;
    }

    /** @return the nonPhasingSide. */
    @Embedded
    public BattleSideEntity getNonPhasing() {
        return nonPhasing;
    }

    /** @param nonPhasingSide the nonPhasingSide to set. */
    public void setNonPhasing(BattleSideEntity nonPhasingSide) {
        this.nonPhasing = nonPhasingSide;
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
