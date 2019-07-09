package com.mkl.eu.service.service.mapping.diplo;

import com.mkl.eu.client.service.vo.diplo.CountryInWar;
import com.mkl.eu.client.service.vo.diplo.War;
import com.mkl.eu.client.service.vo.ref.country.CountryLight;
import com.mkl.eu.service.service.mapping.AbstractMapping;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryInWarEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.WarEntity;
import com.mkl.eu.service.service.persistence.oe.ref.country.CountryEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mapping between VO and OE for a War.
 *
 * @author MKL.
 */
@Component
public class WarMapping extends AbstractMapping {

    /**
     * OEs to VOs.
     *
     * @param sources        object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public List<War> oesToVos(List<WarEntity> sources, Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (sources == null) {
            return null;
        }

        List<War> targets = new ArrayList<>();

        for (WarEntity source : sources) {
            War target = storeVo(War.class, source, objectsCreated, this::oeToVo);
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
    public War oeToVo(WarEntity source, Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (source == null) {
            return null;
        }

        War target = new War();

        target.setId(source.getId());
        target.setCountries(oesToVosCountries(source.getCountries(), objectsCreated));
        target.setType(source.getType());

        return target;
    }

    /**
     * OEs to VOs.
     *
     * @param sources        object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    private List<CountryInWar> oesToVosCountries(List<CountryInWarEntity> sources, Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (sources == null) {
            return null;
        }

        List<CountryInWar> targets = new ArrayList<>();

        for (CountryInWarEntity source : sources) {
            CountryInWar target = oeToVo(source, objectsCreated);
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
    private CountryInWar oeToVo(CountryInWarEntity source, Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (source == null) {
            return null;
        }

        CountryInWar target = new CountryInWar();

        CountryLight country = storeVo(CountryLight.class, source.getCountry(), objectsCreated, this::oeToVoCountry);
        target.setCountry(country);
        target.setImplication(source.getImplication());
        target.setOffensive(source.isOffensive());

        return target;
    }

    /**
     * OE to VO.
     *
     * @param source object source.
     * @return object mapped.
     */
    private CountryLight oeToVoCountry(CountryEntity source) {
        if (source == null) {
            return null;
        }

        CountryLight target = new CountryLight();

        target.setId(source.getId());
        target.setName(source.getName());
        target.setType(source.getType());
        target.setReligion(source.getReligion());
        target.setCulture(source.getCulture());
        target.setArmyClass(source.getArmyClass());

        return target;
    }
}
