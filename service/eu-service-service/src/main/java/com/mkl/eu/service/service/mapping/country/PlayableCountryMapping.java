package com.mkl.eu.service.service.mapping.country;

import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.service.service.mapping.AbstractMapping;
import com.mkl.eu.service.service.mapping.eco.AdministrativeActionMapping;
import com.mkl.eu.service.service.mapping.eco.EconomicalSheetMapping;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mapping between VO and OE for a Country.
 *
 * @author MKL.
 */
@Component
public class PlayableCountryMapping extends AbstractMapping {
    /** Mapping for an economical sheet. */
    @Autowired
    private EconomicalSheetMapping economicalSheetMapping;
    /** Mapping for an administrative action. */
    @Autowired
    private AdministrativeActionMapping administrativeActionMapping;
    /** Mapping for a monarch. */
    @Autowired
    private MonarchMapping monarchMapping;

    /**
     * OEs to VOs.
     *
     * @param sources        object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public List<PlayableCountry> oesToVos(List<PlayableCountryEntity> sources, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        return oesToVos(sources, null, objectsCreated);
    }

    /**
     * OEs to VOs.
     *
     * @param sources        object source.
     * @param idCountry      id of the country of the user doing the request.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public List<PlayableCountry> oesToVos(List<PlayableCountryEntity> sources, Long idCountry, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (sources == null) {
            return null;
        }

        List<PlayableCountry> targets = new ArrayList<>();

        for (PlayableCountryEntity source : sources) {
            PlayableCountry target = storeVo(PlayableCountry.class, source, objectsCreated, (source1, objectsCreated1) -> oeToVo(source1, idCountry, objectsCreated1));
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
    public PlayableCountry oeToVo(PlayableCountryEntity source, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        return oeToVo(source, null, objectsCreated);
    }

    /**
     * OE to VO.
     *
     * @param source         object source.
     * @param idCountry      id of the country of the user doing the request.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public PlayableCountry oeToVo(PlayableCountryEntity source, Long idCountry, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (source == null) {
            return null;
        }

        PlayableCountry target = new PlayableCountry();

        target.setId(source.getId());
        target.setName(source.getName());
        target.setUsername(source.getUsername());
        target.setDti(source.getDti());
        target.setDtiMax(source.getDtiMax());
        target.setFti(source.getFti());
        target.setFtiMax(source.getFtiMax());
        target.setLandTech(source.getLandTech());
        target.setNavalTech(source.getNavalTech());
        target.setEconomicalSheets(economicalSheetMapping.oesToVos(source.getEconomicalSheets(), objectsCreated));
        target.setAdministrativeActions(administrativeActionMapping.oesToVos(source.getAdministrativeActions(), source.getId().equals(idCountry), objectsCreated));
        target.setMonarchs(monarchMapping.oesToVos(source.getMonarchs(), objectsCreated));
        target.setMonarch(monarchMapping.oeToVo(source.getMonarch(), objectsCreated));

        return target;
    }
}
