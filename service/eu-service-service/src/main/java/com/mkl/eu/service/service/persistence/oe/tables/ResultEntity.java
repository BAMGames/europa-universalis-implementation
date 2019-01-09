package com.mkl.eu.service.service.persistence.oe.tables;

import com.mkl.eu.client.service.vo.enumeration.ResultEnum;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity for the result of an action (tables).
 *
 * @author MKL.
 */
@Entity
@Table(name = "T_RESULT")
public class ResultEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Die of the result. */
    private Integer die;
    /** Column of the result. */
    private Integer column;
    /** Result. */
    private ResultEnum result;

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

    /** @return the die. */
    @Column(name = "DIE")
    public Integer getDie() {
        return die;
    }

    /** @param die the die to set. */
    public void setDie(Integer die) {
        this.die = die;
    }

    /** @return the column. */
    @Column(name = "COLUMN")
    public Integer getColumn() {
        return column;
    }

    /** @param column the column to set. */
    public void setColumn(Integer column) {
        this.column = column;
    }

    /** @return the result. */
    @Column(name = "RESULT")
    @Enumerated(EnumType.STRING)
    public ResultEnum getResult() {
        return result;
    }

    /** @param result the result to set. */
    public void setResult(ResultEnum result) {
        this.result = result;
    }
}
