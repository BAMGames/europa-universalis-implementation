package com.mkl.eu.service.service.persistence.oe.tables;

import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity for the battle technology table.
 *
 * @author MKL.
 */
@Entity
@Table(name = "T_BATTLE_TECH")
public class BattleTechEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Technology of the friendly stack. */
    private String technologyFor;
    /** Technology of the opponent stack. */
    private String technologyAgainst;
    /** Flag to say if this table is for land or naval battle. */
    private boolean land;
    /** Column to use for fire. */
    private String columnFire;
    /** Column to use for shock. */
    private String columnShock;
    /** Moral for battle. */
    private int moral;
    /** Flag to say if there is a moral bonus for veteran stacks. */
    private boolean moralBonusVeteran;

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

    /** @return the technologyFor. */
    @Column(name = "TECH_FOR")
    public String getTechnologyFor() {
        return technologyFor;
    }

    /** @param technologyFor the technologyFor to set. */
    public void setTechnologyFor(String technologyFor) {
        this.technologyFor = technologyFor;
    }

    /** @return the technologyAgainst. */
    @Column(name = "TECH_AGAINST")
    public String getTechnologyAgainst() {
        return technologyAgainst;
    }

    /** @param technologyAgainst the technologyAgainst to set. */
    public void setTechnologyAgainst(String technologyAgainst) {
        this.technologyAgainst = technologyAgainst;
    }

    /** @return the land. */
    @Column(name = "LAND")
    public boolean isLand() {
        return land;
    }

    /** @param land the land to set. */
    public void setLand(boolean land) {
        this.land = land;
    }

    /** @return the columnFire. */
    @Column(name = "COLUMN_FIRE")
    public String getColumnFire() {
        return columnFire;
    }

    /** @param columnFire the columnFire to set. */
    public void setColumnFire(String columnFire) {
        this.columnFire = columnFire;
    }

    /** @return the columnShock. */
    @Column(name = "COLUMN_SHOCK")
    public String getColumnShock() {
        return columnShock;
    }

    /** @param columnShock the columnShock to set. */
    public void setColumnShock(String columnShock) {
        this.columnShock = columnShock;
    }

    /** @return the moral. */
    @Column(name = "MORAL")
    public int getMoral() {
        return moral;
    }

    /** @param moral the moral to set. */
    public void setMoral(int moral) {
        this.moral = moral;
    }

    /** @return the moralBonusVeteran. */
    @Column(name = "MORAL_BONUS_VETERAN")
    public boolean isMoralBonusVeteran() {
        return moralBonusVeteran;
    }

    /** @param moralBonusVeteran the moralBonusVeteran to set. */
    public void setMoralBonusVeteran(boolean moralBonusVeteran) {
        this.moralBonusVeteran = moralBonusVeteran;
    }
}
