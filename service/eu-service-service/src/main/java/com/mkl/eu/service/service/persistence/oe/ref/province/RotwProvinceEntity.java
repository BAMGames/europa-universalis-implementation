package com.mkl.eu.service.service.persistence.oe.ref.province;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * European province. A tile on the european board that can contains counters.
 *
 * @author MKL.
 */
@Entity
@Table(name = "R_PROVINCE_ROTW")
@PrimaryKeyJoinColumn(name = "ID")
public class RotwProvinceEntity extends AbstractProvinceEntity {
    /** Name of the region. */
    private String region;
    /** Level of the natural fortress (can be <code>null</code>). */
    private Integer fortress;
    /** Metadata on the province. Names of cities and province for search function. */
    private String metadata;

    /** @return the region. */
    @Column(name = "REGION")
    public String getRegion() {
        return region;
    }

    /** @param region the region to set. */
    public void setRegion(String region) {
        this.region = region;
    }

    /** @return the fortress. */
    @Column(name = "FORTRESS")
    public Integer getFortress() {
        return fortress;
    }

    /** @param fortress the fortress to set. */
    public void setFortress(Integer fortress) {
        this.fortress = fortress;
    }

    /** @return the metadata. */
    @Column(name = "METADATA")
    public String getMetadata() {
        return metadata;
    }

    /** @param metadata the metadata to set. */
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
