package com.mkl.eu.client.service.vo.tables;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.enumeration.LeaderTypeEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Predicate;

/**
 * VO for the leader table.
 *
 * @author MKL.
 */
public class Leader extends EuObject {
    /** Conditions for a leader to be eligible to lead in a european land. */
    public static final Predicate<Leader> landEurope = leader -> leader.getType() == LeaderTypeEnum.GENERAL || leader.getType() == LeaderTypeEnum.PACHA || leader.getType() == LeaderTypeEnum.KING;
    /** Conditions for a leader to be eligible to lead in a rotw land that is neither america nor asia. */
    public static final Predicate<Leader> landRotw = leader -> ((leader.getType() == LeaderTypeEnum.CONQUISTADOR || leader.getType() == LeaderTypeEnum.GOVERNOR) ||
            (leader.getType() == LeaderTypeEnum.GENERAL && leader.isRotw())) && !leader.isAmerica() && !leader.isAsia();
    /** Conditions for a leader to be eligible to lead in a rotw american land. */
    public static final Predicate<Leader> landRotwAmerica = leader -> (((leader.getType() == LeaderTypeEnum.CONQUISTADOR || leader.getType() == LeaderTypeEnum.GOVERNOR) ||
            (leader.getType() == LeaderTypeEnum.GENERAL && leader.isRotw())) && !leader.isAsia()) || leader.isAmerica();
    /** Conditions for a leader to be eligible to lead in a rotw asian land. */
    public static final Predicate<Leader> landRotwAsia = leader -> (((leader.getType() == LeaderTypeEnum.CONQUISTADOR || leader.getType() == LeaderTypeEnum.GOVERNOR) ||
            (leader.getType() == LeaderTypeEnum.GENERAL && leader.isRotw())) && !leader.isAmerica()) || leader.isAsia();
    /** Conditions for a leader to be eligible to lead in a european sea except mediterranean sea. */
    public static final Predicate<Leader> navalEurope = leader -> leader.getType() == LeaderTypeEnum.ADMIRAL && !leader.isMediterranee();
    /** Conditions for a leader to be eligible to lead in the mediterranean sea. */
    public static final Predicate<Leader> navalEuropeMed = leader -> leader.getType() == LeaderTypeEnum.ADMIRAL;
    /** Conditions for a leader to be eligible to lead in a rotw sea. */
    public static final Predicate<Leader> navalRotw = leader -> leader.getType() == LeaderTypeEnum.EXPLORER ||
            (leader.getType() == LeaderTypeEnum.ADMIRAL && leader.isRotw());
    /** Conditions for leader that will die more easily in battles. */
    public static final Predicate<Leader> leaderFragility = leader -> (leader.getFire() == 6 || leader.getShock() == 6) &&
            !StringUtils.equals("Friedrich II", leader.getCode()) && !StringUtils.equals("Marlborough", leader.getCode());
    /** Country of the replacement leader for natives. */
    public static final String REPLACEMENT_NATIVES = "natives";
    /** Country of the replacement leader for minor countries. */
    public static final String REPLACEMENT_MINOR = "minor";

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
    /** If the leader is restricted to Mediterranean sea. */
    private boolean mediterranee;
    /** If the leader is also a privateer. */
    private boolean privateer;
    /** If it is the main side of the leader. */
    private boolean main;
    /** If the leader is anonymous. */
    private boolean anonymous;
    /** Number of LD that this leader stands for. Only used by PACHA. */
    private Integer size;
    /** Other side of the leader for double side leaders. */
    private String otherSide;

    /** @return the country. */
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

    /** @return the mediterranee. */
    public boolean isMediterranee() {
        return mediterranee;
    }

    /** @param mediterranee the mediterranee to set. */
    public void setMediterranee(boolean mediterranee) {
        this.mediterranee = mediterranee;
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

    /** @return the anonymous. */
    public boolean isAnonymous() {
        return anonymous;
    }

    /** @param anonymous the anonymous to set. */
    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    /** @return the size. */
    public Integer getSize() {
        return size;
    }

    /** @param size the size to set. */
    public void setSize(Integer size) {
        this.size = size;
    }

    /** @return the otherSide. */
    public String getOtherSide() {
        return otherSide;
    }

    /** @param otherSide the otherSide to set. */
    public void setOtherSide(String otherSide) {
        this.otherSide = otherSide;
    }
}
