package com.mkl.eu.service.service.persistence.oe.eco;

import com.mkl.eu.client.service.vo.enumeration.EstablishmentTypeEnum;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import org.hibernate.annotations.GenericGenerator;

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
    /** Type of the establishment. */
    private EstablishmentTypeEnum type;
    /** Level of the establishment. */
    private Integer level;
    /** Counter related to the establishment. */
    private CounterEntity counter;

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
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE")
    public EstablishmentTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(EstablishmentTypeEnum type) {
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
