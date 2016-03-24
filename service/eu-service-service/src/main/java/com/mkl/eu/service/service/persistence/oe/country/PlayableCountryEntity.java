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
    /** Max DTI of the country. */
    private int dtiMax;
    /** FTI of the country. */
    private int fti;
    /** Max FTI of the country. */
    private int ftiMax;
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

    /** @return the dtiMax. */
    @Column(name = "DTI_MAX")
    public int getDtiMax() {
        return dtiMax;
    }

    /** @param dtiMax the dtiMax to set. */
    public void setDtiMax(int dtiMax) {
        this.dtiMax = dtiMax;
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

    /** @return the ftiMax. */
    @Column(name = "FTI_MAX")
    public int getFtiMax() {
        return ftiMax;
    }

    /** @param ftiMax the ftiMax to set. */
    public void setFtiMax(int ftiMax) {
        this.ftiMax = ftiMax;
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
