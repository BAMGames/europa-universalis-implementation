package com.mkl.eu.service.service.persistence.oe.eco;

import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Trade fleet of a country minor or major.
 *
 * @author MKL
 */
@Entity
@Table(name = "TRADE_FLEET")
public class TradeFleetEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Owner of the trade fleet (not an entity because it can be a minor country). */
    private String country;
    /** Name of the province where the trade fleet is located. */
    private String province;
    /** Level of the trade fleet. */
    private Integer level;
    /** Game in which the trade fleet is. */
    private GameEntity game;

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

    /** @return the province. */
    @Column(name = "R_PROVINCE")
    public String getProvince() {
        return province;
    }

    /** @param province the province to set. */
    public void setProvince(String province) {
        this.province = province;
    }

    /** @return the level. */
    @Column(name = "LEVEL")
    public Integer getLevel() {
        return level;
    }

    /** @param level the level to set. */
    public void setLevel(Integer level) {
        this.level = level;
    }

    /** @return the game. */
    @ManyToOne
    @JoinColumn(name = "ID_GAME")
    public GameEntity getGame() {
        return game;
    }

    /** @param game the game to set. */
    public void setGame(GameEntity game) {
        this.game = game;
    }
}
