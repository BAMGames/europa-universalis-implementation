package com.mkl.eu.client.service.vo.ref;

import com.mkl.eu.client.service.vo.ref.country.CountryReferential;

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing all referential described in the appendix.
 *
 * @author MKL.
 */
public class Referential {
    /** List of periods. */
    private List<CountryReferential> countries = new ArrayList<>();

    /** @return the countries. */
    public List<CountryReferential> getCountries() {
        return countries;
    }

    /** @param countries the countries to set. */
    public void setCountries(List<CountryReferential> countries) {
        this.countries = countries;
    }
}
