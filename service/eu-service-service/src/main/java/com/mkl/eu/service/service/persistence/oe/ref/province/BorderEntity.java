package com.mkl.eu.service.service.persistence.oe.ref.province;

import com.mkl.eu.client.service.vo.enumeration.BorderEnum;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Border between two provinces.
 *
 * @author MKL.
 */
@Entity
@Table(name = "R_BORDER")
public class BorderEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Province source of the border. */
    private AbstractProvinceEntity provinceFrom;
    /** Province target of the border. */
    private AbstractProvinceEntity provinceTo;
    /** Type of the border. */
    private BorderEnum type;

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

    /** @return the provinceFrom. */
    @ManyToOne
    @JoinColumn(name = "ID_R_PROVINCE_FROM")
    public AbstractProvinceEntity getProvinceFrom() {
        return provinceFrom;
    }

    /** @param provinceFrom the provinceFrom to set. */
    public void setProvinceFrom(AbstractProvinceEntity provinceFrom) {
        this.provinceFrom = provinceFrom;
    }

    /** @return the provinceTo. */
    @ManyToOne
    @JoinColumn(name = "ID_R_PROVINCE_TO")
    public AbstractProvinceEntity getProvinceTo() {
        return provinceTo;
    }

    /** @param provinceTo the provinceTo to set. */
    public void setProvinceTo(AbstractProvinceEntity provinceTo) {
        this.provinceTo = provinceTo;
    }

    /** @return the type. */
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE")
    public BorderEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(BorderEnum type) {
        this.type = type;
    }
}
