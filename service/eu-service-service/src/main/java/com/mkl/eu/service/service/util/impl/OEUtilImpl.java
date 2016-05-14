package com.mkl.eu.service.service.util.impl;

import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.service.util.GameUtil;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.util.IOEUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Utility for OE class.
 *
 * @author MKL.
 */
@Component
public final class OEUtilImpl implements IOEUtil {

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAdministrativeValue(PlayableCountryEntity country) {
        int adm = 3;
        if (country != null && country.getMonarch() != null && country.getMonarch().getAdministrative() != null) {
            adm = country.getMonarch().getAdministrative();
        }
        return adm;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getStability(GameEntity game, String country) {
        int stab = 0;
        if (game != null) {
            CounterEntity stabCounter = CommonUtil.findFirst(game.getStacks().stream().filter(stack -> GameUtil.isStabilityBox(stack.getProvince()))
                            .flatMap(stack -> stack.getCounters().stream()),
                    counter -> StringUtils.equals(country, counter.getCountry()) && counter.getType() == CounterFaceTypeEnum.STABILITY);
            if (stabCounter != null) {
                String box = stabCounter.getOwner().getProvince();
                stab = GameUtil.getStability(box);
            }
        }
        return stab;
    }
}
