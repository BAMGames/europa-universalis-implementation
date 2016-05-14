package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.common.vo.SimpleRequest;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.IEconomicService;
import com.mkl.eu.client.service.service.eco.AddAdminActionRequest;
import com.mkl.eu.client.service.service.eco.EconomicalSheetCountry;
import com.mkl.eu.client.service.service.eco.LoadEcoSheetsRequest;
import com.mkl.eu.client.service.service.eco.RemoveAdminActionRequest;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.tables.Limit;
import com.mkl.eu.client.service.vo.tables.Tech;
import com.mkl.eu.client.service.vo.tables.TradeIncome;
import com.mkl.eu.client.service.vo.tables.Unit;
import com.mkl.eu.client.service.vo.util.EconomicUtil;
import com.mkl.eu.client.service.vo.util.GameUtil;
import com.mkl.eu.client.service.vo.util.MaintenanceUtil;
import com.mkl.eu.service.service.mapping.eco.EconomicalSheetMapping;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.board.IStackDao;
import com.mkl.eu.service.service.persistence.eco.IAdminActionDao;
import com.mkl.eu.service.service.persistence.eco.IEconomicalSheetDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.eco.AdministrativeActionEntity;
import com.mkl.eu.service.service.persistence.oe.eco.EconomicalSheetEntity;
import com.mkl.eu.service.service.persistence.oe.eco.TradeFleetEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.*;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.service.GameDiffsInfo;
import com.mkl.eu.service.service.util.IOEUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    /** Counter Face Type for naval armies. */
    private static final List<CounterFaceTypeEnum> ARMY_NAVAL_TYPES = new ArrayList<>();
    /** Counter Face Type for fortresses. */
    private static final List<CounterFaceTypeEnum> FORTRESS_TYPES = new ArrayList<>();
    /** EconomicalSheet DAO. */
    @Autowired
    private IEconomicalSheetDao economicalSheetDao;
    /** AdminAction DAO. */
    @Autowired
    private IAdminActionDao adminActionDao;
    /** Counter DAO. */
    @Autowired
    private ICounterDao counterDao;
    /** Stack DAO. */
    @Autowired
    private IStackDao stackDao;
    /** Province DAO. */
    @Autowired
    private IProvinceDao provinceDao;
    /** Game mapping. */
    @Autowired
    private EconomicalSheetMapping ecoSheetsMapping;
    /** OEUtil. */
    @Autowired
    private IOEUtil oeUtil;

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
        ARMY_LAND_TYPES.add(CounterFaceTypeEnum.ARMY_TIMAR_PLUS);
        ARMY_LAND_TYPES.add(CounterFaceTypeEnum.ARMY_MINUS);
        ARMY_LAND_TYPES.add(CounterFaceTypeEnum.ARMY_TIMAR_MINUS);
        ARMY_LAND_TYPES.add(CounterFaceTypeEnum.LAND_DETACHMENT);
        ARMY_LAND_TYPES.add(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION);
        ARMY_LAND_TYPES.add(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR);
        ARMY_LAND_TYPES.add(CounterFaceTypeEnum.LAND_DETACHMENT_KOZAK);
        ARMY_LAND_TYPES.add(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION_KOZAK);

        ARMY_NAVAL_TYPES.add(CounterFaceTypeEnum.FLEET_PLUS);
        ARMY_NAVAL_TYPES.add(CounterFaceTypeEnum.FLEET_MINUS);
        ARMY_NAVAL_TYPES.add(CounterFaceTypeEnum.NAVAL_DETACHMENT);
        ARMY_NAVAL_TYPES.add(CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION);
        ARMY_NAVAL_TYPES.add(CounterFaceTypeEnum.NAVAL_GALLEY);
        ARMY_NAVAL_TYPES.add(CounterFaceTypeEnum.NAVAL_TRANSPORT);

        FORTRESS_TYPES.add(CounterFaceTypeEnum.FORTRESS_1);
        FORTRESS_TYPES.add(CounterFaceTypeEnum.FORTRESS_2);
        FORTRESS_TYPES.add(CounterFaceTypeEnum.FORTRESS_3);
        FORTRESS_TYPES.add(CounterFaceTypeEnum.FORTRESS_4);
        FORTRESS_TYPES.add(CounterFaceTypeEnum.FORTRESS_5);
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

        final Integer valueDom = CommonUtil.add(sheet.getProvincesIncome(), sheet.getVassalIncome());
        TradeIncome tradeIncome = CommonUtil.findFirst(getTables().getDomesticTrades(), tradeIncome1 -> tradeIncome1.getCountryValue() == country.getDti()
                        && (tradeIncome1.getMinValue() == null || tradeIncome1.getMinValue() <= valueDom)
                        && (tradeIncome1.getMaxValue() == null || tradeIncome1.getMaxValue() >= valueDom)
        );
        if (tradeIncome != null) {
            sheet.setDomTradeIncome(tradeIncome.getValue());
        }

        // TODO needs War to know the blocked trade
        final Integer valueFor = 0;
        tradeIncome = CommonUtil.findFirst(getTables().getForeignTrades(), tradeIncome1 -> tradeIncome1.getCountryValue() == country.getFti()
                        && (tradeIncome1.getMinValue() == null || tradeIncome1.getMinValue() <= valueFor)
                        && (tradeIncome1.getMaxValue() == null || tradeIncome1.getMaxValue() >= valueFor)
        );
        if (tradeIncome != null) {
            sheet.setForTradeIncome(tradeIncome.getValue());
        }

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
            case LF:
            case DIS:
                admAct = computeDisbandLowMaintenance(request, game, country);
                break;
            case PU:
                admAct = computePurchase(request, game, country);
                break;
            case TFI:
                admAct = computeTradeFleetImplantation(request, game, country);
                break;
            case MNU:
                admAct = computeManufacture(request, game, country);
                break;
            case FTI:
            case DTI:
                admAct = computeFtiDti(request, game, country);
                break;
            case ELT:
                admAct = computeExceptionalTaxes(request, game, country);
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
            if (admAct.getCost() != null) {
                diffAttributes = new DiffAttributesEntity();
                diffAttributes.setType(DiffAttributeTypeEnum.COST);
                diffAttributes.setValue(admAct.getCost().toString());
                diffAttributes.setDiff(diff);
                diff.getAttributes().add(diffAttributes);
            }
            if (admAct.getIdObject() != null) {
                diffAttributes = new DiffAttributesEntity();
                diffAttributes.setType(DiffAttributeTypeEnum.ID_OBJECT);
                diffAttributes.setValue(admAct.getIdObject().toString());
                diffAttributes.setDiff(diff);
                diff.getAttributes().add(diffAttributes);
            }
            if (admAct.getProvince() != null) {
                diffAttributes = new DiffAttributesEntity();
                diffAttributes.setType(DiffAttributeTypeEnum.PROVINCE);
                diffAttributes.setValue(admAct.getProvince());
                diffAttributes.setDiff(diff);
                diff.getAttributes().add(diffAttributes);
            }
            if (admAct.getCounterFaceType() != null) {
                diffAttributes = new DiffAttributesEntity();
                diffAttributes.setType(DiffAttributeTypeEnum.COUNTER_FACE_TYPE);
                diffAttributes.setValue(admAct.getCounterFaceType().name());
                diffAttributes.setDiff(diff);
                diff.getAttributes().add(diffAttributes);
            }
            if (admAct.getColumn() != null) {
                diffAttributes = new DiffAttributesEntity();
                diffAttributes.setType(DiffAttributeTypeEnum.COLUMN);
                diffAttributes.setValue(admAct.getColumn().toString());
                diffAttributes.setDiff(diff);
                diff.getAttributes().add(diffAttributes);
            }
            if (admAct.getBonus() != null) {
                diffAttributes = new DiffAttributesEntity();
                diffAttributes.setType(DiffAttributeTypeEnum.BONUS);
                diffAttributes.setValue(admAct.getBonus().toString());
                diffAttributes.setDiff(diff);
                diff.getAttributes().add(diffAttributes);
            }

            diffs.add(diff);
        }

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(game.getVersion());

        response.setMessages(getMessagesSince(request));

        return response;
    }

    /**
     * Computes the creation of a PLANNED administrative action of type PURCHASE.
     *
     * @param request request containing the info about the action to create.
     * @param game    in which the action will be created.
     * @param country owner of the action.
     * @return the administrative action to create.
     * @throws FunctionalException
     */
    private AdministrativeActionEntity computeDisbandLowMaintenance(Request<AddAdminActionRequest> request, GameEntity game, PlayableCountryEntity country) throws FunctionalException {
        failIfNull(new CheckForThrow<>().setTest(request.getRequest().getIdObject()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_ID_OBJECT).setParams(METHOD_ADD_ADM_ACT));
        CounterEntity counter = CommonUtil.findFirst(game.getStacks().stream().flatMap(stack -> stack.getCounters().stream()),
                c -> c.getId().equals(request.getRequest().getIdObject()));
        failIfNull(new CheckForThrow<>().setTest(counter).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_ID_OBJECT).setParams(METHOD_ADD_ADM_ACT, request.getRequest().getIdObject()));
        failIfFalse(new CheckForThrow<Boolean>().setTest(StringUtils.equals(counter.getCountry(), country.getName())).setCodeError(IConstantsServiceException.COUNTER_NOT_OWNED)
                .setMsgFormat("{1}: {0} The counter {2} is not owned by the country {3}.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_ID_OBJECT).setParams(METHOD_ADD_ADM_ACT, request.getRequest().getIdObject(), request.getRequest().getIdCountry()));
        if (request.getRequest().getType() == AdminActionTypeEnum.LM) {
            failIfFalse(new CheckForThrow<Boolean>().setTest(ARMY_LAND_TYPES.contains(counter.getType())).setCodeError(IConstantsServiceException.COUNTER_CANT_MAINTAIN_LOW)
                    .setMsgFormat("{1}: {0} The counter {2} has the type {3} which cannot be maintained low.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_ID_OBJECT).setParams(METHOD_ADD_ADM_ACT, request.getRequest().getIdObject(), counter.getType()));
        } else if (request.getRequest().getType() == AdminActionTypeEnum.DIS) {
            failIfFalse(new CheckForThrow<Boolean>().setTest(ARMY_TYPES.contains(counter.getType()) || FORTRESS_TYPES.contains(counter.getType())).setCodeError(IConstantsServiceException.COUNTER_CANT_DISBAND)
                    .setMsgFormat("{1}: {0} The counter {2} has the type {3} which cannot be disbanded.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_ID_OBJECT).setParams(METHOD_ADD_ADM_ACT, request.getRequest().getIdObject(), counter.getType()));
        } else if (request.getRequest().getType() == AdminActionTypeEnum.LF) {
            failIfNull(new CheckForThrow<>().setTest(request.getRequest().getCounterFaceType()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                    .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_COUNTER_FACE_TYPE).setParams(METHOD_ADD_ADM_ACT));
            failIfFalse(new CheckForThrow<Boolean>().setTest(FORTRESS_TYPES.contains(counter.getType())).setCodeError(IConstantsServiceException.COUNTER_CANT_LOWER_FORTRESS)
                    .setMsgFormat("{1}: {0} The counter {2} has the type {3} which cannot be lower fortress.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_ID_OBJECT).setParams(METHOD_ADD_ADM_ACT, request.getRequest().getIdObject(), counter.getType()));

            String province = counter.getOwner().getProvince();
            AbstractProvinceEntity prov = provinceDao.getProvinceByName(province);

            int naturalLevel = 0;
            if (prov instanceof EuropeanProvinceEntity) {
                if (((EuropeanProvinceEntity) prov).getFortress() != null) {
                    naturalLevel = ((EuropeanProvinceEntity) prov).getFortress();
                }
            }

            int actualLevel = MaintenanceUtil.getFortressLevelFromType(counter.getType());
            int desiredLevel = MaintenanceUtil.getFortressLevelFromType(request.getRequest().getCounterFaceType());

            failIfFalse(new CheckForThrow<Boolean>().setTest(desiredLevel > naturalLevel && desiredLevel < actualLevel).setCodeError(IConstantsServiceException.COUNTER_WRONG_LOWER_FORTRESS)
                    .setMsgFormat("{1}: {0} The fortress {2} of level {5} cannot be lowered to {3} (natural fortress: {4}).").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_ID_OBJECT).setParams(METHOD_ADD_ADM_ACT, request.getRequest().getIdObject(), desiredLevel, naturalLevel, actualLevel));
        }

        List<AdministrativeActionEntity> actions = adminActionDao.findAdminActions(request.getRequest().getIdCountry(), game.getTurn(),
                request.getRequest().getIdObject(), AdminActionTypeEnum.LM, AdminActionTypeEnum.DIS, AdminActionTypeEnum.LF);
        failIfFalse(new CheckForThrow<Boolean>().setTest(actions == null || actions.isEmpty()).setCodeError(IConstantsServiceException.COUNTER_ALREADY_PLANNED)
                .setMsgFormat("{1}: {0} The counter {2} has already a DIS or LM or LF administrative action PLANNED this turn.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_ID_OBJECT).setParams(METHOD_ADD_ADM_ACT, request.getRequest().getIdObject()));


        AdministrativeActionEntity admAct = new AdministrativeActionEntity();
        admAct.setCountry(country);
        admAct.setStatus(AdminActionStatusEnum.PLANNED);
        admAct.setTurn(game.getTurn());
        admAct.setIdObject(counter.getId());
        admAct.setCounterFaceType(request.getRequest().getCounterFaceType());
        return admAct;
    }

    /**
     * Computes the creation of a PLANNED administrative action of type PURCHASE.
     *
     * @param request request containing the info about the action to create.
     * @param game    in which the action will be created.
     * @param country owner of the action.
     * @return the administrative action to create.
     * @throws FunctionalException
     */
    private AdministrativeActionEntity computePurchase(Request<AddAdminActionRequest> request, GameEntity game, PlayableCountryEntity country) throws FunctionalException {
        String province = request.getRequest().getProvince();
        CounterFaceTypeEnum faceType = request.getRequest().getCounterFaceType();
        failIfNull(new CheckForThrow<>().setTest(faceType).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_COUNTER_FACE_TYPE).setParams(METHOD_ADD_ADM_ACT));
        failIfEmpty(new CheckForThrow<String>().setTest(province).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT));

        AbstractProvinceEntity prov = provinceDao.getProvinceByName(province);

        failIfNull(new CheckForThrow<>().setTest(prov).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT, province));

        List<StackEntity> stacks = stackDao.getStacksOnProvince(province, game.getId());
        String owner = null;

        boolean port = false;

        if (prov instanceof EuropeanProvinceEntity) {
            EuropeanProvinceEntity euProv = (EuropeanProvinceEntity) prov;
            owner = euProv.getDefaultOwner();
            if (euProv.isPort() != null) {
                port = euProv.isPort();
            }
            if (!port && euProv.isArsenal() != null) {
                port = euProv.isArsenal();
            }
        }

        boolean isFortress = FORTRESS_TYPES.contains(faceType);

        CounterEntity ownCounter = CommonUtil.findFirst(stacks.stream().flatMap(stack -> stack.getCounters().stream()), counter -> counter.getType() == CounterFaceTypeEnum.OWN);
        if (ownCounter != null) {
            owner = ownCounter.getCountry();
        }

        boolean provinceOk = StringUtils.equals(country.getName(), owner);

        if (provinceOk || isFortress) {
            CounterEntity ctrlCounter = CommonUtil.findFirst(stacks.stream().flatMap(stack -> stack.getCounters().stream()), counter -> counter.getType() == CounterFaceTypeEnum.CONTROL);
            if (ctrlCounter != null) {
                provinceOk = StringUtils.equals(country.getName(), ctrlCounter.getCountry());
            }
        }

        failIfFalse(new CheckForThrow<Boolean>().setTest(provinceOk).setCodeError(IConstantsServiceException.PROVINCE_NOT_OWN_CONTROL)
                .setMsgFormat("{1}: {0} The province {2} is not owned and controlled by {3}.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT, province, country.getName()));

        boolean faceConsistent = isFortress;

        if (port) {
            faceConsistent |= ARMY_TYPES.contains(faceType);
        } else {
            faceConsistent |= ARMY_LAND_TYPES.contains(faceType);
        }

        failIfFalse(new CheckForThrow<Boolean>().setTest(faceConsistent).setCodeError(IConstantsServiceException.COUNTER_CANT_PURCHASE)
                .setMsgFormat("{1}: {0} The counter face type {2} cannot be purchased on province {3}.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_COUNTER_FACE_TYPE).setParams(METHOD_ADD_ADM_ACT, faceType, province));

        Integer cost;

        if (isFortress) {
            cost = computeFortressPurchase(game, country, stacks, prov, faceType);
        } else {
            cost = computeUnitPurchase(game, country, faceType);
        }

        AdministrativeActionEntity admAct = new AdministrativeActionEntity();
        admAct.setCountry(country);
        admAct.setStatus(AdminActionStatusEnum.PLANNED);
        admAct.setTurn(game.getTurn());
        admAct.setProvince(province);
        admAct.setCounterFaceType(faceType);
        admAct.setCost(cost);

        return admAct;
    }

    /**
     * Computes the creation of a PLANNED administrative action of type PURCHASE for a fortress.
     *
     * @param game     in which the action will be created.
     * @param country  owner of the action.
     * @param stacks   the stacks of the game.
     * @param prov     the province where the action is planned.
     * @param faceType the type of face of the fortress to purchase.
     * @return the administrative action to create.
     * @throws FunctionalException
     */
    private Integer computeFortressPurchase(GameEntity game, PlayableCountryEntity country, List<StackEntity> stacks, AbstractProvinceEntity prov, CounterFaceTypeEnum faceType) throws FunctionalException {
        int actualLevel = 0;
        if (prov instanceof EuropeanProvinceEntity) {
            if (((EuropeanProvinceEntity) prov).getFortress() != null) {
                actualLevel = ((EuropeanProvinceEntity) prov).getFortress();
            }
        }
        CounterEntity fortressCounter = CommonUtil.findFirst(stacks.stream().flatMap(stack -> stack.getCounters().stream()), counter -> StringUtils.equals(country.getName(), counter.getCountry()) && FORTRESS_TYPES.contains(counter.getType()));
        if (fortressCounter != null) {
            actualLevel = MaintenanceUtil.getFortressLevelFromType(fortressCounter.getType());
        }

        int desiredLevel = MaintenanceUtil.getFortressLevelFromType(faceType);


        Tech actualTech = CommonUtil.findFirst(getTables().getTechs(), tech -> StringUtils.equals(tech.getName(), country.getLandTech()));
        boolean canPurchaseFortress = desiredLevel != 0 && actualTech != null;
        final String fortressTech;
        if (desiredLevel == 2) {
            fortressTech = Tech.MEDIEVAL;
        } else if (desiredLevel == 3) {
            fortressTech = Tech.RENAISSANCE;
        } else if (desiredLevel == 4 || desiredLevel == 5) {
            fortressTech = Tech.BAROQUE;
        } else {
            fortressTech = null;
        }
        Tech targetTech = CommonUtil.findFirst(getTables().getTechs(), tech -> StringUtils.equals(tech.getName(), fortressTech));
        if (actualTech != null && targetTech != null) {
            canPurchaseFortress = actualTech.getBeginTurn() >= targetTech.getBeginTurn();
        }
        if (desiredLevel == 5) {
            canPurchaseFortress = game.getTurn() >= 40;
        }

        failIfFalse(new CheckForThrow<Boolean>().setTest(desiredLevel == actualLevel + 1).setCodeError(IConstantsServiceException.COUNTER_CANT_PURCHASE)
                .setMsgFormat("{1}: {0} The counter face type {2} cannot be purchased on province {3}.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_COUNTER_FACE_TYPE).setParams(METHOD_ADD_ADM_ACT, faceType, prov.getName()));


        failIfFalse(new CheckForThrow<Boolean>().setTest(canPurchaseFortress).setCodeError(IConstantsServiceException.FORTRESS_CANT_PURCHASE)
                .setMsgFormat("{1}: {0} The fortress {2} cannot be purchased because actual technology is {3}.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_COUNTER_FACE_TYPE).setParams(METHOD_ADD_ADM_ACT, faceType, country.getLandTech()));

        Integer fortressCost = 25 * (MaintenanceUtil.getFortressLevelFromType(faceType) - 1);
        if (prov instanceof RotwProvinceEntity) {
            // Fortress 1 costs 25 ducats in rotw (not pertinent in Europe)
            if (fortressCost == 0) {
                fortressCost = 25;
            } else {
                fortressCost *= 2;
            }
        }
        boolean doubleCost = (desiredLevel == 4 && game.getTurn() < 40);
        if (desiredLevel == 3) {
            Tech arquebusTech = CommonUtil.findFirst(getTables().getTechs(), tech -> StringUtils.equals(tech.getName(), Tech.ARQUEBUS));
            if (actualTech != null && arquebusTech != null) {
                doubleCost = actualTech.getBeginTurn() < arquebusTech.getBeginTurn();
            }
        }

        if (doubleCost) {
            fortressCost *= 2;
        }

        return fortressCost;
    }

    /**
     * Computes the creation of a PLANNED administrative action of type PURCHASE for a unit.
     *
     * @param game     in which the action will be created.
     * @param country  owner of the action.
     * @param faceType the type of face of the unit to purchase.
     * @return the administrative action to create.
     * @throws FunctionalException
     */
    private Integer computeUnitPurchase(GameEntity game, PlayableCountryEntity country, CounterFaceTypeEnum faceType) throws FunctionalException {
        boolean land = ARMY_LAND_TYPES.contains(faceType);
        final List<CounterFaceTypeEnum> faces;
        final LimitTypeEnum limitType;
        if (land) {
            faces = ARMY_LAND_TYPES;
            limitType = LimitTypeEnum.PURCHASE_LAND_TROOPS;
        } else {
            faces = ARMY_NAVAL_TYPES;
            limitType = LimitTypeEnum.PURCHASE_NAVAL_TROOPS;
        }

        List<AdministrativeActionEntity> actions = adminActionDao.findAdminActions(country.getId(), game.getTurn(),
                null, AdminActionTypeEnum.PU);

        Integer plannedSize = actions.stream().filter(action -> faces.contains(action.getCounterFaceType())).collect(Collectors.summingInt(action -> MaintenanceUtil.getSizeFromType(action.getCounterFaceType())));
        Integer size = MaintenanceUtil.getSizeFromType(faceType);
        Integer maxPurchase = getTables().getLimits().stream().filter(
                limit -> StringUtils.equals(limit.getCountry(), country.getName()) &&
                        limit.getType() == limitType &&
                        limit.getPeriod().getBegin() <= game.getTurn() &&
                        limit.getPeriod().getEnd() >= game.getTurn()).collect(Collectors.summingInt(Limit::getNumber));

        boolean purchaseOk = (land && plannedSize + size <= 3 * maxPurchase) || (!land && plannedSize + size <= maxPurchase);

        failIfFalse(new CheckForThrow<Boolean>().setTest(purchaseOk).setCodeError(IConstantsServiceException.PURCHASE_LIMIT_EXCEED)
                .setMsgFormat("{1}: {0} The counter face type {2} cannot be purchased because country limits were exceeded ({3}/{4}).").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_COUNTER_FACE_TYPE).setParams(METHOD_ADD_ADM_ACT, faceType, plannedSize, maxPurchase));

        ForceTypeEnum type = MaintenanceUtil.getPurchaseForceFromFace(faceType);
        Unit unitCost = CommonUtil.findFirst(getTables().getUnits(), unit -> StringUtils.equals(country.getName(), unit.getCountry()) &&
                        !unit.isSpecial() &&
                        unit.getAction() == UnitActionEnum.PURCHASE &&
                        unit.getType() == type &&
                        (StringUtils.equals(unit.getTech().getName(), country.getLandTech()) || StringUtils.equals(unit.getTech().getName(), country.getNavalTech()))
        );

        Integer cost = null;
        if (unitCost != null) {
            cost = MaintenanceUtil.getPurchasePrice(plannedSize, maxPurchase, unitCost.getPrice(), size);
        }
        return cost;
    }


    /**
     * Computes the creation of a PLANNED administrative action of type TFI.
     *
     * @param request request containing the info about the action to create.
     * @param game    in which the action will be created.
     * @param country owner of the action.
     * @return the administrative action to create.
     * @throws FunctionalException Exception.
     */
    private AdministrativeActionEntity computeTradeFleetImplantation(Request<AddAdminActionRequest> request, GameEntity game, PlayableCountryEntity country) throws FunctionalException {
        List<AdministrativeActionEntity> actions = adminActionDao.findAdminActions(country.getId(), game.getTurn(),
                null, AdminActionTypeEnum.TFI);

        Integer plannedTfis = actions.stream().collect(Collectors.summingInt(action -> 1));
        Integer maxTfis = getTables().getLimits().stream().filter(
                limit -> StringUtils.equals(limit.getCountry(), country.getName()) &&
                        limit.getType() == LimitTypeEnum.ACTION_TFI &&
                        limit.getPeriod().getBegin() <= game.getTurn() &&
                        limit.getPeriod().getEnd() >= game.getTurn()).collect(Collectors.summingInt(Limit::getNumber));

        failIfFalse(new CheckForThrow<Boolean>().setTest(plannedTfis < maxTfis).setCodeError(IConstantsServiceException.ADMIN_ACTION_LIMIT_EXCEED)
                .setMsgFormat("{1}: {0} The administrative action of type {2} for the country {3} cannot be planned because country limits were exceeded ({4}/{5}).").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, LimitTypeEnum.ACTION_TFI, country.getName(), plannedTfis, maxTfis));

        failIfNull(new CheckForThrow<>().setTest(request.getRequest().getInvestment()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_INVESTMENT).setParams(METHOD_ADD_ADM_ACT));

        String province = request.getRequest().getProvince();
        failIfEmpty(new CheckForThrow<String>().setTest(province).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT));

        AbstractProvinceEntity prov = provinceDao.getProvinceByName(province);

        failIfNull(new CheckForThrow<>().setTest(prov).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT, province));

        failIfFalse(new CheckForThrow<Boolean>().setTest(prov instanceof TradeZoneProvinceEntity).setCodeError(IConstantsServiceException.PROVINCE_WRONG_TYPE)
                .setMsgFormat("{1}: {0} The province {2} of type {3} should be of type {4}.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT, province, prov.getClass(), TradeZoneProvinceEntity.class));

        // TODO trade fleet access, VI.7.4.1

        TradeFleetEntity tradeFleet = CommonUtil.findFirst(game.getTradeFleets(), tradeFleetEntity -> StringUtils.equals(province, tradeFleetEntity.getProvince())
                && StringUtils.equals(country.getName(), tradeFleetEntity.getCountry()));

        failIfFalse(new CheckForThrow<Boolean>().setTest(tradeFleet == null || tradeFleet.getLevel() < 6).setCodeError(IConstantsServiceException.TRADE_FLEET_FULL)
                .setMsgFormat("{1}: {0} The trade fleet located in {2} and owned by {3} is already full.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT, province, country.getName()));

        Integer column = country.getFti();
        //noinspection ConstantConditions
        TradeZoneProvinceEntity tradeZone = (TradeZoneProvinceEntity) prov;
        if (tradeZone.getType() == TradeZoneTypeEnum.ZP) {
            if (StringUtils.equals(country.getName(), tradeZone.getCountryName())) {
                column += country.getDti();
            } else if (!StringUtils.isEmpty(tradeZone.getCountryName())) {
                PlayableCountryEntity enemy = CommonUtil.findFirst(game.getCountries(), playableCountryEntity -> StringUtils.equals(tradeZone.getCountryName(), playableCountryEntity.getName()));
                // REMINDER : check if minor countries who were major are still playable countries
                if (enemy != null) {
                    column -= enemy.getDti();
                }
            }
        }
        int otherTfs = game.getTradeFleets().stream().filter(tradeFleetEntity -> StringUtils.equals(province, tradeFleetEntity.getProvince())
                && !StringUtils.equals(country.getName(), tradeFleetEntity.getCountry()))
                .collect(Collectors.summingInt(value -> value.getLevel() != null && value.getLevel() > 0 ? 1 : 0));
        column -= otherTfs;

        // threshold to -4/4
        column = Math.min(Math.max(column, -4), 4);
        column += EconomicUtil.getAdminActionColumnBonus(request.getRequest().getType(), request.getRequest().getInvestment());
        // threshold to -4/4
        column = Math.min(Math.max(column, -4), 4);

        int bonus = 0;
        if (tradeFleet != null && tradeFleet.getLevel() != null && tradeFleet.getLevel() >= 4) {
            bonus = 1;
        }
        List<StackEntity> stacks = game.getStacks().stream().filter(stackEntity -> StringUtils.equals(tradeZone.getSeaZone(), stackEntity.getProvince())).collect(Collectors.toList());
        CounterEntity pirate = CommonUtil.findFirst(stacks.stream().flatMap(stackEntity -> stackEntity.getCounters().stream()), o -> o.getType() == CounterFaceTypeEnum.PIRATE_MINUS || o.getType() == CounterFaceTypeEnum.PIRATE_PLUS);
        if (pirate != null) {
            bonus -= 1;
        }
        // TODO battles

        AdministrativeActionEntity admAct = new AdministrativeActionEntity();
        admAct.setCountry(country);
        admAct.setStatus(AdminActionStatusEnum.PLANNED);
        admAct.setTurn(game.getTurn());
        admAct.setProvince(province);
        admAct.setCost(EconomicUtil.getAdminActionCost(request.getRequest().getType(), request.getRequest().getInvestment()));
        admAct.setColumn(column);
        admAct.setBonus(bonus);

        return admAct;
    }


    /**
     * Computes the creation of a PLANNED administrative action of type MNU.
     *
     * @param request request containing the info about the action to create.
     * @param game    in which the action will be created.
     * @param country owner of the action.
     * @return the administrative action to create.
     * @throws FunctionalException Exception.
     */
    private AdministrativeActionEntity computeManufacture(Request<AddAdminActionRequest> request, GameEntity game, PlayableCountryEntity country) throws FunctionalException {
        List<AdministrativeActionEntity> actions = adminActionDao.findAdminActions(country.getId(), game.getTurn(),
                null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI, AdminActionTypeEnum.DTI, AdminActionTypeEnum.EXL);

        failIfFalse(new CheckForThrow<Boolean>().setTest(actions.isEmpty()).setCodeError(IConstantsServiceException.ACTION_ALREADY_PLANNED)
                .setMsgFormat("{1}: {0} The administrative action of type {1} is already panned for the country {3}.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, AdminActionTypeEnum.MNU, country.getName()));

        failIfNull(new CheckForThrow<>().setTest(request.getRequest().getInvestment()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_INVESTMENT).setParams(METHOD_ADD_ADM_ACT));

        CounterFaceTypeEnum type = request.getRequest().getCounterFaceType();
        failIfNull(new CheckForThrow<>().setTest(type).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_COUNTER_FACE_TYPE).setParams(METHOD_ADD_ADM_ACT));

        failIfFalse(new CheckForThrow<Boolean>().setTest(EconomicUtil.isManufacture(type)).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat("{1}: {0} The type {2} is not a manufacture.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_COUNTER_FACE_TYPE).setParams(METHOD_ADD_ADM_ACT, type));


        String province = request.getRequest().getProvince();
        failIfEmpty(new CheckForThrow<String>().setTest(province).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT));

        AbstractProvinceEntity prov = provinceDao.getProvinceByName(province);

        failIfNull(new CheckForThrow<>().setTest(prov).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT, province));

        failIfFalse(new CheckForThrow<Boolean>().setTest(prov instanceof EuropeanProvinceEntity).setCodeError(IConstantsServiceException.PROVINCE_WRONG_TYPE)
                .setMsgFormat("{1}: {0} The province {2} of type {3} should be of type {4}.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT, province, prov.getClass(), EuropeanProvinceEntity.class));


        @SuppressWarnings("ConstantConditions") EuropeanProvinceEntity euProv = (EuropeanProvinceEntity) prov;
        String owner = euProv.getDefaultOwner();

        List<CounterEntity> counters = game.getStacks().stream().filter(stack -> StringUtils.equals(province, stack.getProvince())).flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> counter.getType() == CounterFaceTypeEnum.OWN
                        || counter.getType() == CounterFaceTypeEnum.CONTROL
                        || EconomicUtil.isManufacture(counter.getType())).collect(Collectors.toList());
        String control = country.getName();
        // MNU to upgrade if it exists, or create if it doesn't
        CounterEntity mnu = null;
        // Other MNU in the province
        boolean otherMnu = false;
        for (CounterEntity counter : counters) {
            if (counter.getType() == CounterFaceTypeEnum.CONTROL) {
                control = counter.getCountry();
            } else if (counter.getType() == CounterFaceTypeEnum.OWN) {
                owner = counter.getCountry();
            } else if (counter.getType() == type) {
                mnu = counter;
            } else if (EconomicUtil.isManufacture(counter.getType())) {
                otherMnu = true;
            }
        }

        // If the MNU is already max, then it is the creation of a new MNU alongside the other one
        if (mnu != null && (EconomicUtil.getManufactureLevel(mnu.getType()) == 2)) {
            mnu = null;
            otherMnu = true;
        }

        boolean provinceOk = StringUtils.equals(country.getName(), owner) && StringUtils.equals(country.getName(), control);

        failIfFalse(new CheckForThrow<Boolean>().setTest(provinceOk).setCodeError(IConstantsServiceException.PROVINCE_NOT_OWN_CONTROL)
                .setMsgFormat("{1}: {0} The province {2} is not owned and controlled by {3}.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT, province, country.getName()));

        // New MNU ? Check placement and limits
        if (mnu == null) {
            Integer actualMnus = game.getStacks().stream().flatMap(stackEntity -> stackEntity.getCounters().stream())
                    .filter(counterEntity -> StringUtils.equals(country.getName(), counterEntity.getCountry()) && EconomicUtil.isManufacture(counterEntity.getType())).collect(Collectors.summingInt(value -> 1));
            Integer maxMnus = getTables().getLimits().stream().filter(
                    limit -> StringUtils.equals(limit.getCountry(), country.getName()) &&
                            limit.getType() == LimitTypeEnum.MAX_MNU &&
                            limit.getPeriod().getBegin() <= game.getTurn() &&
                            limit.getPeriod().getEnd() >= game.getTurn()).collect(Collectors.summingInt(Limit::getNumber));

            failIfFalse(new CheckForThrow<Boolean>().setTest(actualMnus < maxMnus + 2).setCodeError(IConstantsServiceException.ADMIN_ACTION_LIMIT_EXCEED)
                    .setMsgFormat("{1}: {0} The administrative action of type {2} for the country {3} cannot be planned because country limits were exceeded ({4}/{5}).")
                    .setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, AdminActionTypeEnum.MNU, country.getName(), actualMnus, maxMnus));

            // Cereals manufactures on plains
            provinceOk = (type != CounterFaceTypeEnum.MNU_CEREALS_MINUS && type != CounterFaceTypeEnum.MNU_CEREALS_PLUS)
                    || euProv.getTerrain() == TerrainEnum.PLAIN;
            failIfFalse(new CheckForThrow<Boolean>().setTest(provinceOk).setCodeError(IConstantsServiceException.MNU_WRONG_PROVINCE)
                    .setMsgFormat("{1}: {0} The manufacture {2} can''t be created in the province {3}. Actual: {4}. Expected: {5}.")
                    .setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, type, province, euProv.getTerrain(), TerrainEnum.PLAIN));

            // Wood manufactures on sparse or dense forests
            provinceOk = (type != CounterFaceTypeEnum.MNU_WOOD_MINUS && type != CounterFaceTypeEnum.MNU_WOOD_PLUS)
                    || (euProv.getTerrain() == TerrainEnum.DENSE_FOREST || euProv.getTerrain() == TerrainEnum.SPARSE_FOREST);
            failIfFalse(new CheckForThrow<Boolean>().setTest(provinceOk).setCodeError(IConstantsServiceException.MNU_WRONG_PROVINCE)
                    .setMsgFormat("{1}: {0} The manufacture {2} can''t be created in the province {3}. Actual: {4}. Expected: {5}.")
                    .setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, type, province, euProv.getTerrain(), TerrainEnum.SPARSE_FOREST));

            // Salt manufactures on province with salt resource
            provinceOk = (type != CounterFaceTypeEnum.MNU_SALT_MINUS && type != CounterFaceTypeEnum.MNU_SALT_PLUS)
                    || (euProv.getSalt() != null && euProv.getSalt() > 0);
            failIfFalse(new CheckForThrow<Boolean>().setTest(provinceOk).setCodeError(IConstantsServiceException.MNU_WRONG_PROVINCE)
                    .setMsgFormat("{1}: {0} The manufacture {2} can''t be created in the province {3}. Actual: {4}. Expected: {5}.")
                    .setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, type, province, euProv.getSalt(), "1+"));

            // Fish manufactures on coastal province
            if (type == CounterFaceTypeEnum.MNU_FISH_MINUS || type == CounterFaceTypeEnum.MNU_FISH_PLUS) {
                boolean coastal = false;
                for (BorderEntity border : euProv.getBorders()) {
                    coastal = border.getProvinceFrom().getTerrain() == TerrainEnum.SEA
                            || border.getProvinceTo().getTerrain() == TerrainEnum.SEA;
                    if (coastal) {
                        break;
                    }
                }

                failIfFalse(new CheckForThrow<Boolean>().setTest(coastal).setCodeError(IConstantsServiceException.MNU_WRONG_PROVINCE)
                        .setMsgFormat("{1}: {0} The manufacture {2} can''t be created in the province {3}. Actual: {4}. Expected: {5}.")
                        .setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, type, province, 0, "1+"));
            }

            // Art manufactures on province with an income of 5+
            provinceOk = (type != CounterFaceTypeEnum.MNU_ART_MINUS && type != CounterFaceTypeEnum.MNU_ART_PLUS)
                    || (euProv.getIncome() != null && euProv.getIncome() >= 5);
            failIfFalse(new CheckForThrow<Boolean>().setTest(provinceOk).setCodeError(IConstantsServiceException.MNU_WRONG_PROVINCE)
                    .setMsgFormat("{1}: {0} The manufacture {2} can''t be created in the province {3}. Actual: {4}. Expected: {5}.")
                    .setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, type, province, euProv.getIncome(), "5+"));

            if (otherMnu) {
                // Placing a manufacture in the same province than another is only possible if all other controlled provinces have
                // already one. This rule should only apply to holland usually.

                Set<String> provinces = economicalSheetDao.getOwnedAndControlledProvinces(country.getName(), game.getId()).keySet();
                List<String> provincesWithMnu = game.getStacks().stream().flatMap(stackEntity -> stackEntity.getCounters().stream())
                        .filter(counter -> StringUtils.equals(country.getName(), counter.getCountry()) && EconomicUtil.isManufacture(counter.getType()))
                        .map(counter -> counter.getOwner().getProvince())
                        .collect(Collectors.toList());

                provinces.removeAll(provincesWithMnu);
                failIfFalse(new CheckForThrow<Boolean>().setTest(provinces.isEmpty()).setCodeError(IConstantsServiceException.MNU_WRONG_PROVINCE)
                        .setMsgFormat("{1}: {0} The manufacture {2} can''t be created in the province {3}. Actual: {4}. Expected: {5}.")
                        .setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT, type, province, province, String.join(", ", provinces)));
            }
        }

        int column = getColumnForDomesticOperation(request, country);

        int bonus = getBonusForDomesticOperation(game, country);

        AdministrativeActionEntity admAct = new AdministrativeActionEntity();
        admAct.setCountry(country);
        admAct.setStatus(AdminActionStatusEnum.PLANNED);
        admAct.setTurn(game.getTurn());
        admAct.setProvince(province);
        admAct.setCost(EconomicUtil.getAdminActionCost(request.getRequest().getType(), request.getRequest().getInvestment()));
        if (mnu != null) {
            admAct.setIdObject(mnu.getId());
        }
        admAct.setColumn(column);
        admAct.setBonus(bonus);

        return admAct;
    }

    /**
     * Computes the creation of a PLANNED administrative action of type DTI/FTI improve.
     *
     * @param request request containing the info about the action to create.
     * @param game    in which the action will be created.
     * @param country owner of the action.
     * @return the administrative action to create.
     * @throws FunctionalException Exception.
     */
    private AdministrativeActionEntity computeFtiDti(Request<AddAdminActionRequest> request, GameEntity game, PlayableCountryEntity country) throws FunctionalException {
        List<AdministrativeActionEntity> actions = adminActionDao.findAdminActions(country.getId(), game.getTurn(),
                null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI, AdminActionTypeEnum.DTI, AdminActionTypeEnum.EXL);

        failIfFalse(new CheckForThrow<Boolean>().setTest(actions.isEmpty()).setCodeError(IConstantsServiceException.ACTION_ALREADY_PLANNED)
                .setMsgFormat("{1}: {0} The administrative action of type {1} is already panned for the country {3}.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, AdminActionTypeEnum.MNU, country.getName()));

        failIfNull(new CheckForThrow<>().setTest(request.getRequest().getInvestment()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_INVESTMENT).setParams(METHOD_ADD_ADM_ACT));

        Map<LimitTypeEnum, Integer> maxi = getTables().getLimits().stream().filter(
                limit -> StringUtils.equals(limit.getCountry(), country.getName()) &&
                        (limit.getType() == LimitTypeEnum.MAX_DTI ||
                                limit.getType() == LimitTypeEnum.MAX_FTI ||
                                limit.getType() == LimitTypeEnum.MAX_FTI_ROTW) &&
                        limit.getPeriod().getBegin() <= game.getTurn() &&
                        limit.getPeriod().getEnd() >= game.getTurn()).collect(Collectors
                .groupingBy(Limit::getType, Collectors.summingInt(Limit::getNumber)));

        boolean ok;
        int actual;
        int max;

        if (request.getRequest().getType() == AdminActionTypeEnum.DTI) {
            actual = country.getDti();
            max = maxi.get(LimitTypeEnum.MAX_DTI);
            ok = actual < max;
        } else {
            actual = country.getFti();
            max = maxi.get(LimitTypeEnum.MAX_FTI);
            ok = actual < max;
            if (!ok && maxi.get(LimitTypeEnum.MAX_FTI_ROTW) != null) {
                ok = country.getFtiRotw() < maxi.get(LimitTypeEnum.MAX_FTI_ROTW);
            }
        }

        failIfFalse(new CheckForThrow<Boolean>().setTest(ok).setCodeError(IConstantsServiceException.ADMIN_ACTION_LIMIT_EXCEED)
                .setMsgFormat("{1}: {0} The administrative action of type {2} for the country {3} cannot be planned because country limits were exceeded ({4}/{5}).")
                .setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, request.getRequest().getType(), country.getName(), actual, max));


        int column = getColumnForDomesticOperation(request, country);

        int bonus = getBonusForDomesticOperation(game, country);

        AdministrativeActionEntity admAct = new AdministrativeActionEntity();
        admAct.setCountry(country);
        admAct.setStatus(AdminActionStatusEnum.PLANNED);
        admAct.setTurn(game.getTurn());
        admAct.setCost(EconomicUtil.getAdminActionCost(request.getRequest().getType(), request.getRequest().getInvestment()));
        admAct.setColumn(column);
        admAct.setBonus(bonus);

        return admAct;
    }

    /**
     * Compute the column for a domestic operation.
     *
     * @param request of the domestic operation.
     * @param country of the domestic operation.
     * @return the column.
     */
    private int getColumnForDomesticOperation(Request<AddAdminActionRequest> request, PlayableCountryEntity country) {
        int adm = oeUtil.getAdministrativeValue(country);
        int column = adm + country.getDti() - 9;

        // threshold to -4/4
        column = Math.min(Math.max(column, -4), 4);
        column += EconomicUtil.getAdminActionColumnBonus(request.getRequest().getType(), request.getRequest().getInvestment());
        // threshold to -4/4
        column = Math.min(Math.max(column, -4), 4);
        return column;
    }

    /**
     * Compute the bonus for a domestic operation.
     *
     * @param game    to retrieve global information such as stability, inflation,...
     * @param country of the domestic operation.
     * @return the bonus.
     */
    private int getBonusForDomesticOperation(GameEntity game, PlayableCountryEntity country) {
        int stab = oeUtil.getStability(game, country.getName());
        int bonus = stab;
        if (StringUtils.equals(PlayableCountry.SPAIN, country.getName())) {
            CounterEntity inflationCounter = CommonUtil.findFirst(game.getStacks().stream().filter(stack -> GameUtil.isInflationBox(stack.getProvince()))
                            .flatMap(stack -> stack.getCounters().stream()),
                    counter -> counter.getType() == CounterFaceTypeEnum.INFLATION || counter.getType() == CounterFaceTypeEnum.INFLATION_GOLD);
            if (inflationCounter != null) {
                int inflation = GameUtil.getInflation(inflationCounter.getOwner().getProvince(), false);
                if (inflation >= 10) {
                    bonus -= 1;
                }
            }
        } else if (StringUtils.equals(PlayableCountry.RUSSIA, country.getName()) ||
                StringUtils.equals(PlayableCountry.POLAND, country.getName()) ||
                StringUtils.equals(PlayableCountry.TURKEY, country.getName())) {
            // TODO St-Petersburg
            bonus -= 1;
        } else if (StringUtils.equals(PlayableCountry.ENGLAND, country.getName()) && game.getTurn() >= 43) {
            bonus += 2;
        }
        return bonus;
    }

    /**
     * Computes the creation of a PLANNED administrative action of type Exceptional taxes.
     *
     * @param request request containing the info about the action to create.
     * @param game    in which the action will be created.
     * @param country owner of the action.
     * @return the administrative action to create.
     * @throws FunctionalException Exception.
     */
    private AdministrativeActionEntity computeExceptionalTaxes(Request<AddAdminActionRequest> request, GameEntity game, PlayableCountryEntity country) throws FunctionalException {
        List<AdministrativeActionEntity> actions = adminActionDao.findAdminActions(country.getId(), game.getTurn(),
                null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI, AdminActionTypeEnum.DTI, AdminActionTypeEnum.EXL);

        failIfFalse(new CheckForThrow<Boolean>().setTest(actions.isEmpty()).setCodeError(IConstantsServiceException.ACTION_ALREADY_PLANNED)
                .setMsgFormat("{1}: {0} The administrative action of type {1} is already panned for the country {3}.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, AdminActionTypeEnum.MNU, country.getName()));

        // TODO war

        // TODO war if ennemy stack on national territory, no loss of stab nor condition

        int stab = oeUtil.getStability(game, country.getName());

        failIfFalse(new CheckForThrow<Boolean>().setTest(stab > -3).setCodeError(IConstantsServiceException.INSUFICIENT_STABILITY)
                .setMsgFormat("{1}: {0} The stability of the country {1} is too low. Actual: {2}, minimum: {3}.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, country.getName(), stab, -2));

        // TODO war no loss of stab
        stab--;

        int adm = oeUtil.getAdministrativeValue(country);

        int bonus = adm + stab * 3;

        AdministrativeActionEntity admAct = new AdministrativeActionEntity();
        admAct.setCountry(country);
        admAct.setStatus(AdminActionStatusEnum.PLANNED);
        admAct.setTurn(game.getTurn());
        admAct.setBonus(bonus);

        return admAct;
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

        failIfFalse(new CheckForThrow<Boolean>().setTest(action.getStatus() == AdminActionStatusEnum.PLANNED).setCodeError(IConstantsServiceException.ACTION_NOT_PLANNED)
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
