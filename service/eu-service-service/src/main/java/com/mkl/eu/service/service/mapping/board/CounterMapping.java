package com.mkl.eu.service.service.mapping.board;

import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.service.service.mapping.AbstractMapping;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mapping between VO and OE for a Counter.
 *
 * @author MKL.
 */
@Component
public class CounterMapping extends AbstractMapping {

    /**
     * OEs to VOs.
     *
     * @param sources        object source.
     * @param parent         of the object to create.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public List<Counter> oesToVos(List<CounterEntity> sources, final Stack parent, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (sources == null) {
            return null;
        }

        List<Counter> targets = new ArrayList<>();

        for (CounterEntity source : sources) {
            Counter target = storeVo(Counter.class, source, objectsCreated, source1 -> oeToVo(source1, parent));
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
     * @param parent         of the object to create.
     * @return object mapped.
     */
    public Counter oeToVo(CounterEntity source, Stack parent) {
        if (source == null) {
            return null;
        }

        Counter target = new Counter();

        target.setId(source.getId());
        target.setType(source.getType());
        target.setCode(source.getCode());
        target.setOwner(parent);
        target.setCountry(source.getCountry());
        target.setVeterans(source.getVeterans());

        return target;
    }
}
