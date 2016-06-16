package com.mkl.eu.service.service.persistence.oe.tables;

import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.ForceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.UnitActionEnum;
import com.mkl.eu.client.service.vo.tables.IUnit;
import com.mkl.eu.service.service.persistence.oe.IEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity for the manufacture (income) of a country (tables).
 *
 * @author MKL.
 */
@Entity
@Table(name = "T_MNU")
public class ManufactureEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Country owning the manufacture. */
    private String country;
    /** Income of the manufacture. */
    private Integer value;
    /** Type of the manufacture. */
    private CounterFaceTypeEnum type;

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

    /** @return the value. */
    @Column(name = "VALUE")
    public Integer getValue() {
        return value;
    }

    /** @param value the value to set. */
    public void setValue(Integer value) {
        this.value = value;
    }

    /** @return the type. */
    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    public CounterFaceTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(CounterFaceTypeEnum type) {
        this.type = type;
    }
}
