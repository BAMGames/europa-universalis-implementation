package com.mkl.eu.service.service.persistence.oe.board;

import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import com.mkl.eu.service.service.persistence.oe.eco.EstablishmentEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Counter (A+, MNU, fortress,...).
 *
 * @author MKL
 */
@Entity
@Table(name = "COUNTER")
public class CounterEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Name of the country owning of the counter. */
    private String country;
    /** Stack owning the counter. */
    private StackEntity owner;
    /** Type of the counter. */
    private CounterFaceTypeEnum type;
    /** If the counter is an establishment, all the info about. */
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

    /** @return the country. */
    @Column(name = "R_COUNTRY")
    public String getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(String country) {
        this.country = country;
    }

    /** @return the owner. */
    @ManyToOne
    @JoinColumn(name = "ID_STACK")
    public StackEntity getOwner() {
        return owner;
    }

    /** @param owner the owner to set. */
    public void setOwner(StackEntity owner) {
        this.owner = owner;
    }

    /** @return the type. */
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE")
    public CounterFaceTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(CounterFaceTypeEnum type) {
        this.type = type;
    }

    /** @return the establishment. */
    @OneToOne(mappedBy = "counter")
    public EstablishmentEntity getEstablishment() {
        return establishment;
    }

    /** @param establishment the establishment to set. */
    public void setEstablishment(EstablishmentEntity establishment) {
        this.establishment = establishment;
    }
}
