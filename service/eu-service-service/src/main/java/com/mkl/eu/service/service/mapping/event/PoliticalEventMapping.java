package com.mkl.eu.service.service.mapping.event;

import com.mkl.eu.client.service.vo.event.PoliticalEvent;
import com.mkl.eu.service.service.mapping.AbstractMapping;
import com.mkl.eu.service.service.persistence.oe.event.PoliticalEventEntity;
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
public class PoliticalEventMapping extends AbstractMapping {
    /**
     * OEs to VOs.
     *
     * @param sources        object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public List<PoliticalEvent> oesToVos(List<PoliticalEventEntity> sources, Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (sources == null) {
            return null;
        }

        List<PoliticalEvent> targets = new ArrayList<>();

        for (PoliticalEventEntity source : sources) {
            PoliticalEvent target = storeVo(PoliticalEvent.class, source, objectsCreated, this::oeToVo);
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
    public PoliticalEvent oeToVo(PoliticalEventEntity source) {
        if (source == null) {
            return null;
        }

        PoliticalEvent target = new PoliticalEvent();

        target.setId(source.getId());
        target.setTurn(source.getTurn());

        return target;
    }
}
