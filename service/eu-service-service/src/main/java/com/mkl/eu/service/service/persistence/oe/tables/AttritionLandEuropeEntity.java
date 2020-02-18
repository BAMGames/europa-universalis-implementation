package com.mkl.eu.service.service.persistence.oe.tables;

import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity for the attrition land in Europe table.
 *
 * @author MKL.
 */
@Entity
@Table(name = "T_ATTRITION_LAND_EUROPE")
public class AttritionLandEuropeEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Result of the modified dice. */
    private Integer dice;
    /** Minimum number of LD of the stack for land attrition in Europe. */
    private Integer minSize;
    /** Maximum number of LD of the stack for land attrition in Europe. */
    private Integer maxSize;
    /** LD loss in land europe attrition. */
    private Integer loss;
    /** If the land europe attrition leads to a pillage. */
    private boolean pillage;

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

    /** @return the minSize. */
    public Integer getMinSize() {
        return minSize;
    }

    /** @param minSize the minSize to set. */
    public void setMinSize(Integer minSize) {
        this.minSize = minSize;
    }

    /** @return the maxSize. */
    public Integer getMaxSize() {
        return maxSize;
    }

    /** @param maxSize the maxSize to set. */
    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    /** @return the loss. */
    public Integer getLoss() {
        return loss;
    }

    /** @param loss the loss to set. */
    public void setLoss(Integer loss) {
        this.loss = loss;
    }

    /** @return the pillage. */
    public boolean isPillage() {
        return pillage;
    }

    /** @param pillage the pillage to set. */
    public void setPillage(boolean pillage) {
        this.pillage = pillage;
    }
}
