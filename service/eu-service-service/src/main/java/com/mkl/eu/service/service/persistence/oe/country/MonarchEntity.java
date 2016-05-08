package com.mkl.eu.service.service.persistence.oe.country;

import com.mkl.eu.service.service.persistence.oe.IEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity for a monarch of a major country.
 *
 * @author MKL
 */
@Entity
@Table(name = "MONARCH")
public class MonarchEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Country over the monarch rules. */
    private PlayableCountryEntity country;
    /** Turn when the leader comes to play. */
    private Integer begin;
    /** Turn when the leader dies (he dies at the start of this turn). */
    private Integer end;
    /** Administrative value. */
    private Integer administrative;
    /** Diplomatic value. */
    private Integer diplomacy;
    /** Military value. */
    private Integer military;
    /** Military average value for combats. */
    private Integer militaryAverage;

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

    /** @return the country. */
    @ManyToOne
    @JoinColumn(name = "ID_COUNTRY", nullable = true)
    public PlayableCountryEntity getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(PlayableCountryEntity country) {
        this.country = country;
    }

    /** @return the begin. */
    @Column(name = "BEGIN")
    public Integer getBegin() {
        return begin;
    }

    /** @param begin the begin to set. */
    public void setBegin(Integer begin) {
        this.begin = begin;
    }

    /** @return the end. */
    @Column(name = "END")
    public Integer getEnd() {
        return end;
    }

    /** @param end the end to set. */
    public void setEnd(Integer end) {
        this.end = end;
    }

    /** @return the administrative. */
    @Column(name = "ADM")
    public Integer getAdministrative() {
        return administrative;
    }

    /** @param administrative the administrative to set. */
    public void setAdministrative(Integer administrative) {
        this.administrative = administrative;
    }

    /** @return the diplomacy. */
    @Column(name = "DIP")
    public Integer getDiplomacy() {
        return diplomacy;
    }

    /** @param diplomacy the diplomacy to set. */
    public void setDiplomacy(Integer diplomacy) {
        this.diplomacy = diplomacy;
    }

    /** @return the military. */
    @Column(name = "MIL")
    public Integer getMilitary() {
        return military;
    }

    /** @param military the military to set. */
    public void setMilitary(Integer military) {
        this.military = military;
    }

    /** @return the militaryAverage. */
    @Column(name = "MIL_AVG")
    public Integer getMilitaryAverage() {
        return militaryAverage;
    }

    /** @param militaryAverage the militaryAverage to set. */
    public void setMilitaryAverage(Integer militaryAverage) {
        this.militaryAverage = militaryAverage;
    }
}