package com.mkl.eu.service.service.persistence.oe.ref.province;

import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * entity for the presence of gold in a province.
 *
 * @author MKL.
 */
@Entity
@Table(name = "R_GOLD")
public class GoldEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Province where the gold is. */
    private String province;
    /** Amount of gold in the province. */
    private int value;

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

    /** @return the value. */
    @Column(name = "VALUE")
    public int getValue() {
        return value;
    }

    /** @param value the value to set. */
    public void setValue(int value) {
        this.value = value;
    }
}
