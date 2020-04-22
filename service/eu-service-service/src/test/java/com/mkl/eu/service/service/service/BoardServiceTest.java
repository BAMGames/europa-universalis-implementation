package com.mkl.eu.service.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.vo.AuthentInfo;
import com.mkl.eu.client.common.vo.GameInfo;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IBoardService;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.board.*;
import com.mkl.eu.client.service.service.common.ValidateRequest;
import com.mkl.eu.client.service.util.GameUtil;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.tables.*;
import com.mkl.eu.service.service.domain.IStatusWorkflowDomain;
import com.mkl.eu.service.service.domain.impl.CounterDomainImpl;
import com.mkl.eu.service.service.mapping.GameMapping;
import com.mkl.eu.service.service.mapping.chat.ChatMapping;
import com.mkl.eu.service.service.persistence.attrition.IAttritionDao;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.board.IStackDao;
import com.mkl.eu.service.service.persistence.chat.IChatDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.attrition.AttritionCounterEntity;
import com.mkl.eu.service.service.persistence.oe.attrition.AttritionEntity;
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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.Matchers.*;
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
    private IAttritionDao attritionDao;

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
        game.setTurn(22);
        StackEntity stack = new StackEntity();
        stack.setProvince("pecs");
        if (!firstMove) {
            stack.setMovePhase(MovePhaseEnum.IS_MOVING);
            AttritionEntity attrition = new AttritionEntity();
            attrition.setId(666L);
            attrition.setStatus(AttritionStatusEnum.ON_GOING);
            attrition.getProvinces().add("pecs");
            game.getAttritions().add(attrition);
        }
        stack.setId(13L);
        stack.setCountry("france");
        stack.getCounters().add(createCounter(14L, "france", CounterFaceTypeEnum.ARMY_MINUS, stack));
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
        when(oeUtil.getAllies(game.getCountries().get(0), game)).thenReturn(Collections.singletonList(game.getCountries().get(0).getName()));
        when(oeUtil.getController(idf, game)).thenReturn(game.getCountries().get(0).getName());
        when(oeUtil.getMovePoints(pecs, idf, true)).thenReturn(4);
        when(counterDao.getPatrons("france", game.getId())).thenReturn(Collections.singletonList("france"));
        when(attritionDao.create(any())).thenAnswer(invocationOnMock -> {
            AttritionEntity attrition = invocationOnMock.getArgumentAt(0, AttritionEntity.class);
            if (attrition != null) {
                attrition.setId(667L);
            }
            return attrition;
        });

        simulateDiff();

        boardService.moveStack(request);

        List<DiffEntity> diffs = retrieveDiffsCreated();

        Assert.assertEquals(2, diffs.size());
        DiffEntity diffMove = diffs.stream()
                .filter(diff -> diff.getType() == DiffTypeEnum.MOVE && diff.getTypeObject() == DiffTypeObjectEnum.STACK)
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diffMove);
        Assert.assertEquals(13L, diffMove.getIdObject().longValue());
        if (firstMove) {
            Assert.assertEquals(4, diffMove.getAttributes().size());
        } else {
            Assert.assertEquals(3, diffMove.getAttributes().size());
        }
        Assert.assertEquals(pecs.getName(), getAttribute(diffMove, DiffAttributeTypeEnum.PROVINCE_FROM));
        Assert.assertEquals(idf.getName(), getAttribute(diffMove, DiffAttributeTypeEnum.PROVINCE_TO));
        Assert.assertEquals("4", getAttribute(diffMove, DiffAttributeTypeEnum.MOVE_POINTS));
        if (firstMove) {
            Assert.assertEquals(MovePhaseEnum.IS_MOVING.name(), getAttribute(diffMove, DiffAttributeTypeEnum.MOVE_PHASE));
        } else {
            Assert.assertNull(getAttributeFull(diffMove, DiffAttributeTypeEnum.MOVE_PHASE));
        }

        if (firstMove) {
            DiffEntity diffAttrition = diffs.stream()
                    .filter(diff -> diff.getType() == DiffTypeEnum.ADD && diff.getTypeObject() == DiffTypeObjectEnum.ATTRITION)
                    .findAny()
                    .orElse(null);
            Assert.assertNotNull(diffAttrition);
            Assert.assertEquals(667L, diffAttrition.getIdObject().longValue());
            Assert.assertEquals(game.getTurn().toString(), getAttribute(diffAttrition, DiffAttributeTypeEnum.TURN));
            Assert.assertEquals(AttritionTypeEnum.MOVEMENT.name(), getAttribute(diffAttrition, DiffAttributeTypeEnum.TYPE));
            Assert.assertEquals(AttritionStatusEnum.ON_GOING.name(), getAttribute(diffAttrition, DiffAttributeTypeEnum.STATUS));
            Assert.assertEquals(Double.toString(2d), getAttribute(diffAttrition, DiffAttributeTypeEnum.SIZE));
            Assert.assertEquals("14", getAttribute(diffAttrition, DiffAttributeTypeEnum.COUNTER));
            List<String> provinces = diffAttrition.getAttributes().stream()
                    .filter(attr -> attr.getType() == DiffAttributeTypeEnum.PROVINCE)
                    .map(DiffAttributesEntity::getValue)
                    .collect(Collectors.toList());
            Assert.assertEquals(2, provinces.size());
            Assert.assertTrue(provinces.contains(idf.getName()));
            Assert.assertTrue(provinces.contains(pecs.getName()));
        } else {
            DiffEntity diffAttrition = diffs.stream()
                    .filter(diff -> diff.getType() == DiffTypeEnum.MODIFY && diff.getTypeObject() == DiffTypeObjectEnum.ATTRITION)
                    .findAny()
                    .orElse(null);
            Assert.assertNotNull(diffAttrition);
            Assert.assertEquals(666L, diffAttrition.getIdObject().longValue());
            List<String> provinces = diffAttrition.getAttributes().stream()
                    .filter(attr -> attr.getType() == DiffAttributeTypeEnum.PROVINCE)
                    .map(DiffAttributesEntity::getValue)
                    .collect(Collectors.toList());
            Assert.assertEquals(1, provinces.size());
            Assert.assertTrue(provinces.contains(idf.getName()));
        }
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
    public void testEndMoveStack() throws FunctionalException {
        EndMoveStackBuilder.create()
                .provinceController(Camp.SELF)
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.MOVED));
        EndMoveStackBuilder.create()
                .provinceController(Camp.ALLY)
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.MOVED));
        EndMoveStackBuilder.create()
                .provinceController(Camp.NEUTRAL)
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.MOVED));

        EndMoveStackBuilder.create()
                .provinceController(Camp.SELF).enemyUnitInProvince()
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.FIGHTING));
        EndMoveStackBuilder.create()
                .provinceController(Camp.ALLY).enemyUnitInProvince()
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.FIGHTING));
        EndMoveStackBuilder.create()
                .provinceController(Camp.NEUTRAL).enemyUnitInProvince()
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.FIGHTING));

        EndMoveStackBuilder.create()
                .provinceController(Camp.ENEMY)
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.BESIEGING));

        EndMoveStackBuilder.create()
                .provinceController(Camp.ENEMY).enemyUnitInProvince()
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.FIGHTING));

        EndMoveStackBuilder.create()
                .provinceController(Camp.NEUTRAL).attritionStatus(AttritionStatusEnum.ON_GOING)
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.MOVED)
                        .attritionModified().attritionStatus(AttritionStatusEnum.DONE).attritionNoCheck());

        // No attrition cause
        EndMoveStackBuilder.create()
                .provinceController(Camp.NEUTRAL).attritionStatus(AttritionStatusEnum.ON_GOING)
                .stackMovePoints(5)
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.MOVED)
                        .attritionModified().attritionStatus(AttritionStatusEnum.DONE).attritionNoCheck());

        // Various attrition causes
        EndMoveStackBuilder.create()
                .provinceController(Camp.NEUTRAL).attritionStatus(AttritionStatusEnum.ON_GOING)
                .stackMovePoints(5).badWeather()
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.MOVED)
                        .attritionModified().attritionStatus(AttritionStatusEnum.DONE).attritionBonus(0).attritionTech());
        EndMoveStackBuilder.create()
                .provinceController(Camp.NEUTRAL).attritionStatus(AttritionStatusEnum.ON_GOING)
                .stackMovePoints(8)
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.MOVED)
                        .attritionModified().attritionStatus(AttritionStatusEnum.DONE).attritionBonus(0).attritionTech());
        EndMoveStackBuilder.create()
                .provinceController(Camp.NEUTRAL).attritionStatus(AttritionStatusEnum.ON_GOING)
                .stackMovePoints(5).attritionSize(6)
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.MOVED)
                        .attritionModified().attritionStatus(AttritionStatusEnum.DONE).attritionBonus(0).attritionTech());
        EndMoveStackBuilder.create()
                .provinceController(Camp.NEUTRAL).attritionStatus(AttritionStatusEnum.ON_GOING)
                .stackMovePoints(8).badWeather()
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.MOVED)
                        .attritionModified().attritionStatus(AttritionStatusEnum.DONE).attritionBonus(2).attritionTech());
        EndMoveStackBuilder.create()
                .provinceController(Camp.NEUTRAL).attritionStatus(AttritionStatusEnum.ON_GOING)
                .stackMovePoints(8).badWeather().attritionSize(6)
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.MOVED)
                        .attritionModified().attritionStatus(AttritionStatusEnum.DONE).attritionBonus(4).attritionTech());

        // Various modifiers
        EndMoveStackBuilder.create()
                .provinceController(Camp.NEUTRAL).attritionStatus(AttritionStatusEnum.ON_GOING)
                .stackMovePoints(8).stackLeaderManoeuvre(2)
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.MOVED)
                        .attritionModified().attritionStatus(AttritionStatusEnum.DONE).attritionBonus(-2).attritionTech());
        EndMoveStackBuilder.create()
                .provinceController(Camp.NEUTRAL).attritionStatus(AttritionStatusEnum.ON_GOING)
                .stackMovePoints(8).attritionColdAreaPenalty(3)
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.MOVED)
                        .attritionModified().attritionStatus(AttritionStatusEnum.DONE).attritionBonus(3).attritionTech());
        EndMoveStackBuilder.create()
                .provinceController(Camp.NEUTRAL).attritionStatus(AttritionStatusEnum.ON_GOING)
                .stackMovePoints(8)
                .addProvince(EndMoveStackProvinceBuilder.create().name("pecs").addCounter(CounterFaceTypeEnum.PILLAGE_MINUS).inAttrition())
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.MOVED)
                        .attritionModified().attritionStatus(AttritionStatusEnum.DONE).attritionBonus(1).attritionTech());
        EndMoveStackBuilder.create()
                .provinceController(Camp.NEUTRAL).attritionStatus(AttritionStatusEnum.ON_GOING)
                .stackMovePoints(8)
                .addProvince(EndMoveStackProvinceBuilder.create().name("pecs").addCounter(CounterFaceTypeEnum.REVOLT_PLUS).inAttrition())
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.MOVED)
                        .attritionModified().attritionStatus(AttritionStatusEnum.DONE).attritionBonus(2).attritionTech());
        EndMoveStackBuilder.create()
                .provinceController(Camp.NEUTRAL).attritionStatus(AttritionStatusEnum.ON_GOING)
                .stackMovePoints(8)
                .addProvince(EndMoveStackProvinceBuilder.create().name("pecs").addCounter(CounterFaceTypeEnum.REVOLT_PLUS))
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.MOVED)
                        .attritionModified().attritionStatus(AttritionStatusEnum.DONE).attritionBonus(0).attritionTech());

        // Every causes and modifiers
        EndMoveStackBuilder.create()
                .provinceController(Camp.NEUTRAL).attritionStatus(AttritionStatusEnum.ON_GOING)
                .stackMovePoints(8).badWeather().attritionSize(6).stackLeaderManoeuvre(2).attritionColdAreaPenalty(3)
                .addProvince(EndMoveStackProvinceBuilder.create().name("pecs").addCounter(CounterFaceTypeEnum.REVOLT_MINUS).inAttrition())
                .addProvince(EndMoveStackProvinceBuilder.create().name("idf").addCounter(CounterFaceTypeEnum.REVOLT_PLUS))
                .addProvince(EndMoveStackProvinceBuilder.create().name("hanovre").addCounter(CounterFaceTypeEnum.PILLAGE_PLUS).inAttrition())
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.MOVED)
                        .attritionModified().attritionStatus(AttritionStatusEnum.DONE).attritionBonus(8).attritionTech());

        // Result of attrition in Land Europe
        EndMoveStackBuilder.create()
                .provinceController(Camp.NEUTRAL).attritionStatus(AttritionStatusEnum.ON_GOING)
                .stackMovePoints(8).attritionLossFlat(1)
                .addProvince(EndMoveStackProvinceBuilder.create().inAttrition())
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.MOVED)
                        .attritionModified().attritionStatus(AttritionStatusEnum.CHOOSE_LOSS)
                        .attritionBonus(0).attritionTech());

        // Result of other attrition
        EndMoveStackBuilder.create()
                .provinceController(Camp.NEUTRAL).attritionStatus(AttritionStatusEnum.ON_GOING)
                .stackMovePoints(8).attritionSize(3).attritionLossPercentage(10)
                .addProvince(EndMoveStackProvinceBuilder.create().inAttrition())
                .addProvince(EndMoveStackProvinceBuilder.create().rotw().inAttrition())
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.MOVED)
                        .attritionModified().attritionStatus(AttritionStatusEnum.CHOOSE_LOSS)
                        .attritionBonus(0));
        EndMoveStackBuilder.create()
                .provinceController(Camp.NEUTRAL).attritionStatus(AttritionStatusEnum.ON_GOING)
                .stackMovePoints(8).attritionSize(1).attritionLossPercentage(10).additionalLoss()
                .addProvince(EndMoveStackProvinceBuilder.create().inAttrition())
                .addProvince(EndMoveStackProvinceBuilder.create().rotw().inAttrition())
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.MOVED)
                        .attritionModified().attritionStatus(AttritionStatusEnum.CHOOSE_LOSS)
                        .attritionBonus(0).secondaryDieNeeded());
        EndMoveStackBuilder.create()
                .provinceController(Camp.NEUTRAL).attritionStatus(AttritionStatusEnum.ON_GOING)
                .stackMovePoints(8).attritionSize(1).attritionLossPercentage(10)
                .addProvince(EndMoveStackProvinceBuilder.create().inAttrition())
                .addProvince(EndMoveStackProvinceBuilder.create().rotw().inAttrition())
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.MOVED)
                        .attritionModified().attritionStatus(AttritionStatusEnum.DONE)
                        .attritionBonus(0).secondaryDieNeeded());
        EndMoveStackBuilder.create()
                .provinceController(Camp.NEUTRAL).attritionStatus(AttritionStatusEnum.ON_GOING)
                .stackMovePoints(8).attritionSize(1).attritionLossPercentage(80)
                .addProvince(EndMoveStackProvinceBuilder.create().inAttrition())
                .addProvince(EndMoveStackProvinceBuilder.create().rotw().inAttrition())
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.MOVED)
                        .attritionModified().attritionStatus(AttritionStatusEnum.CHOOSE_LOSS)
                        .attritionBonus(0).secondaryDieNeeded());
        EndMoveStackBuilder.create()
                .provinceController(Camp.NEUTRAL).attritionStatus(AttritionStatusEnum.ON_GOING)
                .stackMovePoints(8).attritionSize(1).attritionLossPercentage(80).additionalLoss()
                .addProvince(EndMoveStackProvinceBuilder.create().inAttrition())
                .addProvince(EndMoveStackProvinceBuilder.create().rotw().inAttrition())
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.MOVED)
                        .attritionModified().attritionStatus(AttritionStatusEnum.DONE)
                        .attritionBonus(0).secondaryDieNeeded().stackWiped());
        EndMoveStackBuilder.create()
                .provinceController(Camp.NEUTRAL).attritionStatus(AttritionStatusEnum.ON_GOING)
                .stackMovePoints(8).attritionSize(1).attritionLossPercentage(90)
                .addProvince(EndMoveStackProvinceBuilder.create().inAttrition())
                .addProvince(EndMoveStackProvinceBuilder.create().rotw().inAttrition())
                .whenEndMovePhase(boardService, this)
                .thenExpect(EndMoveStackResultBuilder.create().movePhase(MovePhaseEnum.MOVED)
                        .attritionModified().attritionStatus(AttritionStatusEnum.DONE)
                        .attritionBonus(0).stackWiped());

    }

    static class EndMoveStackBuilder {
        Camp provinceController;
        boolean enemyUnitInProvince;
        AttritionStatusEnum attritionStatus;
        int attritionSize;
        int stackMovePoints;
        Integer stackLeaderManoeuvre;
        boolean badWeather;
        int attritionColdAreaPenalty;
        int attritionLossPercentage;
        int attritionLossFlat;
        boolean additionalLoss;
        List<EndMoveStackProvinceBuilder> provinces = new ArrayList<>();
        List<DiffEntity> diffs;

        static EndMoveStackBuilder create() {
            return new EndMoveStackBuilder();
        }

        EndMoveStackBuilder provinceController(Camp provinceController) {
            this.provinceController = provinceController;
            return this;
        }

        EndMoveStackBuilder enemyUnitInProvince() {
            this.enemyUnitInProvince = true;
            return this;
        }

        EndMoveStackBuilder attritionStatus(AttritionStatusEnum attritionStatus) {
            this.attritionStatus = attritionStatus;
            return this;
        }

        EndMoveStackBuilder attritionSize(int attritionSize) {
            this.attritionSize = attritionSize;
            return this;
        }

        EndMoveStackBuilder stackMovePoints(int stackMovePoints) {
            this.stackMovePoints = stackMovePoints;
            return this;
        }

        EndMoveStackBuilder stackLeaderManoeuvre(Integer stackLeaderManoeuvre) {
            this.stackLeaderManoeuvre = stackLeaderManoeuvre;
            return this;
        }

        EndMoveStackBuilder attritionColdAreaPenalty(int attritionColdAreaPenalty) {
            this.attritionColdAreaPenalty = attritionColdAreaPenalty;
            return this;
        }

        EndMoveStackBuilder attritionLossPercentage(int attritionLossPercentage) {
            this.attritionLossPercentage = attritionLossPercentage;
            return this;
        }

        EndMoveStackBuilder attritionLossFlat(int attritionLossFlat) {
            this.attritionLossFlat = attritionLossFlat;
            return this;
        }

        EndMoveStackBuilder additionalLoss() {
            this.additionalLoss = true;
            return this;
        }

        EndMoveStackBuilder badWeather() {
            this.badWeather = true;
            return this;
        }

        EndMoveStackBuilder addProvince(EndMoveStackProvinceBuilder province) {
            this.provinces.add(province);
            return this;
        }

        EndMoveStackBuilder whenEndMovePhase(IBoardService boardService, BoardServiceTest testClass) throws FunctionalException {
            Request<EndMoveStackRequest> request = new Request<>();
            request.setRequest(new EndMoveStackRequest());
            request.setAuthent(new AuthentInfo());
            request.setGame(new GameInfo());
            request.getGame().setIdCountry(26L);
            request.getGame().setIdGame(12L);
            request.getGame().setVersionGame(1L);
            request.getRequest().setIdStack(13L);
            Tables tables = new Tables();

            GameEntity game = testClass.createGameUsingMocks(GameStatusEnum.MILITARY_MOVE, 26L);
            StackEntity stack = new StackEntity();
            stack.setProvince("pecs");
            stack.setMovePhase(MovePhaseEnum.IS_MOVING);
            stack.setCountry(Camp.SELF.name);
            stack.setId(13L);
            stack.getCounters().add(createCounter(21L, Camp.SELF.name, CounterFaceTypeEnum.ARMY_MINUS, stack));
            stack.setMove(stackMovePoints);
            if (stackLeaderManoeuvre != null) {
                stack.setLeader("Napo");
                stack.setCountry(Camp.SELF.name);
                AbstractGameServiceTest.createLeader(LeaderBuilder.create().code("Napo").country(Camp.SELF.name).stats("A" + stackLeaderManoeuvre + "55"), tables, stack);
            }
            stack.setGame(game);
            game.getStacks().add(stack);
            StackEntity otherStack = new StackEntity();
            otherStack.setProvince("pecs");
            otherStack.setId(14L);
            otherStack.getCounters().add(new CounterEntity());
            otherStack.getCounters().get(0).setType(CounterFaceTypeEnum.ARMY_MINUS);
            if (enemyUnitInProvince) {
                otherStack.setCountry(Camp.ENEMY.name);
                otherStack.getCounters().get(0).setCountry(Camp.ENEMY.name);
            } else {
                otherStack.setCountry(Camp.NEUTRAL.name);
                otherStack.getCounters().get(0).setCountry(Camp.NEUTRAL.name);
            }
            game.getStacks().add(otherStack);
            game.getCountries().add(new PlayableCountryEntity());
            game.getCountries().get(0).setId(26L);
            game.getCountries().get(0).setName(Camp.SELF.name);

            AttritionEntity noiseAttrition = new AttritionEntity();
            noiseAttrition.setId(65L);
            noiseAttrition.setType(AttritionTypeEnum.SIEGE);
            noiseAttrition.setStatus(AttritionStatusEnum.ON_GOING);
            game.getAttritions().add(noiseAttrition);

            AttritionEntity attrition = new AttritionEntity();
            attrition.setId(67L);
            attrition.setType(AttritionTypeEnum.MOVEMENT);
            attrition.setStatus(attritionStatus);
            attrition.setSize(attritionSize);
            AttritionCounterEntity attritionCounter = new AttritionCounterEntity();
            attritionCounter.setCounter(21L);
            attrition.getCounters().add(attritionCounter);
            attrition.getProvinces().addAll(provinces.stream()
                    .filter(prov -> prov.inAttrition)
                    .map(prov -> prov.name)
                    .collect(Collectors.toSet()));
            game.getAttritions().add(attrition);
            provinces.stream()
                    .filter(prov -> CollectionUtils.isNotEmpty(prov.counters))
                    .forEach(prov -> {
                        StackEntity loopStack = new StackEntity();
                        loopStack.setProvince(prov.name);
                        game.getStacks().add(loopStack);
                        prov.counters.forEach(counter -> loopStack.getCounters().add(createCounter(null, null, counter, loopStack)));
                    });

            AttritionOther attritionOther = new AttritionOther();
            attritionOther.setLossPercentage(attritionLossPercentage);
            attritionOther.setDice(5);
            tables.getAttritionsOther().add(attritionOther);
            AttritionLandEurope attritionLandEurope = new AttritionLandEurope();
            attritionLandEurope.setLoss(attritionLossFlat);
            attritionLandEurope.setMinSize(0);
            attritionLandEurope.setMaxSize(66);
            attritionLandEurope.setDice(5);
            tables.getAttritionsLandEurope().add(attritionLandEurope);
            AbstractBack.TABLES = tables;

            when(testClass.oeUtil.isBadWeather(game)).thenReturn(badWeather);
            when(testClass.oeUtil.isMobile(stack)).thenReturn(true);
            when(testClass.oeUtil.getEnemies(game.getCountries().get(0), game)).thenReturn(Collections.singletonList(Camp.ENEMY.name));
            EuropeanProvinceEntity province = new EuropeanProvinceEntity();
            when(testClass.provinceDao.getProvinceByName(stack.getProvince())).thenReturn(province);
            when(testClass.oeUtil.getController(province, game)).thenReturn(provinceController.name);
            when(testClass.counterDao.getPatrons(Camp.SELF.name, game.getId())).thenReturn(Collections.singletonList(Camp.SELF.name));
            provinces.forEach(prov -> when(testClass.oeUtil.isRotwProvince(prov.name, game)).thenReturn(prov.rotw));
            when(testClass.counterDao.getColdAreaPenaltyRotw(any(), any(), any())).thenReturn(attritionColdAreaPenalty);
            when(testClass.oeUtil.getTechnology(any(), anyBoolean(), any(), any(), any())).thenReturn(Tech.RENAISSANCE);
            when(testClass.counterDomain.removeCounter(any())).thenAnswer(AbstractGameServiceTest.removeCounterAnswer());
            if (additionalLoss) {
                when(testClass.oeUtil.rollDie(game, Camp.SELF.name)).thenReturn(5, 2);
            } else {
                when(testClass.oeUtil.rollDie(game, Camp.SELF.name)).thenReturn(5, 3);
            }

            testClass.simulateDiff();

            boardService.endMoveStack(request);

            diffs = testClass.retrieveDiffsCreated();

            return this;
        }

        EndMoveStackBuilder thenExpect(EndMoveStackResultBuilder result) {
            int nbDiffs = 0;
            DiffEntity diff = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.STACK
                            && Objects.equals(d.getIdObject(), 13L))
                    .findAny()
                    .orElse(null);
            if (!result.stackWiped) {
                Assert.assertNotNull("Modify stack diff not sent.", diff);

                Assert.assertEquals("Incorrect number of attributes of modify stack diff.", 1, diff.getAttributes().size());
                Assert.assertEquals("Move Phase attribute of the modify stack diff is wrong.",
                        result.movePhase.name(), getAttribute(diff, DiffAttributeTypeEnum.MOVE_PHASE));
                nbDiffs++;
            } else {
                Assert.assertNull("Modify stack diff sent while stack is wiped.", diff);
            }

            diff = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.ATTRITION
                            && Objects.equals(d.getIdObject(), 67L))
                    .findAny()
                    .orElse(null);
            if (result.attritionModified) {
                Assert.assertNotNull("Modify attrition diff not sent.", diff);
                Assert.assertEquals("Status attribute of the modify attrition diff is wrong.",
                        result.attritionStatus.name(), getAttribute(diff, DiffAttributeTypeEnum.STATUS));

                if (result.attritionNoCheck) {
                    Assert.assertNull("Bonus attribute of the modify attrition diff should not be sent.",
                            getAttributeFull(diff, DiffAttributeTypeEnum.BONUS));
                    Assert.assertNull("Die attribute of the modify attrition diff should not be sent.",
                            getAttributeFull(diff, DiffAttributeTypeEnum.DIE));
                    Assert.assertNull("Secondary die attribute of the modify attrition diff should not be sent.",
                            getAttributeFull(diff, DiffAttributeTypeEnum.SECONDARY_DIE));
                    Assert.assertNull("Tech attribute of the modify attrition diff should not be sent.",
                            getAttributeFull(diff, DiffAttributeTypeEnum.TECH_LAND));
                } else {
                    Assert.assertEquals("Bonus attribute of the modify attrition diff is wrong.",
                            result.attritionBonus + "", getAttribute(diff, DiffAttributeTypeEnum.BONUS));
                    Assert.assertEquals("Die attribute of the modify attrition diff is wrong.",
                            "5", getAttribute(diff, DiffAttributeTypeEnum.DIE));
                    if (result.secondaryDieNeeded) {
                        Assert.assertEquals("Secondary die attribute of the modify attrition diff is wrong.",
                                additionalLoss ? "2" : "3", getAttribute(diff, DiffAttributeTypeEnum.SECONDARY_DIE));
                    } else {
                        Assert.assertNull("Secondary die attribute of the modify attrition diff should not be sent.",
                                getAttributeFull(diff, DiffAttributeTypeEnum.SECONDARY_DIE));
                    }
                    if (result.attritionTech) {
                        Assert.assertEquals("Tech attribute of the modify attrition diff is wrong.",
                                Tech.RENAISSANCE, getAttribute(diff, DiffAttributeTypeEnum.TECH_LAND));
                    } else {
                        Assert.assertNull("Tech attribute of the modify attrition diff should not be sent.",
                                getAttributeFull(diff, DiffAttributeTypeEnum.TECH_LAND));
                    }
                }
                nbDiffs++;
            } else {
                Assert.assertNull("Modify attrition diff should not be sent.", diff);
            }

            diff = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.REMOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER
                            && Objects.equals(d.getIdObject(), 21L))
                    .findAny()
                    .orElse(null);
            if (result.stackWiped) {
                Assert.assertNotNull("Remove counter diff not sent.", diff);
                nbDiffs++;
            } else {
                Assert.assertNull("Remove counter diff should not be sent.", diff);
            }

            diff = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.GAME)
                    .findAny()
                    .orElse(null);
            if (result.attritionStatus == AttritionStatusEnum.CHOOSE_LOSS) {
                Assert.assertNotNull("Modify game diff not sent.", diff);
                Assert.assertEquals("Status attribute of the modify game diff is wrong.", GameStatusEnum.ATTRITION.name(), getAttribute(diff, DiffAttributeTypeEnum.STATUS));
                nbDiffs++;
            } else {
                Assert.assertNull("Modify game diff should not be sent", diff);
            }

            Assert.assertEquals("Number of diffs received if wrong.", nbDiffs, diffs.size());

            return this;
        }
    }

    static class EndMoveStackProvinceBuilder {
        String name;
        boolean rotw;
        boolean inAttrition;
        List<CounterFaceTypeEnum> counters = new ArrayList<>();

        static EndMoveStackProvinceBuilder create() {
            return new EndMoveStackProvinceBuilder();
        }

        EndMoveStackProvinceBuilder name(String name) {
            this.name = name;
            return this;
        }

        EndMoveStackProvinceBuilder rotw() {
            this.rotw = true;
            return this;
        }

        EndMoveStackProvinceBuilder inAttrition() {
            this.inAttrition = true;
            return this;
        }

        EndMoveStackProvinceBuilder addCounter(CounterFaceTypeEnum counter) {
            this.counters.add(counter);
            return this;
        }
    }

    static class EndMoveStackResultBuilder {
        MovePhaseEnum movePhase;
        boolean attritionModified;
        boolean attritionNoCheck;
        int attritionBonus;
        boolean attritionTech;
        boolean secondaryDieNeeded;
        AttritionStatusEnum attritionStatus;
        boolean stackWiped;

        static EndMoveStackResultBuilder create() {
            return new EndMoveStackResultBuilder();
        }

        EndMoveStackResultBuilder movePhase(MovePhaseEnum movePhase) {
            this.movePhase = movePhase;
            return this;
        }

        EndMoveStackResultBuilder attritionModified() {
            this.attritionModified = true;
            return this;
        }

        EndMoveStackResultBuilder attritionNoCheck() {
            this.attritionNoCheck = true;
            return this;
        }

        EndMoveStackResultBuilder attritionBonus(int attritionBonus) {
            this.attritionBonus = attritionBonus;
            return this;
        }

        EndMoveStackResultBuilder attritionTech() {
            this.attritionTech = true;
            return this;
        }

        EndMoveStackResultBuilder secondaryDieNeeded() {
            this.secondaryDieNeeded = true;
            return this;
        }

        EndMoveStackResultBuilder attritionStatus(AttritionStatusEnum attritionStatus) {
            this.attritionStatus = attritionStatus;
            return this;
        }

        EndMoveStackResultBuilder stackWiped() {
            this.stackWiped = true;
            return this;
        }
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
        AbstractBack.TABLES = new Tables();

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
    public void testMoveCounterInNewStackSuccess() throws Exception {
        MoveCounterBuilder.create()
                .whenMoveCounter(this, boardService)
                .thenExpect(MoveCounterResultBuilder.create());
        MoveCounterBuilder.create()
                .inNewStack().oldStackDelete()
                .whenMoveCounter(this, boardService)
                .thenExpect(MoveCounterResultBuilder.create());
        MoveCounterBuilder.create()
                .stackFrom(MoveCounterStackBuilder.create().controllerBefore("france").controllerAfter("genes"))
                .stackTo(MoveCounterStackBuilder.create().controllerBefore("genes").controllerAfter("france"))
                .whenMoveCounter(this, boardService)
                .thenExpect(MoveCounterResultBuilder.create().stackFromChangeController().stackToChangeController());
        MoveCounterBuilder.create()
                .stackFrom(MoveCounterStackBuilder.create().leaderBefore("Napo").leaderAfter("Doria"))
                .stackTo(MoveCounterStackBuilder.create().leaderBefore("anon1").leaderAfter("anon2"))
                .whenMoveCounter(this, boardService)
                .thenExpect(MoveCounterResultBuilder.create().stackFromChangeLeader().stackToChangeLeader());
        MoveCounterBuilder.create()
                .stackFrom(MoveCounterStackBuilder.create().controllerBefore("france").controllerAfter("genes")
                        .leaderBefore("Napo").leaderAfter("Doria"))
                .stackTo(MoveCounterStackBuilder.create().controllerBefore("genes").controllerAfter("france")
                        .leaderBefore("anon1").leaderAfter("anon2"))
                .whenMoveCounter(this, boardService)
                .thenExpect(MoveCounterResultBuilder.create().stackFromChangeController().stackFromChangeLeader()
                        .stackToChangeController().stackToChangeLeader());
        MoveCounterBuilder.create()
                .attritionOnGoing().counterAlreadyInAttrition()
                .whenMoveCounter(this, boardService)
                .thenExpect(MoveCounterResultBuilder.create());
        MoveCounterBuilder.create()
                .attritionOnGoing()
                .whenMoveCounter(this, boardService)
                .thenExpect(MoveCounterResultBuilder.create().attritionCounterAdd());
        MoveCounterBuilder.create()
                .attritionOnGoing().counterAlreadyInAttrition().attritionSizeWillGrow()
                .whenMoveCounter(this, boardService)
                .thenExpect(MoveCounterResultBuilder.create().attritionChangeSize());
        MoveCounterBuilder.create()
                .attritionOnGoing().attritionSizeWillGrow()
                .whenMoveCounter(this, boardService)
                .thenExpect(MoveCounterResultBuilder.create().attritionCounterAdd().attritionChangeSize());
        MoveCounterBuilder.create()
                .attritionOnGoing()
                .stackFrom(MoveCounterStackBuilder.create().controllerBefore("france").controllerAfter("france")
                        .leaderBefore("Nabo").leaderAfter("Napo"))
                .stackTo(MoveCounterStackBuilder.create().controllerBefore("genes").controllerAfter("france")
                        .leaderBefore("anon1").leaderAfter("anon1"))
                .whenMoveCounter(this, boardService)
                .thenExpect(MoveCounterResultBuilder.create().stackFromChangeLeader().stackToChangeController()
                        .attritionCounterAdd());
    }

    static class MoveCounterBuilder {
        MoveCounterStackBuilder stackFrom = MoveCounterStackBuilder.create();
        MoveCounterStackBuilder stackTo = MoveCounterStackBuilder.create();
        boolean inNewStack;
        boolean oldStackDelete;
        boolean attritionOnGoing;
        boolean counterAlreadyInAttrition;
        boolean attritionSizeWillGrow;
        List<DiffEntity> diffs;

        static MoveCounterBuilder create() {
            return new MoveCounterBuilder();
        }

        MoveCounterBuilder stackFrom(MoveCounterStackBuilder stackFrom) {
            this.stackFrom = stackFrom;
            return this;
        }

        MoveCounterBuilder stackTo(MoveCounterStackBuilder stackTo) {
            this.stackTo = stackTo;
            return this;
        }

        MoveCounterBuilder inNewStack() {
            this.inNewStack = true;
            return this;
        }

        MoveCounterBuilder oldStackDelete() {
            this.oldStackDelete = true;
            return this;
        }

        MoveCounterBuilder attritionOnGoing() {
            this.attritionOnGoing = true;
            return this;
        }

        MoveCounterBuilder counterAlreadyInAttrition() {
            this.counterAlreadyInAttrition = true;
            return this;
        }

        MoveCounterBuilder attritionSizeWillGrow() {
            this.attritionSizeWillGrow = true;
            return this;
        }

        MoveCounterBuilder whenMoveCounter(BoardServiceTest testClass, IBoardService boardService) throws FunctionalException {
            Request<MoveCounterRequest> request = new Request<>();
            request.setRequest(new MoveCounterRequest());
            request.setAuthent(new AuthentInfo());
            request.getAuthent().setUsername("toto");
            request.setGame(new GameInfo());
            request.getGame().setIdCountry(26L);
            request.getGame().setIdGame(12L);
            request.getGame().setVersionGame(1L);
            request.getRequest().setIdCounter(13L);

            GameEntity game = testClass.createGameUsingMocks(GameStatusEnum.MILITARY_MOVE, 26L);

            StackEntity oldStack = new StackEntity();
            oldStack.setCountry(stackFrom.controllerBefore);
            oldStack.setLeader(stackFrom.leaderBefore);
            oldStack.setProvince("IdF");
            oldStack.setId(9L);
            oldStack.setMovePhase(MovePhaseEnum.IS_MOVING);
            CounterEntity counter = new CounterEntity();
            counter.setId(13L);
            counter.setCountry("france");
            counter.setType(CounterFaceTypeEnum.ARMY_MINUS);
            counter.setOwner(oldStack);
            oldStack.getCounters().add(counter);
            if (!oldStackDelete) {
                oldStack.getCounters().add(new CounterEntity());
            }
            game.getStacks().add(oldStack);
            game.getCountries().add(new PlayableCountryEntity());
            game.getCountries().get(0).setId(26L);
            game.getCountries().get(0).setName("france");

            StackEntity newStack = new StackEntity();
            newStack.setCountry(stackTo.controllerBefore);
            newStack.setLeader(stackTo.leaderBefore);
            newStack.setProvince("IdF");
            newStack.setId(25L);
            newStack.setMovePhase(MovePhaseEnum.IS_MOVING);
            if (!inNewStack) {
                game.getStacks().add(newStack);
                request.getRequest().setIdStack(newStack.getId());
            }

            AttritionEntity attrition = new AttritionEntity();
            attrition.setId(15L);
            if (attritionOnGoing) {
                attrition.setStatus(AttritionStatusEnum.ON_GOING);
            } else {
                attrition.setStatus(AttritionStatusEnum.DONE);
            }
            attrition.setType(AttritionTypeEnum.MOVEMENT);
            AttritionCounterEntity attritionCounter = new AttritionCounterEntity();
            attritionCounter.setAttrition(attrition);
            attritionCounter.setCounter(666L);
            attrition.getCounters().add(attritionCounter);
            if (counterAlreadyInAttrition) {
                attritionCounter = new AttritionCounterEntity();
                attritionCounter.setAttrition(attrition);
                attritionCounter.setCounter(counter.getId());
                attrition.getCounters().add(attritionCounter);
            }
            game.getAttritions().add(attrition);
            if (attritionSizeWillGrow) {
                attrition.setSize(0d);
            } else {
                attrition.setSize(8d);
            }

            Mockito.reset(testClass.counterDomain);
            when(testClass.counterDao.getCounter(13L, 12L)).thenReturn(counter);
            if (inNewStack) {
                when(testClass.counterDomain.createStack(any(), any(), any())).thenReturn(newStack);
            }
            when(testClass.oeUtil.getController(oldStack)).thenReturn(stackFrom.controllerAfter);
            when(testClass.oeUtil.getController(newStack)).thenReturn(stackTo.controllerAfter);
            when(testClass.oeUtil.getLeader(any(), any(), any())).thenReturn(stackFrom.leaderAfter, stackTo.leaderAfter);
            when(testClass.counterDomain.changeCounterOwner(any(), any(), any())).thenCallRealMethod();
            when(testClass.counterDao.getPatrons("france", game.getId())).thenReturn(Collections.singletonList("france"));
            when(testClass.provinceDao.getProvinceByName(any())).thenReturn(new EuropeanProvinceEntity());

            testClass.simulateDiff();

            boardService.moveCounter(request);

            diffs = testClass.retrieveDiffsCreated();

            return this;
        }

        MoveCounterBuilder thenExpect(MoveCounterResultBuilder result) {
            int nbDiffs = 1;
            DiffEntity diffEntity = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.MOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER
                            && Objects.equals(d.getIdObject(), 13L))
                    .findAny()
                    .orElse(null);
            Assert.assertNotNull(diffEntity);
            Assert.assertEquals(4 + (oldStackDelete ? 1 : 0), diffEntity.getAttributes().size());
            Assert.assertEquals("9", getAttribute(diffEntity, DiffAttributeTypeEnum.STACK_FROM));
            Assert.assertEquals("25", getAttribute(diffEntity, DiffAttributeTypeEnum.STACK_TO));
            Assert.assertEquals("IdF", getAttribute(diffEntity, DiffAttributeTypeEnum.PROVINCE_FROM));
            Assert.assertEquals("IdF", getAttribute(diffEntity, DiffAttributeTypeEnum.PROVINCE_TO));
            if (oldStackDelete) {
                Assert.assertEquals("9", getAttribute(diffEntity, DiffAttributeTypeEnum.STACK_DEL));
            } else {
                Assert.assertNull(getAttributeFull(diffEntity, DiffAttributeTypeEnum.STACK_DEL));
            }

            diffEntity = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.STACK
                            && Objects.equals(d.getIdObject(), 9L))
                    .findAny()
                    .orElse(null);
            if (result.stackFromChangeController || result.stackFromChangeLeader) {
                Assert.assertNotNull(diffEntity);
                if (result.stackFromChangeController) {
                    Assert.assertEquals(stackFrom.controllerAfter, getAttribute(diffEntity, DiffAttributeTypeEnum.COUNTRY));
                } else {
                    Assert.assertNull(getAttributeFull(diffEntity, DiffAttributeTypeEnum.COUNTRY));
                }
                if (result.stackFromChangeLeader) {
                    Assert.assertEquals(stackFrom.leaderAfter, getAttribute(diffEntity, DiffAttributeTypeEnum.LEADER));
                } else {
                    Assert.assertNull(getAttributeFull(diffEntity, DiffAttributeTypeEnum.LEADER));
                }
                nbDiffs++;
            } else {
                Assert.assertNull(diffEntity);
            }

            diffEntity = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.STACK
                            && Objects.equals(d.getIdObject(), 25L))
                    .findAny()
                    .orElse(null);
            if (result.stackToChangeController || result.stackToChangeLeader) {
                Assert.assertNotNull(diffEntity);
                if (result.stackToChangeController) {
                    Assert.assertEquals(stackTo.controllerAfter, getAttribute(diffEntity, DiffAttributeTypeEnum.COUNTRY));
                } else {
                    Assert.assertNull(getAttributeFull(diffEntity, DiffAttributeTypeEnum.COUNTRY));
                }
                if (result.stackToChangeLeader) {
                    Assert.assertEquals(stackTo.leaderAfter, getAttribute(diffEntity, DiffAttributeTypeEnum.LEADER));
                } else {
                    Assert.assertNull(getAttributeFull(diffEntity, DiffAttributeTypeEnum.LEADER));
                }
                nbDiffs++;
            } else {
                Assert.assertNull(diffEntity);
            }

            diffEntity = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.ATTRITION
                            && Objects.equals(d.getIdObject(), 15L))
                    .findAny()
                    .orElse(null);
            if (result.attritionCounterAdd || result.attritionChangeSize) {
                Assert.assertNotNull(diffEntity);
                if (result.attritionCounterAdd) {
                    Assert.assertEquals("13", getAttribute(diffEntity, DiffAttributeTypeEnum.COUNTER));
                } else {
                    Assert.assertNull(getAttributeFull(diffEntity, DiffAttributeTypeEnum.COUNTER));
                }
                if (result.attritionChangeSize) {
                    Assert.assertEquals(Double.toString(2d), getAttribute(diffEntity, DiffAttributeTypeEnum.SIZE));
                } else {
                    Assert.assertNull(getAttributeFull(diffEntity, DiffAttributeTypeEnum.SIZE));
                }
                nbDiffs++;
            } else {
                Assert.assertNull(diffEntity);
            }

            Assert.assertEquals(nbDiffs, diffs.size());

            return this;
        }
    }

    static class MoveCounterStackBuilder {
        String controllerBefore;
        String controllerAfter;
        String leaderBefore;
        String leaderAfter;

        static MoveCounterStackBuilder create() {
            return new MoveCounterStackBuilder();
        }

        MoveCounterStackBuilder controllerBefore(String controllerBefore) {
            this.controllerBefore = controllerBefore;
            return this;
        }

        MoveCounterStackBuilder controllerAfter(String controllerAfter) {
            this.controllerAfter = controllerAfter;
            return this;
        }

        MoveCounterStackBuilder leaderBefore(String leaderBefore) {
            this.leaderBefore = leaderBefore;
            return this;
        }

        MoveCounterStackBuilder leaderAfter(String leaderAfter) {
            this.leaderAfter = leaderAfter;
            return this;
        }
    }

    static class MoveCounterResultBuilder {
        boolean stackFromChangeController;
        boolean stackFromChangeLeader;
        boolean stackToChangeController;
        boolean stackToChangeLeader;
        boolean attritionCounterAdd;
        boolean attritionChangeSize;

        static MoveCounterResultBuilder create() {
            return new MoveCounterResultBuilder();
        }

        MoveCounterResultBuilder stackFromChangeController() {
            this.stackFromChangeController = true;
            return this;
        }

        MoveCounterResultBuilder stackFromChangeLeader() {
            this.stackFromChangeLeader = true;
            return this;
        }

        MoveCounterResultBuilder stackToChangeController() {
            this.stackToChangeController = true;
            return this;
        }

        MoveCounterResultBuilder stackToChangeLeader() {
            this.stackToChangeLeader = true;
            return this;
        }

        MoveCounterResultBuilder attritionCounterAdd() {
            this.attritionCounterAdd = true;
            return this;
        }

        MoveCounterResultBuilder attritionChangeSize() {
            this.attritionChangeSize = true;
            return this;
        }
    }

    @Test
    public void testMoveLeaderFail() {
        Pair<Request<MoveLeaderRequest>, GameEntity> pair = testCheckGame(boardService::moveLeader, "moveLeader");
        Request<MoveLeaderRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        testCheckStatus(pair.getRight(), request, boardService::moveLeader, "moveLeader", GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE, GameStatusEnum.MILITARY_HIERARCHY);
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
        stack.setProvince(GameUtil.ROUND_END);
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

        try {
            boardService.moveLeader(request);
            Assert.fail("Should break because leader is wounded");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.LEADER_WOUNDED, e.getCode());
            Assert.assertEquals("moveLeader.request.idCounter", e.getParams()[0]);
        }

        stack.setProvince("idf");
        game.setStatus(GameStatusEnum.MILITARY_HIERARCHY);

        try {
            boardService.moveLeader(request);
            Assert.fail("Should break because during military hierarchy, you can only placed leader on turn box");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.LEADER_WAS_NOT_WOUNDED, e.getCode());
            Assert.assertEquals("moveLeader.request.idCounter", e.getParams()[0]);
        }

        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
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
        testCheckStatus(pair.getRight(), request, boardService::moveLeader, "moveLeader", GameStatusEnum.MILITARY_HIERARCHY, GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
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
    public void testMoveLeaderHierarchy() throws FunctionalException {
        testMoveLeaderHierarchy(false, false);
    }

    @Test
    public void testMoveLeaderHierarchy2() throws FunctionalException {
        testMoveLeaderHierarchy(false, true);
    }

    @Test
    public void testMoveLeaderHierarchyLastLeader() throws FunctionalException {
        testMoveLeaderHierarchy(true, false);
    }

    @Test
    public void testMoveLeaderHierarchyLastLeaderLastCountry() throws FunctionalException {
        testMoveLeaderHierarchy(true, true);
    }

    private void testMoveLeaderHierarchy(boolean lastLeaderOfCountry, boolean lastCountryReady) throws FunctionalException {
        Pair<Request<MoveLeaderRequest>, GameEntity> pair = testCheckGame(boardService::moveLeader, "moveLeader");
        Request<MoveLeaderRequest> request = pair.getLeft();
        AbstractBack.TABLES = new Tables();
        GameEntity game = pair.getRight();
        game.setTurn(5);
        testCheckStatus(pair.getRight(), request, boardService::moveLeader, "moveLeader", GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE, GameStatusEnum.MILITARY_HIERARCHY);
        request.getGame().setIdCountry(26L);
        request.setRequest(new MoveLeaderRequest());
        request.getRequest().setIdCounter(13L);
        request.getRequest().setProvince("pecs");
        PlayableCountryEntity france = new PlayableCountryEntity();
        france.setId(26L);
        france.setName("france");
        france.setUsername("france");
        game.getCountries().add(france);
        PlayableCountryEntity spain = new PlayableCountryEntity();
        spain.setId(27L);
        spain.setName("spain");
        spain.setUsername("spain");
        if (lastCountryReady) {
            spain.setReady(true);
        }
        game.getCountries().add(spain);
        PlayableCountryEntity genes = new PlayableCountryEntity();
        genes.setId(28L);
        genes.setName("genes");
        game.getCountries().add(genes);
        StackEntity stack = new StackEntity();
        stack.setId(1L);
        stack.setProvince(GameUtil.getTurnBox(game.getTurn()));
        game.getStacks().add(stack);
        CounterEntity counter = createLeader(LeaderBuilder.create().id(13L).code("Napo").country("france").type(LeaderTypeEnum.GENERAL).stats("A666"), AbstractBack.TABLES, stack);
        stack.getCounters().add(counter);
        stack.getCounters().add(createLeader(LeaderBuilder.create().id(14L).code("Infante").country("spain").type(LeaderTypeEnum.GENERAL).stats("A666"), AbstractBack.TABLES, stack));
        if (!lastLeaderOfCountry) {
            stack.getCounters().add(createLeader(LeaderBuilder.create().id(15L).code("Nabo").country("genes").type(LeaderTypeEnum.GENERAL).stats("E111"), AbstractBack.TABLES, stack));
        }

        when(counterDao.getPatrons(counter.getCountry(), game.getId())).thenReturn(Arrays.asList("genes", france.getName()));
        when(counterDao.getMinors(france.getName(), game.getId())).thenReturn(Collections.singletonList("genes"));
        when(oeUtil.getAllies(france, game)).thenReturn(Arrays.asList("genes", france.getName()));
        AbstractProvinceEntity pecs = new EuropeanProvinceEntity();
        when(provinceDao.getProvinceByName("pecs")).thenReturn(pecs);
        when(oeUtil.getController(pecs, game)).thenReturn("genes");
        when(counterDomain.moveLeader(counter, null, "pecs", game)).thenAnswer(invocation -> {
            stack.getCounters().remove(counter);
            return Collections.singletonList(DiffUtil.createDiff(game, DiffTypeEnum.MOVE, DiffTypeObjectEnum.COUNTER, counter.getId()));
        });
        when(statusWorkflowDomain.endHierarchyPhase(any())).thenReturn(Collections.singletonList(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STATUS)));

        simulateDiff();

        boardService.moveLeader(request);

        List<DiffEntity> diffs = retrieveDiffsCreated();

        Assert.assertEquals(lastLeaderOfCountry ? 2 : 1, diffs.size());
        DiffEntity diff = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.MOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER &&
                        Objects.equals(d.getIdObject(), counter.getId()))
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diff);

        diff = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.VALIDATE && d.getTypeObject() == DiffTypeObjectEnum.STATUS &&
                        Objects.equals(d.getIdObject(), france.getId()))
                .findAny()
                .orElse(null);
        if (lastLeaderOfCountry && !lastCountryReady) {
            Assert.assertNotNull(diff);
            Assert.assertEquals("26", getAttribute(diff, DiffAttributeTypeEnum.ID_COUNTRY));
        } else {
            Assert.assertNull(diff);
        }

        diff = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.STATUS)
                .findAny()
                .orElse(null);
        if (lastLeaderOfCountry && lastCountryReady) {
            Assert.assertNotNull(diff);
        } else {
            Assert.assertNull(diff);
        }
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

        request.getAuthent().setUsername("MKL");
        StackEntity stack = new StackEntity();
        stack.setId(29L);
        stack.setMovePhase(MovePhaseEnum.IS_MOVING);
        game.getStacks().add(stack);

        try {
            boardService.validateMilitaryRound(request);
            Assert.fail("Should break because a stack is still moving");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.OTHER_STACK_MOVING, e.getCode());
            Assert.assertEquals("validateMilitaryRound.request.validate", e.getParams()[0]);
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
        Assert.assertEquals(stackChangeController || leaderChange ? 2 : 1, diffs.size());

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
        if (stackChangeController || leaderChange) {
            Assert.assertNotNull(diff);
            if (stackChangeController) {
                Assert.assertEquals(controllerAfter, getAttribute(diff, DiffAttributeTypeEnum.COUNTRY));
            } else {
                Assert.assertNull(getAttributeFull(diff, DiffAttributeTypeEnum.COUNTRY));
            }
            if (leaderChange) {
                Assert.assertEquals(leaderAfter, getAttribute(diff, DiffAttributeTypeEnum.LEADER));
            } else {
                Assert.assertNull(getAttributeFull(diff, DiffAttributeTypeEnum.LEADER));
            }
        } else {
            Assert.assertNull(diff);
        }
    }

    @Test
    public void testInitLeaders() throws FunctionalException {
        GameEntity game = createGameUsingMocks();
        Request<Void> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("toto");
        request.setGame(new GameInfo());
        request.getGame().setIdGame(game.getId());
        request.getGame().setVersionGame(game.getVersion());

        when(statusWorkflowDomain.deployLeaders(game)).thenReturn(Collections.singletonList(DiffUtil.createDiff(game, DiffTypeEnum.ADD, DiffTypeObjectEnum.COUNTER)));

        simulateDiff();

        boardService.initLeaders(request);

        List<DiffEntity> diffs = retrieveDiffsCreated();

        Assert.assertEquals(1, diffs.size());

        DiffEntity diff = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.ADD && d.getTypeObject() == DiffTypeObjectEnum.COUNTER)
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diff);
    }
}
