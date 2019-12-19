package com.mkl.eu.service.service.persistence.oe.tables;

import com.mkl.eu.client.service.vo.enumeration.LeaderTypeEnum;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity for the army artillery table.
 *
 * @author MKL.
 */
@Entity
@Table(name = "T_LEADER")
public class LeaderEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Code of the leader. */
    private String code;
    /** Name of the leader. */
    private String name;
    /** Country of the leader. */
    private String country;
    /** Event linked to the arrival of the leader. */
    private String event;
    /** Turn when the leader is available. */
    private Integer begin;
    /** Turn when the leader dies (a leader dies at the end of the turn). */
    private Integer end;
    /** Rank of the leader for hierarchy purpose. */
    private String rank;
    /** Manoeuvre of the leader. */
    private int manoeuvre;
    /** Leader value during a fire phase of a battle. */
    private int fire;
    /** Leader value during a shock phase of a battle. */
    private int shock;
    /** Leader value during a siege phase of a battle. */
    private int siege;
    /** Type of the leader. */
    private LeaderTypeEnum type;
    /** If the leader can also go in rotw. */
    private boolean rotw;
    /** If the leader is restricted to Asia. */
    private boolean asia;
    /** If the leader is restricted to America. */
    private boolean america;
    /** If the leader is also a privateer. */
    private boolean privateer;
    /** If it is the main side of the leader. */
    private boolean main;
    /** Other side of the leader for double side leaders. */
    private String otherSide;

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

    /** @return the code. */
    public String getCode() {
        return code;
    }

    /** @param code the code to set. */
    public void setCode(String code) {
        this.code = code;
    }

    /** @return the name. */
    public String getName() {
        return name;
    }

    /** @param name the name to set. */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the event. */
    public String getEvent() {
        return event;
    }

    /** @param event the event to set. */
    public void setEvent(String event) {
        this.event = event;
    }

    /** @return the begin. */
    public Integer getBegin() {
        return begin;
    }

    /** @param begin the begin to set. */
    public void setBegin(Integer begin) {
        this.begin = begin;
    }

    /** @return the end. */
    public Integer getEnd() {
        return end;
    }

    /** @param end the end to set. */
    public void setEnd(Integer end) {
        this.end = end;
    }

    /** @return the rank. */
    public String getRank() {
        return rank;
    }

    /** @param rank the rank to set. */
    public void setRank(String rank) {
        this.rank = rank;
    }

    /** @return the manoeuvre. */
    public int getManoeuvre() {
        return manoeuvre;
    }

    /** @param manoeuvre the manoeuvre to set. */
    public void setManoeuvre(int manoeuvre) {
        this.manoeuvre = manoeuvre;
    }

    /** @return the fire. */
    public int getFire() {
        return fire;
    }

    /** @param fire the fire to set. */
    public void setFire(int fire) {
        this.fire = fire;
    }

    /** @return the shock. */
    public int getShock() {
        return shock;
    }

    /** @param shock the shock to set. */
    public void setShock(int shock) {
        this.shock = shock;
    }

    /** @return the siege. */
    public int getSiege() {
        return siege;
    }

    /** @param siege the siege to set. */
    public void setSiege(int siege) {
        this.siege = siege;
    }

    /** @return the type. */
    @Enumerated(EnumType.STRING)
    public LeaderTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(LeaderTypeEnum type) {
        this.type = type;
    }

    /** @return the rotw. */
    public boolean isRotw() {
        return rotw;
    }

    /** @param rotw the rotw to set. */
    public void setRotw(boolean rotw) {
        this.rotw = rotw;
    }

    /** @return the asia. */
    public boolean isAsia() {
        return asia;
    }

    /** @param asia the asia to set. */
    public void setAsia(boolean asia) {
        this.asia = asia;
    }

    /** @return the america. */
    public boolean isAmerica() {
        return america;
    }

    /** @param america the america to set. */
    public void setAmerica(boolean america) {
        this.america = america;
    }

    /** @return the privateer. */
    public boolean isPrivateer() {
        return privateer;
    }

    /** @param privateer the privateer to set. */
    public void setPrivateer(boolean privateer) {
        this.privateer = privateer;
    }

    /** @return the main. */
    public boolean isMain() {
        return main;
    }

    /** @param main the main to set. */
    public void setMain(boolean main) {
        this.main = main;
    }

    /** @return the otherSide. */
    @Column(name = "T_LEADER")
    public String getOtherSide() {
        return otherSide;
    }

    /** @param otherSide the otherSide to set. */
    public void setOtherSide(String otherSide) {
        this.otherSide = otherSide;
    }
}
