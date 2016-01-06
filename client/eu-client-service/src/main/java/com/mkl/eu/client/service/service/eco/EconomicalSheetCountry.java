package com.mkl.eu.client.service.service.eco;

import com.mkl.eu.client.service.vo.eco.EconomicalSheet;

/**
 * Wrapper of an economical sheet and the id of the country it is related.
 *
 * @author MKL.
 */
public class EconomicalSheetCountry {
    /** Id of the country. */
    private Long idCountry;
    /** Economical sheet. */
    private EconomicalSheet sheet;

    /**
     * Constructor for jaxb.
     */
    public EconomicalSheetCountry() {

    }

    /**
     * Constructor.
     *
     * @param idCountry the idCountry to set.
     * @param sheet     the sheet to set.
     */
    public EconomicalSheetCountry(Long idCountry, EconomicalSheet sheet) {
        this.idCountry = idCountry;
        this.sheet = sheet;
    }

    /** @return the idCountry. */
    public Long getIdCountry() {
        return idCountry;
    }

    /** @param idCountry the idCountry to set. */
    public void setIdCountry(Long idCountry) {
        this.idCountry = idCountry;
    }

    /** @return the sheet. */
    public EconomicalSheet getSheet() {
        return sheet;
    }

    /** @param sheet the sheet to set. */
    public void setSheet(EconomicalSheet sheet) {
        this.sheet = sheet;
    }
}
