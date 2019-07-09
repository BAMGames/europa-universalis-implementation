package com.mkl.eu.service.service.mapping;

import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.GameLight;
import com.mkl.eu.service.service.mapping.board.StackMapping;
import com.mkl.eu.service.service.mapping.country.PlayableCountryMapping;
import com.mkl.eu.service.service.mapping.diplo.CountryOrderMapping;
import com.mkl.eu.service.service.mapping.diplo.WarMapping;
import com.mkl.eu.service.service.mapping.eco.CompetitionMapping;
import com.mkl.eu.service.service.mapping.eco.TradeFleetMapping;
import com.mkl.eu.service.service.mapping.event.PoliticalEventMapping;
import com.mkl.eu.service.service.mapping.military.BattleMapping;
import com.mkl.eu.service.service.mapping.military.SiegeMapping;
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
    /** Mapping for wars. */
    @Autowired
    private WarMapping warMapping;
    /** Mapping for stacks. */
    @Autowired
    private StackMapping stackMapping;
    /** Mapping for trade fleets. */
    @Autowired
    private TradeFleetMapping tradeFleetMapping;
    /** Mapping for competition. */
    @Autowired
    private CompetitionMapping competitionMapping;
    /** Mapping for country orders. */
    @Autowired
    private CountryOrderMapping countryOrderMapping;
    /** Mapping for battles. */
    @Autowired
    private BattleMapping battleMapping;
    /** Mapping for sieges. */
    @Autowired
    private SiegeMapping siegeMapping;

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

        target.setWars(warMapping.oesToVos(source.getWars(), objectsCreated));

        target.setStacks(stackMapping.oesToVos(source.getStacks(), objectsCreated));

        target.setTradeFleets(tradeFleetMapping.oesToVos(source.getTradeFleets(), objectsCreated));

        target.setCompetitions(competitionMapping.oesToVos(source.getCompetitions(), objectsCreated));

        target.setOrders(countryOrderMapping.oesToVos(source.getOrders(), objectsCreated));

        target.setBattles(battleMapping.oesToVos(source.getBattles(), objectsCreated));

        target.setSieges(siegeMapping.oesToVos(source.getSieges(), objectsCreated));

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
