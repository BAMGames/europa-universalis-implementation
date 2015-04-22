package com.mkl.eu.service.service.mapping.country;

import com.mkl.eu.client.service.vo.country.Country;
import com.mkl.eu.service.service.mapping.AbstractMapping;
import com.mkl.eu.service.service.persistence.oe.country.CountryEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mapping between VO and OE for a Country.
 *
 * @author MKL.
 */
@Component
public class CountryMapping extends AbstractMapping {

    /**
     * OEs to VOs.
     *
     * @param sources        object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public List<Country> oesToVos(List<CountryEntity> sources, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (sources == null) {
            return null;
        }

        List<Country> targets = new ArrayList<>();

        for (CountryEntity source : sources) {
            Country target = storeVo(Country.class, source, objectsCreated, new AbstractMapping.ITransformation<CountryEntity, Country>() {
                /** {@inheritDoc} */
                @Override
                public Country transform(CountryEntity source) {
                    return oeToVo(source);
                }
            });
            if (target != null) {
                targets.add(target);
            }
        }

        return targets;
    }

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
