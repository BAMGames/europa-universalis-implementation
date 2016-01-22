package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.common.vo.SimpleRequest;
import com.mkl.eu.client.service.service.IEconomicService;
import com.mkl.eu.client.service.service.eco.AddAdminActionRequest;
import com.mkl.eu.client.service.service.eco.EconomicalSheetCountry;
import com.mkl.eu.client.service.service.eco.LoadEcoSheetsRequest;
import com.mkl.eu.client.service.service.eco.RemoveAdminActionRequest;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.service.service.mapping.eco.EconomicalSheetMapping;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.diff.IDiffDao;
import com.mkl.eu.service.service.persistence.eco.IAdminActionDao;
import com.mkl.eu.service.service.persistence.eco.IEconomicalSheetDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.eco.AdministrativeActionEntity;
import com.mkl.eu.service.service.persistence.oe.eco.EconomicalSheetEntity;
import com.mkl.eu.service.service.persistence.tables.ITablesDao;
import com.mkl.eu.service.service.service.GameDiffsInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of the Economic Service.
 *
 * @author MKL.
 */
@Service
@Transactional(rollbackFor = {TechnicalException.class, FunctionalException.class})
public class EconomicServiceImpl extends AbstractService implements IEconomicService {
    /** Counter Face Type for armies. */
    private static final List<CounterFaceTypeEnum> ARMY_TYPES = new ArrayList<>();
    /** Counter Face Type for land armies. */
    private static final List<CounterFaceTypeEnum> ARMY_LAND_TYPES = new ArrayList<>();
    /** EconomicalSheet DAO. */
    @Autowired
    private IEconomicalSheetDao economicalSheetDao;
    /** AdminAction DAO. */
    @Autowired
    private IAdminActionDao adminActionDao;
    /** Tables DAO. */
    @Autowired
    private ITablesDao tablesDao;
    /** Counter DAO. */
    @Autowired
    private ICounterDao counterDao;
    /** Diff DAO. */
    @Autowired
    private IDiffDao diffDao;
    /** Game mapping. */
    @Autowired
    private EconomicalSheetMapping ecoSheetsMapping;

    /**
     * Filling the static List.
     */
    static {
        ARMY_TYPES.add(CounterFaceTypeEnum.ARMY_PLUS);
        ARMY_TYPES.add(CounterFaceTypeEnum.ARMY_MINUS);
        ARMY_TYPES.add(CounterFaceTypeEnum.LAND_DETACHMENT);
        ARMY_TYPES.add(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION);
        ARMY_TYPES.add(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR);
        ARMY_TYPES.add(CounterFaceTypeEnum.LAND_DETACHMENT_KOZAK);
        ARMY_TYPES.add(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION_KOZAK);
        ARMY_TYPES.add(CounterFaceTypeEnum.FLEET_PLUS);
        ARMY_TYPES.add(CounterFaceTypeEnum.FLEET_MINUS);
        ARMY_TYPES.add(CounterFaceTypeEnum.NAVAL_DETACHMENT);
        ARMY_TYPES.add(CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION);
        ARMY_TYPES.add(CounterFaceTypeEnum.NAVAL_GALLEY);
        ARMY_TYPES.add(CounterFaceTypeEnum.NAVAL_TRANSPORT);

        ARMY_LAND_TYPES.add(CounterFaceTypeEnum.ARMY_PLUS);
        ARMY_LAND_TYPES.add(CounterFaceTypeEnum.ARMY_MINUS);
        ARMY_LAND_TYPES.add(CounterFaceTypeEnum.LAND_DETACHMENT);
        ARMY_LAND_TYPES.add(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION);
        ARMY_LAND_TYPES.add(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR);
        ARMY_LAND_TYPES.add(CounterFaceTypeEnum.LAND_DETACHMENT_KOZAK);
        ARMY_LAND_TYPES.add(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION_KOZAK);
    }

