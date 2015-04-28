package com.mkl.eu.service.service.mapping.board;

import com.mkl.eu.client.service.vo.board.AbstractProvince;
import com.mkl.eu.client.service.vo.board.EuropeanProvince;
import com.mkl.eu.service.service.mapping.AbstractMapping;
import com.mkl.eu.service.service.persistence.oe.board.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.board.EuropeanProvinceEntity;
import org.springframework.stereotype.Component;

/**
 * Mapping between VO and OE for a Counter.
 *
 * @author MKL.
 */
@Component
public class ProvinceMapping extends AbstractMapping {

    /**
     * OE to VO.
     *
     * @param source         object source.
     * @return object mapped.
     */
    public AbstractProvince oeToVo(AbstractProvinceEntity source) {
        if (source == null) {
            return null;
        }

        AbstractProvince target;
        if (source instanceof EuropeanProvinceEntity) {
            EuropeanProvinceEntity province = (EuropeanProvinceEntity) source;
            target = new EuropeanProvince();
            ((EuropeanProvince) target).setIncome(province.getIncome());
            if (province.getPort() != null) {
                ((EuropeanProvince) target).setPort(province.getPort());
            }
            if (province.getPraesidiable() != null) {
                ((EuropeanProvince) target).setPraesidiable(province.getPraesidiable());
            }
            ((EuropeanProvince) target).setDefaultOwner(province.getDefaultOwner());
        } else {
            return null;
        }

        target.setId(source.getId());
        target.setName(source.getName());
        target.setTerrain(source.getTerrain());

        return target;
    }
}
