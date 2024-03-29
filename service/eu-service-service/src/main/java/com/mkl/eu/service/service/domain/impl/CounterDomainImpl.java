package com.mkl.eu.service.service.domain.impl;

import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.util.GameUtil;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeObjectEnum;
import com.mkl.eu.client.service.vo.tables.Leader;
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
import com.mkl.eu.service.service.service.impl.AbstractBack;
import com.mkl.eu.service.service.util.DiffUtil;
import com.mkl.eu.service.service.util.IOEUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Cross service class for counter manipulation.
 *
 * @author MKL.
 */
@Component
public class CounterDomainImpl extends AbstractBack implements ICounterDomain {
    /** OEUtil. */
    @Autowired
    private IOEUtil oeUtil;
    /** Counter DAO. */
    @Autowired
    private ICounterDao counterDao;
    /** Stack DAO. */
    @Autowired
    private IStackDao stackDao;

    /** {@inheritDoc} */
    @Override
    public DiffEntity createCounter(CounterFaceTypeEnum type, String country, String province, Integer level, GameEntity game) {
        return createAndGetCounter(type, null, country, null, province, level, game);
    }

    /** {@inheritDoc} */
    @Override
    public DiffEntity createCounter(CounterFaceTypeEnum type, String country, Long idStack, GameEntity game) {
        return createAndGetCounter(type, null, country, idStack, null, null, game);
    }

    /** {@inheritDoc} */
    @Override
    public DiffEntity createLeader(CounterFaceTypeEnum type, String code, String country, Long idStack, String province, GameEntity game) {
        return createAndGetCounter(type, code, country, idStack, province, null, game);
    }

