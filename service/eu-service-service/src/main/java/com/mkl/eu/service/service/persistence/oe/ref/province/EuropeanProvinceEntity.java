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
@Table(name = "R_PROVINCE_EU")
@PrimaryKeyJoinColumn(name = "ID")
public class EuropeanProvinceEntity extends AbstractProvinceEntity {
    /** Base income of the province. */
    private Integer income;
    /** Level of the natural fortress of the province. */
    private Integer fortress;
    /** Flag saying that the province is the capital of the defaultOwner. */
    private Boolean capital;
    /** Name of the country owning of the province if no owner counter is present. */
    private String defaultOwner;
    /** Flag saying that the province contains a natural port. */
    private Boolean port;
    /** Flag saying that the province contains a natural arsenal. */
    private Boolean arsenal;
    /** Flag saying that the natural port can be blocked by a fortress. */
    private Boolean praesidiable;
    /** Number of salt resource in the province. */
    private Integer salt;
    /** Metadata on the province. Names of cities and province for search function. */
    private String metadata;

    /** @return the income. */
    @Column(name = "INCOME")
    public Integer getIncome() {
        return income;
    }

    /** @param income the income to set. */
    public void setIncome(Integer income) {
        this.income = income;
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

    /** @return the capital. */
    @Column(name = "CAPITAL", columnDefinition = "BIT")
    public Boolean isCapital() {
        return capital;
    }

    /** @param capital the capital to set. */
    public void setCapital(Boolean capital) {
        this.capital = capital;
    }

    /** @return the defaultOwner. */
    @Column(name = "R_COUNTRY")
    public String getDefaultOwner() {
        return defaultOwner;
    }

    /** @param defaultOwner the defaultOwner to set. */
    public void setDefaultOwner(String defaultOwner) {
        this.defaultOwner = defaultOwner;
    }

    /** @return the port. */
    @Column(name = "PORT", columnDefinition = "BIT")
    public Boolean isPort() {
        return port;
    }

    /** @param port the port to set. */
    public void setPort(Boolean port) {
        this.port = port;
    }

    /** @return the arsenal. */
    @Column(name = "ARSENAL", columnDefinition = "BIT")
    public Boolean isArsenal() {
        return arsenal;
    }

    /** @param arsenal the arsenal to set. */
    public void setArsenal(Boolean arsenal) {
        this.arsenal = arsenal;
    }

    /** @return the praesidiable. */
    @Column(name = "PRAESIDIABLE", columnDefinition = "BIT")
    public Boolean isPraesidiable() {
        return praesidiable;
    }

    /** @param praesidiable the praesidiable to set. */
    public void setPraesidiable(Boolean praesidiable) {
        this.praesidiable = praesidiable;
    }

    /** @return the salt. */
    @Column(name = "SALT")
    public Integer getSalt() {
        return salt;
    }

    /** @param salt the salt to set. */
    public void setSalt(Integer salt) {
        this.salt = salt;
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
