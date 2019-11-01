package com.mkl.eu.client.service.service.military;

import com.mkl.eu.client.service.vo.enumeration.LandLootTypeEnum;

/**
 * Request for the landLooting service.
 *
 * @author MKL.
 */
public class LandLootingRequest {
    /** Stack that will pillage. */
    private Long idStack;
    /** Type of land looting. */
    private LandLootTypeEnum type;

    /**
     * Constructor for jaxb.
     */
    public LandLootingRequest() {
    }

    /**
     * Constructor.
     *
     * @param idStack the id of the stack.
     * @param type    the type of looting.
     */
    public LandLootingRequest(Long idStack, LandLootTypeEnum type) {
        this.idStack = idStack;
        this.type = type;
    }

    /** @return the idStack. */
    public Long getIdStack() {
        return idStack;
    }

    /** @param idStack the idStack to set. */
    public void setIdStack(Long idStack) {
        this.idStack = idStack;
    }

    /** @return the type. */
    public LandLootTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(LandLootTypeEnum type) {
        this.type = type;
    }
}