    /**
     * Creates a counter.
     *
     * @param type     of the counter to create.
     * @param code     of the counter leader to create.
     * @param country  owner of the counter to create.
     * @param idStack  id of an eventual existing stack.
     * @param province where the counter will be.
     * @param level    new level of the trade fleet or establishment.
     * @param game     the game.
     * @return the diffs related to the creation of the counter and the counter created.
     */
    private DiffEntity createAndGetCounter(CounterFaceTypeEnum type, String code, String country, Long idStack, String province, Integer level, GameEntity game) {
        StackEntity stack = game.getStacks().stream()
                .filter(s -> Objects.equals(idStack, s.getId()))
                .findAny()
                .orElse(null);
        if (stack == null) {
            stack = new StackEntity();
            stack.setProvince(province);
            stack.setGame(game);
            stack.setCountry(country);

        /*
         Thanks Hibernate to have 7 years old bugs.
         https://hibernate.atlassian.net/browse/HHH-6776
         https://hibernate.atlassian.net/browse/HHH-7404
          */

            stackDao.create(stack);
        }

        CounterEntity counterEntity = new CounterEntity();
        counterEntity.setCode(code);
        counterEntity.setCountry(country);
        counterEntity.setType(type);
        counterEntity.setOwner(stack);

        stack.getCounters().add(counterEntity);

        game.getStacks().add(stack);

        level = computeLevel(counterEntity, province, level, game);

        // We want the id to be generated immediately so that we can put it in the diff event.
        counterDao.create(counterEntity);

        return DiffUtil.createDiff(game, DiffTypeEnum.ADD, DiffTypeObjectEnum.COUNTER, counterEntity.getId(),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE, stack.getProvince()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.CODE, counterEntity.getCode(), StringUtils.isNotEmpty(counterEntity.getCode())),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.TYPE, type),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.COUNTRY, country),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STACK, stack.getId().toString()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.LEVEL, level, level != null));
    }

    /** {@inheritDoc} */
    @Override
    public DiffEntity removeCounter(CounterEntity counter) {
        GameEntity game = counter.getOwner().getGame();
        if (counter.getType() == CounterFaceTypeEnum.TRADING_FLEET_MINUS || counter.getType() == CounterFaceTypeEnum.TRADING_FLEET_PLUS) {
            game.getTradeFleets().stream()
                    .filter(tf -> StringUtils.equals(counter.getCountry(), tf.getCountry()) && StringUtils.equals(counter.getOwner().getProvince(), tf.getProvince()))
                    .forEach(tf -> tf.setLevel(0));
        }

        StackEntity stack = counter.getOwner();
        stack.getCounters().remove(counter);
        counter.setOwner(null);
        counterDao.delete(counter);

        if (stack.getCounters().isEmpty()) {
            stack.setGame(null);
            game.getStacks().remove(stack);
        }

        return DiffUtil.createDiff(game, DiffTypeEnum.REMOVE, DiffTypeObjectEnum.COUNTER, counter.getId(),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE, stack.getProvince()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STACK_DEL, stack.getId(), stack.getCounters().isEmpty()));
    }

    /** {@inheritDoc} */
    @Override
    public DiffEntity switchCounter(CounterEntity counter, CounterFaceTypeEnum type, Integer level, GameEntity game) {
        counter.setType(type);
        String province = counter.getOwner().getProvince();

        level = computeLevel(counter, province, level, game);

        return DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.COUNTER, counter.getId(),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.TYPE, type),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE, province),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.LEVEL, level, level != null));
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
    public DiffEntity changeVeteransCounter(Long idCounter, Double veterans, GameEntity game) {
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

        return moveToSpecialBox(counter, province, game);
    }


    /** {@inheritDoc} */
    @Override
    public DiffEntity moveToSpecialBox(CounterEntity counter, String province, GameEntity game) {
        if (counter == null) {
            return null;
        }

        StackEntity stack = CommonUtil.findFirst(game.getStacks().stream(), s -> StringUtils.equals(province, s.getProvince()));
        if (stack == null) {
            stack = createStack(province, counter.getCountry(), game);
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

    /** {@inheritDoc} */
    @Override
    public List<DiffEntity> moveLeader(CounterEntity leader, StackEntity stackTo, String province, GameEntity game) {
        List<DiffEntity> diffs = new ArrayList<>();

        StackEntity stackFrom = leader.getOwner();

        if (stackTo == null) {
            stackTo = createStack(province, leader.getCountry(), game);
        }
        diffs.add(changeCounterOwner(leader, stackTo, game));

        Predicate<Leader> conditions = getLeaderConditions(stackTo.getProvince());
        List<Leader> leaders = oeUtil.getLeaders(stackTo.getCounters(), getTables(), conditions);
        if (leaders.stream().anyMatch(lead -> StringUtils.equals(lead.getCode(), leader.getCode()))) {
            stackTo.setLeader(leader.getCode());
            diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STACK, stackTo.getId(),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.LEADER, leader.getCode())));
        }

        if (StringUtils.equals(stackFrom.getLeader(), leader.getCode())) {
            conditions = getLeaderConditions(stackFrom.getProvince());
            String newLeader = oeUtil.getLeader(stackFrom, getTables(), conditions);
            stackFrom.setLeader(newLeader);
            diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STACK, stackFrom.getId(),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.LEADER, newLeader)));
        }

        return diffs;
    }

    /** {@inheritDoc} */
    @Override
    public StackEntity createStack(String province, String country, GameEntity game) {
        StackEntity stack = new StackEntity();
        stack.setProvince(province);
        stack.setGame(game);
        stack.setCountry(country);

        /*
         Thanks Hibernate to have 7 years old bugs.
         https://hibernate.atlassian.net/browse/HHH-6776
         https://hibernate.atlassian.net/browse/HHH-7404
          */

        stackDao.create(stack);

        game.getStacks().add(stack);

        return stack;
    }

    /** {@inheritDoc} */
    @Override
    public DiffEntity changeCounterOwner(CounterEntity counter, StackEntity newOwner, GameEntity game) {
        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MOVE, DiffTypeObjectEnum.COUNTER, counter.getId(),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STACK_FROM, counter.getOwner().getId()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STACK_TO, newOwner.getId()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE_FROM, counter.getOwner().getProvince()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE_TO, newOwner.getProvince()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STACK_DEL, counter.getOwner().getId(), counter.getOwner().getCounters().size() == 1));

        StackEntity oldStack = counter.getOwner();
        counter.setOwner(newOwner);
        oldStack.getCounters().remove(counter);
        newOwner.getCounters().add(counter);
        if (oldStack.getCounters().isEmpty()) {
            oldStack.setGame(null);
            game.getStacks().remove(oldStack);
        }

        return diff;
    }

    /** {@inheritDoc} */
    @Override
    public DiffEntity changeCounterCountry(CounterEntity counter, String newCountry, GameEntity game) {
        counter.setCountry(newCountry);

        return DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.COUNTER, counter.getId(),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.COUNTRY, newCountry));
    }

    /** {@inheritDoc} */
    @Override
    public Optional<DiffEntity> increaseInflation(GameEntity game) {
        String inflationBox = oeUtil.getInflationBox(game);
        if (GameUtil.isInflationMax(inflationBox)) {
            return Optional.empty();
        }
        int inflationNumber = GameUtil.inflationBoxToNumber(inflationBox);
        String provinceTo = GameUtil.inflationBoxFromNumber(inflationNumber + 1);
        return Optional.of(moveSpecialCounter(CounterFaceTypeEnum.INFLATION, null, provinceTo, game));
    }

    /** {@inheritDoc} */
    @Override
    public Optional<DiffEntity> decreaseInflation(GameEntity game) {
        String inflationBox = oeUtil.getInflationBox(game);
        if (GameUtil.isInflationMin(inflationBox)) {
            return Optional.empty();
        }
        int inflationNumber = GameUtil.inflationBoxToNumber(inflationBox);
        String provinceTo = GameUtil.inflationBoxFromNumber(inflationNumber - 1);
        return Optional.of(moveSpecialCounter(CounterFaceTypeEnum.INFLATION, null, provinceTo, game));
    }
}
