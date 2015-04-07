package com.mkl.eu.service.service.mapping.country;

import com.mkl.eu.client.service.vo.country.Country;
import com.mkl.eu.service.service.persistence.oe.country.CountryEntity;
import org.springframework.stereotype.Component;

/**
 * Mapping between VO and OE for a Country.
 *
 * @author MKL.
 */
@Component
public class CountryMapping {
    /**
     * OE to VO.
     *
     * @param source object source.
     * @return object mapped.
     */
    public Country oeToVo(CountryEntity source) {
        if (source == null) {
            return null;
        }

        Country target = new Country();

        target.setId(source.getId());
        target.setName(source.getName());

        return target;
    }
}
