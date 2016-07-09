package com.mkl.eu.service.service.domain.impl;

import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeObjectEnum;
import com.mkl.eu.service.service.domain.ICounterDomain;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.board.IStackDao;
import com.mkl.eu.service.service.persistence.diff.IDiffDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Cross service class for counter manipulation.
 *
 * @author MKL.
 */
@Component
public class CounterDomainImpl implements ICounterDomain {
    /** Counter DAO. */
    @Autowired
    private ICounterDao counterDao;
    /** Stack DAO. */
    @Autowired
    private IStackDao stackDao;
    /** Diff DAO. */
    @Autowired
    private IDiffDao diffDao;

    /** {@inheritDoc} */
    @Override
    public DiffEntity createCounter(CounterFaceTypeEnum type, String country, String province, GameEntity game) {
        StackEntity stack = new StackEntity();
        stack.setProvince(province);
        stack.setGame(game);

        CounterEntity counterEntity = new CounterEntity();
        counterEntity.setCountry(country);
        counterEntity.setType(type);
        counterEntity.setOwner(stack);

        stack.getCounters().add(counterEntity);

        /*
         Thanks Hibernate to have 7 years old bugs.
         https://hibernate.atlassian.net/browse/HHH-6776
         https://hibernate.atlassian.net/browse/HHH-7404
          */

        stackDao.create(stack);

        game.getStacks().add(stack);

        DiffEntity diff = new DiffEntity();
        diff.setIdGame(game.getId());
        diff.setVersionGame(game.getVersion());
        diff.setType(DiffTypeEnum.ADD);
        diff.setTypeObject(DiffTypeObjectEnum.COUNTER);
        diff.setIdObject(counterEntity.getId());
        DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.PROVINCE);
        diffAttributes.setValue(province);
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);
        diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.TYPE);
        diffAttributes.setValue(type.name());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);
        diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.COUNTRY);
        diffAttributes.setValue(country);
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);
        diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.STACK);
        diffAttributes.setValue(stack.getId().toString());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);

        diffDao.create(diff);

        return diff;
    }

    /** {@inheritDoc} */
    @Override
    public DiffEntity removeCounter(Long idCounter, GameEntity game) {
        CounterEntity counter = CommonUtil.findFirst(game.getStacks().stream().flatMap(s -> s.getCounters().stream()),
                c -> c.getId().equals(idCounter));

        if (counter == null) {
            return null;
        }

        StackEntity stack = counter.getOwner();
        stack.getCounters().remove(counter);
        counter.setOwner(null);
        counterDao.delete(counter);

        if (stack.getCounters().isEmpty()) {
            stack.setGame(null);
            game.getStacks().remove(stack);
        }

        DiffEntity diff = new DiffEntity();
        diff.setIdGame(game.getId());
        diff.setVersionGame(game.getVersion());
        diff.setType(DiffTypeEnum.REMOVE);
        diff.setTypeObject(DiffTypeObjectEnum.COUNTER);
        diff.setIdObject(idCounter);
        DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.PROVINCE);
        diffAttributes.setValue(stack.getProvince());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);

        if (stack.getCounters().isEmpty()) {
            diffAttributes = new DiffAttributesEntity();
            diffAttributes.setType(DiffAttributeTypeEnum.STACK_DEL);
            diffAttributes.setValue(stack.getId().toString());
            diffAttributes.setDiff(diff);
            diff.getAttributes().add(diffAttributes);
        }

        diffDao.create(diff);
        return diff;
    }

    /** {@inheritDoc} */
    @Override
    public DiffEntity switchCounter(Long idCounter, CounterFaceTypeEnum type, GameEntity game) {
        CounterEntity counter = CommonUtil.findFirst(game.getStacks().stream().flatMap(s -> s.getCounters().stream()),
                c -> c.getId().equals(idCounter));

        if (counter == null) {
            return null;
        }

        counter.setType(type);

        DiffEntity diff = new DiffEntity();
        diff.setIdGame(game.getId());
        diff.setVersionGame(game.getVersion());
        diff.setType(DiffTypeEnum.MODIFY);
        diff.setTypeObject(DiffTypeObjectEnum.COUNTER);
        diff.setIdObject(idCounter);
        DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.TYPE);
        diffAttributes.setValue(type.name());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);
        diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.PROVINCE);
        diffAttributes.setValue(counter.getOwner().getProvince());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);

        diffDao.create(diff);
        return diff;
    }
}
