package com.mkl.eu.service.service.persistence.oe.ref.province;

import com.mkl.eu.service.service.persistence.oe.IEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * entity for a group of provinces in the ROTW.
 *
 * @author MKL.
 */
@Entity
@Table(name = "R_REGION")
public class RegionEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Name of the region. */
    private String name;
    /** Income of the region. */
    private int income;
    /** Difficulty of the region. */
    private int difficulty;
    /** Tolerance of the region. */
    private int tolerance;
    /** Number of natives in each province of the region. */
    private int nativesNumber;
    /** Type of natives in each province of the region. */
    private String nativesType;
    /** List of resources of the region. */
    private List<ResourcesEntity> resources = new ArrayList<>();
    /** Penalty for cold area. <code>null</code> for no cold area. */
    private Integer coldArea;

    /**
     * /** @return the id.
     */
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

    /** @return the income. */
    @Column(name = "INCOME")
    public int getIncome() {
        return income;
    }

    /** @param income the income to set. */
    public void setIncome(int income) {
        this.income = income;
    }

    /** @return the difficulty. */
    @Column(name = "DIFFICULTY")
    public int getDifficulty() {
        return difficulty;
    }

    /** @param difficulty the difficulty to set. */
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    /** @return the tolerance. */
    @Column(name = "TOLERANCE")
    public int getTolerance() {
        return tolerance;
    }

    /** @param tolerance the tolerance to set. */
    public void setTolerance(int tolerance) {
        this.tolerance = tolerance;
    }

    /** @return the nativesNumber. */
    @Column(name = "NATIVES_NUMBER")
    public int getNativesNumber() {
        return nativesNumber;
    }

    /** @param nativesNumber the nativesNumber to set. */
    public void setNativesNumber(int nativesNumber) {
        this.nativesNumber = nativesNumber;
    }

    /** @return the nativesType. */
    @Column(name = "NATIVES_TYPE")
    public String getNativesType() {
        return nativesType;
    }

    /** @param nativesType the nativesType to set. */
    public void setNativesType(String nativesType) {
        this.nativesType = nativesType;
    }

    /** @return the resources. */
    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ResourcesEntity> getResources() {
        return resources;
    }

    /** @param resources the resources to set. */
    public void setResources(List<ResourcesEntity> resources) {
        this.resources = resources;
    }

    /** @return the coldArea. */
    @Column(name = "COLD_AREA")
    public Integer getColdArea() {
        return coldArea;
    }

    /** @param coldArea the coldArea to set. */
    public void setColdArea(Integer coldArea) {
        this.coldArea = coldArea;
    }
}
