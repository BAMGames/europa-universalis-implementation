package com.mkl.eu.service.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.vo.AuthentInfo;
import com.mkl.eu.client.common.vo.GameInfo;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.board.*;
import com.mkl.eu.client.service.service.common.ValidateRequest;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.tables.Leader;
import com.mkl.eu.client.service.vo.tables.Tables;
import com.mkl.eu.service.service.domain.IStatusWorkflowDomain;
import com.mkl.eu.service.service.domain.impl.CounterDomainImpl;
import com.mkl.eu.service.service.mapping.GameMapping;
import com.mkl.eu.service.service.mapping.chat.ChatMapping;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.board.IStackDao;
import com.mkl.eu.service.service.persistence.chat.IChatDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryOrderEntity;
import com.mkl.eu.service.service.persistence.oe.ref.country.CountryEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.BorderEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.EuropeanProvinceEntity;
import com.mkl.eu.service.service.persistence.ref.ICountryDao;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.service.impl.AbstractBack;
import com.mkl.eu.service.service.service.impl.BoardServiceImpl;
import com.mkl.eu.service.service.util.DiffUtil;
import com.mkl.eu.service.service.util.IOEUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

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
    private CounterDomainImpl counterDomain;

    @Mock
    private IStatusWorkflowDomain statusWorkflowDomain;

    @Mock
    private IProvinceDao provinceDao;

    @Mock
    private ICountryDao countryDao;

    @Mock
    private ICounterDao counterDao;

    @Mock
    private IStackDao stackDao;

    @Mock
    private IChatDao chatDao;

    @Mock
    private GameMapping gameMapping;

    @Mock
    private ChatMapping chatMapping;

    @Mock
    private IOEUtil oeUtil;

    @Test
    public void testMoveStackFailSimple() {
        Pair<Request<MoveStackRequest>, GameEntity> pair = testCheckGame(boardService::moveStack, "moveStack");
        Request<MoveStackRequest> request = pair.getLeft();
        request.getGame().setIdCountry(26L);
        testCheckStatus(pair.getRight(), request, boardService::moveStack, "moveStack", GameStatusEnum.MILITARY_MOVE);

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
        request.getGame().setIdCountry(26L);
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

        GameEntity game = createGameUsingMocks(GameStatusEnum.MILITARY_MOVE, 26L);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(27L);
        game.getCountries().get(0).setName("angleterre");
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(26L);
        game.getCountries().get(1).setName("france");

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
        stack.setMove(8);
        stack.setProvince("pecs");
        stack.setId(14L);
        stack.setCountry("angleterre");
        stack.getCounters().add(new CounterEntity());
        stack.getCounters().get(0).setType(CounterFaceTypeEnum.ARMY_PLUS);
        stack.getCounters().get(0).setCountry("france");
        game.getStacks().add(stack);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).setProvince("pecs");
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.ARMY_MINUS);
        game.getStacks().get(1).getCounters().get(0).setCountry("angleterre");

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
            Assert.assertEquals(IConstantsServiceException.PROVINCES_NOT_NEIGHBOR, e.getCode());
            Assert.assertEquals("moveStack.request.provinceTo", e.getParams()[0]);
        }

        pecs.getBorders().add(new BorderEntity());
        pecs.getBorders().get(0).setProvinceFrom(pecs);
        pecs.getBorders().get(0).setProvinceTo(idf);
        when(oeUtil.getController(idf, game)).thenReturn(game.getCountries().get(1).getName());

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because stack is owned by user");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.CANT_MOVE_PROVINCE, e.getCode());
            Assert.assertEquals("moveStack.request.provinceTo", e.getParams()[0]);
        }

        when(oeUtil.getAllies(game.getCountries().get(1), game)).thenReturn(Collections.singletonList(game.getCountries().get(1).getName()));
        when(oeUtil.getEnemies(game.getCountries().get(1), game)).thenReturn(Collections.singletonList(game.getCountries().get(0).getName()));
        when(oeUtil.getMovePoints(pecs, idf, true)).thenReturn(-1);

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because province is not close to former one");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PROVINCES_NOT_NEIGHBOR, e.getCode());
            Assert.assertEquals("moveStack.request.provinceTo", e.getParams()[0]);
        }

        when(oeUtil.getMovePoints(pecs, idf, true)).thenReturn(6);

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because stack is owned by user");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PROVINCE_TOO_FAR, e.getCode());
            Assert.assertEquals("moveStack.request.provinceTo", e.getParams()[0]);
        }

        stack.setMove(6);

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because stack is pinned by enemy");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.ENEMY_FORCES_NOT_PINNED, e.getCode());
            Assert.assertEquals("moveStack.request.idStack", e.getParams()[0]);
        }

        game.getStacks().add(new StackEntity());
        game.getStacks().get(2).setProvince("pecs");
        game.getStacks().get(2).getCounters().add(new CounterEntity());
        game.getStacks().get(2).getCounters().get(0).setType(CounterFaceTypeEnum.LAND_DETACHMENT);
        game.getStacks().get(2).getCounters().get(0).setCountry("france");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(3).setProvince("pecs");
        game.getStacks().get(3).setBesieged(true);
        game.getStacks().get(3).getCounters().add(new CounterEntity());
        game.getStacks().get(3).getCounters().get(0).setType(CounterFaceTypeEnum.LAND_DETACHMENT);
        game.getStacks().get(3).getCounters().get(0).setCountry("france");

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because stack is pinned by enemy");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.ENEMY_FORCES_NOT_PINNED, e.getCode());
            Assert.assertEquals("moveStack.request.idStack", e.getParams()[0]);
        }

        game.getStacks().get(3).setBesieged(false);

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because stack is owned by user");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("moveStack.idCountry", e.getParams()[0]);
        }

        when(oeUtil.getController(idf, game)).thenReturn(game.getCountries().get(0).getName());
        when(oeUtil.getController(pecs, game)).thenReturn(game.getCountries().get(0).getName());
        when(oeUtil.getFortressLevel(pecs, game)).thenReturn(4);

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because stack can't break siege");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.CANT_BREAK_SIEGE, e.getCode());
            Assert.assertEquals("moveStack.request.idStack", e.getParams()[0]);
        }

        when(oeUtil.getController(idf, game)).thenReturn(game.getCountries().get(1).getName());

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because stack is owned by user");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("moveStack.idCountry", e.getParams()[0]);
        }

        when(oeUtil.getController(idf, game)).thenReturn(game.getCountries().get(0).getName());
        when(oeUtil.getFortressLevel(pecs, game)).thenReturn(2);

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because stack is owned by user");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("moveStack.idCountry", e.getParams()[0]);
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
        request.getGame().setIdCountry(26L);
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

        GameEntity game = createGameUsingMocks(GameStatusEnum.MILITARY_MOVE, 26L);
        StackEntity stack = new StackEntity();
        stack.setProvince("pecs");
        if (!firstMove) {
            stack.setMovePhase(MovePhaseEnum.IS_MOVING);
        }
        stack.setId(13L);
        stack.setCountry("france");
        BorderEntity border = new BorderEntity();
        border.setProvinceFrom(pecs);
        border.setProvinceTo(idf);
        pecs.getBorders().add(border);
        game.getStacks().add(stack);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(26L);
        game.getCountries().get(0).setName("france");

        when(provinceDao.getProvinceByName("pecs")).thenReturn(pecs);
        when(provinceDao.getProvinceByName("IdF")).thenReturn(idf);
        when(oeUtil.isMobile(stack)).thenReturn(true);
        when(oeUtil.getEnemies(game.getCountries().get(0), game)).thenReturn(Collections.singletonList(game.getCountries().get(0).getName()));
        when(oeUtil.getController(idf, game)).thenReturn(game.getCountries().get(0).getName());
        when(oeUtil.getMovePoints(pecs, idf, false)).thenReturn(4);
        when(counterDao.getPatrons("france", game.getId())).thenReturn(Collections.singletonList("france"));

        simulateDiff();

        DiffResponse response = boardService.moveStack(request);

        DiffEntity diffEntity = retrieveDiffCreated();

        InOrder inOrder = inOrder(gameDao, provinceDao, diffDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 26L, 1L);
        inOrder.verify(provinceDao).getProvinceByName("IdF");
        inOrder.verify(provinceDao).getProvinceByName("pecs");
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.MOVE, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.STACK, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        if (firstMove) {
            Assert.assertEquals(4, diffEntity.getAttributes().size());
        } else {
            Assert.assertEquals(3, diffEntity.getAttributes().size());
        }
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE_FROM, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(pecs.getName(), diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE_TO, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals(idf.getName(), diffEntity.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.MOVE_POINTS, diffEntity.getAttributes().get(2).getType());
        Assert.assertEquals("4", diffEntity.getAttributes().get(2).getValue());
        if (firstMove) {
            Assert.assertEquals(DiffAttributeTypeEnum.MOVE_PHASE, diffEntity.getAttributes().get(3).getType());
            Assert.assertEquals(MovePhaseEnum.IS_MOVING.name(), diffEntity.getAttributes().get(3).getValue());
        }

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
    }

    @Test
    public void testEndMoveStackFailSimple() {
        Pair<Request<EndMoveStackRequest>, GameEntity> pair = testCheckGame(boardService::endMoveStack, "endMoveStack");
        Request<EndMoveStackRequest> request = pair.getLeft();
        request.getGame().setIdCountry(26L);
        GameEntity game = pair.getRight();
        testCheckStatus(game, request, boardService::endMoveStack, "endMoveStack", GameStatusEnum.MILITARY_MOVE);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setId(6L);
        game.getStacks().get(0).setMovePhase(MovePhaseEnum.NOT_MOVED);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).setId(7L);
        game.getStacks().get(1).setMovePhase(MovePhaseEnum.MOVED);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(2).setId(8L);
        game.getStacks().get(2).setCountry("angleterre");
        game.getStacks().get(2).setMovePhase(MovePhaseEnum.IS_MOVING);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(27L);
        game.getCountries().get(0).setName("angleterre");
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(26L);
        game.getCountries().get(1).setName("france");

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

        request.getRequest().setIdStack(8L);

        try {
            boardService.endMoveStack(request);
            Assert.fail("Should break because stack is owned by user");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("endMoveStack.idCountry", e.getParams()[0]);
        }
    }

    @Test
    public void testEndMoveStackSuccessMoved() throws Exception {
        testEndMoveStackSuccess("angleterre", "venise", MovePhaseEnum.MOVED);
    }

    @Test
    public void testEndMoveStackSuccessFighting() throws Exception {
        testEndMoveStackSuccess("espagne", "espagne", MovePhaseEnum.FIGHTING);
    }

    @Test
    public void testEndMoveStackSuccessBesieging() throws Exception {
        testEndMoveStackSuccess("turquie", "turquie", MovePhaseEnum.BESIEGING);
    }

    private void testEndMoveStackSuccess(String enemy, String controller, MovePhaseEnum movePhase) throws Exception {
        Request<EndMoveStackRequest> request = new Request<>();
        request.setRequest(new EndMoveStackRequest());
        request.setAuthent(new AuthentInfo());
        request.setGame(new GameInfo());
        request.getGame().setIdCountry(26L);
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.getRequest().setIdStack(13L);

        GameEntity game = createGameUsingMocks(GameStatusEnum.MILITARY_MOVE, 26L);
        StackEntity stack = new StackEntity();
        stack.setProvince("pecs");
        stack.setMovePhase(MovePhaseEnum.IS_MOVING);
        stack.setCountry("france");
        stack.setId(13L);
        game.getStacks().add(stack);
        StackEntity otherStack = new StackEntity();
        otherStack.setProvince("pecs");
        otherStack.setCountry("espagne");
        otherStack.setId(14L);
        otherStack.getCounters().add(new CounterEntity());
        otherStack.getCounters().get(0).setCountry("espagne");
        otherStack.getCounters().get(0).setType(CounterFaceTypeEnum.ARMY_MINUS);
        game.getStacks().add(otherStack);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(26L);
        game.getCountries().get(0).setName("france");

        when(oeUtil.isMobile(stack)).thenReturn(true);
        when(oeUtil.getEnemies(game.getCountries().get(0), game)).thenReturn(Collections.singletonList(enemy));
        EuropeanProvinceEntity province = new EuropeanProvinceEntity();
        when(provinceDao.getProvinceByName(stack.getProvince())).thenReturn(province);
        when(oeUtil.getController(province, game)).thenReturn(controller);
        when(counterDao.getPatrons("france", game.getId())).thenReturn(Collections.singletonList("france"));

        simulateDiff();

        DiffResponse response = boardService.endMoveStack(request);

        DiffEntity diffEntity = retrieveDiffCreated();

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.MODIFY, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.STACK, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(1, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.MOVE_PHASE, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(movePhase.name(), diffEntity.getAttributes().get(0).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
    }

    @Test
    public void testTakeStackControlFail() {
        Pair<Request<TakeStackControlRequest>, GameEntity> pair = testCheckGame(boardService::takeStackControl, "takeStackControl");
        Request<TakeStackControlRequest> request = pair.getLeft();
        request.getGame().setIdCountry(26L);
        GameEntity game = pair.getRight();
        testCheckStatus(game, request, boardService::takeStackControl, "takeStackControl", GameStatusEnum.MILITARY_MOVE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(27L);
        game.getCountries().get(0).setName("espagne");
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(26L);
        game.getCountries().get(1).setName("france");
        StackEntity stack = new StackEntity();
        stack.setId(7L);
        stack.setProvince("pecs");
        EuropeanProvinceEntity pecs = new EuropeanProvinceEntity();
        pecs.setName("pecs");
        when(provinceDao.getProvinceByName("pecs")).thenReturn(pecs);
        stack.setCountry("france");
        CounterEntity counter = new CounterEntity();
        counter.setCountry("france");
        counter.setType(CounterFaceTypeEnum.ARMY_MINUS);
        stack.getCounters().add(counter);
        counter = new CounterEntity();
        counter.setCountry("genes");
        counter.setType(CounterFaceTypeEnum.LAND_DETACHMENT);
        stack.getCounters().add(counter);
        counter = new CounterEntity();
        counter.setCountry("espagne");
        counter.setType(CounterFaceTypeEnum.LAND_DETACHMENT);
        stack.getCounters().add(counter);
        counter = new CounterEntity();
        counter.setCountry("espagne");
        counter.setType(CounterFaceTypeEnum.LAND_DETACHMENT);
        stack.getCounters().add(counter);
        game.getStacks().add(stack);
        Tables tables = new Tables();
        AbstractBack.TABLES = tables;

        try {
            boardService.takeStackControl(request);
            Assert.fail("Should break because takeStackControl.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("takeStackControl.request", e.getParams()[0]);
        }

        request.setRequest(new TakeStackControlRequest());

        try {
            boardService.takeStackControl(request);
            Assert.fail("Should break because idStack is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("takeStackControl.request.idStack", e.getParams()[0]);
        }

        request.getRequest().setIdStack(666L);

        try {
            boardService.takeStackControl(request);
            Assert.fail("Should break because stack does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("takeStackControl.request.idStack", e.getParams()[0]);
        }

        request.getRequest().setIdStack(7L);

        try {
            boardService.takeStackControl(request);
            Assert.fail("Should break because country is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("takeStackControl.request.country", e.getParams()[0]);
        }

        request.getRequest().setCountry("france");

        try {
            boardService.takeStackControl(request);
            Assert.fail("Should break because stack is already controller by requested country");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.STACK_ALREADY_CONTROLLED, e.getCode());
            Assert.assertEquals("takeStackControl.request.country", e.getParams()[0]);
        }

        request.getRequest().setCountry("genes");
        when(counterDao.getPatrons("genes", game.getId())).thenReturn(Collections.singletonList("angleterre"));

        try {
            boardService.takeStackControl(request);
            Assert.fail("Should break because stack is owned by user");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("takeStackControl.idCountry", e.getParams()[0]);
        }

        when(counterDao.getPatrons("genes", game.getId())).thenReturn(Collections.singletonList("france"));

        try {
            boardService.takeStackControl(request);
            Assert.fail("Should break because stack cant be controlled by requested country");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.STACK_CONTROL_INVALID_COUNTRY, e.getCode());
            Assert.assertEquals("takeStackControl.request.country", e.getParams()[0]);
        }

        when(oeUtil.getLeadingCountries(any())).thenReturn(Collections.singletonList("genes"));
        Leader leader = new Leader();
        leader.setCode("Napo");
        leader.setCountry("genes");
        List<Leader> leaders = new ArrayList<>();
        leaders.add(leader);
        when(oeUtil.getLeaders(any(), any(), any())).thenReturn(leaders);

        try {
            boardService.takeStackControl(request);
            Assert.fail("Should break because stack cant be controlled by requested country without selecting the leader");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.STACK_CONTROL_LEADER_ISSUE, e.getCode());
            Assert.assertEquals("takeStackControl.request.idLeader", e.getParams()[0]);
        }

        request.getRequest().setIdLeader(21L);

        try {
            boardService.takeStackControl(request);
            Assert.fail("Should break because idLeader is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("takeStackControl.request.idLeader", e.getParams()[0]);
        }

        counter = createCounter(21L, "genes", CounterFaceTypeEnum.LEADER, stack);
        stack.getCounters().add(counter);

        try {
            boardService.takeStackControl(request);
            Assert.fail("Should break because idLeader is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("takeStackControl.request.idLeader", e.getParams()[0]);
        }

        counter.setCode("Nabo");

        try {
            boardService.takeStackControl(request);
            Assert.fail("Should break because idLeader is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("takeStackControl.request.idLeader", e.getParams()[0]);
        }

        stack.setLeader("Nabo");
        leader = new Leader();
        leader.setCode("Nabo");
        leader.setCountry("france");
        tables.getLeaders().add(leader);

        try {
            boardService.takeStackControl(request);
            Assert.fail("Should break because leader country take priority over request country");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.STACK_ALREADY_CONTROLLED, e.getCode());
            Assert.assertEquals("takeStackControl.request.country", e.getParams()[0]);
        }
    }

    @Test
    public void testTakeStackControlSuccess() throws Exception {
        Request<TakeStackControlRequest> request = new Request<>();
        request.setRequest(new TakeStackControlRequest());
        request.setAuthent(new AuthentInfo());
        request.setGame(new GameInfo());
        request.getGame().setIdCountry(27L);
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.getRequest().setIdStack(7L);
        request.getRequest().setCountry("espagne");
        Tables tables = new Tables();
        AbstractBack.TABLES = tables;

        GameEntity game = createGameUsingMocks(GameStatusEnum.MILITARY_MOVE, 27L);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(27L);
        game.getCountries().get(0).setName("espagne");
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(26L);
        game.getCountries().get(1).setName("france");
        StackEntity stack = new StackEntity();
        stack.setId(7L);
        stack.setProvince("pecs");
        EuropeanProvinceEntity pecs = new EuropeanProvinceEntity();
        pecs.setName("pecs");
        when(provinceDao.getProvinceByName("pecs")).thenReturn(pecs);
        stack.setCountry("france");
        CounterEntity counter = new CounterEntity();
        counter.setCountry("france");
        counter.setType(CounterFaceTypeEnum.ARMY_MINUS);
        stack.getCounters().add(counter);
        counter = new CounterEntity();
        counter.setCountry("genes");
        counter.setType(CounterFaceTypeEnum.LAND_DETACHMENT);
        stack.getCounters().add(counter);
        counter = new CounterEntity();
        counter.setCountry("espagne");
        counter.setType(CounterFaceTypeEnum.LAND_DETACHMENT);
        stack.getCounters().add(counter);
        counter = new CounterEntity();
        counter.setCountry("espagne");
        counter.setType(CounterFaceTypeEnum.LAND_DETACHMENT);
        stack.getCounters().add(counter);
        game.getStacks().add(stack);
        when(counterDao.getPatrons("espagne", game.getId())).thenReturn(Collections.singletonList("espagne"));
        when(oeUtil.getLeadingCountries(any())).thenReturn(Arrays.asList("france", "espagne"));

        simulateDiff();

        DiffResponse response = boardService.takeStackControl(request);

        DiffEntity diffEntity = retrieveDiffCreated();

        InOrder inOrder = inOrder(gameDao, diffDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 27L, 1L);
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(7L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.MODIFY, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.STACK, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(1, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.COUNTRY, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals("espagne", diffEntity.getAttributes().get(0).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());

        Assert.assertEquals("espagne", stack.getCountry());
    }

    @Test
    public void testMoveCounterFailSimple() {
        Pair<Request<MoveCounterRequest>, GameEntity> pair = testCheckGame(boardService::moveCounter, "moveCounter");
        Request<MoveCounterRequest> request = pair.getLeft();
        request.getGame().setIdCountry(26L);
        testCheckStatus(pair.getRight(), request, boardService::moveCounter, "moveCounter", GameStatusEnum.MILITARY_MOVE);

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
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.getGame().setIdCountry(27L);
        request.getRequest().setIdCounter(13L);
        request.getRequest().setIdStack(8L);

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setId(257L);
        idf.setName("IdF");

        EuropeanProvinceEntity pecs = new EuropeanProvinceEntity();
        pecs.setId(256L);
        pecs.setName("pecs");

        GameEntity game = createGameUsingMocks(GameStatusEnum.MILITARY_MOVE, 26L, 27L);

        StackEntity stack = new StackEntity();
        stack.setProvince("IdF");
        stack.setId(8L);
        game.getStacks().add(stack);

        stack = new StackEntity();
        stack.setProvince("Pecs");
        stack.setId(9L);
        CounterEntity counterToMove = new CounterEntity();
        counterToMove.setId(13L);
        counterToMove.setOwner(stack);
        counterToMove.setCountry("genes");
        counterToMove.setType(CounterFaceTypeEnum.LAND_DETACHMENT);
        game.getStacks().add(stack);
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setName("france");
        country.setUsername("toto");
        country.setId(26L);
        game.getCountries().add(country);
        country = new PlayableCountryEntity();
        country.setName("angleterre");
        country.setUsername("toto");
        country.setId(27L);
        game.getCountries().add(country);

        stack = new StackEntity();
        stack.setProvince("Pecs");
        stack.setId(10L);
        game.getStacks().add(stack);
        CounterEntity counter = new CounterEntity();
        counter.setId(14L);
        counter.setOwner(stack);
        stack.getCounters().add(counter);
        counter.setCountry("genes");
        counter.setType(CounterFaceTypeEnum.ARMY_PLUS);
        game.getStacks().add(stack);
        counter = new CounterEntity();
        counter.setId(14L);
        counter.setOwner(stack);
        stack.getCounters().add(counter);
        counter.setCountry("genes");
        counter.setType(CounterFaceTypeEnum.ARMY_MINUS);
        counter = new CounterEntity();
        counter.setId(15L);
        counter.setOwner(stack);
        stack.getCounters().add(counter);
        counter.setCountry("genes");
        counter.setType(CounterFaceTypeEnum.LAND_DETACHMENT);

        try {
            boardService.moveCounter(request);
            Assert.fail("Should break because counter does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("moveCounter.request.idCounter", e.getParams()[0]);
        }

        when(counterDao.getCounter(13L, 12L)).thenReturn(counterToMove);

        List<String> patrons = new ArrayList<>();
        patrons.add("france");
        when(counterDao.getPatrons("genes", 12L)).thenReturn(patrons);

        try {
            boardService.moveCounter(request);
            Assert.fail("Should break because username has not the right to move this counter");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("moveCounter.idCountry", e.getParams()[0]);
        }

        request.getGame().setIdCountry(26L);

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

        request.getRequest().setIdStack(10L);

        try {
            boardService.moveCounter(request);
            Assert.fail("Should break because trying to move the counter in a too big stack");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.STACK_TOO_BIG, e.getCode());
            Assert.assertEquals("moveCounter.request.idStack", e.getParams()[0]);
        }

        stack.getCounters().remove(2);
        counterToMove.setType(CounterFaceTypeEnum.ARMY_PLUS);

        try {
            boardService.moveCounter(request);
            Assert.fail("Should break because trying to move the counter in a too big stack");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.STACK_TOO_BIG, e.getCode());
            Assert.assertEquals("moveCounter.request.idStack", e.getParams()[0]);
        }
    }

    @Test
    public void testMoveCounterSuccess() throws Exception {
        Request<MoveCounterRequest> request = new Request<>();
        request.setRequest(new MoveCounterRequest());
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.getGame().setIdCountry(666L);
        request.getRequest().setIdCounter(13L);
        request.getRequest().setIdStack(8L);

        GameEntity game = createGameUsingMocks(GameStatusEnum.MILITARY_MOVE, 666L);

        StackEntity stackTo = new StackEntity();
        stackTo.setProvince("IdF");
        stackTo.setId(8L);
        stackTo.setCountry("france");
        game.getStacks().add(stackTo);

        StackEntity stack = new StackEntity();
        stack.setProvince("IdF");
        stack.setId(9L);
        stack.setCountry("genes");
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
        country.setId(666L);
        game.getCountries().add(country);

        List<String> patrons = new ArrayList<>();
        patrons.add("france");

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 666L, 1L)).thenReturn(diffBefore);

        when(counterDao.getCounter(13L, 12L)).thenReturn(counter);

        when(counterDao.getPatrons("genes", 12L)).thenReturn(patrons);
        when(counterDomain.changeCounterOwner(any(), any(), any())).thenCallRealMethod();
        when(oeUtil.getController(stack)).thenReturn("genes");
        when(oeUtil.getController(stackTo)).thenReturn("france");

        simulateDiff();

        boardService.moveCounter(request);

        List<DiffEntity> diffs = retrieveDiffsCreated();
        Assert.assertEquals(1, diffs.size());
        DiffEntity diffEntity = diffs.get(0);

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

        Assert.assertEquals("genes", stack.getCountry());
        Assert.assertEquals("france", stackTo.getCountry());
    }

    @Test
    public void testMoveCounterSuccessSwitchingControllers() throws Exception {
        Request<MoveCounterRequest> request = new Request<>();
        request.setRequest(new MoveCounterRequest());
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.getGame().setIdCountry(666L);
        request.getRequest().setIdCounter(13L);
        request.getRequest().setIdStack(8L);

        GameEntity game = createGameUsingMocks(GameStatusEnum.MILITARY_MOVE, 666L);

        StackEntity stackTo = new StackEntity();
        stackTo.setProvince("IdF");
        stackTo.setId(8L);
        stackTo.setCountry("france");
        game.getStacks().add(stackTo);

        StackEntity stack = new StackEntity();
        stack.setProvince("IdF");
        stack.setId(9L);
        stack.setCountry("genes");
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
        country.setId(666L);
        game.getCountries().add(country);

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setName("IdF");
        when(provinceDao.getProvinceByName("IdF")).thenReturn(idf);

        List<String> patrons = new ArrayList<>();
        patrons.add("france");

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 666L, 1L)).thenReturn(diffBefore);

        when(counterDao.getCounter(13L, 12L)).thenReturn(counter);

        when(counterDao.getPatrons("genes", 12L)).thenReturn(patrons);
        when(counterDomain.changeCounterOwner(any(), any(), any())).thenCallRealMethod();
        when(oeUtil.getController(stack)).thenReturn("france");
        when(oeUtil.getController(stackTo)).thenReturn("genes");
        when(oeUtil.getLeader(any(), any(), any())).thenReturn("leader");

        simulateDiff();

        boardService.moveCounter(request);

        List<DiffEntity> diffs = retrieveDiffsCreated();
        Assert.assertEquals(3, diffs.size());

        DiffEntity diffEntity = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.MOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER)
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diffEntity);
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

        diffEntity = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.STACK
                        && Objects.equals(d.getIdObject(), stack.getId()))
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diffEntity);
        Assert.assertEquals("france", getAttribute(diffEntity, DiffAttributeTypeEnum.COUNTRY));
        Assert.assertEquals("leader", getAttribute(diffEntity, DiffAttributeTypeEnum.LEADER));

        diffEntity = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.STACK
                        && Objects.equals(d.getIdObject(), stackTo.getId()))
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diffEntity);
        Assert.assertEquals("genes", getAttribute(diffEntity, DiffAttributeTypeEnum.COUNTRY));
        Assert.assertEquals("leader", getAttribute(diffEntity, DiffAttributeTypeEnum.LEADER));

        Assert.assertEquals("france", stack.getCountry());
        Assert.assertEquals("leader", stack.getLeader());
        Assert.assertEquals("genes", stackTo.getCountry());
        Assert.assertEquals("leader", stackTo.getLeader());
    }

    @Test
    public void testMoveCounterInNewStackSuccess() throws Exception {
        Request<MoveCounterRequest> request = new Request<>();
        request.setRequest(new MoveCounterRequest());
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("toto");
        request.setGame(new GameInfo());
        request.getGame().setIdCountry(26L);
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.getRequest().setIdCounter(13L);

        GameEntity game = createGameUsingMocks(GameStatusEnum.MILITARY_MOVE, 26L);

        StackEntity stack = new StackEntity();
        stack.setProvince("IdF");
        stack.setId(9L);
        CounterEntity counter = new CounterEntity();
        counter.setId(13L);
        counter.setCountry("france");
        counter.setOwner(stack);
        stack.getCounters().add(counter);
        game.getStacks().add(stack);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(26L);
        game.getCountries().get(0).setName("france");

        when(counterDao.getCounter(13L, 12L)).thenReturn(counter);

        StackEntity newStack = new StackEntity();
        newStack.setProvince("IdF");
        newStack.setId(25L);
        when(counterDomain.createStack(any(), any(), any())).thenReturn(newStack);
        when(counterDomain.changeCounterOwner(any(), any(), any())).thenCallRealMethod();
        when(counterDao.getPatrons("france", game.getId())).thenReturn(Collections.singletonList("france"));

        simulateDiff();

        DiffResponse response = boardService.moveCounter(request);

        DiffEntity diffEntity = retrieveDiffCreated();

        InOrder inOrder = inOrder(gameDao, diffDao, counterDao, stackDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 26L, 1L);
        inOrder.verify(counterDao).getCounter(13L, 12L);
        inOrder.verify(diffDao).create(anyObject());
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
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
    }

    @Test
    public void testMoveLeaderFail() {
        Pair<Request<MoveLeaderRequest>, GameEntity> pair = testCheckGame(boardService::moveLeader, "moveLeader");
        Request<MoveLeaderRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        testCheckStatus(pair.getRight(), request, boardService::moveLeader, "moveLeader", GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        request.getGame().setIdCountry(26L);
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setId(26L);
        country.setName("france");
        game.getCountries().add(country);

        try {
            boardService.moveLeader(request);
            Assert.fail("Should break because moveLeader.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("moveLeader.request", e.getParams()[0]);
        }

        request.setRequest(new MoveLeaderRequest());

        try {
            boardService.moveLeader(request);
            Assert.fail("Should break because idCounter is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("moveLeader.request.idCounter", e.getParams()[0]);
        }

        request.getRequest().setIdCounter(13L);

        try {
            boardService.moveLeader(request);
            Assert.fail("Should break because idCounter is not a counter");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("moveLeader.request.idCounter", e.getParams()[0]);
        }

        StackEntity stack = new StackEntity();
        stack.setId(1L);
        stack.setBesieged(true);
        game.getStacks().add(stack);
        CounterEntity counter = createCounter(13L, "france", CounterFaceTypeEnum.ARMY_MINUS, stack);
        stack.getCounters().add(counter);

        try {
            boardService.moveLeader(request);
            Assert.fail("Should break because idCounter is not a leader");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("moveLeader.request.idCounter", e.getParams()[0]);
        }

        counter.setType(CounterFaceTypeEnum.LEADER);

        try {
            boardService.moveLeader(request);
            Assert.fail("Should break because leader is besieged");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.STACK_BESIEGED, e.getCode());
            Assert.assertEquals("moveLeader.request.idCounter", e.getParams()[0]);
        }

        stack.setBesieged(false);
        List<String> patrons = new ArrayList<>();
        patrons.add("genes");
        when(counterDao.getPatrons(counter.getCountry(), game.getId())).thenReturn(patrons);

        try {
            boardService.moveLeader(request);
            Assert.fail("Should break because idCounter cannot be moved by country");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("moveLeader.request.idCounter", e.getParams()[0]);
        }

        patrons.add(country.getName());

        try {
            boardService.moveLeader(request);
            Assert.fail("Should break because province must be specified");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("moveLeader.request.province", e.getParams()[0]);
        }

        StackEntity newStack = new StackEntity();
        newStack.setId(25L);
        newStack.setCountry("genes");
        game.getStacks().add(newStack);
        request.getRequest().setIdStack(25L);

        try {
            boardService.moveLeader(request);
            Assert.fail("Should break because idCounter cannot be moved in this stack");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("moveLeader.request.idStack", e.getParams()[0]);
        }

        when(oeUtil.getAllies(country, game)).thenReturn(patrons);

        try {
            boardService.moveLeader(request);
            Assert.fail("Should break because province must be specified");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("moveLeader.request.province", e.getParams()[0]);
        }

        newStack.setProvince("pecs");

        try {
            boardService.moveLeader(request);
            Assert.fail("Should break because province does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("moveLeader.request.province", e.getParams()[0]);
        }

        AbstractProvinceEntity pecs = new EuropeanProvinceEntity();
        when(provinceDao.getProvinceByName("pecs")).thenReturn(pecs);

        try {
            boardService.moveLeader(request);
            Assert.fail("Should break because province is not controlled by an ally");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("moveLeader.request.province", e.getParams()[0]);
        }
    }

    @Test
    public void testMoveLeaderInStack() throws FunctionalException {
        testMoveLeader(true);
    }

    @Test
    public void testMoveLeaderInProvince() throws FunctionalException {
        testMoveLeader(false);
    }

    private void testMoveLeader(boolean inStack) throws FunctionalException {
        Pair<Request<MoveLeaderRequest>, GameEntity> pair = testCheckGame(boardService::moveLeader, "moveLeader");
        Request<MoveLeaderRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        testCheckStatus(pair.getRight(), request, boardService::moveLeader, "moveLeader", GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        request.getGame().setIdCountry(26L);
        request.setRequest(new MoveLeaderRequest());
        request.getRequest().setIdCounter(13L);
        if (inStack) {
            request.getRequest().setIdStack(25L);
        } else {
            request.getRequest().setProvince("pecs");
        }
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setId(26L);
        country.setName("france");
        game.getCountries().add(country);
        StackEntity stack = new StackEntity();
        stack.setId(1L);
        game.getStacks().add(stack);
        CounterEntity counter = createCounter(13L, "france", CounterFaceTypeEnum.LEADER, stack);
        stack.getCounters().add(counter);
        StackEntity newStack = new StackEntity();
        newStack.setId(25L);
        newStack.setProvince("pecs");
        newStack.setCountry("genes");
        game.getStacks().add(newStack);

        when(counterDao.getPatrons(counter.getCountry(), game.getId())).thenReturn(Arrays.asList("genes", country.getName()));
        when(oeUtil.getAllies(country, game)).thenReturn(Arrays.asList("genes", country.getName()));
        AbstractProvinceEntity pecs = new EuropeanProvinceEntity();
        when(provinceDao.getProvinceByName("pecs")).thenReturn(pecs);
        when(oeUtil.getController(pecs, game)).thenReturn("genes");
        List<DiffEntity> diffsMoveLeader = new ArrayList<>();
        diffsMoveLeader.add(DiffUtil.createDiff(game, DiffTypeEnum.MOVE, DiffTypeObjectEnum.COUNTER, counter.getId(),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE_TO, "pecs")));
        diffsMoveLeader.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STACK, stack.getId(),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.LEADER, "Nabo")));
        when(counterDomain.moveLeader(counter, inStack ? newStack : null, "pecs", game)).thenReturn(diffsMoveLeader);

        simulateDiff();

        boardService.moveLeader(request);

        List<DiffEntity> diffs = retrieveDiffsCreated();

        Assert.assertEquals(diffsMoveLeader, diffs);
    }

    @Test
    public void testValidateMilRoundFail() {
        Pair<Request<ValidateRequest>, GameEntity> pair = testCheckGame(boardService::validateMilitaryRound, "validateMilitaryRound");
        Request<ValidateRequest> request = pair.getLeft();
        request.getGame().setIdCountry(26L);
        GameEntity game = pair.getRight();

        testCheckStatus(game, request, boardService::validateMilitaryRound, "validateMilitaryRound", GameStatusEnum.MILITARY_MOVE);

        game.setTurn(22);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(13L);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setUsername("MKL");

        try {
            boardService.validateMilitaryRound(request);
            Assert.fail("Should break because request.authent is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("validateMilitaryRound.authent", e.getParams()[0]);
        }

        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("toto");

        try {
            boardService.validateMilitaryRound(request);
            Assert.fail("Should break because request.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("validateMilitaryRound.request", e.getParams()[0]);
        }

        request.setRequest(new ValidateRequest());

        try {
            boardService.validateMilitaryRound(request);
            Assert.fail("Should break because request.request.idCountry is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("validateMilitaryRound.idCountry", e.getParams()[0]);
        }

        game.getCountries().get(0).setId(26L);

        try {
            boardService.validateMilitaryRound(request);
            Assert.fail("Should break because request.authent can't do this action");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("validateMilitaryRound.authent.username", e.getParams()[0]);
        }
    }

    @Test
    public void testValidateMilRoundSuccessSimple() throws FunctionalException {
        Request<ValidateRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("MKL");
        request.setGame(createGameInfo());
        request.setRequest(new ValidateRequest());
        request.getGame().setIdCountry(13L);

        GameEntity game = createGameUsingMocks(GameStatusEnum.MILITARY_MOVE, 13L);
        game.setStatus(GameStatusEnum.MILITARY_MOVE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(13L);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setUsername("MKL");
        game.getCountries().get(0).setReady(false);

        simulateDiff();

        boardService.validateMilitaryRound(request);

        List<DiffEntity> diffEntities = retrieveDiffsCreated();

        Assert.assertEquals(0, diffEntities.size());
    }

    @Test
    public void testValidateMilRoundSuccessSimple2() throws FunctionalException {
        Request<ValidateRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("MKL");
        request.setGame(createGameInfo());
        request.setRequest(new ValidateRequest());
        request.getGame().setIdCountry(13L);

        GameEntity game = createGameUsingMocks(GameStatusEnum.MILITARY_MOVE, 13L);
        game.getOrders().get(0).setReady(true);
        game.setStatus(GameStatusEnum.MILITARY_MOVE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(13L);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setUsername("MKL");
        game.getCountries().get(0).setReady(false);

        simulateDiff();

        boardService.validateMilitaryRound(request);

        List<DiffEntity> diffEntities = retrieveDiffsCreated();

        Assert.assertEquals(1, diffEntities.size());
        Assert.assertEquals(12L, diffEntities.get(0).getIdGame().longValue());
        Assert.assertEquals(null, diffEntities.get(0).getIdObject());
        Assert.assertEquals(1L, diffEntities.get(0).getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.INVALIDATE, diffEntities.get(0).getType());
        Assert.assertEquals(DiffTypeObjectEnum.TURN_ORDER, diffEntities.get(0).getTypeObject());
        Assert.assertEquals(2, diffEntities.get(0).getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.STATUS, diffEntities.get(0).getAttributes().get(0).getType());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE.name(), diffEntities.get(0).getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_COUNTRY, diffEntities.get(0).getAttributes().get(1).getType());
        Assert.assertEquals("13", diffEntities.get(0).getAttributes().get(1).getValue());
    }

    @Test
    public void testValidateMilRoundSuccessSimple3() throws FunctionalException {
        Request<ValidateRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("MKL");
        request.setGame(createGameInfo());
        request.setRequest(new ValidateRequest());
        request.getRequest().setValidate(true);
        request.getGame().setIdCountry(13L);

        GameEntity game = createGameUsingMocks(GameStatusEnum.MILITARY_MOVE, 13L);
        CountryOrderEntity order = new CountryOrderEntity();
        order.setActive(true);
        order.setCountry(new PlayableCountryEntity());
        order.getCountry().setId(14L);
        game.getOrders().add(order);
        game.setStatus(GameStatusEnum.MILITARY_MOVE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(13L);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setUsername("MKL");
        game.getCountries().get(0).setReady(false);

        simulateDiff();

        boardService.validateMilitaryRound(request);

        List<DiffEntity> diffEntities = retrieveDiffsCreated();

        Assert.assertEquals(1, diffEntities.size());
        Assert.assertEquals(12L, diffEntities.get(0).getIdGame().longValue());
        Assert.assertEquals(null, diffEntities.get(0).getIdObject());
        Assert.assertEquals(1L, diffEntities.get(0).getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.VALIDATE, diffEntities.get(0).getType());
        Assert.assertEquals(DiffTypeObjectEnum.TURN_ORDER, diffEntities.get(0).getTypeObject());
        Assert.assertEquals(2, diffEntities.get(0).getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.STATUS, diffEntities.get(0).getAttributes().get(0).getType());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE.name(), diffEntities.get(0).getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_COUNTRY, diffEntities.get(0).getAttributes().get(1).getType());
        Assert.assertEquals("13", diffEntities.get(0).getAttributes().get(1).getValue());
    }

    @Test
    public void testValidateMilRoundSuccessComplexNextMove() throws FunctionalException {
        Request<ValidateRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("MKL");
        request.setGame(createGameInfo());
        request.setRequest(new ValidateRequest());
        request.getRequest().setValidate(true);
        request.getGame().setIdCountry(13L);

        GameEntity game = createGameUsingMocks(GameStatusEnum.MILITARY_MOVE, 13L);
        game.getOrders().clear();
        CountryOrderEntity order = new CountryOrderEntity();
        order.setActive(true);
        order.setCountry(new PlayableCountryEntity());
        order.setPosition(3);
        order.getCountry().setId(13L);
        game.getOrders().add(order);
        order = new CountryOrderEntity();
        order.setActive(false);
        order.setCountry(new PlayableCountryEntity());
        order.setPosition(2);
        order.getCountry().setId(9L);
        game.getOrders().add(order);
        order = new CountryOrderEntity();
        order.setActive(false);
        order.setCountry(new PlayableCountryEntity());
        order.setPosition(4);
        order.getCountry().setId(21L);
        game.getOrders().add(order);
        order = new CountryOrderEntity();
        order.setActive(false);
        order.setCountry(new PlayableCountryEntity());
        order.setPosition(4);
        order.getCountry().setId(22L);
        game.getOrders().add(order);

        game.setStatus(GameStatusEnum.MILITARY_MOVE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(13L);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setUsername("MKL");
        game.getCountries().get(0).setReady(false);
        DiffEntity endMilitaryPhase = DiffUtil.createDiff(game, DiffTypeEnum.INVALIDATE, DiffTypeObjectEnum.TURN_ORDER);
        when(statusWorkflowDomain.endMilitaryPhase(game)).thenReturn(Collections.singletonList(endMilitaryPhase));

        simulateDiff();

        boardService.validateMilitaryRound(request);

        List<DiffEntity> diffEntities = retrieveDiffsCreated();

        Assert.assertEquals(1, diffEntities.size());
        Assert.assertEquals(12L, diffEntities.get(0).getIdGame().longValue());
        Assert.assertEquals(null, diffEntities.get(0).getIdObject());
        Assert.assertEquals(1L, diffEntities.get(0).getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.INVALIDATE, diffEntities.get(0).getType());
        Assert.assertEquals(DiffTypeObjectEnum.TURN_ORDER, diffEntities.get(0).getTypeObject());
        Assert.assertEquals(0, diffEntities.get(0).getAttributes().size());
    }

    @Test
    public void testCreateCounterFailSimple() {
        Pair<Request<CreateCounterRequest>, GameEntity> pair = testCheckGame(boardService::createCounter, "createCounter");
        Request<CreateCounterRequest> request = pair.getLeft();
        AbstractBack.TABLES = new Tables();

        try {
            boardService.createCounter(request);
            Assert.fail("Should break because createCounter.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("createCounter.request", e.getParams()[0]);
        }

        request.setRequest(new CreateCounterRequest());

        try {
            boardService.createCounter(request);
            Assert.fail("Should break because createCounter.request.type is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("createCounter.request.type", e.getParams()[0]);
        }

        request.getRequest().setType(CounterFaceTypeEnum.ARMY_MINUS);

        try {
            boardService.createCounter(request);
            Assert.fail("Should break because createCounter.request.country is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("createCounter.request.country", e.getParams()[0]);
        }

        request.getRequest().setCountry("FRA");

        try {
            boardService.createCounter(request);
            Assert.fail("Should break because createCounter.request.country does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("createCounter.request.country", e.getParams()[0]);
        }

        when(countryDao.getCountryByName("FRA")).thenReturn(new CountryEntity());

        try {
            boardService.createCounter(request);
            Assert.fail("Should break because createCounter.request.province is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("createCounter.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("toto");

        try {
            boardService.createCounter(request);
            Assert.fail("Should break because createCounter.request.province does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("createCounter.request.province", e.getParams()[0]);
        }

        when(provinceDao.getProvinceByName("toto")).thenReturn(new EuropeanProvinceEntity());
        request.getRequest().setType(CounterFaceTypeEnum.LEADER);

        try {
            boardService.createCounter(request);
            Assert.fail("Should break because a leader must have a code");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("createCounter.request.code", e.getParams()[0]);
        }

        request.getRequest().setCode("Napo");

        try {
            boardService.createCounter(request);
            Assert.fail("Should break because leader does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("createCounter.request.code", e.getParams()[0]);
        }

        Leader leader = new Leader();
        leader.setCode("Napo");
        AbstractBack.TABLES.getLeaders().add(leader);

        try {
            boardService.createCounter(request);
            Assert.fail("Should break because leader country and request country mismatch");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("createCounter.request.country", e.getParams()[0]);
        }
    }

    @Test
    public void testCreateCounterSuccess() throws Exception {
        Pair<Request<CreateCounterRequest>, GameEntity> pair = testCheckGame(boardService::createCounter, "createCounter");
        Request<CreateCounterRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        request.setRequest(new CreateCounterRequest());
        request.getRequest().setProvince("IdF");
        request.getRequest().setType(CounterFaceTypeEnum.ARMY_MINUS);
        request.getRequest().setCountry("FRA");

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setId(257L);
        idf.setName("IdF");

        when(countryDao.getCountryByName("FRA")).thenReturn(new CountryEntity());
        when(provinceDao.getProvinceByName("IdF")).thenReturn(idf);

        DiffEntity diffCreate = DiffUtil.createDiff(game, DiffTypeEnum.ADD, DiffTypeObjectEnum.COUNTER);
        when(counterDomain.createCounter(CounterFaceTypeEnum.ARMY_MINUS, "FRA", "IdF", null, game)).thenReturn(diffCreate);

        simulateDiff();

        boardService.createCounter(request);

        DiffEntity diff = retrieveDiffCreated();

        Assert.assertEquals(diff, diffCreate);
    }

    @Test
    public void testCreateCounterLeaderSuccess() throws Exception {
        Pair<Request<CreateCounterRequest>, GameEntity> pair = testCheckGame(boardService::createCounter, "createCounter");
        Request<CreateCounterRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        request.setRequest(new CreateCounterRequest());
        request.getRequest().setProvince("IdF");
        request.getRequest().setType(CounterFaceTypeEnum.LEADER);
        request.getRequest().setCountry("FRA");
        request.getRequest().setCode("Napo");

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setId(257L);
        idf.setName("IdF");

        AbstractBack.TABLES = new Tables();
        Leader leader = new Leader();
        leader.setCode("Napo");
        leader.setCountry("FRA");
        AbstractBack.TABLES.getLeaders().add(leader);

        when(countryDao.getCountryByName("FRA")).thenReturn(new CountryEntity());
        when(provinceDao.getProvinceByName("IdF")).thenReturn(idf);

        DiffEntity diffCreate = DiffUtil.createDiff(game, DiffTypeEnum.ADD, DiffTypeObjectEnum.COUNTER);
        when(counterDomain.createLeader(CounterFaceTypeEnum.LEADER, "Napo", "FRA", null, "IdF", game)).thenReturn(diffCreate);

        simulateDiff();

        boardService.createCounter(request);

        DiffEntity diff = retrieveDiffCreated();

        Assert.assertEquals(diff, diffCreate);
    }

    @Test
    public void testRemoveCounterFailSimple() {
        Pair<Request<RemoveCounterRequest>, GameEntity> pair = testCheckGame(boardService::removeCounter, "removeCounter");
        Request<RemoveCounterRequest> request = pair.getLeft();

        try {
            boardService.removeCounter(request);
            Assert.fail("Should break because removeCounter.request.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("removeCounter.request", e.getParams()[0]);
        }

        request.setRequest(new RemoveCounterRequest());

        try {
            boardService.removeCounter(request);
            Assert.fail("Should break because removeCounter.request.idCounter is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("removeCounter.request.idCounter", e.getParams()[0]);
        }

        request.getRequest().setIdCounter(25L);

        try {
            boardService.removeCounter(request);
            Assert.fail("Should break because counter does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("removeCounter.request.idCounter", e.getParams()[0]);
        }
    }

    @Test
    public void testRemoveCounterNoChange() throws Exception {
        testRemoveCounterSuccess("france", "france", "Napo", "Napo", false);
    }

    @Test
    public void testRemoveCounterNoChangeStackDestroyed() throws Exception {
        testRemoveCounterSuccess("france", "france", "Napo", "Napo", true);
    }

    @Test
    public void testRemoveCounterControllerChange() throws Exception {
        testRemoveCounterSuccess("france", "espagne", null, null, false);
    }

    @Test
    public void testRemoveCounterControllerChangeStackDestroyed() throws Exception {
        testRemoveCounterSuccess("france", "espagne", null, null, true);
    }

    @Test
    public void testRemoveCounterLeaderChange() throws Exception {
        testRemoveCounterSuccess("france", "france", "Napo", "Nabo", false);
    }

    @Test
    public void testRemoveCounterLeaderChangeStackDestroyed() throws Exception {
        testRemoveCounterSuccess("france", "france", "Napo", "Nabo", true);
    }

    @Test
    public void testRemoveCounterControllerAndLeaderChange() throws Exception {
        testRemoveCounterSuccess("france", "espagne", "Napo", "Draco", false);
    }

    @Test
    public void testRemoveCounterControllerAndLeaderChangeStackDestroyed() throws Exception {
        testRemoveCounterSuccess("france", "espagne", "Napo", "Draco", true);
    }

    private void testRemoveCounterSuccess(String controllerBefore, String controllerAfter, String leaderBefore, String leaderAfter, boolean noStackAfter) throws Exception {
        Pair<Request<RemoveCounterRequest>, GameEntity> pair = testCheckGame(boardService::removeCounter, "removeCounter");
        Request<RemoveCounterRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        request.setRequest(new RemoveCounterRequest());
        request.getRequest().setIdCounter(25L);
        StackEntity stack = new StackEntity();
        stack.setId(24L);
        stack.setProvince("pecs");
        EuropeanProvinceEntity pecs = new EuropeanProvinceEntity();
        pecs.setName("pecs");
        when(provinceDao.getProvinceByName("pecs")).thenReturn(pecs);
        stack.setCountry(controllerBefore);
        stack.setLeader(leaderBefore);
        stack.setGame(noStackAfter ? null : game);
        game.getStacks().add(stack);
        CounterEntity counter = createCounter(25L, "france", CounterFaceTypeEnum.ARMY_MINUS, stack);
        stack.getCounters().add(counter);

        DiffEntity diffRemove = DiffUtil.createDiff(game, DiffTypeEnum.REMOVE, DiffTypeObjectEnum.COUNTER);
        when(counterDomain.removeCounter(counter)).thenReturn(diffRemove);
        when(oeUtil.getController(stack)).thenReturn(controllerAfter);
        when(oeUtil.getLeader(any(), any(), any())).thenReturn(leaderAfter);

        simulateDiff();

        boardService.removeCounter(request);

        List<DiffEntity> diffs = retrieveDiffsCreated();

        boolean stackChangeController = !StringUtils.equals(controllerBefore, controllerAfter) && !noStackAfter;
        boolean leaderChange = !StringUtils.equals(leaderBefore, leaderAfter) && !noStackAfter;
        Assert.assertEquals(stackChangeController ? 2 : 1, diffs.size());

        DiffEntity diff = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.REMOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER)
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diff);
        Assert.assertEquals(diff, diffRemove);

        diff = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.STACK
                        && Objects.equals(d.getIdObject(), stack.getId()))
                .findAny()
                .orElse(null);
        if (stackChangeController) {
            Assert.assertNotNull(diff);
            Assert.assertEquals(controllerAfter, getAttribute(diff, DiffAttributeTypeEnum.COUNTRY));
            DiffAttributesEntity attribute = diff.getAttributes().stream()
                    .filter(attr -> attr.getType() == DiffAttributeTypeEnum.LEADER)
                    .findAny()
                    .orElse(null);
            if (leaderChange) {
                Assert.assertNotNull(attribute);
                Assert.assertEquals(leaderAfter, attribute.getValue());
            } else {
                Assert.assertNull(attribute);
            }
        } else {
            Assert.assertNull(diff);
        }
    }
}
