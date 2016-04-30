package com.mkl.eu.client.service.service.eco;

import com.mkl.eu.client.service.vo.enumeration.AdminActionTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.InvestmentEnum;

/**
 * Request for addAdminAction service.
 *
 * @author MKL.
 */
public class AddAdminActionRequest {
    /** Id of the country owner of the administrative action. */
    private Long idCountry;
    /** Type of the administrative action. */
    private AdminActionTypeEnum type;
    /** Eventual if of object subject of the administrative action. */
    private Long idObject;
    /** Eventual name of the province subject of the administrative action. */
    private String province;
    /** Eventual type of counter face subject of the administrative action. */
    private CounterFaceTypeEnum counterFaceType;
    /** Eventual investment of the administrative action. */
    private InvestmentEnum investment;

    /**
     * Constructor for jaxb.
     */
    public AddAdminActionRequest() {
    }

    /**
     * Constructor for disband or low maintenance.
     *
     * @param idCountry       the idCountry to set.
     * @param type            the type to set.
     * @param idObject        the idObject to set.
     * @param counterFaceType the counterFaceType to set.
     */
    public AddAdminActionRequest(Long idCountry, AdminActionTypeEnum type, Long idObject, CounterFaceTypeEnum counterFaceType) {
        this.idCountry = idCountry;
        this.type = type;
        this.idObject = idObject;
        this.counterFaceType = counterFaceType;
    }

    /**
     * Constructor for purchase.
     *
     * @param idCountry       the idCountry to set.
     * @param type            the type to set.
     * @param province        the province to set.
     * @param counterFaceType the counterFaceType to set.
     */
    public AddAdminActionRequest(Long idCountry, AdminActionTypeEnum type, String province, CounterFaceTypeEnum counterFaceType) {
        this.idCountry = idCountry;
        this.type = type;
        this.province = province;
        this.counterFaceType = counterFaceType;
    }


    /** @return the idCountry. */
    public Long getIdCountry() {
        return idCountry;
    }

    /** @param idCountry the idCountry to set. */
    public void setIdCountry(Long idCountry) {
        this.idCountry = idCountry;
    }

    /** @return the type. */
    public AdminActionTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(AdminActionTypeEnum type) {
        this.type = type;
    }

    /** @return the idObject. */
    public Long getIdObject() {
        return idObject;
    }

    /** @param idObject the idObject to set. */
    public void setIdObject(Long idObject) {
        this.idObject = idObject;
    }

    /** @return the province. */
    public String getProvince() {
        return province;
    }

    /** @param province the province to set. */
    public void setProvince(String province) {
        this.province = province;
    }

    /** @return the counterFaceType. */
    public CounterFaceTypeEnum getCounterFaceType() {
        return counterFaceType;
    }

    /** @param counterFaceType the counterFaceType to set. */
    public void setCounterFaceType(CounterFaceTypeEnum counterFaceType) {
        this.counterFaceType = counterFaceType;
    }

    /** @return the investment. */
    public InvestmentEnum getInvestment() {
        return investment;
    }

    /** @param investment the investment to set. */
    public void setInvestment(InvestmentEnum investment) {
        this.investment = investment;
    }
}
