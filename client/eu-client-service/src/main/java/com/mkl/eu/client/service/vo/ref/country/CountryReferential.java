package com.mkl.eu.client.service.vo.ref.country;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.enumeration.ArmyClassEnum;
import com.mkl.eu.client.service.vo.enumeration.CountryTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.CultureEnum;
import com.mkl.eu.client.service.vo.enumeration.ReligionEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Referential VO for a country (minor or major).
 *
 * @author MKL.
 */
public class CountryReferential extends EuObject {
    /** Name of the country. */
    private String name;
    /** Type of the country. */
    private CountryTypeEnum type;
    /** Religion at start of the country. */
    private ReligionEnum religion;
    /** Cultural group of this country. */
    private CultureEnum culture;
    /** Flag saying that the country is part of HRE. */
    private Boolean hre;
    /** Flag saying that the country is an elector of the HRE. */
    private Boolean elector;
    /** Geopolitics preference to another country (name of the major country). */
    private String preference;
    /** Bonus of the geopolitics preference. */
    private Integer preferenceBonus;
    /** Fidelity of this country (high value means it will stay on diplomatic track. */
    private int fidelity;
    /** Basic forces of the country. List of forces already built when a country begins a war. */
    private List<BasicForceReferential> basicForces = new ArrayList<>();
    /** Reinforcements of the country. List of forces that are created each turn of war. */
    private List<ReinforcementsReferential> reinforcements = new ArrayList<>();
    /** Limit forces. Exhaustive list of counters of the country (except leaders). */
    private List<LimitReferential> limits = new ArrayList<>();
    /** TODO conception of preferences. */
    /** Army class of this country. */
    private ArmyClassEnum armyClass;
    /** Capitals of the country (may be empty). */
    private List<String> capitals = new ArrayList<>();
    /** Provinces of the country (province.defaultOwner is often this country). */
    private List<String> provinces = new ArrayList<>();

    /*********************************************************************************************************
     *                       Diplomatic track  (null for unreachable box)                                      *
     *********************************************************************************************************/
    /** Dowry paid or received by a major country concluding a royal marriage with this country. */
    private Integer royalMarriage;
    /** Subsidies paid or received by a major country concluding a subsidies with this country (100 - subsidies earned). */
    private Integer subsidies;
    /** Number of boxes from previous state to enable a military alliance with this country. */
    private Integer militaryAlliance;
    /** Number of boxes from previous state to enable an expeditionary corps with this country. */
    private Integer expCorps;
    /** Number of boxes from previous state to enable an entry in war with this country. */
    private Integer entryInWar;
    /** Number of boxes from previous state to enable a vassalisation of this country. */
    private Integer vassal;
    /** Number of boxes from previous state to enable an annexation of this country. */
    private Integer annexion;

    /** @return the name. */
    public String getName() {
        return name;
    }

    /** @param name the name to set. */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the type. */
    public CountryTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(CountryTypeEnum type) {
        this.type = type;
    }

    /** @return the religion. */
    public ReligionEnum getReligion() {
        return religion;
    }

    /** @param religion the religion to set. */
    public void setReligion(ReligionEnum religion) {
        this.religion = religion;
    }

    /** @return the culture. */
    public CultureEnum getCulture() {
        return culture;
    }

    /** @param culture the culture to set. */
    public void setCulture(CultureEnum culture) {
        this.culture = culture;
    }

    /** @return the hre. */
    public Boolean isHre() {
        return hre;
    }

    /** @param hre the hre to set. */
    public void setHre(Boolean hre) {
        this.hre = hre;
    }

    /** @return the elector. */
    public Boolean isElector() {
        return elector;
    }

    /** @param elector the elector to set. */
    public void setElector(Boolean elector) {
        this.elector = elector;
    }

    /** @return the preference. */
    public String getPreference() {
        return preference;
    }

    /** @param preference the preference to set. */
    public void setPreference(String preference) {
        this.preference = preference;
    }

    /** @return the preferenceBonus. */
    public Integer getPreferenceBonus() {
        return preferenceBonus;
    }

    /** @param preferenceBonus the preferenceBonus to set. */
    public void setPreferenceBonus(Integer preferenceBonus) {
        this.preferenceBonus = preferenceBonus;
    }

    /** @return the royalMarriage. */
    public Integer getRoyalMarriage() {
        return royalMarriage;
    }

    /** @param royalMarriage the royalMarriage to set. */
    public void setRoyalMarriage(Integer royalMarriage) {
        this.royalMarriage = royalMarriage;
    }

    /** @return the subsidies. */
    public Integer getSubsidies() {
        return subsidies;
    }

    /** @param subsidies the subsidies to set. */
    public void setSubsidies(Integer subsidies) {
        this.subsidies = subsidies;
    }

    /** @return the militaryAlliance. */
    public Integer getMilitaryAlliance() {
        return militaryAlliance;
    }

    /** @param militaryAlliance the militaryAlliance to set. */
    public void setMilitaryAlliance(Integer militaryAlliance) {
        this.militaryAlliance = militaryAlliance;
    }

    /** @return the expCorps. */
    public Integer getExpCorps() {
        return expCorps;
    }

    /** @param expCorps the expCorps to set. */
    public void setExpCorps(Integer expCorps) {
        this.expCorps = expCorps;
    }

    /** @return the entryInWar. */
    public Integer getEntryInWar() {
        return entryInWar;
    }

    /** @param entryInWar the entryInWar to set. */
    public void setEntryInWar(Integer entryInWar) {
        this.entryInWar = entryInWar;
    }

    /** @return the vassal. */
    public Integer getVassal() {
        return vassal;
    }

    /** @param vassal the vassal to set. */
    public void setVassal(Integer vassal) {
        this.vassal = vassal;
    }

    /** @return the annexion. */
    public Integer getAnnexion() {
        return annexion;
    }

    /** @param annexion the annexion to set. */
    public void setAnnexion(Integer annexion) {
        this.annexion = annexion;
    }

    /** @return the fidelity. */
    public int getFidelity() {
        return fidelity;
    }

    /** @param fidelity the fidelity to set. */
    public void setFidelity(int fidelity) {
        this.fidelity = fidelity;
    }

    /** @return the basicForces. */
    public List<BasicForceReferential> getBasicForces() {
        return basicForces;
    }

    /** @param basicForces the basicForces to set. */
    public void setBasicForces(List<BasicForceReferential> basicForces) {
        this.basicForces = basicForces;
    }

    /** @return the reinforcements. */
    public List<ReinforcementsReferential> getReinforcements() {
        return reinforcements;
    }

    /** @param reinforcements the reinforcements to set. */
    public void setReinforcements(List<ReinforcementsReferential> reinforcements) {
        this.reinforcements = reinforcements;
    }

    /** @return the limits. */
    public List<LimitReferential> getLimits() {
        return limits;
    }

    /** @param limits the limits to set. */
    public void setLimits(List<LimitReferential> limits) {
        this.limits = limits;
    }

    /** @return the armyClass. */
    public ArmyClassEnum getArmyClass() {
        return armyClass;
    }

    /** @param armyClass the armyClass to set. */
    public void setArmyClass(ArmyClassEnum armyClass) {
        this.armyClass = armyClass;
    }

    /** @return the capitals. */
    public List<String> getCapitals() {
        return capitals;
    }

    /** @param capitals the capital to set. */
    public void setCapitals(List<String> capitals) {
        this.capitals = capitals;
    }

    /** @return the provinces. */
    public List<String> getProvinces() {
        return provinces;
    }

    /** @param provinces the provinces to set. */
    public void setProvinces(List<String> provinces) {
        this.provinces = provinces;
    }
}
