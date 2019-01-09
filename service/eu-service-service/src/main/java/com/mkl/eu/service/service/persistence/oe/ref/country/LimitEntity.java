package com.mkl.eu.service.service.persistence.oe.ref.country;

import com.mkl.eu.client.service.vo.enumeration.CounterTypeEnum;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity for the limit force of a country (eg 2 ARMY counters or 3 LDND counters).
 *
 * @author MKL.
 */
@Entity
@Table(name = "R_LIMIT")
public class LimitEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Number of this type of counter. */
    private Integer number;
    /** Type of limit. */
    private CounterTypeEnum type;
    /** Country owning these forces. */
    private CountryEntity country;

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
    public CounterTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(CounterTypeEnum type) {
        this.type = type;
    }

    /** @return the country. */
    @ManyToOne
    @JoinColumn(name = "ID_R_COUNTRY")
    public CountryEntity getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(CountryEntity country) {
        this.country = country;
    }
}
