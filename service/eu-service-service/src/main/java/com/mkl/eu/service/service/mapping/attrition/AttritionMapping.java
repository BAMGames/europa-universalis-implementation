package com.mkl.eu.service.service.mapping.attrition;

import com.mkl.eu.client.service.vo.attrition.Attrition;
import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.service.service.mapping.AbstractMapping;
import com.mkl.eu.service.service.persistence.oe.attrition.AttritionCounterEntity;
import com.mkl.eu.service.service.persistence.oe.attrition.AttritionEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapping between VO and OE for an Attrition.
 *
 * @author MKL.
 */
@Component
public class AttritionMapping extends AbstractMapping {

    /**
     * OEs to VOs.
     *
     * @param sources object source.
     * @return object mapped.
     */
    public List<Attrition> oesToVos(List<AttritionEntity> sources) {
        if (sources == null) {
            return null;
        }

        List<Attrition> targets = new ArrayList<>();

        for (AttritionEntity source : sources) {
            Attrition target = oeToVo(source);
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
    private Attrition oeToVo(AttritionEntity source) {
        if (source == null) {
            return null;
        }

        Attrition target = new Attrition();

        target.setId(source.getId());
        target.setType(source.getType());
        target.setStatus(source.getStatus());
        target.setTurn(source.getTurn());
        target.setSize(source.getSize());
        target.setTech(source.getTech());
        target.setBonus(source.getBonus());
        target.setDie(source.getDie());
        target.setSecondaryDie(source.getSecondaryDie());
        target.setProvinces(source.getProvinces().stream()
                .collect(Collectors.toList()));
        target.setCounters(oesToVosCounters(source.getCounters()));

        return target;
    }

    /**
     * OEs to VOs.
     *
     * @param sources object source.
     * @return object mapped.
     */
    private List<Counter> oesToVosCounters(Set<AttritionCounterEntity> sources) {
        if (sources == null) {
            return null;
        }

        List<Counter> targets = new ArrayList<>();

        for (AttritionCounterEntity source : sources) {
            Counter target = oeToVo(source);
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
    private Counter oeToVo(AttritionCounterEntity source) {
        if (source == null) {
            return null;
        }

        Counter counter = new Counter();
        counter.setId(source.getCounter());
        counter.setCountry(source.getCountry());
        counter.setType(source.getType());
        counter.setCode(source.getCode());

        return counter;
    }
}
