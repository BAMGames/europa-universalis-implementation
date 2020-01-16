package com.mkl.eu.service.service.mapping.military;

import com.mkl.eu.client.service.vo.AbstractWithLoss;
import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.diplo.WarLight;
import com.mkl.eu.client.service.vo.military.BattleDay;
import com.mkl.eu.client.service.vo.military.Siege;
import com.mkl.eu.client.service.vo.military.SiegeCounter;
import com.mkl.eu.client.service.vo.military.SiegeSide;
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
 * Mapping between VO and OE for a Siege.
 *
 * @author MKL.
 */
@Component
public class SiegeMapping extends AbstractMapping {
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
    public List<Siege> oesToVos(List<SiegeEntity> sources, Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (sources == null) {
            return null;
        }

        List<Siege> targets = new ArrayList<>();

        for (SiegeEntity source : sources) {
            Siege target = oeToVo(source, objectsCreated);
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
    private Siege oeToVo(SiegeEntity source, Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (source == null) {
            return null;
        }

        Siege target = new Siege();

        target.setId(source.getId());
        target.setProvince(source.getProvince());
        target.setStatus(source.getStatus());
        target.setTurn(source.getTurn());
        target.setBonus(source.getBonus());
        target.setBreach(source.isBreach());
        target.setFortressFalls(source.isFortressFalls());
        target.setFortressLevel(source.getFortressLevel());
        target.setUndermineDie(source.getUndermineDie());
        target.setUndermineResult(source.getUndermineResult());
        target.setCounters(oesToVosCountries(source.getCounters(), objectsCreated));
        target.setPhasing(oeToVo(source.getPhasing()));
        target.setNonPhasing(oeToVo(source.getNonPhasing()));
        WarLight war = storeVo(WarLight.class, source.getWar(), objectsCreated, warMapping::oeToVoLight);
        target.setWar(war);
        target.setBesiegingOffensive(source.isBesiegingOffensive());

        return target;
    }

    /**
     * OEs to VOs.
     *
     * @param sources        object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    private List<SiegeCounter> oesToVosCountries(Set<SiegeCounterEntity> sources, Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (sources == null) {
            return null;
        }

        List<SiegeCounter> targets = new ArrayList<>();

        for (SiegeCounterEntity source : sources) {
            SiegeCounter target = oeToVo(source);
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
    private SiegeCounter oeToVo(SiegeCounterEntity source) {
        if (source == null) {
            return null;
        }

        SiegeCounter target = new SiegeCounter();

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
    private SiegeSide oeToVo(SiegeSideEntity source) {
        if (source == null) {
            return null;
        }

        SiegeSide target = new SiegeSide();

        target.setTech(source.getTech());
        target.setMoral(source.getMoral());
        target.setSize(source.getSize());
        target.setLosses(oeToVo(source.getLosses()));
        target.setModifiers(oeToVo(source.getModifiers()));
        target.setLossesSelected(source.isLossesSelected());
        target.setLeader(source.getLeader());
        target.setCountry(source.getCountry());
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
