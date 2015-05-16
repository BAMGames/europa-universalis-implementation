package com.mkl.eu.service.service.persistence.oe.ref.province;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * Trade zone. Can be europe or rotw.
 *
 * @author MKL.
 */
@Entity
@Table(name = "R_PROVINCE_TZ")
@PrimaryKeyJoinColumn(name = "ID")
public class TradeZoneProvinceEntity extends AbstractProvinceEntity {
    /** Type of the trade zone (ZP or ZM). */
    private String type;
    /** Name of the sea zone where the trade zone is located. */
    private String seaZone;
    /** In case of ZP, name of the country where the trade zone is. */
    private String countryName;
    /** Income earned by a total monopoly (halved for partial monopoly). */
    private int monopoly;
    /** Income earned by presence (none if partial or total monopoly). */
    private int presence;

    /** @return the type. */
    @Column(name = "TYPE")
    public String getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(String type) {
        this.type = type;
    }

    /** @return the seaZone. */
    @Column(name = "R_PROVINCE")
    public String getSeaZone() {
        return seaZone;
    }

    /** @param seaZone the seaZone to set. */
    public void setSeaZone(String seaZone) {
        this.seaZone = seaZone;
    }

    /** @return the countryName. */
    @Column(name = "R_COUNTRY")
    public String getCountryName() {
        return countryName;
    }

    /** @param countryName the countryName to set. */
    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    /** @return the monopoly. */
    @Column(name = "MONOPOLY")
    public int getMonopoly() {
        return monopoly;
    }

    /** @param monopoly the monopoly to set. */
    public void setMonopoly(int monopoly) {
        this.monopoly = monopoly;
    }

    /** @return the presence. */
    @Column(name = "PRESENCE")
    public int getPresence() {
        return presence;
    }

    /** @param presence the presence to set. */
    public void setPresence(int presence) {
        this.presence = presence;
    }
}
