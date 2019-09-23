package com.mkl.eu.client.service.service.common;

import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Request for redeploy service.
 *
 * @author MKL.
 */
public class RedeployRequest {
    /** Redeploys chosen. */
    private List<ProvinceRedeploy> redeploys = new ArrayList<>();

    /**
     * Constructor for jaxb.
     */
    public RedeployRequest() {
    }

    /**
     * Constructor.
     *
     * @param redeploys the redeploys to set.
     */
    public RedeployRequest(List<ProvinceRedeploy> redeploys) {
        this.redeploys = redeploys;
    }

    /** @return the redeploys. */
    public List<ProvinceRedeploy> getRedeploys() {
        return redeploys;
    }

    /** @param redeploys the redeploys to set. */
    public void setRedeploys(List<ProvinceRedeploy> redeploys) {
        this.redeploys = redeploys;
    }

    public static class ProvinceRedeploy {
        /** Name of the province where the counter will be redeployed. */
        private String province;
        /** List of units to redeploy there. */
        private List<Unit> units = new ArrayList<>();

        /**
         * Constructor for jaxb.
         */
        public ProvinceRedeploy() {

        }

        /**
         * Constructor.
         *
         * @param province the province to set.
         */
        public ProvinceRedeploy(String province) {
            this.province = province;
        }

        /** @return the province. */
        public String getProvince() {
            return province;
        }

        /** @param province the province to set. */
        public void setProvince(String province) {
            this.province = province;
        }

        /** @return the units. */
        public List<Unit> getUnits() {
            return units;
        }

        /** @param units the units to set. */
        public void setUnits(List<Unit> units) {
            this.units = units;
        }
    }

    /**
     * Class for determine a unit (by its id if it already exists, by its side if not).
     */
    public static class Unit {
        /** Id of the counter that will be redeployed. */
        private Long idCounter;
        /** Face of the counter to create if the id is <code>null</code>. */
        private CounterFaceTypeEnum face;
        /** Country of the counter to create if the id is <code>null</code>. */
        private String country;

        /** @return the idCounter. */
        public Long getIdCounter() {
            return idCounter;
        }

        /** @param idCounter the idCounter to set. */
        public void setIdCounter(Long idCounter) {
            this.idCounter = idCounter;
        }

        /** @return the face. */
        public CounterFaceTypeEnum getFace() {
            return face;
        }

        /** @param face the face to set. */
        public void setFace(CounterFaceTypeEnum face) {
            this.face = face;
        }

        /** @return the country. */
        public String getCountry() {
            return country;
        }

        /** @param country the country to set. */
        public void setCountry(String country) {
            this.country = country;
        }
    }
}
