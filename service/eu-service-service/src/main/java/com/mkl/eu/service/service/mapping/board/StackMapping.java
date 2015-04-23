package com.mkl.eu.service.service.mapping.board;

import com.mkl.eu.client.service.vo.board.AbstractProvince;
import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.service.service.mapping.AbstractMapping;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mapping between VO and OE for a Stack.
 *
 * @author MKL.
 */
@Component
public class StackMapping extends AbstractMapping {
    /** Mapping for a counter. */
    @Autowired
    private CounterMapping counterMapping;
    /** Mapping for a province. */
    @Autowired
    private ProvinceMapping provinceMapping;

    /**
     * OEs to VOs.
     *
     * @param sources        object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public List<Stack> oesToVos(List<StackEntity> sources, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (sources == null) {
            return null;
        }

        List<Stack> targets = new ArrayList<>();

        for (StackEntity source : sources) {
            Stack target = storeVo(Stack.class, source, objectsCreated, source1 -> oeToVo(source1, objectsCreated));
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
    public Stack oeToVo(StackEntity source, Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (source == null) {
            return null;
        }

        Stack target = new Stack();

        target.setId(source.getId());
        target.setProvince(storeVo(AbstractProvince.class, source.getProvince(), objectsCreated, source1 -> provinceMapping.oeToVo(source1, objectsCreated)));
        target.setCounters(counterMapping.oesToVos(source.getCounters(), target, objectsCreated));

        return target;
    }
}
