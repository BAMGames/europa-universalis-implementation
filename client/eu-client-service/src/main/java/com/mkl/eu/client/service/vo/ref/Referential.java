package com.mkl.eu.client.service.vo.ref;

import com.mkl.eu.client.service.vo.ref.country.CountryReferential;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * @param countryName the name of the country.
     * @return the country whose name is countryName.
     */
    public CountryReferential getCountry(String countryName) {
        return countries.stream()
                .filter(country -> StringUtils.equals(country.getName(), countryName))
                .findAny()
                .orElse(null);
    }
}
