package com.mkl.eu.service.service.persistence.oe.diplo;

import com.mkl.eu.client.service.vo.enumeration.WarImplicationEnum;
import com.mkl.eu.service.service.persistence.oe.ref.country.CountryEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity of the association table between War and Country.
 *
 * @author MKL.
 */
@Entity
@Table(name = "WAR_COUNTRY")
@IdClass(CountryInWarEntity.EmbeddedId.class)
public class CountryInWarEntity {
    /** War in which the country is in. */
    private WarEntity war;
    /** Country involved. */
    private CountryEntity country;
    /** Flag to know in which side of the war is the country. */
    private boolean offensive;
    /** Implication of the country in the war. */
    private WarImplicationEnum implication;

    /** @return the war. */
    @Id
    @ManyToOne
    @JoinColumn(name = "ID_WAR")
    public WarEntity getWar() {
        return war;
    }

    /** @param war the war to set. */
    public void setWar(WarEntity war) {
        this.war = war;
    }

    /** @return the country. */
    @Id
    @ManyToOne
    @JoinColumn(name = "R_COUNTRY", referencedColumnName = "NAME")
    public CountryEntity getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(CountryEntity country) {
        this.country = country;
    }

    /** @return the offensive. */
    @Column(name = "OFFENSIVE")
    public boolean isOffensive() {
        return offensive;
    }

    /** @param offensive the offensive to set. */
    public void setOffensive(boolean offensive) {
        this.offensive = offensive;
    }

    /** @return the implication. */
    @Enumerated(EnumType.STRING)
    @Column(name = "IMPLICATION")
    public WarImplicationEnum getImplication() {
        return implication;
    }

    /** @param implication the implication to set. */
    public void setImplication(WarImplicationEnum implication) {
        this.implication = implication;
    }

    /**
     * Embedded class for JPA mapping.
     */
    public static class EmbeddedId implements Serializable {
        /** Composite id war. */
        private WarEntity war;
        /** Composite id country. */
        private CountryEntity country;

        /** @return the war. */
        public WarEntity getWar() {
            return war;
        }

        /** @param war the war to set. */
        public void setWar(WarEntity war) {
            this.war = war;
        }

        /** @return the country. */
        public CountryEntity getCountry() {
            return country;
        }

        /** @param country the country to set. */
        public void setCountry(CountryEntity country) {
            this.country = country;
        }
    }
}
