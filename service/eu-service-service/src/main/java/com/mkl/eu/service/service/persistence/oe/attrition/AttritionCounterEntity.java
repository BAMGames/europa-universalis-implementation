package com.mkl.eu.service.service.persistence.oe.attrition;

import com.mkl.eu.client.service.vo.board.ICounter;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;

import javax.persistence.*;

/**
 * Entity for the relationship table between battle and counter.
 *
 * @author MKL.
 */
@Entity
@Table(name = "ATTRITION_COUNTER")
@AssociationOverrides({
        @AssociationOverride(name = "id.attrition", joinColumns = @JoinColumn(name = "ID_ATTRITION")),
        @AssociationOverride(name = "id.counter", joinColumns = @JoinColumn(name = "ID_COUNTER"))
})
public class AttritionCounterEntity implements ICounter {
    /** Composite id. */
    private AttritionCounterId id = new AttritionCounterId();
    /** The country of the counter. */
    private String country;
    /** The type of the counter. */
    private CounterFaceTypeEnum type;
    /** The code of the counter. */
    private String code;

    /** @return the id. */
    @EmbeddedId
    public AttritionCounterId getId() {
        return id;
    }

    /** @param id the id to set. */
    public void setId(AttritionCounterId id) {
        this.id = id;
    }

    /** @return the attrition. */
    @Transient
    public AttritionEntity getAttrition() {
        return id.getAttrition();
    }

    /** @param attrition the attrition to set. */
    public void setAttrition(AttritionEntity attrition) {
        id.setAttrition(attrition);
    }

    /** @return the counter. */
    @Transient
    public Long getCounter() {
        return id.getCounter();
    }

    /** @param counter the counter to set. */
    public void setCounter(Long counter) {
        id.setCounter(counter);
    }

    /**
     * @return the country of the counter.
     */
    public String getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * @return the type of the counter.
     */
    @Enumerated(EnumType.STRING)
    public CounterFaceTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(CounterFaceTypeEnum type) {
        this.type = type;
    }

    /** @return the code. */
    public String getCode() {
        return code;
    }

    /** @param code the code to set. */
    public void setCode(String code) {
        this.code = code;
    }
}
