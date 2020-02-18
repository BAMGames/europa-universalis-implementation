package com.mkl.eu.service.service.persistence.oe.tables;

import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity for the attrition naval or in rotw table.
 *
 * @author MKL.
 */
@Entity
@Table(name = "T_ATTRITION_OTHER")
public class AttritionOtherEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Result of the modified dice. */
    private Integer dice;
    /** Loss percentage of naval or rotw attrition. */
    private Integer lossPercentage;

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

    /** @return the lossPercentage. */
    public Integer getLossPercentage() {
        return lossPercentage;
    }

    /** @param lossPercentage the lossPercentage to set. */
    public void setLossPercentage(Integer lossPercentage) {
        this.lossPercentage = lossPercentage;
    }
}
