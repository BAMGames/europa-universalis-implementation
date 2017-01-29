package com.mkl.eu.service.service.persistence.oe.diplo;

import com.mkl.eu.client.service.vo.enumeration.WarTypeEnum;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity that describes a War.
 *
 * @author MKL.
 */
@Entity
@Table(name = "WAR")
public class WarEntity {
    /** Id. */
    private Long id;
    /** List of countries in war (either side). */
    private List<CountryInWarEntity> countries = new ArrayList<>();
    /** Type of war. */
    private WarTypeEnum type;

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

    /** @return the countries. */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ID_WAR")
    public List<CountryInWarEntity> getCountries() {
        return countries;
    }

    /** @param countries the countries to set. */
    public void setCountries(List<CountryInWarEntity> countries) {
        this.countries = countries;
    }

    /** @return the type. */
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE")
    public WarTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(WarTypeEnum type) {
        this.type = type;
    }
}
