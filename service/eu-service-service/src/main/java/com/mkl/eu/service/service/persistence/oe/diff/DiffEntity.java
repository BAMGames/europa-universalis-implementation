package com.mkl.eu.service.service.persistence.oe.diff;

import com.mkl.eu.client.service.vo.enumeration.DiffTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeObjectEnum;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Differential entity in a game.
 * Often used in modification service in response to know what changed in the game.
 *
 * @author MKL.
 */
@Entity
@Table(name = "D_DIFF")
public class DiffEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Type of the diff (ADD, REMOVE, MOVE, ...). */
    private DiffTypeEnum type;
    /** Type of the principal object. */
    private DiffTypeObjectEnum typeObject;
    /** Id of the country (can be null). */
    private Long idCountry;
    /** Id of the principal object. */
    private Long idObject;
    /** Attributes of the diff. */
    private List<DiffAttributesEntity> attributes = new ArrayList<>();
    /** Id of the game concerned by this diff. */
    private Long idGame;
    /** Version of the game concerned by this diff. */
    private Long versionGame;

    /** @return the id. */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "ID")
    @Override
    public Long getId() {
        return id;
    }

    /** @param id the id to set. */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return the type. */
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE")
    public DiffTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(DiffTypeEnum type) {
        this.type = type;
    }

    /** @return the typeObject. */
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE_OBJECT")
    public DiffTypeObjectEnum getTypeObject() {
        return typeObject;
    }

    /** @param typeObject the typeObject to set. */
    public void setTypeObject(DiffTypeObjectEnum typeObject) {
        this.typeObject = typeObject;
    }

    /** @return the idCountry. */
    @Column(name = "ID_COUNTRY")
    public Long getIdCountry() {
        return idCountry;
    }

    /** @param idCountry the idCountry to set. */
    public void setIdCountry(Long idCountry) {
        this.idCountry = idCountry;
    }

    /** @return the idObject. */
    @Column(name = "ID_OBJECT")
    public Long getIdObject() {
        return idObject;
    }

    /** @param idObject the idObject to set. */
    public void setIdObject(Long idObject) {
        this.idObject = idObject;
    }

    /** @return the attributes. */
    @OneToMany(mappedBy = "diff", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<DiffAttributesEntity> getAttributes() {
        return attributes;
    }

    /** @param attributes the attributes to set. */
    public void setAttributes(List<DiffAttributesEntity> attributes) {
        this.attributes = attributes;
    }

    /** @return the idGame. */
    @Column(name = "ID_GAME")
    public Long getIdGame() {
        return idGame;
    }

    /** @param idGame the idGame to set. */
    public void setIdGame(Long idGame) {
        this.idGame = idGame;
    }

    /** @return the versionGame. */
    @Column(name = "VERSION_GAME")
    public Long getVersionGame() {
        return versionGame;
    }

    /** @param versionGame the versionGame to set. */
    public void setVersionGame(Long versionGame) {
        this.versionGame = versionGame;
    }
}
