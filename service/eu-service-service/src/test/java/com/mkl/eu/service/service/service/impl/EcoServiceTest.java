package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.AuthentInfo;
import com.mkl.eu.client.common.vo.GameInfo;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.eco.AddAdminActionRequest;
import com.mkl.eu.client.service.service.eco.RemoveAdminActionRequest;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.tables.*;
import com.mkl.eu.service.service.mapping.GameMapping;
import com.mkl.eu.service.service.mapping.chat.ChatMapping;
import com.mkl.eu.service.service.mapping.diff.DiffMapping;
import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.board.IStackDao;
import com.mkl.eu.service.service.persistence.chat.IChatDao;
import com.mkl.eu.service.service.persistence.country.IPlayableCountryDao;
import com.mkl.eu.service.service.persistence.diff.IDiffDao;
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
import com.mkl.eu.service.service.persistence.oe.eco.AdministrativeActionEntity;
import com.mkl.eu.service.service.persistence.oe.eco.EconomicalSheetEntity;
import com.mkl.eu.service.service.persistence.oe.eco.EstablishmentEntity;
import com.mkl.eu.service.service.persistence.oe.eco.TradeFleetEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.*;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.service.ListEquals;
import com.mkl.eu.service.service.socket.SocketHandler;
import com.mkl.eu.service.service.util.IOEUtil;
import com.mkl.eu.service.service.util.impl.OEUtilImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

/**
 * Test of BoardService.
 *
 * @author MKL.
 */
@RunWith(MockitoJUnitRunner.class)
public class EcoServiceTest {
    @InjectMocks
    private EconomicServiceImpl economicService;

    @Mock
    private IGameDao gameDao;

    @Mock
    private IAdminActionDao adminActionDao;

    @Mock
    private ICounterDao counterDao;

    @Mock
    private IStackDao stackDao;

    @Mock
    private IProvinceDao provinceDao;

    @Mock
    private IEconomicalSheetDao economicalSheetDao;

    @Mock
    private IPlayableCountryDao playableCountryDao;

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
    private SocketHandler socketHandler;

    @Mock
    private IOEUtil oeUtil;

    /** Variable used to store something coming from a mock. */
    private DiffEntity diffEntity;

    /** Variable used to store something coming from a mock. */
    private EconomicalSheetEntity sheetEntity;

