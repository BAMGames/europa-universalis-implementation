package com.mkl.eu.service.service.persistence.oe.attrition;

import com.mkl.eu.client.service.vo.enumeration.AttritionStatusEnum;
import com.mkl.eu.client.service.vo.enumeration.AttritionTypeEnum;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity for an attrition.
 *
 * @author MKL.
 */
@Entity
@Table(name = "ATTRITION")
public class AttritionEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Turn of the game when the battle occurred. */
    private Integer turn;
    /** Type of the attrition. */
    private AttritionTypeEnum type;
    /** Status of the attrition. */
    private AttritionStatusEnum status;
    /** Counters involved in the attrition. */
    private Set<AttritionCounterEntity> counters = new HashSet<>();
    /** Provinces where the stack went through during the attrition. */
    private Set<String> provinces = new HashSet<>();
    /** Size of the stack (in case of movement, it is the maximum size reached). */
    private double size;
    /** Technology of the stack. */
    private String tech;
    /** Bonus to the attrition die roll. */
    private Integer bonus;
    /** Unmodified die roll of the attrition. */
    private Integer die;
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

    /** @return the turn. */
    public Integer getTurn() {
        return turn;
    }

    /** @param turn the turn to set. */
    public void setTurn(Integer turn) {
        this.turn = turn;
    }

    /** @return the type. */
    @Enumerated(EnumType.STRING)
    public AttritionTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(AttritionTypeEnum type) {
        this.type = type;
    }

    /** @return the status. */
    @Enumerated(EnumType.STRING)
    public AttritionStatusEnum getStatus() {
        return status;
    }

    /** @param status the status to set. */
    public void setStatus(AttritionStatusEnum status) {
        this.status = status;
    }

    /** @return the counters. */
    @OneToMany(mappedBy = "id.attrition", cascade = CascadeType.ALL)
    public Set<AttritionCounterEntity> getCounters() {
        return counters;
    }

    /** @param counters the counters to set. */
    public void setCounters(Set<AttritionCounterEntity> counters) {
        this.counters = counters;
    }

    /** @return the provinces. */
    @ElementCollection
    @CollectionTable(name = "ATTRITION_PROVINCE", joinColumns = @JoinColumn(name = "ID_ATTRITION"))
    @Column(name = "R_PROVINCE")
    public Set<String> getProvinces() {
        return provinces;
    }

    /** @param provinces the provinces to set. */
    public void setProvinces(Set<String> provinces) {
        this.provinces = provinces;
    }

    /** @return the size. */
    public double getSize() {
        return size;
    }

    /** @param size the size to set. */
    public void setSize(double size) {
        this.size = size;
    }

    /** @return the tech. */
    public String getTech() {
        return tech;
    }

    /** @param tech the tech to set. */
    public void setTech(String tech) {
        this.tech = tech;
    }

    /** @return the bonus. */
    public Integer getBonus() {
        return bonus;
    }

    /** @param bonus the bonus to set. */
    public void setBonus(Integer bonus) {
        this.bonus = bonus;
    }

    /** @return the die. */
    public Integer getDie() {
        return die;
    }

    /** @param die the die to set. */
    public void setDie(Integer die) {
        this.die = die;
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
