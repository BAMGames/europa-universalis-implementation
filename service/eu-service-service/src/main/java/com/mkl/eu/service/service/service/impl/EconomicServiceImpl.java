package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.common.vo.SimpleRequest;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.IEconomicService;
import com.mkl.eu.client.service.service.eco.*;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.util.EconomicUtil;
import com.mkl.eu.client.service.util.GameUtil;
import com.mkl.eu.client.service.util.MaintenanceUtil;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.ref.IReferentielConstants;
import com.mkl.eu.client.service.vo.tables.*;
import com.mkl.eu.service.service.domain.ICounterDomain;
import com.mkl.eu.service.service.mapping.eco.EconomicalSheetMapping;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.board.IStackDao;
import com.mkl.eu.service.service.persistence.country.IPlayableCountryDao;
import com.mkl.eu.service.service.persistence.eco.IAdminActionDao;
import com.mkl.eu.service.service.persistence.eco.IEconomicalSheetDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.OtherForcesEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.DiscoveryEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.eco.AdministrativeActionEntity;
import com.mkl.eu.service.service.persistence.oe.eco.EconomicalSheetEntity;
import com.mkl.eu.service.service.persistence.oe.eco.TradeFleetEntity;
import com.mkl.eu.service.service.persistence.oe.ref.country.CountryEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.*;
import com.mkl.eu.service.service.persistence.ref.ICountryDao;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.service.GameDiffsInfo;
import com.mkl.eu.service.service.util.IOEUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
    /** Constant used in the costs Map to retrieve the cost for unit purchase. */
    private static final String COST_UNIT_PURCHASE = "unitPurchase";
    /** Constant used in the costs Map to retrieve the cost for fort purchase. */
    private static final String COST_FORT_PURCHASE = "fortPurchase";
    /** Constant used in the costs Map to retrieve the cost for other. */
    private static final String COST_OTHER = "other";
    /** Constant used in the costs Map to retrieve the cost for administrative actions. */
    private static final String COST_ACTION = "action";
    /** Constant used in the costs Map to retrieve the cost for exceptional levies (even if it is not a cost, or not always). */
    private static final String COST_EXC_LEVIES = "excLevies";
    /** Counter Domain. */
    @Autowired
    private ICounterDomain counterDomain;
    /** EconomicalSheet DAO. */
    @Autowired
    private IEconomicalSheetDao economicalSheetDao;
    /** AdminAction DAO. */
    @Autowired
    private IAdminActionDao adminActionDao;
    /** Country DAO. */
    @Autowired
    private IPlayableCountryDao playableCountryDao;
    /** Counter DAO. */
    @Autowired
    private ICounterDao counterDao;
    /** Stack DAO. */
    @Autowired
    private IStackDao stackDao;
    /** Province DAO. */
    @Autowired
    private IProvinceDao provinceDao;
    /** Country DAO. */
    @Autowired
    private ICountryDao countryDao;
    /** Game mapping. */
    @Autowired
    private EconomicalSheetMapping ecoSheetsMapping;
    /** OEUtil. */
    @Autowired
    private IOEUtil oeUtil;

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
            computeEconomicalSheet(country, game.getId(), game.getTurn(), tradeCenters);
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
     * @param idGame       id of the game.
     * @param turn         turn of the game.
     * @param tradeCenters the trade centers and their owners.
     */
    protected void computeEconomicalSheet(PlayableCountryEntity country, Long idGame, Integer turn, Map<String, List<CounterFaceTypeEnum>> tradeCenters) {
        EconomicalSheetEntity sheet = CommonUtil.findFirst(country.getEconomicalSheets(), economicalSheetEntity -> economicalSheetEntity.getTurn().equals(turn));
        if (sheet == null) {
            sheet = new EconomicalSheetEntity();
            sheet.setCountry(country);
            sheet.setTurn(turn);

            economicalSheetDao.create(sheet);

            country.getEconomicalSheets().add(sheet);
        }

        String name = country.getName();

        Map<String, Integer> allProvinces = new HashMap<>();

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

        allProvinces.putAll(provinces);
        allProvinces.putAll(vassalProvinces);

        Integer pillagedIncome = pillagedProvinces.stream().collect(Collectors.summingInt(allProvinces::get));

        sheet.setPillages(pillagedIncome);

        Integer sum = CommonUtil.add(sheet.getProvincesIncome(), sheet.getVassalIncome(), sheet.getEventLandIncome());
        if (sheet.getPillages() != null) {
            sum -= sheet.getPillages();
        }
        sheet.setLandIncome(sum);

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

        sum = CommonUtil.add(sheet.getDomTradeIncome(), sheet.getForTradeIncome(), sheet.getFleetLevelIncome(), sheet.getFleetMonopIncome(), sheet.getTradeCenterIncome());
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
            case EXL:
                admAct = computeExceptionalTaxes(request, game, country);
                break;
            case COL:
                admAct = computeColonisation(request, game, country);
                break;
            case TP:
                admAct = computeTradingPost(request, game, country);
                break;
            case ELT:
                admAct = computeTechnology(request, game, country, true);
                break;
            case ENT:
                admAct = computeTechnology(request, game, country, false);
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
            failIfFalse(new CheckForThrow<Boolean>().setTest(CounterUtil.isLandArmy(counter.getType())).setCodeError(IConstantsServiceException.COUNTER_CANT_MAINTAIN_LOW)
                    .setMsgFormat("{1}: {0} The counter {2} has the type {3} which cannot be maintained low.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_ID_OBJECT).setParams(METHOD_ADD_ADM_ACT, request.getRequest().getIdObject(), counter.getType()));
        } else if (request.getRequest().getType() == AdminActionTypeEnum.DIS) {
            failIfFalse(new CheckForThrow<Boolean>().setTest(CounterUtil.isArmy(counter.getType()) || CounterUtil.isFortress(counter.getType())).setCodeError(IConstantsServiceException.COUNTER_CANT_DISBAND)
                    .setMsgFormat("{1}: {0} The counter {2} has the type {3} which cannot be disbanded.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_ID_OBJECT).setParams(METHOD_ADD_ADM_ACT, request.getRequest().getIdObject(), counter.getType()));
        } else if (request.getRequest().getType() == AdminActionTypeEnum.LF) {
            failIfNull(new CheckForThrow<>().setTest(request.getRequest().getCounterFaceType()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                    .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_COUNTER_FACE_TYPE).setParams(METHOD_ADD_ADM_ACT));
            failIfFalse(new CheckForThrow<Boolean>().setTest(CounterUtil.isFortress(counter.getType())).setCodeError(IConstantsServiceException.COUNTER_CANT_LOWER_FORTRESS)
                    .setMsgFormat("{1}: {0} The counter {2} has the type {3} which cannot be lower fortress.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_ID_OBJECT).setParams(METHOD_ADD_ADM_ACT, request.getRequest().getIdObject(), counter.getType()));

            String province = counter.getOwner().getProvince();
            AbstractProvinceEntity prov = provinceDao.getProvinceByName(province);

            int naturalLevel = 0;
            if (prov instanceof EuropeanProvinceEntity) {
                if (((EuropeanProvinceEntity) prov).getFortress() != null) {
                    naturalLevel = ((EuropeanProvinceEntity) prov).getFortress();
                }
            }

            int actualLevel = CounterUtil.getFortressLevelFromType(counter.getType());
            int desiredLevel = CounterUtil.getFortressLevelFromType(request.getRequest().getCounterFaceType());

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

        boolean isFortress = CounterUtil.isFortress(faceType);

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
            faceConsistent |= CounterUtil.isArmy(faceType);
        } else {
            faceConsistent |= CounterUtil.isLandArmy(faceType);
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
        CounterEntity fortressCounter = CommonUtil.findFirst(stacks.stream().flatMap(stack -> stack.getCounters().stream()),
                counter -> StringUtils.equals(country.getName(), counter.getCountry()) && CounterUtil.isFortress(counter.getType()));
        if (fortressCounter != null) {
            actualLevel = CounterUtil.getFortressLevelFromType(fortressCounter.getType());
        }

        int desiredLevel = CounterUtil.getFortressLevelFromType(faceType);


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
            canPurchaseFortress = actualTech.compareTo(targetTech) >= 0;
        }
        if (desiredLevel == 5) {
            canPurchaseFortress = game.getTurn() >= 40;
        }

        failIfFalse(new CheckForThrow<Boolean>().setTest(desiredLevel == actualLevel + 1).setCodeError(IConstantsServiceException.COUNTER_CANT_PURCHASE)
                .setMsgFormat("{1}: {0} The counter face type {2} cannot be purchased on province {3}.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_COUNTER_FACE_TYPE).setParams(METHOD_ADD_ADM_ACT, faceType, prov.getName()));


        failIfFalse(new CheckForThrow<Boolean>().setTest(canPurchaseFortress).setCodeError(IConstantsServiceException.FORTRESS_CANT_PURCHASE)
                .setMsgFormat("{1}: {0} The fortress {2} cannot be purchased because actual technology is {3}.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_COUNTER_FACE_TYPE).setParams(METHOD_ADD_ADM_ACT, faceType, country.getLandTech()));

        Integer fortressCost = 25 * (CounterUtil.getFortressLevelFromType(faceType) - 1);
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
        boolean land = CounterUtil.isLandArmy(faceType);
        Integer plannedSize;
        final LimitTypeEnum limitType;

        List<AdministrativeActionEntity> actions = adminActionDao.findAdminActions(country.getId(), game.getTurn(),
                null, AdminActionTypeEnum.PU);
        if (land) {
            plannedSize = actions.stream().filter(action -> CounterUtil.isLandArmy(action.getCounterFaceType())).collect(Collectors.summingInt(action -> CounterUtil.getSizeFromType(action.getCounterFaceType())));
            limitType = LimitTypeEnum.PURCHASE_LAND_TROOPS;
        } else {
            plannedSize = actions.stream().filter(action -> CounterUtil.isNavalArmy(action.getCounterFaceType())).collect(Collectors.summingInt(action -> CounterUtil.getSizeFromType(action.getCounterFaceType())));
            limitType = LimitTypeEnum.PURCHASE_NAVAL_TROOPS;
        }

        Integer size = CounterUtil.getSizeFromType(faceType);
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

        //noinspection ConstantConditions
        TradeZoneProvinceEntity tradeZone = (TradeZoneProvinceEntity) prov;
        if (Arrays.binarySearch(IReferentielConstants.TRADE_ZONES_EUROPE, prov.getName()) < 0) {
            List<String> countries = adminActionDao.getCountriesTradeFleetAccessRotw(tradeZone.getSeaZone(), game.getId());
            // TODO trade rights taken
            if (!countries.contains(country.getName())) {
                boolean right = false;
                if (Arrays.binarySearch(IReferentielConstants.TRADE_ZONES_TRADE, prov.getName()) > +0) {
                    SeaProvinceEntity sea = (SeaProvinceEntity) provinceDao.getProvinceByName(tradeZone.getSeaZone());
                    List<String> discovers = country.getDiscoveries().stream().filter(d -> d.getStack() == null && d.getTurn() != null)
                            .map(DiscoveryEntity::getProvince).collect(Collectors.toList());
                    long miss = sea.getBorders().stream().filter(border -> border.getProvinceTo() instanceof SeaProvinceEntity &&
                            discovers.contains(border.getProvinceTo().getName()))
                            .count();

                    right = miss == 0 && discovers.contains(tradeZone.getSeaZone());
                }

                failIfFalse(new CheckForThrow<Boolean>().setTest(right).setCodeError(IConstantsServiceException.TRADE_FLEET_ACCESS_ROTW)
                        .setMsgFormat("{1}: {0} The country {3} can''t implant a trade fleet located in {2} because of rotw access limitation.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT, province, country.getName()));
            }
        } else if (StringUtils.equals(IReferentielConstants.TRADE_ZONE_CASPIAN, prov.getName())) {
            List<String> owners = counterDao.getNeighboringOwners(tradeZone.getSeaZone(), game.getId());
            owners.add(game.getMedCommCenterOwner());
            owners.add(game.getOrientCommCenterOwner());
            boolean right = owners.contains(country.getName());

            failIfFalse(new CheckForThrow<Boolean>().setTest(right).setCodeError(IConstantsServiceException.TRADE_FLEET_ACCESS_CASPIAN)
                    .setMsgFormat("{1}: {0} The country {3} can''t implant a trade fleet located in {2} because of caspian access limitation.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT, province, country.getName()));
        }

        TradeFleetEntity tradeFleet = CommonUtil.findFirst(game.getTradeFleets(), tradeFleetEntity -> StringUtils.equals(province, tradeFleetEntity.getProvince())
                && StringUtils.equals(country.getName(), tradeFleetEntity.getCountry()));

        failIfFalse(new CheckForThrow<Boolean>().setTest(tradeFleet == null || tradeFleet.getLevel() < 6).setCodeError(IConstantsServiceException.TRADE_FLEET_FULL)
                .setMsgFormat("{1}: {0} The trade fleet located in {2} and owned by {3} is already full.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT, province, country.getName()));

        Integer column = country.getFti();
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

        failIfFalse(new CheckForThrow<Boolean>().setTest(CounterUtil.isManufacture(type)).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
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
                        || CounterUtil.isManufacture(counter.getType())).collect(Collectors.toList());
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
            } else if (CounterUtil.isManufacture(counter.getType())) {
                otherMnu = true;
            }
        }

        // If the MNU is already max, then it is the creation of a new MNU alongside the other one
        if (mnu != null && (CounterUtil.getManufactureLevel(mnu.getType()) == 2)) {
            mnu = null;
            otherMnu = true;
        }

        boolean provinceOk = StringUtils.equals(country.getName(), owner) && StringUtils.equals(country.getName(), control);

        failIfFalse(new CheckForThrow<Boolean>().setTest(provinceOk).setCodeError(IConstantsServiceException.PROVINCE_NOT_OWN_CONTROL)
                .setMsgFormat("{1}: {0} The province {2} is not owned and controlled by {3}.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT, province, country.getName()));

        // New MNU ? Check placement and limits
        if (mnu == null) {
            Integer actualMnus = game.getStacks().stream().flatMap(stackEntity -> stackEntity.getCounters().stream())
                    .filter(counterEntity -> StringUtils.equals(country.getName(), counterEntity.getCountry()) && CounterUtil.isManufacture(counterEntity.getType())).collect(Collectors.summingInt(value -> 1));
            Integer maxMnus = getTables().getLimits().stream().filter(
                    limit -> StringUtils.equals(limit.getCountry(), country.getName()) &&
                            limit.getType() == LimitTypeEnum.MAX_MNU &&
                            limit.getPeriod().getBegin() <= game.getTurn() &&
                            limit.getPeriod().getEnd() >= game.getTurn()).collect(Collectors.summingInt(Limit::getNumber));

            failIfFalse(new CheckForThrow<Boolean>().setTest(actualMnus < maxMnus + 2).setCodeError(IConstantsServiceException.ADMIN_ACTION_LIMIT_EXCEED)
                    .setMsgFormat(MSG_COUNTER_LIMIT_EXCEED)
                    .setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, type, country.getName(), actualMnus, maxMnus));

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
                        .filter(counter -> StringUtils.equals(country.getName(), counter.getCountry()) && CounterUtil.isManufacture(counter.getType()))
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
        int bonus = oeUtil.getStability(game, country.getName());
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
        } else if ((StringUtils.equals(PlayableCountry.RUSSIA, country.getName()) && StringUtils.isEmpty(game.getStPeterProvince())) ||
                StringUtils.equals(PlayableCountry.POLAND, country.getName()) ||
                StringUtils.equals(PlayableCountry.TURKEY, country.getName())) {
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

        failIfFalse(new CheckForThrow<Boolean>().setTest(stab > -3).setCodeError(IConstantsServiceException.INSUFFICIENT_STABILITY)
                .setMsgFormat("{1}: {0} The stability of the country {2} is too low. Actual: {3}, minimum: {4}.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, country.getName(), stab, -2));

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

    /**
     * Computes the creation of a PLANNED administrative action of type Colonisation.
     *
     * @param request request containing the info about the action to create.
     * @param game    in which the action will be created.
     * @param country owner of the action.
     * @return the administrative action to create.
     * @throws FunctionalException Exception.
     */
    private AdministrativeActionEntity computeColonisation(Request<AddAdminActionRequest> request, GameEntity game, PlayableCountryEntity country) throws FunctionalException {
        List<AdministrativeActionEntity> actions = adminActionDao.findAdminActions(country.getId(), game.getTurn(),
                null, AdminActionTypeEnum.COL);

        Integer plannedCols = actions.stream().collect(Collectors.summingInt(action -> 1));
        Integer maxCols = getTables().getLimits().stream().filter(
                limit -> StringUtils.equals(limit.getCountry(), country.getName()) &&
                        limit.getType() == LimitTypeEnum.ACTION_COL &&
                        limit.getPeriod().getBegin() <= game.getTurn() &&
                        limit.getPeriod().getEnd() >= game.getTurn()).collect(Collectors.summingInt(Limit::getNumber));

        failIfFalse(new CheckForThrow<Boolean>().setTest(plannedCols < maxCols).setCodeError(IConstantsServiceException.ADMIN_ACTION_LIMIT_EXCEED)
                .setMsgFormat("{1}: {0} The administrative action of type {2} for the country {3} cannot be planned because country limits were exceeded ({4}/{5}).").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, LimitTypeEnum.ACTION_COL, country.getName(), plannedCols, maxCols));

        failIfNull(new CheckForThrow<>().setTest(request.getRequest().getInvestment()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_INVESTMENT).setParams(METHOD_ADD_ADM_ACT));

        String province = request.getRequest().getProvince();
        failIfEmpty(new CheckForThrow<String>().setTest(province).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT));

        AbstractProvinceEntity prov = provinceDao.getProvinceByName(province);

        failIfNull(new CheckForThrow<>().setTest(prov).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT, province));

        failIfFalse(new CheckForThrow<Boolean>().setTest(prov instanceof RotwProvinceEntity).setCodeError(IConstantsServiceException.PROVINCE_WRONG_TYPE)
                .setMsgFormat("{1}: {0} The province {2} of type {3} should be of type {4}.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT, province, prov.getClass(), RotwProvinceEntity.class));

        List<CounterEntity> counters = game.getStacks().stream().filter(stack -> StringUtils.equals(province, stack.getProvince())).flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> counter.getType() == CounterFaceTypeEnum.COLONY_MINUS
                        || counter.getType() == CounterFaceTypeEnum.COLONY_PLUS || CounterUtil.isArsenal(counter.getType())
                        || counter.getType() == CounterFaceTypeEnum.MISSION).collect(Collectors.toList());

        CounterEntity colony = null;
        boolean arsenal = false;
        boolean mission = false;
        for (CounterEntity counter : counters) {
            if (counter.getType() == CounterFaceTypeEnum.COLONY_MINUS || counter.getType() == CounterFaceTypeEnum.COLONY_PLUS) {
                colony = counter;
            } else if (counter.getType() == CounterFaceTypeEnum.MISSION) {
                mission = true;
            } else if (CounterUtil.isArsenal(counter.getType())) {
                arsenal = true;
            }
        }

        boolean provinceOk = colony == null || StringUtils.equals(country.getName(), colony.getCountry());

        failIfFalse(new CheckForThrow<Boolean>().setTest(provinceOk).setCodeError(IConstantsServiceException.PROVINCE_NOT_OWN_CONTROL)
                .setMsgFormat("{1}: {0} The colony located in {2} is not owned by {3}.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT, province, country.getName()));

        if (colony == null) {
            Integer cols = game.getStacks().stream().flatMap(stack -> stack.getCounters().stream())
                    .filter(counter -> StringUtils.equals(country.getName(), counter.getCountry())
                            && (counter.getType() == CounterFaceTypeEnum.COLONY_MINUS || counter.getType() == CounterFaceTypeEnum.COLONY_PLUS))
                    .collect(Collectors.summingInt(c -> 1));
            Integer maxColCounters = getTables().getLimits().stream().filter(
                    limit -> StringUtils.equals(limit.getCountry(), country.getName()) &&
                            limit.getType() == LimitTypeEnum.MAX_COL &&
                            limit.getPeriod().getBegin() <= game.getTurn() &&
                            limit.getPeriod().getEnd() >= game.getTurn()).collect(Collectors.summingInt(Limit::getNumber));

            failIfFalse(new CheckForThrow<Boolean>().setTest(cols < maxColCounters).setCodeError(IConstantsServiceException.COUNTER_LIMIT_EXCEED)
                    .setMsgFormat(MSG_COUNTER_LIMIT_EXCEED).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, CounterFaceTypeEnum.COLONY_MINUS, country.getName(), cols, maxColCounters));

        }

        //noinspection ConstantConditions
        RotwProvinceEntity rotwProv = (RotwProvinceEntity) prov;

        Period period = CommonUtil.findFirst(getTables().getPeriods(), per -> per.getBegin() <= game.getTurn() && per.getEnd() >= game.getTurn());
        if (period != null && period.getName().compareTo(Period.PERIOD_V) <= 0 && colony != null) {
            GoldEntity gold = provinceDao.getGoldInProvince(province);
            int level = 0;
            if (colony.getEstablishment() != null && colony.getEstablishment().getLevel() != null) {
                level = colony.getEstablishment().getLevel();
            }
            provinceOk = level < 2 || gold != null || mission || arsenal;

            failIfFalse(new CheckForThrow<Boolean>().setTest(provinceOk).setCodeError(IConstantsServiceException.PIONEERING)
                    .setMsgFormat("{1}: {0} The colony located in {2} and of level {3} can't be upgraded because of pioneering rules.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT, province, level));
            // TODO countries and economical events rules
        }

        checkSettlements(game, country, rotwProv);

        if (colony != null) {
            boolean sea = false;
            for (BorderEntity border : rotwProv.getBorders()) {
                if (border.getProvinceTo().getTerrain() == TerrainEnum.SEA) {
                    sea = true;
                    break;
                }
            }

            if (!sea) {
                List<String> countries = adminActionDao.getCountriesInlandAdvance(rotwProv.getName(), game.getId());

                provinceOk = countries.contains(country.getName());
                failIfFalse(new CheckForThrow<Boolean>().setTest(provinceOk).setCodeError(IConstantsServiceException.INLAND_ADVANCE)
                        .setMsgFormat("{1}: {0} The colony located in {2} can't be improved because of inland advance rules.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT, rotwProv.getName()));
            }
        }

        // TODO Native empires, VI.7.4.4

        RegionEntity region = provinceDao.getRegionByName(rotwProv.getRegion());
        Integer column = country.getFtiRotw() - region.getDifficulty();

        // threshold to -4/4
        column = Math.min(Math.max(column, -4), 4);
        column += EconomicUtil.getAdminActionColumnBonus(request.getRequest().getType(), request.getRequest().getInvestment());
        // threshold to -4/4
        column = Math.min(Math.max(column, -4), 4);

        int bonus = -country.getColonisationPenalty();
        if (colony != null) {
            bonus += 2;
        }
        OtherForcesEntity natives = CommonUtil.findFirst(game.getOtherForces(),
                o -> o.getType() == OtherForcesTypeEnum.NATIVES && StringUtils.equals(province, o.getProvince()));
        if (natives != null && !natives.isReplenish() && natives.getNbLd() == 0 && natives.getNbLde() == 0) {
            bonus += 2;
        }
        // TODO battles
        // TODO leaders

        AdministrativeActionEntity admAct = new AdministrativeActionEntity();
        admAct.setCountry(country);
        admAct.setStatus(AdminActionStatusEnum.PLANNED);
        admAct.setTurn(game.getTurn());
        admAct.setProvince(province);
        if (colony != null) {
            admAct.setIdObject(colony.getId());
        }
        admAct.setCost(EconomicUtil.getAdminActionCost(request.getRequest().getType(), request.getRequest().getInvestment()));
        admAct.setColumn(column);
        admAct.setBonus(bonus);

        return admAct;
    }

    /**
     * Computes the creation of a PLANNED administrative action of type Trading post.
     *
     * @param request request containing the info about the action to create.
     * @param game    in which the action will be created.
     * @param country owner of the action.
     * @return the administrative action to create.
     * @throws FunctionalException Exception.
     */
    private AdministrativeActionEntity computeTradingPost(Request<AddAdminActionRequest> request, GameEntity game, PlayableCountryEntity country) throws FunctionalException {
        List<AdministrativeActionEntity> actions = adminActionDao.findAdminActions(country.getId(), game.getTurn(),
                null, AdminActionTypeEnum.TP);

        Integer plannedCols = actions.stream().collect(Collectors.summingInt(action -> 1));
        Integer maxCols = getTables().getLimits().stream().filter(
                limit -> StringUtils.equals(limit.getCountry(), country.getName()) &&
                        limit.getType() == LimitTypeEnum.ACTION_TP &&
                        limit.getPeriod().getBegin() <= game.getTurn() &&
                        limit.getPeriod().getEnd() >= game.getTurn()).collect(Collectors.summingInt(Limit::getNumber));

        failIfFalse(new CheckForThrow<Boolean>().setTest(plannedCols < maxCols).setCodeError(IConstantsServiceException.ADMIN_ACTION_LIMIT_EXCEED)
                .setMsgFormat("{1}: {0} The administrative action of type {2} for the country {3} cannot be planned because country limits were exceeded ({4}/{5}).").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, LimitTypeEnum.ACTION_COL, country.getName(), plannedCols, maxCols));

        failIfNull(new CheckForThrow<>().setTest(request.getRequest().getInvestment()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_INVESTMENT).setParams(METHOD_ADD_ADM_ACT));

        String province = request.getRequest().getProvince();
        failIfEmpty(new CheckForThrow<String>().setTest(province).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT));

        AbstractProvinceEntity prov = provinceDao.getProvinceByName(province);

        failIfNull(new CheckForThrow<>().setTest(prov).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT, province));

        failIfFalse(new CheckForThrow<Boolean>().setTest(prov instanceof RotwProvinceEntity).setCodeError(IConstantsServiceException.PROVINCE_WRONG_TYPE)
                .setMsgFormat("{1}: {0} The province {2} of type {3} should be of type {4}.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT, province, prov.getClass(), RotwProvinceEntity.class));

        List<CounterEntity> counters = game.getStacks().stream().filter(stack -> StringUtils.equals(province, stack.getProvince())).flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> counter.getType() == CounterFaceTypeEnum.COLONY_MINUS || counter.getType() == CounterFaceTypeEnum.COLONY_PLUS
                        || counter.getType() == CounterFaceTypeEnum.TRADING_POST_MINUS || counter.getType() == CounterFaceTypeEnum.TRADING_POST_PLUS)
                .collect(Collectors.toList());

        CounterEntity colony = null;
        CounterEntity tp = null;
        for (CounterEntity counter : counters) {
            if (counter.getType() == CounterFaceTypeEnum.COLONY_MINUS || counter.getType() == CounterFaceTypeEnum.COLONY_PLUS) {
                colony = counter;
            } else if ((counter.getType() == CounterFaceTypeEnum.TRADING_POST_MINUS || counter.getType() == CounterFaceTypeEnum.TRADING_POST_PLUS)
                    && StringUtils.equals(country.getName(), counter.getCountry())) {
                tp = counter;
            }
        }

        if (tp == null) {
            Integer tps = game.getStacks().stream().flatMap(stack -> stack.getCounters().stream())
                    .filter(counter -> StringUtils.equals(country.getName(), counter.getCountry())
                            && (counter.getType() == CounterFaceTypeEnum.TRADING_POST_MINUS || counter.getType() == CounterFaceTypeEnum.TRADING_POST_PLUS))
                    .collect(Collectors.summingInt(c -> 1));
            Integer maxTpCounters = getTables().getLimits().stream().filter(
                    limit -> StringUtils.equals(limit.getCountry(), country.getName()) &&
                            limit.getType() == LimitTypeEnum.MAX_TP &&
                            limit.getPeriod().getBegin() <= game.getTurn() &&
                            limit.getPeriod().getEnd() >= game.getTurn()).collect(Collectors.summingInt(Limit::getNumber));

            failIfFalse(new CheckForThrow<Boolean>().setTest(tps < maxTpCounters).setCodeError(IConstantsServiceException.COUNTER_LIMIT_EXCEED)
                    .setMsgFormat(MSG_COUNTER_LIMIT_EXCEED).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, CounterFaceTypeEnum.TRADING_POST_MINUS, country.getName(), tps, maxTpCounters));

        }

        boolean provinceOk = colony == null || StringUtils.equals(country.getName(), colony.getCountry());

        failIfFalse(new CheckForThrow<Boolean>().setTest(provinceOk).setCodeError(IConstantsServiceException.PROVINCE_NOT_OWN_CONTROL)
                .setMsgFormat("{1}: {0} The colony located in {2} is not owned by {3}.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT, province, country.getName()));

        //noinspection ConstantConditions
        RotwProvinceEntity rotwProv = (RotwProvinceEntity) prov;

        checkSettlements(game, country, rotwProv);

        // TODO Native empires, VI.7.4.4

        RegionEntity region = provinceDao.getRegionByName(rotwProv.getRegion());
        int tolerance = region.getTolerance();
        if (tolerance == 0) {
            tolerance = region.getDifficulty();
        }
        Integer column = country.getFtiRotw() - tolerance;

        // threshold to -4/4
        column = Math.min(Math.max(column, -4), 4);
        column += EconomicUtil.getAdminActionColumnBonus(request.getRequest().getType(), request.getRequest().getInvestment());
        // threshold to -4/4
        column = Math.min(Math.max(column, -4), 4);

        int bonus = -adminActionDao.countOtherTpsInRegion(country.getName(), rotwProv.getRegion(), game.getId());
        long otherForces = game.getStacks().stream().filter(s -> StringUtils.equals(province, s.getProvince()))
                .flatMap(s -> s.getCounters().stream())
                .filter(c -> !StringUtils.equals(country.getName(), c.getCountry()) && CounterUtil.isForce(c.getType())).count();
        if (otherForces > 0) {
            bonus -= 1;
        }
        OtherForcesEntity natives = CommonUtil.findFirst(game.getOtherForces(),
                o -> o.getType() == OtherForcesTypeEnum.NATIVES && StringUtils.equals(province, o.getProvince()));
        if (natives != null && !natives.isReplenish() && natives.getNbLd() == 0 && natives.getNbLde() == 0) {
            bonus += 2;
        }
        // TODO battles
        // TODO leaders

        AdministrativeActionEntity admAct = new AdministrativeActionEntity();
        admAct.setCountry(country);
        admAct.setStatus(AdminActionStatusEnum.PLANNED);
        admAct.setTurn(game.getTurn());
        admAct.setProvince(province);
        if (tp != null) {
            admAct.setIdObject(tp.getId());
        }
        admAct.setCost(EconomicUtil.getAdminActionCost(request.getRequest().getType(), request.getRequest().getInvestment()));
        admAct.setColumn(column);
        admAct.setBonus(bonus);

        return admAct;
    }

    /**
     * Check the settlement rules (VI.7.4.3).
     *
     * @param game     the game.
     * @param country  the country creating an establishment.
     * @param province where the establishment is.
     * @throws FunctionalException Functional error.
     */
    private void checkSettlements(GameEntity game, PlayableCountryEntity country, RotwProvinceEntity province) throws FunctionalException {
        List<String> discoveries = country.getDiscoveries().stream().filter(d -> d.getStack() == null && d.getTurn() != null).map(DiscoveryEntity::getProvince).collect(Collectors.toList());
        List<String> forts = new ArrayList<>();
        List<String> sources = playableCountryDao.getOwnedProvinces(country.getName(), game.getId());

        game.getStacks().stream().flatMap(s -> s.getCounters().stream())
                .filter(c -> StringUtils.equals(country.getName(), c.getCountry()) &&
                        (c.getType() == CounterFaceTypeEnum.TRADING_POST_PLUS ||
                                c.getType() == CounterFaceTypeEnum.TRADING_POST_MINUS ||
                                c.getType() == CounterFaceTypeEnum.COLONY_PLUS ||
                                c.getType() == CounterFaceTypeEnum.COLONY_MINUS ||
                                c.getType() == CounterFaceTypeEnum.FORT))
                .forEach(c -> {
                    if (c.getType() == CounterFaceTypeEnum.FORT) {
                        forts.add(c.getOwner().getProvince());
                    } else {
                        sources.add(c.getOwner().getProvince());
                    }
                });
        boolean provinceOk = oeUtil.canSettle(province, discoveries, sources, forts);
        failIfFalse(new CheckForThrow<Boolean>().setTest(provinceOk).setCodeError(IConstantsServiceException.SETTLEMENTS)
                .setMsgFormat("{1}: {0} The establishment located in {2} can't be settled because of settlements rules.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_ADD_ADM_ACT, province.getName()));
    }

    /**
     * Computes the creation of a PLANNED administrative action of type Technology enhancement.
     *
     * @param request request containing the info about the action to create.
     * @param game    in which the action will be created.
     * @param country owner of the action.
     * @param land    <code>true</code> if the technology to enhance is land, <code>false</code> otherwise.
     * @return the administrative action to create.
     * @throws FunctionalException Exception.
     */
    private AdministrativeActionEntity computeTechnology(Request<AddAdminActionRequest> request, GameEntity game, PlayableCountryEntity country, boolean land) throws FunctionalException {
        List<AdministrativeActionEntity> actions = adminActionDao.findAdminActions(country.getId(), game.getTurn(),
                null, AdminActionTypeEnum.ELT, AdminActionTypeEnum.ENT);

        boolean techAlreadyPlanned = false;
        boolean otherTechBigInvestment = false;
        Integer smallCost = EconomicUtil.getAdminActionCost(request.getRequest().getType(), InvestmentEnum.S);
        for (AdministrativeActionEntity action : actions) {
            if ((action.getType() == AdminActionTypeEnum.ELT && land) ||
                    (action.getType() == AdminActionTypeEnum.ENT && !land)) {
                techAlreadyPlanned = true;
            } else {
                otherTechBigInvestment = action.getCost() > smallCost;
            }
        }

        failIfTrue(new CheckForThrow<Boolean>().setTest(techAlreadyPlanned).setCodeError(IConstantsServiceException.ADMIN_ACTION_LIMIT_EXCEED)
                .setMsgFormat("{1}: {0} The administrative action of type {2} for the country {3} cannot be planned because country limits were exceeded ({4}/{5}).").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, request.getRequest().getType(), country.getName(), 1, 1));

        failIfNull(new CheckForThrow<>().setTest(request.getRequest().getInvestment()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_INVESTMENT).setParams(METHOD_ADD_ADM_ACT));

        failIfTrue(new CheckForThrow<Boolean>().setTest(request.getRequest().getInvestment() != InvestmentEnum.S && otherTechBigInvestment).setCodeError(IConstantsServiceException.TECH_ALREADY_HIGH_INVESTMENT)
                .setMsgFormat("{1}: {0} The administrative action of type {2} for the country {3} cannot be planned because the other tech has already a high investment.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, request.getRequest().getType(), country.getName(), 1, 1));

        CounterFaceTypeEnum type;
        String actualTechName;
        if (land) {
            type = CounterFaceTypeEnum.TECH_LAND;
            actualTechName = country.getLandTech();
        } else {
            type = CounterFaceTypeEnum.TECH_NAVAL;
            actualTechName = country.getNavalTech();
        }
        CounterEntity techCounter = CommonUtil.findFirst(game.getStacks().stream().flatMap(s -> s.getCounters().stream()),
                c -> StringUtils.equals(c.getCountry(), country.getName()) && c.getType() == type);

        failIfNull(new CheckForThrow<>().setTest(techCounter).setCodeError(IConstantsServiceException.MISSING_COUNTER)
                .setMsgFormat(MSG_MISSING_COUNTER).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, type, country.getName()));

        int techBox = GameUtil.getTechnologyBox(techCounter.getOwner().getProvince());
        Tech actualTech = CommonUtil.findFirst(getTables().getTechs(), t -> StringUtils.equals(t.getName(), actualTechName));

        failIfNull(new CheckForThrow<>().setTest(actualTech).setCodeError(IConstantsServiceException.MISSING_TABLE_ENTRY)
                .setMsgFormat("{1}: {0} The entry {3} of the table {2} is missing. Please ask an admin for correction.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, "TECH", actualTechName));

        List<Tech> higherTechs = getTables().getTechs().stream()
                .filter(t -> t.getBeginTurn() > actualTech.getBeginTurn())
                .collect(Collectors.toList());

        Collections.sort(higherTechs);

        if (higherTechs.size() > 0) {
            Tech tech = higherTechs.get(0);

            CounterEntity nextTechCounter = CommonUtil.findFirst(game.getStacks().stream().flatMap(s -> s.getCounters().stream()),
                    c -> c.getType() == CounterUtil.getTechnologyType(tech.getName()));

            failIfNull(new CheckForThrow<>().setTest(nextTechCounter).setCodeError(IConstantsServiceException.MISSING_COUNTER)
                    .setMsgFormat(MSG_MISSING_COUNTER).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, CounterUtil.getTechnologyType(tech.getName()), "neutral"));

            int nextTechBox = GameUtil.getTechnologyBox(nextTechCounter.getOwner().getProvince());

            boolean nextTechReachable = tech.getBeginTurn() <= game.getTurn();

            failIfFalse(new CheckForThrow<Boolean>().setTest(nextTechReachable || nextTechBox > techBox + 1).setCodeError(IConstantsServiceException.TECH_ALREADY_MAX)
                    .setMsgFormat("{1}: {0} The administrative action of type {2} for the country {3} cannot be planned because the tech is already at max level.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, request.getRequest().getType(), country.getName(), 1, 1));
        } else {
            // TODO it is possible to go through box 70. Disable this test when the conception of the 70+ will be made.
            failIfFalse(new CheckForThrow<Boolean>().setTest(techBox < 70).setCodeError(IConstantsServiceException.TECH_ALREADY_MAX)
                    .setMsgFormat("{1}: {0} The administrative action of type {2} for the country {3} cannot be planned because the tech is already at max level.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, request.getRequest().getType(), country.getName(), 1, 1));
        }

        int adm = oeUtil.getMilitaryValue(country);
        int column = adm + country.getDti() - 9;

        // threshold to -4/4
        column = Math.min(Math.max(column, -4), 4);
        column += getTechColumnBonus(game, country, land);
        column += EconomicUtil.getAdminActionColumnBonus(request.getRequest().getType(), request.getRequest().getInvestment());
        // threshold to -4/4
        column = Math.min(Math.max(column, -4), 4);

        int bonus = 0;
        if (StringUtils.equals(PlayableCountry.TURKEY, country.getName())) {
            // TODO REFORMS
            bonus -= 1;
        }
        CountryEntity countryRef = countryDao.getCountryByName(country.getName());
        CounterFaceTypeEnum groupTechType = CounterUtil.getTechnologyGroup(countryRef.getCulture(), land);
        CounterEntity groupTechCounter = CommonUtil.findFirst(game.getStacks().stream().flatMap(s -> s.getCounters().stream()),
                c -> c.getType() == groupTechType);
        if (groupTechCounter != null) {
            int groupBox = GameUtil.getTechnologyBox(groupTechCounter.getOwner().getProvince());
            if (groupBox > techBox + 5) {
                bonus += groupBox - techBox - 5;
            }
        }

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
     * @param game    containing all the counters (including the manufactures).
     * @param country checking the bonus.
     * @param land    <code>true</code> for land technology, <code>false</code> for naval technology.
     * @return the column bonus given by a manufacture for a technology enhancement.
     */
    protected int getTechColumnBonus(GameEntity game, PlayableCountryEntity country, boolean land) {
        int column = 0;
        if (land) {
            CounterEntity mnu = CommonUtil.findFirst(game.getStacks().stream().flatMap(s -> s.getCounters().stream()),
                    c -> (c.getType() == CounterFaceTypeEnum.MNU_METAL_PLUS || c
                            .getType() == CounterFaceTypeEnum.MNU_METAL_SCHLESIEN_PLUS) && StringUtils
                            .equals(c.getCountry(), country.getName()));
            if (mnu != null) {
                column = 2;
            } else {
                mnu = CommonUtil.findFirst(game.getStacks().stream().flatMap(s -> s.getCounters().stream()),
                        c -> (c.getType() == CounterFaceTypeEnum.MNU_METAL_MINUS || c.getType() == CounterFaceTypeEnum.MNU_METAL_SCHLESIEN_MINUS)
                                && StringUtils.equals(c.getCountry(), country.getName()));
                if (mnu != null) {
                    column = 1;
                }
            }
        } else {
            CounterEntity mnu = CommonUtil.findFirst(game.getStacks().stream().flatMap(s -> s.getCounters().stream()),
                    c -> c.getType() == CounterFaceTypeEnum.MNU_INSTRUMENTS_PLUS && StringUtils.equals(c.getCountry(), country.getName()));
            if (mnu != null) {
                column = 2;
            } else {
                mnu = CommonUtil.findFirst(game.getStacks().stream().flatMap(s -> s.getCounters().stream()),
                        c -> c.getType() == CounterFaceTypeEnum.MNU_INSTRUMENTS_MINUS && StringUtils.equals(c.getCountry(), country.getName()));
                if (mnu != null) {
                    column = 1;
                }
            }
        }
        return column;
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
    public DiffResponse validateAdminActions(Request<ValidateAdminActionsRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_VALIDATE_ADM_ACT).setParams(METHOD_VALIDATE_ADM_ACT));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getAuthent()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_VALIDATE_ADM_ACT, PARAMETER_AUTHENT).setParams(METHOD_VALIDATE_ADM_ACT));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_VALIDATE_ADM_ACT, PARAMETER_VALIDATE_ADM_ACT);
        GameEntity game = gameDiffs.getGame();

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_VALIDATE_ADM_ACT, PARAMETER_REQUEST).setParams(METHOD_VALIDATE_ADM_ACT));

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest().getIdCountry()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_VALIDATE_ADM_ACT, PARAMETER_REQUEST, PARAMETER_ID_COUNTRY).setParams(METHOD_VALIDATE_ADM_ACT));

        PlayableCountryEntity country = CommonUtil.findFirst(game.getCountries(), c -> c.getId().equals(request.getRequest().getIdCountry()));

        failIfNull(new AbstractService.CheckForThrow<>().setTest(country).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_VALIDATE_ADM_ACT, PARAMETER_REQUEST, PARAMETER_ID_COUNTRY).setParams(METHOD_VALIDATE_ADM_ACT, request.getRequest().getIdCountry()));

        failIfFalse(new CheckForThrow<Boolean>().setTest(StringUtils.equals(request.getAuthent().getUsername(), country.getUsername()))
                .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                .setMsgFormat(MSG_ACCESS_RIGHT).setName(PARAMETER_VALIDATE_ADM_ACT, PARAMETER_AUTHENT, PARAMETER_USERNAME).setParams(METHOD_VALIDATE_ADM_ACT, request.getAuthent().getUsername(), country.getUsername()));

        List<DiffEntity> diffs = gameDiffs.getDiffs();

        if (country.isReady() != request.getRequest().isValidate()) {
            country.setReady(request.getRequest().isValidate());

            long countriesNotReady = game.getCountries().stream()
                    .filter(c -> StringUtils.isNotEmpty(c.getUsername()) && !c.isReady())
                    .count();

            if (countriesNotReady == 0) {
                List<PlayableCountryEntity> countries = game.getCountries().stream()
                        .filter(c -> StringUtils.isNotEmpty(c.getUsername()))
                        .collect(Collectors.toList());
                for (PlayableCountryEntity countryAct : countries) {
                    diffs.addAll(computeAdministrativeActions(countryAct, game));
                    countryAct.setReady(false);
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
                diffs.add(diff);

                diff = new DiffEntity();
                diff.setIdGame(game.getId());
                diff.setVersionGame(game.getVersion());
                diff.setType(DiffTypeEnum.VALIDATE);
                diff.setTypeObject(DiffTypeObjectEnum.ADM_ACT);
                diffAttributes = new DiffAttributesEntity();
                diffAttributes.setType(DiffAttributeTypeEnum.TURN);
                diffAttributes.setValue(Integer.toString(game.getTurn()));
                diffAttributes.setDiff(diff);
                diff.getAttributes().add(diffAttributes);
                diffs.add(diff);

                diff = new DiffEntity();
                diff.setIdGame(game.getId());
                diff.setVersionGame(game.getVersion());
                diff.setType(DiffTypeEnum.INVALIDATE);
                diff.setTypeObject(DiffTypeObjectEnum.STATUS);
                diffs.add(diff);

                game.setStatus(GameStatusEnum.MILITARY);

                diff = new DiffEntity();
                diff.setIdGame(game.getId());
                diff.setVersionGame(game.getVersion());
                diff.setType(DiffTypeEnum.MODIFY);
                diff.setTypeObject(DiffTypeObjectEnum.STATUS);
                diffAttributes = new DiffAttributesEntity();
                diffAttributes.setType(DiffAttributeTypeEnum.STATUS);
                diffAttributes.setValue(GameStatusEnum.MILITARY.name());
                diffAttributes.setDiff(diff);
                diff.getAttributes().add(diffAttributes);
                diffs.add(diff);
            } else {
                DiffEntity diff = new DiffEntity();
                diff.setIdGame(game.getId());
                diff.setVersionGame(game.getVersion());
                if (request.getRequest().isValidate()) {
                    diff.setType(DiffTypeEnum.VALIDATE);
                } else {
                    diff.setType(DiffTypeEnum.INVALIDATE);
                }
                diff.setTypeObject(DiffTypeObjectEnum.STATUS);
                diff.setIdObject(country.getId());
                DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
                diffAttributes.setType(DiffAttributeTypeEnum.ID_COUNTRY);
                diffAttributes.setValue(country.getId().toString());
                diffAttributes.setDiff(diff);
                diff.getAttributes().add(diffAttributes);
                diffs.add(diff);
            }
        }

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(game.getVersion());

        response.setMessages(getMessagesSince(request));

        return response;
    }

    /**
     * Compute the planned administrative actions of the current game turn for the specified country.
     * Will also fill the economical sheet of the country if there is one (does not create one if none).
     *
     * @param country owner of the administrative actions.
     * @param game    current game.
     * @return a List of Diff containing all the modifications due to the administrative actions.
     */
    List<DiffEntity> computeAdministrativeActions(PlayableCountryEntity country, GameEntity game) {
        List<DiffEntity> diffs = new ArrayList<>();

        Map<String, Integer> costs = new HashMap<>();

        List<AdministrativeActionEntity> actions = country.getAdministrativeActions().stream()
                .filter(a -> a.getStatus() == AdminActionStatusEnum.PLANNED && a.getTurn().equals(game.getTurn()))
                .collect(Collectors.toList());
        for (AdministrativeActionEntity action : actions) {
            switch (action.getType()) {
                case LM:
                    // TODO check if country is at war
                    diffs.add(executeLowMaintenance(action, game));
                    break;
                case LF:
                    diffs.add(executeLowerFortress(action, game));
                    break;
                case DIS:
                    diffs.add(executeDisband(action, game));
                    break;
                case PU:
                    diffs.add(executePurchase(action, game, country, costs));
                    break;
                case TFI:
//                    diffs = executeTradeFleetImplantation(action, game, country, costs);
                    break;
                case MNU:
//                    diffs = executeManufacture(action, game, country, costs);
                    break;
                case FTI:
                case DTI:
//                    diffs = executeFtiDti(action, game, country, costs);
                    break;
                case EXL:
//                    diffs = executeExceptionalTaxes(action, game, country, costs);
                    break;
                case COL:
//                    diffs = executeColonisation(action, game, country, costs);
                    break;
                case TP:
//                    diffs = executeTradingPost(action, game, country, costs);
                    break;
                case ELT:
//                    diffs = executeTechnology(action, game, country, true, costs);
                    break;
                case ENT:
//                    diffs = executeTechnology(action, game, country, false, costs);
                    break;
            }

            action.setStatus(AdminActionStatusEnum.DONE);
        }

        EconomicalSheetEntity sheet = CommonUtil.findFirst(country.getEconomicalSheets(), economicalSheetEntity -> economicalSheetEntity.getTurn().equals(game.getTurn()));
        if (sheet != null) {
            Map<CounterFaceTypeEnum, Long> forces = game.getStacks().stream().flatMap(stack -> stack.getCounters().stream()
                    .filter(counter -> StringUtils.equals(counter.getCountry(), country.getName()) &&
                            CounterUtil.isArmy(counter.getType())))
                    .collect(Collectors.groupingBy(CounterEntity::getType, Collectors.counting()));
            List<BasicForce> basicForces = getTables().getBasicForces().stream()
                    .filter(basicForce -> StringUtils.equals(basicForce.getCountry(), country.getName()) &&
                            basicForce.getPeriod().getBegin() <= game.getTurn() &&
                            basicForce.getPeriod().getEnd() >= game.getTurn()).collect(Collectors.toList());
            // TODO manage wars
            List<Unit> units = getTables().getUnits().stream()
                    .filter(unit -> StringUtils.equals(unit.getCountry(), country.getName()) &&
                            (unit.getAction() == UnitActionEnum.MAINT_WAR || unit.getAction() == UnitActionEnum.MAINT) &&
                            !unit.isSpecial() &&
                            (StringUtils.equals(unit.getTech().getName(), country.getLandTech()) || StringUtils.equals(unit.getTech().getName(), country.getNavalTech()))).collect(Collectors.toList());
            Integer unitMaintenanceCost = MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units);

            Map<CounterFaceTypeEnum, Long> conscriptForces = game.getStacks().stream().flatMap(stack -> stack.getCounters().stream()
                    .filter(counter -> StringUtils.equals(counter.getCountry(), country.getName()) &&
                            CounterUtil.isArmy(counter.getType())))
                    .collect(Collectors.groupingBy(CounterEntity::getType, Collectors.counting()));
            List<Unit> conscriptUnits = getTables().getUnits().stream()
                    .filter(unit -> StringUtils.equals(unit.getCountry(), country.getName()) &&
                            unit.getAction() == UnitActionEnum.MAINT_WAR &&
                            unit.isSpecial() &&
                            StringUtils.equals(unit.getTech().getName(), country.getLandTech())).collect(Collectors.toList());
            Integer unitMaintenanceConscriptCost = MaintenanceUtil.computeUnitMaintenance(conscriptForces, null, conscriptUnits);

            sheet.setUnitMaintExpense(CommonUtil.add(unitMaintenanceCost, unitMaintenanceConscriptCost));

            Tech ownerLandTech = CommonUtil.findFirst(getTables().getTechs(), tech -> StringUtils.equals(tech.getName(), country.getLandTech()));
            Map<Pair<Integer, Boolean>, Integer> orderedFortresses = game.getStacks().stream().flatMap(stack -> stack.getCounters().stream()
                    .filter(counter -> StringUtils.equals(counter.getCountry(), country.getName()) &&
                            CounterUtil.isFortress(counter.getType())))
                    .collect(Collectors.groupingBy(
                            this::getFortressKeyFromCounter,
                            Collectors.summingInt(value -> 1)));

            Integer fortressesMaintenance = MaintenanceUtil.computeFortressesMaintenance(
                    orderedFortresses,
                    getTables().getTechs(),
                    ownerLandTech,
                    game.getTurn());

            sheet.setFortMaintExpense(fortressesMaintenance);

            Long missionMaintenance = game.getStacks().stream().flatMap(stack -> stack.getCounters().stream()
                    .filter(counter -> StringUtils.equals(counter.getCountry(), country.getName()) &&
                            counter.getType() == CounterFaceTypeEnum.MISSION))
                    .count();

            sheet.setMissMaintExpense(missionMaintenance.intValue());

            sheet.setUnitPurchExpense(costs.get(COST_UNIT_PURCHASE));
            sheet.setFortPurchExpense(costs.get(COST_FORT_PURCHASE));
            sheet.setAdminActExpense(costs.get(COST_ACTION));
            sheet.setOtherExpense(costs.get(COST_OTHER));

            sheet.setAdmTotalExpense(CommonUtil.add(sheet.getOptRefundExpense(), sheet.getUnitMaintExpense(), sheet.getFortMaintExpense(),
                    sheet.getMissMaintExpense(), sheet.getUnitPurchExpense(), sheet.getFortPurchExpense(), sheet.getAdminActExpense(),
                    sheet.getAdminReactExpense(), sheet.getOtherExpense()));

            sheet.setExcTaxesMod(costs.get(COST_EXC_LEVIES));
        }

        return diffs;
    }

    /**
     * Execute a planned administrative action of type low maintenance.
     *
     * @param action the planned administrative action.
     * @param game   the game.
     * @return a List of Diff related to the action.
     */
    private DiffEntity executeLowMaintenance(AdministrativeActionEntity action, GameEntity game) {
        return counterDomain.changeVeteransCounter(action.getIdObject(), 0, game);
    }

    /**
     * Execute a planned administrative action of type lower fortress.
     *
     * @param action the planned administrative action.
     * @param game   the game.
     * @return a List of Diff related to the action.
     */
    private DiffEntity executeLowerFortress(AdministrativeActionEntity action, GameEntity game) {
        return counterDomain.switchCounter(action.getIdObject(), action.getCounterFaceType(), game);
    }

    /**
     * Execute a planned administrative action of type disband.
     *
     * @param action the planned administrative action.
     * @param game   the game.
     * @return a List of Diff related to the action.
     */
    private DiffEntity executeDisband(AdministrativeActionEntity action, GameEntity game) {
        return counterDomain.removeCounter(action.getIdObject(), game);
    }

    /**
     * Execute a planned administrative action of type purchase.
     *
     * @param action  the planned administrative action.
     * @param game    the game.
     * @param country the country doing the action.
     * @param costs   various costs that could change with the action.
     * @return a List of Diff related to the action.
     */
    private DiffEntity executePurchase(AdministrativeActionEntity action, GameEntity game, PlayableCountryEntity country, Map<String, Integer> costs) {
        DiffEntity diff = counterDomain.createCounter(action.getCounterFaceType(), country.getName(), action.getProvince(), game);

        if (CounterUtil.isFortress(action.getCounterFaceType())) {
            addInMap(costs, COST_FORT_PURCHASE, action.getColumn());
        } else {
            addInMap(costs, COST_UNIT_PURCHASE, action.getColumn());
        }

        return diff;
    }

    /**
     * Add the number add to the Map costs given its key. Manages <code>null</code> values.
     *
     * @param costs Map holding various costs regrouped by keys.
     * @param key   the key related to the cost to be added.
     * @param add   the number to add.
     */
    private void addInMap(Map<String, Integer> costs, String key, Integer add) {
        if (add != null) {
            Integer oldCost = costs.get(key);

            if (oldCost == null) {
                oldCost = 0;
            }

            costs.put(key, oldCost + add);
        }
    }

    /**
     * @param counter whose we want the key.
     * @return the key used for computing fortress maintenance. It is a Pair consisting of level and location (<code>true</code> for ROTW).
     */
    private Pair<Integer, Boolean> getFortressKeyFromCounter(CounterEntity counter) {
        return new ImmutablePair<>(CounterUtil.getFortressLevelFromType(counter.getType()), GameUtil.isRotwProvince(counter.getOwner().getProvince()));
    }
}