    @Test
    public void testComputeSheets() {
        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setTurn(2);
        when(gameDao.lock(12L)).thenReturn(game);

        Diff diffAfter = new Diff();

        when(diffMapping.oeToVo((DiffEntity) anyObject())).thenReturn(diffAfter);

        when(diffDao.create(anyObject())).thenAnswer(invocation -> {
            diffEntity = (DiffEntity) invocation.getArguments()[0];
            return diffEntity;
        });

        DiffResponse response = economicService.computeEconomicalSheets(game.getId().longValue());

        InOrder inOrder = inOrder(gameDao, economicalSheetDao, diffDao, diffMapping, socketHandler);

        inOrder.verify(gameDao).lock(game.getId());
        inOrder.verify(economicalSheetDao).getTradeCenters(game.getId());
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(socketHandler).push(anyObject(), anyObject(), anyObject());
        inOrder.verify(diffMapping).oeToVo((DiffEntity) anyObject());

        Assert.assertNull(diffEntity.getIdObject());
        Assert.assertEquals(DiffTypeEnum.INVALIDATE, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.ECO_SHEET, diffEntity.getTypeObject());
        Assert.assertEquals(game.getId().longValue(), diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(1, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.TURN, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(Integer.toString(game.getTurn()), diffEntity.getAttributes().get(0).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(1, response.getDiffs().size());
        Assert.assertEquals(diffAfter, response.getDiffs().get(0));
    }

    @Test
    public void testComputeSheet1() {
        testComputeSheet("france", 200, true);
    }

    @Test
    public void testComputeSheet2() {
        testComputeSheet("angleterre", 50, false);
    }

    private void testComputeSheet(String name, long tradeIncome, boolean createSheet) {
        Long idGame = 12L;
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setName(name);
        country.setDti(3);
        country.setFti(2);
        country.getEconomicalSheets().add(new EconomicalSheetEntity());
        country.getEconomicalSheets().get(0).setTurn(0);
        country.getEconomicalSheets().add(new EconomicalSheetEntity());
        country.getEconomicalSheets().get(1).setTurn(1);
        if (!createSheet) {
            sheetEntity = new EconomicalSheetEntity();
            sheetEntity.setTurn(2);
            sheetEntity.setTradeCenterLoss(13);
            sheetEntity.setCountry(country);

            country.getEconomicalSheets().add(sheetEntity);
        }
        Map<String, List<CounterFaceTypeEnum>> centers = new HashMap<>();
        centers.put("france", new ArrayList<>());
        centers.get("france").add(CounterFaceTypeEnum.TRADE_CENTER_MEDITERRANEAN);
        centers.get("france").add(CounterFaceTypeEnum.TRADE_CENTER_ATLANTIC);
        centers.put("angleterre", new ArrayList<>());
        centers.get("angleterre").add(CounterFaceTypeEnum.TRADE_CENTER_INDIAN);

        if (createSheet) {
            when(economicalSheetDao.create(anyObject())).thenAnswer(invocation -> {
                sheetEntity = (EconomicalSheetEntity) invocation.getArguments()[0];
                return sheetEntity;
            });
        }
        Map<String, Integer> provinces = new HashMap<>();
        provinces.put("idf", 12);
        provinces.put("lyonnais", 5);
        provinces.put("languedoc", 8);
        when(economicalSheetDao.getOwnedAndControlledProvinces(name, idGame)).thenReturn(provinces);
        List<String> vassals = new ArrayList<>();
        vassals.add("sabaudia");
        vassals.add("alsacia");
        when(counterDao.getVassals(name, idGame)).thenReturn(vassals);
        Map<String, Integer> provincesAlsacia = new HashMap<>();
        provincesAlsacia.put("alsacia", 9);
        when(economicalSheetDao.getOwnedAndControlledProvinces("alsacia", idGame)).thenReturn(provincesAlsacia);
        Map<String, Integer> provincesSabaudia = new HashMap<>();
        provincesSabaudia.put("bresse", 4);
        provincesSabaudia.put("nice", 8);
        when(economicalSheetDao.getOwnedAndControlledProvinces("sabaudia", idGame)).thenReturn(provincesSabaudia);
        List<String> provinceNames = new ArrayList<>();
        provinceNames.add("idf");
        provinceNames.add("lyonnais");
        provinceNames.add("languedoc");
        provinceNames.add("bresse");
        provinceNames.add("nice");
        provinceNames.add("alsacia");
        List<String> pillagedProvinces = new ArrayList<>();
        pillagedProvinces.add("lyonnais");
        pillagedProvinces.add("nice");
        when(economicalSheetDao.getPillagedProvinces(argThat(new ListEquals<>(provinceNames)), eq(idGame))).thenReturn(pillagedProvinces);
        when(economicalSheetDao.getMnuIncome(name, pillagedProvinces, idGame)).thenReturn(60);
        List<String> provincesOwnedNotPilaged = new ArrayList<>();
        provincesOwnedNotPilaged.add("idf");
        provincesOwnedNotPilaged.add("languedoc");
        when(economicalSheetDao.getGoldIncome(argThat(new ListEquals<>(provincesOwnedNotPilaged)), eq(idGame))).thenReturn(
                20);
        when(economicalSheetDao.getFleetLevelIncome(name, idGame)).thenReturn(30);
        when(economicalSheetDao.getFleetLevelMonopoly(name, idGame)).thenReturn(12);
        when(economicalSheetDao.getColTpIncome(name, idGame)).thenReturn(new ImmutablePair<>(22, 18));
        when(economicalSheetDao.getExoResIncome(name, idGame)).thenReturn(8);

        Tables tables = new Tables();
        List<TradeIncome> domTrades = new ArrayList<>();
        TradeIncome trade = new TradeIncome();
        trade.setCountryValue(2);
        trade.setMinValue(40);
        trade.setMaxValue(80);
        trade.setValue(6);
        domTrades.add(trade);
        trade = new TradeIncome();
        trade.setCountryValue(3);
        trade.setMaxValue(39);
        trade.setValue(13);
        domTrades.add(trade);
        trade = new TradeIncome();
        trade.setCountryValue(3);
        trade.setMinValue(40);
        trade.setMaxValue(79);
        trade.setValue(16);
        domTrades.add(trade);
        trade = new TradeIncome();
        trade.setCountryValue(3);
        trade.setMinValue(80);
        trade.setValue(18);
        domTrades.add(trade);
        tables.setDomesticTrades(domTrades);
        List<TradeIncome> forTrades = new ArrayList<>();
        trade = new TradeIncome();
        trade.setCountryValue(1);
        trade.setMaxValue(49);
        trade.setValue(5);
        forTrades.add(trade);
        trade = new TradeIncome();
        trade.setCountryValue(2);
        trade.setMaxValue(49);
        trade.setValue(10);
        forTrades.add(trade);
        trade = new TradeIncome();
        trade.setCountryValue(2);
        trade.setMinValue(50);
        trade.setMaxValue(100);
        trade.setValue(12);
        forTrades.add(trade);
        trade = new TradeIncome();
        trade.setCountryValue(3);
        trade.setMaxValue(49);
        trade.setValue(15);
        forTrades.add(trade);
        tables.setForeignTrades(forTrades);
        EconomicServiceImpl.TABLES = tables;

        economicService.computeEconomicalSheet(country, idGame, 2, centers);

        InOrder inOrder = inOrder(economicalSheetDao, counterDao);

        if (createSheet) {
            inOrder.verify(economicalSheetDao).create(anyObject());
        }
        inOrder.verify(economicalSheetDao).getOwnedAndControlledProvinces(name, idGame);
        inOrder.verify(counterDao).getVassals(name, idGame);
        inOrder.verify(economicalSheetDao).getOwnedAndControlledProvinces("sabaudia", idGame);
        inOrder.verify(economicalSheetDao).getOwnedAndControlledProvinces("alsacia", idGame);
        inOrder.verify(economicalSheetDao).getPillagedProvinces(argThat(new ListEquals<>(provinceNames)), eq(idGame));
        inOrder.verify(economicalSheetDao).getMnuIncome(name, pillagedProvinces, idGame);
        inOrder.verify(economicalSheetDao).getGoldIncome(argThat(new ListEquals<>(provincesOwnedNotPilaged)), eq(idGame));
        inOrder.verify(economicalSheetDao).getFleetLevelIncome(name, idGame);
        inOrder.verify(economicalSheetDao).getFleetLevelMonopoly(name, idGame);
        inOrder.verify(economicalSheetDao).getColTpIncome(name, idGame);
        inOrder.verify(economicalSheetDao).getExoResIncome(name, idGame);

        Assert.assertEquals(3, country.getEconomicalSheets().size());
        Assert.assertEquals(sheetEntity, country.getEconomicalSheets().get(2));
        Assert.assertEquals(country, sheetEntity.getCountry());
        Assert.assertEquals(2, sheetEntity.getTurn().intValue());
        Assert.assertEquals(25, sheetEntity.getProvincesIncome().longValue());
        Assert.assertEquals(21, sheetEntity.getVassalIncome().longValue());
        Assert.assertEquals(13, sheetEntity.getPillages().longValue());
        Assert.assertEquals(33, sheetEntity.getLandIncome().longValue());
        Assert.assertEquals(60, sheetEntity.getMnuIncome().longValue());
        Assert.assertEquals(20, sheetEntity.getGoldIncome().longValue());
        Assert.assertEquals(80, sheetEntity.getIndustrialIncome().longValue());
        Assert.assertEquals(16, sheetEntity.getDomTradeIncome().longValue());
        Assert.assertEquals(10, sheetEntity.getForTradeIncome().longValue());
        Assert.assertEquals(30, sheetEntity.getFleetLevelIncome().longValue());
        Assert.assertEquals(12, sheetEntity.getFleetMonopIncome().longValue());
        Assert.assertEquals(tradeIncome, sheetEntity.getTradeCenterIncome().longValue());
        if (createSheet) {
            Assert.assertNull(sheetEntity.getTradeCenterLoss());
            Assert.assertEquals(tradeIncome + 68, sheetEntity.getTradeIncome().longValue());
            Assert.assertEquals(tradeIncome + 229, sheetEntity.getIncome().longValue());
            Assert.assertEquals(tradeIncome + 229, sheetEntity.getGrossIncome().longValue());
        } else {
            Assert.assertEquals(13, sheetEntity.getTradeCenterLoss().longValue());
            Assert.assertEquals(tradeIncome + 55, sheetEntity.getTradeIncome().longValue());
            Assert.assertEquals(tradeIncome + 216, sheetEntity.getIncome().longValue());
            Assert.assertEquals(tradeIncome + 216, sheetEntity.getGrossIncome().longValue());
        }
        Assert.assertEquals(22, sheetEntity.getColIncome().longValue());
        Assert.assertEquals(18, sheetEntity.getTpIncome().longValue());
        Assert.assertEquals(8, sheetEntity.getExoResIncome().longValue());
        Assert.assertEquals(48, sheetEntity.getRotwIncome().longValue());
    }

    @Test
    public void testAddAdmActFailSimple() {
        try {
            economicService.addAdminAction(null);
            Assert.fail("Should break because addAdminAction is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction", e.getParams()[0]);
        }

        Request<AddAdminActionRequest> request = new Request<>();

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because addAdminAction.authent is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.authent", e.getParams()[0]);
        }

        request.setAuthent(new AuthentInfo());

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because addAdminAction.game is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.game", e.getParams()[0]);
        }

        request.setGame(new GameInfo());

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because idGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.game.idGame", e.getParams()[0]);
        }

        request.getGame().setIdGame(12L);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because versionGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.game.versionGame", e.getParams()[0]);
        }

