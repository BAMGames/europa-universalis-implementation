package com.mkl.eu.service.service.persistence.oe.tables;

import com.mkl.eu.client.service.vo.enumeration.ForceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.UnitActionEnum;
import com.mkl.eu.client.service.vo.tables.IUnit;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity for the unit (purchase, maintenance) of a country (tables).
 *
 * @author MKL.
 */
@Entity
@Table(name = "T_UNIT")
public class UnitEntity implements IUnit, IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Country owning this unit. */
    private String country;
    /** Tech of the unit. */
    private TechEntity tech;
    /** Price of the pruchase/maintenance of the unit. */
    private Integer price;
    /** Type of the unit. */
    private ForceTypeEnum type;
    /** Action (purchase, maintenance). */
    private UnitActionEnum action;
    /** Flag for special cases (veterans). */
    private boolean special;

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

    /** @return the country. */
    @Column(name = "R_COUNTRY")
    public String getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(String country) {
        this.country = country;
    }

    /** @return the tech. */
    @ManyToOne
    @JoinColumn(name = "ID_TECH")
    public TechEntity getTech() {
        return tech;
    }

    /** @param tech the tech to set. */
    public void setTech(TechEntity tech) {
        this.tech = tech;
    }

    /** @return the price. */
    @Column(name = "PRICE")
    public Integer getPrice() {
        return price;
    }

    /** @param price the price to set. */
    public void setPrice(Integer price) {
        this.price = price;
    }

    /** @return the type. */
    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    public ForceTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(ForceTypeEnum type) {
        this.type = type;
    }

    /** @return the action. */
    @Column(name = "ACTION")
    @Enumerated(EnumType.STRING)
    public UnitActionEnum getAction() {
        return action;
    }

    /** @param action the action to set. */
    public void setAction(UnitActionEnum action) {
        this.action = action;
    }

    /** @return the special. */
    @Column(name = "SPECIAL")
    public boolean isSpecial() {
        return special;
    }

    /** @param special the special to set. */
    public void setSpecial(boolean special) {
        this.special = special;
    }
}
