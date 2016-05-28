package com.mkl.eu.service.service.persistence.oe.eco;

import com.mkl.eu.client.service.vo.enumeration.ExoticResourcesEnum;
import com.mkl.eu.service.service.persistence.oe.IEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Exotic resources exploited.
 *
 * @author MKL
 */
@Entity
@Table(name = "EXOTIC_RESOURCES")
public class ExoticResourcesEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Type of the exploited exotic resources. */
    private ExoticResourcesEnum resource;
    /** Number of exotic resources exploited. */
    private Integer number;
    /** Establishment exploiting the exotic resources. */
    private EstablishmentEntity establishment;

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

    /** @return the resource. */
    @Enumerated(EnumType.STRING)
    @Column(name = "RESOURCE")
    public ExoticResourcesEnum getResource() {
        return resource;
    }

    /** @param resource the resource to set. */
    public void setResource(ExoticResourcesEnum resource) {
        this.resource = resource;
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

    /** @return the colony. */
    @ManyToOne
    @JoinColumn(name = "ID_ESTABLISHMENT")
    public EstablishmentEntity getEstablishment() {
        return establishment;
    }

    /** @param colony the colony to set. */
    public void setEstablishment(EstablishmentEntity colony) {
        this.establishment = colony;
    }
}
