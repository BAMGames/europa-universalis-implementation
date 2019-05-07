package com.mkl.eu.service.service.persistence.oe.tables;

import com.mkl.eu.service.service.persistence.oe.AbstractWithLossEntity;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity for the assault result table.
 *
 * @author MKL.
 */
@Entity
@Table(name = "T_ASSAULT_RESULT")
public class AssaultResultEntity extends AbstractWithLossEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Result of the modified dice. */
    private Integer dice;
    /** If it is the fire or the shock phase. */
    private boolean fire;
    /** If the fortress is breached. */
    private boolean breach;
    /** If it is the besieger or the besieging. */
    private boolean besieger;

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

    /** @return the dice. */
    public Integer getDice() {
        return dice;
    }

    /** @param dice the dice to set. */
    public void setDice(Integer dice) {
        this.dice = dice;
    }

    /** @return the fire. */
    public boolean isFire() {
        return fire;
    }

    /** @param fire the fire to set. */
    public void setFire(boolean fire) {
        this.fire = fire;
    }

    /** @return the breach. */
    public boolean isBreach() {
        return breach;
    }

    /** @param breach the breach to set. */
    public void setBreach(boolean breach) {
        this.breach = breach;
    }

    /** @return the besieger. */
    public boolean isBesieger() {
        return besieger;
    }

    /** @param besieger the besieger to set. */
    public void setBesieger(boolean besieger) {
        this.besieger = besieger;
    }
}
