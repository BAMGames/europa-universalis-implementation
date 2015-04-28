package com.mkl.eu.service.service.persistence.oe.ref;

import com.mkl.eu.service.service.persistence.oe.IEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity for a country (minor or major).
 *
 * @author MKL.
 */
@Entity
@Table(name = "R_COUNTRY")
public class CountryEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Name of the country. */
    private String name;

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

    /** @return the name. */
    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    /** @param name the name to set. */
    public void setName(String name) {
        this.name = name;
    }
}
