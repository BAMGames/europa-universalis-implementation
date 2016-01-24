package com.mkl.eu.service.service.persistence.oe.tables;

import com.mkl.eu.client.service.vo.enumeration.LimitTypeEnum;
import com.mkl.eu.service.service.persistence.oe.IEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity for the basic forces of a country (tables).
 *
 * @author MKL.
 */
@Entity
@Table(name = "T_LIMIT")
public class LimitTableEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Country owning these forces. */
    private String country;
    /** Period concerned. */
    private PeriodEntity period;
    /** Number of this type of counter. */
    private Integer number;
    /** Type of limit. */
    private LimitTypeEnum type;

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

    /** @return the period. */
    @ManyToOne
    @JoinColumn(name = "ID_PERIOD")
    public PeriodEntity getPeriod() {
        return period;
    }

    /** @param period the period to set. */
    public void setPeriod(PeriodEntity period) {
        this.period = period;
    }

    /** @return the number. */
    @Column(name = "NUMBER")
    public Integer getNumber() {
        return number;
    }

    /** @param number the number to set. */
    public void setNumber(Integer number) {
        this.number = number;
    }

    /** @return the type. */
    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    public LimitTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(LimitTypeEnum type) {
        this.type = type;
    }
}
