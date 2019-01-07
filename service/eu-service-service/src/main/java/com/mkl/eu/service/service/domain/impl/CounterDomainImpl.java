package com.mkl.eu.service.service.domain.impl;

import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeObjectEnum;
import com.mkl.eu.service.service.domain.ICounterDomain;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.board.IStackDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.eco.EstablishmentEntity;
import com.mkl.eu.service.service.persistence.oe.eco.TradeFleetEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.RotwProvinceEntity;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.util.DiffUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
    /** Province DAO. */
    @Autowired
    private IProvinceDao provinceDao;

    /** {@inheritDoc} */
    @Override
    public DiffEntity createCounter(CounterFaceTypeEnum type, String country, String province, Integer level, GameEntity game) {
        return createAndGetCounter(type, country, province, level, game).getLeft();
    }

    /** {@inheritDoc} */
    @Override
    public Pair<DiffEntity, CounterEntity> createAndGetCounter(CounterFaceTypeEnum type, String country, String province, Integer level, GameEntity game) {
        StackEntity stack = new StackEntity();
        stack.setProvince(province);
        stack.setGame(game);
        stack.setCountry(country);

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

        level = computeLevel(counterEntity, province, level, game);

        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.ADD, DiffTypeObjectEnum.COUNTER, counterEntity.getId(),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE, province),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.TYPE, type),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.COUNTRY, country),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STACK, stack.getId().toString()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.LEVEL, level, level != null));

        return new ImmutablePair<>(diff, counterEntity);
    }

    /** {@inheritDoc} */
    @Override
    public DiffEntity removeCounter(Long idCounter, GameEntity game) {
        CounterEntity counter = CommonUtil.findFirst(game.getStacks().stream().flatMap(s -> s.getCounters().stream()),
                c -> c.getId().equals(idCounter));

        if (counter == null) {
            return null;
        }

        if (counter.getType() == CounterFaceTypeEnum.TRADING_FLEET_MINUS || counter.getType() == CounterFaceTypeEnum.TRADING_FLEET_PLUS) {
            TradeFleetEntity tradeFleet = CommonUtil.findFirst(game.getTradeFleets(),
                    tf -> StringUtils.equals(counter.getCountry(), tf.getCountry()) && StringUtils.equals(counter.getOwner().getProvince(), tf.getProvince()));
            if (tradeFleet != null) {
                tradeFleet.setLevel(0);
            }
        }

        StackEntity stack = counter.getOwner();
        stack.getCounters().remove(counter);
        counter.setOwner(null);
        counterDao.delete(counter);

        if (stack.getCounters().isEmpty()) {
            stack.setGame(null);
            game.getStacks().remove(stack);
        }

        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.REMOVE, DiffTypeObjectEnum.COUNTER, idCounter,
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE, stack.getProvince()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STACK_DEL, stack.getId(), stack.getCounters().isEmpty()));

        return diff;
    }

    /** {@inheritDoc} */
    @Override
    public DiffEntity switchCounter(Long idCounter, CounterFaceTypeEnum type, Integer level, GameEntity game) {
        Pair<DiffEntity, CounterEntity> pair = switchAndGetCounter(idCounter, type, level, game);

        if (pair == null) {
            return null;
        }

        return pair.getLeft();
    }

    /** {@inheritDoc} */
    @Override
    public Pair<DiffEntity, CounterEntity> switchAndGetCounter(Long idCounter, CounterFaceTypeEnum type, Integer level, GameEntity game) {
        CounterEntity counter = CommonUtil.findFirst(game.getStacks().stream().flatMap(s -> s.getCounters().stream()),
                c -> c.getId().equals(idCounter));

        if (counter == null) {
            return null;
        }

        counter.setType(type);
        String province = counter.getOwner().getProvince();

        level = computeLevel(counter, province, level, game);

        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.COUNTER, idCounter,
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.TYPE, type),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE, province),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.LEVEL, level, level != null));

        return new ImmutablePair<>(diff, counter);
    }

    /**
     * Creates an establishment or a trading fleet if necessary, and sets it to the new level.
     *
     * @param counter  being created or modified.
     * @param province where the counter is.
     * @param level    new level of the counter (trading fleet / establishment).
     * @param game     the game.
     * @return the level (and <code>null</code> if there should not be a level).
     */
    private Integer computeLevel(CounterEntity counter, String province, Integer level, GameEntity game) {
        if (level != null) {
            if (CounterUtil.isEstablishment(counter.getType())) {
                if (counter.getEstablishment() == null) {
                    EstablishmentEntity establishment = new EstablishmentEntity();

                    establishment.setCounter(counter);
                    establishment.setType(CounterUtil.getEstablishmentType(counter.getType()));
                    AbstractProvinceEntity prov = provinceDao.getProvinceByName(province);
                    if (prov instanceof RotwProvinceEntity) {
                        establishment.setRegion(((RotwProvinceEntity) prov).getRegion());
                    }

                    counter.setEstablishment(establishment);
                }

                counter.getEstablishment().setLevel(level);
            } else if (counter.getType() == CounterFaceTypeEnum.TRADING_FLEET_MINUS ||
                    counter.getType() == CounterFaceTypeEnum.TRADING_FLEET_PLUS) {
                TradeFleetEntity tradeFleet = CommonUtil.findFirst(game.getTradeFleets(),
                        tf -> StringUtils.equals(tf.getCountry(), counter.getCountry()) && StringUtils.equals(tf.getProvince(), province));

                if (tradeFleet == null) {
                    tradeFleet = new TradeFleetEntity();
                    tradeFleet.setCountry(counter.getCountry());
                    tradeFleet.setProvince(province);
                    tradeFleet.setGame(game);
                    game.getTradeFleets().add(tradeFleet);
                }

                tradeFleet.setLevel(level);
            } else {
                level = null;
            }
        }
        return level;
    }

    /** {@inheritDoc} */
    @Override
    public DiffEntity changeVeteransCounter(Long idCounter, Integer veterans, GameEntity game) {
        CounterEntity counter = CommonUtil.findFirst(game.getStacks().stream().flatMap(s -> s.getCounters().stream()),
                c -> c.getId().equals(idCounter));

        if (counter == null) {
            return null;
        }

        counter.setVeterans(veterans);

        return DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.COUNTER, idCounter,
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.VETERANS, veterans.toString()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE, counter.getOwner().getProvince()));
    }

    /** {@inheritDoc} */
    @Override
    public DiffEntity moveSpecialCounter(CounterFaceTypeEnum type, String country, String province, GameEntity game) {
        CounterEntity counter = CommonUtil.findFirst(game.getStacks().stream().flatMap(s -> s.getCounters().stream()),
                c -> StringUtils.equals(country, c.getCountry()) && c.getType() == type);

        if (counter == null) {
            return null;
        }

        StackEntity stack = CommonUtil.findFirst(game.getStacks().stream(), s -> StringUtils.equals(province, s.getProvince()));
        if (stack == null) {
            stack = new StackEntity();
            stack.setProvince(province);
            stack.setGame(game);
            stack.setCountry(counter.getCountry());

            /**
             Thanks Hibernate to have 7 years old bugs.
             https://hibernate.atlassian.net/browse/HHH-6776
             https://hibernate.atlassian.net/browse/HHH-7404
             */

            stackDao.create(stack);

            game.getStacks().add(stack);
        }

        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MOVE, DiffTypeObjectEnum.COUNTER, counter.getId(),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STACK_FROM, counter.getOwner().getId().toString()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STACK_TO, stack.getId().toString()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE_FROM, counter.getOwner().getProvince()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE_TO, stack.getProvince()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STACK_DEL, counter.getOwner().getId(), counter.getOwner().getCounters().size() == 1));

        StackEntity oldStack = counter.getOwner();
        counter.setOwner(stack);
        oldStack.getCounters().remove(counter);
        stack.getCounters().add(counter);
        if (oldStack.getCounters().isEmpty()) {
            oldStack.setGame(null);
            game.getStacks().remove(oldStack);
        }

        return diff;
    }
}
