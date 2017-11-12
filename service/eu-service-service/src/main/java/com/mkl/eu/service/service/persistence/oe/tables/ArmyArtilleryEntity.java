package com.mkl.eu.service.service.persistence.oe.tables;

import com.mkl.eu.client.service.vo.enumeration.ArmyClassEnum;
import com.mkl.eu.service.service.persistence.oe.IEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity for the army artillery table.
 *
 * @author MKL.
 */
@Entity
@Table(name = "T_ARMY_ARTILLERY")
public class ArmyArtilleryEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Name of the country of the army. */
    private String country;
    /** Class of the army. */
    private ArmyClassEnum armyClass;
    /** Period. */
    private String period;
    /** Number of artillery of the army. */
    private Integer artillery;

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

    /** @return the armyClass. */
    @Column(name = "CLASS")
    @Enumerated(EnumType.STRING)
    public ArmyClassEnum getArmyClass() {
        return armyClass;
    }

    /** @param armyClass the armyClass to set. */
    public void setArmyClass(ArmyClassEnum armyClass) {
        this.armyClass = armyClass;
    }

    /** @return the period. */
    @Column(name = "PERIOD")
    public String getPeriod() {
        return period;
    }

    /** @param period the period to set. */
    public void setPeriod(String period) {
        this.period = period;
    }

    /** @return the size. */
    @Column(name = "ARTILLERY")
    public Integer getArtillery() {
        return artillery;
    }

    /** @param size the size to set. */
    public void setArtillery(Integer size) {
        this.artillery = size;
    }
}
