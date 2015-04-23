package com.mkl.eu.service.service.mapping.player;

import com.mkl.eu.client.service.vo.player.Player;
import com.mkl.eu.client.service.vo.player.Relation;
import com.mkl.eu.service.service.mapping.AbstractMapping;
import com.mkl.eu.service.service.persistence.oe.player.RelationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mapping between VO and OE for a Relation.
 *
 * @author MKL.
 */
@Component
public class RelationMapping extends AbstractMapping {
    /** Mapping for a player. */
    @Autowired
    private PlayerMapping playerMapping;

    /**
     * OEs to VOs.
     *
     * @param sources        object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public List<Relation> oesToVos(List<RelationEntity> sources, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (sources == null) {
            return null;
        }

        List<Relation> targets = new ArrayList<>();

        for (RelationEntity source : sources) {
            Relation target = storeVo(Relation.class, source, objectsCreated, source1 -> oeToVo(source1, objectsCreated));
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
    public Relation oeToVo(RelationEntity source, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (source == null) {
            return null;
        }

        Relation target = new Relation();

        target.setId(source.getId());
        target.setType(source.getType());
        target.setFirst(storeVo(Player.class, source.getFirst(), objectsCreated, source1 -> playerMapping.oeToVo(source1, objectsCreated)));
        target.setSecond(storeVo(Player.class, source.getSecond(), objectsCreated, source1 -> playerMapping.oeToVo(source1, objectsCreated)));

        return target;
    }
}
