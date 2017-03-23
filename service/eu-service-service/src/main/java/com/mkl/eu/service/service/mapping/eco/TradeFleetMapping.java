package com.mkl.eu.service.service.mapping.eco;

import com.mkl.eu.client.service.vo.eco.TradeFleet;
import com.mkl.eu.service.service.mapping.AbstractMapping;
import com.mkl.eu.service.service.persistence.oe.eco.TradeFleetEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mapping between VO and OE for a Trade fleet.
 *
 * @author MKL.
 */
@Component
public class TradeFleetMapping extends AbstractMapping {

    /**
     * OEs to VOs.
     *
     * @param sources        object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public List<TradeFleet> oesToVos(List<TradeFleetEntity> sources, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (sources == null) {
            return null;
        }

        List<TradeFleet> targets = new ArrayList<>();

        for (TradeFleetEntity source : sources) {
            TradeFleet target = storeVo(TradeFleet.class, source, objectsCreated, this::oeToVo);
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
    public TradeFleet oeToVo(TradeFleetEntity source, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (source == null) {
            return null;
        }

        TradeFleet target = new TradeFleet();

        target.setId(source.getId());
        target.setCountry(source.getCountry());
        target.setProvince(source.getProvince());
        target.setLevel(source.getLevel());

        return target;
    }
}
