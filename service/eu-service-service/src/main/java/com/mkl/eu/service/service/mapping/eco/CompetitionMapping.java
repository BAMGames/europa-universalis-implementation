package com.mkl.eu.service.service.mapping.eco;

import com.mkl.eu.client.service.vo.eco.Competition;
import com.mkl.eu.client.service.vo.eco.CompetitionRound;
import com.mkl.eu.service.service.mapping.AbstractMapping;
import com.mkl.eu.service.service.persistence.oe.eco.CompetitionEntity;
import com.mkl.eu.service.service.persistence.oe.eco.CompetitionRoundEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mapping between VO and OE for a Competition.
 *
 * @author MKL.
 */
@Component
public class CompetitionMapping extends AbstractMapping {

    /**
     * OEs to VOs.
     *
     * @param sources        object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public List<Competition> oesToVos(List<CompetitionEntity> sources, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (sources == null) {
            return null;
        }

        List<Competition> targets = new ArrayList<>();

        for (CompetitionEntity source : sources) {
            Competition target = storeVo(Competition.class, source, objectsCreated, this::oeToVo);
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
    public Competition oeToVo(CompetitionEntity source, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (source == null) {
            return null;
        }

        Competition target = new Competition();

        target.setId(source.getId());
        target.setTurn(source.getTurn());
        target.setType(source.getType());
        target.setProvince(source.getProvince());
        target.setRounds(oesToVosRound(source.getRounds(), objectsCreated));

        return target;
    }

    /**
     * OEs to VOs.
     *
     * @param sources        object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public List<CompetitionRound> oesToVosRound(List<CompetitionRoundEntity> sources, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (sources == null) {
            return null;
        }

        List<CompetitionRound> targets = new ArrayList<>();

        for (CompetitionRoundEntity source : sources) {
            CompetitionRound target = storeVo(CompetitionRound.class, source, objectsCreated, this::oeToVo);
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
    public CompetitionRound oeToVo(CompetitionRoundEntity source, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (source == null) {
            return null;
        }

        CompetitionRound target = new CompetitionRound();

        target.setId(source.getId());
        target.setCountry(source.getCountry());
        target.setRound(source.getRound());
        target.setColumn(source.getColumn());
        target.setDie(source.getDie());
        target.setSecondaryDie(source.getSecondaryDie());
        target.setResult(source.getResult());
        target.setSecondaryResult(source.isSecondaryResult());

        return target;
    }
}
