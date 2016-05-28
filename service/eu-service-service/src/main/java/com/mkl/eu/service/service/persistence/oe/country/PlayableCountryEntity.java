package com.mkl.eu.service.service.persistence.oe.country;

import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import com.mkl.eu.service.service.persistence.oe.eco.AdministrativeActionEntity;
import com.mkl.eu.service.service.persistence.oe.eco.EconomicalSheetEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Country (major or former major or future major one).
 *
 * @author MKL
 */
@Entity
@Table(name = "COUNTRY")
public class PlayableCountryEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /**
     * Name of the country.
     */
    private String name;
    /** Name of the player. External functional id. */
    private String username;
    /** DTI of the country. */
    private int dti;
    /** FTI of the country. */
    private int fti;
    /** FTI of the country in the rotw. */
    private int ftiRotw;
    /** Penalty for colonisation. */
    private int colonisationPenalty;
    /** Current land technology of the country. */
    private String landTech;
    /** Current naval technology of the country. */
    private String navalTech;
    /**
     * Economical sheet of the country.
     */
    private List<EconomicalSheetEntity> economicalSheets = new ArrayList<>();
    /**
     * Administrative actions of the country.
     */
    private List<AdministrativeActionEntity> administrativeActions = new ArrayList<>();
    /** Actual monarch of the country. */
    private MonarchEntity monarch;
    /**
     * Monarchs of the country (including the actual).
     */
    private List<MonarchEntity> monarchs = new ArrayList<>();
    /**
     * Discoveries of the country.
     */
    private List<DiscoveryEntity> discoveries = new ArrayList<>();
    /** Game of the entity. */
    private GameEntity game;

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

    /**
     * @return the name.
     */
    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the username. */
    @Column(name = "USERNAME")
    public String getUsername() {
        return username;
    }

    /** @param username the username to set. */
    public void setUsername(String username) {
        this.username = username;
    }

    /** @return the dti. */
    @Column(name = "DTI")
    public int getDti() {
        return dti;
    }

    /** @param dti the dti to set. */
    public void setDti(int dti) {
        this.dti = dti;
    }

    /** @return the fti. */
    @Column(name = "FTI")
    public int getFti() {
        return fti;
    }

    /** @param fti the fti to set. */
    public void setFti(int fti) {
        this.fti = fti;
    }

    /** @return the ftiRotw. */
    @Column(name = "FTI_ROTW")
    public int getFtiRotw() {
        return ftiRotw;
    }

    /** @param ftiRotw the ftiRotw to set. */
    public void setFtiRotw(int ftiRotw) {
        this.ftiRotw = ftiRotw;
    }

    /** @return the colonisationPenalty. */
    @Column(name = "COL_MALUS")
    public int getColonisationPenalty() {
        return colonisationPenalty;
    }

    /** @param colonisationPenalty the colonisationPenalty to set. */
    public void setColonisationPenalty(int colonisationPenalty) {
        this.colonisationPenalty = colonisationPenalty;
    }

    /** @return the landTech. */
    @Column(name = "T_LAND_TECH")
    public String getLandTech() {
        return landTech;
    }

    /** @param landTech the landTech to set. */
    public void setLandTech(String landTech) {
        this.landTech = landTech;
    }

    /** @return the navalTech. */
    @Column(name = "T_NAVAL_TECH")
    public String getNavalTech() {
        return navalTech;
    }

    /** @param navalTech the navalTech to set. */
    public void setNavalTech(String navalTech) {
        this.navalTech = navalTech;
    }

    /** @return the economicalSheets. */
    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<EconomicalSheetEntity> getEconomicalSheets() {
        return economicalSheets;
    }

    /** @param economicalSheets the economicalSheets to set. */
    public void setEconomicalSheets(List<EconomicalSheetEntity> economicalSheets) {
        this.economicalSheets = economicalSheets;
    }

    /** @return the administrativeActions. */
    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<AdministrativeActionEntity> getAdministrativeActions() {
        return administrativeActions;
    }

    /** @param administrativeActions the administrativeActions to set. */
    public void setAdministrativeActions(List<AdministrativeActionEntity> administrativeActions) {
        this.administrativeActions = administrativeActions;
    }

    /** @return the monarch. */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ID_MONARCH")
    public MonarchEntity getMonarch() {
        return monarch;
    }

    /** @param monarch the monarch to set. */
    public void setMonarch(MonarchEntity monarch) {
        this.monarch = monarch;
    }

    /** @return the monarchs. */
    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<MonarchEntity> getMonarchs() {
        return monarchs;
    }

    /** @param monarchs the monarchs to set. */
    public void setMonarchs(List<MonarchEntity> monarchs) {
        this.monarchs = monarchs;
    }

    /** @return the discoveries. */
    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<DiscoveryEntity> getDiscoveries() {
        return discoveries;
    }

    /** @param discoveries the discoveries to set. */
    public void setDiscoveries(List<DiscoveryEntity> discoveries) {
        this.discoveries = discoveries;
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
