package com.mkl.eu.service.service.domain;

import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.service.service.domain.impl.CounterDomainImpl;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.board.IStackDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.eco.EstablishmentEntity;
import com.mkl.eu.service.service.persistence.oe.eco.TradeFleetEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.RotwProvinceEntity;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.service.AbstractGameServiceTest;
import com.mkl.eu.service.service.util.IOEUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Objects;
import java.util.Optional;

import static com.mkl.eu.client.common.util.CommonUtil.EPSILON;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

/**
 * Test of CounterDomainImpl.
 *
 * @author MKL.
 */
@RunWith(MockitoJUnitRunner.class)
public class CounterDomainTest {
    @InjectMocks
    private CounterDomainImpl counterDomain;

    @Mock
    private IOEUtil oeUtil;

    @Mock
    private ICounterDao counterDao;

    @Mock
    private IStackDao stackDao;

    @Mock
    private IProvinceDao provinceDao;

    @Test
    public void testCreateCounter() throws Exception {
        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);

        when(stackDao.create(anyObject())).thenAnswer(invocationOnMock -> {
            StackEntity stack = (StackEntity) invocationOnMock.getArguments()[0];

            stack.setId(2L);

            return stack;
        });

        DiffEntity diff = counterDomain.createCounter(CounterFaceTypeEnum.ARMY_MINUS, "france", "idf", null, game);

        InOrder inOrder = inOrder(stackDao);
        inOrder.verify(stackDao).create(anyObject());

        Assert.assertEquals(game.getId(), diff.getIdGame());
        Assert.assertEquals(game.getVersion(), diff.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.ADD, diff.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diff.getTypeObject());
        Assert.assertEquals(4, diff.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diff.getAttributes().get(0).getType());
        Assert.assertEquals("idf", diff.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diff.getAttributes().get(1).getType());
        Assert.assertEquals(CounterFaceTypeEnum.ARMY_MINUS.name(), diff.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COUNTRY, diff.getAttributes().get(2).getType());
        Assert.assertEquals("france", diff.getAttributes().get(2).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK, diff.getAttributes().get(3).getType());
        Assert.assertEquals("2", diff.getAttributes().get(3).getValue());

