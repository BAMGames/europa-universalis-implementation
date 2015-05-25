package com.mkl.eu.service.service.mapping.country;

import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.country.Relation;
import com.mkl.eu.service.service.mapping.AbstractMapping;
import com.mkl.eu.service.service.persistence.oe.country.RelationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mapping between VO and OE for a Relation.
 *
 * @author MKL.
 */
@Component
public class RelationMapping extends AbstractMapping {
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
    public List<Relation> oesToVos(List<RelationEntity> sources, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (sources == null) {
            return null;
        }

        List<Relation> targets = new ArrayList<>();

        for (RelationEntity source : sources) {
            Relation target = storeVo(Relation.class, source, objectsCreated, this::oeToVo);
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
    public Relation oeToVo(RelationEntity source, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (source == null) {
            return null;
        }

        Relation target = new Relation();

        target.setId(source.getId());
        target.setType(source.getType());
        target.setFirst(storeVo(PlayableCountry.class, source.getFirst(), objectsCreated, playableCountryMapping::oeToVo));
        target.setSecond(storeVo(PlayableCountry.class, source.getSecond(), objectsCreated, playableCountryMapping::oeToVo));

        return target;
    }
}
