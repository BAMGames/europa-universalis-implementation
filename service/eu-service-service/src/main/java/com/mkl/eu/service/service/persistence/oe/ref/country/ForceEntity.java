package com.mkl.eu.service.service.persistence.oe.ref.country;

import com.mkl.eu.client.service.vo.enumeration.ForceTypeEnum;
import com.mkl.eu.service.service.persistence.oe.IEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity for a force of a country (eg 2 A+ or 3 LD).
 * Can be used for basic forces or reinforcments.
 *
 * @author MKL.
 */
@MappedSuperclass
public class ForceEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Number of this type of counter. */
    private Integer number;
    /** Type of limit. */
    private ForceTypeEnum type;
    /** Country owning these forces. */
    private CountryEntity country;

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
    public ForceTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(ForceTypeEnum type) {
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