        Assert.assertEquals(1, game.getStacks().size());
        Assert.assertEquals("idf", game.getStacks().get(0).getProvince());
        Assert.assertEquals("france", game.getStacks().get(0).getCountry());
        Assert.assertEquals(1, game.getStacks().get(0).getCounters().size());
        Assert.assertEquals("france", game.getStacks().get(0).getCounters().get(0).getCountry());
        Assert.assertEquals(CounterFaceTypeEnum.ARMY_MINUS, game.getStacks().get(0).getCounters().get(0).getType());
    }

    @Test
    public void testCreateCounterEstablishment() throws Exception {
        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);

        RotwProvinceEntity prov = new RotwProvinceEntity();
        prov.setRegion("regionTest");

        when(provinceDao.getProvinceByName("rAzteca~C")).thenReturn(prov);

        when(stackDao.create(anyObject())).thenAnswer(invocationOnMock -> {
            StackEntity stack = (StackEntity) invocationOnMock.getArguments()[0];

            stack.setId(2L);

            return stack;
        });

        DiffEntity diff = counterDomain.createCounter(CounterFaceTypeEnum.TRADING_POST_MINUS, "france", "rAzteca~C", 3, game);

        InOrder inOrder = inOrder(stackDao);
        inOrder.verify(stackDao).create(anyObject());

        Assert.assertEquals(game.getId(), diff.getIdGame());
        Assert.assertEquals(game.getVersion(), diff.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.ADD, diff.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diff.getTypeObject());
        Assert.assertEquals(5, diff.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diff.getAttributes().get(0).getType());
        Assert.assertEquals("rAzteca~C", diff.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diff.getAttributes().get(1).getType());
        Assert.assertEquals(CounterFaceTypeEnum.TRADING_POST_MINUS.name(), diff.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COUNTRY, diff.getAttributes().get(2).getType());
        Assert.assertEquals("france", diff.getAttributes().get(2).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK, diff.getAttributes().get(3).getType());
        Assert.assertEquals("2", diff.getAttributes().get(3).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.LEVEL, diff.getAttributes().get(4).getType());
        Assert.assertEquals("3", diff.getAttributes().get(4).getValue());

        Assert.assertEquals(1, game.getStacks().size());
        Assert.assertEquals(2L, game.getStacks().get(0).getId().longValue());
        Assert.assertEquals("rAzteca~C", game.getStacks().get(0).getProvince());
        Assert.assertEquals(1, game.getStacks().get(0).getCounters().size());
        Assert.assertEquals("france", game.getStacks().get(0).getCounters().get(0).getCountry());
        Assert.assertEquals(CounterFaceTypeEnum.TRADING_POST_MINUS, game.getStacks().get(0).getCounters().get(0).getType());
        Assert.assertEquals(EstablishmentTypeEnum.TRADING_POST, game.getStacks().get(0).getCounters().get(0).getEstablishment().getType());
        Assert.assertEquals("regionTest", game.getStacks().get(0).getCounters().get(0).getEstablishment().getRegion());
        Assert.assertEquals(3, game.getStacks().get(0).getCounters().get(0).getEstablishment().getLevel().intValue());
    }

    @Test
    public void testCreateCounterTradeFleet() throws Exception {
        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);

        when(stackDao.create(anyObject())).thenAnswer(invocationOnMock -> {
            StackEntity stack = (StackEntity) invocationOnMock.getArguments()[0];

            stack.setId(2L);

            return stack;
        });

        DiffEntity diff = counterDomain.createCounter(CounterFaceTypeEnum.TRADING_FLEET_MINUS, "france", "ZPFrance", 1, game);

        InOrder inOrder = inOrder(stackDao);
        inOrder.verify(stackDao).create(anyObject());

        Assert.assertEquals(game.getId(), diff.getIdGame());
        Assert.assertEquals(game.getVersion(), diff.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.ADD, diff.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diff.getTypeObject());
        Assert.assertEquals(5, diff.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diff.getAttributes().get(0).getType());
        Assert.assertEquals("ZPFrance", diff.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diff.getAttributes().get(1).getType());
        Assert.assertEquals(CounterFaceTypeEnum.TRADING_FLEET_MINUS.name(), diff.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COUNTRY, diff.getAttributes().get(2).getType());
        Assert.assertEquals("france", diff.getAttributes().get(2).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK, diff.getAttributes().get(3).getType());
        Assert.assertEquals("2", diff.getAttributes().get(3).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.LEVEL, diff.getAttributes().get(4).getType());
        Assert.assertEquals("1", diff.getAttributes().get(4).getValue());

        Assert.assertEquals(1, game.getStacks().size());
        Assert.assertEquals("ZPFrance", game.getStacks().get(0).getProvince());
        Assert.assertEquals(1, game.getStacks().get(0).getCounters().size());
        Assert.assertEquals("france", game.getStacks().get(0).getCounters().get(0).getCountry());
        Assert.assertEquals(CounterFaceTypeEnum.TRADING_FLEET_MINUS, game.getStacks().get(0).getCounters().get(0).getType());
        Assert.assertEquals("france", game.getTradeFleets().get(0).getCountry());
        Assert.assertEquals("ZPFrance", game.getTradeFleets().get(0).getProvince());
        Assert.assertEquals(1, game.getTradeFleets().get(0).getLevel().intValue());
    }

    @Test
    public void testCreateCounterExistingTradeFleet() throws Exception {
        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(0).setCountry("france");
        game.getTradeFleets().get(0).setProvince("ZPFrance");
        game.getTradeFleets().get(0).setLevel(4);

        when(stackDao.create(anyObject())).thenAnswer(invocationOnMock -> {
            StackEntity stack = (StackEntity) invocationOnMock.getArguments()[0];

            stack.setId(2L);

            return stack;
        });

        DiffEntity diff = counterDomain.createCounter(CounterFaceTypeEnum.TRADING_FLEET_MINUS, "france", "ZPFrance", 1, game);

        InOrder inOrder = inOrder(stackDao);
        inOrder.verify(stackDao).create(anyObject());

        Assert.assertEquals(game.getId(), diff.getIdGame());
        Assert.assertEquals(game.getVersion(), diff.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.ADD, diff.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diff.getTypeObject());
        Assert.assertEquals(5, diff.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diff.getAttributes().get(0).getType());
        Assert.assertEquals("ZPFrance", diff.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diff.getAttributes().get(1).getType());
        Assert.assertEquals(CounterFaceTypeEnum.TRADING_FLEET_MINUS.name(), diff.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COUNTRY, diff.getAttributes().get(2).getType());
        Assert.assertEquals("france", diff.getAttributes().get(2).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK, diff.getAttributes().get(3).getType());
        Assert.assertEquals("2", diff.getAttributes().get(3).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.LEVEL, diff.getAttributes().get(4).getType());
        Assert.assertEquals("1", diff.getAttributes().get(4).getValue());

        Assert.assertEquals(1, game.getStacks().size());
        Assert.assertEquals("ZPFrance", game.getStacks().get(0).getProvince());
        Assert.assertEquals(1, game.getStacks().get(0).getCounters().size());
        Assert.assertEquals("france", game.getStacks().get(0).getCounters().get(0).getCountry());
        Assert.assertEquals(CounterFaceTypeEnum.TRADING_FLEET_MINUS, game.getStacks().get(0).getCounters().get(0).getType());
        Assert.assertEquals("france", game.getTradeFleets().get(0).getCountry());
        Assert.assertEquals("ZPFrance", game.getTradeFleets().get(0).getProvince());
        Assert.assertEquals(1, game.getTradeFleets().get(0).getLevel().intValue());
    }

    @Test
    public void testRemoveCounter2() throws Exception {
        abstractRemoveCounter(101L, "idf", null);
    }

    @Test
    public void testRemoveCounter3() throws Exception {
        abstractRemoveCounter(200L, "languedoc", "2");
    }

    private void abstractRemoveCounter(Long idCounter, String province, String stackDel) {
        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setId(1L);
        game.getStacks().get(0).setGame(game);
        game.getStacks().get(0).setProvince("idf");
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(100L);
        game.getStacks().get(0).getCounters().get(0).setOwner(game.getStacks().get(0));
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(1).setId(101L);
        game.getStacks().get(0).getCounters().get(1).setOwner(game.getStacks().get(0));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).setId(2L);
        game.getStacks().get(1).setGame(game);
        game.getStacks().get(1).setProvince("languedoc");
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setId(200L);
        game.getStacks().get(1).getCounters().get(0).setOwner(game.getStacks().get(1));

        CounterEntity counter = game.getStacks().stream()
                .flatMap(stack -> stack.getCounters().stream())
                .filter(c -> Objects.equals(c.getId(), idCounter))
                .findAny()
                .orElse(null);
        DiffEntity diff = counterDomain.removeCounter(counter);

        InOrder inOrder = inOrder(counterDao);
        inOrder.verify(counterDao).delete(anyObject());

        Assert.assertEquals(game.getId(), diff.getIdGame());
        Assert.assertEquals(game.getVersion(), diff.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.REMOVE, diff.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diff.getTypeObject());
        Assert.assertEquals(idCounter, diff.getIdObject());
        Assert.assertEquals(1 + (stackDel != null ? 1 : 0), diff.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diff.getAttributes().get(0).getType());
        Assert.assertEquals(province, diff.getAttributes().get(0).getValue());
        if (stackDel != null) {
            Assert.assertEquals(DiffAttributeTypeEnum.STACK_DEL, diff.getAttributes().get(1).getType());
            Assert.assertEquals(stackDel, diff.getAttributes().get(1).getValue());

            Assert.assertEquals(1, game.getStacks().size());
        } else {
            Assert.assertEquals(2, game.getStacks().size());
        }
    }

    @Test
    public void testRemoveCounter4() {
        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(0).setCountry("france");
        game.getTradeFleets().get(0).setProvince("ZPFrance");
        game.getTradeFleets().get(0).setLevel(3);
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(1).setCountry("angleterre");
        game.getTradeFleets().get(1).setProvince("ZPFrance");
        game.getTradeFleets().get(1).setLevel(5);
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(2).setCountry("france");
        game.getTradeFleets().get(2).setProvince("ZMNord");
        game.getTradeFleets().get(2).setLevel(2);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setId(1L);
        game.getStacks().get(0).setGame(game);
        game.getStacks().get(0).setProvince("idf");
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(100L);
        game.getStacks().get(0).getCounters().get(0).setOwner(game.getStacks().get(0));
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(1).setId(101L);
        game.getStacks().get(0).getCounters().get(1).setOwner(game.getStacks().get(0));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).setId(2L);
        game.getStacks().get(1).setGame(game);
        game.getStacks().get(1).setProvince("ZPFrance");
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setId(200L);
        game.getStacks().get(1).getCounters().get(0).setCountry("france");
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.TRADING_FLEET_MINUS);
        game.getStacks().get(1).getCounters().get(0).setOwner(game.getStacks().get(1));

        DiffEntity diff = counterDomain.removeCounter(game.getStacks().get(1).getCounters().get(0));

        InOrder inOrder = inOrder(counterDao);
        inOrder.verify(counterDao).delete(anyObject());

        Assert.assertEquals(game.getId(), diff.getIdGame());
        Assert.assertEquals(game.getVersion(), diff.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.REMOVE, diff.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diff.getTypeObject());
        Assert.assertEquals(200L, diff.getIdObject().longValue());
        Assert.assertEquals(2, diff.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diff.getAttributes().get(0).getType());
        Assert.assertEquals("ZPFrance", diff.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK_DEL, diff.getAttributes().get(1).getType());
        Assert.assertEquals("2", diff.getAttributes().get(1).getValue());

        Assert.assertEquals(1, game.getStacks().size());
        Assert.assertEquals(3, game.getTradeFleets().size());
        Assert.assertEquals(0, game.getTradeFleets().get(0).getLevel().intValue());
        Assert.assertEquals("angleterre", game.getTradeFleets().get(1).getCountry());
        Assert.assertEquals("ZMNord", game.getTradeFleets().get(2).getProvince());
    }

    @Test
    public void testSwitchCounter() throws Exception {
        GameEntity game = createSwitchCounterGame();
        CounterEntity counter = game.getStacks().stream()
                .flatMap(stack -> stack.getCounters().stream())
                .filter(c -> Objects.equals(c.getId(), 100L))
                .findAny()
                .orElse(null);

        DiffEntity diff = counterDomain.switchCounter(counter, CounterFaceTypeEnum.ARMY_PLUS, null, game);

        Assert.assertEquals(game.getId(), diff.getIdGame());
        Assert.assertEquals(game.getVersion(), diff.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.MODIFY, diff.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diff.getTypeObject());
        Assert.assertEquals(100L, diff.getIdObject().longValue());
        Assert.assertEquals(2, diff.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diff.getAttributes().get(0).getType());
        Assert.assertEquals(CounterFaceTypeEnum.ARMY_PLUS.name(), diff.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diff.getAttributes().get(1).getType());
        Assert.assertEquals("idf", diff.getAttributes().get(1).getValue());

        Assert.assertEquals(CounterFaceTypeEnum.ARMY_PLUS, game.getStacks().get(0).getCounters().get(0).getType());
    }

    @Test
    public void testSwitchCounterTradeFleet1() throws Exception {
        GameEntity game = createSwitchCounterGame();
        CounterEntity counter = game.getStacks().stream()
                .flatMap(stack -> stack.getCounters().stream())
                .filter(c -> Objects.equals(c.getId(), 300L))
                .findAny()
                .orElse(null);

        DiffEntity diff = counterDomain.switchCounter(counter, CounterFaceTypeEnum.TRADING_FLEET_MINUS, 3, game);

        Assert.assertEquals(game.getId(), diff.getIdGame());
        Assert.assertEquals(game.getVersion(), diff.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.MODIFY, diff.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diff.getTypeObject());
        Assert.assertEquals(300L, diff.getIdObject().longValue());
        Assert.assertEquals(3, diff.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diff.getAttributes().get(0).getType());
        Assert.assertEquals(CounterFaceTypeEnum.TRADING_FLEET_MINUS.name(), diff.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diff.getAttributes().get(1).getType());
        Assert.assertEquals("ZPFrance", diff.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.LEVEL, diff.getAttributes().get(2).getType());
        Assert.assertEquals("3", diff.getAttributes().get(2).getValue());

        Assert.assertEquals(CounterFaceTypeEnum.TRADING_FLEET_MINUS, game.getStacks().get(2).getCounters().get(0).getType());
        Assert.assertEquals(3, game.getTradeFleets().get(2).getLevel().intValue());
    }

    @Test
    public void testSwitchCounterTradeFleet2() throws Exception {
        GameEntity game = createSwitchCounterGame();
        CounterEntity counter = game.getStacks().stream()
                .flatMap(stack -> stack.getCounters().stream())
                .filter(c -> Objects.equals(c.getId(), 301L))
                .findAny()
                .orElse(null);

        DiffEntity diff = counterDomain.switchCounter(counter, CounterFaceTypeEnum.TRADING_FLEET_PLUS, 4, game);

        Assert.assertEquals(game.getId(), diff.getIdGame());
        Assert.assertEquals(game.getVersion(), diff.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.MODIFY, diff.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diff.getTypeObject());
        Assert.assertEquals(301L, diff.getIdObject().longValue());
        Assert.assertEquals(3, diff.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diff.getAttributes().get(0).getType());
        Assert.assertEquals(CounterFaceTypeEnum.TRADING_FLEET_PLUS.name(), diff.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diff.getAttributes().get(1).getType());
        Assert.assertEquals("ZPFrance", diff.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.LEVEL, diff.getAttributes().get(2).getType());
        Assert.assertEquals("4", diff.getAttributes().get(2).getValue());

        Assert.assertEquals(CounterFaceTypeEnum.TRADING_FLEET_PLUS, game.getStacks().get(2).getCounters().get(1).getType());
        Assert.assertEquals(4, game.getTradeFleets().get(1).getLevel().intValue());
    }

    @Test
    public void testSwitchCounterEstablishment1() throws Exception {
        GameEntity game = createSwitchCounterGame();
        CounterEntity counter = game.getStacks().stream()
                .flatMap(stack -> stack.getCounters().stream())
                .filter(c -> Objects.equals(c.getId(), 400L))
                .findAny()
                .orElse(null);

        RotwProvinceEntity prov = new RotwProvinceEntity();
        prov.setRegion("regionTest");

        when(provinceDao.getProvinceByName("rAzteca~C")).thenReturn(prov);

        DiffEntity diff = counterDomain.switchCounter(counter, CounterFaceTypeEnum.TRADING_POST_PLUS, 6, game);

        InOrder inOrder = inOrder(provinceDao);
        inOrder.verify(provinceDao).getProvinceByName("rAzteca~C");

        Assert.assertEquals(game.getId(), diff.getIdGame());
        Assert.assertEquals(game.getVersion(), diff.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.MODIFY, diff.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diff.getTypeObject());
        Assert.assertEquals(400L, diff.getIdObject().longValue());
        Assert.assertEquals(3, diff.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diff.getAttributes().get(0).getType());
        Assert.assertEquals(CounterFaceTypeEnum.TRADING_POST_PLUS.name(), diff.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diff.getAttributes().get(1).getType());
        Assert.assertEquals("rAzteca~C", diff.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.LEVEL, diff.getAttributes().get(2).getType());
        Assert.assertEquals("6", diff.getAttributes().get(2).getValue());

        Assert.assertEquals(CounterFaceTypeEnum.TRADING_POST_PLUS, game.getStacks().get(3).getCounters().get(0).getType());
        Assert.assertEquals(6, game.getStacks().get(3).getCounters().get(0).getEstablishment().getLevel().intValue());
        Assert.assertEquals("regionTest", game.getStacks().get(3).getCounters().get(0).getEstablishment().getRegion());
        Assert.assertEquals(EstablishmentTypeEnum.TRADING_POST, game.getStacks().get(3).getCounters().get(0).getEstablishment().getType());
    }

    @Test
    public void testSwitchCounterEstablishment2() throws Exception {
        GameEntity game = createSwitchCounterGame();
        CounterEntity counter = game.getStacks().stream()
                .flatMap(stack -> stack.getCounters().stream())
                .filter(c -> Objects.equals(c.getId(), 401L))
                .findAny()
                .orElse(null);

        DiffEntity diff = counterDomain.switchCounter(counter, CounterFaceTypeEnum.COLONY_MINUS, 1, game);

        Assert.assertEquals(game.getId(), diff.getIdGame());
        Assert.assertEquals(game.getVersion(), diff.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.MODIFY, diff.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diff.getTypeObject());
        Assert.assertEquals(401L, diff.getIdObject().longValue());
        Assert.assertEquals(3, diff.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diff.getAttributes().get(0).getType());
        Assert.assertEquals(CounterFaceTypeEnum.COLONY_MINUS.name(), diff.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diff.getAttributes().get(1).getType());
        Assert.assertEquals("rAzteca~C", diff.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.LEVEL, diff.getAttributes().get(2).getType());
        Assert.assertEquals("1", diff.getAttributes().get(2).getValue());

        Assert.assertEquals(CounterFaceTypeEnum.COLONY_MINUS, game.getStacks().get(3).getCounters().get(1).getType());
        Assert.assertEquals(1, game.getStacks().get(3).getCounters().get(1).getEstablishment().getLevel().intValue());
    }

    private GameEntity createSwitchCounterGame() {
        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(0).setId(1L);
        game.getTradeFleets().get(0).setProvince("ZPFrance");
        game.getTradeFleets().get(0).setCountry("france");
        game.getTradeFleets().get(0).setLevel(5);
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(1).setId(2L);
        game.getTradeFleets().get(1).setProvince("ZPFrance");
        game.getTradeFleets().get(1).setCountry("angleterre");
        game.getTradeFleets().get(1).setLevel(2);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setId(1L);
        game.getStacks().get(0).setProvince("idf");
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(100L);
        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.ARMY_MINUS);
        game.getStacks().get(0).getCounters().get(0).setOwner(game.getStacks().get(0));
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(1).setId(101L);
        game.getStacks().get(0).getCounters().get(1).setOwner(game.getStacks().get(0));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).setId(2L);
        game.getStacks().get(1).setProvince("languedoc");
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setId(200L);
        game.getStacks().get(1).getCounters().get(0).setOwner(game.getStacks().get(1));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(2).setId(3L);
        game.getStacks().get(2).setProvince("ZPFrance");
        game.getStacks().get(2).getCounters().add(new CounterEntity());
        game.getStacks().get(2).getCounters().get(0).setId(300L);
        game.getStacks().get(2).getCounters().get(0).setType(CounterFaceTypeEnum.TRADING_FLEET_PLUS);
        game.getStacks().get(2).getCounters().get(0).setCountry("espagne");
        game.getStacks().get(2).getCounters().get(0).setOwner(game.getStacks().get(2));
        game.getStacks().get(2).getCounters().add(new CounterEntity());
        game.getStacks().get(2).getCounters().get(1).setId(301L);
        game.getStacks().get(2).getCounters().get(1).setType(CounterFaceTypeEnum.TRADING_FLEET_MINUS);
        game.getStacks().get(2).getCounters().get(1).setCountry("angleterre");
        game.getStacks().get(2).getCounters().get(1).setOwner(game.getStacks().get(2));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(3).setId(4L);
        game.getStacks().get(3).setProvince("rAzteca~C");
        game.getStacks().get(3).getCounters().add(new CounterEntity());
        game.getStacks().get(3).getCounters().get(0).setId(400L);
        game.getStacks().get(3).getCounters().get(0).setType(CounterFaceTypeEnum.TRADING_POST_MINUS);
        game.getStacks().get(3).getCounters().get(0).setCountry("france");
        game.getStacks().get(3).getCounters().get(0).setOwner(game.getStacks().get(3));
        game.getStacks().get(3).getCounters().add(new CounterEntity());
        game.getStacks().get(3).getCounters().get(1).setId(401L);
        game.getStacks().get(3).getCounters().get(1).setType(CounterFaceTypeEnum.COLONY_PLUS);
        game.getStacks().get(3).getCounters().get(1).setEstablishment(new EstablishmentEntity());
        game.getStacks().get(3).getCounters().get(1).getEstablishment().setLevel(5);
        game.getStacks().get(3).getCounters().get(1).setCountry("espagne");
        game.getStacks().get(3).getCounters().get(1).setOwner(game.getStacks().get(3));

        return game;
    }

    @Test
    public void testchangeVeteransCounter() throws Exception {
        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setId(1L);
        game.getStacks().get(0).setProvince("idf");
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(100L);
        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.ARMY_MINUS);
        game.getStacks().get(0).getCounters().get(0).setOwner(game.getStacks().get(0));
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(1).setId(101L);
        game.getStacks().get(0).getCounters().get(1).setOwner(game.getStacks().get(0));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).setId(2L);
        game.getStacks().get(1).setProvince("languedoc");
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setId(200L);
        game.getStacks().get(1).getCounters().get(0).setOwner(game.getStacks().get(1));

        DiffEntity diff = counterDomain.changeVeteransCounter(99L, 2d, game);

        Assert.assertNull(diff);

        diff = counterDomain.changeVeteransCounter(101L, 2d, game);

        Assert.assertEquals(game.getId(), diff.getIdGame());
        Assert.assertEquals(game.getVersion(), diff.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.MODIFY, diff.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diff.getTypeObject());
        Assert.assertEquals(101L, diff.getIdObject().longValue());
        Assert.assertEquals(2, diff.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.VETERANS, diff.getAttributes().get(0).getType());
        Assert.assertEquals(Double.toString(2d), diff.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diff.getAttributes().get(1).getType());
        Assert.assertEquals("idf", diff.getAttributes().get(1).getValue());

        Assert.assertEquals(2, game.getStacks().get(0).getCounters().get(1).getVeterans().doubleValue(), EPSILON);
    }

    @Test
    public void testMoveSpecialCounter() throws Exception {
        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setId(1L);
        game.getStacks().get(0).setProvince("B_1");
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(7L);
        game.getStacks().get(0).getCounters().get(0).setCountry("france");
        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.STABILITY);
        game.getStacks().get(0).getCounters().get(0).setOwner(game.getStacks().get(0));

        when(stackDao.create(anyObject())).thenAnswer(invocationOnMock -> {
            StackEntity stack = (StackEntity) invocationOnMock.getArguments()[0];

            stack.setId(2L);

            return stack;
        });

        DiffEntity diff = counterDomain.moveSpecialCounter(CounterFaceTypeEnum.STABILITY, "espagne", "B_2", game);

        Assert.assertNull(diff);

        diff = counterDomain.moveSpecialCounter(CounterFaceTypeEnum.STABILITY, "france", "B_2", game);

        InOrder inOrder = inOrder(stackDao);
        inOrder.verify(stackDao).create(anyObject());

        Assert.assertEquals(game.getId(), diff.getIdGame());
        Assert.assertEquals(game.getVersion(), diff.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.MOVE, diff.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diff.getTypeObject());
        Assert.assertEquals(7L, diff.getIdObject().longValue());
        Assert.assertEquals(5, diff.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK_FROM, diff.getAttributes().get(0).getType());
        Assert.assertEquals("1", diff.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK_TO, diff.getAttributes().get(1).getType());
        Assert.assertEquals("2", diff.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE_FROM, diff.getAttributes().get(2).getType());
        Assert.assertEquals("B_1", diff.getAttributes().get(2).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE_TO, diff.getAttributes().get(3).getType());
        Assert.assertEquals("B_2", diff.getAttributes().get(3).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK_DEL, diff.getAttributes().get(4).getType());
        Assert.assertEquals("1", diff.getAttributes().get(4).getValue());

        Assert.assertEquals(1, game.getStacks().size());
        Assert.assertEquals(2L, game.getStacks().get(0).getId().longValue());
        Assert.assertEquals("B_2", game.getStacks().get(0).getProvince());
        Assert.assertEquals(1, game.getStacks().get(0).getCounters().size());
        Assert.assertEquals(7L, game.getStacks().get(0).getCounters().get(0).getId().longValue());
        Assert.assertEquals("france", game.getStacks().get(0).getCounters().get(0).getCountry());
        Assert.assertEquals(CounterFaceTypeEnum.STABILITY, game.getStacks().get(0).getCounters().get(0).getType());
    }

    @Test
    public void testMoveSpecialCounter2() throws Exception {
        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setId(1L);
        game.getStacks().get(0).setProvince("B_1");
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(7L);
        game.getStacks().get(0).getCounters().get(0).setCountry("france");
        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_LAND);
        game.getStacks().get(0).getCounters().get(0).setOwner(game.getStacks().get(0));
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(1).setId(8L);
        game.getStacks().get(0).getCounters().get(1).setType(CounterFaceTypeEnum.TECH_LAND_LATIN);
        game.getStacks().get(0).getCounters().get(1).setOwner(game.getStacks().get(0));

        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).setId(2L);
        game.getStacks().get(1).setProvince("B_2");
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setId(9L);
        game.getStacks().get(1).getCounters().get(0).setCountry("espagne");
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_NAVAL);
        game.getStacks().get(1).getCounters().get(0).setOwner(game.getStacks().get(1));

        DiffEntity diff = counterDomain.moveSpecialCounter(CounterFaceTypeEnum.TECH_LAND_LATIN, null, "B_2", game);

        Assert.assertEquals(game.getId(), diff.getIdGame());
        Assert.assertEquals(game.getVersion(), diff.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.MOVE, diff.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diff.getTypeObject());
        Assert.assertEquals(8L, diff.getIdObject().longValue());
        Assert.assertEquals(4, diff.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK_FROM, diff.getAttributes().get(0).getType());
        Assert.assertEquals("1", diff.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK_TO, diff.getAttributes().get(1).getType());
        Assert.assertEquals("2", diff.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE_FROM, diff.getAttributes().get(2).getType());
        Assert.assertEquals("B_1", diff.getAttributes().get(2).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE_TO, diff.getAttributes().get(3).getType());
        Assert.assertEquals("B_2", diff.getAttributes().get(3).getValue());

        Assert.assertEquals(2, game.getStacks().size());
        Assert.assertEquals(1L, game.getStacks().get(0).getId().longValue());
        Assert.assertEquals("B_1", game.getStacks().get(0).getProvince());
        Assert.assertEquals(1, game.getStacks().get(0).getCounters().size());
        Assert.assertEquals(7L, game.getStacks().get(0).getCounters().get(0).getId().longValue());
        Assert.assertEquals("france", game.getStacks().get(0).getCounters().get(0).getCountry());
        Assert.assertEquals(CounterFaceTypeEnum.TECH_LAND, game.getStacks().get(0).getCounters().get(0).getType());

        Assert.assertEquals(2L, game.getStacks().get(1).getId().longValue());
        Assert.assertEquals("B_2", game.getStacks().get(1).getProvince());
        Assert.assertEquals(2, game.getStacks().get(1).getCounters().size());
        Assert.assertEquals(9L, game.getStacks().get(1).getCounters().get(0).getId().longValue());
        Assert.assertEquals("espagne", game.getStacks().get(1).getCounters().get(0).getCountry());
        Assert.assertEquals(CounterFaceTypeEnum.TECH_NAVAL, game.getStacks().get(1).getCounters().get(0).getType());
        Assert.assertEquals(8L, game.getStacks().get(1).getCounters().get(1).getId().longValue());
        Assert.assertEquals(null, game.getStacks().get(1).getCounters().get(1).getCountry());
        Assert.assertEquals(CounterFaceTypeEnum.TECH_LAND_LATIN, game.getStacks().get(1).getCounters().get(1).getType());
    }

    @Test
    public void testIncreaseInflation() {
        GameEntity game = new GameEntity();
        StackEntity stack = new StackEntity();
        stack.setId(5L);
        stack.setProvince("B_PB_2D");
        CounterEntity counter = AbstractGameServiceTest.createCounter(1L, null, CounterFaceTypeEnum.INFLATION, stack);
        stack.getCounters().add(counter);
        game.getStacks().add(stack);

        when(oeUtil.getInflationBox(game)).thenReturn(stack.getProvince());
        when(stackDao.create(anyObject())).thenAnswer(invocationOnMock -> {
            StackEntity stackCreate = (StackEntity) invocationOnMock.getArguments()[0];

            stackCreate.setId(2L);

            return stackCreate;
        });

        Optional<DiffEntity> diffOpt = counterDomain.increaseInflation(game);

        Assert.assertTrue(diffOpt.isPresent());
        Assert.assertEquals("B_PB_3G", counter.getOwner().getProvince());
        DiffEntity diff = diffOpt.get();
        Assert.assertEquals(DiffTypeEnum.MOVE, diff.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diff.getTypeObject());
        Assert.assertEquals(counter.getId(), diff.getIdObject());
        Assert.assertEquals("B_PB_3G", AbstractGameServiceTest.getAttribute(diff, DiffAttributeTypeEnum.PROVINCE_TO));
    }

    @Test
    public void testIncreaseInflationOrNot() {
        GameEntity game = new GameEntity();
        StackEntity stack = new StackEntity();
        stack.setId(5L);
        stack.setProvince("B_PB_4D");
        CounterEntity counter = AbstractGameServiceTest.createCounter(1L, null, CounterFaceTypeEnum.INFLATION, stack);
        stack.getCounters().add(counter);
        game.getStacks().add(stack);

        when(oeUtil.getInflationBox(game)).thenReturn(stack.getProvince());
        when(stackDao.create(anyObject())).thenAnswer(invocationOnMock -> {
            StackEntity stackCreate = (StackEntity) invocationOnMock.getArguments()[0];

            stackCreate.setId(2L);

            return stackCreate;
        });

        Optional<DiffEntity> diffOpt = counterDomain.increaseInflation(game);

        Assert.assertFalse(diffOpt.isPresent());
        Assert.assertEquals("B_PB_4D", counter.getOwner().getProvince());
    }

    @Test
    public void testDecreaseInflation() {
        GameEntity game = new GameEntity();
        StackEntity stack = new StackEntity();
        stack.setId(5L);
        stack.setProvince("B_PB_2D");
        CounterEntity counter = AbstractGameServiceTest.createCounter(1L, null, CounterFaceTypeEnum.INFLATION, stack);
        stack.getCounters().add(counter);
        game.getStacks().add(stack);

        when(oeUtil.getInflationBox(game)).thenReturn(stack.getProvince());
        when(stackDao.create(anyObject())).thenAnswer(invocationOnMock -> {
            StackEntity stackCreate = (StackEntity) invocationOnMock.getArguments()[0];

            stackCreate.setId(2L);

            return stackCreate;
        });

        Optional<DiffEntity> diffOpt = counterDomain.decreaseInflation(game);

        Assert.assertTrue(diffOpt.isPresent());
        Assert.assertEquals("B_PB_2G", counter.getOwner().getProvince());
        DiffEntity diff = diffOpt.get();
        Assert.assertEquals(DiffTypeEnum.MOVE, diff.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diff.getTypeObject());
        Assert.assertEquals(counter.getId(), diff.getIdObject());
        Assert.assertEquals("B_PB_2G", AbstractGameServiceTest.getAttribute(diff, DiffAttributeTypeEnum.PROVINCE_TO));
    }

    @Test
    public void testDecreaseInflationOrNot() {
        GameEntity game = new GameEntity();
        StackEntity stack = new StackEntity();
        stack.setId(5L);
        stack.setProvince("B_PB_0G");
        CounterEntity counter = AbstractGameServiceTest.createCounter(1L, null, CounterFaceTypeEnum.INFLATION, stack);
        stack.getCounters().add(counter);
        game.getStacks().add(stack);

        when(oeUtil.getInflationBox(game)).thenReturn(stack.getProvince());
        when(stackDao.create(anyObject())).thenAnswer(invocationOnMock -> {
            StackEntity stackCreate = (StackEntity) invocationOnMock.getArguments()[0];

            stackCreate.setId(2L);

            return stackCreate;
        });

        Optional<DiffEntity> diffOpt = counterDomain.decreaseInflation(game);

        Assert.assertFalse(diffOpt.isPresent());
        Assert.assertEquals("B_PB_0G", counter.getOwner().getProvince());
    }
}
