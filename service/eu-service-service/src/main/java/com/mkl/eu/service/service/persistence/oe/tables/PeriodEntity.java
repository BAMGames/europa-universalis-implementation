package com.mkl.eu.service.service.persistence.oe.tables;

import com.mkl.eu.service.service.persistence.oe.IEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Description of a period entity.
 *
 * @author MKL.
 */
@Entity
@Table(name = "T_PERIOD")
public class PeriodEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Name of the period. */
    private String name;
    /** Turn when the period begins (inclusive). */
    private Integer begin;
    /** Turn when the period ends (inclusive). */
    private Integer end;

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

    /** @return the begin. */
    @Column(name = "BEGIN")
    public Integer getBegin() {
        return begin;
    }

    /** @param begin the begin to set. */
    public void setBegin(Integer begin) {
        this.begin = begin;
    }

    /** @return the end. */
    @Column(name = "END")
    public Integer getEnd() {
        return end;
    }

    /** @param end the end to set. */
    public void setEnd(Integer end) {
        this.end = end;
    }
}
