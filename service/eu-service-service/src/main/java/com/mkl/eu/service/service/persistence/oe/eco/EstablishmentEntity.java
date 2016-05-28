package com.mkl.eu.service.service.persistence.oe.eco;

import com.mkl.eu.service.service.persistence.oe.IEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Establishment exploiting an exotic resource (trading post or colony, or further minor establishment).
 *
 * @author MKL
 */
@Entity
@Table(name = "ESTABLISHMENT")
public class EstablishmentEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Region where the establishment is. */
    private String region;
    /** Name of the province where the trade fleet is located. */
    private String type;
    /** Level of the trade fleet. */
    private Integer level;
    /** Game in which the trade fleet is. */
    private CounterEntity counter;

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

    /** @return the region. */
    @Column(name = "R_REGION")
    public String getRegion() {
        return region;
    }

    /** @param region the region to set. */
    public void setRegion(String region) {
        this.region = region;
    }

    /** @return the type. */
    @Column(name = "TYPE")
    public String getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(String type) {
        this.type = type;
    }

    /** @return the level. */
    @Column(name = "LEVEL")
    public Integer getLevel() {
        return level;
    }

    /** @param level the level to set. */
    public void setLevel(Integer level) {
        this.level = level;
    }

    /** @return the counter. */
    @OneToOne
    @JoinColumn(name = "ID_COUNTER")
    public CounterEntity getCounter() {
        return counter;
    }

    /** @param counter the counter to set. */
    public void setCounter(CounterEntity counter) {
        this.counter = counter;
    }
}
