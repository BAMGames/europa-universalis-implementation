package com.mkl.eu.service.service.mapping.ref;

import com.mkl.eu.client.service.vo.ref.Referential;
import com.mkl.eu.client.service.vo.ref.country.*;
import com.mkl.eu.service.service.persistence.oe.ref.country.*;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapping between VO and OE for the referential.
 *
 * @author MKL.
 */
@Component
public class ReferentialMapping {

    /**
     * Fill the countries referential.
     *
     * @param sources     List of countries entity.
     * @param referential the target tables.
     */
    public void fillCountriesReferential(List<CountryEntity> sources, Referential referential) {
        if (referential != null && sources != null) {
            for (CountryEntity source : sources) {
                CountryReferential target = oeToVo(source);
                referential.getCountries().add(target);
            }
        }
    }

    /**
     * OE to VO.
     *
     * @param source object source.
     * @return object mapped.
     */
    private CountryReferential oeToVo(CountryEntity source) {
        if (source == null) {
            return null;
        }

        CountryReferential target = new CountryReferential();

        target.setId(source.getId());
        target.setName(source.getName());
        target.setType(source.getType());
        target.setReligion(source.getReligion());
        target.setCulture(source.getCulture());
        target.setHre(source.isHre());
        target.setElector(source.isElector());
        target.setPreference(source.getPreference());
        target.setPreferenceBonus(source.getPreferenceBonus());
        target.setFidelity(source.getFidelity());
        target.setArmyClass(source.getArmyClass());
        if (source.getCapitals() != null) {
            target.setCapitals(source.getCapitals().stream().map(AbstractProvinceEntity::getName).collect(Collectors.toList()));
        }
        if (source.getProvinces() != null) {
            target.setProvinces(source.getProvinces().stream().map(AbstractProvinceEntity::getName).collect(Collectors.toList()));
        }
        target.setRoyalMarriage(source.getRoyalMarriage());
        target.setSubsidies(source.getSubsidies());
        target.setMilitaryAlliance(source.getMilitaryAlliance());
        target.setExpCorps(source.getExpCorps());
        target.setEntryInWar(source.getEntryInWar());
        target.setVassal(source.getVassal());
        target.setAnnexion(source.getAnnexion());
        target.setBasicForces(oesToVosBasicForces(source.getBasicForces()));
        target.setReinforcements(oesToVosReinforcements(source.getReinforcements()));
        target.setLimits(oesToVos(source.getLimits()));

        return target;
    }

    /**
     * OEs to VOs.
     *
     * @param sources object source.
     * @return object mapped.
     */
    private List<BasicForceReferential> oesToVosBasicForces(List<BasicForceEntity> sources) {
        if (sources == null) {
            return null;
        }

        return sources.stream().map(this::oeToVo).collect(Collectors.toList());
    }

    /**
     * OE to VO.
     *
     * @param source object source.
     * @param target object to map.
     */
    private void oeToVo(ForceEntity source, ForceReferential target) {
        if (source == null || target == null) {
            return;
        }

        target.setId(source.getId());
        target.setNumber(source.getNumber());
        target.setType(source.getType());
        if (source.getCountry() != null) {
            target.setCountry(source.getCountry().getName());
        }
    }

    /**
     * OE to VO.
     *
     * @param source object source.
     * @return object mapped.
     */
    private BasicForceReferential oeToVo(BasicForceEntity source) {
        if (source == null) {
            return null;
        }

        BasicForceReferential target = new BasicForceReferential();

        oeToVo(source, target);

        return target;
    }


    /**
     * OEs to VOs.
     *
     * @param sources object source.
     * @return object mapped.
     */
    private List<ReinforcementsReferential> oesToVosReinforcements(List<ReinforcementsEntity> sources) {
        if (sources == null) {
            return null;
        }

        return sources.stream().map(this::oeToVo).collect(Collectors.toList());
    }

    /**
     * OE to VO.
     *
     * @param source object source.
     * @return object mapped.
     */
    private ReinforcementsReferential oeToVo(ReinforcementsEntity source) {
        if (source == null) {
            return null;
        }

        ReinforcementsReferential target = new ReinforcementsReferential();

        oeToVo(source, target);

        return target;
    }


    /**
     * OEs to VOs.
     *
     * @param sources object source.
     * @return object mapped.
     */
    private List<LimitReferential> oesToVos(List<LimitEntity> sources) {
        if (sources == null) {
            return null;
        }

        return sources.stream().map(this::oeToVo).collect(Collectors.toList());
    }

    /**
     * OE to VO.
     *
     * @param source object source.
     * @return object mapped.
     */
    private LimitReferential oeToVo(LimitEntity source) {
        if (source == null) {
            return null;
        }

        LimitReferential target = new LimitReferential();

        target.setId(source.getId());
        target.setNumber(source.getNumber());
        target.setType(source.getType());
        if (source.getCountry() != null) {
            target.setCountry(source.getCountry().getName());
        }

        return target;
    }
}
