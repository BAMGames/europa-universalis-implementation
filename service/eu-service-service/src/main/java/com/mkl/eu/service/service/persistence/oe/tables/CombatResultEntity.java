package com.mkl.eu.service.service.persistence.oe.tables;

import com.mkl.eu.service.service.persistence.oe.AbstractWithLossEntity;
import com.mkl.eu.service.service.persistence.oe.IEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity for the battle technology table.
 *
 * @author MKL.
 */
@Entity
@Table(name = "T_COMBAT_RESULT")
public class CombatResultEntity extends AbstractWithLossEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Column used. */
    private String column;
    /** Result of the modified dice. */
    private Integer dice;

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

    /** @return the column. */
    @Column(name = "COLUMN")
    public String getColumn() {
        return column;
    }

    /** @param column the column to set. */
    public void setColumn(String column) {
        this.column = column;
    }

    /** @return the dice. */
    @Column(name = "DICE")
    public Integer getDice() {
        return dice;
    }

    /** @param dice the dice to set. */
    public void setDice(Integer dice) {
        this.dice = dice;
    }
}
