package com.mkl.eu.service.service.persistence.oe.diplo;

import com.mkl.eu.client.service.vo.enumeration.WarTypeEnum;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

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
public class WarEntity implements IEntity {
    /** Id. */
    private Long id;
    /** Name of the war. */
    private String name;
    /** List of countries in war (either side). */
    private List<CountryInWarEntity> countries = new ArrayList<>();
    /** Type of war. */
    private WarTypeEnum type;

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

    /** @return the name. */
    public String getName() {
        return name;
    }

    /** @param name the name to set. */
    public void setName(String name) {
        this.name = name;
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
