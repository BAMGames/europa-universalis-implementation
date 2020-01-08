package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.AuthentInfo;
import com.mkl.eu.client.common.vo.GameInfo;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.common.ValidateRequest;
import com.mkl.eu.client.service.service.eco.*;
import com.mkl.eu.client.service.util.GameUtil;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.eco.Competition;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.ref.Referential;
import com.mkl.eu.client.service.vo.ref.country.CountryReferential;
import com.mkl.eu.client.service.vo.ref.country.LimitReferential;
import com.mkl.eu.client.service.vo.tables.*;
import com.mkl.eu.service.service.domain.ICounterDomain;
import com.mkl.eu.service.service.domain.IStatusWorkflowDomain;
import com.mkl.eu.service.service.mapping.GameMapping;
import com.mkl.eu.service.service.mapping.chat.ChatMapping;
import com.mkl.eu.service.service.mapping.eco.AdministrativeActionMapping;
import com.mkl.eu.service.service.mapping.eco.CompetitionMapping;
import com.mkl.eu.service.service.mapping.eco.EconomicalSheetMapping;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.chat.IChatDao;
import com.mkl.eu.service.service.persistence.country.IPlayableCountryDao;
import com.mkl.eu.service.service.persistence.eco.IAdminActionDao;
import com.mkl.eu.service.service.persistence.eco.IEconomicalSheetDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.OtherForcesEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.DiscoveryEntity;
import com.mkl.eu.service.service.persistence.oe.country.MonarchEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.eco.*;
import com.mkl.eu.service.service.persistence.oe.ref.country.CountryEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.*;
import com.mkl.eu.service.service.persistence.ref.ICountryDao;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.service.AbstractGameServiceTest;
import com.mkl.eu.service.service.util.DiffUtil;
import com.mkl.eu.service.service.util.IOEUtil;
import com.mkl.eu.service.service.util.impl.OEUtilImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
public class EcoServiceTest extends AbstractGameServiceTest {
    @InjectMocks
    private EconomicServiceImpl economicService;

    @Mock
    private ICounterDomain counterDomain;

    @Mock
    private IStatusWorkflowDomain statusWorkflowDomain;

    @Mock
    private IAdminActionDao adminActionDao;

    @Mock
    private ICounterDao counterDao;

    @Mock
    private IProvinceDao provinceDao;

    @Mock
    private IEconomicalSheetDao economicalSheetDao;

    @Mock
    private IPlayableCountryDao playableCountryDao;

    @Mock
    private ICountryDao countryDao;

    @Mock
    private IChatDao chatDao;

    @Mock
    private GameMapping gameMapping;

    @Mock
    private EconomicalSheetMapping ecoSheetMapping;

    @Mock
    private AdministrativeActionMapping admActMapping;

    @Mock
    private CompetitionMapping competitionMapping;

    @Mock
    private ChatMapping chatMapping;

    @Mock
    private IOEUtil oeUtil;

    @Test
    public void testComputeSheets() throws FunctionalException {
        GameEntity game = createGameUsingMocks();
        game.setTurn(2);

        Request<Void> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("toto");
        request.setGame(new GameInfo());
        request.getGame().setIdGame(game.getId());
        request.getGame().setVersionGame(game.getVersion());

        DiffEntity diffEco = DiffUtil.createDiff(game, DiffTypeEnum.INVALIDATE, DiffTypeObjectEnum.ECO_SHEET);
        when(statusWorkflowDomain.computeEconomicalSheets(game)).thenReturn(diffEco);

        simulateDiff();

        economicService.computeEconomicalSheets(request);

        DiffEntity diffEntity = retrieveDiffCreated();

        Assert.assertEquals(diffEco, diffEntity);
    }

    @Test
    public void testLoadEcoSheets() {
        try {
            economicService.loadEconomicSheets(null);
            Assert.fail("Should break because loadEcoSheets is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("loadEcoSheets", e.getParams()[0]);
        }

        Request<LoadEcoSheetsRequest> request = new Request<>();

        try {
            economicService.loadEconomicSheets(request);
            Assert.fail("Should break because loadEcoSheets.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("loadEcoSheets.request", e.getParams()[0]);
        }

        request.setRequest(new LoadEcoSheetsRequest(1L, null, 2));

        List<EconomicalSheetEntity> sheets = new ArrayList<>();
        sheets.add(new EconomicalSheetEntity());
        when(economicalSheetDao.loadSheets(null, 2, 1L)).thenReturn(sheets);
        List<EconomicalSheetCountry> sheetCountries = new ArrayList<>();
        sheetCountries.add(new EconomicalSheetCountry());
        sheetCountries.add(new EconomicalSheetCountry());
        when(ecoSheetMapping.oesToVosCountry(sheets)).thenReturn(sheetCountries);

        try {
            List<EconomicalSheetCountry> ecoSheets = economicService.loadEconomicSheets(request);

            InOrder inOrder = inOrder(economicalSheetDao, ecoSheetMapping);

            inOrder.verify(economicalSheetDao).loadSheets(null, 2, 1L);
            inOrder.verify(ecoSheetMapping).oesToVosCountry(sheets);

            Assert.assertEquals(ecoSheets, sheetCountries);
        } catch (FunctionalException e) {
            Assert.fail("Should not break " + e.getMessage());
        }
    }

    @Test
    public void testLoadAdminActions() {
        try {
            economicService.loadAdminActions(null);
            Assert.fail("Should break because loadAdminActions is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("loadAdminActions", e.getParams()[0]);
        }

        Request<LoadAdminActionsRequest> request = new Request<>();

        try {
            economicService.loadAdminActions(request);
            Assert.fail("Should break because loadAdminActions.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("loadAdminActions.request", e.getParams()[0]);
        }

        request.setRequest(new LoadAdminActionsRequest(1L, 2));

        List<AdministrativeActionEntity> actions = new ArrayList<>();
        actions.add(new AdministrativeActionEntity());
        when(adminActionDao.findDoneAdminActions(2, 1L)).thenReturn(actions);
        List<AdministrativeActionCountry> actionCountries = new ArrayList<>();
        actionCountries.add(new AdministrativeActionCountry());
        actionCountries.add(new AdministrativeActionCountry());
        when(admActMapping.oesToVosCountry(actions)).thenReturn(actionCountries);

        try {
            List<AdministrativeActionCountry> admActions = economicService.loadAdminActions(request);

            InOrder inOrder = inOrder(adminActionDao, admActMapping);

            inOrder.verify(adminActionDao).findDoneAdminActions(2, 1L);
            inOrder.verify(admActMapping).oesToVosCountry(actions);

            Assert.assertEquals(admActions, actionCountries);
        } catch (FunctionalException e) {
            Assert.fail("Should not break " + e.getMessage());
        }
    }

    @Test
    public void testLoadCompetitions() {
        try {
            economicService.loadCompetitions(null);
            Assert.fail("Should break because loadCompetitions is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("loadCompetitions", e.getParams()[0]);
        }

        Request<LoadCompetitionsRequest> request = new Request<>();

        try {
            economicService.loadCompetitions(request);
            Assert.fail("Should break because loadCompetitions.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("loadCompetitions.request", e.getParams()[0]);
        }

        request.setRequest(new LoadCompetitionsRequest(1L, 2));

        List<CompetitionEntity> competitionEntities = new ArrayList<>();
        competitionEntities.add(new CompetitionEntity());
        when(adminActionDao.findCompetitions(2, 1L)).thenReturn(competitionEntities);
        List<Competition> comptetitionVos = new ArrayList<>();
        comptetitionVos.add(new Competition());
        comptetitionVos.add(new Competition());
        when(competitionMapping.oesToVos(competitionEntities, new HashMap<>())).thenReturn(comptetitionVos);

        try {
            List<Competition> competitions = economicService.loadCompetitions(request);

            InOrder inOrder = inOrder(adminActionDao, competitionMapping);

            inOrder.verify(adminActionDao).findCompetitions(2, 1L);
            inOrder.verify(competitionMapping).oesToVos(competitionEntities, new HashMap<>());

            Assert.assertEquals(competitions, comptetitionVos);
        } catch (FunctionalException e) {
            Assert.fail("Should not break " + e.getMessage());
        }
    }

    @Test
    public void testAddAdmActFailSimple() {
        Pair<Request<AddAdminActionRequest>, GameEntity> pair = testCheckGame(economicService::addAdminAction, "addAdminAction");
        Request<AddAdminActionRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();

        testCheckStatus(game, request, economicService::addAdminAction, "addAdminAction", GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);

        game.setTurn(1);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setName("france");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because addAdminAction.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request", e.getParams()[0]);
        }

        request.setRequest(new AddAdminActionRequest());

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because idCountry is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.idCountry", e.getParams()[0]);
        }

        request.getRequest().setIdCountry(11L);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because idCountry is not found");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.idCountry", e.getParams()[0]);
        }

        request.getRequest().setIdCountry(12L);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because type is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }
    }

    @Test
    public void testAddAdmActDisLmFail() {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(createGameInfo());
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(12L);

        GameEntity game = createGameUsingMocks();
        game.setTurn(1);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setName("france");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(2L);
        game.getStacks().get(0).getCounters().get(0).setCountry("angleterre");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setId(3L);
        game.getStacks().get(1).getCounters().get(0).setCountry("france");
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.MNU_ART_MINUS);
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(1).setId(4L);
        game.getStacks().get(1).getCounters().get(1).setCountry("france");
        game.getStacks().get(1).getCounters().get(1).setType(CounterFaceTypeEnum.FLEET_MINUS);
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(2).setId(5L);
        game.getStacks().get(1).getCounters().get(2).setCountry("france");
        game.getStacks().get(1).getCounters().get(2).setType(CounterFaceTypeEnum.FORTRESS_4);
        game.getStacks().get(1).getCounters().get(2).setOwner(game.getStacks().get(1));
        game.getStacks().get(1).setProvince("idf");

        request.getRequest().setType(AdminActionTypeEnum.DIS);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because idObject is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.idObject", e.getParams()[0]);
        }

