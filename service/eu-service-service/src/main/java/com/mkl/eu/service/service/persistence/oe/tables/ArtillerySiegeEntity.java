package com.mkl.eu.service.service.persistence.oe.tables;

import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity for the artillery siege table.
 *
 * @author MKL.
 */
@Entity
@Table(name = "T_ARTILLERY_SIEGE")
public class ArtillerySiegeEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Number of artillery of the besieging army. */
    private Integer artillery;
    /** Level of the fortress under siege. */
    private Integer fortress;
    /** Bonus of the artillery on the siege. */
    private Integer bonus;

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

    /** @return the size. */
    public Integer getArtillery() {
        return artillery;
    }

    /** @param artillery the size to set. */
    public void setArtillery(Integer artillery) {
        this.artillery = artillery;
    }

    /** @return the fortress. */
    public Integer getFortress() {
        return fortress;
    }

    /** @param fortress the fortress to set. */
    public void setFortress(Integer fortress) {
        this.fortress = fortress;
    }

    /** @return the bonus. */
    public Integer getBonus() {
        return bonus;
    }

    /** @param bonus the bonus to set. */
    public void setBonus(Integer bonus) {
        this.bonus = bonus;
    }
}
