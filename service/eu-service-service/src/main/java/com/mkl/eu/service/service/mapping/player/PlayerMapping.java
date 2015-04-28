package com.mkl.eu.service.service.mapping.player;

import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.player.Player;
import com.mkl.eu.service.service.mapping.AbstractMapping;
import com.mkl.eu.service.service.mapping.country.CountryMapping;
import com.mkl.eu.service.service.persistence.oe.player.PlayerEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mapping between VO and OE for a Player.
 *
 * @author MKL.
 */
@Component
public class PlayerMapping extends AbstractMapping {
    /** Mapping for a country. */
    @Autowired
    private CountryMapping countryMapping;

    /**
     * OEs to VOs.
     *
     * @param sources        object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public List<Player> oesToVos(List<PlayerEntity> sources, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (sources == null) {
            return null;
        }

        List<Player> targets = new ArrayList<>();

        for (PlayerEntity source : sources) {
            Player target = storeVo(Player.class, source, objectsCreated, this::oeToVo);
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
    public Player oeToVo(PlayerEntity source, Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (source == null) {
            return null;
        }

        Player target = new Player();

        target.setId(source.getId());
        target.setCountry(storeVo(PlayableCountry.class, source.getCountry(), objectsCreated, countryMapping::oeToVo));

        return target;
    }
}
