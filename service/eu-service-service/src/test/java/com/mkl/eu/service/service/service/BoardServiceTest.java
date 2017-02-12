package com.mkl.eu.service.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.vo.AuthentInfo;
import com.mkl.eu.client.common.vo.GameInfo;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.board.EndMoveStackRequest;
import com.mkl.eu.client.service.service.board.MoveCounterRequest;
import com.mkl.eu.client.service.service.board.MoveStackRequest;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeObjectEnum;
import com.mkl.eu.client.service.vo.enumeration.MovePhaseEnum;
import com.mkl.eu.service.service.mapping.GameMapping;
import com.mkl.eu.service.service.mapping.chat.ChatMapping;
import com.mkl.eu.service.service.mapping.diff.DiffMapping;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.board.IStackDao;
import com.mkl.eu.service.service.persistence.chat.IChatDao;
import com.mkl.eu.service.service.persistence.diff.IDiffDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.BorderEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.EuropeanProvinceEntity;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.service.impl.BoardServiceImpl;
import com.mkl.eu.service.service.socket.SocketHandler;
import com.mkl.eu.service.service.util.IOEUtil;
import org.apache.commons.lang3.tuple.Pair;
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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

/**
 * Test of BoardService.
 *
 * @author MKL.
 */
@RunWith(MockitoJUnitRunner.class)
public class BoardServiceTest extends AbstractGameServiceTest {
    @InjectMocks
    private BoardServiceImpl boardService;

    @Mock
    private IProvinceDao provinceDao;

    @Mock
    private ICounterDao counterDao;

    @Mock
    private IStackDao stackDao;

    @Mock
    private IChatDao chatDao;

    @Mock
    private IDiffDao diffDao;

    @Mock
    private GameMapping gameMapping;

    @Mock
    private ChatMapping chatMapping;

    @Mock
    private DiffMapping diffMapping;

    @Mock
    private IOEUtil oeUtil;

    @Mock
    private SocketHandler socketHandler;

    /** Variable used to store something coming from a mock. */
    private DiffEntity diffEntity;

    @Test
    public void testMoveStackFailSimple() {
        Pair<Request<MoveStackRequest>, GameEntity> pair = testCheckGame(boardService::moveStack, "moveStack");
        Request<MoveStackRequest> request = pair.getLeft();

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because moveStack.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("moveStack.request", e.getParams()[0]);
        }

