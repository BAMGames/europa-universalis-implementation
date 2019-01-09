package com.mkl.eu.service.service.persistence.oe.ref.province;

import com.mkl.eu.client.service.vo.enumeration.RelationTypeEnum;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity the resources of a rotw region.
 *
 * @author MKL.
 */
@Entity
@Table(name = "R_RESOURCES")
public class ResourcesEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Number of this type of counter. */
    private Integer number;
    /** Type of limit. */
    private RelationTypeEnum type;
    /** Country owning these forces. */
    private RegionEntity region;

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

    /** @return the number. */
    @Column(name = "NUMBER")
    public Integer getNumber() {
        return number;
    }

    /** @param number the number to set. */
    public void setNumber(Integer number) {
        this.number = number;
    }

    /** @return the type. */
    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    public RelationTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(RelationTypeEnum type) {
        this.type = type;
    }

    /** @return the region. */
    @ManyToOne
    @JoinColumn(name = "ID_R_REGION")
    public RegionEntity getRegion() {
        return region;
    }

    /** @param region the region to set. */
    public void setRegion(RegionEntity region) {
        this.region = region;
    }
}
