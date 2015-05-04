package com.mkl.eu.service.service.mapping.ref;

import com.mkl.eu.client.service.vo.ref.AbstractProvince;
import com.mkl.eu.client.service.vo.ref.EuropeanProvince;
import com.mkl.eu.service.service.mapping.AbstractMapping;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.EuropeanProvinceEntity;
import org.springframework.stereotype.Component;

/**
 * Mapping between VO and OE for a Counter.
 * TODO check if still used later.
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
            if (province.isPort() != null) {
                ((EuropeanProvince) target).setPort(province.isPort());
            }
            if (province.isPraesidiable() != null) {
                ((EuropeanProvince) target).setPraesidiable(province.isPraesidiable());
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
