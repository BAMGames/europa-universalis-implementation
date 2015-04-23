package com.mkl.eu.service.service.mapping.board;

import com.mkl.eu.client.service.vo.board.AbstractProvince;
import com.mkl.eu.client.service.vo.board.EuropeanProvince;
import com.mkl.eu.client.service.vo.country.Country;
import com.mkl.eu.service.service.mapping.AbstractMapping;
import com.mkl.eu.service.service.mapping.country.CountryMapping;
import com.mkl.eu.service.service.persistence.oe.board.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.board.EuropeanProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.country.CountryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Mapping between VO and OE for a Counter.
 *
 * @author MKL.
 */
@Component
public class ProvinceMapping extends AbstractMapping {
    /** Mapping for a country. */
    @Autowired
    private CountryMapping countryMapping;

    /**
     * OE to VO.
     *
     * @param source         object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public AbstractProvince oeToVo(AbstractProvinceEntity source, Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (source == null) {
            return null;
        }

        AbstractProvince target;
        if (source instanceof EuropeanProvinceEntity) {
            EuropeanProvinceEntity province = (EuropeanProvinceEntity) source;
            target = new EuropeanProvince();
            ((EuropeanProvince) target).setIncome(province.getIncome());
            if (province.getPort() != null) {
                ((EuropeanProvince) target).setPort(province.getPort());
            }
            if (province.getPraesidiable() != null) {
                ((EuropeanProvince) target).setPraesidiable(province.getPraesidiable());
            }
            ((EuropeanProvince) target).setDefaultOwner(storeVo(Country.class, province.getDefaultOwner(), objectsCreated, new ITransformation<CountryEntity, Country>() {
                /** {@inheritDoc} */
                @Override
                public Country transform(CountryEntity source) {
                    return countryMapping.oeToVo(source);
                }
            }));
        } else {
            return null;
        }

        target.setId(source.getId());
        target.setName(source.getName());
        target.setTerrain(source.getTerrain());

        return target;
    }
}
