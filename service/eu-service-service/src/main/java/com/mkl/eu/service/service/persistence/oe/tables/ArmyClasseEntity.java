package com.mkl.eu.service.service.persistence.oe.tables;

import com.mkl.eu.client.service.vo.enumeration.ArmyClassEnum;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity for the army classe table.
 *
 * @author MKL.
 */
@Entity
@Table(name = "T_ARMY_CLASS")
public class ArmyClasseEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Class of the army. */
    private ArmyClassEnum armyClass;
    /** Period. */
    private String period;
    /** Size of the army. */
    private Integer size;

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

    /** @return the armyClass. */
    @Column(name = "CLASS")
    @Enumerated(EnumType.STRING)
    public ArmyClassEnum getArmyClass() {
        return armyClass;
    }

    /** @param armyClass the armyClass to set. */
    public void setArmyClass(ArmyClassEnum armyClass) {
        this.armyClass = armyClass;
    }

    /** @return the period. */
    @Column(name = "PERIOD")
    public String getPeriod() {
        return period;
    }

    /** @param period the period to set. */
    public void setPeriod(String period) {
        this.period = period;
    }

    /** @return the size. */
    @Column(name = "SIZE")
    public Integer getSize() {
        return size;
    }

    /** @param size the size to set. */
    public void setSize(Integer size) {
        this.size = size;
    }
}
