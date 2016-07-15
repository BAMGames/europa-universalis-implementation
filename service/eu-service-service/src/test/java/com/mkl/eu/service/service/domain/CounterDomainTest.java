package com.mkl.eu.service.service.domain;

import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeObjectEnum;
import com.mkl.eu.service.service.domain.impl.CounterDomainImpl;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.board.IStackDao;
import com.mkl.eu.service.service.persistence.diff.IDiffDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

/**
 * Test of BoardService.
 *
 * @author MKL.
 */
@RunWith(MockitoJUnitRunner.class)
public class CounterDomainTest {
    @InjectMocks
    private CounterDomainImpl counterDomain;

    @Mock
    private ICounterDao counterDao;

    @Mock
    private IStackDao stackDao;

    @Mock
    private IDiffDao diffDao;

    @Test
    public void testCreateCounter() throws Exception {
        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);

        when(stackDao.create(anyObject())).thenAnswer(invocationOnMock -> {
            StackEntity stack = (StackEntity) invocationOnMock.getArguments()[0];

            stack.getCounters().get(0).setId(7L);
            stack.setId(2L);

            return stack;
        });

        DiffEntity diff = counterDomain.createCounter(CounterFaceTypeEnum.ARMY_MINUS, "france", "idf", game);

        InOrder inOrder = inOrder(stackDao, diffDao);
        inOrder.verify(stackDao).create(anyObject());
        inOrder.verify(diffDao).create(anyObject());

        Assert.assertEquals(game.getId(), diff.getIdGame());
        Assert.assertEquals(game.getVersion(), diff.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.ADD, diff.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diff.getTypeObject());
        Assert.assertEquals(7L, diff.getIdObject().longValue());
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
        Assert.assertEquals(2L, game.getStacks().get(0).getId().longValue());
        Assert.assertEquals("idf", game.getStacks().get(0).getProvince());
        Assert.assertEquals(1, game.getStacks().get(0).getCounters().size());
        Assert.assertEquals(7L, game.getStacks().get(0).getCounters().get(0).getId().longValue());
        Assert.assertEquals("france", game.getStacks().get(0).getCounters().get(0).getCountry());
        Assert.assertEquals(CounterFaceTypeEnum.ARMY_MINUS, game.getStacks().get(0).getCounters().get(0).getType());
    }

    @Test
    public void testRemoveCounter1() throws Exception {
        DiffEntity diff = counterDomain.removeCounter(99L, new GameEntity());

        Assert.assertNull(diff);
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
        game.getStacks().get(0).setProvince("idf");
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(100L);
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

        DiffEntity diff = counterDomain.removeCounter(idCounter, game);

        InOrder inOrder = inOrder(counterDao, diffDao);
        inOrder.verify(counterDao).delete(anyObject());
        inOrder.verify(diffDao).create(anyObject());

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
    public void testSwitchCounter() throws Exception {
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

        DiffEntity diff = counterDomain.switchCounter(99L, CounterFaceTypeEnum.ARMY_PLUS, game);

        Assert.assertNull(diff);

        diff = counterDomain.switchCounter(100L, CounterFaceTypeEnum.ARMY_PLUS, game);

        InOrder inOrder = inOrder(diffDao);
        inOrder.verify(diffDao).create(anyObject());

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

        DiffEntity diff = counterDomain.changeVeteransCounter(99L, 2, game);

        Assert.assertNull(diff);

        diff = counterDomain.changeVeteransCounter(101L, 2, game);

        InOrder inOrder = inOrder(diffDao);
        inOrder.verify(diffDao).create(anyObject());

        Assert.assertEquals(game.getId(), diff.getIdGame());
        Assert.assertEquals(game.getVersion(), diff.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.MODIFY, diff.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diff.getTypeObject());
        Assert.assertEquals(101L, diff.getIdObject().longValue());
        Assert.assertEquals(2, diff.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.VETERANS, diff.getAttributes().get(0).getType());
        Assert.assertEquals("2", diff.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diff.getAttributes().get(1).getType());
        Assert.assertEquals("idf", diff.getAttributes().get(1).getValue());

        Assert.assertEquals(2, game.getStacks().get(0).getCounters().get(1).getVeterans().intValue());
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

        InOrder inOrder = inOrder(stackDao, diffDao);
        inOrder.verify(stackDao).create(anyObject());
        inOrder.verify(diffDao).create(anyObject());

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

        InOrder inOrder = inOrder(diffDao);
        inOrder.verify(diffDao).create(anyObject());

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
}
