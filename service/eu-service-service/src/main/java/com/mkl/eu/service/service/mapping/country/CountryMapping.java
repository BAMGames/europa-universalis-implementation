package com.mkl.eu.service.service.mapping.country;

import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.service.service.mapping.AbstractMapping;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
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
    public List<PlayableCountry> oesToVos(List<PlayableCountryEntity> sources, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (sources == null) {
            return null;
        }

        List<PlayableCountry> targets = new ArrayList<>();

        for (PlayableCountryEntity source : sources) {
            PlayableCountry target = storeVo(PlayableCountry.class, source, objectsCreated, this::oeToVo);
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
    public PlayableCountry oeToVo(PlayableCountryEntity source) {
        if (source == null) {
            return null;
        }

        PlayableCountry target = new PlayableCountry();

        target.setId(source.getId());
        target.setName(source.getName());

        return target;
    }
}
