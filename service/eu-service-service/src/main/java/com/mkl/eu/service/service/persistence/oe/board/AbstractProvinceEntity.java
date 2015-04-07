package com.mkl.eu.service.service.persistence.oe.board;

import com.mkl.eu.client.service.vo.enumeration.TerrainEnum;
import com.mkl.eu.service.service.persistence.oe.IEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Province. A tile on the board that can contains counters.
 *
 * @author MKL.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "PROVINCE")
public abstract class AbstractProvinceEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Name of the province. Must match the one in the geo.json file. */
    private String name;
    /** Stacks of counters in the province. */
    private List<StackEntity> stacks;
    /** Terrain of the province. */
    private TerrainEnum terrain;

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

    /** @return the name. */
    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    /** @param name the name to set. */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the stacks. */
    @OneToMany(mappedBy = "province", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<StackEntity> getStacks() {
        return stacks;
    }

    /** @param stacks the stacks to set. */
    public void setStacks(List<StackEntity> stacks) {
        this.stacks = stacks;
    }

    /** @return the terrain. */
    @Enumerated(EnumType.STRING)
    @Column(name = "TERRAIN")
    public TerrainEnum getTerrain() {
        return terrain;
    }

    /** @param terrain the terrain to set. */
    public void setTerrain(TerrainEnum terrain) {
        this.terrain = terrain;
    }
}
