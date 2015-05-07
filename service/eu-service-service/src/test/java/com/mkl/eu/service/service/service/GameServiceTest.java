package com.mkl.eu.service.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeObjectEnum;
import com.mkl.eu.service.service.mapping.diff.DiffMapping;
import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.board.IStackDao;
import com.mkl.eu.service.service.persistence.diff.IDiffDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.BorderEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.EuropeanProvinceEntity;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.service.impl.GameServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

/**
 * Test of GameService.
 *
 * @author MKL.
 */
@RunWith(MockitoJUnitRunner.class)
public class GameServiceTest {
    @InjectMocks
    private GameServiceImpl gameService;

    @Mock
    private IGameDao gameDao;

    @Mock
    private IProvinceDao provinceDao;

    @Mock
    private ICounterDao counterDao;

    @Mock
    private IStackDao stackDao;

    @Mock
    private IDiffDao diffDao;

    @Mock
    private DiffMapping diffMapping;

    /** Variable used to store something coming from a mock. */
    private DiffEntity diffEntity;

    @Test
    public void testUpdateGame() throws Exception {
        Long idGame = 12L;
        Long versionGame = 1L;

        List<DiffEntity> diffs = new ArrayList<>();
        DiffEntity diff1 = new DiffEntity();
        diff1.setVersionGame(1L);
        diffs.add(diff1);
        DiffEntity diff2 = new DiffEntity();
        diff2.setVersionGame(2L);
        diffs.add(diff2);
        DiffEntity diff3 = new DiffEntity();
        diff3.setVersionGame(3L);
        diffs.add(diff3);
        DiffEntity diff4 = new DiffEntity();
        diff4.setVersionGame(4L);
        diffs.add(diff4);
        DiffEntity diff5 = new DiffEntity();
        diff5.setVersionGame(5L);
        diffs.add(diff5);

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffs);

        List<Diff> diffVos = new ArrayList<>();
        diffVos.add(new Diff());
        diffVos.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenReturn(diffVos);

        DiffResponse response = gameService.updateGame(idGame, versionGame);

        InOrder inOrder = inOrder(diffDao, diffMapping);

        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(5L, response.getVersionGame().longValue());
        Assert.assertEquals(diffVos, response.getDiffs());

        diff2.setVersionGame(7L);
        diff4.setVersionGame(7L);

        response = gameService.updateGame(idGame, versionGame);

        inOrder = inOrder(diffDao, diffMapping);

        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(7L, response.getVersionGame().longValue());
        Assert.assertEquals(diffVos, response.getDiffs());

        response = gameService.updateGame(1L, 12L);

