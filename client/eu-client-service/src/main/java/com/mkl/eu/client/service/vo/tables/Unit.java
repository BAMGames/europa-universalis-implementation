package com.mkl.eu.client.service.vo.tables;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.enumeration.ForceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.UnitActionEnum;

/**
 * VO for the unit (purchase, maintenance) of a country (tables).
 *
 * @author MKL.
 */
public class Unit extends EuObject implements IUnit {
    /** Id. */
    private Long id;
    /** Country owning this unit. */
    private String country;
    /** Tech of the unit. */
    private String tech;
    /** Price of the pruchase/maintenance of the unit. */
    private Integer price;
    /** Type of the unit. */
    private ForceTypeEnum type;
    /** Action (purchase, maintenance). */
    private UnitActionEnum action;
    /** Flag for special cases (veterans). */
    private boolean special;

    /** @return the id. */
    public Long getId() {
        return id;
    }

    /** @param id the id to set. */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return the country. */
    public String getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(String country) {
        this.country = country;
    }

    /** @return the tech. */
    public String getTech() {
        return tech;
    }

    /** @param tech the tech to set. */
    public void setTech(String tech) {
        this.tech = tech;
    }

    /** @return the price. */
    @Override
    public Integer getPrice() {
        return price;
    }

    /** @param price the price to set. */
    public void setPrice(Integer price) {
        this.price = price;
    }

    /** @return the type. */
    @Override
    public ForceTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(ForceTypeEnum type) {
        this.type = type;
    }

    /** @return the action. */
    @Override
    public UnitActionEnum getAction() {
        return action;
    }

    /** @param action the action to set. */
    public void setAction(UnitActionEnum action) {
        this.action = action;
    }

    /** @return the special. */
    @Override
    public boolean isSpecial() {
        return special;
    }

    /** @param special the special to set. */
    public void setSpecial(boolean special) {
        this.special = special;
    }
}