        request.getRequest().setIdObject(1L);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because idObject is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.idObject", e.getParams()[0]);
        }

        request.getRequest().setIdObject(2L);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because idObject is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.COUNTER_NOT_OWNED, e.getCode());
            Assert.assertEquals("addAdminAction.request.idObject", e.getParams()[0]);
        }

        request.getRequest().setIdObject(3L);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because idObject is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.COUNTER_CANT_DISBAND, e.getCode());
            Assert.assertEquals("addAdminAction.request.idObject", e.getParams()[0]);
        }

        request.getRequest().setIdObject(4L);
        request.getRequest().setType(AdminActionTypeEnum.LM);
        when(oeUtil.getWarStatus(game, game.getCountries().get(0))).thenReturn(WarStatusEnum.PEACE);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because idObject is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.COUNTER_MAINTAIN_LOW_FORBIDDEN, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        when(oeUtil.getWarStatus(game, game.getCountries().get(0))).thenReturn(WarStatusEnum.CLASSIC_WAR);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because idObject is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.COUNTER_CANT_MAINTAIN_LOW, e.getCode());
            Assert.assertEquals("addAdminAction.request.idObject", e.getParams()[0]);
        }

        request.getRequest().setType(AdminActionTypeEnum.LF);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because faceType is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.counterFaceType", e.getParams()[0]);
        }

        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.FORTRESS_5);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because idObject is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.COUNTER_CANT_LOWER_FORTRESS, e.getCode());
            Assert.assertEquals("addAdminAction.request.idObject", e.getParams()[0]);
        }

        request.getRequest().setIdObject(5L);
        EuropeanProvinceEntity corn = new EuropeanProvinceEntity();
        corn.setName("idf");
        corn.setFortress(2);
        when(provinceDao.getProvinceByName("idf")).thenReturn(corn);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because idObject is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.COUNTER_WRONG_LOWER_FORTRESS, e.getCode());
            Assert.assertEquals("addAdminAction.request.idObject", e.getParams()[0]);
        }

        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.FORTRESS_1);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because idObject is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.COUNTER_WRONG_LOWER_FORTRESS, e.getCode());
            Assert.assertEquals("addAdminAction.request.idObject", e.getParams()[0]);
        }

        request.getRequest().setIdObject(4L);
        request.getRequest().setType(AdminActionTypeEnum.DIS);

        List<AdministrativeActionEntity> actions = new ArrayList<>();
        actions.add(new AdministrativeActionEntity());
        when(adminActionDao.findPlannedAdminActions(12L, 1, 4L, AdminActionTypeEnum.LM, AdminActionTypeEnum.DIS,
                AdminActionTypeEnum.LF)).thenReturn(actions);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because idObject is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.COUNTER_ALREADY_PLANNED, e.getCode());
            Assert.assertEquals("addAdminAction.request.idObject", e.getParams()[0]);
        }
    }

    @Test
    public void testAddAdmActLMSuccess() throws Exception {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setRequest(new AddAdminActionRequest());
        request.setAuthent(new AuthentInfo());
        request.setGame(createGameInfo());
        request.getRequest().setIdCountry(14L);
        request.getRequest().setType(AdminActionTypeEnum.LM);
        request.getRequest().setIdObject(4L);

        GameEntity game = createGameUsingMocks();
        game.setTurn(2);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(14L);
        game.getCountries().get(0).setName("france");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(2L);
        game.getStacks().get(0).getCounters().get(0).setCountry("angleterre");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setId(3L);
        game.getStacks().get(1).getCounters().get(0).setCountry("france");
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.MNU_ART_MINUS);
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(1).setId(4L);
        game.getStacks().get(1).getCounters().get(1).setCountry("france");
        game.getStacks().get(1).getCounters().get(1).setType(CounterFaceTypeEnum.ARMY_MINUS);
        game.getStacks().get(1).getCounters().get(1).setOwner(game.getStacks().get(1));
        game.getStacks().get(1).setProvince("idf");

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        simulateDiff();

        when(oeUtil.getWarStatus(game, game.getCountries().get(0))).thenReturn(WarStatusEnum.CLASSIC_WAR);

        DiffResponse response = economicService.addAdminAction(request);

        DiffEntity diffEntity = retrieveDiffCreated();

        InOrder inOrder = inOrder(gameDao, adminActionDao, diffDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, request.getGame().getIdCountry(), 1L);
        inOrder.verify(adminActionDao).findPlannedAdminActions(request.getRequest().getIdCountry(), game.getTurn(),
                request.getRequest().getIdObject(), AdminActionTypeEnum.LM,
                AdminActionTypeEnum.DIS, AdminActionTypeEnum.LF);
        inOrder.verify(adminActionDao).create(anyObject());
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.ADD, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.ADM_ACT, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(6, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_COUNTRY, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(request.getRequest().getIdCountry().toString(),
                diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TURN, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals(game.getTurn().toString(), diffEntity.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diffEntity.getAttributes().get(2).getType());
        Assert.assertEquals(request.getRequest().getType().name(), diffEntity.getAttributes().get(2).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_OBJECT, diffEntity.getAttributes().get(3).getType());
        Assert.assertEquals(request.getRequest().getIdObject().toString(),
                diffEntity.getAttributes().get(3).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diffEntity.getAttributes().get(4).getType());
        Assert.assertEquals("idf", diffEntity.getAttributes().get(4).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COUNTER_FACE_TYPE, diffEntity.getAttributes().get(5).getType());
        Assert.assertEquals(CounterFaceTypeEnum.ARMY_MINUS.toString(),
                diffEntity.getAttributes().get(5).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
    }

    @Test
    public void testAddAdmActLFSuccess() throws Exception {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setRequest(new AddAdminActionRequest());
        request.setAuthent(new AuthentInfo());
        request.setGame(createGameInfo());
        request.getRequest().setIdCountry(14L);
        request.getRequest().setType(AdminActionTypeEnum.LF);
        request.getRequest().setIdObject(5L);
        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.FORTRESS_3);

        GameEntity game = createGameUsingMocks();
        game.setTurn(2);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(14L);
        game.getCountries().get(0).setName("france");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(2L);
        game.getStacks().get(0).getCounters().get(0).setCountry("angleterre");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setId(3L);
        game.getStacks().get(1).getCounters().get(0).setCountry("france");
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.MNU_ART_MINUS);
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(1).setId(4L);
        game.getStacks().get(1).getCounters().get(1).setCountry("france");
        game.getStacks().get(1).getCounters().get(1).setType(CounterFaceTypeEnum.ARMY_MINUS);
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(2).setId(5L);
        game.getStacks().get(1).getCounters().get(2).setCountry("france");
        game.getStacks().get(1).getCounters().get(2).setType(CounterFaceTypeEnum.FORTRESS_4);
        game.getStacks().get(1).getCounters().get(2).setOwner(game.getStacks().get(1));
        game.getStacks().get(1).setProvince("idf");

        EuropeanProvinceEntity corn = new EuropeanProvinceEntity();
        corn.setName("idf");
        corn.setFortress(2);
        when(provinceDao.getProvinceByName("idf")).thenReturn(corn);

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        simulateDiff();

        DiffResponse response = economicService.addAdminAction(request);

        DiffEntity diffEntity = retrieveDiffCreated();

        InOrder inOrder = inOrder(gameDao, provinceDao, adminActionDao, diffDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, request.getGame().getIdCountry(), 1L);
        inOrder.verify(provinceDao).getProvinceByName("idf");
        inOrder.verify(adminActionDao).findPlannedAdminActions(request.getRequest().getIdCountry(), game.getTurn(),
                request.getRequest().getIdObject(), AdminActionTypeEnum.LM,
                AdminActionTypeEnum.DIS, AdminActionTypeEnum.LF);
        inOrder.verify(adminActionDao).create(anyObject());
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.ADD, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.ADM_ACT, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(6, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_COUNTRY, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(request.getRequest().getIdCountry().toString(),
                            diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TURN, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals(game.getTurn().toString(), diffEntity.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diffEntity.getAttributes().get(2).getType());
        Assert.assertEquals(request.getRequest().getType().name(), diffEntity.getAttributes().get(2).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_OBJECT, diffEntity.getAttributes().get(3).getType());
        Assert.assertEquals(request.getRequest().getIdObject().toString(),
                            diffEntity.getAttributes().get(3).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diffEntity.getAttributes().get(4).getType());
        Assert.assertEquals("idf", diffEntity.getAttributes().get(4).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COUNTER_FACE_TYPE, diffEntity.getAttributes().get(5).getType());
        Assert.assertEquals(request.getRequest().getCounterFaceType().toString(),
                diffEntity.getAttributes().get(5).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
    }

    @Test
    public void testAddAdmActDISSuccess() throws Exception {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setRequest(new AddAdminActionRequest());
        request.setAuthent(new AuthentInfo());
        request.setGame(createGameInfo());
        request.getRequest().setIdCountry(14L);
        request.getRequest().setType(AdminActionTypeEnum.DIS);
        request.getRequest().setIdObject(4L);

        GameEntity game = createGameUsingMocks();
        game.setTurn(2);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(14L);
        game.getCountries().get(0).setName("france");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(2L);
        game.getStacks().get(0).getCounters().get(0).setCountry("angleterre");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setId(3L);
        game.getStacks().get(1).getCounters().get(0).setCountry("france");
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.MNU_ART_MINUS);
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(1).setId(4L);
        game.getStacks().get(1).getCounters().get(1).setCountry("france");
        game.getStacks().get(1).getCounters().get(1).setType(CounterFaceTypeEnum.ARMY_MINUS);
        game.getStacks().get(1).getCounters().get(1).setOwner(game.getStacks().get(1));
        game.getStacks().get(1).setProvince("idf");

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        simulateDiff();

        DiffResponse response = economicService.addAdminAction(request);

        DiffEntity diffEntity = retrieveDiffCreated();

        InOrder inOrder = inOrder(gameDao, adminActionDao, diffDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, request.getGame().getIdCountry(), 1L);
        inOrder.verify(adminActionDao).findPlannedAdminActions(request.getRequest().getIdCountry(), game.getTurn(),
                request.getRequest().getIdObject(), AdminActionTypeEnum.LM,
                AdminActionTypeEnum.DIS, AdminActionTypeEnum.LF);
        inOrder.verify(adminActionDao).create(anyObject());
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.ADD, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.ADM_ACT, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(6, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_COUNTRY, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(request.getRequest().getIdCountry().toString(),
                            diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TURN, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals(game.getTurn().toString(), diffEntity.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diffEntity.getAttributes().get(2).getType());
        Assert.assertEquals(request.getRequest().getType().name(), diffEntity.getAttributes().get(2).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_OBJECT, diffEntity.getAttributes().get(3).getType());
        Assert.assertEquals(request.getRequest().getIdObject().toString(),
                            diffEntity.getAttributes().get(3).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diffEntity.getAttributes().get(4).getType());
        Assert.assertEquals("idf", diffEntity.getAttributes().get(4).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COUNTER_FACE_TYPE, diffEntity.getAttributes().get(5).getType());
        Assert.assertEquals(CounterFaceTypeEnum.ARMY_MINUS.toString(),
                diffEntity.getAttributes().get(5).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
    }

    @Test
    public void testAddAdmActPuFail() {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(createGameInfo());
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(12L);

        GameEntity game = createGameUsingMocks();
        game.setTurn(1);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setLandTech(Tech.MEDIEVAL);
        game.getCountries().get(0).getAdministrativeActions().add(new AdministrativeActionEntity());
        game.getCountries().get(0).getAdministrativeActions().get(0).setTurn(1);
        game.getCountries().get(0).getAdministrativeActions().get(0).setStatus(AdminActionStatusEnum.PLANNED);
        game.getCountries().get(0).getAdministrativeActions().get(0).setType(AdminActionTypeEnum.PU);
        game.getCountries().get(0).getAdministrativeActions().get(0).setCounterFaceType(CounterFaceTypeEnum.FORTRESS_2);
        game.getCountries().get(0).getAdministrativeActions().get(0).setProvince("poitou");

        request.getRequest().setType(AdminActionTypeEnum.PU);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because counterFaceType is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.counterFaceType", e.getParams()[0]);
        }

        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.MNU_ART_MINUS);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because province is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("rotw");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because province is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        StackEntity stackOwnFor = new StackEntity();
        CounterEntity counterOwnFor = new CounterEntity();
        counterOwnFor.setType(CounterFaceTypeEnum.OWN);
        counterOwnFor.setCountry("angleterre");
        stackOwnFor.getCounters().add(counterOwnFor);

        StackEntity stackOwn = new StackEntity();
        CounterEntity counterOwn = new CounterEntity();
        counterOwn.setType(CounterFaceTypeEnum.OWN);
        counterOwn.setCountry("france");
        stackOwn.getCounters().add(counterOwn);

        StackEntity stackCtrlFor = new StackEntity();
        CounterEntity counterCtrlFor = new CounterEntity();
        counterCtrlFor.setType(CounterFaceTypeEnum.CONTROL);
        counterCtrlFor.setCountry("angleterre");
        stackCtrlFor.getCounters().add(counterCtrlFor);

        StackEntity stackCtrl = new StackEntity();
        CounterEntity counterCtrl = new CounterEntity();
        counterCtrl.setType(CounterFaceTypeEnum.CONTROL);
        counterCtrl.setCountry("france");
        stackCtrl.getCounters().add(counterCtrl);

        StackEntity stackFortress = new StackEntity();
        CounterEntity counterFortress = new CounterEntity();
        counterFortress.setType(CounterFaceTypeEnum.FORTRESS_2);
        counterFortress.setCountry("france");
        stackFortress.getCounters().add(counterFortress);

        RotwProvinceEntity rotw = new RotwProvinceEntity();
        rotw.setName("rotw");
        when(provinceDao.getProvinceByName("rotw")).thenReturn(rotw);

        EuropeanProvinceEntity controlledNotOwn = new EuropeanProvinceEntity();
        controlledNotOwn.setName("controlledNotOwn");
        when(provinceDao.getProvinceByName("controlledNotOwn")).thenReturn(controlledNotOwn);

        when(oeUtil.getStacksOnProvince(game, "controlledNotOwn")).thenReturn(Arrays.asList(stackOwnFor, stackCtrl));

        EuropeanProvinceEntity ownedNotControlled = new EuropeanProvinceEntity();
        ownedNotControlled.setName("ownedNotControlled");
        when(provinceDao.getProvinceByName("ownedNotControlled")).thenReturn(ownedNotControlled);

        when(oeUtil.getStacksOnProvince(game, "ownedNotControlled")).thenReturn(Arrays.asList(stackOwn, stackCtrlFor));

        EuropeanProvinceEntity owned = new EuropeanProvinceEntity();
        owned.setName("owned");
        when(provinceDao.getProvinceByName("owned")).thenReturn(owned);

        when(oeUtil.getStacksOnProvince(game, "owned")).thenReturn(Collections.singletonList(stackOwn));

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setName("IdF");
        idf.setDefaultOwner("france");
        idf.setPort(false);
        when(provinceDao.getProvinceByName("IdF")).thenReturn(idf);

        EuropeanProvinceEntity poitou = new EuropeanProvinceEntity();
        poitou.setName("poitou");
        poitou.setDefaultOwner("france");
        poitou.setPort(true);
        poitou.setArsenal(false);
        when(provinceDao.getProvinceByName("poitou")).thenReturn(poitou);

        EuropeanProvinceEntity corn = new EuropeanProvinceEntity();
        corn.setName("corn");
        corn.setDefaultOwner("france");
        corn.setArsenal(true);
        corn.setFortress(1);
        when(provinceDao.getProvinceByName("corn")).thenReturn(corn);
        when(oeUtil.getStacksOnProvince(game, "corn")).thenReturn(Collections.singletonList(stackFortress));

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because province is not owned by the country");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PROVINCE_NOT_OWN_CONTROL, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("controlledNotOwn");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because province is not owned by the country");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PROVINCE_NOT_OWN_CONTROL, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("ownedNotControlled");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because province is not owned by the country");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PROVINCE_NOT_OWN_CONTROL, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("owned");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because face type is not correct");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.COUNTER_CANT_PURCHASE, e.getCode());
            Assert.assertEquals("addAdminAction.request.counterFaceType", e.getParams()[0]);
        }

        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.FLEET_MINUS);
        request.getRequest().setProvince("IdF");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because face type is not correct");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.COUNTER_CANT_PURCHASE, e.getCode());
            Assert.assertEquals("addAdminAction.request.counterFaceType", e.getParams()[0]);
        }

        EconomicServiceImpl.TABLES = new Tables();
        request.getRequest().setProvince("poitou");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because limits were exceeded");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PURCHASE_LIMIT_EXCEED, e.getCode());
            Assert.assertEquals("addAdminAction.request.counterFaceType", e.getParams()[0]);
        }

        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.FORTRESS_2);
        request.getRequest().setProvince("rotw");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because province is not controller by the country");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PROVINCE_NOT_OWN_CONTROL, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("ownedNotControlled");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because province is not controller by the country");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PROVINCE_NOT_OWN_CONTROL, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("controlledNotOwn");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because fortress level in inconsistent");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.COUNTER_CANT_PURCHASE, e.getCode());
            Assert.assertEquals("addAdminAction.request.counterFaceType", e.getParams()[0]);
        }

        request.getRequest().setProvince("corn");
        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.FORTRESS_2);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because fortress level in inconsistent");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.COUNTER_CANT_PURCHASE, e.getCode());
            Assert.assertEquals("addAdminAction.request.counterFaceType", e.getParams()[0]);
        }

        Tables tables = new Tables();
        List<Limit> limits = new ArrayList<>();
        Limit limit = new Limit();
        limit.setCountry("france");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(6);
        limit.setNumber(3);
        limit.setType(LimitTypeEnum.PURCHASE_NAVAL_TROOPS);
        limits.add(limit);
        limit = new Limit();
        limit.setCountry("france");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(6);
        limit.setNumber(3);
        limit.setType(LimitTypeEnum.PURCHASE_LAND_TROOPS);
        limits.add(limit);
        tables.getLimits().addAll(limits);
        List<Tech> techs = new ArrayList<>();
        Tech tech = new Tech();
        tech.setName(Tech.MEDIEVAL);
        tech.setBeginTurn(1);
        techs.add(tech);
        tech = new Tech();
        tech.setName(Tech.RENAISSANCE);
        tech.setBeginTurn(11);
        techs.add(tech);
        tables.getTechs().addAll(techs);
        EconomicServiceImpl.TABLES = tables;

        request.getRequest().setProvince("poitou");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because fortress is already planned");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.FORTRESS_ALREADY_PLANNED, e.getCode());
            Assert.assertEquals("addAdminAction.request.counterFaceType", e.getParams()[0]);
        }

        request.getRequest().setProvince("corn");
        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.ARSENAL_3);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because arsenal cant be purchased in this province (not rotw)");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.COUNTER_CANT_PURCHASE, e.getCode());
            Assert.assertEquals("addAdminAction.request.counterFaceType", e.getParams()[0]);
        }

        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.FORTRESS_3);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because fortress level cant be purchased by this country (technology)");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.FORTRESS_CANT_PURCHASE, e.getCode());
            Assert.assertEquals("addAdminAction.request.counterFaceType", e.getParams()[0]);
        }

        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.FORTRESS_4);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because fortress level cant be purchased by this country (technology)");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.COUNTER_CANT_PURCHASE, e.getCode());
            Assert.assertEquals("addAdminAction.request.counterFaceType", e.getParams()[0]);
        }

        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.FORTRESS_5);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because fortress level cant be purchased by this country (technology)");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.COUNTER_CANT_PURCHASE, e.getCode());
            Assert.assertEquals("addAdminAction.request.counterFaceType", e.getParams()[0]);
        }

        request.getRequest().setProvince("corn");
        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.FLEET_MINUS);

        try {
            economicService.addAdminAction(request);
        } catch (FunctionalException e) {
            Assert.fail(e.getMessage());
        }


        List<AdministrativeActionEntity> actions = new ArrayList<>();
        AdministrativeActionEntity action = new AdministrativeActionEntity();
        action.setCounterFaceType(CounterFaceTypeEnum.ARMY_PLUS);
        actions.add(action);
        action = new AdministrativeActionEntity();
        action.setCounterFaceType(CounterFaceTypeEnum.FLEET_PLUS);
        actions.add(action);
        when(adminActionDao.findPlannedAdminActions(12l, 1, null, AdminActionTypeEnum.PU)).thenReturn(actions);
        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.ARMY_MINUS);

        try {
            economicService.addAdminAction(request);
        } catch (FunctionalException e) {
            Assert.fail(e.getMessage());
        }

        action = new AdministrativeActionEntity();
        action.setCounterFaceType(CounterFaceTypeEnum.ARMY_PLUS);
        actions.add(action);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because limits were exceeded");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PURCHASE_LIMIT_EXCEED, e.getCode());
            Assert.assertEquals("addAdminAction.request.counterFaceType", e.getParams()[0]);
        }
    }

    @Test
    public void testAddAdmActPuSuccess() throws FunctionalException {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(createGameInfo());
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(12L);
        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.ARMY_MINUS);
        request.getRequest().setProvince("corn");

        GameEntity game = createGameUsingMocks();
        game.setTurn(1);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setLandTech("MEDIEVAL");

        request.getRequest().setType(AdminActionTypeEnum.PU);

        StackEntity stackOwn = new StackEntity();
        CounterEntity counterOwn = new CounterEntity();
        counterOwn.setType(CounterFaceTypeEnum.OWN);
        counterOwn.setCountry("france");
        stackOwn.getCounters().add(counterOwn);

        when(oeUtil.getStacksOnProvince(game, "corn")).thenReturn(Collections.singletonList(stackOwn));

        EuropeanProvinceEntity corn = new EuropeanProvinceEntity();
        corn.setName("corn");
        corn.setDefaultOwner("angleterre");
        corn.setArsenal(true);
        when(provinceDao.getProvinceByName("corn")).thenReturn(corn);

        Tables tables = new Tables();
        List<Unit> units = new ArrayList<>();
        Unit unit = new Unit();
        unit.setCountry("france");
        unit.setPrice(15);
        unit.setType(ForceTypeEnum.ARMY_MINUS);
        unit.setAction(UnitActionEnum.PURCHASE);
        unit.setTech(new Tech());
        unit.getTech().setName("LACE_WAR");
        units.add(unit);
        unit = new Unit();
        unit.setCountry("angleterre");
        unit.setPrice(10);
        unit.setType(ForceTypeEnum.ARMY_MINUS);
        unit.setAction(UnitActionEnum.PURCHASE);
        unit.setTech(new Tech());
        unit.getTech().setName("MEDIEVAL");
        units.add(unit);
        unit = new Unit();
        unit.setCountry("france");
        unit.setPrice(5);
        unit.setType(ForceTypeEnum.ARMY_MINUS);
        unit.setAction(UnitActionEnum.PURCHASE);
        unit.setTech(new Tech());
        unit.getTech().setName("MEDIEVAL");
        units.add(unit);
        tables.getUnits().addAll(units);
        List<Limit> limits = new ArrayList<>();
        Limit limit = new Limit();
        limit.setCountry("france");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(7);
        limit.getPeriod().setEnd(15);
        limit.setNumber(3);
        limit.setType(LimitTypeEnum.PURCHASE_LAND_TROOPS);
        limits.add(limit);
        limit = new Limit();
        limit.setCountry("france");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(7);
        limit.setNumber(1);
        limit.setType(LimitTypeEnum.PURCHASE_LAND_TROOPS);
        limits.add(limit);
        limit = new Limit();
        limit.setCountry("angleterre");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(7);
        limit.setNumber(2);
        limit.setType(LimitTypeEnum.PURCHASE_LAND_TROOPS);
        limits.add(limit);
        tables.getLimits().addAll(limits);
        EconomicServiceImpl.TABLES = tables;

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        simulateDiff();

        DiffResponse response = economicService.addAdminAction(request);

        DiffEntity diffEntity = retrieveDiffCreated();

        InOrder inOrder = inOrder(gameDao, provinceDao, oeUtil, adminActionDao, diffDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, request.getGame().getIdCountry(), 1L);
        inOrder.verify(provinceDao).getProvinceByName("corn");
        inOrder.verify(oeUtil).getStacksOnProvince(game, "corn");
        inOrder.verify(adminActionDao).findPlannedAdminActions(12L, 1, null, AdminActionTypeEnum.PU);
        inOrder.verify(adminActionDao).create(anyObject());
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.ADD, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.ADM_ACT, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(6, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_COUNTRY, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(request.getRequest().getIdCountry().toString(),
                            diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TURN, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals(game.getTurn().toString(), diffEntity.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diffEntity.getAttributes().get(2).getType());
        Assert.assertEquals(request.getRequest().getType().name(), diffEntity.getAttributes().get(2).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COST, diffEntity.getAttributes().get(3).getType());
        Assert.assertEquals("8", diffEntity.getAttributes().get(3).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diffEntity.getAttributes().get(4).getType());
        Assert.assertEquals(request.getRequest().getProvince(), diffEntity.getAttributes().get(4).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COUNTER_FACE_TYPE, diffEntity.getAttributes().get(5).getType());
        Assert.assertEquals(request.getRequest().getCounterFaceType().name(),
                            diffEntity.getAttributes().get(5).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
    }

    @Test
    public void testAddAdmActPuFortressSuccess() throws FunctionalException {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(createGameInfo());
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(12L);
        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.FORTRESS_3);
        request.getRequest().setProvince("corn");

        GameEntity game = createGameUsingMocks();
        game.setTurn(1);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setLandTech("RENAISSANCE");

        request.getRequest().setType(AdminActionTypeEnum.PU);

        StackEntity stackOwn = new StackEntity();
        CounterEntity counterOwn = new CounterEntity();
        counterOwn.setType(CounterFaceTypeEnum.OWN);
        counterOwn.setCountry("angleterre");
        stackOwn.getCounters().add(counterOwn);

        StackEntity stackCtrl = new StackEntity();
        CounterEntity counterCtrl = new CounterEntity();
        counterCtrl.setType(CounterFaceTypeEnum.CONTROL);
        counterCtrl.setCountry("france");
        stackCtrl.getCounters().add(counterCtrl);

        StackEntity stackFortress = new StackEntity();
        CounterEntity counterFortress = new CounterEntity();
        counterFortress.setType(CounterFaceTypeEnum.FORTRESS_2);
        counterFortress.setCountry("france");
        stackFortress.getCounters().add(counterFortress);

        when(oeUtil.getStacksOnProvince(game, "corn")).thenReturn(Arrays.asList(stackOwn, stackCtrl, stackFortress));

        RotwProvinceEntity corn = new RotwProvinceEntity();
        corn.setName("corn");
        corn.setFortress(4);
        when(provinceDao.getProvinceByName("corn")).thenReturn(corn);

        Tables tables = new Tables();
        List<Unit> units = new ArrayList<>();
        Unit unit = new Unit();
        unit.setCountry("france");
        unit.setPrice(15);
        unit.setType(ForceTypeEnum.ARMY_MINUS);
        unit.setAction(UnitActionEnum.PURCHASE);
        unit.setTech(new Tech());
        unit.getTech().setName("LACE_WAR");
        units.add(unit);
        unit = new Unit();
        unit.setCountry("angleterre");
        unit.setPrice(10);
        unit.setType(ForceTypeEnum.ARMY_MINUS);
        unit.setAction(UnitActionEnum.PURCHASE);
        unit.setTech(new Tech());
        unit.getTech().setName("MEDIEVAL");
        units.add(unit);
        unit = new Unit();
        unit.setCountry("france");
        unit.setPrice(5);
        unit.setType(ForceTypeEnum.ARMY_MINUS);
        unit.setAction(UnitActionEnum.PURCHASE);
        unit.setTech(new Tech());
        unit.getTech().setName("MEDIEVAL");
        units.add(unit);
        tables.getUnits().addAll(units);
        List<Limit> limits = new ArrayList<>();
        Limit limit = new Limit();
        limit.setCountry("france");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(7);
        limit.getPeriod().setEnd(15);
        limit.setNumber(3);
        limit.setType(LimitTypeEnum.PURCHASE_LAND_TROOPS);
        limits.add(limit);
        limit = new Limit();
        limit.setCountry("france");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(7);
        limit.setNumber(1);
        limit.setType(LimitTypeEnum.PURCHASE_LAND_TROOPS);
        limits.add(limit);
        limit = new Limit();
        limit.setCountry("angleterre");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(7);
        limit.setNumber(2);
        limit.setType(LimitTypeEnum.PURCHASE_LAND_TROOPS);
        limits.add(limit);
        tables.getLimits().addAll(limits);
        List<Tech> techs = new ArrayList<>();
        Tech tech = new Tech();
        tech.setName("RENAISSANCE");
        tech.setBeginTurn(1);
        techs.add(tech);
        tech = new Tech();
        tech.setName(Tech.ARQUEBUS);
        tech.setBeginTurn(11);
        techs.add(tech);
        tables.getTechs().addAll(techs);
        EconomicServiceImpl.TABLES = tables;

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        simulateDiff();

        DiffResponse response = economicService.addAdminAction(request);

        DiffEntity diffEntity = retrieveDiffCreated();

        InOrder inOrder = inOrder(gameDao, provinceDao, oeUtil, adminActionDao, diffDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, request.getGame().getIdCountry(), 1L);
        inOrder.verify(provinceDao).getProvinceByName("corn");
        inOrder.verify(oeUtil).getStacksOnProvince(game, "corn");
        inOrder.verify(adminActionDao).create(anyObject());
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.ADD, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.ADM_ACT, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(6, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_COUNTRY, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(request.getRequest().getIdCountry().toString(),
                            diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TURN, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals(game.getTurn().toString(), diffEntity.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diffEntity.getAttributes().get(2).getType());
        Assert.assertEquals(request.getRequest().getType().name(), diffEntity.getAttributes().get(2).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COST, diffEntity.getAttributes().get(3).getType());
        Assert.assertEquals("200", diffEntity.getAttributes().get(3).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diffEntity.getAttributes().get(4).getType());
        Assert.assertEquals(request.getRequest().getProvince(), diffEntity.getAttributes().get(4).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COUNTER_FACE_TYPE, diffEntity.getAttributes().get(5).getType());
        Assert.assertEquals(request.getRequest().getCounterFaceType().name(),
                            diffEntity.getAttributes().get(5).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
    }

    @Test
    public void testRemoveAdmActFailSimple() {
        Pair<Request<RemoveAdminActionRequest>, GameEntity> pair = testCheckGame(economicService::removeAdminAction, "removeAdminAction");
        Request<RemoveAdminActionRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();

        testCheckStatus(game, request, economicService::removeAdminAction, "removeAdminAction", GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);

        try {
            economicService.removeAdminAction(request);
            Assert.fail("Should break because removeAdminAction.authent is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("removeAdminAction.authent", e.getParams()[0]);
        }

        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("MKL");

        try {
            economicService.removeAdminAction(request);
            Assert.fail("Should break because removeAdminAction.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("removeAdminAction.request", e.getParams()[0]);
        }

        request.setRequest(new RemoveAdminActionRequest());

        try {
            economicService.removeAdminAction(request);
            Assert.fail("Should break because idAdmAct is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("removeAdminAction.request.idAdmAct", e.getParams()[0]);
        }

        request.getRequest().setIdAdmAct(11L);

        try {
            economicService.removeAdminAction(request);
            Assert.fail("Should break because idAdmAct is not found");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("removeAdminAction.request.idAdmAct", e.getParams()[0]);
        }

        AdministrativeActionEntity action12 = new AdministrativeActionEntity();
        action12.setId(12L);
        action12.setCountry(new PlayableCountryEntity());
        action12.getCountry().setUsername("Jym");
        when(adminActionDao.load(12L)).thenReturn(action12);

        request.getRequest().setIdAdmAct(12L);

        try {
            economicService.removeAdminAction(request);
            Assert.fail("Should break because idAdmAct is not owned by the player");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("removeAdminAction.authent.username", e.getParams()[0]);
        }

        AdministrativeActionEntity action13 = new AdministrativeActionEntity();
        action13.setId(13L);
        action13.setCountry(new PlayableCountryEntity());
        action13.getCountry().setUsername("MKL");
        when(adminActionDao.load(13L)).thenReturn(action13);

        request.getRequest().setIdAdmAct(13L);

        try {
            economicService.removeAdminAction(request);
            Assert.fail("Should break because idAdmAct is not planned");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.ACTION_NOT_PLANNED, e.getCode());
            Assert.assertEquals("removeAdminAction.request.idAdmAct", e.getParams()[0]);
        }
    }

    @Test
    public void testRemoveAdmActSuccess() throws FunctionalException {
        Request<RemoveAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("MKL");
        request.setGame(createGameInfo());

        request.setRequest(new RemoveAdminActionRequest());

        request.getRequest().setIdAdmAct(13L);

        GameEntity game = createGameUsingMocks();
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);

        AdministrativeActionEntity action13 = new AdministrativeActionEntity();
        action13.setId(13L);
        action13.setCountry(new PlayableCountryEntity());
        action13.getCountry().setId(666L);
        action13.getCountry().setUsername("MKL");
        action13.setType(AdminActionTypeEnum.DIS);
        action13.setStatus(AdminActionStatusEnum.PLANNED);

        when(adminActionDao.load(13L)).thenReturn(action13);

        simulateDiff();

        DiffResponse response = economicService.removeAdminAction(request);

        DiffEntity diffEntity = retrieveDiffCreated();

        InOrder inOrder = inOrder(gameDao, adminActionDao, diffDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, request.getGame().getIdCountry(), 1L);
        inOrder.verify(adminActionDao).load(request.getRequest().getIdAdmAct());
        inOrder.verify(adminActionDao).delete(action13);
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.REMOVE, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.ADM_ACT, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(2, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_COUNTRY, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(action13.getCountry().getId().toString(), diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals(action13.getType().name(), diffEntity.getAttributes().get(1).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
    }

    @Test
    public void testAddAdmActTfiFail() {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(createGameInfo());
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(11L);

        GameEntity game = createGameUsingMocks();
        game.setTurn(1);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setName("france");
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(11L);
        game.getCountries().get(1).setName("angleterre");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(2L);
        game.getStacks().get(0).getCounters().get(0).setCountry("angleterre");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setId(3L);
        game.getStacks().get(1).getCounters().get(0).setCountry("france");
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.MNU_ART_MINUS);
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(1).setId(4L);
        game.getStacks().get(1).getCounters().get(1).setCountry("france");
        game.getStacks().get(1).getCounters().get(1).setType(CounterFaceTypeEnum.FLEET_MINUS);
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(2).setId(5L);
        game.getStacks().get(1).getCounters().get(2).setCountry("france");
        game.getStacks().get(1).getCounters().get(2).setType(CounterFaceTypeEnum.FORTRESS_4);
        game.getStacks().get(1).getCounters().get(2).setOwner(game.getStacks().get(1));
        game.getStacks().get(1).setProvince("idf");
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(0).setCountry("france");
        game.getTradeFleets().get(0).setProvince("zp_france");
        game.getTradeFleets().get(0).setLevel(6);

        List<AdministrativeActionEntity> actionsFor11 = new ArrayList<>();
        AdministrativeActionEntity action = new AdministrativeActionEntity();
        actionsFor11.add(action);
        actionsFor11.add(action);
        when(adminActionDao.findPlannedAdminActions(11L, 1, null, AdminActionTypeEnum.TFI)).thenReturn(actionsFor11);

        List<AdministrativeActionEntity> actionsFor12 = new ArrayList<>();
        actionsFor12.add(action);
        when(adminActionDao.findPlannedAdminActions(12L, 1, null, AdminActionTypeEnum.TFI)).thenReturn(actionsFor12);

        request.getRequest().setType(AdminActionTypeEnum.TFI);

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setName("idf");
        when(provinceDao.getProvinceByName("idf")).thenReturn(idf);

        TradeZoneProvinceEntity zmPerou = new TradeZoneProvinceEntity();
        zmPerou.setName("ZMPerou");
        zmPerou.setSeaZone("ZMPerou");
        zmPerou.setType(TradeZoneTypeEnum.ZM);
        when(provinceDao.getProvinceByName("ZMPerou")).thenReturn(zmPerou);

        TradeZoneProvinceEntity zmCanarias = new TradeZoneProvinceEntity();
        zmCanarias.setName("ZMCanarias");
        zmCanarias.setSeaZone("eCanarias");
        zmCanarias.setType(TradeZoneTypeEnum.ZM);
        when(provinceDao.getProvinceByName("ZMCanarias")).thenReturn(zmCanarias);

        TradeZoneProvinceEntity zmCasp = new TradeZoneProvinceEntity();
        zmCasp.setName("ZMCaspienne");
        zmCasp.setSeaZone("eCaspienne");
        zmCasp.setType(TradeZoneTypeEnum.ZM);
        when(provinceDao.getProvinceByName("ZMCaspienne")).thenReturn(zmCasp);

        TradeZoneProvinceEntity zpFr = new TradeZoneProvinceEntity();
        zpFr.setName("ZPfrance");
        zpFr.setSeaZone("ZPfrance");
        zpFr.setType(TradeZoneTypeEnum.ZP);
        zpFr.setCountryName("france");
        when(provinceDao.getProvinceByName("zp_france")).thenReturn(zpFr);

        SeaProvinceEntity canarias = new SeaProvinceEntity();
        when(provinceDao.getProvinceByName("eCanarias")).thenReturn(canarias);

        Tables tables = new Tables();
        List<Limit> limits = new ArrayList<>();
        Limit limit = new Limit();
        limit.setCountry("angleterre");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(6);
        limit.setNumber(2);
        limit.setType(LimitTypeEnum.ACTION_TFI);
        limits.add(limit);
        limit = new Limit();
        limit.setCountry("france");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(6);
        limit.setNumber(2);
        limit.setType(LimitTypeEnum.ACTION_TFI);
        limits.add(limit);
        tables.getLimits().addAll(limits);
        EconomicServiceImpl.TABLES = tables;

        List<String> countries = new ArrayList<>();
        countries.add("espagne");
        when(adminActionDao.getCountriesTradeFleetAccessRotw("ZMPerou", 12L)).thenReturn(countries);

        List<String> countries2 = new ArrayList<>();
        countries2.add("portugal");
        when(adminActionDao.getCountriesTradeFleetAccessRotw("ZMCanarias", 12L)).thenReturn(countries2);

        List<String> countries3 = new ArrayList<>();
        countries3.add("russie");
        when(counterDao.getNeighboringOwners("eCaspienne", 12L)).thenReturn(countries3);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because limit tfi already reached");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.ADMIN_ACTION_LIMIT_EXCEED, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        request.getRequest().setIdCountry(12L);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because investment is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.investment", e.getParams()[0]);
        }

        request.getRequest().setInvestment(InvestmentEnum.M);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because province is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("toto");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because province does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("idf");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because province is not a trade zone");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PROVINCE_WRONG_TYPE, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("ZMPerou");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because of trade fleet access in rotw");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.TRADE_FLEET_ACCESS_ROTW, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        countries.add("france");

        try {
            economicService.addAdminAction(request);
        } catch (FunctionalException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        request.getRequest().setProvince("ZMCanarias");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because of trade fleet access in rotw");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.TRADE_FLEET_ACCESS_ROTW, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        canarias.getBorders().add(new BorderEntity());
        canarias.getBorders().get(0).setProvinceTo(new RotwProvinceEntity());
        canarias.getBorders().get(0).getProvinceTo().setName("rotw");
        canarias.getBorders().add(new BorderEntity());
        canarias.getBorders().get(1).setProvinceTo(new SeaProvinceEntity());
        canarias.getBorders().get(1).getProvinceTo().setName("eGuinea");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because of trade fleet access in rotw");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.TRADE_FLEET_ACCESS_ROTW, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        DiscoveryEntity disc = new DiscoveryEntity();
        disc.setProvince("sGuinea");
        disc.setTurn(10);
        game.getCountries().get(0).getDiscoveries().add(disc);
        disc = new DiscoveryEntity();
        disc.setProvince("eCanarias");
        game.getCountries().get(0).getDiscoveries().add(disc);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because of trade fleet access in rotw");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.TRADE_FLEET_ACCESS_ROTW, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        disc.setTurn(10);

        try {
            economicService.addAdminAction(request);
        } catch (FunctionalException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        request.getRequest().setProvince("ZMCaspienne");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because of trade fleet access in caspian");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.TRADE_FLEET_ACCESS_CASPIAN, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        game.setMedCommCenterOwner("france");

        try {
            economicService.addAdminAction(request);
        } catch (FunctionalException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        game.setMedCommCenterOwner("espagne");
        game.setOrientCommCenterOwner("france");

        try {
            economicService.addAdminAction(request);
        } catch (FunctionalException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        game.setOrientCommCenterOwner("turquie");
        countries3.add("france");

        try {
            economicService.addAdminAction(request);
        } catch (FunctionalException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        request.getRequest().setProvince("zp_france");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because trade fleet is full");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.TRADE_FLEET_FULL, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }
    }

    @Test
    public void testAddAdmActTfiSuccess1() throws FunctionalException {
        subTestAddAdmActTfiSuccess("ZPfrance", InvestmentEnum.M, "30", "4", "1");
    }

    @Test
    public void testAddAdmActTfiSuccess2() throws FunctionalException {
        subTestAddAdmActTfiSuccess("ZPangleterre", InvestmentEnum.S, "10", "-3", "0");
    }

    @Test
    public void testAddAdmActTfiSuccess3() throws FunctionalException {
        subTestAddAdmActTfiSuccess("ZPBaltique", InvestmentEnum.L, "50", "-1", "-1");
    }

    private void subTestAddAdmActTfiSuccess(String province, InvestmentEnum investment, String cost, String column,
                                            String bonus) throws FunctionalException {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(createGameInfo());
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(12L);

        GameEntity game = createGameUsingMocks();
        game.setTurn(1);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setFti(2);
        game.getCountries().get(0).setDti(3);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(11L);
        game.getCountries().get(1).setName("angleterre");
        game.getCountries().get(1).setDti(3);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setProvince("s_baltique");
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(2L);
        game.getStacks().get(0).getCounters().get(0).setCountry("pirate");
        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.PIRATE_MINUS);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setId(3L);
        game.getStacks().get(1).getCounters().get(0).setCountry("france");
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.MNU_ART_MINUS);
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(1).setId(4L);
        game.getStacks().get(1).getCounters().get(1).setCountry("france");
        game.getStacks().get(1).getCounters().get(1).setType(CounterFaceTypeEnum.FLEET_MINUS);
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(2).setId(5L);
        game.getStacks().get(1).getCounters().get(2).setCountry("france");
        game.getStacks().get(1).getCounters().get(2).setType(CounterFaceTypeEnum.FORTRESS_4);
        game.getStacks().get(1).getCounters().get(2).setOwner(game.getStacks().get(1));
        game.getStacks().get(1).setProvince("idf");
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(0).setCountry("france");
        game.getTradeFleets().get(0).setProvince("ZPfrance");
        game.getTradeFleets().get(0).setLevel(5);
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(1).setCountry("angleterre");
        game.getTradeFleets().get(1).setProvince("ZPangleterre");
        game.getTradeFleets().get(1).setLevel(4);
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(2).setCountry("hollande");
        game.getTradeFleets().get(2).setProvince("ZPangleterre");
        game.getTradeFleets().get(2).setLevel(2);
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(3).setCountry("france");
        game.getTradeFleets().get(3).setProvince("ZPangleterre");
        game.getTradeFleets().get(3).setLevel(2);
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(4).setCountry("hollande");
        game.getTradeFleets().get(4).setProvince("ZPBaltique");
        game.getTradeFleets().get(4).setLevel(1);
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(5).setCountry("suede");
        game.getTradeFleets().get(5).setProvince("ZPBaltique");
        game.getTradeFleets().get(5).setLevel(1);
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(6).setCountry("angleterre");
        game.getTradeFleets().get(6).setProvince("ZPBaltique");
        game.getTradeFleets().get(6).setLevel(1);
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(7).setCountry("espagne");
        game.getTradeFleets().get(7).setProvince("ZPBaltique");
        game.getTradeFleets().get(7).setLevel(1);
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(8).setCountry("russie");
        game.getTradeFleets().get(8).setProvince("ZPBaltique");
        game.getTradeFleets().get(8).setLevel(1);
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(9).setCountry("ecosse");
        game.getTradeFleets().get(9).setProvince("ZPBaltique");
        game.getTradeFleets().get(9).setLevel(1);
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(10).setCountry("hanse");
        game.getTradeFleets().get(10).setProvince("ZPBaltique");
        game.getTradeFleets().get(10).setLevel(1);

        List<AdministrativeActionEntity> actionsFor11 = new ArrayList<>();
        AdministrativeActionEntity otherAction = new AdministrativeActionEntity();
        actionsFor11.add(otherAction);
        actionsFor11.add(otherAction);
        when(adminActionDao.findPlannedAdminActions(11L, 1, null, AdminActionTypeEnum.TFI)).thenReturn(actionsFor11);

        List<AdministrativeActionEntity> actionsFor12 = new ArrayList<>();
        actionsFor12.add(otherAction);
        when(adminActionDao.findPlannedAdminActions(12L, 1, null, AdminActionTypeEnum.TFI)).thenReturn(actionsFor12);

        request.getRequest().setType(AdminActionTypeEnum.TFI);

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setName("idf");
        when(provinceDao.getProvinceByName("idf")).thenReturn(idf);

        TradeZoneProvinceEntity zpFr = new TradeZoneProvinceEntity();
        zpFr.setName("ZPfrance");
        zpFr.setType(TradeZoneTypeEnum.ZP);
        zpFr.setCountryName("france");
        when(provinceDao.getProvinceByName("ZPfrance")).thenReturn(zpFr);

        TradeZoneProvinceEntity zpEn = new TradeZoneProvinceEntity();
        zpEn.setName("ZPangleterre");
        zpEn.setType(TradeZoneTypeEnum.ZP);
        zpEn.setCountryName("angleterre");
        when(provinceDao.getProvinceByName("ZPangleterre")).thenReturn(zpEn);

        TradeZoneProvinceEntity zmBal = new TradeZoneProvinceEntity();
        zmBal.setName("ZPBaltique");
        zmBal.setType(TradeZoneTypeEnum.ZP);
        zmBal.setSeaZone("s_baltique");
        when(provinceDao.getProvinceByName("ZPBaltique")).thenReturn(zmBal);

        Tables tables = new Tables();
        List<Limit> limits = new ArrayList<>();
        Limit limit = new Limit();
        limit.setCountry("angleterre");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(6);
        limit.setNumber(2);
        limit.setType(LimitTypeEnum.ACTION_TFI);
        limits.add(limit);
        limit = new Limit();
        limit.setCountry("france");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(6);
        limit.setNumber(2);
        limit.setType(LimitTypeEnum.ACTION_TFI);
        limits.add(limit);
        tables.getLimits().addAll(limits);
        EconomicServiceImpl.TABLES = tables;

        when(oeUtil.getDti(game, EconomicServiceImpl.TABLES, "france")).thenReturn(3);
        when(oeUtil.getDti(game, EconomicServiceImpl.TABLES, "angleterre")).thenReturn(3);

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        simulateDiff();

        request.getRequest().setInvestment(investment);
        request.getRequest().setProvince(province);

        DiffResponse response = economicService.addAdminAction(request);

        DiffEntity diffEntity = retrieveDiffCreated();

        InOrder inOrder = inOrder(gameDao, provinceDao, adminActionDao, oeUtil, diffDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, request.getGame().getIdCountry(), 1L);
        inOrder.verify(adminActionDao).findPlannedAdminActions(12L, 1, null, AdminActionTypeEnum.TFI);
        inOrder.verify(provinceDao).getProvinceByName(province);
        inOrder.verify(oeUtil).getDti(anyObject(), anyObject(), anyObject());
        inOrder.verify(adminActionDao).create(anyObject());
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.ADD, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.ADM_ACT, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(7, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_COUNTRY, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(request.getRequest().getIdCountry().toString(),
                            diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TURN, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals(game.getTurn().toString(), diffEntity.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diffEntity.getAttributes().get(2).getType());
        Assert.assertEquals(request.getRequest().getType().name(), diffEntity.getAttributes().get(2).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COST, diffEntity.getAttributes().get(3).getType());
        Assert.assertEquals(cost, diffEntity.getAttributes().get(3).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diffEntity.getAttributes().get(4).getType());
        Assert.assertEquals(request.getRequest().getProvince(), diffEntity.getAttributes().get(4).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COLUMN, diffEntity.getAttributes().get(5).getType());
        Assert.assertEquals(column, diffEntity.getAttributes().get(5).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.BONUS, diffEntity.getAttributes().get(6).getType());
        Assert.assertEquals(bonus, diffEntity.getAttributes().get(6).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
    }

    @Test
    public void testAddAdmActMnuFail() {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(createGameInfo());
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(11L);

        GameEntity game = createGameUsingMocks();
        game.setTurn(1);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setName("france");
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(11L);
        game.getCountries().get(1).setName("angleterre");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(2L);
        game.getStacks().get(0).getCounters().get(0).setCountry("angleterre");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setId(3L);
        game.getStacks().get(1).getCounters().get(0).setCountry("france");
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.MNU_ART_PLUS);
        game.getStacks().get(1).getCounters().get(0).setOwner(game.getStacks().get(1));
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(1).setId(4L);
        game.getStacks().get(1).getCounters().get(1).setCountry("france");
        game.getStacks().get(1).getCounters().get(1).setType(CounterFaceTypeEnum.FLEET_MINUS);
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(2).setId(5L);
        game.getStacks().get(1).getCounters().get(2).setCountry("france");
        game.getStacks().get(1).getCounters().get(2).setType(CounterFaceTypeEnum.MNU_WINE_MINUS);
        game.getStacks().get(1).getCounters().get(2).setOwner(game.getStacks().get(1));
        game.getStacks().get(1).getCounters().get(2).setOwner(game.getStacks().get(1));
        game.getStacks().get(1).setProvince("idf");
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(0).setCountry("france");
        game.getTradeFleets().get(0).setProvince("zp_france");
        game.getTradeFleets().get(0).setLevel(6);

        Map<String, Integer> ownedProvinces = new HashMap<>();
        ownedProvinces.put("provence", 5);
        ownedProvinces.put("lyonnais", 8);

        when(economicalSheetDao.getOwnedAndControlledProvinces("france", 12L)).thenReturn(ownedProvinces);

        List<AdministrativeActionEntity> actionsFor11 = new ArrayList<>();
        AdministrativeActionEntity action = new AdministrativeActionEntity();
        actionsFor11.add(action);
        when(adminActionDao.findPlannedAdminActions(11L, 1, null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI,
                AdminActionTypeEnum.DTI, AdminActionTypeEnum.EXL))
                .thenReturn(actionsFor11);

        List<AdministrativeActionEntity> actionsFor12 = new ArrayList<>();
        when(adminActionDao.findPlannedAdminActions(12L, 1, null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI,
                AdminActionTypeEnum.DTI, AdminActionTypeEnum.EXL))
                .thenReturn(actionsFor12);

        request.getRequest().setType(AdminActionTypeEnum.MNU);

        StackEntity stackOwnFor = new StackEntity();
        stackOwnFor.setProvince("controlledNotOwn");
        CounterEntity counterOwnFor = new CounterEntity();
        counterOwnFor.setType(CounterFaceTypeEnum.OWN);
        counterOwnFor.setCountry("angleterre");
        stackOwnFor.getCounters().add(counterOwnFor);
        game.getStacks().add(stackOwnFor);

        StackEntity stackOwn = new StackEntity();
        stackOwn.setProvince("ownedNotControlled");
        CounterEntity counterOwn = new CounterEntity();
        counterOwn.setType(CounterFaceTypeEnum.OWN);
        counterOwn.setCountry("france");
        stackOwn.getCounters().add(counterOwn);
        game.getStacks().add(stackOwn);

        StackEntity stackOwn2 = new StackEntity();
        stackOwn2.setProvince("owned");
        stackOwn2.getCounters().add(counterOwn);
        game.getStacks().add(stackOwn2);

        StackEntity stackCtrlFor = new StackEntity();
        stackCtrlFor.setProvince("ownedNotControlled");
        CounterEntity counterCtrlFor = new CounterEntity();
        counterCtrlFor.setType(CounterFaceTypeEnum.CONTROL);
        counterCtrlFor.setCountry("angleterre");
        stackCtrlFor.getCounters().add(counterCtrlFor);
        game.getStacks().add(stackCtrlFor);

        StackEntity stackCtrl = new StackEntity();
        stackCtrl.setProvince("controlledNotOwn");
        CounterEntity counterCtrl = new CounterEntity();
        counterCtrl.setType(CounterFaceTypeEnum.CONTROL);
        counterCtrl.setCountry("france");
        stackCtrl.getCounters().add(counterCtrl);
        game.getStacks().add(stackCtrl);

        StackEntity stackFortress = new StackEntity();
        CounterEntity counterFortress = new CounterEntity();
        counterFortress.setType(CounterFaceTypeEnum.FORTRESS_2);
        counterFortress.setCountry("france");
        stackFortress.getCounters().add(counterFortress);
        game.getStacks().add(stackFortress);

        EuropeanProvinceEntity controlledNotOwn = new EuropeanProvinceEntity();
        controlledNotOwn.setName("controlledNotOwn");
        when(provinceDao.getProvinceByName("controlledNotOwn")).thenReturn(controlledNotOwn);

        when(oeUtil.getStacksOnProvince(game, "controlledNotOwn")).thenReturn(Arrays.asList(stackOwnFor, stackCtrl));

        EuropeanProvinceEntity ownedNotControlled = new EuropeanProvinceEntity();
        ownedNotControlled.setName("ownedNotControlled");
        when(provinceDao.getProvinceByName("ownedNotControlled")).thenReturn(ownedNotControlled);

        when(oeUtil.getStacksOnProvince(game, "ownedNotControlled")).thenReturn(Arrays.asList(stackOwn, stackCtrlFor));

        EuropeanProvinceEntity owned = new EuropeanProvinceEntity();
        owned.setName("owned");
        when(provinceDao.getProvinceByName("owned")).thenReturn(owned);

        when(oeUtil.getStacksOnProvince(game, "owned")).thenReturn(Collections.singletonList(stackOwn));

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setName("idf");
        idf.setDefaultOwner("france");
        idf.setTerrain(TerrainEnum.DESERT);
        when(provinceDao.getProvinceByName("idf")).thenReturn(idf);

        Tables tables = new Tables();
        List<Limit> limits = new ArrayList<>();
        Limit limit = new Limit();
        limit.setCountry("angleterre");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(6);
        limit.setNumber(2);
        limit.setType(LimitTypeEnum.MAX_MNU);
        limits.add(limit);
        limit = new Limit();
        limit.setCountry("france");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(6);
        limit.setNumber(0);
        limit.setType(LimitTypeEnum.MAX_MNU);
        limits.add(limit);
        tables.getLimits().addAll(limits);
        EconomicServiceImpl.TABLES = tables;

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because domestic operation already planned");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.ACTION_ALREADY_PLANNED, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        request.getRequest().setIdCountry(12L);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because investment is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.investment", e.getParams()[0]);
        }

        request.getRequest().setInvestment(InvestmentEnum.M);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because counter face type is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.counterFaceType", e.getParams()[0]);
        }

        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.ARMY_MINUS);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because counter face type is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.counterFaceType", e.getParams()[0]);
        }

        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.MNU_CEREALS_MINUS);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because province is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("toto");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because province does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("controlledNotOwn");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because province is not owned and controlled");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PROVINCE_NOT_OWN_CONTROL, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("ownedNotControlled");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because province is not owned and controlled");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PROVINCE_NOT_OWN_CONTROL, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("owned");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because mnu limit was reached");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.COUNTER_LIMIT_EXCEED, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        limit.setNumber(2);
        request.getRequest().setProvince("idf");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because the mnu can't be placed in this province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.MNU_WRONG_PROVINCE, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        idf.setTerrain(TerrainEnum.PLAIN);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because the mnu should be placed on an empty province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.MNU_WRONG_PROVINCE, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.MNU_WOOD_MINUS);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because the mnu can't be placed in this province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.MNU_WRONG_PROVINCE, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        idf.setTerrain(TerrainEnum.SPARSE_FOREST);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because the mnu should be placed on an empty province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.MNU_WRONG_PROVINCE, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        idf.setTerrain(TerrainEnum.DENSE_FOREST);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because the mnu should be placed on an empty province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.MNU_WRONG_PROVINCE, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.MNU_SALT_MINUS);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because the mnu can't be placed in this province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.MNU_WRONG_PROVINCE, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        idf.setSalt(0);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because the mnu can't be placed in this province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.MNU_WRONG_PROVINCE, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        idf.setSalt(1);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because the mnu should be placed on an empty province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.MNU_WRONG_PROVINCE, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.MNU_FISH_MINUS);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because the mnu can't be placed in this province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.MNU_WRONG_PROVINCE, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        idf.getBorders().add(new BorderEntity());
        idf.getBorders().get(0).setProvinceFrom(idf);
        idf.getBorders().get(0).setProvinceTo(new EuropeanProvinceEntity());

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because the mnu can't be placed in this province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.MNU_WRONG_PROVINCE, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        idf.getBorders().add(new BorderEntity());
        EuropeanProvinceEntity sea = new EuropeanProvinceEntity();
        sea.setTerrain(TerrainEnum.SEA);
        idf.getBorders().get(1).setProvinceFrom(idf);
        idf.getBorders().get(1).setProvinceTo(sea);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because the mnu should be placed on an empty province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.MNU_WRONG_PROVINCE, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        idf.getBorders().get(1).setProvinceFrom(sea);
        idf.getBorders().get(1).setProvinceTo(idf);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because the mnu should be placed on an empty province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.MNU_WRONG_PROVINCE, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.MNU_ART_PLUS);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because the mnu can't be placed in this province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.MNU_WRONG_PROVINCE, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        idf.setIncome(4);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because the mnu can't be placed in this province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.MNU_WRONG_PROVINCE, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        idf.setIncome(5);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because the mnu should be placed on an empty province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.MNU_WRONG_PROVINCE, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        when(economicalSheetDao.getOwnedAndControlledProvinces("france", 12L))
                .thenReturn(new HashMap<>());

        Referential ref = new Referential();
        CountryReferential countryRef = new CountryReferential();
        countryRef.setName("france");
        LimitReferential limitRef = new LimitReferential();
        limitRef.setType(CounterTypeEnum.MNU_ART);
        limitRef.setNumber(1);
        countryRef.getLimits().add(limitRef);
        ref.getCountries().add(countryRef);
        EconomicServiceImpl.REFERENTIAL = ref;

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because the mnu limit is exceeded");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.COUNTER_LIMIT_EXCEED, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        limitRef.setNumber(2);

        try {
            economicService.addAdminAction(request);
        } catch (FunctionalException e) {
            Assert.fail("Should have worked.");
            e.printStackTrace();
        }
    }

    @Test
    public void testAddAdmActMnuSuccess1() throws FunctionalException {
        testAddAdmActMnuSuccess(10L, "turquie", null, 0, "B_PB_1D", 1, InvestmentEnum.M, "50", "-3", "-1");
    }

    @Test
    public void testAddAdmActMnuSuccess2() throws FunctionalException {
        testAddAdmActMnuSuccess(12L, "espagne", "toto", 0, "B_PB_1D", 1, InvestmentEnum.S, "30", "-4", "0");
    }

    @Test
    public void testAddAdmActMnuSuccess3() throws FunctionalException {
        testAddAdmActMnuSuccess(12L, "espagne", "B_STAB_0", 0, "B_PB_2G", 1, InvestmentEnum.L, "100", "-1", "-1");
    }

    @Test
    public void testAddAdmActMnuSuccess4() throws FunctionalException {
        testAddAdmActMnuSuccess(11L, "angleterre", "B_STAB_-3", -3, "B_PB_1D", 42, InvestmentEnum.S, "30", "4", "-3");
    }

    @Test
    public void testAddAdmActMnuSuccess5() throws FunctionalException {
        testAddAdmActMnuSuccess(11L, "angleterre", "B_STAB_3", 3, "B_PB_1D", 43, InvestmentEnum.L, "100", "4", "5");
    }

    @Test
    public void testAddAdmActMnuSuccess6() throws FunctionalException {
        testAddAdmActMnuSuccess(13L, "russie", null, 0, "B_PB_1D", 1, InvestmentEnum.M, "50", "-3", "0");
    }

    private void testAddAdmActMnuSuccess(Long idCountry, String country, String stabilityBox, int stability,
                                         String inflationBox, int turn, InvestmentEnum investment, String cost,
                                         String column, String bonus) throws FunctionalException {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(createGameInfo());
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(idCountry);
        request.getRequest().setInvestment(investment);
        request.getRequest().setType(AdminActionTypeEnum.MNU);
        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.MNU_METAL_MINUS);
        request.getRequest().setProvince("idf");

        GameEntity game = createGameUsingMocks();
        game.setTurn(turn);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        game.setStPeterProvince("Neva");
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setName("espagne");
        game.getCountries().get(0).setDti(2);
        game.getCountries().get(0).setMonarch(new MonarchEntity());
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(11L);
        game.getCountries().get(1).setName("angleterre");
        game.getCountries().get(1).setDti(5);
        game.getCountries().get(1).setMonarch(new MonarchEntity());
        game.getCountries().get(1).getMonarch().setAdministrative(9);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(2).setId(10L);
        game.getCountries().get(2).setName("turquie");
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(3).setId(13L);
        game.getCountries().get(3).setName("russie");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(2L);
        game.getStacks().get(0).getCounters().get(0).setCountry("angleterre");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setId(3L);
        game.getStacks().get(1).getCounters().get(0).setCountry(country);
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.MNU_METAL_MINUS);
        game.getStacks().get(1).getCounters().get(0).setOwner(game.getStacks().get(1));
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(1).setId(4L);
        game.getStacks().get(1).getCounters().get(1).setCountry("france");
        game.getStacks().get(1).getCounters().get(1).setType(CounterFaceTypeEnum.FLEET_MINUS);
        game.getStacks().get(1).getCounters().get(1).setOwner(game.getStacks().get(1));
        game.getStacks().get(1).setProvince("idf");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(2).setProvince(inflationBox);
        game.getStacks().get(2).getCounters().add(new CounterEntity());
        game.getStacks().get(2).getCounters().get(0).setType(CounterFaceTypeEnum.INFLATION);
        game.getStacks().get(2).getCounters().get(0).setOwner(game.getStacks().get(2));
        if (stabilityBox != null) {
            game.getStacks().add(new StackEntity());
            game.getStacks().get(3).setProvince(stabilityBox);
            game.getStacks().get(3).getCounters().add(new CounterEntity());
            game.getStacks().get(3).getCounters().get(0).setCountry(country);
            game.getStacks().get(3).getCounters().get(0).setType(CounterFaceTypeEnum.STABILITY);
            game.getStacks().get(3).getCounters().get(0).setOwner(game.getStacks().get(3));
        }
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(0).setCountry("france");
        game.getTradeFleets().get(0).setProvince("zp_france");
        game.getTradeFleets().get(0).setLevel(6);

        when(adminActionDao.findPlannedAdminActions(idCountry, turn, null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI,
                AdminActionTypeEnum.DTI, AdminActionTypeEnum.EXL))
                .thenReturn(new ArrayList<>());

        PlayableCountryEntity countryEntity = CommonUtil
                .findFirst(game.getCountries(), o -> StringUtils.equals(country, o.getName()));
        when(oeUtil.getAdministrativeValue(countryEntity))
                .thenReturn(new OEUtilImpl().getAdministrativeValue(countryEntity));
        when(oeUtil.getStability(game, country)).thenReturn(stability);

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setName("idf");
        idf.setDefaultOwner(country);
        idf.setTerrain(TerrainEnum.DESERT);
        when(provinceDao.getProvinceByName("idf")).thenReturn(idf);

        Tables tables = new Tables();
        List<Limit> limits = new ArrayList<>();
        Limit limit = new Limit();
        limit.setCountry(country);
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(6);
        limit.setNumber(2);
        limit.setType(LimitTypeEnum.MAX_MNU);
        limits.add(limit);
        tables.getLimits().addAll(limits);
        EconomicServiceImpl.TABLES = tables;

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        simulateDiff();

        DiffResponse response = economicService.addAdminAction(request);

        DiffEntity diffEntity = retrieveDiffCreated();

        InOrder inOrder = inOrder(gameDao, provinceDao, adminActionDao, diffDao, diffMapping, oeUtil);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, request.getGame().getIdCountry(), 1L);
        inOrder.verify(adminActionDao)
                .findPlannedAdminActions(idCountry, turn, null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI,
                       AdminActionTypeEnum.DTI, AdminActionTypeEnum.EXL);
        inOrder.verify(provinceDao).getProvinceByName("idf");
        inOrder.verify(oeUtil).getAdministrativeValue(countryEntity);
        inOrder.verify(oeUtil).getStability(game, country);
        inOrder.verify(adminActionDao).create(anyObject());
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.ADD, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.ADM_ACT, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(9, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_COUNTRY, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(request.getRequest().getIdCountry().toString(),
                            diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TURN, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals(game.getTurn().toString(), diffEntity.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diffEntity.getAttributes().get(2).getType());
        Assert.assertEquals(request.getRequest().getType().name(), diffEntity.getAttributes().get(2).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COST, diffEntity.getAttributes().get(3).getType());
        Assert.assertEquals(cost, diffEntity.getAttributes().get(3).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_OBJECT, diffEntity.getAttributes().get(4).getType());
        Assert.assertEquals("3", diffEntity.getAttributes().get(4).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diffEntity.getAttributes().get(5).getType());
        Assert.assertEquals(request.getRequest().getProvince(), diffEntity.getAttributes().get(5).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COUNTER_FACE_TYPE, diffEntity.getAttributes().get(6).getType());
        Assert.assertEquals(CounterFaceTypeEnum.MNU_METAL_MINUS.name(), diffEntity.getAttributes().get(6).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COLUMN, diffEntity.getAttributes().get(7).getType());
        Assert.assertEquals(column, diffEntity.getAttributes().get(7).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.BONUS, diffEntity.getAttributes().get(8).getType());
        Assert.assertEquals(bonus, diffEntity.getAttributes().get(8).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
    }

    @Test
    public void testAddAdmActDtiFtiFail() {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(createGameInfo());
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(11L);

        GameEntity game = createGameUsingMocks();
        game.setTurn(1);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setDti(3);
        game.getCountries().get(0).setFti(4);
        game.getCountries().get(0).setFtiRotw(5);
        game.getCountries().get(0).setName("france");
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(11L);
        game.getCountries().get(1).setName("angleterre");

        List<AdministrativeActionEntity> actionsFor11 = new ArrayList<>();
        AdministrativeActionEntity action = new AdministrativeActionEntity();
        actionsFor11.add(action);
        when(adminActionDao.findPlannedAdminActions(11L, 1, null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI,
                AdminActionTypeEnum.DTI, AdminActionTypeEnum.EXL))
                .thenReturn(actionsFor11);

        List<AdministrativeActionEntity> actionsFor12 = new ArrayList<>();
        when(adminActionDao.findPlannedAdminActions(12L, 1, null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI,
                AdminActionTypeEnum.DTI, AdminActionTypeEnum.EXL))
                .thenReturn(actionsFor12);

        request.getRequest().setType(AdminActionTypeEnum.DTI);

        Tables tables = new Tables();
        List<Limit> limits = new ArrayList<>();
        Limit limit = new Limit();
        limit.setCountry("angleterre");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(6);
        limit.setNumber(2);
        limit.setType(LimitTypeEnum.MAX_DTI);
        limits.add(limit);
        limit = new Limit();
        limit.setCountry("france");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(6);
        limit.setNumber(3);
        limit.setType(LimitTypeEnum.MAX_DTI);
        limits.add(limit);
        limit = new Limit();
        limit.setCountry("france");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(6);
        limit.setNumber(4);
        limit.setType(LimitTypeEnum.MAX_FTI);
        limits.add(limit);
        limit = new Limit();
        limit.setCountry("france");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(6);
        limit.setNumber(5);
        limit.setType(LimitTypeEnum.MAX_FTI_ROTW);
        limits.add(limit);
        tables.getLimits().addAll(limits);
        EconomicServiceImpl.TABLES = tables;

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because domestic operation already planned");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.ACTION_ALREADY_PLANNED, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        request.getRequest().setIdCountry(12L);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because investment is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.investment", e.getParams()[0]);
        }

        request.getRequest().setInvestment(InvestmentEnum.M);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because dti limit was reached");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.ADMIN_ACTION_LIMIT_EXCEED, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        request.getRequest().setType(AdminActionTypeEnum.FTI);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because fti limit was reached");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.ADMIN_ACTION_LIMIT_EXCEED, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }
    }

    @Test
    public void testAddAdmActDtiFtiSuccess() throws FunctionalException {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(createGameInfo());
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(12L);
        request.getRequest().setInvestment(InvestmentEnum.M);
        request.getRequest().setType(AdminActionTypeEnum.FTI);

        GameEntity game = createGameUsingMocks();
        game.setTurn(1);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setDti(3);
        game.getCountries().get(0).setFti(4);
        game.getCountries().get(0).setFtiRotw(4);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setMonarch(new MonarchEntity());
        game.getCountries().get(0).getMonarch().setAdministrative(7);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(11L);
        game.getCountries().get(1).setName("angleterre");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setProvince("B_STAB_2");
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setCountry("france");
        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.STABILITY);
        game.getStacks().get(0).getCounters().get(0).setOwner(game.getStacks().get(0));

        List<AdministrativeActionEntity> actionsFor12 = new ArrayList<>();
        when(adminActionDao.findPlannedAdminActions(12L, 1, null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI,
                AdminActionTypeEnum.DTI, AdminActionTypeEnum.EXL))
                .thenReturn(actionsFor12);

        when(oeUtil.getAdministrativeValue(game.getCountries().get(0))).thenReturn(7);
        when(oeUtil.getStability(game, "france")).thenReturn(2);

        request.getRequest().setType(AdminActionTypeEnum.FTI);

        Tables tables = new Tables();
        List<Limit> limits = new ArrayList<>();
        Limit limit = new Limit();
        limit.setCountry("angleterre");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(6);
        limit.setNumber(2);
        limit.setType(LimitTypeEnum.MAX_DTI);
        limits.add(limit);
        limit = new Limit();
        limit.setCountry("france");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(6);
        limit.setNumber(3);
        limit.setType(LimitTypeEnum.MAX_DTI);
        limits.add(limit);
        limit = new Limit();
        limit.setCountry("france");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(6);
        limit.setNumber(4);
        limit.setType(LimitTypeEnum.MAX_FTI);
        limits.add(limit);
        limit = new Limit();
        limit.setCountry("france");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(6);
        limit.setNumber(5);
        limit.setType(LimitTypeEnum.MAX_FTI_ROTW);
        limits.add(limit);
        tables.getLimits().addAll(limits);
        EconomicServiceImpl.TABLES = tables;

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        simulateDiff();

        DiffResponse response = economicService.addAdminAction(request);

        DiffEntity diffEntity = retrieveDiffCreated();

        InOrder inOrder = inOrder(gameDao, adminActionDao, diffDao, diffMapping, oeUtil);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, request.getGame().getIdCountry(), 1L);
        inOrder.verify(adminActionDao).findPlannedAdminActions(12L, 1, null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI,
                AdminActionTypeEnum.DTI, AdminActionTypeEnum.EXL);
        inOrder.verify(oeUtil).getAdministrativeValue(game.getCountries().get(0));
        inOrder.verify(oeUtil).getStability(game, "france");
        inOrder.verify(adminActionDao).create(anyObject());
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.ADD, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.ADM_ACT, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(6, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_COUNTRY, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(request.getRequest().getIdCountry().toString(),
                            diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TURN, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals(game.getTurn().toString(), diffEntity.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diffEntity.getAttributes().get(2).getType());
        Assert.assertEquals(request.getRequest().getType().name(), diffEntity.getAttributes().get(2).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COST, diffEntity.getAttributes().get(3).getType());
        Assert.assertEquals("50", diffEntity.getAttributes().get(3).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COLUMN, diffEntity.getAttributes().get(4).getType());
        Assert.assertEquals("2", diffEntity.getAttributes().get(4).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.BONUS, diffEntity.getAttributes().get(5).getType());
        Assert.assertEquals("2", diffEntity.getAttributes().get(5).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
    }

    @Test
    public void testAddAdmActExcTaxesFail() {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(createGameInfo());
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(11L);

        GameEntity game = createGameUsingMocks();
        game.setTurn(1);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setDti(3);
        game.getCountries().get(0).setFti(4);
        game.getCountries().get(0).setFtiRotw(5);
        game.getCountries().get(0).setName("france");
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(11L);
        game.getCountries().get(1).setName("angleterre");

        List<AdministrativeActionEntity> actionsFor11 = new ArrayList<>();
        AdministrativeActionEntity action = new AdministrativeActionEntity();
        actionsFor11.add(action);
        when(adminActionDao.findPlannedAdminActions(11L, 1, null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI,
                AdminActionTypeEnum.DTI, AdminActionTypeEnum.EXL))
                .thenReturn(actionsFor11);

        List<AdministrativeActionEntity> actionsFor12 = new ArrayList<>();
        when(adminActionDao.findPlannedAdminActions(12L, 1, null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI,
                AdminActionTypeEnum.DTI, AdminActionTypeEnum.EXL))
                .thenReturn(actionsFor12);

        when(oeUtil.getStability(game, "france")).thenReturn(-3);

        request.getRequest().setType(AdminActionTypeEnum.EXL);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because domestic operation already planned");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.ACTION_ALREADY_PLANNED, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        request.getRequest().setIdCountry(12L);
        when(oeUtil.getWarStatus(game, game.getCountries().get(0))).thenReturn(WarStatusEnum.PEACE);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because country is not at war");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.EXC_TAXES_NOT_AT_WAR, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        when(oeUtil.getWarStatus(game, game.getCountries().get(0))).thenReturn(WarStatusEnum.CLASSIC_WAR);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because stab is -3");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.INSUFFICIENT_STABILITY, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        List<String> enemies = new ArrayList<>();
        enemies.add("enemy");
        when(oeUtil.getEnemies(game.getCountries().get(0), game, true)).thenReturn(enemies);
        when(playableCountryDao.isFatherlandInDanger("france", enemies, 12L)).thenReturn(true);

        try {
            economicService.addAdminAction(request);
        } catch (FunctionalException e) {
            Assert.fail("Should not break. " + e.getMessage());
        }

        when(oeUtil.getWarStatus(game, game.getCountries().get(0))).thenReturn(WarStatusEnum.CIVIL_WAR);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because stab is -3");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.INSUFFICIENT_STABILITY, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }
    }

    @Test
    public void testAddAdmActExcTaxesSuccess() throws FunctionalException {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(createGameInfo());
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(12L);
        request.getRequest().setType(AdminActionTypeEnum.EXL);

        GameEntity game = createGameUsingMocks();
        game.setTurn(1);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setName("france");

        List<AdministrativeActionEntity> actionsFor12 = new ArrayList<>();
        when(adminActionDao.findPlannedAdminActions(12L, 1, null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI,
                AdminActionTypeEnum.DTI, AdminActionTypeEnum.EXL))
                .thenReturn(actionsFor12);

        when(oeUtil.getWarStatus(game, game.getCountries().get(0))).thenReturn(WarStatusEnum.CLASSIC_WAR);
        when(oeUtil.getStability(game, "france")).thenReturn(2);
        when(oeUtil.getAdministrativeValue(game.getCountries().get(0))).thenReturn(7);

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        simulateDiff();

        DiffResponse response = economicService.addAdminAction(request);

        DiffEntity diffEntity = retrieveDiffCreated();

        InOrder inOrder = inOrder(gameDao, adminActionDao, diffDao, diffMapping, oeUtil);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, request.getGame().getIdCountry(), 1L);
        inOrder.verify(adminActionDao).findPlannedAdminActions(12L, 1, null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI,
                AdminActionTypeEnum.DTI, AdminActionTypeEnum.EXL);
        inOrder.verify(oeUtil).getStability(game, "france");
        inOrder.verify(oeUtil).getAdministrativeValue(game.getCountries().get(0));
        inOrder.verify(adminActionDao).create(anyObject());
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.ADD, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.ADM_ACT, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(5, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_COUNTRY, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(request.getRequest().getIdCountry().toString(),
                            diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TURN, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals(game.getTurn().toString(), diffEntity.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diffEntity.getAttributes().get(2).getType());
        Assert.assertEquals(request.getRequest().getType().name(), diffEntity.getAttributes().get(2).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COLUMN, diffEntity.getAttributes().get(3).getType());
        Assert.assertEquals("-1", diffEntity.getAttributes().get(3).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.BONUS, diffEntity.getAttributes().get(4).getType());
        Assert.assertEquals("10", diffEntity.getAttributes().get(4).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
    }

    @Test
    public void testAddAdmActColFail() {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(createGameInfo());
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(11L);

        GameEntity game = createGameUsingMocks();
        game.setTurn(1);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setDti(3);
        game.getCountries().get(0).setFti(4);
        game.getCountries().get(0).setFtiRotw(5);
        game.getCountries().get(0).setName("france");
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(11L);
        game.getCountries().get(1).setName("angleterre");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setProvince("virginia");
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.COLONY_MINUS);
        game.getStacks().get(0).getCounters().get(0).setCountry("angleterre");
        game.getStacks().get(0).getCounters().get(0).setOwner(game.getStacks().get(0));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).setProvince("quebec");
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.COLONY_MINUS);
        game.getStacks().get(1).getCounters().get(0).setCountry("france");
        game.getStacks().get(1).getCounters().get(0).setOwner(game.getStacks().get(1));
        EstablishmentEntity establishment = new EstablishmentEntity();
        establishment.setLevel(2);
        game.getStacks().get(1).getCounters().get(0).setEstablishment(establishment);

        Tables tables = new Tables();
        List<Limit> limits = new ArrayList<>();
        Limit limit = new Limit();
        limit.setCountry("angleterre");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(6);
        limit.setNumber(2);
        limit.setType(LimitTypeEnum.ACTION_COL);
        limits.add(limit);
        limit = new Limit();
        limit.setCountry("france");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(6);
        limit.setNumber(2);
        limit.setType(LimitTypeEnum.ACTION_COL);
        limits.add(limit);
        Limit limitCol = new Limit();
        limitCol.setCountry("france");
        limitCol.setPeriod(new Period());
        limitCol.getPeriod().setBegin(1);
        limitCol.getPeriod().setEnd(6);
        limitCol.setNumber(1);
        limitCol.setType(LimitTypeEnum.MAX_COL);
        limits.add(limitCol);
        tables.getLimits().addAll(limits);
        List<Period> periods = new ArrayList<>();
        Period period = new Period();
        period.setName(Period.PERIOD_I);
        period.setBegin(1);
        period.setEnd(6);
        periods.add(period);
        tables.getPeriods().addAll(periods);
        EconomicServiceImpl.TABLES = tables;

        List<AdministrativeActionEntity> actionsFor11 = new ArrayList<>();
        AdministrativeActionEntity action = new AdministrativeActionEntity();
        actionsFor11.add(action);
        actionsFor11.add(action);
        when(adminActionDao.findPlannedAdminActions(11L, 1, null, AdminActionTypeEnum.COL)).thenReturn(actionsFor11);

        List<AdministrativeActionEntity> actionsFor12 = new ArrayList<>();
        actionsFor12.add(action);
        when(adminActionDao.findPlannedAdminActions(12L, 1, null, AdminActionTypeEnum.COL)).thenReturn(actionsFor12);

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setName("IdF");
        idf.setDefaultOwner("france");
        idf.setPort(false);
        when(provinceDao.getProvinceByName("IdF")).thenReturn(idf);

        RotwProvinceEntity virginia = new RotwProvinceEntity();
        virginia.setName("virginia");
        when(provinceDao.getProvinceByName("virginia")).thenReturn(virginia);

        RotwProvinceEntity quebec = new RotwProvinceEntity();
        quebec.setName("quebec");
        quebec.setRegion("Canada");
        when(provinceDao.getProvinceByName("quebec")).thenReturn(quebec);

        when(provinceDao.getRegionByName("Canada")).thenReturn(new RegionEntity());

        RotwProvinceEntity terreneuve = new RotwProvinceEntity();
        terreneuve.setName("terreneuve");
        when(provinceDao.getProvinceByName("terreneuve")).thenReturn(terreneuve);

        List<String> sources = new ArrayList<>();
        sources.add("Idf");
        sources.add("Lyonnais");
        when(playableCountryDao.getOwnedProvinces("france", 12L)).thenReturn(sources);

        request.getRequest().setType(AdminActionTypeEnum.COL);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because limit exceeded");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.ADMIN_ACTION_LIMIT_EXCEED, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        request.getRequest().setIdCountry(12L);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because investment is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.investment", e.getParams()[0]);
        }

        request.getRequest().setInvestment(InvestmentEnum.M);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because province is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("toto");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because province does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("IdF");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because province is not a rotw province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PROVINCE_WRONG_TYPE, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("terreneuve");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because there the number of counter is reached");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.COUNTER_LIMIT_EXCEED, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        limitCol.setNumber(2);
        request.getRequest().setProvince("virginia");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because there is a colony of another player on it");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PROVINCE_NOT_OWN_CONTROL, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("quebec");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because of pioneering rules");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PIONEERING, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        period.setName(Period.PERIOD_VI);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because of settlement rules");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.SETTLEMENTS, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        period.setName(Period.PERIOD_V);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because of pioneering rules");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PIONEERING, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("terreneuve");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because of settlement rules");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.SETTLEMENTS, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("quebec");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because of pioneering rules");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PIONEERING, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        establishment.setLevel(null);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because of settlement rules");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.SETTLEMENTS, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        establishment.setLevel(1);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because of settlement rules");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.SETTLEMENTS, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        establishment.setLevel(2);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because of pioneering rules");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PIONEERING, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        when(provinceDao.getGoldInProvince("quebec")).thenReturn(new GoldEntity());

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because of settlement rules");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.SETTLEMENTS, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        when(oeUtil.canSettle(quebec, new ArrayList<>(), sources, new ArrayList<>())).thenReturn(true);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because of inland advance rules");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.INLAND_ADVANCE, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        quebec.getBorders().add(new BorderEntity());
        quebec.getBorders().get(0).setProvinceTo(new RotwProvinceEntity());

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because of inland advance rules");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.INLAND_ADVANCE, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        quebec.getBorders().add(new BorderEntity());
        quebec.getBorders().get(1).setProvinceTo(new RotwProvinceEntity());
        quebec.getBorders().get(1).getProvinceTo().setTerrain(TerrainEnum.SEA);

        quebec.getBorders().add(new BorderEntity());
        quebec.getBorders().get(2).setProvinceTo(new RotwProvinceEntity());
        quebec.getBorders().get(2).getProvinceTo().setTerrain(TerrainEnum.PLAIN);

        try {
            economicService.addAdminAction(request);
        } catch (FunctionalException e) {
            Assert.fail("Should have worked.");
            e.printStackTrace();
        }

        quebec.getBorders().get(1).getProvinceTo().setTerrain(TerrainEnum.DENSE_FOREST);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because of inland advance rules");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.INLAND_ADVANCE, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        List<String> countries = new ArrayList<>();
        countries.add("angleterre");
        when(adminActionDao.getCountriesInlandAdvance("quebec", 12L)).thenReturn(countries);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because of inland advance rules");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.INLAND_ADVANCE, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        countries.add("france");

        try {
            economicService.addAdminAction(request);
        } catch (FunctionalException e) {
            e.printStackTrace();
            Assert.fail("Should have worked.");
        }
    }

    @Test
    public void testAddAdmActColSuccessWithoutLeader() throws FunctionalException {
        testAddAdmActColSuccess(new ArrayList<>(), false, 0);
    }

    @Test
    public void testAddAdmActColSuccessWithExplorer() throws FunctionalException {
        testAddAdmActColSuccess(Collections.singletonList(new ImmutablePair<>(LeaderTypeEnum.EXPLORER, 2)), false, 1);
    }

    @Test
    public void testAddAdmActColSuccessWithBadExplorer() throws FunctionalException {
        testAddAdmActColSuccess(Collections.singletonList(new ImmutablePair<>(LeaderTypeEnum.EXPLORER, 1)), false, 0);
    }

    @Test
    public void testAddAdmActColSuccessWithExplorers() throws FunctionalException {
        testAddAdmActColSuccess(Arrays.asList(new ImmutablePair<>(LeaderTypeEnum.EXPLORER, 2),
                new ImmutablePair<>(LeaderTypeEnum.EXPLORER, 5)), true, 2);
    }

    @Test
    public void testAddAdmActColSuccessWithExplorerAndConq() throws FunctionalException {
        testAddAdmActColSuccess(Arrays.asList(new ImmutablePair<>(LeaderTypeEnum.EXPLORER, 2),
                new ImmutablePair<>(LeaderTypeEnum.CONQUISTADOR, 2)), true, 2);
    }

    @Test
    public void testAddAdmActColSuccessWithAll() throws FunctionalException {
        testAddAdmActColSuccess(Arrays.asList(new ImmutablePair<>(LeaderTypeEnum.EXPLORER, 2),
                new ImmutablePair<>(LeaderTypeEnum.CONQUISTADOR, 2),
                new ImmutablePair<>(LeaderTypeEnum.GOVERNOR, 3)), true, 3);
    }

    @Test
    public void testAddAdmActColSuccessWithGovernorInRegion() throws FunctionalException {
        testAddAdmActColSuccess(new ArrayList<>(), true, 1);
    }

    @Test
    public void testAddAdmActColSuccessWithBadExplorerButGovernorInRegion() throws FunctionalException {
        testAddAdmActColSuccess(Collections.singletonList(new ImmutablePair<>(LeaderTypeEnum.EXPLORER, 1)), true, 1);
    }

    private void testAddAdmActColSuccess(List<Pair<LeaderTypeEnum, Integer>> leaders, boolean governorInProvince, int bonusFromLeader) throws FunctionalException {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(createGameInfo());
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(12L);
        request.getRequest().setInvestment(InvestmentEnum.M);
        request.getRequest().setProvince("quebec");
        request.getRequest().setType(AdminActionTypeEnum.COL);

        GameEntity game = createGameUsingMocks();
        game.setTurn(1);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setDti(3);
        game.getCountries().get(0).setFti(4);
        game.getCountries().get(0).setFtiRotw(5);
        game.getCountries().get(0).setColonisationPenalty(2);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).getDiscoveries().add(new DiscoveryEntity());
        game.getCountries().get(0).getDiscoveries().get(0).setProvince("atlantique");
        game.getCountries().get(0).getDiscoveries().get(0).setTurn(2);
        game.getCountries().get(0).getDiscoveries().add(new DiscoveryEntity());
        game.getCountries().get(0).getDiscoveries().get(1).setProvince("pacifique");
        game.getCountries().get(0).getDiscoveries().get(1).setStack(new StackEntity());
        game.getCountries().get(0).getDiscoveries().add(new DiscoveryEntity());
        game.getCountries().get(0).getDiscoveries().get(2).setProvince("indien");
        game.getCountries().get(0).getDiscoveries().get(2).setTurn(2);
        game.getCountries().get(0).getDiscoveries().get(2).setStack(new StackEntity());
        game.getCountries().get(0).getDiscoveries().add(new DiscoveryEntity());
        game.getCountries().get(0).getDiscoveries().get(3).setProvince("quebec");
        game.getCountries().get(0).getDiscoveries().get(3).setTurn(3);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(11L);
        game.getCountries().get(1).setName("angleterre");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setProvince("virginia");
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.COLONY_MINUS);
        game.getStacks().get(0).getCounters().get(0).setCountry("angleterre");
        game.getStacks().get(0).getCounters().get(0).setOwner(game.getStacks().get(0));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).setProvince("quebec");
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.COLONY_MINUS);
        game.getStacks().get(1).getCounters().get(0).setCountry("france");
        game.getStacks().get(1).getCounters().get(0).setId(666L);
        game.getStacks().get(1).getCounters().get(0).setOwner(game.getStacks().get(1));
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(1).setType(CounterFaceTypeEnum.ARSENAL_2);
        game.getStacks().get(1).getCounters().get(1).setCountry("france");
        game.getStacks().get(1).getCounters().get(1).setOwner(game.getStacks().get(1));
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(2).setType(CounterFaceTypeEnum.MISSION);
        game.getStacks().get(1).getCounters().get(2).setCountry("france");
        game.getStacks().get(1).getCounters().get(2).setOwner(game.getStacks().get(1));
        EstablishmentEntity establishment = new EstablishmentEntity();
        establishment.setLevel(2);
        game.getStacks().get(1).getCounters().get(0).setEstablishment(establishment);
        int index = 1;
        Tables tables = new Tables();
        CounterEntity counter = new CounterEntity();
        counter.setType(CounterFaceTypeEnum.LEADER);
        counter.setCountry("espagne");
        String code = "Leader-" + index;
        counter.setCode(code);
        counter.setOwner(game.getStacks().get(1));
        game.getStacks().get(1).getCounters().add(counter);

        Leader lead = new Leader();
        lead.setCode(code);
        lead.setType(LeaderTypeEnum.CONQUISTADOR);
        lead.setManoeuvre(5);
        tables.getLeaders().add(lead);
        index++;
        for (Pair<LeaderTypeEnum, Integer> leader : leaders) {
            counter = new CounterEntity();
            counter.setType(CounterFaceTypeEnum.LEADER);
            counter.setCountry("france");
            code = "Leader-" + index;
            counter.setCode(code);
            counter.setOwner(game.getStacks().get(1));
            game.getStacks().get(1).getCounters().add(counter);

            lead = new Leader();
            lead.setCode(code);
            lead.setType(leader.getLeft());
            lead.setManoeuvre(leader.getRight());
            tables.getLeaders().add(lead);

            index++;
        }
        game.getOtherForces().add(new OtherForcesEntity());
        game.getOtherForces().get(0).setProvince("quebec");
        game.getOtherForces().get(0).setType(OtherForcesTypeEnum.NATIVES);

        when(counterDao.isGovernorInSameRegion("Canada", "france", game.getId())).thenReturn(governorInProvince);

        List<Limit> limits = new ArrayList<>();
        Limit limit = new Limit();
        limit.setCountry("angleterre");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(6);
        limit.setNumber(2);
        limit.setType(LimitTypeEnum.ACTION_COL);
        limits.add(limit);
        limit = new Limit();
        limit.setCountry("france");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(6);
        limit.setNumber(2);
        limit.setType(LimitTypeEnum.ACTION_COL);
        limits.add(limit);
        tables.getLimits().addAll(limits);
        List<Period> periods = new ArrayList<>();
        Period period = new Period();
        period.setName(Period.PERIOD_I);
        period.setBegin(1);
        period.setEnd(6);
        periods.add(period);
        tables.getPeriods().addAll(periods);
        EconomicServiceImpl.TABLES = tables;

        List<AdministrativeActionEntity> actionsFor12 = new ArrayList<>();
        actionsFor12.add(new AdministrativeActionEntity());
        when(adminActionDao.findPlannedAdminActions(12L, 1, null, AdminActionTypeEnum.COL)).thenReturn(actionsFor12);

        RotwProvinceEntity quebec = new RotwProvinceEntity();
        quebec.setName("quebec");
        quebec.setRegion("Canada");
        quebec.getBorders().add(new BorderEntity());
        quebec.getBorders().get(0).setProvinceTo(new RotwProvinceEntity());
        quebec.getBorders().add(new BorderEntity());
        quebec.getBorders().get(1).setProvinceTo(new RotwProvinceEntity());
        quebec.getBorders().get(1).getProvinceTo().setTerrain(TerrainEnum.DENSE_FOREST);
        quebec.getBorders().add(new BorderEntity());
        quebec.getBorders().get(2).setProvinceTo(new RotwProvinceEntity());
        quebec.getBorders().get(2).getProvinceTo().setTerrain(TerrainEnum.PLAIN);
        when(provinceDao.getProvinceByName("quebec")).thenReturn(quebec);

        RegionEntity region = new RegionEntity();
        region.setDifficulty(7);
        when(provinceDao.getRegionByName("Canada")).thenReturn(region);

        List<String> sources = new ArrayList<>();
        sources.add("Idf");
        sources.add("Lyonnais");
        when(playableCountryDao.getOwnedProvinces("france", 12L)).thenReturn(sources);

        List<String> discoveries = new ArrayList<>();
        discoveries.add("atlantique");
        discoveries.add("quebec");
        when(oeUtil.canSettle(quebec, discoveries, sources, new ArrayList<>())).thenReturn(true);

        List<String> countries = new ArrayList<>();
        countries.add("angleterre");
        countries.add("france");
        when(adminActionDao.getCountriesInlandAdvance("quebec", 12L)).thenReturn(countries);

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        simulateDiff();

        DiffResponse response = economicService.addAdminAction(request);

        DiffEntity diffEntity = retrieveDiffCreated();

        InOrder inOrder = inOrder(gameDao, adminActionDao, provinceDao, playableCountryDao, diffDao, diffMapping,
                                  oeUtil);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, request.getGame().getIdCountry(), 1L);
        inOrder.verify(adminActionDao).findPlannedAdminActions(12L, 1, null, AdminActionTypeEnum.COL);
        inOrder.verify(provinceDao).getProvinceByName("quebec");
        inOrder.verify(provinceDao).getGoldInProvince("quebec");
        inOrder.verify(playableCountryDao).getOwnedProvinces("france", 12L);
        inOrder.verify(oeUtil).canSettle(quebec, discoveries, sources, new ArrayList<>());
        inOrder.verify(adminActionDao).getCountriesInlandAdvance("quebec", 12L);
        inOrder.verify(provinceDao).getRegionByName("Canada");
        inOrder.verify(adminActionDao).create(anyObject());
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.ADD, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.ADM_ACT, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(8, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_COUNTRY, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(request.getRequest().getIdCountry().toString(),
                            diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TURN, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals(game.getTurn().toString(), diffEntity.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diffEntity.getAttributes().get(2).getType());
        Assert.assertEquals(request.getRequest().getType().name(), diffEntity.getAttributes().get(2).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COST, diffEntity.getAttributes().get(3).getType());
        Assert.assertEquals("50", diffEntity.getAttributes().get(3).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_OBJECT, diffEntity.getAttributes().get(4).getType());
        Assert.assertEquals("666", diffEntity.getAttributes().get(4).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diffEntity.getAttributes().get(5).getType());
        Assert.assertEquals("quebec", diffEntity.getAttributes().get(5).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COLUMN, diffEntity.getAttributes().get(6).getType());
        Assert.assertEquals("-1", diffEntity.getAttributes().get(6).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.BONUS, diffEntity.getAttributes().get(7).getType());
        Assert.assertEquals(2 + bonusFromLeader + "", diffEntity.getAttributes().get(7).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
    }

    @Test
    public void testAddAdmActTpFail() {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(createGameInfo());
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(11L);

        GameEntity game = createGameUsingMocks();
        game.setTurn(1);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setDti(3);
        game.getCountries().get(0).setFti(4);
        game.getCountries().get(0).setFtiRotw(5);
        game.getCountries().get(0).setName("france");
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(11L);
        game.getCountries().get(1).setName("angleterre");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setProvince("virginia");
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.COLONY_MINUS);
        game.getStacks().get(0).getCounters().get(0).setCountry("angleterre");
        game.getStacks().get(0).getCounters().get(0).setOwner(game.getStacks().get(0));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).setProvince("quebec");
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.COLONY_MINUS);
        game.getStacks().get(1).getCounters().get(0).setCountry("france");
        game.getStacks().get(1).getCounters().get(0).setOwner(game.getStacks().get(1));
        EstablishmentEntity establishment = new EstablishmentEntity();
        establishment.setLevel(2);
        game.getStacks().get(1).getCounters().get(0).setEstablishment(establishment);

        Tables tables = new Tables();
        List<Limit> limits = new ArrayList<>();
        Limit limit = new Limit();
        limit.setCountry("angleterre");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(6);
        limit.setNumber(2);
        limit.setType(LimitTypeEnum.ACTION_TP);
        limits.add(limit);
        limit = new Limit();
        limit.setCountry("france");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(6);
        limit.setNumber(2);
        limit.setType(LimitTypeEnum.ACTION_TP);
        limits.add(limit);
        Limit limitCol = new Limit();
        limitCol.setCountry("france");
        limitCol.setPeriod(new Period());
        limitCol.getPeriod().setBegin(1);
        limitCol.getPeriod().setEnd(6);
        limitCol.setNumber(0);
        limitCol.setType(LimitTypeEnum.MAX_TP);
        limits.add(limitCol);
        tables.getLimits().addAll(limits);
        EconomicServiceImpl.TABLES = tables;

        List<AdministrativeActionEntity> actionsFor11 = new ArrayList<>();
        AdministrativeActionEntity action = new AdministrativeActionEntity();
        actionsFor11.add(action);
        actionsFor11.add(action);
        when(adminActionDao.findPlannedAdminActions(11L, 1, null, AdminActionTypeEnum.TP)).thenReturn(actionsFor11);

        List<AdministrativeActionEntity> actionsFor12 = new ArrayList<>();
        actionsFor12.add(action);
        when(adminActionDao.findPlannedAdminActions(12L, 1, null, AdminActionTypeEnum.TP)).thenReturn(actionsFor12);

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setName("IdF");
        idf.setDefaultOwner("france");
        idf.setPort(false);
        when(provinceDao.getProvinceByName("IdF")).thenReturn(idf);

        RotwProvinceEntity virginia = new RotwProvinceEntity();
        virginia.setName("virginia");
        when(provinceDao.getProvinceByName("virginia")).thenReturn(virginia);

        RotwProvinceEntity quebec = new RotwProvinceEntity();
        quebec.setName("quebec");
        quebec.setRegion("Canada");
        when(provinceDao.getProvinceByName("quebec")).thenReturn(quebec);

        when(provinceDao.getRegionByName("Canada")).thenReturn(new RegionEntity());

        List<String> sources = new ArrayList<>();
        sources.add("Idf");
        sources.add("Lyonnais");
        when(playableCountryDao.getOwnedProvinces("france", 12L)).thenReturn(sources);

        request.getRequest().setType(AdminActionTypeEnum.TP);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because limit exceeded");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.ADMIN_ACTION_LIMIT_EXCEED, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        request.getRequest().setIdCountry(12L);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because investment is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.investment", e.getParams()[0]);
        }

        request.getRequest().setInvestment(InvestmentEnum.M);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because province is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("toto");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because province does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("IdF");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because province is not a rotw province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PROVINCE_WRONG_TYPE, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("virginia");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because there the number of counter is reached");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.COUNTER_LIMIT_EXCEED, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        limitCol.setNumber(2);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because there is a colony of another player on it");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PROVINCE_NOT_OWN_CONTROL, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("quebec");

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because of settlement rules");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.SETTLEMENTS, e.getCode());
            Assert.assertEquals("addAdminAction.request.province", e.getParams()[0]);
        }

        when(oeUtil.canSettle(quebec, new ArrayList<>(), sources, new ArrayList<>())).thenReturn(true);

        try {
            economicService.addAdminAction(request);
        } catch (FunctionalException e) {
            Assert.fail("Should have worked.");
            e.printStackTrace();
        }
    }

    @Test
    public void testAddAdmActTpSuccess() throws FunctionalException {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(createGameInfo());
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(12L);
        request.getRequest().setInvestment(InvestmentEnum.M);
        request.getRequest().setProvince("quebec");
        request.getRequest().setType(AdminActionTypeEnum.TP);

        GameEntity game = createGameUsingMocks();
        game.setTurn(1);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setDti(3);
        game.getCountries().get(0).setFti(4);
        game.getCountries().get(0).setFtiRotw(5);
        game.getCountries().get(0).setColonisationPenalty(2);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).getDiscoveries().add(new DiscoveryEntity());
        game.getCountries().get(0).getDiscoveries().get(0).setProvince("atlantique");
        game.getCountries().get(0).getDiscoveries().get(0).setTurn(2);
        game.getCountries().get(0).getDiscoveries().add(new DiscoveryEntity());
        game.getCountries().get(0).getDiscoveries().get(1).setProvince("pacifique");
        game.getCountries().get(0).getDiscoveries().get(1).setStack(new StackEntity());
        game.getCountries().get(0).getDiscoveries().add(new DiscoveryEntity());
        game.getCountries().get(0).getDiscoveries().get(2).setProvince("indien");
        game.getCountries().get(0).getDiscoveries().get(2).setTurn(2);
        game.getCountries().get(0).getDiscoveries().get(2).setStack(new StackEntity());
        game.getCountries().get(0).getDiscoveries().add(new DiscoveryEntity());
        game.getCountries().get(0).getDiscoveries().get(3).setProvince("quebec");
        game.getCountries().get(0).getDiscoveries().get(3).setTurn(3);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(11L);
        game.getCountries().get(1).setName("angleterre");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setProvince("virginia");
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.COLONY_MINUS);
        game.getStacks().get(0).getCounters().get(0).setCountry("angleterre");
        game.getStacks().get(0).getCounters().get(0).setOwner(game.getStacks().get(0));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).setProvince("quebec");
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.TRADING_POST_PLUS);
        game.getStacks().get(1).getCounters().get(0).setCountry("france");
        game.getStacks().get(1).getCounters().get(0).setId(666L);
        game.getStacks().get(1).getCounters().get(0).setOwner(game.getStacks().get(1));
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(1).setType(CounterFaceTypeEnum.ARSENAL_2);
        game.getStacks().get(1).getCounters().get(1).setCountry("france");
        game.getStacks().get(1).getCounters().get(1).setOwner(game.getStacks().get(1));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(2).setProvince("quebec");
        game.getStacks().get(2).getCounters().add(new CounterEntity());
        game.getStacks().get(2).getCounters().get(0).setType(CounterFaceTypeEnum.ARMY_MINUS);
        game.getStacks().get(2).getCounters().get(0).setCountry("angleterre");
        game.getStacks().get(2).getCounters().add(new CounterEntity());
        game.getStacks().get(2).getCounters().get(1).setType(CounterFaceTypeEnum.LAND_SEPOY_EXPLORATION);
        game.getStacks().get(2).getCounters().get(1).setCountry("angleterre");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(3).setProvince("carolina");
        game.getStacks().get(3).getCounters().add(new CounterEntity());
        game.getStacks().get(3).getCounters().get(0).setType(CounterFaceTypeEnum.FORT);
        game.getStacks().get(3).getCounters().get(0).setCountry("angleterre");
        game.getStacks().get(3).getCounters().get(0).setOwner(game.getStacks().get(3));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(4).setProvince("terreneuve");
        game.getStacks().get(4).getCounters().add(new CounterEntity());
        game.getStacks().get(4).getCounters().get(0).setType(CounterFaceTypeEnum.FORT);
        game.getStacks().get(4).getCounters().get(0).setCountry("france");
        game.getStacks().get(4).getCounters().get(0).setOwner(game.getStacks().get(4));
        EstablishmentEntity establishment = new EstablishmentEntity();
        establishment.setLevel(2);

        Tables tables = new Tables();
        CounterEntity counter = new CounterEntity();
        counter.setType(CounterFaceTypeEnum.LEADER);
        counter.setCountry("france");
        String code = "Leader-1";
        counter.setCode(code);
        counter.setOwner(game.getStacks().get(1));
        game.getStacks().get(1).getCounters().add(counter);

        Leader lead = new Leader();
        lead.setCode(code);
        lead.setType(LeaderTypeEnum.CONQUISTADOR);
        lead.setManoeuvre(1);
        tables.getLeaders().add(lead);

        game.getStacks().get(1).getCounters().get(0).setEstablishment(establishment);
        game.getOtherForces().add(new OtherForcesEntity());
        game.getOtherForces().get(0).setProvince("quebec");
        game.getOtherForces().get(0).setType(OtherForcesTypeEnum.NATIVES);

        List<Limit> limits = new ArrayList<>();
        Limit limit = new Limit();
        limit.setCountry("angleterre");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(6);
        limit.setNumber(2);
        limit.setType(LimitTypeEnum.ACTION_TP);
        limits.add(limit);
        limit = new Limit();
        limit.setCountry("france");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(6);
        limit.setNumber(2);
        limit.setType(LimitTypeEnum.ACTION_TP);
        limits.add(limit);
        tables.getLimits().addAll(limits);
        EconomicServiceImpl.TABLES = tables;

        List<AdministrativeActionEntity> actionsFor12 = new ArrayList<>();
        actionsFor12.add(new AdministrativeActionEntity());
        when(adminActionDao.findPlannedAdminActions(12L, 1, null, AdminActionTypeEnum.COL)).thenReturn(actionsFor12);

        RotwProvinceEntity quebec = new RotwProvinceEntity();
        quebec.setName("quebec");
        quebec.setRegion("Canada");
        when(provinceDao.getProvinceByName("quebec")).thenReturn(quebec);

        RegionEntity region = new RegionEntity();
        region.setDifficulty(7);
        region.setTolerance(5);
        when(provinceDao.getRegionByName("Canada")).thenReturn(region);

        List<String> sources = new ArrayList<>();
        sources.add("Idf");
        sources.add("Lyonnais");
        when(playableCountryDao.getOwnedProvinces("france", 12L)).thenReturn(sources);

        List<String> forts = new ArrayList<>();
        forts.add("terreneuve");

        List<String> discoveries = new ArrayList<>();
        discoveries.add("atlantique");
        discoveries.add("quebec");
        when(oeUtil.canSettle(quebec, discoveries, sources, forts)).thenReturn(true);

        when(adminActionDao.countOtherTpsInRegion("france", "Canada", 12L)).thenReturn(3);

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        simulateDiff();

        DiffResponse response = economicService.addAdminAction(request);

        DiffEntity diffEntity = retrieveDiffCreated();

        InOrder inOrder = inOrder(gameDao, adminActionDao, provinceDao, playableCountryDao, diffDao, diffMapping,
                                  oeUtil);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, request.getGame().getIdCountry(), 1L);
        inOrder.verify(adminActionDao).findPlannedAdminActions(12L, 1, null, AdminActionTypeEnum.TP);
        inOrder.verify(provinceDao).getProvinceByName("quebec");
        inOrder.verify(playableCountryDao).getOwnedProvinces("france", 12L);
        inOrder.verify(oeUtil).canSettle(quebec, discoveries, sources, forts);
        inOrder.verify(provinceDao).getRegionByName("Canada");
        inOrder.verify(adminActionDao).countOtherTpsInRegion("france", "Canada", 12L);
        inOrder.verify(adminActionDao).create(anyObject());
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.ADD, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.ADM_ACT, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(8, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_COUNTRY, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(request.getRequest().getIdCountry().toString(),
                            diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TURN, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals(game.getTurn().toString(), diffEntity.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diffEntity.getAttributes().get(2).getType());
        Assert.assertEquals(request.getRequest().getType().name(), diffEntity.getAttributes().get(2).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COST, diffEntity.getAttributes().get(3).getType());
        Assert.assertEquals("30", diffEntity.getAttributes().get(3).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_OBJECT, diffEntity.getAttributes().get(4).getType());
        Assert.assertEquals("666", diffEntity.getAttributes().get(4).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diffEntity.getAttributes().get(5).getType());
        Assert.assertEquals("quebec", diffEntity.getAttributes().get(5).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COLUMN, diffEntity.getAttributes().get(6).getType());
        Assert.assertEquals("1", diffEntity.getAttributes().get(6).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.BONUS, diffEntity.getAttributes().get(7).getType());
        Assert.assertEquals("-1", diffEntity.getAttributes().get(7).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
    }

    @Test
    public void testTechColumnBonus() {
        GameEntity game = createGameUsingMocks();
        game.setTurn(22);
        PlayableCountryEntity turquie = new PlayableCountryEntity();
        turquie.setName("turquie");
        game.getCountries().add(turquie);
        PlayableCountryEntity hollande = new PlayableCountryEntity();
        hollande.setName("hollande");
        game.getCountries().add(hollande);
        PlayableCountryEntity france = new PlayableCountryEntity();
        france.setName("france");
        game.getCountries().add(france);
        PlayableCountryEntity prusse = new PlayableCountryEntity();
        prusse.setName("prusse");
        game.getCountries().add(prusse);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setProvince("eTrakya");
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.MNU_METAL_MINUS);
        game.getStacks().get(0).getCounters().get(0).setCountry("turquie");
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(1).setType(CounterFaceTypeEnum.MNU_INSTRUMENTS_MINUS);
        game.getStacks().get(0).getCounters().get(1).setCountry("turquie");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).setProvince("eIdf");
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.MNU_METAL_PLUS);
        game.getStacks().get(1).getCounters().get(0).setCountry("france");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(2).setProvince("eLorraine");
        game.getStacks().get(2).getCounters().add(new CounterEntity());
        game.getStacks().get(2).getCounters().get(0).setType(CounterFaceTypeEnum.MNU_METAL_MINUS);
        game.getStacks().get(2).getCounters().get(0).setCountry("france");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(3).setProvince("eHollande");
        game.getStacks().get(3).getCounters().add(new CounterEntity());
        game.getStacks().get(3).getCounters().get(0).setType(CounterFaceTypeEnum.MNU_INSTRUMENTS_MINUS);
        game.getStacks().get(3).getCounters().get(0).setCountry("hollande");
        game.getStacks().get(3).getCounters().add(new CounterEntity());
        game.getStacks().get(3).getCounters().get(1).setType(CounterFaceTypeEnum.MNU_INSTRUMENTS_PLUS);
        game.getStacks().get(3).getCounters().get(1).setCountry("hollande");
        game.getStacks().get(3).getCounters().add(new CounterEntity());
        game.getStacks().get(3).getCounters().get(2).setType(CounterFaceTypeEnum.MNU_INSTRUMENTS_PLUS);
        game.getStacks().get(3).getCounters().get(2).setCountry("hollande");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(4).setProvince("eSaxe");
        game.getStacks().get(4).getCounters().add(new CounterEntity());
        game.getStacks().get(4).getCounters().get(0).setType(CounterFaceTypeEnum.MNU_METAL_MINUS);
        game.getStacks().get(4).getCounters().get(0).setCountry("prusse");
        game.getStacks().get(4).getCounters().add(new CounterEntity());
        game.getStacks().get(4).getCounters().get(1).setType(CounterFaceTypeEnum.MNU_METAL_SCHLESIEN_PLUS);
        game.getStacks().get(4).getCounters().get(1).setCountry("prusse");

        Assert.assertEquals(1, economicService.getTechColumnBonus(game, turquie, true));
        Assert.assertEquals(1, economicService.getTechColumnBonus(game, turquie, false));

        Assert.assertEquals(2, economicService.getTechColumnBonus(game, france, true));
        Assert.assertEquals(0, economicService.getTechColumnBonus(game, france, false));

        Assert.assertEquals(0, economicService.getTechColumnBonus(game, hollande, true));
        Assert.assertEquals(2, economicService.getTechColumnBonus(game, hollande, false));

        Assert.assertEquals(2, economicService.getTechColumnBonus(game, prusse, true));
        Assert.assertEquals(0, economicService.getTechColumnBonus(game, prusse, false));
    }

    @Test
    public void testAddAdmActTechFail() {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(createGameInfo());
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(11L);
        request.getRequest().setType(AdminActionTypeEnum.ELT);

        GameEntity game = createGameUsingMocks();
        game.setTurn(1);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setDti(3);
        game.getCountries().get(0).setFti(4);
        game.getCountries().get(0).setFtiRotw(5);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setLandTech(Tech.RENAISSANCE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(11L);
        game.getCountries().get(1).setName("angleterre");
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(2).setId(13L);
        game.getCountries().get(2).setName("hollande");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setProvince("B_TECH_5");
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_LAND);
        game.getStacks().get(0).getCounters().get(0).setCountry("france");
        game.getStacks().get(0).getCounters().get(0).setOwner(game.getStacks().get(0));

        Tables tables = new Tables();
        List<Tech> techs = new ArrayList<>();
        Tech tech = new Tech();
        tech.setBeginTurn(1);
        tech.setBeginBox(1);
        tech.setLand(true);
        tech.setName(Tech.MEDIEVAL);
        techs.add(tech);
        tables.getTechs().addAll(techs);
        EconomicServiceImpl.TABLES = tables;

        List<AdministrativeActionEntity> actionsFor11 = new ArrayList<>();
        AdministrativeActionEntity action = new AdministrativeActionEntity();
        action.setType(AdminActionTypeEnum.ELT);
        action.setCost(30);
        actionsFor11.add(action);
        action = new AdministrativeActionEntity();
        action.setType(AdminActionTypeEnum.ENT);
        action.setCost(30);
        actionsFor11.add(action);
        when(adminActionDao.findPlannedAdminActions(11L, 1, null, AdminActionTypeEnum.ELT, AdminActionTypeEnum.ENT)).thenReturn(actionsFor11);

        List<AdministrativeActionEntity> actionsFor12 = new ArrayList<>();
        when(adminActionDao.findPlannedAdminActions(12L, 1, null, AdminActionTypeEnum.ELT, AdminActionTypeEnum.ENT)).thenReturn(actionsFor12);

        List<AdministrativeActionEntity> actionsFor13 = new ArrayList<>();
        action = new AdministrativeActionEntity();
        action.setType(AdminActionTypeEnum.ELT);
        action.setCost(50);
        actionsFor13.add(action);
        when(adminActionDao.findPlannedAdminActions(13L, 1, null, AdminActionTypeEnum.ELT, AdminActionTypeEnum.ENT)).thenReturn(actionsFor13);

        CountryEntity france = new CountryEntity();
        france.setCulture(CultureEnum.LATIN);
        when(countryDao.getCountryByName("france")).thenReturn(france);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because limit exceeded");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.ADMIN_ACTION_LIMIT_EXCEED, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        request.getRequest().setType(AdminActionTypeEnum.ENT);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because limit exceeded");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.ADMIN_ACTION_LIMIT_EXCEED, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        request.getRequest().setIdCountry(13L);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because investment is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.investment", e.getParams()[0]);
        }

        request.getRequest().setInvestment(InvestmentEnum.M);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because other tech has high investment");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.TECH_ALREADY_HIGH_INVESTMENT, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        request.getRequest().setIdCountry(12L);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because no tech naval counter");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.MISSING_COUNTER, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        request.getRequest().setType(AdminActionTypeEnum.ELT);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because tech table missing");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.MISSING_TABLE_ENTRY, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        tech = new Tech();
        tech.setBeginTurn(11);
        tech.setBeginBox(11);
        tech.setLand(true);
        tech.setName(Tech.RENAISSANCE);
        tables.getTechs().add(tech);

        try {
            economicService.addAdminAction(request);
        } catch (FunctionalException e) {
            Assert.fail("Should not fail" + e.getMessage());
        }

        tech = new Tech();
        tech.setBeginTurn(21);
        tech.setBeginBox(21);
        tech.setLand(true);
        tech.setName(Tech.MUSKET);
        tables.getTechs().add(tech);
        tech = new Tech();
        tech.setBeginTurn(31);
        tech.setBeginBox(31);
        tech.setLand(true);
        tech.setName(Tech.BAROQUE);
        tables.getTechs().add(tech);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because musket counter is missing");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.MISSING_COUNTER, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).setProvince("B_TECH_6");
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_MUSKET);
        game.getStacks().get(1).getCounters().get(0).setOwner(game.getStacks().get(1));

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because next tech unknown and one box beyond");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.TECH_ALREADY_MAX, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }
    }

    @Test
    public void testAddAdmActTechSuccess() throws FunctionalException {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(createGameInfo());
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(12L);
        request.getRequest().setType(AdminActionTypeEnum.ELT);
        request.getRequest().setInvestment(InvestmentEnum.M);

        GameEntity game = createGameUsingMocks();
        game.setTurn(22);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setName("turquie");
        game.getCountries().get(0).setLandTech(Tech.RENAISSANCE);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setProvince("B_TECH_5");
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_LAND);
        game.getStacks().get(0).getCounters().get(0).setCountry("turquie");
        game.getStacks().get(0).getCounters().get(0).setOwner(game.getStacks().get(0));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).setProvince("B_TECH_6");
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_MUSKET);
        game.getStacks().get(1).getCounters().get(0).setOwner(game.getStacks().get(1));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(2).setProvince("B_TECH_12");
        game.getStacks().get(2).getCounters().add(new CounterEntity());
        game.getStacks().get(2).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_LAND_ISLAM);
        game.getStacks().get(2).getCounters().get(0).setOwner(game.getStacks().get(2));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(3).setProvince("eTrakya");
        game.getStacks().get(3).getCounters().add(new CounterEntity());
        game.getStacks().get(3).getCounters().get(0).setType(CounterFaceTypeEnum.MNU_METAL_MINUS);
        game.getStacks().get(3).getCounters().get(0).setCountry("turquie");
        game.getStacks().get(3).getCounters().add(new CounterEntity());
        game.getStacks().get(3).getCounters().get(1).setType(CounterFaceTypeEnum.MNU_METAL_PLUS);
        game.getStacks().get(3).getCounters().get(1).setCountry("france");

        Tables tables = new Tables();
        List<Tech> techs = new ArrayList<>();
        Tech tech = new Tech();
        tech.setBeginTurn(1);
        tech.setBeginBox(1);
        tech.setLand(true);
        tech.setName(Tech.MEDIEVAL);
        techs.add(tech);
        tech = new Tech();
        tech.setBeginTurn(11);
        tech.setBeginBox(11);
        tech.setLand(true);
        tech.setName(Tech.RENAISSANCE);
        techs.add(tech);
        tech = new Tech();
        tech.setBeginTurn(21);
        tech.setBeginBox(21);
        tech.setLand(true);
        tech.setName(Tech.MUSKET);
        techs.add(tech);
        tech = new Tech();
        tech.setBeginTurn(31);
        tech.setBeginBox(31);
        tech.setLand(true);
        tech.setName(Tech.BAROQUE);
        techs.add(tech);
        tables.getTechs().addAll(techs);
        EconomicServiceImpl.TABLES = tables;

        List<AdministrativeActionEntity> actionsFor12 = new ArrayList<>();
        when(adminActionDao.findPlannedAdminActions(12L, 1, null, AdminActionTypeEnum.ELT, AdminActionTypeEnum.ENT)).thenReturn(actionsFor12);

        when(oeUtil.getMilitaryValue(game.getCountries().get(0))).thenReturn(3);

        CountryEntity country = new CountryEntity();
        country.setCulture(CultureEnum.ISLAM);
        when(countryDao.getCountryByName("turquie")).thenReturn(country);

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        simulateDiff();

        DiffResponse response = economicService.addAdminAction(request);

        DiffEntity diffEntity = retrieveDiffCreated();

        InOrder inOrder = inOrder(gameDao, adminActionDao, countryDao, diffDao, diffMapping, oeUtil);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, request.getGame().getIdCountry(), 1L);
        inOrder.verify(adminActionDao).findPlannedAdminActions(12L, 22, null, AdminActionTypeEnum.ELT, AdminActionTypeEnum.ENT);
        inOrder.verify(oeUtil).getMilitaryValue(game.getCountries().get(0));
        inOrder.verify(countryDao).getCountryByName("turquie");
        inOrder.verify(adminActionDao).create(anyObject());
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.ADD, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.ADM_ACT, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(6, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_COUNTRY, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(request.getRequest().getIdCountry().toString(),
                            diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TURN, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals(game.getTurn().toString(), diffEntity.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diffEntity.getAttributes().get(2).getType());
        Assert.assertEquals(request.getRequest().getType().name(), diffEntity.getAttributes().get(2).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COST, diffEntity.getAttributes().get(3).getType());
        Assert.assertEquals("50", diffEntity.getAttributes().get(3).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COLUMN, diffEntity.getAttributes().get(4).getType());
        Assert.assertEquals("-2", diffEntity.getAttributes().get(4).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.BONUS, diffEntity.getAttributes().get(5).getType());
        Assert.assertEquals("1", diffEntity.getAttributes().get(5).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
    }

    @Test
    public void testValidateAdmActFail() {
        Pair<Request<ValidateRequest>, GameEntity> pair = testCheckGame(economicService::validateAdminActions, "validateAdminActions");
        Request<ValidateRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();

        testCheckStatus(game, request, economicService::validateAdminActions, "validateAdminActions", GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);

        game.setTurn(22);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(13L);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setUsername("MKL");

        try {
            economicService.validateAdminActions(request);
            Assert.fail("Should break because request.authent is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("validateAdminActions.authent", e.getParams()[0]);
        }

        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("toto");

        try {
            economicService.validateAdminActions(request);
            Assert.fail("Should break because request.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("validateAdminActions.request", e.getParams()[0]);
        }

        request.setRequest(new ValidateRequest());

        try {
            economicService.validateAdminActions(request);
            Assert.fail("Should break because request.request.idCountry is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("validateAdminActions.idCountry", e.getParams()[0]);
        }

        request.getGame().setIdCountry(666L);

        try {
            economicService.validateAdminActions(request);
            Assert.fail("Should break because request.request.idCountry is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("validateAdminActions.idCountry", e.getParams()[0]);
        }

        request.getGame().setIdCountry(13L);

        try {
            economicService.validateAdminActions(request);
            Assert.fail("Should break because request.authent can't do this action");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("validateAdminActions.authent.username", e.getParams()[0]);
        }
    }

    @Test
    public void testValidateAdmActSuccessSimple() throws FunctionalException {
        Request<ValidateRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("MKL");
        request.setGame(createGameInfo());
        request.setRequest(new ValidateRequest());
        request.getGame().setIdCountry(13L);

        GameEntity game = createGameUsingMocks();
        game.setTurn(22);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(13L);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setUsername("MKL");
        game.getCountries().get(0).setReady(false);

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        simulateDiff();

        economicService.validateAdminActions(request);

        List<DiffEntity> diffEntities = retrieveDiffsCreated();

        InOrder inOrder = inOrder(gameDao, adminActionDao, provinceDao, playableCountryDao, diffDao, diffMapping,
                oeUtil);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, request.getGame().getIdCountry(), 1L);
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(0, diffEntities.size());
    }

    @Test
    public void testValidateAdmActSuccessMedium() throws FunctionalException {
        Request<ValidateRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("MKL");
        request.setGame(createGameInfo());
        request.setRequest(new ValidateRequest());
        request.getGame().setIdCountry(13L);

        GameEntity game = createGameUsingMocks();
        game.setTurn(22);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(13L);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setUsername("MKL");
        game.getCountries().get(0).setReady(true);

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        simulateDiff();

        economicService.validateAdminActions(request);

        List<DiffEntity> diffEntities = retrieveDiffsCreated();

        InOrder inOrder = inOrder(gameDao, adminActionDao, provinceDao, playableCountryDao, diffDao, diffMapping,
                oeUtil);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, request.getGame().getIdCountry(), 1L);
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(1, diffEntities.size());
        Assert.assertEquals(12L, diffEntities.get(0).getIdGame().longValue());
        Assert.assertEquals(13L, diffEntities.get(0).getIdObject().longValue());
        Assert.assertEquals(1L, diffEntities.get(0).getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.INVALIDATE, diffEntities.get(0).getType());
        Assert.assertEquals(DiffTypeObjectEnum.STATUS, diffEntities.get(0).getTypeObject());
        Assert.assertEquals(1, diffEntities.get(0).getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_COUNTRY, diffEntities.get(0).getAttributes().get(0).getType());
        Assert.assertEquals("13", diffEntities.get(0).getAttributes().get(0).getValue());
    }

    @Test
    public void testValidateAdmActSuccessComplex() throws FunctionalException {
        Request<ValidateRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("MKL");
        request.setGame(createGameInfo());
        request.setRequest(new ValidateRequest());
        request.getGame().setIdCountry(13L);
        request.getRequest().setValidate(true);

        GameEntity game = createGameUsingMocks();
        game.setTurn(22);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(13L);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setUsername("MKL");
        game.getCountries().get(0).setReady(false);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(14L);
        game.getCountries().get(1).setName("angleterre");
        game.getCountries().get(1).setReady(false);

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        List<DiffEntity> statusDiffs = new ArrayList<>();
        DiffEntity statusDiff = new DiffEntity();
        statusDiffs.add(statusDiff);
        when(statusWorkflowDomain.computeEndAdministrativeActions(game)).thenReturn(statusDiffs);

        simulateDiff();

        economicService.validateAdminActions(request);

        List<DiffEntity> diffEntities = retrieveDiffsCreated();

        InOrder inOrder = inOrder(gameDao, adminActionDao, provinceDao, playableCountryDao, diffDao, diffMapping,
                oeUtil);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, request.getGame().getIdCountry(), 1L);
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(3, diffEntities.size());
        Assert.assertEquals(12L, diffEntities.get(0).getIdGame().longValue());
        Assert.assertEquals(null, diffEntities.get(0).getIdObject());
        Assert.assertEquals(1L, diffEntities.get(0).getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.INVALIDATE, diffEntities.get(0).getType());
        Assert.assertEquals(DiffTypeObjectEnum.ECO_SHEET, diffEntities.get(0).getTypeObject());
        Assert.assertEquals(1, diffEntities.get(0).getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.TURN, diffEntities.get(0).getAttributes().get(0).getType());
        Assert.assertEquals("22", diffEntities.get(0).getAttributes().get(0).getValue());

        Assert.assertEquals(12L, diffEntities.get(1).getIdGame().longValue());
        Assert.assertEquals(null, diffEntities.get(1).getIdObject());
        Assert.assertEquals(1L, diffEntities.get(1).getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.VALIDATE, diffEntities.get(1).getType());
        Assert.assertEquals(DiffTypeObjectEnum.ADM_ACT, diffEntities.get(1).getTypeObject());
        Assert.assertEquals(1, diffEntities.get(1).getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.TURN, diffEntities.get(1).getAttributes().get(0).getType());
        Assert.assertEquals("22", diffEntities.get(1).getAttributes().get(0).getValue());

        Assert.assertEquals(statusDiff, diffEntities.get(2));
    }

    @Test
    public void testComputeAdministrativeActions() {
        GameEntity game = new GameEntity();
        game.setId(33L);
        game.setVersion(365);
        game.setTurn(3);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setId(100L);
        game.getStacks().get(0).setProvince("rAzteca~W");
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(100L);
        game.getStacks().get(0).getCounters().get(0).setCountry("france");
        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.TRADING_POST_MINUS);
        game.getStacks().get(0).getCounters().get(0).setOwner(game.getStacks().get(0));
        CounterEntity counterTp1 = game.getStacks().get(0).getCounters().get(0);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).setId(101L);
        game.getStacks().get(1).setProvince("rAzteca~N");
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setId(101L);
        game.getStacks().get(1).getCounters().get(0).setCountry("france");
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.COLONY_MINUS);
        game.getStacks().get(1).getCounters().get(0).setOwner(game.getStacks().get(1));
        game.getStacks().get(1).getCounters().get(0).setEstablishment(new EstablishmentEntity());
        game.getStacks().get(1).getCounters().get(0).getEstablishment().setLevel(3);
        CounterEntity counterCol = game.getStacks().get(1).getCounters().get(0);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(2).setId(102L);
        game.getStacks().get(2).setProvince("rAzteca~E");
        game.getStacks().get(2).setCountry("france");
        game.getStacks().get(2).setLeader("Nabo");
        game.getStacks().get(2).setGame(game);
        game.getStacks().get(2).getCounters().add(new CounterEntity());
        game.getStacks().get(2).getCounters().get(0).setId(102L);
        game.getStacks().get(2).getCounters().get(0).setCountry("france");
        game.getStacks().get(2).getCounters().get(0).setType(CounterFaceTypeEnum.TRADING_POST_MINUS);
        game.getStacks().get(2).getCounters().get(0).setOwner(game.getStacks().get(2));
        game.getStacks().get(2).getCounters().get(0).setEstablishment(new EstablishmentEntity());
        game.getStacks().get(2).getCounters().get(0).getEstablishment().setLevel(1);
        CounterEntity counterTp2 = game.getStacks().get(2).getCounters().get(0);
        CounterEntity counterFortress = game.getStacks().get(2).getCounters().get(0);
        game.getStacks().get(2).getCounters().add(new CounterEntity());
        game.getStacks().get(2).getCounters().get(1).setId(103L);
        game.getStacks().get(2).getCounters().get(1).setCountry("france");
        game.getStacks().get(2).getCounters().get(1).setType(CounterFaceTypeEnum.ARMY_MINUS);
        game.getStacks().get(2).getCounters().get(1).setOwner(game.getStacks().get(2));
        CounterEntity counterArmy = game.getStacks().get(2).getCounters().get(1);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(3).setId(201L);
        game.getStacks().get(3).setProvince("rAzteca~S");
        game.getStacks().get(3).getCounters().add(new CounterEntity());
        game.getStacks().get(3).getCounters().get(0).setId(200L);
        game.getStacks().get(3).getCounters().get(0).setCountry("angleterre");
        game.getStacks().get(3).getCounters().get(0).setType(CounterFaceTypeEnum.FORT);
        game.getStacks().get(3).getCounters().get(0).setOwner(game.getStacks().get(3));
        CounterEntity counterFort = game.getStacks().get(3).getCounters().get(0);
        game.getStacks().get(3).getCounters().add(new CounterEntity());
        game.getStacks().get(3).getCounters().get(1).setId(201L);
        game.getStacks().get(3).getCounters().get(1).setCountry("france");
        game.getStacks().get(3).getCounters().get(1).setType(CounterFaceTypeEnum.TRADING_POST_PLUS);
        game.getStacks().get(3).getCounters().get(1).setOwner(game.getStacks().get(3));
        game.getStacks().get(3).getCounters().get(1).setEstablishment(new EstablishmentEntity());
        game.getStacks().get(3).getCounters().get(1).getEstablishment().setLevel(4);
        CounterEntity counterTp3 = game.getStacks().get(3).getCounters().get(1);
        game.getStacks().get(3).getCounters().add(new CounterEntity());
        game.getStacks().get(3).getCounters().get(2).setId(199L);
        game.getStacks().get(3).getCounters().get(2).setCountry("angleterre");
        game.getStacks().get(3).getCounters().get(2).setType(CounterFaceTypeEnum.COLONY_PLUS);
        game.getStacks().get(3).getCounters().get(2).setOwner(game.getStacks().get(3));
        game.getStacks().get(3).getCounters().get(2).setEstablishment(new EstablishmentEntity());
        game.getStacks().get(3).getCounters().get(2).getEstablishment().setLevel(4);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(4).setId(1000L);
        game.getStacks().get(4).setProvince("B_TECH_3");
        game.getStacks().get(4).getCounters().add(new CounterEntity());
        game.getStacks().get(4).getCounters().get(0).setId(1000L);
        game.getStacks().get(4).getCounters().get(0).setCountry("france");
        game.getStacks().get(4).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_LAND);
        game.getStacks().get(4).getCounters().get(0).setOwner(game.getStacks().get(4));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(5).setId(1001L);
        game.getStacks().get(5).setProvince("B_TECH_4");
        game.getStacks().get(5).getCounters().add(new CounterEntity());
        game.getStacks().get(5).getCounters().get(0).setId(1001L);
        game.getStacks().get(5).getCounters().get(0).setCountry("france");
        game.getStacks().get(5).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_NAVAL);
        game.getStacks().get(5).getCounters().get(0).setOwner(game.getStacks().get(5));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(6).setId(1002L);
        game.getStacks().get(6).setProvince("B_TECH_5");
        game.getStacks().get(6).getCounters().add(new CounterEntity());
        game.getStacks().get(6).getCounters().get(0).setId(1002L);
        game.getStacks().get(6).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_NAE_GALEON);
        game.getStacks().get(6).getCounters().get(0).setOwner(game.getStacks().get(6));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(7).setId(1001L);
        game.getStacks().get(7).setProvince("idf");
        game.getStacks().get(7).getCounters().add(new CounterEntity());
        game.getStacks().get(7).getCounters().get(0).setId(500L);
        game.getStacks().get(7).getCounters().get(0).setCountry("france");
        game.getStacks().get(7).getCounters().get(0).setType(CounterFaceTypeEnum.ARMY_PLUS);
        game.getStacks().get(7).getCounters().get(0).setOwner(game.getStacks().get(7));
        game.getStacks().get(7).getCounters().add(new CounterEntity());
        game.getStacks().get(7).getCounters().get(1).setId(501L);
        game.getStacks().get(7).getCounters().get(1).setCountry("france");
        game.getStacks().get(7).getCounters().get(1).setType(CounterFaceTypeEnum.FLEET_MINUS);
        game.getStacks().get(7).getCounters().get(1).setOwner(game.getStacks().get(7));
        game.getStacks().get(7).getCounters().add(new CounterEntity());
        game.getStacks().get(7).getCounters().get(2).setId(502L);
        game.getStacks().get(7).getCounters().get(2).setCountry("france");
        game.getStacks().get(7).getCounters().get(2).setType(CounterFaceTypeEnum.NAVAL_TRANSPORT);
        game.getStacks().get(7).getCounters().get(2).setOwner(game.getStacks().get(7));
        game.getStacks().get(7).getCounters().add(new CounterEntity());
        game.getStacks().get(7).getCounters().get(3).setId(503L);
        game.getStacks().get(7).getCounters().get(3).setCountry("angleterre");
        game.getStacks().get(7).getCounters().get(3).setType(CounterFaceTypeEnum.FLEET_PLUS);
        game.getStacks().get(7).getCounters().get(3).setOwner(game.getStacks().get(7));
        game.getStacks().get(7).getCounters().add(new CounterEntity());
        game.getStacks().get(7).getCounters().get(4).setId(504L);
        game.getStacks().get(7).getCounters().get(4).setCountry("france");
        game.getStacks().get(7).getCounters().get(4).setType(CounterFaceTypeEnum.FORTRESS_1);
        game.getStacks().get(7).getCounters().get(4).setOwner(game.getStacks().get(7));
        game.getStacks().get(7).getCounters().add(new CounterEntity());
        game.getStacks().get(7).getCounters().get(5).setId(505L);
        game.getStacks().get(7).getCounters().get(5).setCountry("france");
        game.getStacks().get(7).getCounters().get(5).setType(CounterFaceTypeEnum.MISSION);
        game.getStacks().get(7).getCounters().get(5).setOwner(game.getStacks().get(7));
        StackEntity stack = game.getStacks().get(7);
        CounterEntity counterMnu = createCounter(105L, "france", CounterFaceTypeEnum.MNU_ART_MINUS, stack);
        stack.getCounters().add(counterMnu);

        PlayableCountryEntity france = new PlayableCountryEntity();
        game.getCountries().add(france);
        france.setId(65L);
        france.setName("france");
        france.setDti(4);
        france.setFti(3);
        france.setFtiRotw(3);
        france.setLandTech(Tech.RENAISSANCE);
        france.setNavalTech(Tech.CARRACK);
        france.getEconomicalSheets().add(new EconomicalSheetEntity());
        france.getEconomicalSheets().get(0).setTurn(game.getTurn());
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(0).setId(1L);
        france.getAdministrativeActions().get(0).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(0).setTurn(game.getTurn());
        france.getAdministrativeActions().get(0).setType(AdminActionTypeEnum.LM);
        france.getAdministrativeActions().get(0).setIdObject(101L);
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(1).setId(2L);
        france.getAdministrativeActions().get(1).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(1).setTurn(game.getTurn());
        france.getAdministrativeActions().get(1).setType(AdminActionTypeEnum.LF);
        france.getAdministrativeActions().get(1).setIdObject(102L);
        france.getAdministrativeActions().get(1).setCounterFaceType(CounterFaceTypeEnum.FORTRESS_1);
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(2).setId(3L);
        france.getAdministrativeActions().get(2).setStatus(AdminActionStatusEnum.DONE);
        france.getAdministrativeActions().get(2).setTurn(game.getTurn());
        france.getAdministrativeActions().get(2).setType(AdminActionTypeEnum.DIS);
        france.getAdministrativeActions().get(2).setIdObject(103L);
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(3).setId(4L);
        france.getAdministrativeActions().get(3).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(3).setTurn(game.getTurn() + 1);
        france.getAdministrativeActions().get(3).setType(AdminActionTypeEnum.DIS);
        france.getAdministrativeActions().get(3).setIdObject(103L);
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(4).setId(5L);
        france.getAdministrativeActions().get(4).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(4).setTurn(game.getTurn());
        france.getAdministrativeActions().get(4).setType(AdminActionTypeEnum.DIS);
        france.getAdministrativeActions().get(4).setIdObject(103L);
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(5).setId(6L);
        france.getAdministrativeActions().get(5).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(5).setTurn(game.getTurn());
        france.getAdministrativeActions().get(5).setType(AdminActionTypeEnum.PU);
        france.getAdministrativeActions().get(5).setCounterFaceType(CounterFaceTypeEnum.ARMY_MINUS);
        france.getAdministrativeActions().get(5).setCost(45);
        france.getAdministrativeActions().get(5).setProvince("idf");
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(6).setId(7L);
        france.getAdministrativeActions().get(6).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(6).setTurn(game.getTurn());
        france.getAdministrativeActions().get(6).setType(AdminActionTypeEnum.PU);
        france.getAdministrativeActions().get(6).setCounterFaceType(CounterFaceTypeEnum.NAVAL_DETACHMENT);
        france.getAdministrativeActions().get(6).setCost(18);
        france.getAdministrativeActions().get(6).setProvince("gironde");
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(7).setId(8L);
        france.getAdministrativeActions().get(7).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(7).setTurn(game.getTurn());
        france.getAdministrativeActions().get(7).setType(AdminActionTypeEnum.PU);
        france.getAdministrativeActions().get(7).setCounterFaceType(CounterFaceTypeEnum.FORTRESS_2);
        france.getAdministrativeActions().get(7).setCost(30);
        france.getAdministrativeActions().get(7).setProvince("idf");
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(8).setId(9L);
        france.getAdministrativeActions().get(8).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(8).setTurn(game.getTurn());
        france.getAdministrativeActions().get(8).setType(AdminActionTypeEnum.TFI);
        france.getAdministrativeActions().get(8).setColumn(0);
        france.getAdministrativeActions().get(8).setBonus(0);
        france.getAdministrativeActions().get(8).setCost(10);
        france.getAdministrativeActions().get(8).setProvince("ZPfrance");
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(9).setId(10L);
        france.getAdministrativeActions().get(9).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(9).setTurn(game.getTurn());
        france.getAdministrativeActions().get(9).setType(AdminActionTypeEnum.TFI);
        france.getAdministrativeActions().get(9).setColumn(0);
        france.getAdministrativeActions().get(9).setBonus(0);
        france.getAdministrativeActions().get(9).setCost(10);
        france.getAdministrativeActions().get(9).setProvince("ZPfrance");
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(10).setId(11L);
        france.getAdministrativeActions().get(10).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(10).setTurn(game.getTurn());
        france.getAdministrativeActions().get(10).setType(AdminActionTypeEnum.TFI);
        france.getAdministrativeActions().get(10).setColumn(0);
        france.getAdministrativeActions().get(10).setBonus(0);
        france.getAdministrativeActions().get(10).setCost(10);
        france.getAdministrativeActions().get(10).setProvince("ZPfrance");
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(11).setId(12L);
        france.getAdministrativeActions().get(11).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(11).setTurn(game.getTurn());
        france.getAdministrativeActions().get(11).setType(AdminActionTypeEnum.TFI);
        france.getAdministrativeActions().get(11).setColumn(0);
        france.getAdministrativeActions().get(11).setBonus(0);
        france.getAdministrativeActions().get(11).setCost(10);
        france.getAdministrativeActions().get(11).setProvince("ZPfrance");
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(12).setId(13L);
        france.getAdministrativeActions().get(12).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(12).setTurn(game.getTurn());
        france.getAdministrativeActions().get(12).setType(AdminActionTypeEnum.MNU);
        france.getAdministrativeActions().get(12).setCounterFaceType(CounterFaceTypeEnum.MNU_ART_MINUS);
        france.getAdministrativeActions().get(12).setColumn(1);
        france.getAdministrativeActions().get(12).setBonus(-1);
        france.getAdministrativeActions().get(12).setCost(30);
        france.getAdministrativeActions().get(12).setProvince("idf");
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(13).setId(14L);
        france.getAdministrativeActions().get(13).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(13).setTurn(game.getTurn());
        france.getAdministrativeActions().get(13).setType(AdminActionTypeEnum.MNU);
        france.getAdministrativeActions().get(13).setCounterFaceType(CounterFaceTypeEnum.MNU_ART_MINUS);
        france.getAdministrativeActions().get(13).setIdObject(105L);
        france.getAdministrativeActions().get(13).setColumn(1);
        france.getAdministrativeActions().get(13).setBonus(-1);
        france.getAdministrativeActions().get(13).setCost(30);
        france.getAdministrativeActions().get(13).setProvince("lyonnais");
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(14).setId(15L);
        france.getAdministrativeActions().get(14).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(14).setTurn(game.getTurn());
        france.getAdministrativeActions().get(14).setType(AdminActionTypeEnum.MNU);
        france.getAdministrativeActions().get(14).setCounterFaceType(CounterFaceTypeEnum.MNU_ART_MINUS);
        france.getAdministrativeActions().get(14).setIdObject(105L);
        france.getAdministrativeActions().get(14).setColumn(1);
        france.getAdministrativeActions().get(14).setBonus(-1);
        france.getAdministrativeActions().get(14).setCost(30);
        france.getAdministrativeActions().get(14).setProvince("lyonnais");
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(15).setId(16L);
        france.getAdministrativeActions().get(15).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(15).setTurn(game.getTurn());
        france.getAdministrativeActions().get(15).setType(AdminActionTypeEnum.DTI);
        france.getAdministrativeActions().get(15).setColumn(0);
        france.getAdministrativeActions().get(15).setBonus(0);
        france.getAdministrativeActions().get(15).setCost(30);
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(16).setId(17L);
        france.getAdministrativeActions().get(16).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(16).setTurn(game.getTurn());
        france.getAdministrativeActions().get(16).setType(AdminActionTypeEnum.FTI);
        france.getAdministrativeActions().get(16).setColumn(0);
        france.getAdministrativeActions().get(16).setBonus(0);
        france.getAdministrativeActions().get(16).setCost(50);
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(17).setId(18L);
        france.getAdministrativeActions().get(17).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(17).setTurn(game.getTurn());
        france.getAdministrativeActions().get(17).setType(AdminActionTypeEnum.FTI);
        france.getAdministrativeActions().get(17).setColumn(0);
        france.getAdministrativeActions().get(17).setBonus(0);
        france.getAdministrativeActions().get(17).setCost(50);
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(18).setId(19L);
        france.getAdministrativeActions().get(18).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(18).setTurn(game.getTurn());
        france.getAdministrativeActions().get(18).setType(AdminActionTypeEnum.EXL);
        france.getAdministrativeActions().get(18).setColumn(0);
        france.getAdministrativeActions().get(18).setBonus(6);
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(19).setId(20L);
        france.getAdministrativeActions().get(19).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(19).setTurn(game.getTurn());
        france.getAdministrativeActions().get(19).setType(AdminActionTypeEnum.EXL);
        france.getAdministrativeActions().get(19).setColumn(-1);
        france.getAdministrativeActions().get(19).setBonus(9);
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(20).setId(21L);
        france.getAdministrativeActions().get(20).setIdObject(666L);
        france.getAdministrativeActions().get(20).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(20).setTurn(game.getTurn());
        france.getAdministrativeActions().get(20).setType(AdminActionTypeEnum.COL);
        france.getAdministrativeActions().get(20).setProvince("rAzteca~W");
        france.getAdministrativeActions().get(20).setColumn(0);
        france.getAdministrativeActions().get(20).setBonus(0);
        france.getAdministrativeActions().get(20).setCost(30);
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(21).setId(22L);
        france.getAdministrativeActions().get(21).setIdObject(100L);
        france.getAdministrativeActions().get(21).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(21).setTurn(game.getTurn());
        france.getAdministrativeActions().get(21).setType(AdminActionTypeEnum.TP);
        france.getAdministrativeActions().get(21).setColumn(0);
        france.getAdministrativeActions().get(21).setBonus(0);
        france.getAdministrativeActions().get(21).setCost(10);
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(22).setId(23L);
        france.getAdministrativeActions().get(22).setIdObject(101L);
        france.getAdministrativeActions().get(22).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(22).setTurn(game.getTurn());
        france.getAdministrativeActions().get(22).setType(AdminActionTypeEnum.COL);
        france.getAdministrativeActions().get(22).setColumn(0);
        france.getAdministrativeActions().get(22).setBonus(0);
        france.getAdministrativeActions().get(22).setCost(30);
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(23).setId(24L);
        france.getAdministrativeActions().get(23).setIdObject(102L);
        france.getAdministrativeActions().get(23).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(23).setTurn(game.getTurn());
        france.getAdministrativeActions().get(23).setType(AdminActionTypeEnum.TP);
        france.getAdministrativeActions().get(23).setColumn(0);
        france.getAdministrativeActions().get(23).setBonus(0);
        france.getAdministrativeActions().get(23).setCost(10);
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(24).setId(25L);
        france.getAdministrativeActions().get(24).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(24).setTurn(game.getTurn());
        france.getAdministrativeActions().get(24).setType(AdminActionTypeEnum.COL);
        france.getAdministrativeActions().get(24).setProvince("rAzteca~S");
        france.getAdministrativeActions().get(24).setColumn(0);
        france.getAdministrativeActions().get(24).setBonus(0);
        france.getAdministrativeActions().get(24).setCost(30);
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(25).setId(26L);
        france.getAdministrativeActions().get(25).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(25).setTurn(game.getTurn());
        france.getAdministrativeActions().get(25).setType(AdminActionTypeEnum.TP);
        france.getAdministrativeActions().get(25).setProvince("rAzteca~S");
        france.getAdministrativeActions().get(25).setColumn(0);
        france.getAdministrativeActions().get(25).setBonus(0);
        france.getAdministrativeActions().get(25).setCost(30);
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(26).setId(27L);
        france.getAdministrativeActions().get(26).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(26).setTurn(game.getTurn());
        france.getAdministrativeActions().get(26).setType(AdminActionTypeEnum.ELT);
        france.getAdministrativeActions().get(26).setColumn(0);
        france.getAdministrativeActions().get(26).setBonus(0);
        france.getAdministrativeActions().get(26).setCost(30);
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(27).setId(28L);
        france.getAdministrativeActions().get(27).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(27).setTurn(game.getTurn());
        france.getAdministrativeActions().get(27).setType(AdminActionTypeEnum.ENT);
        france.getAdministrativeActions().get(27).setColumn(0);
        france.getAdministrativeActions().get(27).setBonus(0);
        france.getAdministrativeActions().get(27).setCost(30);
        france.getAdministrativeActions().add(new AdministrativeActionEntity());
        france.getAdministrativeActions().get(28).setId(29L);
        france.getAdministrativeActions().get(28).setStatus(AdminActionStatusEnum.PLANNED);
        france.getAdministrativeActions().get(28).setTurn(game.getTurn());
        france.getAdministrativeActions().get(28).setType(AdminActionTypeEnum.ELT);
        france.getAdministrativeActions().get(28).setColumn(1);
        france.getAdministrativeActions().get(28).setBonus(0);
        france.getAdministrativeActions().get(28).setCost(30);


        Map<String, Map<String, Integer>> newTfis = new HashMap<>();
        Set<String> provinces = new HashSet<>();
        List<DiffEntity> diffs;

        // To isolate unexpected calls
        DiffEntity diffOther = new DiffEntity();
        when(counterDomain.removeCounter(any())).thenReturn(diffOther);
        when(counterDomain.switchCounter(any(), any(), any(), any())).thenReturn(diffOther);
        when(counterDomain.createCounter(any(), any(), any(), any(), any())).thenReturn(diffOther);
        when(counterDomain.moveSpecialCounter(any(), any(), any(), any())).thenReturn(diffOther);

        DiffEntity diffVeteran = new DiffEntity();
        when(counterDomain.changeVeteransCounter(101L, 0d, game)).thenReturn(diffVeteran);

        DiffEntity diffLowerFortress = new DiffEntity();
        when(counterDomain.switchCounter(counterFortress, CounterFaceTypeEnum.FORTRESS_1, null, game)).thenReturn(diffLowerFortress);

        DiffEntity diffRemove = new DiffEntity();
        when(counterDomain.removeCounter(counterArmy)).thenReturn(diffRemove);
        when(oeUtil.getController(game.getStacks().get(2))).thenReturn("espagne");

        DiffEntity diffAddLand = new DiffEntity();
        when(counterDomain.createCounter(CounterFaceTypeEnum.ARMY_MINUS, "france", "idf", null, game)).thenReturn(diffAddLand);

        DiffEntity diffAddSea = new DiffEntity();
        when(counterDomain.createCounter(CounterFaceTypeEnum.NAVAL_DETACHMENT, "france", "gironde", null, game)).thenReturn(diffAddSea);

        DiffEntity diffAddFortress = new DiffEntity();
        when(counterDomain.createCounter(CounterFaceTypeEnum.FORTRESS_2, "france", "idf", null, game)).thenReturn(diffAddFortress);

        DiffEntity diffAddMnu = new DiffEntity();
        when(counterDomain.createCounter(CounterFaceTypeEnum.MNU_ART_MINUS, "france", "idf", null, game)).thenReturn(diffAddMnu);

        DiffEntity diffUpMnu = new DiffEntity();
        when(counterDomain.switchCounter(counterMnu, CounterFaceTypeEnum.MNU_ART_PLUS, null, game)).thenReturn(diffUpMnu);

        when(oeUtil.getStability(game, "france")).thenReturn(2);

        DiffEntity diffLowerStab = new DiffEntity();
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.STABILITY, "france", GameUtil.getStabilityBox(1), game)).thenReturn(diffLowerStab);

        DiffEntity diffAddExistingCol = new DiffEntity();
        when(counterDomain.createCounter(CounterFaceTypeEnum.COLONY_MINUS, "france", "rAzteca~W", 1, game)).thenReturn(diffAddExistingCol);

        DiffEntity diffUpTp1 = new DiffEntity();
        when(counterDomain.switchCounter(counterTp1, CounterFaceTypeEnum.TRADING_POST_MINUS, 1, game)).thenReturn(diffUpTp1);

        DiffEntity diffUpCol = new DiffEntity();
        when(counterDomain.switchCounter(counterCol, CounterFaceTypeEnum.COLONY_PLUS, 4, game)).thenReturn(diffUpCol);

        DiffEntity diffUpTp2 = new DiffEntity();
        when(counterDomain.switchCounter(counterTp2, CounterFaceTypeEnum.TRADING_POST_MINUS, 2, game)).thenReturn(diffUpTp2);

        DiffEntity diffDestroyFort = new DiffEntity();
        when(counterDomain.removeCounter(counterFort)).thenReturn(diffDestroyFort);

        DiffEntity diffAddCol = new DiffEntity();
        when(counterDomain.createCounter(CounterFaceTypeEnum.COLONY_MINUS, "france", "rAzteca~S", 1, game)).thenReturn(diffAddCol);

        DiffEntity diffUpTp3 = new DiffEntity();
        when(counterDomain.switchCounter(counterTp3, CounterFaceTypeEnum.TRADING_POST_PLUS, 5, game)).thenReturn(diffUpTp3);

        when(oeUtil.getTechnologyAdvance(game, france.getName(), false)).thenReturn(4);

        DiffEntity diffTechNaval = new DiffEntity();
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.TECH_NAVAL, "france", "B_TECH_6", game)).thenReturn(diffTechNaval);

        when(oeUtil.getTechnologyAdvance(game, france.getName(), true)).thenReturn(3);

        DiffEntity diffTechLand = new DiffEntity();
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.TECH_LAND, "france", "B_TECH_5", game)).thenReturn(diffTechLand);

        Tables tables = new Tables();
        List<Period> periods = new ArrayList<>();
        Period periodI = new Period();
        periodI.setName(Period.PERIOD_I);
        periodI.setBegin(1);
        periodI.setEnd(60);
        periods.add(periodI);
        tables.getPeriods().addAll(periods);
        List<Result> results = new ArrayList<>();
        Result result = new Result();
        result.setColumn(0);
        result.setDie(4);
        result.setResult(ResultEnum.FAILED);
        results.add(result);
        result = new Result();
        result.setColumn(0);
        result.setDie(5);
        result.setResult(ResultEnum.AVERAGE);
        results.add(result);
        result = new Result();
        result.setColumn(0);
        result.setDie(6);
        result.setResult(ResultEnum.SUCCESS);
        results.add(result);
        tables.getResults().addAll(results);
        result = new Result();
        result.setColumn(1);
        result.setDie(4);
        result.setResult(ResultEnum.FUMBLE);
        results.add(result);
        result = new Result();
        result.setColumn(1);
        result.setDie(5);
        result.setResult(ResultEnum.AVERAGE_PLUS);
        results.add(result);
        result = new Result();
        result.setColumn(1);
        result.setDie(6);
        result.setResult(ResultEnum.CRITICAL_HIT);
        results.add(result);
        tables.getResults().addAll(results);
        List<Limit> limits = new ArrayList<>();
        Limit limit = new Limit();
        limit.setCountry("france");
        limit.setPeriod(new Period());
        limit.getPeriod().setBegin(1);
        limit.getPeriod().setEnd(game.getTurn());
        limit.setType(LimitTypeEnum.MAX_FTI);
        limit.setNumber(4);
        limits.add(limit);
        tables.getLimits().addAll(limits);
        List<Tech> techs = new ArrayList<>();
        Tech renaissance = new Tech();
        renaissance.setName(Tech.RENAISSANCE);
        renaissance.setBeginTurn(2);
        renaissance.setLand(true);
        techs.add(renaissance);
        Tech carrack = new Tech();
        carrack.setName(Tech.CARRACK);
        carrack.setBeginTurn(1);
        carrack.setLand(false);
        techs.add(carrack);
        Tech naeG = new Tech();
        naeG.setName(Tech.NAE_GALEON);
        naeG.setBeginTurn(3);
        naeG.setLand(false);
        techs.add(naeG);
        tables.getTechs().addAll(techs);
        List<Unit> units = new ArrayList<>();
        Unit unit = new Unit();
        unit.setCountry("france");
        unit.setAction(UnitActionEnum.MAINT_WAR);
        unit.setSpecial(true);
        unit.setType(ForceTypeEnum.ARMY_PLUS);
        unit.setTech(renaissance);
        unit.setPrice(25);
        units.add(unit);
        unit = new Unit();
        unit.setCountry("france");
        unit.setAction(UnitActionEnum.MAINT_PEACE);
        unit.setType(ForceTypeEnum.ARMY_PLUS);
        unit.setTech(renaissance);
        unit.setPrice(60);
        units.add(unit);
        unit = new Unit();
        unit.setCountry("france");
        unit.setAction(UnitActionEnum.MAINT);
        unit.setType(ForceTypeEnum.FLEET_MINUS);
        unit.setTech(carrack);
        unit.setPrice(100);
        units.add(unit);
        unit = new Unit();
        unit.setCountry("france");
        unit.setAction(UnitActionEnum.MAINT);
        unit.setType(ForceTypeEnum.ND);
        unit.setTech(carrack);
        unit.setPrice(40);
        units.add(unit);
        unit = new Unit();
        unit.setCountry("france");
        unit.setAction(UnitActionEnum.MAINT);
        unit.setType(ForceTypeEnum.FLEET_MINUS);
        unit.setTech(naeG);
        unit.setPrice(120);
        units.add(unit);
        unit = new Unit();
        unit.setCountry("france");
        unit.setAction(UnitActionEnum.MAINT);
        unit.setType(ForceTypeEnum.ND);
        unit.setTech(naeG);
        unit.setPrice(50);
        units.add(unit);
        tables.getUnits().addAll(units);
        List<BasicForce> basicForces = new ArrayList<>();
        BasicForce basicForce = new BasicForce();
        basicForce.setCountry("france");
        basicForce.setPeriod(periodI);
        basicForce.setType(ForceTypeEnum.ND);
        basicForce.setNumber(1);
        basicForces.add(basicForce);
        tables.getBasicForces().addAll(basicForces);
        EconomicServiceImpl.TABLES = tables;

        when(oeUtil.rollDie(game, france))
                // First four rolls: success, failure, 1/2 with success, 1/2 with failure for TFI
                .thenReturn(6, 4, 5, 3, 5, 4)
                        // Then a critical hit, a 1/2 with success and a fumble for MNU
                .thenReturn(7, 6, 1, 5)
                        // Then two successes and a failure for DTI/FTI
                .thenReturn(6, 6, 4)
                        // Then 6 successes for COL/TP
                .thenReturn(6, 6, 6, 6, 6, 6)
                        // Then 2 1/2, each with a secondary failure then a critical hit for Techs
                .thenReturn(5, 5, 5, 5, 6);

        when(oeUtil.getWarStatus(game, france)).thenReturn(WarStatusEnum.CLASSIC_WAR);

        diffs = economicService.computeAdministrativeActions(france, game, newTfis, provinces);

        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(0).getStatus());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(1).getStatus());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(4).getStatus());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(5).getStatus());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(6).getStatus());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(7).getStatus());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(8).getStatus());
        Assert.assertEquals(6, france.getAdministrativeActions().get(8).getDie().intValue());
        Assert.assertEquals(ResultEnum.SUCCESS, france.getAdministrativeActions().get(8).getResult());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(9).getStatus());
        Assert.assertEquals(4, france.getAdministrativeActions().get(9).getDie().intValue());
        Assert.assertEquals(ResultEnum.FAILED, france.getAdministrativeActions().get(9).getResult());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(10).getStatus());
        Assert.assertEquals(5, france.getAdministrativeActions().get(10).getDie().intValue());
        Assert.assertEquals(ResultEnum.AVERAGE, france.getAdministrativeActions().get(10).getResult());
        Assert.assertEquals(true, france.getAdministrativeActions().get(10).isSecondaryResult());
        Assert.assertEquals(3, france.getAdministrativeActions().get(10).getSecondaryDie().intValue());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(11).getStatus());
        Assert.assertEquals(5, france.getAdministrativeActions().get(11).getDie().intValue());
        Assert.assertEquals(ResultEnum.AVERAGE, france.getAdministrativeActions().get(11).getResult());
        Assert.assertEquals(false, france.getAdministrativeActions().get(11).isSecondaryResult());
        Assert.assertEquals(4, france.getAdministrativeActions().get(11).getSecondaryDie().intValue());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(12).getStatus());
        Assert.assertEquals(7, france.getAdministrativeActions().get(12).getDie().intValue());
        Assert.assertEquals(ResultEnum.CRITICAL_HIT, france.getAdministrativeActions().get(12).getResult());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(13).getStatus());
        Assert.assertEquals(6, france.getAdministrativeActions().get(13).getDie().intValue());
        Assert.assertEquals(ResultEnum.AVERAGE_PLUS, france.getAdministrativeActions().get(13).getResult());
        Assert.assertEquals(true, france.getAdministrativeActions().get(13).isSecondaryResult());
        Assert.assertEquals(1, france.getAdministrativeActions().get(13).getSecondaryDie().intValue());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(14).getStatus());
        Assert.assertEquals(5, france.getAdministrativeActions().get(14).getDie().intValue());
        Assert.assertEquals(ResultEnum.FUMBLE, france.getAdministrativeActions().get(14).getResult());
        Assert.assertEquals(6, france.getAdministrativeActions().get(15).getDie().intValue());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(15).getStatus());
        Assert.assertEquals(ResultEnum.SUCCESS, france.getAdministrativeActions().get(15).getResult());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(16).getStatus());
        Assert.assertEquals(6, france.getAdministrativeActions().get(16).getDie().intValue());
        Assert.assertEquals(ResultEnum.SUCCESS, france.getAdministrativeActions().get(16).getResult());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(17).getStatus());
        Assert.assertEquals(4, france.getAdministrativeActions().get(17).getDie().intValue());
        Assert.assertEquals(ResultEnum.FAILED, france.getAdministrativeActions().get(17).getResult());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(18).getStatus());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(19).getStatus());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(20).getStatus());
        Assert.assertEquals(6, france.getAdministrativeActions().get(20).getDie().intValue());
        Assert.assertEquals(ResultEnum.SUCCESS, france.getAdministrativeActions().get(20).getResult());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(21).getStatus());
        Assert.assertEquals(6, france.getAdministrativeActions().get(21).getDie().intValue());
        Assert.assertEquals(ResultEnum.SUCCESS, france.getAdministrativeActions().get(21).getResult());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(22).getStatus());
        Assert.assertEquals(6, france.getAdministrativeActions().get(22).getDie().intValue());
        Assert.assertEquals(ResultEnum.SUCCESS, france.getAdministrativeActions().get(22).getResult());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(23).getStatus());
        Assert.assertEquals(6, france.getAdministrativeActions().get(23).getDie().intValue());
        Assert.assertEquals(ResultEnum.SUCCESS, france.getAdministrativeActions().get(23).getResult());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(24).getStatus());
        Assert.assertEquals(6, france.getAdministrativeActions().get(24).getDie().intValue());
        Assert.assertEquals(ResultEnum.SUCCESS, france.getAdministrativeActions().get(24).getResult());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(25).getStatus());
        Assert.assertEquals(6, france.getAdministrativeActions().get(25).getDie().intValue());
        Assert.assertEquals(ResultEnum.SUCCESS, france.getAdministrativeActions().get(25).getResult());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(26).getStatus());
        Assert.assertEquals(5, france.getAdministrativeActions().get(26).getDie().intValue());
        Assert.assertEquals(ResultEnum.AVERAGE, france.getAdministrativeActions().get(26).getResult());
        Assert.assertEquals(false, france.getAdministrativeActions().get(26).isSecondaryResult());
        Assert.assertEquals(5, france.getAdministrativeActions().get(26).getSecondaryDie().intValue());
        Assert.assertEquals(null, france.getAdministrativeActions().get(26).getProvince());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(27).getStatus());
        Assert.assertEquals(5, france.getAdministrativeActions().get(27).getDie().intValue());
        Assert.assertEquals(ResultEnum.AVERAGE, france.getAdministrativeActions().get(27).getResult());
        Assert.assertEquals(false, france.getAdministrativeActions().get(27).isSecondaryResult());
        Assert.assertEquals(5, france.getAdministrativeActions().get(27).getSecondaryDie().intValue());
        Assert.assertEquals("1", france.getAdministrativeActions().get(27).getProvince());
        Assert.assertEquals(AdminActionStatusEnum.DONE, france.getAdministrativeActions().get(28).getStatus());
        Assert.assertEquals(6, france.getAdministrativeActions().get(28).getDie().intValue());
        Assert.assertEquals(ResultEnum.CRITICAL_HIT, france.getAdministrativeActions().get(28).getResult());
        Assert.assertEquals("2", france.getAdministrativeActions().get(28).getProvince());

        Assert.assertEquals(Tech.NAE_GALEON, france.getNavalTech());

        Assert.assertEquals(105, france.getEconomicalSheets().get(0).getUnitMaintExpense().intValue());
        Assert.assertEquals(1, france.getEconomicalSheets().get(0).getFortMaintExpense().intValue());
        Assert.assertEquals(1, france.getEconomicalSheets().get(0).getMissMaintExpense().intValue());
        Assert.assertEquals(63, france.getEconomicalSheets().get(0).getUnitPurchExpense().intValue());
        Assert.assertEquals(30, france.getEconomicalSheets().get(0).getFortPurchExpense().intValue());
        Assert.assertEquals(490, france.getEconomicalSheets().get(0).getAdminActExpense().intValue());
        Assert.assertEquals(6, france.getEconomicalSheets().get(0).getOtherExpense().intValue());
        Assert.assertEquals(696, france.getEconomicalSheets().get(0).getAdmTotalExpense().intValue());
        Assert.assertEquals(15, france.getEconomicalSheets().get(0).getExcTaxesMod().intValue());

        Assert.assertEquals(22, diffs.size());
        Assert.assertEquals(diffVeteran, diffs.get(0));
        Assert.assertEquals(diffLowerFortress, diffs.get(1));
        Assert.assertEquals(diffRemove, diffs.get(2));
        Assert.assertEquals(diffAddLand, diffs.get(3));
        Assert.assertEquals(diffAddSea, diffs.get(4));
        Assert.assertEquals(diffAddFortress, diffs.get(5));
        Assert.assertEquals(diffAddMnu, diffs.get(6));
        Assert.assertEquals(diffUpMnu, diffs.get(7));
        Assert.assertEquals(game.getId(), diffs.get(8).getIdGame());
        Assert.assertEquals(game.getVersion(), diffs.get(8).getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.MODIFY, diffs.get(8).getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTRY, diffs.get(8).getTypeObject());
        Assert.assertEquals(france.getId(), diffs.get(8).getIdObject());
        Assert.assertEquals(1, diffs.get(8).getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.DTI, diffs.get(8).getAttributes().get(0).getType());
        Assert.assertEquals("5", diffs.get(8).getAttributes().get(0).getValue());
        Assert.assertEquals(game.getId(), diffs.get(9).getIdGame());
        Assert.assertEquals(game.getVersion(), diffs.get(9).getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.MODIFY, diffs.get(9).getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTRY, diffs.get(9).getTypeObject());
        Assert.assertEquals(france.getId(), diffs.get(9).getIdObject());
        Assert.assertEquals(2, diffs.get(9).getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.FTI, diffs.get(9).getAttributes().get(0).getType());
        Assert.assertEquals("4", diffs.get(9).getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.FTI_ROTW, diffs.get(9).getAttributes().get(1).getType());
        Assert.assertEquals("4", diffs.get(9).getAttributes().get(1).getValue());
        Assert.assertEquals(diffLowerStab, diffs.get(10));
        Assert.assertEquals(diffAddExistingCol, diffs.get(11));
        Assert.assertEquals(diffUpTp1, diffs.get(12));
        Assert.assertEquals(diffUpCol, diffs.get(13));
        Assert.assertEquals(diffUpTp2, diffs.get(14));
        Assert.assertEquals(diffDestroyFort, diffs.get(15));
        Assert.assertEquals(diffAddCol, diffs.get(16));
        Assert.assertEquals(diffUpTp3, diffs.get(17));
        Assert.assertEquals(diffTechNaval, diffs.get(18));
        Assert.assertEquals(game.getId(), diffs.get(19).getIdGame());
        Assert.assertEquals(game.getVersion(), diffs.get(19).getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.MODIFY, diffs.get(19).getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTRY, diffs.get(19).getTypeObject());
        Assert.assertEquals(france.getId(), diffs.get(19).getIdObject());
        Assert.assertEquals(1, diffs.get(19).getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.TECH_NAVAL, diffs.get(19).getAttributes().get(0).getType());
        Assert.assertEquals(Tech.NAE_GALEON, diffs.get(19).getAttributes().get(0).getValue());
        Assert.assertEquals(diffTechLand, diffs.get(20));
        Assert.assertEquals(DiffTypeEnum.MODIFY, diffs.get(21).getType());
        Assert.assertEquals(DiffTypeObjectEnum.STACK, diffs.get(21).getTypeObject());
        Assert.assertEquals("espagne", getAttribute(diffs.get(21), DiffAttributeTypeEnum.COUNTRY));
        Assert.assertEquals(null, getAttribute(diffs.get(21), DiffAttributeTypeEnum.LEADER));

        Assert.assertEquals(1, newTfis.size());
        Assert.assertEquals(1, newTfis.get("ZPfrance").size());
        Assert.assertEquals(2, newTfis.get("ZPfrance").get("france").intValue());

        when(oeUtil.getWarStatus(game, france)).thenReturn(WarStatusEnum.PEACE);

        economicService.computeAdministrativeActions(france, game, newTfis, provinces);

        Assert.assertEquals(160, france.getEconomicalSheets().get(0).getUnitMaintExpense().intValue());
        Assert.assertEquals(1, france.getEconomicalSheets().get(0).getFortMaintExpense().intValue());
        Assert.assertEquals(1, france.getEconomicalSheets().get(0).getMissMaintExpense().intValue());
    }

    @Test
    public void testTfCompetition() {
        GameEntity game = new GameEntity();
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(0).setProvince("ZPVenise");
        game.getTradeFleets().get(0).setCountry("venise");
        game.getTradeFleets().get(0).setLevel(5);
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(1).setProvince("ZPVenise");
        game.getTradeFleets().get(1).setCountry("turquie");
        game.getTradeFleets().get(1).setLevel(1);
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(2).setProvince("ZMCaraibes");
        game.getTradeFleets().get(2).setCountry("france");
        game.getTradeFleets().get(2).setLevel(5);
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(3).setProvince("ZMCaraibes");
        game.getTradeFleets().get(3).setCountry("espagne");
        game.getTradeFleets().get(3).setLevel(4);
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(4).setProvince("ZMCaraibes");
        game.getTradeFleets().get(4).setCountry("angleterre");
        game.getTradeFleets().get(4).setLevel(1);
        game.getTradeFleets().add(new TradeFleetEntity());
        game.getTradeFleets().get(5).setProvince("ZMCaraibes");
        game.getTradeFleets().get(5).setCountry("portugal");
        game.getTradeFleets().get(5).setLevel(0);

        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setId(100L);
        game.getStacks().get(0).setProvince("ZPVenise");
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(100L);
        game.getStacks().get(0).getCounters().get(0).setCountry("venise");
        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.TRADING_FLEET_PLUS);
        game.getStacks().get(0).getCounters().get(0).setOwner(game.getStacks().get(0));
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(1).setId(101L);
        game.getStacks().get(0).getCounters().get(1).setCountry("turquie");
        game.getStacks().get(0).getCounters().get(1).setType(CounterFaceTypeEnum.TRADING_FLEET_MINUS);
        game.getStacks().get(0).getCounters().get(1).setOwner(game.getStacks().get(0));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).setId(200L);
        game.getStacks().get(1).setProvince("ZMCaraibes");
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setId(200L);
        game.getStacks().get(1).getCounters().get(0).setCountry("france");
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.TRADING_FLEET_PLUS);
        game.getStacks().get(1).getCounters().get(0).setOwner(game.getStacks().get(1));
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(1).setId(201L);
        game.getStacks().get(1).getCounters().get(1).setCountry("espagne");
        game.getStacks().get(1).getCounters().get(1).setType(CounterFaceTypeEnum.TRADING_FLEET_PLUS);
        game.getStacks().get(1).getCounters().get(1).setOwner(game.getStacks().get(1));
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(2).setId(202L);
        game.getStacks().get(1).getCounters().get(2).setCountry("angleterre");
        game.getStacks().get(1).getCounters().get(2).setType(CounterFaceTypeEnum.TRADING_FLEET_MINUS);
        game.getStacks().get(1).getCounters().get(2).setOwner(game.getStacks().get(1));

        Tables tables = new Tables();
        List<Result> results = new ArrayList<>();
        Result result = new Result();
        result.setColumn(-2);
        result.setDie(1);
        result.setResult(ResultEnum.FUMBLE);
        results.add(result);
        result = new Result();
        result.setColumn(-2);
        result.setDie(2);
        result.setResult(ResultEnum.FUMBLE);
        results.add(result);
        result = new Result();
        result.setColumn(-2);
        result.setDie(3);
        result.setResult(ResultEnum.FUMBLE);
        results.add(result);
        result = new Result();
        result.setColumn(-2);
        result.setDie(4);
        result.setResult(ResultEnum.FAILED);
        results.add(result);
        result = new Result();
        result.setColumn(-2);
        result.setDie(5);
        result.setResult(ResultEnum.FAILED);
        results.add(result);
        result = new Result();
        result.setColumn(-2);
        result.setDie(6);
        result.setResult(ResultEnum.AVERAGE);
        results.add(result);
        result = new Result();
        result.setColumn(-2);
        result.setDie(7);
        result.setResult(ResultEnum.AVERAGE_PLUS);
        results.add(result);
        result = new Result();
        result.setColumn(-2);
        result.setDie(8);
        result.setResult(ResultEnum.SUCCESS);
        results.add(result);
        result = new Result();
        result.setColumn(-2);
        result.setDie(9);
        result.setResult(ResultEnum.SUCCESS);
        results.add(result);
        result = new Result();
        result.setColumn(-2);
        result.setDie(10);
        result.setResult(ResultEnum.CRITICAL_HIT);
        results.add(result);

        result = new Result();
        result.setColumn(-1);
        result.setDie(1);
        result.setResult(ResultEnum.FUMBLE);
        results.add(result);
        result = new Result();
        result.setColumn(-1);
        result.setDie(2);
        result.setResult(ResultEnum.FUMBLE);
        results.add(result);
        result = new Result();
        result.setColumn(-1);
        result.setDie(3);
        result.setResult(ResultEnum.FAILED);
        results.add(result);
        result = new Result();
        result.setColumn(-1);
        result.setDie(4);
        result.setResult(ResultEnum.FAILED);
        results.add(result);
        result = new Result();
        result.setColumn(-1);
        result.setDie(5);
        result.setResult(ResultEnum.AVERAGE);
        results.add(result);
        result = new Result();
        result.setColumn(-1);
        result.setDie(6);
        result.setResult(ResultEnum.AVERAGE_PLUS);
        results.add(result);
        result = new Result();
        result.setColumn(-1);
        result.setDie(7);
        result.setResult(ResultEnum.SUCCESS);
        results.add(result);
        result = new Result();
        result.setColumn(-1);
        result.setDie(8);
        result.setResult(ResultEnum.SUCCESS);
        results.add(result);
        result = new Result();
        result.setColumn(-1);
        result.setDie(9);
        result.setResult(ResultEnum.CRITICAL_HIT);
        results.add(result);
        result = new Result();
        result.setColumn(-1);
        result.setDie(10);
        result.setResult(ResultEnum.CRITICAL_HIT);
        results.add(result);

        result = new Result();
        result.setColumn(1);
        result.setDie(1);
        result.setResult(ResultEnum.FUMBLE);
        results.add(result);
        result = new Result();
        result.setColumn(1);
        result.setDie(2);
        result.setResult(ResultEnum.FAILED);
        results.add(result);
        result = new Result();
        result.setColumn(1);
        result.setDie(3);
        result.setResult(ResultEnum.FAILED);
        results.add(result);
        result = new Result();
        result.setColumn(1);
        result.setDie(4);
        result.setResult(ResultEnum.AVERAGE);
        results.add(result);
        result = new Result();
        result.setColumn(1);
        result.setDie(5);
        result.setResult(ResultEnum.AVERAGE_PLUS);
        results.add(result);
        result = new Result();
        result.setColumn(1);
        result.setDie(6);
        result.setResult(ResultEnum.SUCCESS);
        results.add(result);
        result = new Result();
        result.setColumn(1);
        result.setDie(7);
        result.setResult(ResultEnum.SUCCESS);
        results.add(result);
        result = new Result();
        result.setColumn(1);
        result.setDie(8);
        result.setResult(ResultEnum.CRITICAL_HIT);
        results.add(result);
        result = new Result();
        result.setColumn(1);
        result.setDie(9);
        result.setResult(ResultEnum.CRITICAL_HIT);
        results.add(result);
        result = new Result();
        result.setColumn(1);
        result.setDie(10);
        result.setResult(ResultEnum.CRITICAL_HIT);
        results.add(result);

        result = new Result();
        result.setColumn(4);
        result.setDie(1);
        result.setResult(ResultEnum.FAILED);
        results.add(result);
        result = new Result();
        result.setColumn(4);
        result.setDie(2);
        result.setResult(ResultEnum.AVERAGE);
        results.add(result);
        result = new Result();
        result.setColumn(4);
        result.setDie(3);
        result.setResult(ResultEnum.AVERAGE_PLUS);
        results.add(result);
        result = new Result();
        result.setColumn(4);
        result.setDie(4);
        result.setResult(ResultEnum.AVERAGE_PLUS);
        results.add(result);
        result = new Result();
        result.setColumn(4);
        result.setDie(5);
        result.setResult(ResultEnum.SUCCESS);
        results.add(result);
        result = new Result();
        result.setColumn(4);
        result.setDie(6);
        result.setResult(ResultEnum.SUCCESS);
        results.add(result);
        result = new Result();
        result.setColumn(4);
        result.setDie(7);
        result.setResult(ResultEnum.CRITICAL_HIT);
        results.add(result);
        result = new Result();
        result.setColumn(4);
        result.setDie(8);
        result.setResult(ResultEnum.CRITICAL_HIT);
        results.add(result);
        result = new Result();
        result.setColumn(4);
        result.setDie(9);
        result.setResult(ResultEnum.CRITICAL_HIT);
        results.add(result);
        result = new Result();
        result.setColumn(4);
        result.setDie(10);
        result.setResult(ResultEnum.CRITICAL_HIT);
        results.add(result);

        tables.getResults().addAll(results);
        EconomicServiceImpl.TABLES = tables;

        Map<String, Map<String, Integer>> newTfis = new HashMap<>();
        newTfis.put("ZPVenise", new HashMap<>());
        newTfis.get("ZPVenise").put("venise", 1);
        newTfis.get("ZPVenise").put("angleterre", 1);
        newTfis.put("ZMCaraibes", new HashMap<>());
        newTfis.get("ZMCaraibes").put("france", 1);
        newTfis.get("ZMCaraibes").put("suede", 2);

        TradeZoneProvinceEntity zpVenise = new TradeZoneProvinceEntity();
        zpVenise.setName("ZPVenise");
        zpVenise.setCountryName("venise");
        when(provinceDao.getProvinceByName("ZPVenise")).thenReturn(zpVenise);

        TradeZoneProvinceEntity zmCaraibes = new TradeZoneProvinceEntity();
        zmCaraibes.setName("ZMCaraibes");
        when(provinceDao.getProvinceByName("ZMCaraibes")).thenReturn(zmCaraibes);

        when(oeUtil.getFti(game, tables, "venise")).thenReturn(4);
        when(oeUtil.getDti(game, tables, "venise")).thenReturn(5);
        when(oeUtil.getFti(game, tables, "angleterre")).thenReturn(5);
        when(oeUtil.getFti(game, tables, "turquie")).thenReturn(3);
        when(oeUtil.getFti(game, tables, "espagne")).thenReturn(3);
        when(oeUtil.getFti(game, tables, "france")).thenReturn(4);
        when(oeUtil.getFti(game, tables, "suede")).thenReturn(3);

        // Venise wins its competition on first round with 5 on column 4 => SUCCESS
        when(oeUtil.rollDie(game, "venise")).thenReturn(5);
        // Turquie loses its competition on first round with 1 on column -2 => FUMBLE
        when(oeUtil.rollDie(game, "turquie")).thenReturn(1);
        // Angleterre loses its competition on ZPVenise on first round with 4 on column 1 => AVERAGE then 6 fail secondary test
        when(oeUtil.rollDie(game, "angleterre")).thenReturn(4, 6)
        // Angleterre loses its first round of competition in ZMCaraibes with 2 on column 1 => FAILED
        .thenReturn(2);
        // France wins its first round of competition in ZMCaraibes with 6 on column -1 => AVERAGE_PLUS then 4 succeed secondary test
        when(oeUtil.rollDie(game, "france")).thenReturn(6, 4)
        // Then wins the second round with 6 on column 1 => SUCCESS
        .thenReturn(6)
        // Then fails the third round with 3 on column 1 => FAILED
        .thenReturn(3)
        // Then loses the first round on the second competition in ZMCaraibes with 1 on column 1 => FUMBLE
        .thenReturn(1);
        // Espagne wins the three rounds of competition in ZMCaraibes with triple 9 on column -2/-1/-1 => SUCCESS/CRITICAL_HIT/CRITICAL_HIT
        when(oeUtil.rollDie(game, "espagne")).thenReturn(9, 9, 9)
        // Then loses the first round on the second competition in ZMCaraibes with 1 on column -1 => FUMBLE
        .thenReturn(1);
        // Suede loses the first round of competition in ZMCaraibes with 1 on column -2 => FUBMLE
        when(oeUtil.rollDie(game, "suede")).thenReturn(1)
        // Then wins the two others rounds with 10 on column -1 => CRITICAL_HIT
                .thenReturn(10, 10);

        // To isolate unexpected calls
        DiffEntity diffOther = new DiffEntity();
        when(counterDomain.removeCounter(any())).thenReturn(diffOther);
        when(counterDomain.switchCounter(any(), any(), any(), any())).thenReturn(diffOther);
        when(counterDomain.createCounter(any(), any(), any(), any(), any())).thenReturn(diffOther);

        DiffEntity diffSwitch56 = new DiffEntity();
        when(counterDomain.switchCounter(game.getStacks().get(0).getCounters().get(0), CounterFaceTypeEnum.TRADING_FLEET_PLUS, 6, game)).thenReturn(diffSwitch56);

        DiffEntity diffRemove10 = new DiffEntity();
        when(counterDomain.removeCounter(game.getStacks().get(0).getCounters().get(1))).thenReturn(diffRemove10);

        DiffEntity diffSwitch54 = new DiffEntity();
        when(counterDomain.switchCounter(game.getStacks().get(1).getCounters().get(0), CounterFaceTypeEnum.TRADING_FLEET_PLUS, 4, game)).thenReturn(diffSwitch54);

        DiffEntity diffSwitch43 = new DiffEntity();
        when(counterDomain.switchCounter(game.getStacks().get(1).getCounters().get(1), CounterFaceTypeEnum.TRADING_FLEET_MINUS, 3, game)).thenReturn(diffSwitch43);

        DiffEntity diffRemove10Ang = new DiffEntity();
        when(counterDomain.removeCounter(game.getStacks().get(1).getCounters().get(2))).thenReturn(diffRemove10Ang);

        DiffEntity diffAdd01 = new DiffEntity();
        when(counterDomain.createCounter(CounterFaceTypeEnum.TRADING_FLEET_MINUS, "suede", "ZMCaraibes", 1, game)).thenReturn(diffAdd01);

        List<DiffEntity> diffs = economicService.computeAutomaticTfCompetitions(game, newTfis);

        Assert.assertEquals(6, diffs.size());
        Assert.assertEquals(diffRemove10Ang, diffs.get(0));
        Assert.assertEquals(diffSwitch43, diffs.get(1));
        Assert.assertEquals(diffSwitch54, diffs.get(2));
        Assert.assertEquals(diffAdd01, diffs.get(3));
        Assert.assertEquals(diffSwitch56, diffs.get(4));
        Assert.assertEquals(diffRemove10, diffs.get(5));

        Collections.sort(game.getCompetitions(), (o1, o2) -> {
            int diff = o2.getProvince().compareTo(o1.getProvince());

            if (diff == 0) {
                diff = o1.getType().compareTo(o2.getType());
            }

            return diff;
        });

        Assert.assertEquals(3, game.getCompetitions().size());

        Comparator<CompetitionRoundEntity> comparatorRound = (o1, o2) -> {
            int diff = o1.getRound().compareTo(o2.getRound());

            if (diff == 0) {
                diff = o2.getCountry().compareTo(o1.getCountry());
            }

            return diff;
        };

        Collections.sort(game.getCompetitions().get(0).getRounds(), comparatorRound);
        Collections.sort(game.getCompetitions().get(1).getRounds(), comparatorRound);
        Collections.sort(game.getCompetitions().get(2).getRounds(), comparatorRound);

        Assert.assertEquals("ZPVenise", game.getCompetitions().get(0).getProvince());
        Assert.assertEquals(CompetitionTypeEnum.TF_6, game.getCompetitions().get(0).getType());
        Assert.assertEquals(game, game.getCompetitions().get(0).getGame());
        Assert.assertEquals(game.getTurn(), game.getCompetitions().get(0).getTurn());
        Assert.assertEquals(3, game.getCompetitions().get(0).getRounds().size());
        Assert.assertEquals("venise", game.getCompetitions().get(0).getRounds().get(0).getCountry());
        Assert.assertEquals(4, game.getCompetitions().get(0).getRounds().get(0).getColumn().intValue());
        Assert.assertEquals(5, game.getCompetitions().get(0).getRounds().get(0).getDie().intValue());
        Assert.assertEquals(ResultEnum.SUCCESS, game.getCompetitions().get(0).getRounds().get(0).getResult());
        Assert.assertEquals(1, game.getCompetitions().get(0).getRounds().get(0).getRound().intValue());
        Assert.assertEquals(null, game.getCompetitions().get(0).getRounds().get(0).getSecondaryDie());
        Assert.assertEquals(null, game.getCompetitions().get(0).getRounds().get(0).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(0), game.getCompetitions().get(0).getRounds().get(0).getCompetition());
        Assert.assertEquals("turquie", game.getCompetitions().get(0).getRounds().get(1).getCountry());
        Assert.assertEquals(-2, game.getCompetitions().get(0).getRounds().get(1).getColumn().intValue());
        Assert.assertEquals(1, game.getCompetitions().get(0).getRounds().get(1).getDie().intValue());
        Assert.assertEquals(ResultEnum.FUMBLE, game.getCompetitions().get(0).getRounds().get(1).getResult());
        Assert.assertEquals(1, game.getCompetitions().get(0).getRounds().get(1).getRound().intValue());
        Assert.assertEquals(null, game.getCompetitions().get(0).getRounds().get(1).getSecondaryDie());
        Assert.assertEquals(null, game.getCompetitions().get(0).getRounds().get(1).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(0), game.getCompetitions().get(0).getRounds().get(1).getCompetition());
        Assert.assertEquals("angleterre", game.getCompetitions().get(0).getRounds().get(2).getCountry());
        Assert.assertEquals(1, game.getCompetitions().get(0).getRounds().get(2).getColumn().intValue());
        Assert.assertEquals(4, game.getCompetitions().get(0).getRounds().get(2).getDie().intValue());
        Assert.assertEquals(ResultEnum.AVERAGE, game.getCompetitions().get(0).getRounds().get(2).getResult());
        Assert.assertEquals(1, game.getCompetitions().get(0).getRounds().get(2).getRound().intValue());
        Assert.assertEquals(6, game.getCompetitions().get(0).getRounds().get(2).getSecondaryDie().intValue());
        Assert.assertEquals(false, game.getCompetitions().get(0).getRounds().get(2).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(0), game.getCompetitions().get(0).getRounds().get(2).getCompetition());

        Assert.assertEquals("ZMCaraibes", game.getCompetitions().get(1).getProvince());
        Assert.assertEquals(CompetitionTypeEnum.TF_6, game.getCompetitions().get(1).getType());
        Assert.assertEquals(game, game.getCompetitions().get(1).getGame());
        Assert.assertEquals(game.getTurn(), game.getCompetitions().get(1).getTurn());
        Assert.assertEquals(10, game.getCompetitions().get(1).getRounds().size());
        Assert.assertEquals("suede", game.getCompetitions().get(1).getRounds().get(0).getCountry());
        Assert.assertEquals(-2, game.getCompetitions().get(1).getRounds().get(0).getColumn().intValue());
        Assert.assertEquals(1, game.getCompetitions().get(1).getRounds().get(0).getDie().intValue());
        Assert.assertEquals(ResultEnum.FUMBLE, game.getCompetitions().get(1).getRounds().get(0).getResult());
        Assert.assertEquals(1, game.getCompetitions().get(1).getRounds().get(0).getRound().intValue());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(0).getSecondaryDie());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(0).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(1), game.getCompetitions().get(1).getRounds().get(0).getCompetition());
        Assert.assertEquals("france", game.getCompetitions().get(1).getRounds().get(1).getCountry());
        Assert.assertEquals(-1, game.getCompetitions().get(1).getRounds().get(1).getColumn().intValue());
        Assert.assertEquals(6, game.getCompetitions().get(1).getRounds().get(1).getDie().intValue());
        Assert.assertEquals(ResultEnum.AVERAGE_PLUS, game.getCompetitions().get(1).getRounds().get(1).getResult());
        Assert.assertEquals(1, game.getCompetitions().get(1).getRounds().get(1).getRound().intValue());
        Assert.assertEquals(4, game.getCompetitions().get(1).getRounds().get(1).getSecondaryDie().intValue());
        Assert.assertEquals(true, game.getCompetitions().get(1).getRounds().get(1).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(1), game.getCompetitions().get(1).getRounds().get(1).getCompetition());
        Assert.assertEquals("espagne", game.getCompetitions().get(1).getRounds().get(2).getCountry());
        Assert.assertEquals(-2, game.getCompetitions().get(1).getRounds().get(2).getColumn().intValue());
        Assert.assertEquals(9, game.getCompetitions().get(1).getRounds().get(2).getDie().intValue());
        Assert.assertEquals(ResultEnum.SUCCESS, game.getCompetitions().get(1).getRounds().get(2).getResult());
        Assert.assertEquals(1, game.getCompetitions().get(1).getRounds().get(2).getRound().intValue());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(2).getSecondaryDie());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(2).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(1), game.getCompetitions().get(1).getRounds().get(2).getCompetition());
        Assert.assertEquals("angleterre", game.getCompetitions().get(1).getRounds().get(3).getCountry());
        Assert.assertEquals(1, game.getCompetitions().get(1).getRounds().get(3).getColumn().intValue());
        Assert.assertEquals(2, game.getCompetitions().get(1).getRounds().get(3).getDie().intValue());
        Assert.assertEquals(ResultEnum.FAILED, game.getCompetitions().get(1).getRounds().get(3).getResult());
        Assert.assertEquals(1, game.getCompetitions().get(1).getRounds().get(3).getRound().intValue());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(3).getSecondaryDie());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(3).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(1), game.getCompetitions().get(1).getRounds().get(3).getCompetition());
        Assert.assertEquals("suede", game.getCompetitions().get(1).getRounds().get(4).getCountry());
        Assert.assertEquals(-1, game.getCompetitions().get(1).getRounds().get(4).getColumn().intValue());
        Assert.assertEquals(10, game.getCompetitions().get(1).getRounds().get(4).getDie().intValue());
        Assert.assertEquals(ResultEnum.CRITICAL_HIT, game.getCompetitions().get(1).getRounds().get(4).getResult());
        Assert.assertEquals(2, game.getCompetitions().get(1).getRounds().get(4).getRound().intValue());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(4).getSecondaryDie());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(4).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(1), game.getCompetitions().get(1).getRounds().get(4).getCompetition());
        Assert.assertEquals("france", game.getCompetitions().get(1).getRounds().get(5).getCountry());
        Assert.assertEquals(1, game.getCompetitions().get(1).getRounds().get(5).getColumn().intValue());
        Assert.assertEquals(6, game.getCompetitions().get(1).getRounds().get(5).getDie().intValue());
        Assert.assertEquals(ResultEnum.SUCCESS, game.getCompetitions().get(1).getRounds().get(5).getResult());
        Assert.assertEquals(2, game.getCompetitions().get(1).getRounds().get(5).getRound().intValue());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(5).getSecondaryDie());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(5).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(1), game.getCompetitions().get(1).getRounds().get(5).getCompetition());
        Assert.assertEquals("espagne", game.getCompetitions().get(1).getRounds().get(6).getCountry());
        Assert.assertEquals(-1, game.getCompetitions().get(1).getRounds().get(6).getColumn().intValue());
        Assert.assertEquals(9, game.getCompetitions().get(1).getRounds().get(6).getDie().intValue());
        Assert.assertEquals(ResultEnum.CRITICAL_HIT, game.getCompetitions().get(1).getRounds().get(6).getResult());
        Assert.assertEquals(2, game.getCompetitions().get(1).getRounds().get(6).getRound().intValue());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(6).getSecondaryDie());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(6).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(1), game.getCompetitions().get(1).getRounds().get(6).getCompetition());
        Assert.assertEquals("suede", game.getCompetitions().get(1).getRounds().get(7).getCountry());
        Assert.assertEquals(-1, game.getCompetitions().get(1).getRounds().get(7).getColumn().intValue());
        Assert.assertEquals(10, game.getCompetitions().get(1).getRounds().get(7).getDie().intValue());
        Assert.assertEquals(ResultEnum.CRITICAL_HIT, game.getCompetitions().get(1).getRounds().get(7).getResult());
        Assert.assertEquals(3, game.getCompetitions().get(1).getRounds().get(7).getRound().intValue());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(7).getSecondaryDie());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(7).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(1), game.getCompetitions().get(1).getRounds().get(7).getCompetition());
        Assert.assertEquals("france", game.getCompetitions().get(1).getRounds().get(8).getCountry());
        Assert.assertEquals(1, game.getCompetitions().get(1).getRounds().get(8).getColumn().intValue());
        Assert.assertEquals(3, game.getCompetitions().get(1).getRounds().get(8).getDie().intValue());
        Assert.assertEquals(ResultEnum.FAILED, game.getCompetitions().get(1).getRounds().get(8).getResult());
        Assert.assertEquals(3, game.getCompetitions().get(1).getRounds().get(8).getRound().intValue());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(8).getSecondaryDie());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(8).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(1), game.getCompetitions().get(1).getRounds().get(8).getCompetition());
        Assert.assertEquals("espagne", game.getCompetitions().get(1).getRounds().get(9).getCountry());
        Assert.assertEquals(-1, game.getCompetitions().get(1).getRounds().get(9).getColumn().intValue());
        Assert.assertEquals(9, game.getCompetitions().get(1).getRounds().get(9).getDie().intValue());
        Assert.assertEquals(ResultEnum.CRITICAL_HIT, game.getCompetitions().get(1).getRounds().get(9).getResult());
        Assert.assertEquals(3, game.getCompetitions().get(1).getRounds().get(9).getRound().intValue());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(9).getSecondaryDie());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(9).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(1), game.getCompetitions().get(1).getRounds().get(9).getCompetition());

        Assert.assertEquals("ZMCaraibes", game.getCompetitions().get(2).getProvince());
        Assert.assertEquals(CompetitionTypeEnum.TF_4, game.getCompetitions().get(2).getType());
        Assert.assertEquals(game, game.getCompetitions().get(2).getGame());
        Assert.assertEquals(game.getTurn(), game.getCompetitions().get(2).getTurn());
        Assert.assertEquals(2, game.getCompetitions().get(2).getRounds().size());
        Assert.assertEquals("france", game.getCompetitions().get(2).getRounds().get(0).getCountry());
        Assert.assertEquals(1, game.getCompetitions().get(2).getRounds().get(0).getColumn().intValue());
        Assert.assertEquals(1, game.getCompetitions().get(2).getRounds().get(0).getDie().intValue());
        Assert.assertEquals(ResultEnum.FUMBLE, game.getCompetitions().get(2).getRounds().get(0).getResult());
        Assert.assertEquals(1, game.getCompetitions().get(2).getRounds().get(0).getRound().intValue());
        Assert.assertEquals(null, game.getCompetitions().get(2).getRounds().get(0).getSecondaryDie());
        Assert.assertEquals(null, game.getCompetitions().get(2).getRounds().get(0).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(2), game.getCompetitions().get(2).getRounds().get(0).getCompetition());
        Assert.assertEquals("espagne", game.getCompetitions().get(2).getRounds().get(1).getCountry());
        Assert.assertEquals(-1, game.getCompetitions().get(2).getRounds().get(1).getColumn().intValue());
        Assert.assertEquals(1, game.getCompetitions().get(2).getRounds().get(1).getDie().intValue());
        Assert.assertEquals(ResultEnum.FUMBLE, game.getCompetitions().get(2).getRounds().get(1).getResult());
        Assert.assertEquals(1, game.getCompetitions().get(2).getRounds().get(1).getRound().intValue());
        Assert.assertEquals(null, game.getCompetitions().get(2).getRounds().get(1).getSecondaryDie());
        Assert.assertEquals(null, game.getCompetitions().get(2).getRounds().get(1).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(2), game.getCompetitions().get(2).getRounds().get(1).getCompetition());
    }

    @Test
    public void testEstablishmentCompetitions() {
        GameEntity game = new GameEntity();

        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setId(100L);
        game.getStacks().get(0).setProvince("rBelem~W");
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(100L);
        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.MINOR_ESTABLISHMENT_PLUS);
        game.getStacks().get(0).getCounters().get(0).setOwner(game.getStacks().get(0));
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(1).setId(101L);
        game.getStacks().get(0).getCounters().get(1).setCountry("hollande");
        game.getStacks().get(0).getCounters().get(1).setType(CounterFaceTypeEnum.COLONY_PLUS);
        game.getStacks().get(0).getCounters().get(1).setOwner(game.getStacks().get(0));
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(2).setId(102L);
        game.getStacks().get(0).getCounters().get(2).setCountry("portugal");
        game.getStacks().get(0).getCounters().get(2).setType(CounterFaceTypeEnum.TRADING_POST_MINUS);
        game.getStacks().get(0).getCounters().get(2).setOwner(game.getStacks().get(0));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).setId(200L);
        game.getStacks().get(1).setProvince("rBenin~E");
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setId(200L);
        game.getStacks().get(1).getCounters().get(0).setCountry("france");
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.COLONY_PLUS);
        game.getStacks().get(1).getCounters().get(0).setEstablishment(new EstablishmentEntity());
        game.getStacks().get(1).getCounters().get(0).getEstablishment().setLevel(4);
        game.getStacks().get(1).getCounters().get(0).setOwner(game.getStacks().get(1));
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(1).setId(201L);
        game.getStacks().get(1).getCounters().get(1).setCountry("espagne");
        game.getStacks().get(1).getCounters().get(1).setType(CounterFaceTypeEnum.COLONY_MINUS);
        game.getStacks().get(1).getCounters().get(1).setOwner(game.getStacks().get(1));
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(2).setId(202L);
        game.getStacks().get(1).getCounters().get(2).setCountry("angleterre");
        game.getStacks().get(1).getCounters().get(2).setType(CounterFaceTypeEnum.TRADING_POST_MINUS);
        game.getStacks().get(1).getCounters().get(2).setOwner(game.getStacks().get(1));
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(3).setId(203L);
        game.getStacks().get(1).getCounters().get(3).setCountry("portugal");
        game.getStacks().get(1).getCounters().get(3).setType(CounterFaceTypeEnum.TRADING_POST_PLUS);
        game.getStacks().get(1).getCounters().get(3).setOwner(game.getStacks().get(1));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(2).setId(300L);
        game.getStacks().get(2).setProvince("Congo~S");
        game.getStacks().get(2).getCounters().add(new CounterEntity());
        game.getStacks().get(2).getCounters().get(0).setId(300L);
        game.getStacks().get(2).getCounters().get(0).setCountry("turquie");
        game.getStacks().get(2).getCounters().get(0).setType(CounterFaceTypeEnum.TRADING_POST_MINUS);
        game.getStacks().get(2).getCounters().get(0).setEstablishment(new EstablishmentEntity());
        game.getStacks().get(2).getCounters().get(0).getEstablishment().setLevel(2);
        game.getStacks().get(2).getCounters().get(0).setOwner(game.getStacks().get(1));
        game.getStacks().get(2).getCounters().add(new CounterEntity());
        game.getStacks().get(2).getCounters().get(1).setId(301L);
        game.getStacks().get(2).getCounters().get(1).setCountry("portugal");
        game.getStacks().get(2).getCounters().get(1).setType(CounterFaceTypeEnum.TRADING_POST_PLUS);
        game.getStacks().get(2).getCounters().get(1).setEstablishment(new EstablishmentEntity());
        game.getStacks().get(2).getCounters().get(1).getEstablishment().setLevel(4);
        game.getStacks().get(2).getCounters().get(1).setOwner(game.getStacks().get(1));

        Tables tables = new Tables();
        List<Result> results = new ArrayList<>();
        Result result = new Result();
        result.setColumn(-2);
        result.setDie(1);
        result.setResult(ResultEnum.FUMBLE);
        results.add(result);
        result = new Result();
        result.setColumn(-2);
        result.setDie(2);
        result.setResult(ResultEnum.FUMBLE);
        results.add(result);
        result = new Result();
        result.setColumn(-2);
        result.setDie(3);
        result.setResult(ResultEnum.FUMBLE);
        results.add(result);
        result = new Result();
        result.setColumn(-2);
        result.setDie(4);
        result.setResult(ResultEnum.FAILED);
        results.add(result);
        result = new Result();
        result.setColumn(-2);
        result.setDie(5);
        result.setResult(ResultEnum.FAILED);
        results.add(result);
        result = new Result();
        result.setColumn(-2);
        result.setDie(6);
        result.setResult(ResultEnum.AVERAGE);
        results.add(result);
        result = new Result();
        result.setColumn(-2);
        result.setDie(7);
        result.setResult(ResultEnum.AVERAGE_PLUS);
        results.add(result);
        result = new Result();
        result.setColumn(-2);
        result.setDie(8);
        result.setResult(ResultEnum.SUCCESS);
        results.add(result);
        result = new Result();
        result.setColumn(-2);
        result.setDie(9);
        result.setResult(ResultEnum.SUCCESS);
        results.add(result);
        result = new Result();
        result.setColumn(-2);
        result.setDie(10);
        result.setResult(ResultEnum.CRITICAL_HIT);
        results.add(result);

        result = new Result();
        result.setColumn(-1);
        result.setDie(1);
        result.setResult(ResultEnum.FUMBLE);
        results.add(result);
        result = new Result();
        result.setColumn(-1);
        result.setDie(2);
        result.setResult(ResultEnum.FUMBLE);
        results.add(result);
        result = new Result();
        result.setColumn(-1);
        result.setDie(3);
        result.setResult(ResultEnum.FAILED);
        results.add(result);
        result = new Result();
        result.setColumn(-1);
        result.setDie(4);
        result.setResult(ResultEnum.FAILED);
        results.add(result);
        result = new Result();
        result.setColumn(-1);
        result.setDie(5);
        result.setResult(ResultEnum.AVERAGE);
        results.add(result);
        result = new Result();
        result.setColumn(-1);
        result.setDie(6);
        result.setResult(ResultEnum.AVERAGE_PLUS);
        results.add(result);
        result = new Result();
        result.setColumn(-1);
        result.setDie(7);
        result.setResult(ResultEnum.SUCCESS);
        results.add(result);
        result = new Result();
        result.setColumn(-1);
        result.setDie(8);
        result.setResult(ResultEnum.SUCCESS);
        results.add(result);
        result = new Result();
        result.setColumn(-1);
        result.setDie(9);
        result.setResult(ResultEnum.CRITICAL_HIT);
        results.add(result);
        result = new Result();
        result.setColumn(-1);
        result.setDie(10);
        result.setResult(ResultEnum.CRITICAL_HIT);
        results.add(result);

        result = new Result();
        result.setColumn(1);
        result.setDie(1);
        result.setResult(ResultEnum.FUMBLE);
        results.add(result);
        result = new Result();
        result.setColumn(1);
        result.setDie(2);
        result.setResult(ResultEnum.FAILED);
        results.add(result);
        result = new Result();
        result.setColumn(1);
        result.setDie(3);
        result.setResult(ResultEnum.FAILED);
        results.add(result);
        result = new Result();
        result.setColumn(1);
        result.setDie(4);
        result.setResult(ResultEnum.AVERAGE);
        results.add(result);
        result = new Result();
        result.setColumn(1);
        result.setDie(5);
        result.setResult(ResultEnum.AVERAGE_PLUS);
        results.add(result);
        result = new Result();
        result.setColumn(1);
        result.setDie(6);
        result.setResult(ResultEnum.SUCCESS);
        results.add(result);
        result = new Result();
        result.setColumn(1);
        result.setDie(7);
        result.setResult(ResultEnum.SUCCESS);
        results.add(result);
        result = new Result();
        result.setColumn(1);
        result.setDie(8);
        result.setResult(ResultEnum.CRITICAL_HIT);
        results.add(result);
        result = new Result();
        result.setColumn(1);
        result.setDie(9);
        result.setResult(ResultEnum.CRITICAL_HIT);
        results.add(result);
        result = new Result();
        result.setColumn(1);
        result.setDie(10);
        result.setResult(ResultEnum.CRITICAL_HIT);
        results.add(result);

        result = new Result();
        result.setColumn(4);
        result.setDie(1);
        result.setResult(ResultEnum.FAILED);
        results.add(result);
        result = new Result();
        result.setColumn(4);
        result.setDie(2);
        result.setResult(ResultEnum.AVERAGE);
        results.add(result);
        result = new Result();
        result.setColumn(4);
        result.setDie(3);
        result.setResult(ResultEnum.AVERAGE_PLUS);
        results.add(result);
        result = new Result();
        result.setColumn(4);
        result.setDie(4);
        result.setResult(ResultEnum.AVERAGE_PLUS);
        results.add(result);
        result = new Result();
        result.setColumn(4);
        result.setDie(5);
        result.setResult(ResultEnum.SUCCESS);
        results.add(result);
        result = new Result();
        result.setColumn(4);
        result.setDie(6);
        result.setResult(ResultEnum.SUCCESS);
        results.add(result);
        result = new Result();
        result.setColumn(4);
        result.setDie(7);
        result.setResult(ResultEnum.CRITICAL_HIT);
        results.add(result);
        result = new Result();
        result.setColumn(4);
        result.setDie(8);
        result.setResult(ResultEnum.CRITICAL_HIT);
        results.add(result);
        result = new Result();
        result.setColumn(4);
        result.setDie(9);
        result.setResult(ResultEnum.CRITICAL_HIT);
        results.add(result);
        result = new Result();
        result.setColumn(4);
        result.setDie(10);
        result.setResult(ResultEnum.CRITICAL_HIT);
        results.add(result);

        tables.getResults().addAll(results);
        EconomicServiceImpl.TABLES = tables;

        Set<String> establishments = new HashSet<>();
        establishments.add("rBelem~W");
        establishments.add("rBenin~E");
        establishments.add("Congo~S");

        RotwProvinceEntity belem = new RotwProvinceEntity();
        belem.setName("rBelem~W");
        when(provinceDao.getProvinceByName("rBelem~W")).thenReturn(belem);

        RotwProvinceEntity benin = new RotwProvinceEntity();
        benin.setName("rBenin~E");
        when(provinceDao.getProvinceByName("rBenin~E")).thenReturn(benin);

        RotwProvinceEntity congo = new RotwProvinceEntity();
        congo.setName("Congo~S");
        when(provinceDao.getProvinceByName("Congo~S")).thenReturn(congo);

        // To isolate unexpected calls
        DiffEntity diffOther = new DiffEntity();
        when(counterDomain.removeCounter(any())).thenReturn(diffOther);
        when(counterDomain.switchCounter(any(), any(), any(), any())).thenReturn(diffOther);

        DiffEntity remove101 = new DiffEntity();
        when(counterDomain.removeCounter(game.getStacks().get(0).getCounters().get(1))).thenReturn(remove101);

        DiffEntity remove102 = new DiffEntity();
        when(counterDomain.removeCounter(game.getStacks().get(0).getCounters().get(2))).thenReturn(remove102);

        DiffEntity remove202 = new DiffEntity();
        when(counterDomain.removeCounter(game.getStacks().get(1).getCounters().get(2))).thenReturn(remove202);

        DiffEntity remove203 = new DiffEntity();
        when(counterDomain.removeCounter(game.getStacks().get(1).getCounters().get(3))).thenReturn(remove203);

        DiffEntity remove201 = new DiffEntity();
        when(counterDomain.removeCounter(game.getStacks().get(1).getCounters().get(1))).thenReturn(remove201);

        DiffEntity remove300 = new DiffEntity();
        when(counterDomain.removeCounter(game.getStacks().get(2).getCounters().get(0))).thenReturn(remove300);

        DiffEntity switch301 = new DiffEntity();
        when(counterDomain.switchCounter(game.getStacks().get(2).getCounters().get(1), CounterFaceTypeEnum.TRADING_POST_MINUS, 1, game)).thenReturn(switch301);

        when(oeUtil.getFti(game, tables, "france")).thenReturn(3);
        when(oeUtil.getFti(game, tables, "espagne")).thenReturn(2);
        when(oeUtil.getFti(game, tables, "turquie")).thenReturn(3);
        when(oeUtil.getFti(game, tables, "portugal")).thenReturn(4);

        // France wins its competition on first round with 6 on column 1 => SUCCESS
        when(oeUtil.rollDie(game, "france")).thenReturn(6);
        // Espagne loses its competition on first round with 4 on column -1 => FAILED
        when(oeUtil.rollDie(game, "espagne")).thenReturn(4);
        // Turquie wins the first three rounds with 7 on column -1 => SUCCESS
        when(oeUtil.rollDie(game, "turquie")).thenReturn(7, 7, 7)
        // Then loses the last two rounds with 4 on column -1 => FAILED
                .thenReturn(4, 4);
        // Portugal wins the first round and loses the second and the third with 6/3/3 on column 1 => SUCCESS/FAILED/FAILED
        when(oeUtil.rollDie(game, "portugal")).thenReturn(6, 3, 3)
        // Then loses the third round with 3 on column 1 => FAILED
                .thenReturn(3)
        // Then wins the last round with 6 on column 1 => SUCCESS
                .thenReturn(6);

        List<DiffEntity> diffs = economicService.computeAutomaticEstablishmentCompetitions(game, establishments);

        Assert.assertEquals(7, diffs.size());

        Assert.assertEquals(remove102, diffs.get(0));
        Assert.assertEquals(remove101, diffs.get(1));
        Assert.assertEquals(remove300, diffs.get(2));
        Assert.assertEquals(switch301, diffs.get(3));
        Assert.assertEquals(remove202, diffs.get(4));
        Assert.assertEquals(remove203, diffs.get(5));
        Assert.assertEquals(remove201, diffs.get(6));

        Collections.sort(game.getCompetitions(), (o1, o2) -> {
            int diff = o2.getProvince().compareTo(o1.getProvince());

            if (diff == 0) {
                diff = o1.getType().compareTo(o2.getType());
            }

            return diff;
        });

        Assert.assertEquals(2, game.getCompetitions().size());

        Comparator<CompetitionRoundEntity> comparatorRound = (o1, o2) -> {
            int diff = o1.getRound().compareTo(o2.getRound());

            if (diff == 0) {
                diff = o1.getCountry().compareTo(o2.getCountry());
            }

            return diff;
        };

        Collections.sort(game.getCompetitions().get(0).getRounds(), comparatorRound);
        Collections.sort(game.getCompetitions().get(1).getRounds(), comparatorRound);

        Assert.assertEquals("rBenin~E", game.getCompetitions().get(0).getProvince());
        Assert.assertEquals(CompetitionTypeEnum.ESTABLISHMENT, game.getCompetitions().get(0).getType());
        Assert.assertEquals(game, game.getCompetitions().get(0).getGame());
        Assert.assertEquals(game.getTurn(), game.getCompetitions().get(0).getTurn());
        Assert.assertEquals(2, game.getCompetitions().get(0).getRounds().size());
        Assert.assertEquals("espagne", game.getCompetitions().get(0).getRounds().get(0).getCountry());
        Assert.assertEquals(-1, game.getCompetitions().get(0).getRounds().get(0).getColumn().intValue());
        Assert.assertEquals(4, game.getCompetitions().get(0).getRounds().get(0).getDie().intValue());
        Assert.assertEquals(ResultEnum.FAILED, game.getCompetitions().get(0).getRounds().get(0).getResult());
        Assert.assertEquals(1, game.getCompetitions().get(0).getRounds().get(0).getRound().intValue());
        Assert.assertEquals(null, game.getCompetitions().get(0).getRounds().get(0).getSecondaryDie());
        Assert.assertEquals(null, game.getCompetitions().get(0).getRounds().get(0).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(0), game.getCompetitions().get(0).getRounds().get(0).getCompetition());
        Assert.assertEquals("france", game.getCompetitions().get(0).getRounds().get(1).getCountry());
        Assert.assertEquals(1, game.getCompetitions().get(0).getRounds().get(1).getColumn().intValue());
        Assert.assertEquals(6, game.getCompetitions().get(0).getRounds().get(1).getDie().intValue());
        Assert.assertEquals(ResultEnum.SUCCESS, game.getCompetitions().get(0).getRounds().get(1).getResult());
        Assert.assertEquals(1, game.getCompetitions().get(0).getRounds().get(1).getRound().intValue());
        Assert.assertEquals(null, game.getCompetitions().get(0).getRounds().get(1).getSecondaryDie());
        Assert.assertEquals(null, game.getCompetitions().get(0).getRounds().get(1).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(0), game.getCompetitions().get(0).getRounds().get(1).getCompetition());

        Assert.assertEquals("Congo~S", game.getCompetitions().get(1).getProvince());
        Assert.assertEquals(CompetitionTypeEnum.ESTABLISHMENT, game.getCompetitions().get(1).getType());
        Assert.assertEquals(game, game.getCompetitions().get(1).getGame());
        Assert.assertEquals(game.getTurn(), game.getCompetitions().get(1).getTurn());
        Assert.assertEquals(10, game.getCompetitions().get(1).getRounds().size());
        Assert.assertEquals("portugal", game.getCompetitions().get(1).getRounds().get(0).getCountry());
        Assert.assertEquals(1, game.getCompetitions().get(1).getRounds().get(0).getColumn().intValue());
        Assert.assertEquals(6, game.getCompetitions().get(1).getRounds().get(0).getDie().intValue());
        Assert.assertEquals(ResultEnum.SUCCESS, game.getCompetitions().get(1).getRounds().get(0).getResult());
        Assert.assertEquals(1, game.getCompetitions().get(1).getRounds().get(0).getRound().intValue());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(0).getSecondaryDie());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(0).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(1), game.getCompetitions().get(1).getRounds().get(0).getCompetition());
        Assert.assertEquals("turquie", game.getCompetitions().get(1).getRounds().get(1).getCountry());
        Assert.assertEquals(-1, game.getCompetitions().get(1).getRounds().get(1).getColumn().intValue());
        Assert.assertEquals(7, game.getCompetitions().get(1).getRounds().get(1).getDie().intValue());
        Assert.assertEquals(ResultEnum.SUCCESS, game.getCompetitions().get(1).getRounds().get(1).getResult());
        Assert.assertEquals(1, game.getCompetitions().get(1).getRounds().get(1).getRound().intValue());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(1).getSecondaryDie());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(1).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(1), game.getCompetitions().get(1).getRounds().get(1).getCompetition());
        Assert.assertEquals("portugal", game.getCompetitions().get(1).getRounds().get(2).getCountry());
        Assert.assertEquals(1, game.getCompetitions().get(1).getRounds().get(2).getColumn().intValue());
        Assert.assertEquals(3, game.getCompetitions().get(1).getRounds().get(2).getDie().intValue());
        Assert.assertEquals(ResultEnum.FAILED, game.getCompetitions().get(1).getRounds().get(2).getResult());
        Assert.assertEquals(2, game.getCompetitions().get(1).getRounds().get(2).getRound().intValue());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(2).getSecondaryDie());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(2).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(1), game.getCompetitions().get(1).getRounds().get(2).getCompetition());
        Assert.assertEquals("turquie", game.getCompetitions().get(1).getRounds().get(3).getCountry());
        Assert.assertEquals(-1, game.getCompetitions().get(1).getRounds().get(3).getColumn().intValue());
        Assert.assertEquals(7, game.getCompetitions().get(1).getRounds().get(3).getDie().intValue());
        Assert.assertEquals(ResultEnum.SUCCESS, game.getCompetitions().get(1).getRounds().get(3).getResult());
        Assert.assertEquals(2, game.getCompetitions().get(1).getRounds().get(3).getRound().intValue());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(3).getSecondaryDie());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(3).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(1), game.getCompetitions().get(1).getRounds().get(3).getCompetition());
        Assert.assertEquals("portugal", game.getCompetitions().get(1).getRounds().get(4).getCountry());
        Assert.assertEquals(1, game.getCompetitions().get(1).getRounds().get(4).getColumn().intValue());
        Assert.assertEquals(3, game.getCompetitions().get(1).getRounds().get(4).getDie().intValue());
        Assert.assertEquals(ResultEnum.FAILED, game.getCompetitions().get(1).getRounds().get(4).getResult());
        Assert.assertEquals(3, game.getCompetitions().get(1).getRounds().get(4).getRound().intValue());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(4).getSecondaryDie());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(4).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(1), game.getCompetitions().get(1).getRounds().get(4).getCompetition());
        Assert.assertEquals("turquie", game.getCompetitions().get(1).getRounds().get(5).getCountry());
        Assert.assertEquals(-1, game.getCompetitions().get(1).getRounds().get(5).getColumn().intValue());
        Assert.assertEquals(7, game.getCompetitions().get(1).getRounds().get(5).getDie().intValue());
        Assert.assertEquals(ResultEnum.SUCCESS, game.getCompetitions().get(1).getRounds().get(5).getResult());
        Assert.assertEquals(3, game.getCompetitions().get(1).getRounds().get(5).getRound().intValue());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(5).getSecondaryDie());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(5).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(1), game.getCompetitions().get(1).getRounds().get(5).getCompetition());
        Assert.assertEquals("portugal", game.getCompetitions().get(1).getRounds().get(6).getCountry());
        Assert.assertEquals(1, game.getCompetitions().get(1).getRounds().get(6).getColumn().intValue());
        Assert.assertEquals(3, game.getCompetitions().get(1).getRounds().get(6).getDie().intValue());
        Assert.assertEquals(ResultEnum.FAILED, game.getCompetitions().get(1).getRounds().get(6).getResult());
        Assert.assertEquals(4, game.getCompetitions().get(1).getRounds().get(6).getRound().intValue());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(6).getSecondaryDie());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(6).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(1), game.getCompetitions().get(1).getRounds().get(6).getCompetition());
        Assert.assertEquals("turquie", game.getCompetitions().get(1).getRounds().get(7).getCountry());
        Assert.assertEquals(-1, game.getCompetitions().get(1).getRounds().get(7).getColumn().intValue());
        Assert.assertEquals(4, game.getCompetitions().get(1).getRounds().get(7).getDie().intValue());
        Assert.assertEquals(ResultEnum.FAILED, game.getCompetitions().get(1).getRounds().get(7).getResult());
        Assert.assertEquals(4, game.getCompetitions().get(1).getRounds().get(7).getRound().intValue());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(7).getSecondaryDie());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(7).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(1), game.getCompetitions().get(1).getRounds().get(7).getCompetition());
        Assert.assertEquals("portugal", game.getCompetitions().get(1).getRounds().get(8).getCountry());
        Assert.assertEquals(1, game.getCompetitions().get(1).getRounds().get(8).getColumn().intValue());
        Assert.assertEquals(6, game.getCompetitions().get(1).getRounds().get(8).getDie().intValue());
        Assert.assertEquals(ResultEnum.SUCCESS, game.getCompetitions().get(1).getRounds().get(8).getResult());
        Assert.assertEquals(5, game.getCompetitions().get(1).getRounds().get(8).getRound().intValue());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(8).getSecondaryDie());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(8).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(1), game.getCompetitions().get(1).getRounds().get(8).getCompetition());
        Assert.assertEquals("turquie", game.getCompetitions().get(1).getRounds().get(9).getCountry());
        Assert.assertEquals(-1, game.getCompetitions().get(1).getRounds().get(9).getColumn().intValue());
        Assert.assertEquals(4, game.getCompetitions().get(1).getRounds().get(9).getDie().intValue());
        Assert.assertEquals(ResultEnum.FAILED, game.getCompetitions().get(1).getRounds().get(9).getResult());
        Assert.assertEquals(5, game.getCompetitions().get(1).getRounds().get(9).getRound().intValue());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(9).getSecondaryDie());
        Assert.assertEquals(null, game.getCompetitions().get(1).getRounds().get(9).isSecondaryResult());
        Assert.assertEquals(game.getCompetitions().get(1), game.getCompetitions().get(1).getRounds().get(9).getCompetition());
    }

    @Test
    public void testAutomaticTechnology() {
        // Turn 11 = ISLAM, ORTHODOX, ROT
        // Turn 6 = LATIN, ISLAM

        GameEntity game = new GameEntity();
        game.setId(1L);
        game.setTurn(11);


        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setId(300L);
        game.getStacks().get(0).setProvince("B_TECH_3");
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(300L);
        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_LAND_ASIA);
        game.getStacks().get(0).getCounters().get(0).setOwner(game.getStacks().get(0));
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(1).setId(301L);
        game.getStacks().get(0).getCounters().get(1).setType(CounterFaceTypeEnum.TECH_NAVAL_ASIA);
        game.getStacks().get(0).getCounters().get(1).setOwner(game.getStacks().get(0));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).setId(600L);
        game.getStacks().get(1).setProvince("B_TECH_6");
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setId(600L);
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_LAND_ORTHODOX);
        game.getStacks().get(1).getCounters().get(0).setOwner(game.getStacks().get(1));
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(1).setId(601L);
        game.getStacks().get(1).getCounters().get(1).setType(CounterFaceTypeEnum.TECH_NAVAL_ISLAM);
        game.getStacks().get(1).getCounters().get(1).setOwner(game.getStacks().get(1));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(2).setId(700L);
        game.getStacks().get(2).setProvince("B_TECH_7");
        game.getStacks().get(2).getCounters().add(new CounterEntity());
        game.getStacks().get(2).getCounters().get(0).setId(700L);
        game.getStacks().get(2).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_GALLEON_FLUYT);
        game.getStacks().get(2).getCounters().get(0).setOwner(game.getStacks().get(2));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(3).setId(800L);
        game.getStacks().get(3).setProvince("B_TECH_8");
        game.getStacks().get(3).getCounters().add(new CounterEntity());
        game.getStacks().get(3).getCounters().get(0).setId(800L);
        game.getStacks().get(3).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_LAND_ISLAM);
        game.getStacks().get(3).getCounters().get(0).setOwner(game.getStacks().get(3));
        game.getStacks().get(3).getCounters().add(new CounterEntity());
        game.getStacks().get(3).getCounters().get(1).setId(801L);
        game.getStacks().get(3).getCounters().get(1).setType(CounterFaceTypeEnum.TECH_NAVAL_ORTHODOX);
        game.getStacks().get(3).getCounters().get(1).setOwner(game.getStacks().get(3));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(4).setId(900L);
        game.getStacks().get(4).setProvince("B_TECH_9");
        game.getStacks().get(4).getCounters().add(new CounterEntity());
        game.getStacks().get(4).getCounters().get(0).setId(900L);
        game.getStacks().get(4).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_RENAISSANCE);
        game.getStacks().get(4).getCounters().get(0).setOwner(game.getStacks().get(4));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(5).setId(1200L);
        game.getStacks().get(5).setProvince("B_TECH_12");
        game.getStacks().get(5).getCounters().add(new CounterEntity());
        game.getStacks().get(5).getCounters().get(0).setId(1200L);
        game.getStacks().get(5).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_MUSKET);
        game.getStacks().get(5).getCounters().get(0).setOwner(game.getStacks().get(5));
        game.getStacks().get(5).getCounters().add(new CounterEntity());
        game.getStacks().get(5).getCounters().get(1).setId(1201L);
        game.getStacks().get(5).getCounters().get(1).setType(CounterFaceTypeEnum.TECH_NAVAL_LATIN);
        game.getStacks().get(5).getCounters().get(1).setOwner(game.getStacks().get(5));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(6).setId(200L);
        game.getStacks().get(6).setProvince("B_TECH_2");
        game.getStacks().get(6).getCounters().add(new CounterEntity());
        game.getStacks().get(6).getCounters().get(0).setId(200L);
        game.getStacks().get(6).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_NAE_GALEON);
        game.getStacks().get(6).getCounters().get(0).setOwner(game.getStacks().get(6));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(7).setId(1600L);
        game.getStacks().get(7).setProvince("B_TECH_16");
        game.getStacks().get(7).getCounters().add(new CounterEntity());
        game.getStacks().get(7).getCounters().get(0).setId(200L);
        game.getStacks().get(7).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_BAROQUE);
        game.getStacks().get(7).getCounters().get(0).setOwner(game.getStacks().get(7));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(8).setId(1700L);
        game.getStacks().get(8).setProvince("B_TECH_17");
        game.getStacks().get(8).getCounters().add(new CounterEntity());
        game.getStacks().get(8).getCounters().get(0).setId(200L);
        game.getStacks().get(8).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_LAND);
        game.getStacks().get(8).getCounters().get(0).setCountry("turquie");
        game.getStacks().get(8).getCounters().get(0).setOwner(game.getStacks().get(8));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(9).setId(1300L);
        game.getStacks().get(9).setProvince("B_TECH_13");
        game.getStacks().get(9).getCounters().add(new CounterEntity());
        game.getStacks().get(9).getCounters().get(0).setId(1300L);
        game.getStacks().get(9).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_NAVAL);
        game.getStacks().get(9).getCounters().get(0).setCountry("turquie");
        game.getStacks().get(9).getCounters().get(0).setOwner(game.getStacks().get(9));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(10).setId(1500L);
        game.getStacks().get(10).setProvince("B_TECH_15");
        game.getStacks().get(10).getCounters().add(new CounterEntity());
        game.getStacks().get(10).getCounters().get(0).setId(1500L);
        game.getStacks().get(10).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_BATTERY);
        game.getStacks().get(10).getCounters().get(0).setOwner(game.getStacks().get(10));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(11).setId(2000L);
        game.getStacks().get(11).setProvince("B_TECH_20");
        game.getStacks().get(11).getCounters().add(new CounterEntity());
        game.getStacks().get(11).getCounters().get(0).setId(2000L);
        game.getStacks().get(11).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_VESSEL);
        game.getStacks().get(11).getCounters().get(0).setOwner(game.getStacks().get(11));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(12).setId(2500L);
        game.getStacks().get(12).setProvince("B_TECH_25");
        game.getStacks().get(12).getCounters().add(new CounterEntity());
        game.getStacks().get(12).getCounters().get(0).setId(2500L);
        game.getStacks().get(12).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_NAVAL);
        game.getStacks().get(12).getCounters().get(0).setOwner(game.getStacks().get(12));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(13).setId(5000L);
        game.getStacks().get(13).setProvince("B_TECH_50");
        game.getStacks().get(13).getCounters().add(new CounterEntity());
        game.getStacks().get(13).getCounters().get(0).setId(5000L);
        game.getStacks().get(13).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_MANOEUVRE);
        game.getStacks().get(13).getCounters().get(0).setOwner(game.getStacks().get(13));
        game.getStacks().get(13).getCounters().add(new CounterEntity());
        game.getStacks().get(13).getCounters().get(1).setId(5001L);
        game.getStacks().get(13).getCounters().get(1).setType(CounterFaceTypeEnum.TECH_THREE_DECKER);
        game.getStacks().get(13).getCounters().get(1).setOwner(game.getStacks().get(13));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(14).setId(1000L);
        game.getStacks().get(14).setProvince("B_TECH_10");
        game.getStacks().get(14).getCounters().add(new CounterEntity());
        game.getStacks().get(14).getCounters().get(0).setId(1000L);
        game.getStacks().get(14).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_TERCIO);
        game.getStacks().get(14).getCounters().get(0).setOwner(game.getStacks().get(14));

        Tables tables = new Tables();
        List<Tech> techs = new ArrayList<>();
        Tech tech = new Tech();
        tech.setBeginTurn(1);
        tech.setBeginBox(1);
        tech.setLand(true);
        tech.setName(Tech.MEDIEVAL);
        techs.add(tech);
        tech = new Tech();
        tech.setBeginTurn(6);
        tech.setBeginBox(11);
        tech.setLand(true);
        tech.setName(Tech.RENAISSANCE);
        techs.add(tech);
        tech = new Tech();
        tech.setBeginTurn(11);
        tech.setBeginBox(21);
        tech.setLand(true);
        tech.setName(Tech.MUSKET);
        techs.add(tech);
        tech = new Tech();
        tech.setBeginTurn(21);
        tech.setBeginBox(31);
        tech.setLand(true);
        tech.setName(Tech.BAROQUE);
        techs.add(tech);
        tech = new Tech();
        tech.setBeginTurn(31);
        tech.setBeginBox(31);
        tech.setLand(true);
        tech.setName(Tech.MANOEUVRE);
        techs.add(tech);
        tech = new Tech();
        tech.setBeginTurn(6);
        tech.setBeginBox(2);
        tech.setLand(false);
        tech.setName(Tech.NAE_GALEON);
        techs.add(tech);
        tech = new Tech();
        tech.setBeginTurn(11);
        tech.setBeginBox(2);
        tech.setLand(false);
        tech.setName(Tech.BATTERY);
        techs.add(tech);
        tech = new Tech();
        tech.setBeginTurn(21);
        tech.setBeginBox(2);
        tech.setLand(false);
        tech.setName(Tech.VESSEL);
        techs.add(tech);
        tech = new Tech();
        tech.setBeginTurn(31);
        tech.setBeginBox(2);
        tech.setLand(false);
        tech.setName(Tech.THREE_DECKER);
        techs.add(tech);
        tables.getTechs().addAll(techs);
        EconomicServiceImpl.TABLES = tables;

        // To isolate unexpected calls
        DiffEntity diffOther = new DiffEntity();
        when(counterDomain.moveSpecialCounter(any(), any(), any(), any())).thenReturn(diffOther);

        DiffEntity diffOrthLand = new DiffEntity();
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.TECH_LAND_ORTHODOX, null, "B_TECH_7", game)).thenReturn(diffOrthLand);

        DiffEntity diffOrthNaval = new DiffEntity();
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.TECH_NAVAL_ORTHODOX, null, "B_TECH_9", game)).thenReturn(diffOrthNaval);

        DiffEntity diffIslLand = new DiffEntity();
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.TECH_LAND_ISLAM, null, "B_TECH_10", game)).thenReturn(diffIslLand);

        DiffEntity diffIslNaval = new DiffEntity();
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.TECH_NAVAL_ISLAM, null, "B_TECH_8", game)).thenReturn(diffIslNaval);

        DiffEntity diffAsiaLand = new DiffEntity();
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.TECH_LAND_ASIA, null, "B_TECH_4", game)).thenReturn(diffAsiaLand);

        DiffEntity diffAsiaNaval = new DiffEntity();
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.TECH_NAVAL_ASIA, null, "B_TECH_4", game)).thenReturn(diffAsiaNaval);

        DiffEntity diffMusk = new DiffEntity();
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.TECH_MUSKET, null, "B_TECH_11", game)).thenReturn(diffMusk);

        DiffEntity diffBar = new DiffEntity();
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.TECH_BAROQUE, null, "B_TECH_14", game)).thenReturn(diffBar);

        DiffEntity diffMan = new DiffEntity();
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.TECH_MANOEUVRE, null, "B_TECH_49", game)).thenReturn(diffMan);

        DiffEntity diffNaeGal = new DiffEntity();
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.TECH_NAE_GALEON, null, "B_TECH_1", game)).thenReturn(diffNaeGal);

        DiffEntity diffBat = new DiffEntity();
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.TECH_BATTERY, null, "B_TECH_14", game)).thenReturn(diffBat);

        DiffEntity diffVes = new DiffEntity();
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.TECH_VESSEL, null, "B_TECH_17", game)).thenReturn(diffVes);

        DiffEntity diff3d = new DiffEntity();
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.TECH_THREE_DECKER, null, "B_TECH_49", game)).thenReturn(diff3d);

        List<DiffEntity> diffs = economicService.computeAutomaticTechnologyAdvances(game);

        Assert.assertEquals(9, diffs.size());
        Assert.assertEquals(diffOrthLand, diffs.get(0));
        Assert.assertEquals(diffOrthNaval, diffs.get(1));
        Assert.assertEquals(diffIslLand, diffs.get(2));
        Assert.assertEquals(diffIslNaval, diffs.get(3));
        Assert.assertEquals(diffAsiaLand, diffs.get(4));
        Assert.assertEquals(diffAsiaNaval, diffs.get(5));
        Assert.assertEquals(diffMusk, diffs.get(6));
        Assert.assertEquals(diffNaeGal, diffs.get(7));
        Assert.assertEquals(diffBat, diffs.get(8));

        when(adminActionDao.getMaxTechBox(true, CultureEnum.ISLAM.getTechnologyCultures(), game.getId())).thenReturn(18);
        when(adminActionDao.getMaxTechBox(false, CultureEnum.ISLAM.getTechnologyCultures(), game.getId())).thenReturn(13);
        when(adminActionDao.getMaxTechBox(false, CultureEnum.LATIN.getTechnologyCultures(), game.getId())).thenReturn(19);

        DiffEntity diffLatNaval = new DiffEntity();
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.TECH_NAVAL_LATIN, null, "B_TECH_13", game)).thenReturn(diffLatNaval);

        DiffEntity diffIslLand2 = new DiffEntity();
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.TECH_LAND_ISLAM, null, "B_TECH_13", game)).thenReturn(diffIslLand2);

        DiffEntity diffIslNaval2 = new DiffEntity();
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.TECH_NAVAL_ISLAM, null, "B_TECH_8", game)).thenReturn(diffIslNaval2);

        game.setTurn(6);

        diffs = economicService.computeAutomaticTechnologyAdvances(game);

        Assert.assertEquals(4, diffs.size());
        Assert.assertEquals(diffLatNaval, diffs.get(0));
        Assert.assertEquals(diffIslLand2, diffs.get(1));
        Assert.assertEquals(diffIslNaval2, diffs.get(2));
        Assert.assertEquals(diffNaeGal, diffs.get(3));

        game.setTurn(25);

        diffs = economicService.computeAutomaticTechnologyAdvances(game);

        Assert.assertEquals(5, diffs.size());
        Assert.assertEquals(diffMusk, diffs.get(0));
        Assert.assertEquals(diffBar, diffs.get(1));
        Assert.assertEquals(diffNaeGal, diffs.get(2));
        Assert.assertEquals(diffBat, diffs.get(3));
        Assert.assertEquals(diffVes, diffs.get(4));

        game.setTurn(33);

        diffs = economicService.computeAutomaticTechnologyAdvances(game);

        Assert.assertEquals(7, diffs.size());
        Assert.assertEquals(diffMusk, diffs.get(0));
        Assert.assertEquals(diffBar, diffs.get(1));
        Assert.assertEquals(diffMan, diffs.get(2));
        Assert.assertEquals(diffNaeGal, diffs.get(3));
        Assert.assertEquals(diffBat, diffs.get(4));
        Assert.assertEquals(diffVes, diffs.get(5));
        Assert.assertEquals(diff3d, diffs.get(6));
    }
}
