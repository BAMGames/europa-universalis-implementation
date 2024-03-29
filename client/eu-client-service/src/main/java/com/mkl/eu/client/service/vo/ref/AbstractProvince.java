package com.mkl.eu.client.service.vo.ref;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.client.service.vo.enumeration.TerrainEnum;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

/**
 * Province. A tile on the board that can contains counters.
 * TODO check if still used later.
 *
 * @author MKL.
 */
@XmlTransient
@XmlSeeAlso({EuropeanProvince.class})
public abstract class AbstractProvince extends EuObject {
    /** Name of the province. Must match the one in the geo.json file. */
    private String name;
    /** Stacks of counters in the province. */
    private List<Stack> stacks = new ArrayList<>();
    /** Terrain of the province. */
    private TerrainEnum terrain;

    /** @return the name. */
    public String getName() {
        return name;
    }

    /** @param name the name to set. */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the stacks. */
    @XmlTransient
    public List<Stack> getStacks() {
        return stacks;
    }

    /** @param stacks the sacks to set. */
    public void setStacks(List<Stack> stacks) {
        this.stacks = stacks;
    }

    /** @return the terrain. */
    public TerrainEnum getTerrain() {
        return terrain;
    }

    /** @param terrain the terrain to set. */
    public void setTerrain(TerrainEnum terrain) {
        this.terrain = terrain;
    }
}
