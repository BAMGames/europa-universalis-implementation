package com.mkl.eu.service.service.mapping.diff;

import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffAttributes;
import com.mkl.eu.service.service.mapping.AbstractMapping;
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapping between VO and OE for a Diff and DiffAttribute.
 *
 * @author MKL.
 */
@Component
public class DiffMapping extends AbstractMapping {

    /**
     * OEs to VOs.
     *
     * @param sources object source.
     * @return object mapped.
     */
    public List<Diff> oesToVos(List<DiffEntity> sources) {
        if (sources == null) {
            return null;
        }

        List<Diff> targets = new ArrayList<>();

        for (DiffEntity source : sources) {
            Diff target = oeToVo(source);
            if (target != null) {
                targets.add(target);
            }
        }

        return targets;
    }

    /**
     * OE to VO.
     *
     * @param source object source.
     * @return object mapped.
     */
    public Diff oeToVo(DiffEntity source) {
        if (source == null) {
            return null;
        }

        Diff target = new Diff();

        target.setVersionGame(source.getVersionGame());
        target.setType(source.getType());
        target.setTypeObject(source.getTypeObject());
        target.setIdObject(source.getIdObject());
        target.setAttributes(oesToVosAttr(source.getAttributes()));

        return target;
    }

    /**
     * OEs to VOs.
     *
     * @param sources object source.
     * @return object mapped.
     */
    public List<DiffAttributes> oesToVosAttr(List<DiffAttributesEntity> sources) {
        if (sources == null) {
            return null;
        }

        List<DiffAttributes> targets = new ArrayList<>();

        for (DiffAttributesEntity source : sources) {
            DiffAttributes target = oeToVo(source);
            if (target != null) {
                targets.add(target);
            }
        }

        return targets;
    }

    /**
     * OE to VO.
     *
     * @param source object source.
     * @return object mapped.
     */
    public DiffAttributes oeToVo(DiffAttributesEntity source) {
        if (source == null) {
            return null;
        }

        DiffAttributes target = new DiffAttributes();

        target.setType(source.getType());
        target.setValue(source.getValue());

        return target;
    }
}
