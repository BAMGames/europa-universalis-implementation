package com.mkl.eu.service.service.mapping.eco;

import com.mkl.eu.client.service.service.eco.AdministrativeActionCountry;
import com.mkl.eu.client.service.vo.eco.AdministrativeAction;
import com.mkl.eu.client.service.vo.enumeration.AdminActionStatusEnum;
import com.mkl.eu.service.service.mapping.AbstractMapping;
import com.mkl.eu.service.service.persistence.oe.eco.AdministrativeActionEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapping between VO and OE for an Administrative action.
 *
 * @author MKL.
 */
@Component
public class AdministrativeActionMapping extends AbstractMapping {

    /**
     * OEs to VOs.
     *
     * @param sources        object source.
     * @param full           flag saying that we want the administration actions DONE and PLANNED (if <code>false</code>, will return only the DONE ones).
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public List<AdministrativeAction> oesToVos(List<AdministrativeActionEntity> sources, boolean full, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (sources == null) {
            return null;
        }

        List<AdministrativeAction> targets = new ArrayList<>();

        for (AdministrativeActionEntity source : sources) {
            AdministrativeAction target = storeVo(AdministrativeAction.class, source, objectsCreated, this::oeToVo);
            if (target != null && (target.getStatus() == AdminActionStatusEnum.DONE || full)) {
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
    public AdministrativeAction oeToVo(AdministrativeActionEntity source, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (source == null) {
            return null;
        }

        AdministrativeAction target = new AdministrativeAction();

        target.setId(source.getId());
        target.setTurn(source.getTurn());
        target.setType(source.getType());
        target.setCost(source.getCost());
        target.setColumn(source.getColumn());
        target.setBonus(source.getBonus());
        target.setDie(source.getDie());
        target.setSecondaryDie(source.getSecondaryDie());
        target.setResult(source.getResult());
        target.setSecondaryResult(source.isSecondaryResult());
        target.setStatus(source.getStatus());
        target.setIdObject(source.getIdObject());
        target.setProvince(source.getProvince());
        target.setCounterFaceType(source.getCounterFaceType());

        return target;
    }

    /**
     * OEs to VOs.
     *
     * @param sources object source.
     * @return object mapped.
     */
    public List<AdministrativeActionCountry> oesToVosCountry(List<AdministrativeActionEntity> sources) {
        if (sources == null) {
            return null;
        }

        List<AdministrativeActionCountry> targets = new ArrayList<>();

        Map<Class<?>, Map<Long, Object>> objectsCreated = new HashMap<>();

        for (AdministrativeActionEntity source : sources) {
            AdministrativeActionCountry target = oeToVoCountry(source, objectsCreated);
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
    public AdministrativeActionCountry oeToVoCountry(AdministrativeActionEntity source, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (source == null) {
            return null;
        }

        AdministrativeActionCountry target = new AdministrativeActionCountry();
        target.setAction(oeToVo(source, objectsCreated));
        if (source.getCountry() != null) {
            target.setIdCountry(source.getCountry().getId());
        }

        return target;
    }
}