        request.setRequest(new MoveStackRequest());

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because idStack is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("moveStack.request.idStack", e.getParams()[0]);
        }

        request.getRequest().setIdStack(4L);

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because provinceTo is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("moveStack.request.provinceTo", e.getParams()[0]);
        }

        request.getRequest().setProvinceTo("IdF");
    }

    @Test
    public void testMoveStackFailComplex() {
        Request<MoveStackRequest> request = new Request<>();
        request.setRequest(new MoveStackRequest());
        request.setAuthent(new AuthentInfo());
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.getRequest().setIdStack(13L);
        request.getRequest().setProvinceTo("IdF");

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setId(257L);
        idf.setName("IdF");

        EuropeanProvinceEntity pecs = new EuropeanProvinceEntity();
        pecs.setId(256L);
        pecs.setName("pecs");

        GameEntity game = createGameUsingMocks();

        when(oeUtil.isMobile(any())).thenReturn(false);

        List<StackEntity> stacks = new ArrayList<>();
        stacks.add(new StackEntity());
        stacks.get(0).setId(22L);
        stacks.add(new StackEntity());
        stacks.get(1).setId(23L);
        when(stackDao.getMovingStacks(12L)).thenReturn(stacks);

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because stack does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("moveStack.request.idStack", e.getParams()[0]);
        }

        StackEntity stack = new StackEntity();
        stack.setMovePhase(MovePhaseEnum.MOVED);
        stack.setProvince("pecs");
        stack.setId(14L);
        game.getStacks().add(stack);

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because stack does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("moveStack.request.idStack", e.getParams()[0]);
        }

        stack.setId(13L);

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because stack is not mobile");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.STACK_NOT_MOBILE, e.getCode());
            Assert.assertEquals("moveStack.request.idStack", e.getParams()[0]);
        }

        when(oeUtil.isMobile(any())).thenReturn(true);

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because stack has already moved");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.STACK_ALREADY_MOVED, e.getCode());
            Assert.assertEquals("moveStack.request.idStack", e.getParams()[0]);
        }

        stack.setMovePhase(MovePhaseEnum.NOT_MOVED);

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because another stack is moving");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.OTHER_STACK_MOVING, e.getCode());
            Assert.assertEquals("moveStack.request.idStack", e.getParams()[0]);
        }

        stack.setMovePhase(MovePhaseEnum.IS_MOVING);

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because province does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("moveStack.request.provinceTo", e.getParams()[0]);
        }

        when(provinceDao.getProvinceByName("pecs")).thenReturn(pecs);
        when(provinceDao.getProvinceByName("IdF")).thenReturn(idf);

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because province is not close to former one");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("moveStack.request.provinceTo", e.getParams()[0]);
        }
    }

    @Test
    public void testMoveStackSuccess() throws Exception {
        testMoveStackSuccess(true);
    }

    @Test
    public void testMoveAgainStackSuccess() throws Exception {
        testMoveStackSuccess(false);
    }

    private void testMoveStackSuccess(boolean firstMove) throws Exception {
        Request<MoveStackRequest> request = new Request<>();
        request.setRequest(new MoveStackRequest());
        request.setAuthent(new AuthentInfo());
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.getRequest().setIdStack(13L);
        request.getRequest().setProvinceTo("IdF");

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setId(257L);
        idf.setName("IdF");

        EuropeanProvinceEntity pecs = new EuropeanProvinceEntity();
        pecs.setId(256L);
        pecs.setName("pecs");

        GameEntity game = createGameUsingMocks();
        StackEntity stack = new StackEntity();
        stack.setProvince("pecs");
        if (!firstMove) {
            stack.setMovePhase(MovePhaseEnum.IS_MOVING);
        }
        stack.setId(13L);
        BorderEntity border = new BorderEntity();
        border.setProvinceFrom(pecs);
        border.setProvinceTo(idf);
        pecs.getBorders().add(border);
        game.getStacks().add(stack);

        when(provinceDao.getProvinceByName("pecs")).thenReturn(pecs);
        when(provinceDao.getProvinceByName("IdF")).thenReturn(idf);
        when(oeUtil.isMobile(stack)).thenReturn(true);

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

        DiffResponse response = boardService.moveStack(request);

        InOrder inOrder = inOrder(gameDao, provinceDao, diffDao, socketHandler, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(provinceDao).getProvinceByName("IdF");
        inOrder.verify(provinceDao).getProvinceByName("pecs");
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(socketHandler).push(anyObject(), anyObject(), anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.MOVE, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.STACK, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        if (firstMove) {
            Assert.assertEquals(3, diffEntity.getAttributes().size());
        } else {
            Assert.assertEquals(2, diffEntity.getAttributes().size());
        }
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE_FROM, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(pecs.getName(), diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE_TO, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals(idf.getName(), diffEntity.getAttributes().get(1).getValue());
        if (firstMove) {
            Assert.assertEquals(DiffAttributeTypeEnum.MOVE_PHASE, diffEntity.getAttributes().get(2).getType());
            Assert.assertEquals(MovePhaseEnum.IS_MOVING.name(), diffEntity.getAttributes().get(2).getValue());
        }

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
    }

    @Test
    public void testEndMoveStackFailSimple() {
        Pair<Request<EndMoveStackRequest>, GameEntity> pair = testCheckGame(boardService::endMoveStack, "endMoveStack");
        Request<EndMoveStackRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setId(6L);
        game.getStacks().get(0).setMovePhase(MovePhaseEnum.NOT_MOVED);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).setId(7L);
        game.getStacks().get(1).setMovePhase(MovePhaseEnum.MOVED);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(2).setId(8L);
        game.getStacks().get(2).setMovePhase(MovePhaseEnum.IS_MOVING);

        try {
            boardService.endMoveStack(request);
            Assert.fail("Should break because endMoveStack.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("endMoveStack.request", e.getParams()[0]);
        }

        request.setRequest(new EndMoveStackRequest());

        try {
            boardService.endMoveStack(request);
            Assert.fail("Should break because idStack is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("endMoveStack.request.idStack", e.getParams()[0]);
        }

        request.getRequest().setIdStack(6L);

        try {
            boardService.endMoveStack(request);
            Assert.fail("Should break because stack is not moving");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.STACK_NOT_MOVING, e.getCode());
            Assert.assertEquals("endMoveStack.request.idStack", e.getParams()[0]);
        }


        request.getRequest().setIdStack(7L);

        try {
            boardService.endMoveStack(request);
            Assert.fail("Should break because stack is not moving");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.STACK_NOT_MOVING, e.getCode());
            Assert.assertEquals("endMoveStack.request.idStack", e.getParams()[0]);
        }
    }

    @Test
    public void testEndMoveStackSuccess() throws Exception {
        Request<EndMoveStackRequest> request = new Request<>();
        request.setRequest(new EndMoveStackRequest());
        request.setAuthent(new AuthentInfo());
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.getRequest().setIdStack(13L);

        GameEntity game = createGameUsingMocks();
        StackEntity stack = new StackEntity();
        stack.setProvince("pecs");
        stack.setMovePhase(MovePhaseEnum.IS_MOVING);
        stack.setId(13L);
        game.getStacks().add(stack);

        when(oeUtil.isMobile(stack)).thenReturn(true);

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

        DiffResponse response = boardService.endMoveStack(request);

        InOrder inOrder = inOrder(gameDao, diffDao, socketHandler, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(socketHandler).push(anyObject(), anyObject(), anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.MODIFY, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.STACK, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(1, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.MOVE_PHASE, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(MovePhaseEnum.MOVED.name(), diffEntity.getAttributes().get(0).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
    }

    @Test
    public void testMoveCounterFailSimple() {
        Pair<Request<MoveCounterRequest>, GameEntity> pair = testCheckGame(boardService::moveCounter, "moveCounter");
        Request<MoveCounterRequest> request = pair.getLeft();

        try {
            boardService.moveCounter(request);
            Assert.fail("Should break because moveCounter.authent is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("moveCounter.authent", e.getParams()[0]);
        }

        request.setAuthent(new AuthentInfo());

        try {
            boardService.moveCounter(request);
            Assert.fail("Should break because moveCounter.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("moveCounter.request", e.getParams()[0]);
        }

        request.setRequest(new MoveCounterRequest());

        try {
            boardService.moveCounter(request);
            Assert.fail("Should break because idCounter is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("moveCounter.request.idCounter", e.getParams()[0]);
        }
    }

    @Test
    public void testMoveCounterFailComplex() {
        Request<MoveCounterRequest> request = new Request<>();
        request.setRequest(new MoveCounterRequest());
        request.setAuthent(new AuthentInfo());
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.getRequest().setIdCounter(13L);
        request.getRequest().setIdStack(8L);

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setId(257L);
        idf.setName("IdF");

        EuropeanProvinceEntity pecs = new EuropeanProvinceEntity();
        pecs.setId(256L);
        pecs.setName("pecs");

        GameEntity game = createGameUsingMocks();

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
        counter.setCountry("france");
        game.getStacks().add(stack);
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setName("france");
        country.setUsername("toto");
        game.getCountries().add(country);

        try {
            boardService.moveCounter(request);
            Assert.fail("Should break because counter does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("moveCounter.request.idCounter", e.getParams()[0]);
        }

        when(counterDao.getCounter(13L, 12L)).thenReturn(counter);

        try {
            boardService.moveCounter(request);
            Assert.fail("Should break because username has not the right to move this counter");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("moveCounter.authent.username", e.getParams()[0]);
        }

        counter.setCountry("genes");

        List<String> patrons = new ArrayList<>();
        patrons.add("france");
        when(counterDao.getPatrons("genes", 12L)).thenReturn(patrons);

        try {
            boardService.moveCounter(request);
            Assert.fail("Should break because username has not the right to move this counter");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("username", e.getParams()[0]);
        }

        request.getAuthent().setUsername("toto");

        try {
            boardService.moveCounter(request);
            Assert.fail("Should break because trying to move the counter in another province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("moveCounter.request.idStack", e.getParams()[0]);
        }

        request.getRequest().setIdStack(9L);

        try {
            boardService.moveCounter(request);
            Assert.fail("Should break because trying to move the counter in the same stack");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("moveCounter.request.idStack", e.getParams()[0]);
        }
    }

    @Test
    public void testMoveCounterSuccess() throws Exception {
        Request<MoveCounterRequest> request = new Request<>();
        request.setRequest(new MoveCounterRequest());
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("toto");
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.getRequest().setIdCounter(13L);
        request.getRequest().setIdStack(8L);

        GameEntity game = createGameUsingMocks();

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
        counter.setCountry("genes");
        stack.getCounters().add(counter);
        stack.getCounters().add(new CounterEntity());
        game.getStacks().add(stack);
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setName("france");
        country.setUsername("toto");
        game.getCountries().add(country);

        List<String> patrons = new ArrayList<>();
        patrons.add("france");

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        when(counterDao.getCounter(13L, 12L)).thenReturn(counter);

        when(counterDao.getPatrons("genes", 12L)).thenReturn(patrons);

        when(diffDao.create(anyObject())).thenAnswer(invocation -> {
            diffEntity = (DiffEntity) invocation.getArguments()[0];
            return diffEntity;
        });

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenReturn(diffAfter);

        DiffResponse response = boardService.moveCounter(request);

        InOrder inOrder = inOrder(gameDao, diffDao, socketHandler, counterDao, stackDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(counterDao).getCounter(13L, 12L);
        inOrder.verify(counterDao).getPatrons("genes", 12L);
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(socketHandler).push(anyObject(), anyObject(), anyObject());
        inOrder.verify(gameDao).update(game, false);
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.MOVE, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(4, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK_FROM, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals("9", diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK_TO, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals("8", diffEntity.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE_FROM, diffEntity.getAttributes().get(2).getType());
        Assert.assertEquals("IdF", diffEntity.getAttributes().get(2).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE_TO, diffEntity.getAttributes().get(3).getType());
        Assert.assertEquals("IdF", diffEntity.getAttributes().get(3).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
    }

    @Test
    public void testMoveCounterInNewStackSuccess() throws Exception {
        Request<MoveCounterRequest> request = new Request<>();
        request.setRequest(new MoveCounterRequest());
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("toto");
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.getRequest().setIdCounter(13L);

        GameEntity game = createGameUsingMocks();

        StackEntity stack = new StackEntity();
        stack.setProvince("IdF");
        stack.setId(9L);
        CounterEntity counter = new CounterEntity();
        counter.setId(13L);
        counter.setOwner(stack);
        stack.getCounters().add(counter);
        game.getStacks().add(stack);

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

        DiffResponse response = boardService.moveCounter(request);

        InOrder inOrder = inOrder(gameDao, diffDao, counterDao, stackDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(counterDao).getCounter(13L, 12L);
        inOrder.verify(stackDao).create(arg.capture());
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(gameDao).update(game, false);
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.MOVE, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(5, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK_FROM, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals("9", diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK_TO, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals("25", diffEntity.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE_FROM, diffEntity.getAttributes().get(2).getType());
        Assert.assertEquals("IdF", diffEntity.getAttributes().get(2).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE_TO, diffEntity.getAttributes().get(3).getType());
        Assert.assertEquals("IdF", diffEntity.getAttributes().get(3).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK_DEL, diffEntity.getAttributes().get(4).getType());
        Assert.assertEquals("9", diffEntity.getAttributes().get(4).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
    }
}
