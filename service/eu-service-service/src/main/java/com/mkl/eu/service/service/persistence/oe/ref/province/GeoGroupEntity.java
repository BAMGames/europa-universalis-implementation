package com.mkl.eu.service.service.persistence.oe.ref.province;

import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * entity for the geographical group entities.
 *
 * @author MKL.
 */
@Entity
@Table(name = "R_GEO_GROUP")
public class GeoGroupEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Name of the geographical group entity. */
    private String name;
    /** Province that belongs to the geographical entity. */
    private String province;
    /** Region that belongs to the geographical entity. */
    private String region;

    /**
     * /** @return the id.
     */
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

    /** @return the name. */
    public String getName() {
        return name;
    }

    /** @param name the name to set. */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the region. */
    @Column(name = "R_REGION")
    public String getRegion() {
        return region;
    }

    /** @param region the region to set. */
    public void setRegion(String region) {
        this.region = region;
    }
}