        Assert.assertEquals(12L, response.getVersionGame().longValue());
    }

    @Test
    public void testMoveStackFailSimple() {
        Long idGame = 12L;
        Long versionGame = 1L;
        Long idStack = 4L;
        String provinceTo = "IdF";

        try {
            gameService.moveStack(null, null, null, null);
            Assert.fail("Should break because idGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("idGame", e.getParams()[0]);
        }

        try {
            gameService.moveStack(idGame, null, null, null);
            Assert.fail("Should break because versionGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("versionGame", e.getParams()[0]);
        }

        try {
            gameService.moveStack(idGame, versionGame, null, null);
            Assert.fail("Should break because idStack is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("idStack", e.getParams()[0]);
        }

        try {
            gameService.moveStack(idGame, versionGame, idStack, null);
            Assert.fail("Should break because provinceTo is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("provinceTo", e.getParams()[0]);
        }

        try {
            gameService.moveStack(idGame, versionGame, idStack, provinceTo);
            Assert.fail("Should break because game does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("idGame", e.getParams()[0]);
        }

        GameEntity game = new GameEntity();
        game.setId(12L);

        when(gameDao.lock(12L)).thenReturn(game);

        try {
            gameService.moveStack(idGame, versionGame, idStack, provinceTo);
            Assert.fail("Should break because versions does not match");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("versionGame", e.getParams()[0]);
        }
    }

    @Test
    public void testMoveStackFailComplex() {
        Long idGame = 12L;
        Long versionGame = 1L;
        Long idStack = 13L;
        String provinceTo = "IdF";

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setId(257L);
        idf.setName("IdF");

        EuropeanProvinceEntity pecs = new EuropeanProvinceEntity();
        pecs.setId(256L);
        pecs.setName("pecs");

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);

        when(gameDao.lock(12L)).thenReturn(game);

        try {
            gameService.moveStack(idGame, versionGame, idStack, provinceTo);
            Assert.fail("Should break because stack does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("idStack", e.getParams()[0]);
        }

        StackEntity stack = new StackEntity();
        stack.setProvince("pecs");
        stack.setId(14L);
        game.getStacks().add(stack);

        try {
            gameService.moveStack(idGame, versionGame, idStack, provinceTo);
            Assert.fail("Should break because stack does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("idStack", e.getParams()[0]);
        }

        stack.setId(13L);

        try {
            gameService.moveStack(idGame, versionGame, idStack, provinceTo);
            Assert.fail("Should break because province does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("provinceTo", e.getParams()[0]);
        }

        when(provinceDao.getProvinceByName("pecs")).thenReturn(pecs);
        when(provinceDao.getProvinceByName("IdF")).thenReturn(idf);

        try {
            gameService.moveStack(idGame, versionGame, idStack, provinceTo);
            Assert.fail("Should break because province is not close to former one");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("provinceTo", e.getParams()[0]);
        }
    }

    @Test
    public void testMoveStackSuccess() throws Exception {
        Long idGame = 12L;
        Long versionGame = 1L;
        Long idStack = 13L;
        String provinceTo = "IdF";

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setId(257L);
        idf.setName("IdF");

        EuropeanProvinceEntity pecs = new EuropeanProvinceEntity();
        pecs.setId(256L);
        pecs.setName("pecs");

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);
        StackEntity stack = new StackEntity();
        stack.setProvince("pecs");
        stack.setId(13L);
        BorderEntity border = new BorderEntity();
        border.setProvinceFrom(pecs);
        border.setProvinceTo(idf);
        pecs.getBorders().add(border);
        game.getStacks().add(stack);

        when(gameDao.lock(12L)).thenReturn(game);

        when(provinceDao.getProvinceByName("pecs")).thenReturn(pecs);
        when(provinceDao.getProvinceByName("IdF")).thenReturn(idf);

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        when(diffDao.create(anyObject())).thenAnswer(invocation -> {
            diffEntity = (DiffEntity) invocation.getArguments()[0];
            return diffEntity;
        });

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenReturn(diffAfter);

        DiffResponse response = gameService.moveStack(idGame, versionGame, idStack, provinceTo);

        InOrder inOrder = inOrder(gameDao, provinceDao, diffDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(provinceDao).getProvinceByName("IdF");
        inOrder.verify(provinceDao).getProvinceByName("pecs");
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(DiffTypeEnum.MOVE, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.STACK, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(2, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE_FROM, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(pecs.getName(), diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE_TO, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals(idf.getName(), diffEntity.getAttributes().get(1).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
    }

    @Test
    public void testMoveCounterFailSimple() {
        Long idGame = 12L;
        Long versionGame = 1L;
        Long idCounter = 4L;
        Long idStack = 8L;

        try {
            gameService.moveCounter(null, null, null, null);
            Assert.fail("Should break because idGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("idGame", e.getParams()[0]);
        }

        try {
            gameService.moveCounter(idGame, null, null, null);
            Assert.fail("Should break because versionGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("versionGame", e.getParams()[0]);
        }

        try {
            gameService.moveCounter(idGame, versionGame, null, null);
            Assert.fail("Should break because idCounter is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("idCounter", e.getParams()[0]);
        }

        try {
            gameService.moveCounter(idGame, versionGame, idCounter, idStack);
            Assert.fail("Should break because game does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("idGame", e.getParams()[0]);
        }

        GameEntity game = new GameEntity();
        game.setId(12L);

        when(gameDao.lock(12L)).thenReturn(game);

        try {
            gameService.moveCounter(idGame, versionGame, idCounter, idStack);
            Assert.fail("Should break because versions does not match");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("versionGame", e.getParams()[0]);
        }
    }

    @Test
    public void testMoveCounterFailComplex() {
        Long idGame = 12L;
        Long versionGame = 1L;
        Long idCounter = 13L;
        Long idStack = 8L;

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setId(257L);
        idf.setName("IdF");

        EuropeanProvinceEntity pecs = new EuropeanProvinceEntity();
        pecs.setId(256L);
        pecs.setName("pecs");

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);

        StackEntity stack = new StackEntity();
        stack.setProvince("IdF");
        stack.setId(8L);
        game.getStacks().add(stack);

        stack = new StackEntity();
        stack.setProvince("Pecs");
        stack.setId(9L);
        CounterEntity counter = new CounterEntity();
        counter.setId(13L);
        counter.setOwner(stack);
        game.getStacks().add(stack);

        when(gameDao.lock(12L)).thenReturn(game);

        try {
            gameService.moveCounter(idGame, versionGame, idCounter, idStack);
            Assert.fail("Should break because counter does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("idCounter", e.getParams()[0]);
        }

        when(counterDao.getCounter(13L, 12L)).thenReturn(counter);

        try {
            gameService.moveCounter(idGame, versionGame, idCounter, idStack);
            Assert.fail("Should break because trying to move the counter in another province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("idStack", e.getParams()[0]);
        }

        try {
            gameService.moveCounter(idGame, versionGame, idCounter, 9L);
            Assert.fail("Should break because trying to move the counter in the same stack");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("idStack", e.getParams()[0]);
        }
    }

    @Test
    public void testMoveCounterSuccess() throws Exception {
        Long idGame = 12L;
        Long versionGame = 1L;
        Long idCounter = 13L;
        Long idStack = 8L;

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);

        StackEntity stack = new StackEntity();
        stack.setProvince("IdF");
        stack.setId(8L);
        game.getStacks().add(stack);

        stack = new StackEntity();
        stack.setProvince("IdF");
        stack.setId(9L);
        CounterEntity counter = new CounterEntity();
        counter.setId(13L);
        counter.setOwner(stack);
        stack.getCounters().add(counter);
        stack.getCounters().add(new CounterEntity());
        game.getStacks().add(stack);

        when(gameDao.lock(12L)).thenReturn(game);

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        when(counterDao.getCounter(13L, 12L)).thenReturn(counter);

        when(diffDao.create(anyObject())).thenAnswer(invocation -> {
            diffEntity = (DiffEntity) invocation.getArguments()[0];
            return diffEntity;
        });

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenReturn(diffAfter);

        DiffResponse response = gameService.moveCounter(idGame, versionGame, idCounter, idStack);

        InOrder inOrder = inOrder(gameDao, diffDao, counterDao, stackDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(counterDao).getCounter(13L, 12L);
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(gameDao).update(game, false);
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(DiffTypeEnum.MOVE, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(2, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK_FROM, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals("9", diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK_TO, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals("8", diffEntity.getAttributes().get(1).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
    }

    @Test
    public void testMoveCounterInNewStackSuccess() throws Exception {
        Long idGame = 12L;
        Long versionGame = 1L;
        Long idCounter = 13L;
        Long idStack = null;

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);

        StackEntity stack = new StackEntity();
        stack.setProvince("IdF");
        stack.setId(9L);
        CounterEntity counter = new CounterEntity();
        counter.setId(13L);
        counter.setOwner(stack);
        stack.getCounters().add(counter);
        game.getStacks().add(stack);

        when(gameDao.lock(12L)).thenReturn(game);

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        when(counterDao.getCounter(13L, 12L)).thenReturn(counter);

        ArgumentCaptor<StackEntity> arg = ArgumentCaptor.forClass(StackEntity.class);
        when(stackDao.create(arg.capture())).thenAnswer(invocationOnMock -> {
            StackEntity entity = (StackEntity) invocationOnMock.getArguments()[0];
            entity.setId(25L);
            return entity;
        });

        when(diffDao.create(anyObject())).thenAnswer(invocation -> {
            diffEntity = (DiffEntity) invocation.getArguments()[0];
            return diffEntity;
        });

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenReturn(diffAfter);

        DiffResponse response = gameService.moveCounter(idGame, versionGame, idCounter, idStack);

        InOrder inOrder = inOrder(gameDao, diffDao, counterDao, stackDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(counterDao).getCounter(13L, 12L);
        inOrder.verify(stackDao).create(arg.capture());
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(stackDao).delete(stack);
        inOrder.verify(gameDao).update(game, false);
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(DiffTypeEnum.MOVE, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(3, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK_FROM, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals("9", diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK_TO, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals("25", diffEntity.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK_DEL, diffEntity.getAttributes().get(2).getType());
        Assert.assertEquals("9", diffEntity.getAttributes().get(2).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
    }
}
