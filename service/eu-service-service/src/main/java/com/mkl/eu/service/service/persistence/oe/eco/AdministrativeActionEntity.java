package com.mkl.eu.service.service.persistence.oe.eco;

import com.mkl.eu.client.service.vo.enumeration.AdminActionResultEnum;
import com.mkl.eu.client.service.vo.enumeration.AdminActionStatusEnum;
import com.mkl.eu.client.service.vo.enumeration.AdminActionTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Administrative action of a country at a given turn.
 * A country can have zero or multiple administrative actions at a given turn.
 *
 * @author MKL
 */
@Entity
@Table(name = "ADMINISTRATIVE_ACTION")
public class AdministrativeActionEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Owner of the administrative action. */
    private PlayableCountryEntity country;
    /** Turn of the administrative action. */
    private Integer turn;
    /** Type of the administrative action (ie MNU, DTI, COL, Exc Levies,...). */
    private AdminActionTypeEnum type;
    /** Cost of the administrative action. */
    private Integer cost;
    /** Column to test the administrative action. */
    private Integer column;
    /** Bonus to the die of the test of the administrative action. */
    private Integer bonus;
    /** Result of the die of the test of the administrative action. */
    private Integer die;
    /** Result of the administrative action. */
    private AdminActionResultEnum result;
    /** Status of the administrative action. */
    private AdminActionStatusEnum status;
    /** Eventual if of object subject of the administrative action. */
    private Long idObject;
    /** Eventual name of the province subject of the administrative action. */
    private String province;
    /** Eventual type of counter face subject of the administrative action. */
    private CounterFaceTypeEnum counterFaceType;

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
    @ManyToOne
    @JoinColumn(name = "ID_COUNTRY")
    public PlayableCountryEntity getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(PlayableCountryEntity country) {
        this.country = country;
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

    /** @return the type. */
    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    public AdminActionTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(AdminActionTypeEnum type) {
        this.type = type;
    }

    /** @return the cost. */
    @Column(name = "COST")
    public Integer getCost() {
        return cost;
    }

    /** @param cost the cost to set. */
    public void setCost(Integer cost) {
        this.cost = cost;
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

    /** @return the bonus. */
    @Column(name = "BONUS")
    public Integer getBonus() {
        return bonus;
    }

    /** @param bonus the bonus to set. */
    public void setBonus(Integer bonus) {
        this.bonus = bonus;
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

    /** @return the result. */
    @Column(name = "RESULT")
    @Enumerated(EnumType.STRING)
    public AdminActionResultEnum getResult() {
        return result;
    }

    /** @param result the result to set. */
    public void setResult(AdminActionResultEnum result) {
        this.result = result;
    }

    /** @return the status. */
    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    public AdminActionStatusEnum getStatus() {
        return status;
    }

    /** @param status the status to set. */
    public void setStatus(AdminActionStatusEnum status) {
        this.status = status;
    }

    /** @return the idObject. */
    @Column(name = "ID_OBJECT")
    public Long getIdObject() {
        return idObject;
    }

    /** @param idObject the idObject to set. */
    public void setIdObject(Long idObject) {
        this.idObject = idObject;
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

    /** @return the counterFaceType. */
    @Column(name = "COUNTER_FACE_TYPE")
    @Enumerated(EnumType.STRING)
    public CounterFaceTypeEnum getCounterFaceType() {
        return counterFaceType;
    }

    /** @param counterFaceType the counterFaceType to set. */
    public void setCounterFaceType(CounterFaceTypeEnum counterFaceType) {
        this.counterFaceType = counterFaceType;
    }
}
