package com.mkl.eu.service.service.mapping;

import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.GameLight;
import com.mkl.eu.service.service.mapping.board.StackMapping;
import com.mkl.eu.service.service.mapping.country.PlayableCountryMapping;
import com.mkl.eu.service.service.mapping.country.RelationMapping;
import com.mkl.eu.service.service.mapping.event.PoliticalEventMapping;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapping between VO and OE for a Game.
 *
 * @author MKL.
 */
@Component
public class GameMapping {
    /** Mapping for a country. */
    @Autowired
    private PlayableCountryMapping playableCountryMapping;
    /** Mapping for political events. */
    @Autowired
    private PoliticalEventMapping politicalEventMapping;
    /** Mapping for relations. */
    @Autowired
    private RelationMapping relationMapping;
    /** Mapping for stacks. */
    @Autowired
    private StackMapping stackMapping;

    /**
     * OE to VO.
     *
     * @param source    object source.
     * @param idCountry id of the country of the user doing the request.
     * @return object mapped.
     */
    public Game oeToVo(GameEntity source, Long idCountry) {
        if (source == null) {
            return null;
        }

        Game target = new Game();

        target.setId(source.getId());
        target.setStatus(source.getStatus());
        target.setTurn(source.getTurn());
        target.setVersion(source.getVersion());

        Map<Class<?>, Map<Long, Object>> objectsCreated = new HashMap<>();

        target.setCountries(playableCountryMapping.oesToVos(source.getCountries(), idCountry, objectsCreated));

        target.setEvents(politicalEventMapping.oesToVos(source.getEvents(), objectsCreated));

        target.setRelations(relationMapping.oesToVos(source.getRelations(), objectsCreated));

        target.setStacks(stackMapping.oesToVos(source.getStacks(), objectsCreated));

        return target;
    }

    /**
     * OE to VO.
     *
     * @param source object source.
     * @return object mapped.
     */
    public GameLight oeToVoLight(GameEntity source) {
        if (source == null) {
            return null;
        }

        GameLight target = new GameLight();

        target.setId(source.getId());
        target.setStatus(source.getStatus());
        target.setTurn(source.getTurn());

        return target;
    }
}
