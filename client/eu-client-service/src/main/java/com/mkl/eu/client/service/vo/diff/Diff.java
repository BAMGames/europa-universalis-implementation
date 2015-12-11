package com.mkl.eu.client.service.vo.diff;

import com.mkl.eu.client.service.vo.enumeration.DiffTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeObjectEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Differential in a game.
 * Often used in modification service in response to know what changed in the game.
 *
 * @author MKL.
 */
public class Diff implements Serializable {
    /** Version of the game affected by this diff. */
    private Long versionGame;
    /** Type of the diff (ADD, REMOVE, MOVE, ...). */
    private DiffTypeEnum type;
    /** Type of the principal object. */
    private DiffTypeObjectEnum typeObject;
    /** Id of the principal object. */
    private Long idObject;
    /** Attributes of the diff. */
    private List<DiffAttributes> attributes = new ArrayList<>();

    /** @return the versionGame. */
    public Long getVersionGame() {
        return versionGame;
    }

    /** @param versionGame the versionGame to set. */
    public void setVersionGame(Long versionGame) {
        this.versionGame = versionGame;
    }

    /** @return the type. */
    public DiffTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(DiffTypeEnum type) {
        this.type = type;
    }

    /** @return the typeObject. */
    public DiffTypeObjectEnum getTypeObject() {
        return typeObject;
    }

    /** @param typeObject the typeObject to set. */
    public void setTypeObject(DiffTypeObjectEnum typeObject) {
        this.typeObject = typeObject;
    }

    /** @return the idObject. */
    public Long getIdObject() {
        return idObject;
    }

    /** @param idObject the idObject to set. */
    public void setIdObject(Long idObject) {
        this.idObject = idObject;
    }

    /** @return the attributes. */
    public List<DiffAttributes> getAttributes() {
        return attributes;
    }

    /** @param attributes the attributes to set. */
    public void setAttributes(List<DiffAttributes> attributes) {
        this.attributes = attributes;
    }
}
