package com.mkl.eu.service.service.mapping.military;

import com.mkl.eu.client.service.vo.AbstractWithLoss;
import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.diplo.WarLight;
import com.mkl.eu.client.service.vo.military.Battle;
import com.mkl.eu.client.service.vo.military.BattleCounter;
import com.mkl.eu.client.service.vo.military.BattleDay;
import com.mkl.eu.client.service.vo.military.BattleSide;
import com.mkl.eu.service.service.mapping.AbstractMapping;
import com.mkl.eu.service.service.mapping.WithLossMapping;
import com.mkl.eu.service.service.mapping.diplo.WarMapping;
import com.mkl.eu.service.service.persistence.oe.military.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mapping between VO and OE for a Battle.
 *
 * @author MKL.
 */
@Component
public class BattleMapping extends AbstractMapping {
    /** War Mapping. */
    @Autowired
    private WarMapping warMapping;
    /** Loss Mapping. */
    @Autowired
    private WithLossMapping withLossMapping;

    /**
     * OEs to VOs.
     *
     * @param sources        object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public List<Battle> oesToVos(List<BattleEntity> sources, Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (sources == null) {
            return null;
        }

        List<Battle> targets = new ArrayList<>();

        for (BattleEntity source : sources) {
            Battle target = oeToVo(source, objectsCreated);
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
    private Battle oeToVo(BattleEntity source, Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (source == null) {
            return null;
        }

        Battle target = new Battle();

        target.setId(source.getId());
        target.setEnd(source.getEnd());
        target.setProvince(source.getProvince());
        target.setStatus(source.getStatus());
        target.setTurn(source.getTurn());
        target.setWinner(source.getWinner());
        target.setCounters(oesToVosCountries(source.getCounters()));
        target.setPhasing(oeToVo(source.getPhasing()));
        target.setNonPhasing(oeToVo(source.getNonPhasing()));
        WarLight war = storeVo(WarLight.class, source.getWar(), objectsCreated, warMapping::oeToVoLight);
        target.setWar(war);
        target.setPhasingOffensive(source.isPhasingOffensive());

        return target;
    }

    /**
     * OEs to VOs.
     *
     * @param sources        object source.
     * @return object mapped.
     */
    private List<BattleCounter> oesToVosCountries(Set<BattleCounterEntity> sources) {
        if (sources == null) {
            return null;
        }

        List<BattleCounter> targets = new ArrayList<>();

        for (BattleCounterEntity source : sources) {
            BattleCounter target = oeToVo(source);
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
     * @return object mapped.
     */
    private BattleCounter oeToVo(BattleCounterEntity source) {
        if (source == null) {
            return null;
        }

        BattleCounter target = new BattleCounter();

        target.setPhasing(source.isPhasing());
        Counter counter = new Counter();
        counter.setId(source.getCounter());
        counter.setCountry(source.getCountry());
        counter.setType(source.getType());
        counter.setCode(source.getCode());
        target.setCounter(counter);

        return target;
    }

    /**
     * OE to VO.
     *
     * @param source object source.
     * @return object mapped.
     */
    private BattleSide oeToVo(BattleSideEntity source) {
        if (source == null) {
            return null;
        }

        BattleSide target = new BattleSide();

        target.setFireColumn(source.getFireColumn());
        target.setShockColumn(source.getShockColumn());
        target.setTech(source.getTech());
        target.setMoral(source.getMoral());
        target.setLeader(source.getLeader());
        target.setCountry(source.getCountry());
        target.setSize(source.getSize());
        target.setSizeDiff(source.getSizeDiff());
        target.setPursuitMod(source.getPursuitMod());
        target.setLosses(oeToVo(source.getLosses()));
        target.setForces(source.isForces());
        target.setFirstDay(oeToVo(source.getFirstDay()));
        target.setSecondDay(oeToVo(source.getSecondDay()));
        target.setPursuit(source.getPursuit());
        target.setRetreat(source.getRetreat());
        target.setRetreatSelected(source.isRetreatSelected());
        target.setLossesSelected(source.isLossesSelected());
        target.setLeaderCheck(source.getLeaderCheck());
        target.setLeaderWounds(source.getLeaderWounds());

        return target;
    }

    /**
     * OE to VO.
     *
     * @param source object source.
     * @return object mapped.
     */
    private AbstractWithLoss oeToVo(BattleLossesEntity source) {
        if (source == null) {
            return null;
        }

        AbstractWithLoss target = new AbstractWithLoss();
        withLossMapping.oeToVo(source, target);

        return target;
    }

    /**
     * OE to VO.
     *
     * @param source object source.
     * @return object mapped.
     */
    private BattleDay oeToVo(BattleDayEntity source) {
        if (source == null) {
            return null;
        }

        BattleDay target = new BattleDay();

        target.setFireMod(source.getFireMod());
        target.setFire(source.getFire());
        target.setShockMod(source.getShockMod());
        target.setShock(source.getShock());

        return target;
    }
}
