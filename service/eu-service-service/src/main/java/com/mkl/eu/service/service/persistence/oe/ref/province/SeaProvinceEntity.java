package com.mkl.eu.service.service.persistence.oe.ref.province;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * Sea province. Can be europe or rotw.
 *
 * @author MKL.
 */
@Entity
@Table(name = "R_PROVINCE_SEA")
@PrimaryKeyJoinColumn(name = "ID")
public class SeaProvinceEntity extends AbstractProvinceEntity {
    /** Difficulty of the sea zone (positive). */
    private int difficulty;
    /** Penalty of the sea zone (positive or 0). */
    private int penalty;

    /** @return the difficulty. */
    @Column(name = "DIFFICULTY")
    public int getDifficulty() {
        return difficulty;
    }

    /** @param difficulty the difficulty to set. */
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    /** @return the penalty. */
    @Column(name = "PENALTY")
    public int getPenalty() {
        return penalty;
    }

    /** @param penalty the penalty to set. */
    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }
}
