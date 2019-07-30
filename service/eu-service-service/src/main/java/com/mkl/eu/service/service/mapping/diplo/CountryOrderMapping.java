package com.mkl.eu.service.service.mapping.diplo;

import com.mkl.eu.client.service.vo.diplo.CountryOrder;
import com.mkl.eu.service.service.mapping.AbstractMapping;
import com.mkl.eu.service.service.mapping.country.PlayableCountryMapping;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryOrderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mapping between VO and OE for a Political Event.
 *
 * @author MKL.
 */
@Component
public class CountryOrderMapping extends AbstractMapping {
    /** Mapping for a country. */
    @Autowired
    private PlayableCountryMapping playableCountryMapping;

    /**
     * OEs to VOs.
     *
     * @param sources        object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public List<CountryOrder> oesToVos(List<CountryOrderEntity> sources, Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (sources == null) {
            return null;
        }

        List<CountryOrder> targets = new ArrayList<>();

        for (CountryOrderEntity source : sources) {
            CountryOrder target = oeToVo(source, objectsCreated);
            if (target != null) {
                targets.add(target);
            }
        }

        return targets;
    }

    /**
     * OE to VO.
     *
     * @param source         object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public CountryOrder oeToVo(CountryOrderEntity source, Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (source == null) {
            return null;
        }

        CountryOrder target = new CountryOrder();

        target.setCountry(playableCountryMapping.oeToVo(source.getCountry(), objectsCreated));
        target.setGameStatus(source.getGameStatus());
        target.setPosition(source.getPosition());
        target.setActive(source.isActive());
        target.setReady(source.isReady());

        return target;
    }
}