    /** {@inheritDoc} */
    @Override
    public List<EconomicalSheetCountry> loadEconomicSheets(SimpleRequest<LoadEcoSheetsRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_LOAD_ECO_SHEETS).setParams(METHOD_LOAD_ECO_SHEETS));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_LOAD_ECO_SHEETS, PARAMETER_REQUEST).setParams(METHOD_LOAD_ECO_SHEETS));

        List<EconomicalSheetEntity> sheetEntities = economicalSheetDao.loadSheets(
                request.getRequest().getIdCountry(),
                request.getRequest().getTurn(),
                request.getRequest().getIdGame());

        return ecoSheetsMapping.oesToVosCountry(sheetEntities);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse computeEconomicalSheets(Long idGame) {
        GameEntity game = gameDao.lock(idGame);

        Map<String, List<CounterFaceTypeEnum>> tradeCenters = economicalSheetDao.getTradeCenters(game.getId());

        for (PlayableCountryEntity country : game.getCountries()) {
            computeEconomicalSheet(country, game, tradeCenters);
        }

        DiffEntity diff = new DiffEntity();
        diff.setIdGame(game.getId());
        diff.setVersionGame(game.getVersion());
        diff.setType(DiffTypeEnum.INVALIDATE);
        diff.setTypeObject(DiffTypeObjectEnum.ECO_SHEET);
        DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.TURN);
        diffAttributes.setValue(Integer.toString(game.getTurn()));
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);

        createDiff(diff);
        List<Diff> diffs = new ArrayList<>();
        diffs.add(diffMapping.oeToVo(diff));

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffs);
        response.setVersionGame(game.getVersion());

        return response;
    }

    /**
     * Compute the economical sheet of a country for the turn of the game.
     *
     * @param country      the country.
     * @param game         the game.
     * @param tradeCenters the trade centers and their owners.
     */
    private void computeEconomicalSheet(PlayableCountryEntity country, GameEntity game, Map<String, List<CounterFaceTypeEnum>> tradeCenters) {
        EconomicalSheetEntity sheet = CommonUtil.findFirst(country.getEconomicalSheets(), economicalSheetEntity -> economicalSheetEntity.getTurn().equals(game.getTurn()));
        if (sheet == null) {
            sheet = new EconomicalSheetEntity();
            sheet.setCountry(country);
            sheet.setTurn(game.getTurn());

            economicalSheetDao.create(sheet);

            country.getEconomicalSheets().add(sheet);
        }

        Long idGame = game.getId();
        String name = country.getName();

        Map<String, Integer> provinces = economicalSheetDao.getOwnedAndControlledProvinces(name, idGame);
        sheet.setProvincesIncome(provinces.values().stream().collect(Collectors.summingInt(value -> value)));

        Map<String, Integer> vassalProvinces = new HashMap<>();
        List<String> vassals = counterDao.getVassals(name, idGame);
        for (String vassal : vassals) {
            vassalProvinces.putAll(economicalSheetDao.getOwnedAndControlledProvinces(vassal, idGame));
        }
        sheet.setVassalIncome(vassalProvinces.values().stream().collect(Collectors.summingInt(value -> value)));

        List<String> provinceNames = new ArrayList<>();
        provinceNames.addAll(provinces.keySet());
        provinceNames.addAll(vassalProvinces.keySet());
        List<String> pillagedProvinces = economicalSheetDao.getPillagedProvinces(provinceNames, idGame);

        Integer pillagedIncome = pillagedProvinces.stream().collect(Collectors.summingInt(provinces::get));

        sheet.setPillages(pillagedIncome);

        sheet.setLandIncome(CommonUtil.add(sheet.getProvincesIncome(), sheet.getVassalIncome(), sheet.getPillages(), sheet.getEventLandIncome()));

        sheet.setMnuIncome(economicalSheetDao.getMnuIncome(name, pillagedProvinces, idGame));

        List<String> provincesOwnedNotPilaged = provinces.keySet().stream().filter(s -> !pillagedProvinces.contains(s)).collect(Collectors.toList());
        sheet.setGoldIncome(economicalSheetDao.getGoldIncome(provincesOwnedNotPilaged, idGame));

        sheet.setIndustrialIncome(CommonUtil.add(sheet.getMnuIncome(), sheet.getGoldIncome()));

        sheet.setDomTradeIncome(tablesDao.getTradeIncome(CommonUtil.add(sheet.getProvincesIncome(), sheet.getVassalIncome()), country.getDti(), false));

        // TODO needs War to know the blocked trade
        sheet.setForTradeIncome(tablesDao.getTradeIncome(0, country.getFti(), true));

        sheet.setFleetLevelIncome(economicalSheetDao.getFleetLevelIncome(name, idGame));

        sheet.setFleetMonopIncome(economicalSheetDao.getFleetLevelMonopoly(name, idGame));

        Integer tradeCentersIncome = 0;

        if (tradeCenters.get(name) != null) {
            for (CounterFaceTypeEnum tradeCenter : tradeCenters.get(name)) {
                if (tradeCenter == CounterFaceTypeEnum.TRADE_CENTER_ATLANTIC) {
                    tradeCentersIncome += 100;
                } else if (tradeCenter == CounterFaceTypeEnum.TRADE_CENTER_MEDITERRANEAN) {
                    tradeCentersIncome += 100;
                } else if (tradeCenter == CounterFaceTypeEnum.TRADE_CENTER_INDIAN) {
                    tradeCentersIncome += 50;
                }
            }
        }

        sheet.setTradeCenterIncome(tradeCentersIncome);

        Integer sum = CommonUtil.add(sheet.getDomTradeIncome(), sheet.getForTradeIncome(), sheet.getFleetLevelIncome(), sheet.getFleetMonopIncome(), sheet.getTradeCenterIncome());
        if (sheet.getTradeCenterLoss() != null) {
            sum -= sheet.getTradeCenterLoss();
        }
        sheet.setTradeIncome(sum);

        Pair<Integer, Integer> colTpIncome = economicalSheetDao.getColTpIncome(name, idGame);
        sheet.setColIncome(colTpIncome.getLeft());
        sheet.setTpIncome(colTpIncome.getRight());
        sheet.setExoResIncome(economicalSheetDao.getExoResIncome(name, idGame));

        sheet.setRotwIncome(CommonUtil.add(sheet.getColIncome(), sheet.getTpIncome(), sheet.getExoResIncome()));

        sheet.setIncome(CommonUtil.add(sheet.getLandIncome(), sheet.getIndustrialIncome(), sheet.getTradeIncome(), sheet.getRotwIncome(), sheet.getSpecialIncome()));

        sheet.setGrossIncome(CommonUtil.add(sheet.getIncome(), sheet.getEventIncome()));
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse addAdminAction(Request<AddAdminActionRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ADD_ADM_ACT).setParams(METHOD_ADD_ADM_ACT));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getAuthent()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_AUTHENT).setParams(METHOD_ADD_ADM_ACT));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_ADD_ADM_ACT, PARAMETER_ADD_ADM_ACT);
        GameEntity game = gameDiffs.getGame();

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST).setParams(METHOD_ADD_ADM_ACT));

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest().getIdCountry()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_ID_COUNTRY).setParams(METHOD_ADD_ADM_ACT));

        PlayableCountryEntity country = CommonUtil.findFirst(game.getCountries(), c -> c.getId().equals(request.getRequest().getIdCountry()));

        failIfNull(new AbstractService.CheckForThrow<>().setTest(country).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_ID_COUNTRY).setParams(METHOD_ADD_ADM_ACT, request.getRequest().getIdCountry()));

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest().getType()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT));

        AdministrativeActionEntity admAct;
        switch (request.getRequest().getType()) {
            case LM:
                // TODO check if country is at war
            case DIS:
                failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest().getIdObject()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                        .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_ID_OBJECT).setParams(METHOD_ADD_ADM_ACT));
                CounterEntity counter = CommonUtil.findFirst(game.getStacks().stream().flatMap(stack -> stack.getCounters().stream()),
                        c -> c.getId().equals(request.getRequest().getIdObject()));
                failIfNull(new AbstractService.CheckForThrow<>().setTest(counter).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                        .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_ID_OBJECT).setParams(METHOD_ADD_ADM_ACT, request.getRequest().getIdObject()));
                failIfFalse(new AbstractService.CheckForThrow<Boolean>().setTest(StringUtils.equals(counter.getCountry(), country.getName())).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                        .setMsgFormat("{1}: {0} The counter {2} is not owned by the country {3}.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_ID_OBJECT).setParams(METHOD_ADD_ADM_ACT, request.getRequest().getIdObject(), request.getRequest().getIdCountry()));
                if (request.getRequest().getType() == AdminActionTypeEnum.LM) {
                    failIfFalse(new AbstractService.CheckForThrow<Boolean>().setTest(ARMY_LAND_TYPES.contains(counter.getType())).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                            .setMsgFormat("{1}: {0} The counter {2} has the type {3} which cannot be maintained low.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_ID_OBJECT).setParams(METHOD_ADD_ADM_ACT, request.getRequest().getIdObject(), counter.getType()));
                } else {
                    failIfFalse(new AbstractService.CheckForThrow<Boolean>().setTest(ARMY_TYPES.contains(counter.getType())).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                            .setMsgFormat("{1}: {0} The counter {2} has the type {3} which cannot be disbanded.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_ID_OBJECT).setParams(METHOD_ADD_ADM_ACT, request.getRequest().getIdObject(), counter.getType()));
                }

                List<AdministrativeActionEntity> actions = adminActionDao.findAdminActions(request.getRequest().getIdCountry(), game.getTurn(),
                        request.getRequest().getIdObject(), AdminActionTypeEnum.LM, AdminActionTypeEnum.DIS);
                failIfFalse(new AbstractService.CheckForThrow<Boolean>().setTest(actions == null || actions.isEmpty()).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                        .setMsgFormat("{1}: {0} The counter {2} has already a DIS or LM administrative action PLANNED this turn.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_ID_OBJECT).setParams(METHOD_ADD_ADM_ACT, request.getRequest().getIdObject()));


                admAct = new AdministrativeActionEntity();
                admAct.setCountry(country);
                admAct.setStatus(AdminActionStatusEnum.PLANNED);
                admAct.setTurn(game.getTurn());
                admAct.setIdObject(counter.getId());
                break;
            default:
                admAct = null;
                break;
        }

        List<DiffEntity> diffs = gameDiffs.getDiffs();

        if (admAct != null) {
            admAct.setType(request.getRequest().getType());
            adminActionDao.create(admAct);

            DiffEntity diff = new DiffEntity();
            diff.setIdGame(game.getId());
            diff.setVersionGame(game.getVersion());
            diff.setType(DiffTypeEnum.ADD);
            diff.setTypeObject(DiffTypeObjectEnum.ADM_ACT);
            diff.setIdObject(admAct.getId());
            DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
            diffAttributes.setType(DiffAttributeTypeEnum.ID_COUNTRY);
            diffAttributes.setValue(admAct.getCountry().getId().toString());
            diffAttributes.setDiff(diff);
            diff.getAttributes().add(diffAttributes);
            diffAttributes = new DiffAttributesEntity();
            diffAttributes.setType(DiffAttributeTypeEnum.TURN);
            diffAttributes.setValue(admAct.getTurn().toString());
            diffAttributes.setDiff(diff);
            diff.getAttributes().add(diffAttributes);
            diffAttributes = new DiffAttributesEntity();
            diffAttributes.setType(DiffAttributeTypeEnum.TYPE);
            diffAttributes.setValue(admAct.getType().name());
            diffAttributes.setDiff(diff);
            diff.getAttributes().add(diffAttributes);
            diffAttributes = new DiffAttributesEntity();
            diffAttributes.setType(DiffAttributeTypeEnum.ID_OBJECT);
            diffAttributes.setValue(admAct.getIdObject().toString());
            diffAttributes.setDiff(diff);
            diff.getAttributes().add(diffAttributes);

            diffs.add(diff);
        }

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(game.getVersion());

        response.setMessages(getMessagesSince(request));

        return response;
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse removeAdminAction(Request<RemoveAdminActionRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_REMOVE_ADM_ACT).setParams(METHOD_REMOVE_ADM_ACT));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getAuthent()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_REMOVE_ADM_ACT, PARAMETER_AUTHENT).setParams(METHOD_REMOVE_ADM_ACT));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_REMOVE_ADM_ACT, PARAMETER_REMOVE_ADM_ACT);
        GameEntity game = gameDiffs.getGame();

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_REMOVE_ADM_ACT, PARAMETER_REQUEST).setParams(METHOD_REMOVE_ADM_ACT));

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest().getIdAdmAct()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_REMOVE_ADM_ACT, PARAMETER_REQUEST, PARAMETER_ID_ADM_ACT).setParams(METHOD_REMOVE_ADM_ACT));

        AdministrativeActionEntity action = adminActionDao.load(request.getRequest().getIdAdmAct());

        failIfNull(new AbstractService.CheckForThrow<>().setTest(action).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_REMOVE_ADM_ACT, PARAMETER_REQUEST, PARAMETER_ID_ADM_ACT).setParams(METHOD_REMOVE_ADM_ACT, request.getRequest().getIdAdmAct()));

        failIfFalse(new CheckForThrow<Boolean>().setTest(StringUtils.equals(request.getAuthent().getUsername(), action.getCountry().getUsername()))
                .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                .setMsgFormat(MSG_ACCESS_RIGHT).setName(PARAMETER_REMOVE_ADM_ACT, PARAMETER_AUTHENT, PARAMETER_USERNAME).setParams(METHOD_MOVE_COUNTER, request.getAuthent().getUsername(), action.getCountry().getUsername()));

        failIfFalse(new CheckForThrow<Boolean>().setTest(action.getStatus() == AdminActionStatusEnum.PLANNED).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat("{1}: {0} The administrative action {2} is not PLANNED and cannot be removed.").setName(PARAMETER_REMOVE_ADM_ACT, PARAMETER_REQUEST, PARAMETER_ID_ADM_ACT).setParams(METHOD_REMOVE_ADM_ACT, request.getRequest().getIdAdmAct()));

        adminActionDao.delete(action);

        List<DiffEntity> diffs = gameDiffs.getDiffs();

        DiffEntity diff = new DiffEntity();
        diff.setIdGame(game.getId());
        diff.setVersionGame(game.getVersion());
        diff.setType(DiffTypeEnum.REMOVE);
        diff.setTypeObject(DiffTypeObjectEnum.ADM_ACT);
        diff.setIdObject(action.getId());
        DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.ID_COUNTRY);
        diffAttributes.setValue(action.getCountry().getId().toString());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);
        diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.TYPE);
        diffAttributes.setValue(action.getType().name());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);

        diffs.add(diff);

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(game.getVersion());

        response.setMessages(getMessagesSince(request));

        return response;
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse computeAdminActions(Long idGame) throws FunctionalException, TechnicalException {
        return null;
    }
}
