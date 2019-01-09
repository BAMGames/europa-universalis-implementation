package com.mkl.eu.service.service.persistence.oe.eco;

import com.mkl.eu.client.service.vo.enumeration.CompetitionTypeEnum;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity for automatic competitions (normal competitions are administrative actions).
 *
 * @author MKL.
 */
@Entity
@Table(name = "COMPETITION")
public class CompetitionEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Type of competition. */
    private CompetitionTypeEnum type;
    /** Province where the competition occurs. */
    private String province;
    /** Turn of the competition. */
    private Integer turn;
    /** The rounds of the competition. */
    private List<CompetitionRoundEntity> rounds = new ArrayList<>();
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

    /** @return the type. */
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE")
    public CompetitionTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(CompetitionTypeEnum type) {
        this.type = type;
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

    /** @return the rounds. */
    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<CompetitionRoundEntity> getRounds() {
        return rounds;
    }

    /** @param rounds the rounds to set. */
    public void setRounds(List<CompetitionRoundEntity> rounds) {
        this.rounds = rounds;
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