        request.getGame().setVersionGame(1L);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because game does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.game.idGame", e.getParams()[0]);
        }

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setTurn(1);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setName("france");

        when(gameDao.lock(12L)).thenReturn(game);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because versions does not match");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.game.versionGame", e.getParams()[0]);
        }

        game.setVersion(5L);

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
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(12L);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setTurn(1);
        game.setVersion(5L);
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

        when(gameDao.lock(12L)).thenReturn(game);

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
        when(adminActionDao.findAdminActions(12L, 1, 4L, AdminActionTypeEnum.LM, AdminActionTypeEnum.DIS,
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
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.getRequest().setIdCountry(14L);
        request.getRequest().setType(AdminActionTypeEnum.LM);
        request.getRequest().setIdObject(4L);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);
        game.setTurn(2);
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

        when(gameDao.lock(12L)).thenReturn(game);

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenAnswer(invocation -> {
            diffEntity = ((List<DiffEntity>) invocation.getArguments()[0]).get(2);
            return diffAfter;
        });

        DiffResponse response = economicService.addAdminAction(request);

        InOrder inOrder = inOrder(gameDao, adminActionDao, diffDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(adminActionDao).findAdminActions(request.getRequest().getIdCountry(), game.getTurn(),
                                                        request.getRequest().getIdObject(), AdminActionTypeEnum.LM,
                                                        AdminActionTypeEnum.DIS, AdminActionTypeEnum.LF);
        inOrder.verify(adminActionDao).create(anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.ADD, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.ADM_ACT, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(4, diffEntity.getAttributes().size());
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

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
    }

    @Test
    public void testAddAdmActLFSuccess() throws Exception {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setRequest(new AddAdminActionRequest());
        request.setAuthent(new AuthentInfo());
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.getRequest().setIdCountry(14L);
        request.getRequest().setType(AdminActionTypeEnum.LF);
        request.getRequest().setIdObject(5L);
        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.FORTRESS_3);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);
        game.setTurn(2);
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

        when(gameDao.lock(12L)).thenReturn(game);

        EuropeanProvinceEntity corn = new EuropeanProvinceEntity();
        corn.setName("idf");
        corn.setFortress(2);
        when(provinceDao.getProvinceByName("idf")).thenReturn(corn);

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenAnswer(invocation -> {
            diffEntity = ((List<DiffEntity>) invocation.getArguments()[0]).get(2);
            return diffAfter;
        });

        DiffResponse response = economicService.addAdminAction(request);

        InOrder inOrder = inOrder(gameDao, provinceDao, adminActionDao, diffDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(provinceDao).getProvinceByName("idf");
        inOrder.verify(adminActionDao).findAdminActions(request.getRequest().getIdCountry(), game.getTurn(),
                                                        request.getRequest().getIdObject(), AdminActionTypeEnum.LM,
                                                        AdminActionTypeEnum.DIS, AdminActionTypeEnum.LF);
        inOrder.verify(adminActionDao).create(anyObject());
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
        Assert.assertEquals(DiffAttributeTypeEnum.ID_OBJECT, diffEntity.getAttributes().get(3).getType());
        Assert.assertEquals(request.getRequest().getIdObject().toString(),
                            diffEntity.getAttributes().get(3).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COUNTER_FACE_TYPE, diffEntity.getAttributes().get(4).getType());
        Assert.assertEquals(request.getRequest().getCounterFaceType().toString(),
                            diffEntity.getAttributes().get(4).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
    }

    @Test
    public void testAddAdmActDISSuccess() throws Exception {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setRequest(new AddAdminActionRequest());
        request.setAuthent(new AuthentInfo());
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.getRequest().setIdCountry(14L);
        request.getRequest().setType(AdminActionTypeEnum.DIS);
        request.getRequest().setIdObject(4L);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);
        game.setTurn(2);
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

        when(gameDao.lock(12L)).thenReturn(game);

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenAnswer(invocation -> {
            diffEntity = ((List<DiffEntity>) invocation.getArguments()[0]).get(2);
            return diffAfter;
        });

        DiffResponse response = economicService.addAdminAction(request);

        InOrder inOrder = inOrder(gameDao, adminActionDao, diffDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(adminActionDao).findAdminActions(request.getRequest().getIdCountry(), game.getTurn(),
                                                        request.getRequest().getIdObject(), AdminActionTypeEnum.LM,
                                                        AdminActionTypeEnum.DIS, AdminActionTypeEnum.LF);
        inOrder.verify(adminActionDao).create(anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.ADD, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.ADM_ACT, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(4, diffEntity.getAttributes().size());
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

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
    }

    @Test
    public void testAddAdmActPuFail() {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(12L);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setTurn(1);
        game.setVersion(5L);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setLandTech(Tech.MEDIEVAL);

        when(gameDao.lock(12L)).thenReturn(game);

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

        when(stackDao.getStacksOnProvince("controlledNotOwn", 12L)).thenReturn(Arrays.asList(stackOwnFor, stackCtrl));

        EuropeanProvinceEntity ownedNotControlled = new EuropeanProvinceEntity();
        ownedNotControlled.setName("ownedNotControlled");
        when(provinceDao.getProvinceByName("ownedNotControlled")).thenReturn(ownedNotControlled);

        when(stackDao.getStacksOnProvince("ownedNotControlled", 12L)).thenReturn(Arrays.asList(stackOwn, stackCtrlFor));

        EuropeanProvinceEntity owned = new EuropeanProvinceEntity();
        owned.setName("owned");
        when(provinceDao.getProvinceByName("owned")).thenReturn(owned);

        when(stackDao.getStacksOnProvince("owned", 12L)).thenReturn(Collections.singletonList(stackOwn));

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
        when(stackDao.getStacksOnProvince("corn", 12L)).thenReturn(Collections.singletonList(stackFortress));

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
        tables.setLimits(limits);
        List<Tech> techs = new ArrayList<>();
        Tech tech = new Tech();
        tech.setName(Tech.MEDIEVAL);
        tech.setBeginTurn(1);
        techs.add(tech);
        tech = new Tech();
        tech.setName(Tech.RENAISSANCE);
        tech.setBeginTurn(11);
        techs.add(tech);
        tables.setTechs(techs);
        EconomicServiceImpl.TABLES = tables;

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
        when(adminActionDao.findAdminActions(12l, 1, null, AdminActionTypeEnum.PU)).thenReturn(actions);
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
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(12L);
        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.ARMY_MINUS);
        request.getRequest().setProvince("corn");

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setTurn(1);
        game.setVersion(5L);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setLandTech("MEDIEVAL");

        when(gameDao.lock(12L)).thenReturn(game);

        request.getRequest().setType(AdminActionTypeEnum.PU);

        StackEntity stackOwn = new StackEntity();
        CounterEntity counterOwn = new CounterEntity();
        counterOwn.setType(CounterFaceTypeEnum.OWN);
        counterOwn.setCountry("france");
        stackOwn.getCounters().add(counterOwn);

        when(stackDao.getStacksOnProvince("corn", 12L)).thenReturn(Collections.singletonList(stackOwn));

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
        tables.setUnits(units);
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
        tables.setLimits(limits);
        EconomicServiceImpl.TABLES = tables;

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenAnswer(invocation -> {
            diffEntity = ((List<DiffEntity>) invocation.getArguments()[0]).get(2);
            return diffAfter;
        });

        DiffResponse response = economicService.addAdminAction(request);

        InOrder inOrder = inOrder(gameDao, provinceDao, stackDao, adminActionDao, diffDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(provinceDao).getProvinceByName("corn");
        inOrder.verify(stackDao).getStacksOnProvince("corn", 12L);
        inOrder.verify(adminActionDao).findAdminActions(12L, 1, null, AdminActionTypeEnum.PU);
        inOrder.verify(adminActionDao).create(anyObject());
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
        Assert.assertEquals(diffAfter, response.getDiffs());
    }

    @Test
    public void testAddAdmActPuFortressSuccess() throws FunctionalException {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(12L);
        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.FORTRESS_3);
        request.getRequest().setProvince("corn");

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setTurn(1);
        game.setVersion(5L);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setLandTech("RENAISSANCE");

        when(gameDao.lock(12L)).thenReturn(game);

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

        when(stackDao.getStacksOnProvince("corn", 12L)).thenReturn(Arrays.asList(stackOwn, stackCtrl, stackFortress));

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
        tables.setUnits(units);
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
        tables.setLimits(limits);
        List<Tech> techs = new ArrayList<>();
        Tech tech = new Tech();
        tech.setName("RENAISSANCE");
        tech.setBeginTurn(1);
        techs.add(tech);
        tech = new Tech();
        tech.setName(Tech.ARQUEBUS);
        tech.setBeginTurn(11);
        techs.add(tech);
        tables.setTechs(techs);
        EconomicServiceImpl.TABLES = tables;

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenAnswer(invocation -> {
            diffEntity = ((List<DiffEntity>) invocation.getArguments()[0]).get(2);
            return diffAfter;
        });

        DiffResponse response = economicService.addAdminAction(request);

        InOrder inOrder = inOrder(gameDao, provinceDao, stackDao, adminActionDao, diffDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(provinceDao).getProvinceByName("corn");
        inOrder.verify(stackDao).getStacksOnProvince("corn", 12L);
        inOrder.verify(adminActionDao).create(anyObject());
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
        Assert.assertEquals(diffAfter, response.getDiffs());
    }

    @Test
    public void testRemoveAdmActFailSimple() {
        try {
            economicService.removeAdminAction(null);
            Assert.fail("Should break because removeAdminAction is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("removeAdminAction", e.getParams()[0]);
        }

        Request<RemoveAdminActionRequest> request = new Request<>();

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
            Assert.fail("Should break because removeAdminAction.game is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("removeAdminAction.game", e.getParams()[0]);
        }

        request.setGame(new GameInfo());

        try {
            economicService.removeAdminAction(request);
            Assert.fail("Should break because idGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("removeAdminAction.game.idGame", e.getParams()[0]);
        }

        request.getGame().setIdGame(12L);

        try {
            economicService.removeAdminAction(request);
            Assert.fail("Should break because versionGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("removeAdminAction.game.versionGame", e.getParams()[0]);
        }

        request.getGame().setVersionGame(1L);

        try {
            economicService.removeAdminAction(request);
            Assert.fail("Should break because game does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("removeAdminAction.game.idGame", e.getParams()[0]);
        }

        GameEntity game = new GameEntity();
        game.setId(12L);

        when(gameDao.lock(12L)).thenReturn(game);

        try {
            economicService.removeAdminAction(request);
            Assert.fail("Should break because versions does not match");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("removeAdminAction.game.versionGame", e.getParams()[0]);
        }

        game.setVersion(5L);

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
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);

        request.setRequest(new RemoveAdminActionRequest());

        request.getRequest().setIdAdmAct(13L);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);

        when(gameDao.lock(12L)).thenReturn(game);

        AdministrativeActionEntity action13 = new AdministrativeActionEntity();
        action13.setId(13L);
        action13.setCountry(new PlayableCountryEntity());
        action13.getCountry().setId(666L);
        action13.getCountry().setUsername("MKL");
        action13.setType(AdminActionTypeEnum.DIS);
        action13.setStatus(AdminActionStatusEnum.PLANNED);

        when(adminActionDao.load(13L)).thenReturn(action13);

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenAnswer(invocation -> {
            diffEntity = ((List<DiffEntity>) invocation.getArguments()[0]).get(2);
            return diffAfter;
        });

        DiffResponse response = economicService.removeAdminAction(request);

        InOrder inOrder = inOrder(gameDao, adminActionDao, diffDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
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
        Assert.assertEquals(diffAfter, response.getDiffs());
    }

    @Test
    public void testAddAdmActTfiFail() {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(11L);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setTurn(1);
        game.setVersion(5L);
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

        when(gameDao.lock(12L)).thenReturn(game);

        List<AdministrativeActionEntity> actionsFor11 = new ArrayList<>();
        AdministrativeActionEntity action = new AdministrativeActionEntity();
        actionsFor11.add(action);
        actionsFor11.add(action);
        when(adminActionDao.findAdminActions(11L, 1, null, AdminActionTypeEnum.TFI)).thenReturn(actionsFor11);

        List<AdministrativeActionEntity> actionsFor12 = new ArrayList<>();
        actionsFor12.add(action);
        when(adminActionDao.findAdminActions(12L, 1, null, AdminActionTypeEnum.TFI)).thenReturn(actionsFor12);

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
        tables.setLimits(limits);
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
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(12L);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setTurn(1);
        game.setVersion(5L);
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

        when(gameDao.lock(12L)).thenReturn(game);

        List<AdministrativeActionEntity> actionsFor11 = new ArrayList<>();
        AdministrativeActionEntity otherAction = new AdministrativeActionEntity();
        actionsFor11.add(otherAction);
        actionsFor11.add(otherAction);
        when(adminActionDao.findAdminActions(11L, 1, null, AdminActionTypeEnum.TFI)).thenReturn(actionsFor11);

        List<AdministrativeActionEntity> actionsFor12 = new ArrayList<>();
        actionsFor12.add(otherAction);
        when(adminActionDao.findAdminActions(12L, 1, null, AdminActionTypeEnum.TFI)).thenReturn(actionsFor12);

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
        tables.setLimits(limits);
        EconomicServiceImpl.TABLES = tables;

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        when(diffMapping.oesToVos(anyObject())).thenAnswer(invocation -> {
            diffEntity = ((List<DiffEntity>) invocation.getArguments()[0]).get(2);
            return diffAfter;
        });

        request.getRequest().setInvestment(investment);
        request.getRequest().setProvince(province);

        DiffResponse response = economicService.addAdminAction(request);

        InOrder inOrder = inOrder(gameDao, provinceDao, stackDao, adminActionDao, diffDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(adminActionDao).findAdminActions(12L, 1, null, AdminActionTypeEnum.TFI);
        inOrder.verify(provinceDao).getProvinceByName(province);
        inOrder.verify(adminActionDao).create(anyObject());
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
        Assert.assertEquals(diffAfter, response.getDiffs());
    }

    @Test
    public void testAddAdmActMnuFail() {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(11L);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setTurn(1);
        game.setVersion(5L);
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

        when(gameDao.lock(12L)).thenReturn(game);

        Map<String, Integer> ownedProvinces = new HashMap<>();
        ownedProvinces.put("provence", 5);
        ownedProvinces.put("lyonnais", 8);

        when(economicalSheetDao.getOwnedAndControlledProvinces("france", 12L)).thenReturn(ownedProvinces);

        List<AdministrativeActionEntity> actionsFor11 = new ArrayList<>();
        AdministrativeActionEntity action = new AdministrativeActionEntity();
        actionsFor11.add(action);
        when(adminActionDao.findAdminActions(11L, 1, null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI,
                                             AdminActionTypeEnum.DTI, AdminActionTypeEnum.EXL))
                .thenReturn(actionsFor11);

        List<AdministrativeActionEntity> actionsFor12 = new ArrayList<>();
        when(adminActionDao.findAdminActions(12L, 1, null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI,
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

        when(stackDao.getStacksOnProvince("controlledNotOwn", 12L)).thenReturn(Arrays.asList(stackOwnFor, stackCtrl));

        EuropeanProvinceEntity ownedNotControlled = new EuropeanProvinceEntity();
        ownedNotControlled.setName("ownedNotControlled");
        when(provinceDao.getProvinceByName("ownedNotControlled")).thenReturn(ownedNotControlled);

        when(stackDao.getStacksOnProvince("ownedNotControlled", 12L)).thenReturn(Arrays.asList(stackOwn, stackCtrlFor));

        EuropeanProvinceEntity owned = new EuropeanProvinceEntity();
        owned.setName("owned");
        when(provinceDao.getProvinceByName("owned")).thenReturn(owned);

        when(stackDao.getStacksOnProvince("owned", 12L)).thenReturn(Collections.singletonList(stackOwn));

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
        tables.setLimits(limits);
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
            Assert.assertEquals(IConstantsServiceException.ADMIN_ACTION_LIMIT_EXCEED, e.getCode());
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
                .thenReturn(new HashMap<String, Integer>());

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
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(idCountry);
        request.getRequest().setInvestment(investment);
        request.getRequest().setType(AdminActionTypeEnum.MNU);
        request.getRequest().setCounterFaceType(CounterFaceTypeEnum.MNU_METAL_MINUS);
        request.getRequest().setProvince("idf");

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setTurn(turn);
        game.setVersion(5L);
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

        when(gameDao.lock(12L)).thenReturn(game);

        when(adminActionDao.findAdminActions(idCountry, turn, null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI,
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
        tables.setLimits(limits);
        EconomicServiceImpl.TABLES = tables;

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        when(diffMapping.oesToVos(anyObject())).thenAnswer(invocation -> {
            diffEntity = ((List<DiffEntity>) invocation.getArguments()[0]).get(2);
            return diffAfter;
        });

        DiffResponse response = economicService.addAdminAction(request);

        InOrder inOrder = inOrder(gameDao, provinceDao, stackDao, adminActionDao, diffDao, diffMapping, oeUtil);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(adminActionDao)
               .findAdminActions(idCountry, turn, null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI,
                                 AdminActionTypeEnum.DTI, AdminActionTypeEnum.EXL);
        inOrder.verify(provinceDao).getProvinceByName("idf");
        inOrder.verify(oeUtil).getAdministrativeValue(countryEntity);
        inOrder.verify(oeUtil).getStability(game, country);
        inOrder.verify(adminActionDao).create(anyObject());
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
        Assert.assertEquals(cost, diffEntity.getAttributes().get(3).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_OBJECT, diffEntity.getAttributes().get(4).getType());
        Assert.assertEquals("3", diffEntity.getAttributes().get(4).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diffEntity.getAttributes().get(5).getType());
        Assert.assertEquals(request.getRequest().getProvince(), diffEntity.getAttributes().get(5).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COLUMN, diffEntity.getAttributes().get(6).getType());
        Assert.assertEquals(column, diffEntity.getAttributes().get(6).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.BONUS, diffEntity.getAttributes().get(7).getType());
        Assert.assertEquals(bonus, diffEntity.getAttributes().get(7).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
    }

    @Test
    public void testAddAdmActDtiFtiFail() {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(11L);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setTurn(1);
        game.setVersion(5L);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setDti(3);
        game.getCountries().get(0).setFti(4);
        game.getCountries().get(0).setFtiRotw(5);
        game.getCountries().get(0).setName("france");
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(11L);
        game.getCountries().get(1).setName("angleterre");

        when(gameDao.lock(12L)).thenReturn(game);

        List<AdministrativeActionEntity> actionsFor11 = new ArrayList<>();
        AdministrativeActionEntity action = new AdministrativeActionEntity();
        actionsFor11.add(action);
        when(adminActionDao.findAdminActions(11L, 1, null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI,
                                             AdminActionTypeEnum.DTI, AdminActionTypeEnum.EXL))
                .thenReturn(actionsFor11);

        List<AdministrativeActionEntity> actionsFor12 = new ArrayList<>();
        when(adminActionDao.findAdminActions(12L, 1, null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI,
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
        tables.setLimits(limits);
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
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(12L);
        request.getRequest().setInvestment(InvestmentEnum.M);
        request.getRequest().setType(AdminActionTypeEnum.FTI);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setTurn(1);
        game.setVersion(5L);
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

        when(gameDao.lock(12L)).thenReturn(game);

        List<AdministrativeActionEntity> actionsFor12 = new ArrayList<>();
        when(adminActionDao.findAdminActions(12L, 1, null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI,
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
        tables.setLimits(limits);
        EconomicServiceImpl.TABLES = tables;

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        when(diffMapping.oesToVos(anyObject())).thenAnswer(invocation -> {
            diffEntity = ((List<DiffEntity>) invocation.getArguments()[0]).get(2);
            return diffAfter;
        });

        DiffResponse response = economicService.addAdminAction(request);

        InOrder inOrder = inOrder(gameDao, adminActionDao, diffDao, diffMapping, oeUtil);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(adminActionDao).findAdminActions(12L, 1, null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI,
                                                        AdminActionTypeEnum.DTI, AdminActionTypeEnum.EXL);
        inOrder.verify(oeUtil).getAdministrativeValue(game.getCountries().get(0));
        inOrder.verify(oeUtil).getStability(game, "france");
        inOrder.verify(adminActionDao).create(anyObject());
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
        Assert.assertEquals(diffAfter, response.getDiffs());
    }

    @Test
    public void testAddAdmActExcTaxesFail() {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(11L);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setTurn(1);
        game.setVersion(5L);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setDti(3);
        game.getCountries().get(0).setFti(4);
        game.getCountries().get(0).setFtiRotw(5);
        game.getCountries().get(0).setName("france");
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(11L);
        game.getCountries().get(1).setName("angleterre");

        when(gameDao.lock(12L)).thenReturn(game);

        List<AdministrativeActionEntity> actionsFor11 = new ArrayList<>();
        AdministrativeActionEntity action = new AdministrativeActionEntity();
        actionsFor11.add(action);
        when(adminActionDao.findAdminActions(11L, 1, null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI,
                                             AdminActionTypeEnum.DTI, AdminActionTypeEnum.EXL))
                .thenReturn(actionsFor11);

        List<AdministrativeActionEntity> actionsFor12 = new ArrayList<>();
        when(adminActionDao.findAdminActions(12L, 1, null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI,
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

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because stab is -3");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.INSUFICIENT_STABILITY, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }
    }

    @Test
    public void testAddAdmActExcTaxesSuccess() throws FunctionalException {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(12L);
        request.getRequest().setType(AdminActionTypeEnum.EXL);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setTurn(1);
        game.setVersion(5L);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setName("france");

        when(gameDao.lock(12L)).thenReturn(game);

        List<AdministrativeActionEntity> actionsFor12 = new ArrayList<>();
        when(adminActionDao.findAdminActions(12L, 1, null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI,
                                             AdminActionTypeEnum.DTI, AdminActionTypeEnum.EXL))
                .thenReturn(actionsFor12);

        when(oeUtil.getStability(game, "france")).thenReturn(2);
        when(oeUtil.getAdministrativeValue(game.getCountries().get(0))).thenReturn(7);

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        when(diffMapping.oesToVos(anyObject())).thenAnswer(invocation -> {
            diffEntity = ((List<DiffEntity>) invocation.getArguments()[0]).get(2);
            return diffAfter;
        });

        DiffResponse response = economicService.addAdminAction(request);

        InOrder inOrder = inOrder(gameDao, adminActionDao, diffDao, diffMapping, oeUtil);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(adminActionDao).findAdminActions(12L, 1, null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI,
                                                        AdminActionTypeEnum.DTI, AdminActionTypeEnum.EXL);
        inOrder.verify(oeUtil).getStability(game, "france");
        inOrder.verify(oeUtil).getAdministrativeValue(game.getCountries().get(0));
        inOrder.verify(adminActionDao).create(anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.ADD, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.ADM_ACT, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(4, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_COUNTRY, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(request.getRequest().getIdCountry().toString(),
                            diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TURN, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals(game.getTurn().toString(), diffEntity.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diffEntity.getAttributes().get(2).getType());
        Assert.assertEquals(request.getRequest().getType().name(), diffEntity.getAttributes().get(2).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.BONUS, diffEntity.getAttributes().get(3).getType());
        Assert.assertEquals("10", diffEntity.getAttributes().get(3).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
    }

    @Test
    public void testAddAdmActColFail() {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(11L);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setTurn(1);
        game.setVersion(5L);
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
        tables.setLimits(limits);
        List<Period> periods = new ArrayList<>();
        Period period = new Period();
        period.setName(Period.PERIOD_I);
        period.setBegin(1);
        period.setEnd(6);
        periods.add(period);
        tables.setPeriods(periods);
        EconomicServiceImpl.TABLES = tables;

        when(gameDao.lock(12L)).thenReturn(game);

        List<AdministrativeActionEntity> actionsFor11 = new ArrayList<>();
        AdministrativeActionEntity action = new AdministrativeActionEntity();
        actionsFor11.add(action);
        actionsFor11.add(action);
        when(adminActionDao.findAdminActions(11L, 1, null, AdminActionTypeEnum.COL)).thenReturn(actionsFor11);

        List<AdministrativeActionEntity> actionsFor12 = new ArrayList<>();
        actionsFor12.add(action);
        when(adminActionDao.findAdminActions(12L, 1, null, AdminActionTypeEnum.COL)).thenReturn(actionsFor12);

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
    public void testAddAdmActColSuccess() throws FunctionalException {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(12L);
        request.getRequest().setInvestment(InvestmentEnum.M);
        request.getRequest().setProvince("quebec");
        request.getRequest().setType(AdminActionTypeEnum.COL);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setTurn(1);
        game.setVersion(5L);
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
        game.getOtherForces().add(new OtherForcesEntity());
        game.getOtherForces().get(0).setProvince("quebec");
        game.getOtherForces().get(0).setType(OtherForcesTypeEnum.NATIVES);

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
        tables.setLimits(limits);
        List<Period> periods = new ArrayList<>();
        Period period = new Period();
        period.setName(Period.PERIOD_I);
        period.setBegin(1);
        period.setEnd(6);
        periods.add(period);
        tables.setPeriods(periods);
        EconomicServiceImpl.TABLES = tables;

        when(gameDao.lock(12L)).thenReturn(game);

        List<AdministrativeActionEntity> actionsFor12 = new ArrayList<>();
        actionsFor12.add(new AdministrativeActionEntity());
        when(adminActionDao.findAdminActions(12L, 1, null, AdminActionTypeEnum.COL)).thenReturn(actionsFor12);

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


        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        when(diffMapping.oesToVos(anyObject())).thenAnswer(invocation -> {
            diffEntity = ((List<DiffEntity>) invocation.getArguments()[0]).get(2);
            return diffAfter;
        });

        DiffResponse response = economicService.addAdminAction(request);

        InOrder inOrder = inOrder(gameDao, adminActionDao, provinceDao, playableCountryDao, diffDao, diffMapping,
                                  oeUtil);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(adminActionDao).findAdminActions(12L, 1, null, AdminActionTypeEnum.COL);
        inOrder.verify(provinceDao).getProvinceByName("quebec");
        inOrder.verify(provinceDao).getGoldInProvince("quebec");
        inOrder.verify(playableCountryDao).getOwnedProvinces("france", 12L);
        inOrder.verify(oeUtil).canSettle(quebec, discoveries, sources, new ArrayList<>());
        inOrder.verify(adminActionDao).getCountriesInlandAdvance("quebec", 12L);
        inOrder.verify(provinceDao).getRegionByName("Canada");
        inOrder.verify(adminActionDao).create(anyObject());
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
        Assert.assertEquals("2", diffEntity.getAttributes().get(7).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
    }

    @Test
    public void testAddAdmActTpFail() {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(11L);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setTurn(1);
        game.setVersion(5L);
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
        tables.setLimits(limits);
        EconomicServiceImpl.TABLES = tables;

        when(gameDao.lock(12L)).thenReturn(game);

        List<AdministrativeActionEntity> actionsFor11 = new ArrayList<>();
        AdministrativeActionEntity action = new AdministrativeActionEntity();
        actionsFor11.add(action);
        actionsFor11.add(action);
        when(adminActionDao.findAdminActions(11L, 1, null, AdminActionTypeEnum.TP)).thenReturn(actionsFor11);

        List<AdministrativeActionEntity> actionsFor12 = new ArrayList<>();
        actionsFor12.add(action);
        when(adminActionDao.findAdminActions(12L, 1, null, AdminActionTypeEnum.TP)).thenReturn(actionsFor12);

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
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.setRequest(new AddAdminActionRequest());
        request.getRequest().setIdCountry(12L);
        request.getRequest().setInvestment(InvestmentEnum.M);
        request.getRequest().setProvince("quebec");
        request.getRequest().setType(AdminActionTypeEnum.TP);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setTurn(1);
        game.setVersion(5L);
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
        game.getStacks().get(1).getCounters().get(0).setEstablishment(establishment);
        game.getOtherForces().add(new OtherForcesEntity());
        game.getOtherForces().get(0).setProvince("quebec");
        game.getOtherForces().get(0).setType(OtherForcesTypeEnum.NATIVES);

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
        tables.setLimits(limits);
        EconomicServiceImpl.TABLES = tables;

        when(gameDao.lock(12L)).thenReturn(game);

        List<AdministrativeActionEntity> actionsFor12 = new ArrayList<>();
        actionsFor12.add(new AdministrativeActionEntity());
        when(adminActionDao.findAdminActions(12L, 1, null, AdminActionTypeEnum.COL)).thenReturn(actionsFor12);

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


        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        when(diffMapping.oesToVos(anyObject())).thenAnswer(invocation -> {
            diffEntity = ((List<DiffEntity>) invocation.getArguments()[0]).get(2);
            return diffAfter;
        });

        DiffResponse response = economicService.addAdminAction(request);

        InOrder inOrder = inOrder(gameDao, adminActionDao, provinceDao, playableCountryDao, diffDao, diffMapping,
                                  oeUtil);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(adminActionDao).findAdminActions(12L, 1, null, AdminActionTypeEnum.TP);
        inOrder.verify(provinceDao).getProvinceByName("quebec");
        inOrder.verify(playableCountryDao).getOwnedProvinces("france", 12L);
        inOrder.verify(oeUtil).canSettle(quebec, discoveries, sources, forts);
        inOrder.verify(provinceDao).getRegionByName("Canada");
        inOrder.verify(adminActionDao).countOtherTpsInRegion("france", "Canada", 12L);
        inOrder.verify(adminActionDao).create(anyObject());
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
        Assert.assertEquals("-2", diffEntity.getAttributes().get(7).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
    }
}
