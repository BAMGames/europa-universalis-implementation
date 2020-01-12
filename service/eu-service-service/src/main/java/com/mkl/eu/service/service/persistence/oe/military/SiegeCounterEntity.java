package com.mkl.eu.service.service.persistence.oe.military;

import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;

import javax.persistence.*;

/**
 * Entity for the relationship table between siege and counter.
 *
 * @author MKL.
 */
@Entity
@Table(name = "SIEGE_COUNTER")
@AssociationOverrides({
        @AssociationOverride(name = "id.siege", joinColumns = @JoinColumn(name = "ID_SIEGE")),
        @AssociationOverride(name = "id.counter", joinColumns = @JoinColumn(name = "ID_COUNTER"))
})
public class SiegeCounterEntity {
    /** Composite id. */
    private SiegeCounterId id = new SiegeCounterId();
    /** The country of the counter. */
    private String country;
    /** The type of the counter. */
    private CounterFaceTypeEnum type;
    /** The code of the counter. */
    private String code;
    /** Flag <code>true</code> when the counter is in the phasing side. */
    private boolean phasing;

    /** @return the id. */
    @EmbeddedId
    public SiegeCounterId getId() {
        return id;
    }

    /** @param id the id to set. */
    public void setId(SiegeCounterId id) {
        this.id = id;
    }

    /** @return the siege. */
    @Transient
    public SiegeEntity getSiege() {
        return id.getSiege();
    }

    /** @param siege the siege to set. */
    public void setSiege(SiegeEntity siege) {
        id.setSiege(siege);
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

    /** @return the phasing. */
    @Column(name = "PHASING")
    public boolean isPhasing() {
        return phasing;
    }

    /** @return the not phasing. */
    @Transient
    public boolean isNotPhasing() {
        return !phasing;
    }

    /** @param phasing the phasing to set. */
    public void setPhasing(boolean phasing) {
        this.phasing = phasing;
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
