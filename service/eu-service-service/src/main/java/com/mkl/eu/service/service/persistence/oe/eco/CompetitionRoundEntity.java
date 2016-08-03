package com.mkl.eu.service.service.persistence.oe.eco;

import com.mkl.eu.client.service.vo.enumeration.ResultEnum;
import com.mkl.eu.service.service.persistence.oe.IEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity for a round of a country of an automatic competition.
 *
 * @author MKL.
 */
@Entity
@Table(name = "COMPETITION_ROUND")
public class CompetitionRoundEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Country rolling for the competition (can be minor). */
    private String country;
    /** Round of the competition. */
    private Integer round;
    /** Column to test the administrative action. */
    private Integer column;
    /** Result of the die (without bonus) of the test of the administrative action. */
    private Integer die;
    /** Result of the die (without bonus) of an eventual secondary test of the administrative action (often test under FTI). */
    private Integer secondaryDie;
    /** Result of the administrative action. */
    private ResultEnum result;
    /** Secondary result of the administrative action (when secondary tests are needed). */
    private Boolean secondaryResult;
    /** Global competition of which this object is a round. */
    private CompetitionEntity competition;

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

    /** @return the country. */
    @Column(name = "R_COUNTRY")
    public String getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(String country) {
        this.country = country;
    }

    /** @return the round. */
    @Column(name = "ROUND")
    public Integer getRound() {
        return round;
    }

    /** @param round the round to set. */
    public void setRound(Integer round) {
        this.round = round;
    }

    /** @return the column. */
    @Column(name = "COLUNM")
    public Integer getColumn() {
        return column;
    }

    /** @param column the column to set. */
    public void setColumn(Integer column) {
        this.column = column;
    }

    /** @return the die. */
    @Column(name = "DIE")
    public Integer getDie() {
        return die;
    }

    /** @param die the die to set. */
    public void setDie(Integer die) {
        this.die = die;
    }

    /** @return the secondaryDie. */
    @Column(name = "SECONDARY_DIE")
    public Integer getSecondaryDie() {
        return secondaryDie;
    }

    /** @param secondaryDie the secondaryDie to set. */
    public void setSecondaryDie(Integer secondaryDie) {
        this.secondaryDie = secondaryDie;
    }

    /** @return the result. */
    @Enumerated(EnumType.STRING)
    @Column(name = "RESULT")
    public ResultEnum getResult() {
        return result;
    }

    /** @param result the result to set. */
    public void setResult(ResultEnum result) {
        this.result = result;
    }

    /** @return the secondaryResult. */
    @Column(name = "SECONDARY_RESULT")
    public Boolean isSecondaryResult() {
        return secondaryResult;
    }

    /** @param secondaryResult the secondaryResult to set. */
    public void setSecondaryResult(Boolean secondaryResult) {
        this.secondaryResult = secondaryResult;
    }

    /** @return the competition. */
    @ManyToOne
    @JoinColumn(name = "ID_COMPETITION")
    public CompetitionEntity getCompetition() {
        return competition;
    }

    /** @param competition the competition to set. */
    public void setCompetition(CompetitionEntity competition) {
        this.competition = competition;
    }

}
