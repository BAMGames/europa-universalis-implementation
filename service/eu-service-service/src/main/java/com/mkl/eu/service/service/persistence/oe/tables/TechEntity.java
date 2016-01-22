package com.mkl.eu.service.service.persistence.oe.tables;

import com.mkl.eu.service.service.persistence.oe.IEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Description of a technology entity.
 *
 * @author MKL.
 */
@Entity
@Table(name = "T_TECH")
public class TechEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Name of the technology. */
    private String name;
    /** Flag to know if the technology is land or naval. */
    private boolean land;
    /** Country that can have this technology, <code>null</code> for all. */
    private String country;
    /** Box where the technology begins. */
    private Integer beginBox;
    /** Turn when the technology can be reached. */
    private Integer beginTurn;

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

    /** @return the name. */
    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    /** @param name the name to set. */
    public void setName(String name) {
        this.name = name;
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

    /** @return the country. */
    @Column(name = "R_COUNTRY")
    public String getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(String country) {
        this.country = country;
    }

    /** @return the beginBox. */
    @Column(name = "BEGIN_BOX")
    public Integer getBeginBox() {
        return beginBox;
    }

    /** @param beginBox the beginBox to set. */
    public void setBeginBox(Integer beginBox) {
        this.beginBox = beginBox;
    }

    /** @return the beginTurn. */
    @Column(name = "BEGIN_TURN")
    public Integer getBeginTurn() {
        return beginTurn;
    }

    /** @param beginTurn the beginTurn to set. */
    public void setBeginTurn(Integer beginTurn) {
        this.beginTurn = beginTurn;
    }
}
