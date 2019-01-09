package com.mkl.eu.service.service.persistence.oe.diff;

import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Attribute entity of a diff entity.
 *
 * @author MKL.
 */
@Entity
@Table(name = "D_ATTRIBUTE")
public class DiffAttributesEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Type of the diff attribute. */
    private DiffAttributeTypeEnum type;
    /** Value of the diff attribute. */
    private String value;
    /** Diff owner of the attribute. */
    private DiffEntity diff;

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
    public DiffAttributeTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(DiffAttributeTypeEnum type) {
        this.type = type;
    }

    /** @return the value. */
    @Column(name = "VALUE")
    public String getValue() {
        return value;
    }

    /** @param value the value to set. */
    public void setValue(String value) {
        this.value = value;
    }

    /** @return the diff. */
    @ManyToOne
    @JoinColumn(name = "ID_DIFF")
    public DiffEntity getDiff() {
        return diff;
    }

    /** @param diff the diff to set. */
    public void setDiff(DiffEntity diff) {
        this.diff = diff;
    }
}
