package com.mkl.eu.service.service.persistence.oe.ref.country;

import com.mkl.eu.client.service.vo.enumeration.ArmyClassEnum;
import com.mkl.eu.client.service.vo.enumeration.CountryTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.CultureEnum;
import com.mkl.eu.client.service.vo.enumeration.ReligionEnum;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.EuropeanProvinceEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Entity for a country (minor or major).
 *
 * @author MKL.
 */
@Entity
@Table(name = "R_COUNTRY")
public class CountryEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
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
    private List<BasicForceEntity> basicForces;
    /** Reinforcements of the country. List of forces that are created each turn of war. */
    private List<ReinforcementsEntity> reinforcements;
    /** Limit forces. Exhaustive list of counters of the country (except leaders). */
    private List<LimitEntity> limits;
    /** TODO TG-18 conception of preferences. */
    /** Army class of this country. */
    private ArmyClassEnum armyClass;
    /** Capitals of the country (may be empty). */
    private List<EuropeanProvinceEntity> capitals;
    /** Provinces of the country (province.defaultOwner is often this country). */
    private List<EuropeanProvinceEntity> provinces;

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

    /** @return the name. */
    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    /** @param name the name to set. */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the type. */
    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    public CountryTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(CountryTypeEnum type) {
        this.type = type;
    }

    /** @return the religion. */
    @Column(name = "RELIGION")
    @Enumerated(EnumType.STRING)
    public ReligionEnum getReligion() {
        return religion;
    }

    /** @param religion the religion to set. */
    public void setReligion(ReligionEnum religion) {
        this.religion = religion;
    }

    /** @return the culture. */
    @Column(name = "CULTURE")
    @Enumerated(EnumType.STRING)
    public CultureEnum getCulture() {
        return culture;
    }

    /** @param culture the culture to set. */
    public void setCulture(CultureEnum culture) {
        this.culture = culture;
    }

    /** @return the hre. */
    @Column(name = "HRE", columnDefinition = "BIT")
    public Boolean isHre() {
        return hre;
    }

    /** @param hre the hre to set. */
    public void setHre(Boolean hre) {
        this.hre = hre;
    }

    /** @return the elector. */
    @Column(name = "ELECTOR", columnDefinition = "BIT")
    public Boolean isElector() {
        return elector;
    }

    /** @param elector the elector to set. */
    public void setElector(Boolean elector) {
        this.elector = elector;
    }

    /** @return the preference. */
    @Column(name = "GEOPOLITICS_COUNTRY")
    public String getPreference() {
        return preference;
    }

    /** @param preference the preference to set. */
    public void setPreference(String preference) {
        this.preference = preference;
    }

    /** @return the preferenceBonus. */
    @Column(name = "GEOPOLITICS_BONUS")
    public Integer getPreferenceBonus() {
        return preferenceBonus;
    }

    /** @param preferenceBonus the preferenceBonus to set. */
    public void setPreferenceBonus(Integer preferenceBonus) {
        this.preferenceBonus = preferenceBonus;
    }

    /** @return the royalMarriage. */
    @Column(name = "RM")
    public Integer getRoyalMarriage() {
        return royalMarriage;
    }

    /** @param royalMarriage the royalMarriage to set. */
    public void setRoyalMarriage(Integer royalMarriage) {
        this.royalMarriage = royalMarriage;
    }

    /** @return the subsidies. */
    @Column(name = "SUB")
    public Integer getSubsidies() {
        return subsidies;
    }

    /** @param subsidies the subsidies to set. */
    public void setSubsidies(Integer subsidies) {
        this.subsidies = subsidies;
    }

    /** @return the militaryAlliance. */
    @Column(name = "MA")
    public Integer getMilitaryAlliance() {
        return militaryAlliance;
    }

    /** @param militaryAlliance the militaryAlliance to set. */
    public void setMilitaryAlliance(Integer militaryAlliance) {
        this.militaryAlliance = militaryAlliance;
    }

    /** @return the expCorps. */
    @Column(name = "EC")
    public Integer getExpCorps() {
        return expCorps;
    }

    /** @param expCorps the expCorps to set. */
    public void setExpCorps(Integer expCorps) {
        this.expCorps = expCorps;
    }

    /** @return the entryInWar. */
    @Column(name = "EW")
    public Integer getEntryInWar() {
        return entryInWar;
    }

    /** @param entryInWar the entryInWar to set. */
    public void setEntryInWar(Integer entryInWar) {
        this.entryInWar = entryInWar;
    }

    /** @return the vassal. */
    @Column(name = "VA")
    public Integer getVassal() {
        return vassal;
    }

    /** @param vassal the vassal to set. */
    public void setVassal(Integer vassal) {
        this.vassal = vassal;
    }

    /** @return the annexion. */
    @Column(name = "AN")
    public Integer getAnnexion() {
        return annexion;
    }

    /** @param annexion the annexion to set. */
    public void setAnnexion(Integer annexion) {
        this.annexion = annexion;
    }

    /** @return the fidelity. */
    @Column(name = "FIDELITY")
    public int getFidelity() {
        return fidelity;
    }

    /** @param fidelity the fidelity to set. */
    public void setFidelity(int fidelity) {
        this.fidelity = fidelity;
    }

    /** @return the basicForces. */
    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<BasicForceEntity> getBasicForces() {
        return basicForces;
    }

    /** @param basicForces the basicForces to set. */
    public void setBasicForces(List<BasicForceEntity> basicForces) {
        this.basicForces = basicForces;
    }

    /** @return the reinforcements. */
    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ReinforcementsEntity> getReinforcements() {
        return reinforcements;
    }

    /** @param reinforcements the reinforcements to set. */
    public void setReinforcements(List<ReinforcementsEntity> reinforcements) {
        this.reinforcements = reinforcements;
    }

    /** @return the limits. */
    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<LimitEntity> getLimits() {
        return limits;
    }

    /** @param limits the limits to set. */
    public void setLimits(List<LimitEntity> limits) {
        this.limits = limits;
    }

    /** @return the armyClass. */
    @Column(name = "ARMY_CLASS")
    @Enumerated(EnumType.STRING)
    public ArmyClassEnum getArmyClass() {
        return armyClass;
    }

    /** @param armyClass the armyClass to set. */
    public void setArmyClass(ArmyClassEnum armyClass) {
        this.armyClass = armyClass;
    }

    /** @return the capitals. */
    @ManyToMany
    @JoinTable(
            name = "R_COUNTRY_PROVINCE_EU_CAPITALS",
            joinColumns = @JoinColumn(name = "ID_R_COUNTRY"),
            inverseJoinColumns = @JoinColumn(name = "ID_R_PROVINCE_EU")
    )
    public List<EuropeanProvinceEntity> getCapitals() {
        return capitals;
    }

    /** @param capitals the capital to set. */
    public void setCapitals(List<EuropeanProvinceEntity> capitals) {
        this.capitals = capitals;
    }

    /** @return the provinces. */
    @ManyToMany
    @JoinTable(
            name = "R_COUNTRY_PROVINCE_EU",
            joinColumns = @JoinColumn(name = "ID_R_COUNTRY"),
            inverseJoinColumns = @JoinColumn(name = "ID_R_PROVINCE_EU")
    )
    public List<EuropeanProvinceEntity> getProvinces() {
        return provinces;
    }

    /** @param provinces the provinces to set. */
    public void setProvinces(List<EuropeanProvinceEntity> provinces) {
        this.provinces = provinces;
    }
}
