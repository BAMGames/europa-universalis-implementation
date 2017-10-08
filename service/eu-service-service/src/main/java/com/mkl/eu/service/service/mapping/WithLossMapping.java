package com.mkl.eu.service.service.mapping;

import com.mkl.eu.client.service.vo.AbstractWithLoss;
import com.mkl.eu.service.service.persistence.oe.AbstractWithLossEntity;
import org.springframework.stereotype.Component;

/**
 * Mapping between VO and OE for an object that extends the WithLoss concept.
 *
 * @author MKL.
 */
@Component
public class WithLossMapping extends AbstractMapping {

    /**
     * OE to VO.
     *
     * @param source object source.
     * @param target object target.
     */
    public void oeToVo(AbstractWithLossEntity source, AbstractWithLoss target) {
        if (source == null || target == null) {
            return;
        }

        target.setRoundLoss(source.getRoundLoss());
        target.setThirdLoss(source.getThirdLoss());
        target.setMoraleLoss(source.getMoraleLoss());
    }
}
