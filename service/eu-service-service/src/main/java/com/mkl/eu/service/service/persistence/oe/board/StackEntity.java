package com.mkl.eu.service.service.persistence.oe.board;

import com.mkl.eu.client.service.vo.enumeration.MovePhaseEnum;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import com.mkl.eu.service.service.persistence.oe.country.DiscoveryEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Stack of counters (regrouping).
 *
 * @author MKL
 */
@Entity
@Table(name = "STACK")
public class StackEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Name of the province where the stack is located. */
    private String province;
    /** Phase of the move the stack is (has moved, is moving,..). */
    private MovePhaseEnum movePhase;
    /** Flag saying that the stack is being besieged. */
    private boolean besieged;
    /** Country controlling the stack. */
    private String country;
    /** Number of movement points already done by this stack. */
    private int move;
    /** Counters of the stack. */
    private List<CounterEntity> counters = new ArrayList<>();
    /**
     * Discoveries of the stack (the one being repatriated).
     */
    private List<DiscoveryEntity> discoveries = new ArrayList<>();
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
    @Column(name = "R_PROVINCE")
    public String getProvince() {
        return province;
    }

    /** @param province the province to set. */
    public void setProvince(String province) {
        this.province = province;
    }

    /** @return the movePhase. */
    @Enumerated(EnumType.STRING)
    @Column(name = "MOVE_PHASE")
    public MovePhaseEnum getMovePhase() {
        return movePhase;
    }

    /** @param movePhase the movePhase to set. */
    public void setMovePhase(MovePhaseEnum movePhase) {
        this.movePhase = movePhase;
    }

    /** @return the besieged. */
    @Column(name = "BESIEGED", columnDefinition = "BIT default b'0'")
    public boolean isBesieged() {
        return besieged;
    }

    /** @param besieged the besieged to set. */
    public void setBesieged(boolean besieged) {
        this.besieged = besieged;
    }

    /**
     * Thanks Hibernate to delete orphan when moving a child from a parent to another.
     *
     * @return the counters.
     */
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = false)
    public List<CounterEntity> getCounters() {
        return counters;
    }

    /** @param counters the counters to set. */
    public void setCounters(List<CounterEntity> counters) {
        this.counters = counters;
    }

    /** @return the discoveries. */
    @OneToMany(mappedBy = "stack", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<DiscoveryEntity> getDiscoveries() {
        return discoveries;
    }

    /** @param discoveries the discoveries to set. */
    public void setDiscoveries(List<DiscoveryEntity> discoveries) {
        this.discoveries = discoveries;
    }

    /** @return the country. */
    @Column(name = "R_COUNTRY")
    public String getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(String country) {
        this.country = country;
    }

    /** @return the move. */
    @Column(name = "MOVE")
    public int getMove() {
        return move;
    }

    /** @param move the move to set. */
    public void setMove(int move) {
        this.move = move;
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
