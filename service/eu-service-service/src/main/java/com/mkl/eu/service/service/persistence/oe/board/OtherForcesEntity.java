package com.mkl.eu.service.service.persistence.oe.board;

import com.mkl.eu.client.service.vo.enumeration.OtherForcesTypeEnum;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Forces not represented with counters.
 *
 * @author MKL
 */
@Entity
@Table(name = "OTHER_FORCES")
public class OtherForcesEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Name of the province where the stack is located. */
    private String province;
    /** Type of forces (Natives, militia,...). */
    private OtherForcesTypeEnum type;
    /** Number of LD. */
    private int nbLd;
    /** Number of LDE. */
    private int nbLde;
    /** Flag saying that the forces are veteran. */
    private boolean veteran;
    /** Flag saying that the forces will replenish at full at the end of the turn. Natives in America and Siberia does not. */
    private boolean replenish;
    /** Game of the entity. */
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

    /** @return the province. */
    @Column(name = "R_PROVINCE")
    public String getProvince() {
        return province;
    }

    /** @param province the province to set. */
    public void setProvince(String province) {
        this.province = province;
    }

    /** @return the type. */
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE")
    public OtherForcesTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(OtherForcesTypeEnum type) {
        this.type = type;
    }

    /** @return the nbLd. */
    @Column(name = "LD")
    public int getNbLd() {
        return nbLd;
    }

    /** @param nbLd the nbLd to set. */
    public void setNbLd(int nbLd) {
        this.nbLd = nbLd;
    }

    /** @return the nbLde. */
    @Column(name = "LDE")
    public int getNbLde() {
        return nbLde;
    }

    /** @param nbLde the nbLde to set. */
    public void setNbLde(int nbLde) {
        this.nbLde = nbLde;
    }

    /** @return the veteran. */
    @Column(name = "VETERAN")
    public boolean isVeteran() {
        return veteran;
    }

    /** @param veteran the veteran to set. */
    public void setVeteran(boolean veteran) {
        this.veteran = veteran;
    }

    /** @return the replenish. */
    @Column(name = "REPLENISH")
    public boolean isReplenish() {
        return replenish;
    }

    /** @param replenish the replenish to set. */
    public void setReplenish(boolean replenish) {
        this.replenish = replenish;
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
