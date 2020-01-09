package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.common.vo.SimpleRequest;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.IEconomicService;
import com.mkl.eu.client.service.service.common.ValidateRequest;
import com.mkl.eu.client.service.service.eco.*;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.util.EconomicUtil;
import com.mkl.eu.client.service.util.GameUtil;
import com.mkl.eu.client.service.util.MaintenanceUtil;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.eco.Competition;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.ref.IReferentielConstants;
import com.mkl.eu.client.service.vo.ref.country.CountryReferential;
import com.mkl.eu.client.service.vo.ref.country.LimitReferential;
import com.mkl.eu.client.service.vo.tables.*;
import com.mkl.eu.service.service.domain.ICounterDomain;
import com.mkl.eu.service.service.domain.IStatusWorkflowDomain;
import com.mkl.eu.service.service.mapping.eco.AdministrativeActionMapping;
import com.mkl.eu.service.service.mapping.eco.CompetitionMapping;
import com.mkl.eu.service.service.mapping.eco.EconomicalSheetMapping;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
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
import com.mkl.eu.service.service.persistence.oe.eco.*;
import com.mkl.eu.service.service.persistence.oe.ref.country.CountryEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.*;
import com.mkl.eu.service.service.persistence.ref.ICountryDao;
import com.mkl.eu.service.service.service.GameDiffsInfo;
import com.mkl.eu.service.service.util.DiffUtil;
import com.mkl.eu.service.service.util.IOEUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
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
    /** Status workflow Domain. */
    @Autowired
    private IStatusWorkflowDomain statusWorkflowDomain;
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
    /** Country DAO. */
    @Autowired
    private ICountryDao countryDao;
    /** Economical sheet mapping. */
    @Autowired
    private EconomicalSheetMapping ecoSheetsMapping;
    /** Administrative action mapping. */
    @Autowired
    private AdministrativeActionMapping adminActMapping;
    /** Competition mapping. */
    @Autowired
    private CompetitionMapping competitionMapping;
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
    public List<AdministrativeActionCountry> loadAdminActions(SimpleRequest<LoadAdminActionsRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_LOAD_ADM_ACT).setParams(METHOD_LOAD_ADM_ACT));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_LOAD_ADM_ACT, PARAMETER_REQUEST).setParams(METHOD_LOAD_ADM_ACT));

        List<AdministrativeActionEntity> adminActions = adminActionDao.findDoneAdminActions(request.getRequest().getTurn(), request.getRequest().getIdGame());

        return adminActMapping.oesToVosCountry(adminActions);
    }

    /** {@inheritDoc} */
    @Override
    public List<Competition> loadCompetitions(SimpleRequest<LoadCompetitionsRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_LOAD_COMPETITIONS).setParams(METHOD_LOAD_COMPETITIONS));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_LOAD_COMPETITIONS, PARAMETER_REQUEST).setParams(METHOD_LOAD_COMPETITIONS));

        List<CompetitionEntity> competitions = adminActionDao.findCompetitions(request.getRequest().getTurn(), request.getRequest().getIdGame());

        return competitionMapping.oesToVos(competitions, new HashMap<>());
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse computeEconomicalSheets(Request<Void> request) throws FunctionalException {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_COMPUTE_ECO_SHEETS).setParams(METHOD_COMPUTE_ECO_SHEETS));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_COMPUTE_ECO_SHEETS, PARAMETER_COMPUTE_ECO_SHEETS);

        GameEntity game = gameDiffs.getGame();

        DiffEntity diff = statusWorkflowDomain.computeEconomicalSheets(game);

        return createDiff(diff, gameDiffs, request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse addAdminAction(Request<AddAdminActionRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ADD_ADM_ACT).setParams(METHOD_ADD_ADM_ACT));
        // TODO TG-2 authent

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_ADD_ADM_ACT, PARAMETER_ADD_ADM_ACT);
        GameEntity game = gameDiffs.getGame();

        checkGameStatus(game, GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE, null, METHOD_ADD_ADM_ACT, PARAMETER_ADD_ADM_ACT);

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
                WarStatusEnum warStatus = oeUtil.getWarStatus(game, country);
                failIfFalse(new AbstractService.CheckForThrow<Boolean>().setTest(warStatus.canWarMaintenance()).setCodeError(IConstantsServiceException.COUNTER_MAINTAIN_LOW_FORBIDDEN)
                        .setMsgFormat("{1}: {0} The country {3} cannot maintain low units because it is not at war.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, country.getName()));
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
                admAct = computeExceptionalTaxes(game, country);
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

        List<DiffEntity> diffs = new ArrayList<>();

        if (admAct != null) {
            admAct.setType(request.getRequest().getType());
            adminActionDao.create(admAct);

            DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.ADD, DiffTypeObjectEnum.ADM_ACT, admAct.getId(),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ID_COUNTRY, admAct.getCountry().getId()),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.TURN, admAct.getTurn()),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.TYPE, admAct.getType()),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.COST, admAct.getCost(), admAct.getCost() != null),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ID_OBJECT, admAct.getIdObject(), admAct.getIdObject() != null),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE, admAct.getProvince(), admAct.getProvince() != null),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.COUNTER_FACE_TYPE, admAct.getCounterFaceType(), admAct.getCounterFaceType() != null),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.COLUMN, admAct.getColumn(), admAct.getColumn() != null),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BONUS, admAct.getBonus(), admAct.getBonus() != null));
            diff.setIdCountry(country.getId());

            diffs.add(diff);
        }

        return createDiffs(diffs, gameDiffs, request);
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

        List<AdministrativeActionEntity> actions = adminActionDao.findPlannedAdminActions(request.getRequest().getIdCountry(), game.getTurn(),
                request.getRequest().getIdObject(), AdminActionTypeEnum.LM, AdminActionTypeEnum.DIS, AdminActionTypeEnum.LF);
        failIfFalse(new CheckForThrow<Boolean>().setTest(actions == null || actions.isEmpty()).setCodeError(IConstantsServiceException.COUNTER_ALREADY_PLANNED)
                .setMsgFormat("{1}: {0} The counter {2} has already a DIS or LM or LF administrative action PLANNED this turn.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_ID_OBJECT).setParams(METHOD_ADD_ADM_ACT, request.getRequest().getIdObject()));


        AdministrativeActionEntity admAct = new AdministrativeActionEntity();
        admAct.setCountry(country);
        admAct.setStatus(AdminActionStatusEnum.PLANNED);
        admAct.setTurn(game.getTurn());
        admAct.setIdObject(counter.getId());
        admAct.setProvince(counter.getOwner().getProvince());
        CounterFaceTypeEnum face = request.getRequest().getCounterFaceType();
        if (face == null) {
            face = counter.getType();
        }
        admAct.setCounterFaceType(face);
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

        List<StackEntity> stacks = oeUtil.getStacksOnProvince(game, province);
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
        List<AdministrativeActionEntity> actions = country.getAdministrativeActions().stream()
                .filter(a -> a.getTurn().equals(game.getTurn()) && a.getType() == AdminActionTypeEnum.PU && a.getStatus() == AdminActionStatusEnum.PLANNED
                        && StringUtils.equals(prov.getName(), a.getProvince()) && CounterUtil.isFortress(a.getCounterFaceType()))
                .collect(Collectors.toList());

        failIfFalse(new CheckForThrow<Boolean>().setTest(actions == null || actions.isEmpty()).setCodeError(IConstantsServiceException.FORTRESS_ALREADY_PLANNED)
                .setMsgFormat("{1}: {0} The fortress {2} is already PLANNED on the province {3}.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_COUNTER_FACE_TYPE).setParams(METHOD_ADD_ADM_ACT, faceType, prov.getName()));


        int actualLevel = 0;
        if (prov instanceof EuropeanProvinceEntity) {
            if (((EuropeanProvinceEntity) prov).getFortress() != null) {
                actualLevel = ((EuropeanProvinceEntity) prov).getFortress();
            }

            failIfTrue(new CheckForThrow<Boolean>().setTest(CounterUtil.isArsenal(faceType)).setCodeError(IConstantsServiceException.COUNTER_CANT_PURCHASE)
                    .setMsgFormat("{1}: {0} The counter face type {2} cannot be purchased on province {3}.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_COUNTER_FACE_TYPE).setParams(METHOD_ADD_ADM_ACT, faceType, prov.getName()));
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
        Double plannedSize;
        final LimitTypeEnum limitType;

        List<AdministrativeActionEntity> actions = adminActionDao.findPlannedAdminActions(country.getId(), game.getTurn(),
                null, AdminActionTypeEnum.PU);
        if (land) {
            plannedSize = actions.stream().filter(action -> CounterUtil.isLandArmy(action.getCounterFaceType())).collect(Collectors.summingDouble(action -> CounterUtil.getSizeFromType(action.getCounterFaceType())));
            limitType = LimitTypeEnum.PURCHASE_LAND_TROOPS;
        } else {
            plannedSize = actions.stream().filter(action -> CounterUtil.isNavalArmy(action.getCounterFaceType())).collect(Collectors.summingDouble(action -> CounterUtil.getSizeFromType(action.getCounterFaceType())));
            limitType = LimitTypeEnum.PURCHASE_NAVAL_TROOPS;
        }

        Double size = CounterUtil.getSizeFromType(faceType);
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
            cost = MaintenanceUtil.getPurchasePrice(plannedSize.intValue(), maxPurchase, unitCost.getPrice(), size.intValue());
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
        List<AdministrativeActionEntity> actions = adminActionDao.findPlannedAdminActions(country.getId(), game.getTurn(),
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
            int dti = oeUtil.getDti(game, getTables(), tradeZone.getCountryName());
            if (StringUtils.equals(country.getName(), tradeZone.getCountryName())) {
                column += dti;
            } else if (!StringUtils.isEmpty(tradeZone.getCountryName())) {
                column -= dti;
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
        List<AdministrativeActionEntity> actions = adminActionDao.findPlannedAdminActions(country.getId(), game.getTurn(),
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

            failIfFalse(new CheckForThrow<Boolean>().setTest(actualMnus < maxMnus + 2).setCodeError(IConstantsServiceException.COUNTER_LIMIT_EXCEED)
                    .setMsgFormat(MSG_COUNTER_LIMIT_EXCEED)
                    .setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, "MNU", country.getName(), actualMnus, maxMnus + "+2"));

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

            int max = 0;
            CountryReferential countryRef = CommonUtil.findFirst(REFERENTIAL.getCountries(),
                    c -> StringUtils.equals(c.getName(), country.getName()));
            if (countryRef != null) {
                LimitReferential limit = CommonUtil.findFirst(countryRef.getLimits(),
                        l -> l.getType() == CounterUtil.getManufactureCounter(type));
                if (limit != null && limit.getNumber() != null) {
                    max = limit.getNumber();
                }
            }
            CounterFaceTypeEnum faceMinus = CounterUtil.getManufactureLevel1(type);
            CounterFaceTypeEnum facePlus = CounterUtil.getManufactureLevel2(type);
            Long existingTypeMnus = game.getStacks().stream()
                    .flatMap(s -> s.getCounters().stream())
                    .filter(c -> StringUtils.equals(c.getCountry(), country.getName()) &&
                            (c.getType() == faceMinus || c.getType() == facePlus))
                    .count();

            failIfFalse(new CheckForThrow<Boolean>().setTest(existingTypeMnus < max).setCodeError(IConstantsServiceException.COUNTER_LIMIT_EXCEED)
                    .setMsgFormat(MSG_COUNTER_LIMIT_EXCEED)
                    .setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, CounterUtil.getManufactureCounter(type).name(), country.getName(), existingTypeMnus, max));
        }

        int column = getColumnForDomesticOperation(request, country);

        int bonus = getBonusForDomesticOperation(game, country);

        AdministrativeActionEntity admAct = new AdministrativeActionEntity();
        admAct.setCountry(country);
        admAct.setStatus(AdminActionStatusEnum.PLANNED);
        admAct.setTurn(game.getTurn());
        admAct.setProvince(province);
        admAct.setCounterFaceType(type);
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
        List<AdministrativeActionEntity> actions = adminActionDao.findPlannedAdminActions(country.getId(), game.getTurn(),
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
            // TODO TG-37 Gold flow is not inflation > 10, it is 40 gold exploited in rotw and the malus is -2
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
     * @param game    in which the action will be created.
     * @param country owner of the action.
     * @return the administrative action to create.
     * @throws FunctionalException Exception.
     */
    private AdministrativeActionEntity computeExceptionalTaxes(GameEntity game, PlayableCountryEntity country) throws FunctionalException {
        List<AdministrativeActionEntity> actions = adminActionDao.findPlannedAdminActions(country.getId(), game.getTurn(),
                null, AdminActionTypeEnum.MNU, AdminActionTypeEnum.FTI, AdminActionTypeEnum.DTI, AdminActionTypeEnum.EXL);

        failIfFalse(new CheckForThrow<Boolean>().setTest(actions.isEmpty()).setCodeError(IConstantsServiceException.ACTION_ALREADY_PLANNED)
                .setMsgFormat("{1}: {0} The administrative action of type {1} is already panned for the country {3}.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, AdminActionTypeEnum.EXL, country.getName()));

        WarStatusEnum warStatus = oeUtil.getWarStatus(game, country);
        failIfFalse(new CheckForThrow<Boolean>().setTest(warStatus.canTaxes()).setCodeError(IConstantsServiceException.EXC_TAXES_NOT_AT_WAR)
                .setMsgFormat("{1}: {0} The country {2} is not at war and thus cannot levy exceptional taxes.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, country.getName()));

        int stab = oeUtil.getStability(game, country.getName());

        boolean stabFree = false;
        if (warStatus.hasTaxesReduction()) {
            List<String> enemies = oeUtil.getEnemies(country, game, true);

            stabFree = playableCountryDao.isFatherlandInDanger(country.getName(), enemies, game.getId());
        }

        if (!stabFree) {
            failIfFalse(new CheckForThrow<Boolean>().setTest(stab > -3).setCodeError(IConstantsServiceException.INSUFFICIENT_STABILITY)
                    .setMsgFormat("{1}: {0} The stability of the country {2} is too low. Actual: {3}, minimum: {4}.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, country.getName(), stab, -2));
            stab--;
        }

        int adm = oeUtil.getAdministrativeValue(country);

        int bonus = adm + stab * 3;

        AdministrativeActionEntity admAct = new AdministrativeActionEntity();
        admAct.setCountry(country);
        admAct.setStatus(AdminActionStatusEnum.PLANNED);
        admAct.setTurn(game.getTurn());
        admAct.setBonus(bonus);
        if (!stabFree) {
            // use column to know if stab has to be lowered or not
            admAct.setColumn(-1);
        }

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
        List<AdministrativeActionEntity> actions = adminActionDao.findPlannedAdminActions(country.getId(), game.getTurn(),
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
            // TODO TG-131countries and economical events rules
            // TODO TG-12
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
        bonus += getBonusFromLeader(country.getName(), rotwProv, game);

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
     * @param countryName name of the country doing the operation.
     * @param rotwProv    the rotw province where the operation is done.
     * @param game        the game.
     * @return the bonus given by a leader for an external operation.
     */
    private int getBonusFromLeader(String countryName, RotwProvinceEntity rotwProv, GameEntity game) {
        List<String> leaderCodes = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(stack.getProvince(), rotwProv.getName()))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> StringUtils.equals(counter.getCountry(), countryName) &&
                        counter.getType() == CounterFaceTypeEnum.LEADER)
                .map(CounterEntity::getCode)
                .collect(Collectors.toList());
        List<Leader> leaders = getTables().getLeaders().stream()
                .filter(l -> leaderCodes.contains(l.getCode()))
                .collect(Collectors.toList());
        int bonusLeader = leaders.stream()
                .map(this::getBonusLeader)
                .max(Comparator.<Integer>naturalOrder())
                .orElse(0);
        if (bonusLeader < 1 && counterDao.isGovernorInSameRegion(rotwProv.getRegion(), countryName, game.getId())) {
            bonusLeader = 1;
        }

        return bonusLeader;
    }

    /**
     * @param leader in the province.
     * @return the bonus given by the leader for an external operation.
     */
    private int getBonusLeader(Leader leader) {
        switch (leader.getType()) {
            case CONQUISTADOR:
            case GOVERNOR:
                return leader.getManoeuvre();
            case EXPLORER:
                return leader.getManoeuvre() / 2;
            default:
                return 0;
        }
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
        List<AdministrativeActionEntity> actions = adminActionDao.findPlannedAdminActions(country.getId(), game.getTurn(),
                null, AdminActionTypeEnum.TP);

        Integer plannedCols = actions.stream().collect(Collectors.summingInt(action -> 1));
        Integer maxCols = getTables().getLimits().stream().filter(
                limit -> StringUtils.equals(limit.getCountry(), country.getName()) &&
                        limit.getType() == LimitTypeEnum.ACTION_TP &&
                        limit.getPeriod().getBegin() <= game.getTurn() &&
                        limit.getPeriod().getEnd() >= game.getTurn()).collect(Collectors.summingInt(Limit::getNumber));

        failIfFalse(new CheckForThrow<Boolean>().setTest(plannedCols < maxCols).setCodeError(IConstantsServiceException.ADMIN_ACTION_LIMIT_EXCEED)
                .setMsgFormat("{1}: {0} The administrative action of type {2} for the country {3} cannot be planned because country limits were exceeded ({4}/{5}).").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, LimitTypeEnum.ACTION_TP, country.getName(), plannedCols, maxCols));

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
        bonus += getBonusFromLeader(country.getName(), rotwProv, game);

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
        List<AdministrativeActionEntity> actions = adminActionDao.findPlannedAdminActions(country.getId(), game.getTurn(),
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

        int techBox = GameUtil.getTechnology(techCounter.getOwner().getProvince());
        Tech actualTech = CommonUtil.findFirst(getTables().getTechs(), t -> StringUtils.equals(t.getName(), actualTechName));

        failIfNull(new CheckForThrow<>().setTest(actualTech).setCodeError(IConstantsServiceException.MISSING_TABLE_ENTRY)
                .setMsgFormat("{1}: {0} The entry {3} of the table {2} is missing. Please ask an admin for correction.").setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, "TECH", actualTechName));

        List<Tech> higherTechs = getTables().getTechs().stream()
                .filter(t -> t.getBeginTurn() > actualTech.getBeginTurn() && t.isLand() == land)
                .collect(Collectors.toList());

        Collections.sort(higherTechs);

        if (higherTechs.size() > 0) {
            Tech tech = higherTechs.get(0);

            CounterEntity nextTechCounter = CommonUtil.findFirst(game.getStacks().stream().flatMap(s -> s.getCounters().stream()),
                    c -> c.getType() == CounterUtil.getTechnologyType(tech.getName()));

            failIfNull(new CheckForThrow<>().setTest(nextTechCounter).setCodeError(IConstantsServiceException.MISSING_COUNTER)
                    .setMsgFormat(MSG_MISSING_COUNTER).setName(PARAMETER_ADD_ADM_ACT, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_ADD_ADM_ACT, CounterUtil.getTechnologyType(tech.getName()), "neutral"));

            int nextTechBox = GameUtil.getTechnology(nextTechCounter.getOwner().getProvince());

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
            // TODO TG-131 REFORMS
            bonus -= 1;
        }
        CountryEntity countryRef = countryDao.getCountryByName(country.getName());
        CounterFaceTypeEnum groupTechType = CounterUtil.getTechnologyGroup(countryRef.getCulture(), land);
        CounterEntity groupTechCounter = CommonUtil.findFirst(game.getStacks().stream().flatMap(s -> s.getCounters().stream()),
                c -> c.getType() == groupTechType);
        if (groupTechCounter != null) {
            int groupBox = GameUtil.getTechnology(groupTechCounter.getOwner().getProvince());
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

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_REMOVE_ADM_ACT, PARAMETER_REMOVE_ADM_ACT);
        GameEntity game = gameDiffs.getGame();

        checkGameStatus(game, GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE, null, METHOD_REMOVE_ADM_ACT, PARAMETER_REMOVE_ADM_ACT);

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getAuthent()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_REMOVE_ADM_ACT, PARAMETER_AUTHENT).setParams(METHOD_REMOVE_ADM_ACT));

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

        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.REMOVE, DiffTypeObjectEnum.ADM_ACT, action.getId(),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ID_COUNTRY, action.getCountry().getId()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.TYPE, action.getType()));
        diff.setIdCountry(action.getCountry().getId());

        return createDiff(diff, gameDiffs, request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse validateAdminActions(Request<ValidateRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_VALIDATE_ADM_ACT).setParams(METHOD_VALIDATE_ADM_ACT));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_VALIDATE_ADM_ACT, PARAMETER_VALIDATE_ADM_ACT);
        GameEntity game = gameDiffs.getGame();

        checkGameStatus(game, GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE, null, METHOD_VALIDATE_ADM_ACT, PARAMETER_VALIDATE_ADM_ACT);

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getAuthent()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_VALIDATE_ADM_ACT, PARAMETER_AUTHENT).setParams(METHOD_VALIDATE_ADM_ACT));

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_VALIDATE_ADM_ACT, PARAMETER_REQUEST).setParams(METHOD_VALIDATE_ADM_ACT));

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getGame().getIdCountry()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_VALIDATE_ADM_ACT, PARAMETER_ID_COUNTRY).setParams(METHOD_VALIDATE_ADM_ACT));

        PlayableCountryEntity country = CommonUtil.findFirst(game.getCountries(), c -> c.getId().equals(request.getGame().getIdCountry()));

        failIfNull(new AbstractService.CheckForThrow<>().setTest(country).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_VALIDATE_ADM_ACT, PARAMETER_ID_COUNTRY).setParams(METHOD_VALIDATE_ADM_ACT, request.getGame().getIdCountry()));

        failIfFalse(new CheckForThrow<Boolean>().setTest(StringUtils.equals(request.getAuthent().getUsername(), country.getUsername()))
                .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                .setMsgFormat(MSG_ACCESS_RIGHT).setName(PARAMETER_VALIDATE_ADM_ACT, PARAMETER_AUTHENT, PARAMETER_USERNAME).setParams(METHOD_VALIDATE_ADM_ACT, request.getAuthent().getUsername(), country.getUsername()));

        List<DiffEntity> newDiffs = new ArrayList<>();

        if (country.isReady() != request.getRequest().isValidate()) {
            country.setReady(request.getRequest().isValidate());

            long countriesNotReady = game.getCountries().stream()
                    .filter(c -> StringUtils.isNotEmpty(c.getUsername()) && !c.isReady())
                    .count();

            if (countriesNotReady == 0) {
                List<PlayableCountryEntity> countries = game.getCountries().stream()
                        .filter(c -> StringUtils.isNotEmpty(c.getUsername()))
                        .collect(Collectors.toList());

                /**
                 * New tfis grouped by TZ then by country in order to minimize the diffs created.
                 * For example, if a country gains a tfi and becomes lvl 6 and then is reduced
                 * during global concurrency, then he will go back to its former 5 and no diff
                 * will be created.
                 */
                Map<String, Map<String, Integer>> newTfis = new HashMap<>();

                /**
                 * New colonies and trading posts will trigger the concurrency check on the
                 * province they appear.
                 */
                Set<String> newEstablishments = new HashSet<>();

                // automatic refit of tfis

                for (PlayableCountryEntity countryAct : countries) {
                    newDiffs.addAll(computeAdministrativeActions(countryAct, game, newTfis, newEstablishments));
                    countryAct.setReady(false);
                }

                // automatic tf refill (before competitions)
                newDiffs.addAll(computeAutomaticTfCompetitions(game, newTfis));
                newDiffs.addAll(computeAutomaticEstablishmentCompetitions(game, newEstablishments));
                // exotic resources concurrencies
                newDiffs.addAll(computeAutomaticTechnologyAdvances(game));

                DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.INVALIDATE, DiffTypeObjectEnum.ECO_SHEET,
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.TURN, game.getTurn()));
                newDiffs.add(diff);

                diff = DiffUtil.createDiff(game, DiffTypeEnum.VALIDATE, DiffTypeObjectEnum.ADM_ACT,
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.TURN, game.getTurn()));
                newDiffs.add(diff);

                newDiffs.addAll(statusWorkflowDomain.computeEndAdministrativeActions(game));
            } else {
                DiffEntity diff = DiffUtil.createDiff(game, request.getRequest().isValidate() ? DiffTypeEnum.VALIDATE : DiffTypeEnum.INVALIDATE, DiffTypeObjectEnum.STATUS, country.getId(),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ID_COUNTRY, country.getId()));
                newDiffs.add(diff);
            }
        }

        return createDiffs(newDiffs, gameDiffs, request);
    }

    /**
     * Compute the planned administrative actions of the current game turn for the specified country.
     * Will also fill the economical sheet of the country if there is one (does not create one if none).
     *
     * @param country           owner of the administrative actions.
     * @param game              current game.
     * @param newTfis           New tfis grouped by TZ then by country in order to minimize the diffs created.
     *                          For example, if a country gains a tfi and becomes lvl 6 and then is reduced
     *                          during global concurrency, then he will go back to its former 5 and no diff
     *                          will be created.
     * @param newEstablishments New colonies and trading posts will trigger the concurrency check on the
     *                          province they appear.
     * @return a List of Diff containing all the modifications due to the administrative actions.
     */
    protected List<DiffEntity> computeAdministrativeActions(PlayableCountryEntity country, GameEntity game, Map<String, Map<String, Integer>> newTfis, Set<String> newEstablishments) {
        List<DiffEntity> diffs = new ArrayList<>();

        Map<String, Integer> costs = new HashMap<>();
        List<StackEntity> stacksThatMayChangeController = new ArrayList<>();

        // We stock the techs because if there is a technology advance, the maintenance should be of former technology.
        String landTech = country.getLandTech();
        String navalTech = country.getNavalTech();

        List<AdministrativeActionEntity> actions = country.getAdministrativeActions().stream()
                .filter(a -> a.getStatus() == AdminActionStatusEnum.PLANNED && a.getTurn().equals(game.getTurn()))
                .collect(Collectors.toList());
        DiffEntity diff;
        for (AdministrativeActionEntity action : actions) {
            switch (action.getType()) {
                case LM:
                    diffs.add(executeLowMaintenance(action, game));
                    break;
                case LF:
                    diffs.add(executeLowerFortress(action, game));
                    break;
                case DIS:
                    diffs.add(executeDisband(action, game, stacksThatMayChangeController));
                    break;
                case PU:
                    diffs.add(executePurchase(action, game, country, costs));
                    break;
                case TFI:
                    executeTradeFleetImplantation(action, game, country, costs, newTfis);
                    break;
                case MNU:
                    diff = executeManufacture(action, game, country, costs);
                    if (diff != null) {
                        diffs.add(diff);
                    }
                    break;
                case FTI:
                case DTI:
                    diff = executeFtiDti(action, game, country, costs);
                    if (diff != null) {
                        diffs.add(diff);
                    }
                    break;
                case EXL:
                    diff = executeExceptionalTaxes(action, game, country, costs);
                    if (diff != null) {
                        diffs.add(diff);
                    }
                    break;
                case COL:
                case TP:
                    diffs.addAll(executeEstablishment(action, game, country, costs, newEstablishments));
                    break;
                case ELT:
                case ENT:
                    diffs.addAll(executeTechnology(action, game, country, costs));
                    break;
            }

            action.setStatus(AdminActionStatusEnum.DONE);
        }

        for (StackEntity stack : stacksThatMayChangeController) {
            if (stack.getGame() != null) {
                String newStackController = oeUtil.getController(stack);
                if (!StringUtils.equals(newStackController, stack.getCountry())) {
                    String newLeader = oeUtil.getLeader(stack, getTables(), getLeaderConditions(stack.getProvince()));
                    diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STACK, stack.getId(),
                            DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.COUNTRY, newStackController),
                            DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.LEADER, newLeader, !StringUtils.equals(newLeader, stack.getLeader()))));
                    stack.setCountry(newStackController);
                    stack.setLeader(newLeader);
                }
            }
        }

        EconomicalSheetEntity sheet = CommonUtil.findFirst(country.getEconomicalSheets(), economicalSheetEntity -> economicalSheetEntity.getTurn().equals(game.getTurn()));
        if (sheet != null) {
            // Maintenance computation:
            // If at peace, conscript or veteran troops are paid the same.
            // If at war, conscript land forces can be less expensive.
            // Naval units are always paid the same price, conscript or veteran.
            WarStatusEnum warStatus = oeUtil.getWarStatus(game, country);
            if (warStatus.canWarMaintenance()) {
                Map<CounterFaceTypeEnum, Long> forces = game.getStacks().stream().flatMap(stack -> stack.getCounters().stream()
                        .filter(counter -> StringUtils.equals(counter.getCountry(), country.getName()) &&
                                (counter.getVeterans() != null && (counter.getVeterans() > 0) || CounterUtil.isNavalArmy(counter.getType())) && CounterUtil.isArmy(counter.getType())))
                        .collect(Collectors.groupingBy(CounterEntity::getType, Collectors.counting()));
                List<BasicForce> basicForces = getTables().getBasicForces().stream()
                        .filter(basicForce -> StringUtils.equals(basicForce.getCountry(), country.getName()) &&
                                basicForce.getPeriod().getBegin() <= game.getTurn() &&
                                basicForce.getPeriod().getEnd() >= game.getTurn()).collect(Collectors.toList());
                List<Unit> units = getTables().getUnits().stream()
                        .filter(unit -> StringUtils.equals(unit.getCountry(), country.getName()) &&
                                (unit.getAction() == UnitActionEnum.MAINT_WAR || unit.getAction() == UnitActionEnum.MAINT) &&
                                !unit.isSpecial() &&
                                (StringUtils.equals(unit.getTech().getName(), landTech) || StringUtils.equals(unit.getTech().getName(), navalTech))).collect(Collectors.toList());
                Integer unitMaintenanceCost = MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units);

                Map<CounterFaceTypeEnum, Long> conscriptForces = game.getStacks().stream().flatMap(stack -> stack.getCounters().stream()
                        .filter(counter -> StringUtils.equals(counter.getCountry(), country.getName()) &&
                                (counter.getVeterans() == null || counter.getVeterans() == 0) && CounterUtil.isLandArmy(counter.getType())))
                        .collect(Collectors.groupingBy(CounterEntity::getType, Collectors.counting()));
                List<Unit> conscriptUnits = getTables().getUnits().stream()
                        .filter(unit -> StringUtils.equals(unit.getCountry(), country.getName()) &&
                                unit.getAction() == UnitActionEnum.MAINT_WAR &&
                                unit.isSpecial() &&
                                StringUtils.equals(unit.getTech().getName(), landTech)).collect(Collectors.toList());
                Integer unitMaintenanceConscriptCost = MaintenanceUtil.computeUnitMaintenance(conscriptForces, null, conscriptUnits);

                sheet.setUnitMaintExpense(CommonUtil.add(unitMaintenanceCost, unitMaintenanceConscriptCost));
            } else {
                Map<CounterFaceTypeEnum, Long> forces = game.getStacks().stream().flatMap(stack -> stack.getCounters().stream()
                        .filter(counter -> StringUtils.equals(counter.getCountry(), country.getName()) &&
                                CounterUtil.isArmy(counter.getType())))
                        .collect(Collectors.groupingBy(CounterEntity::getType, Collectors.counting()));
                List<BasicForce> basicForces = getTables().getBasicForces().stream()
                        .filter(basicForce -> StringUtils.equals(basicForce.getCountry(), country.getName()) &&
                                basicForce.getPeriod().getBegin() <= game.getTurn() &&
                                basicForce.getPeriod().getEnd() >= game.getTurn()).collect(Collectors.toList());
                List<Unit> units = getTables().getUnits().stream()
                        .filter(unit -> StringUtils.equals(unit.getCountry(), country.getName()) &&
                                (unit.getAction() == UnitActionEnum.MAINT_PEACE || unit.getAction() == UnitActionEnum.MAINT) &&
                                !unit.isSpecial() &&
                                (StringUtils.equals(unit.getTech().getName(), landTech) || StringUtils.equals(unit.getTech().getName(), navalTech))).collect(Collectors.toList());
                Integer unitMaintenanceCost = MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units);

                sheet.setUnitMaintExpense(unitMaintenanceCost);
            }

            Tech ownerLandTech = CommonUtil.findFirst(getTables().getTechs(), tech -> StringUtils.equals(tech.getName(), landTech));
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
        return counterDomain.changeVeteransCounter(action.getIdObject(), 0d, game);
    }

    /**
     * Execute a planned administrative action of type lower fortress.
     *
     * @param action the planned administrative action.
     * @param game   the game.
     * @return a List of Diff related to the action.
     */
    private DiffEntity executeLowerFortress(AdministrativeActionEntity action, GameEntity game) {
        CounterEntity counter = game.getStacks().stream()
                .flatMap(s -> s.getCounters().stream())
                .filter(c -> c.getId().equals(action.getIdObject()))
                .findAny()
                .orElse(null);
        return counterDomain.switchCounter(counter, action.getCounterFaceType(), null, game);
    }

    /**
     * Execute a planned administrative action of type disband.
     *
     * @param action        the planned administrative action.
     * @param game          the game.
     * @param stacksToCheck the stacks that might have changed controller at the end of administrative phase.
     * @return a List of Diff related to the action.
     */
    private DiffEntity executeDisband(AdministrativeActionEntity action, GameEntity game, List<StackEntity> stacksToCheck) {
        CounterEntity counter = game.getStacks().stream()
                .flatMap(s -> s.getCounters().stream())
                .filter(c -> c.getId().equals(action.getIdObject()))
                .findAny()
                .orElse(null);
        if (!stacksToCheck.contains(counter.getOwner())) {
            stacksToCheck.add(counter.getOwner());
        }
        return counterDomain.removeCounter(counter);
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
        DiffEntity diff = counterDomain.createCounter(action.getCounterFaceType(), country.getName(), action.getProvince(), null, game);

        if (CounterUtil.isFortress(action.getCounterFaceType())) {
            addInMap(costs, COST_FORT_PURCHASE, action.getCost());
        } else {
            addInMap(costs, COST_UNIT_PURCHASE, action.getCost());
        }

        return diff;
    }

    /**
     * Execute a planned administrative action of type trade fleet implantation.
     *
     * @param action  the planned administrative action.
     * @param game    the game.
     * @param country the country doing the action.
     * @param costs   various costs that could change with the action.
     * @param newTfis map of the new tfis ordered by TZ and country.
     */
    private void executeTradeFleetImplantation(AdministrativeActionEntity action, GameEntity game, PlayableCountryEntity country, Map<String, Integer> costs, Map<String, Map<String, Integer>> newTfis) {
        addInMap(costs, COST_ACTION, action.getCost());
        if (rollDie(action, game, country)) {
            if (!newTfis.containsKey(action.getProvince())) {
                newTfis.put(action.getProvince(), new HashMap<>());
            }
            if (!newTfis.get(action.getProvince()).containsKey(country.getName())) {
                newTfis.get(action.getProvince()).put(country.getName(), 1);
            } else {
                newTfis.get(action.getProvince()).put(country.getName(), newTfis.get(action.getProvince()).get(country.getName()) + 1);
            }
        }
    }

    /**
     * @param action  administrative action that need some die rolls.
     * @param game    the game.
     * @param country the country.
     * @return <code>true</code> if the action is a globally a success.
     */
    private boolean rollDie(AdministrativeActionEntity action, GameEntity game, PlayableCountryEntity country) {
        Integer die = oeUtil.rollDie(game, country);
        action.setDie(die);

        Integer modifiedDie = Math.min(Math.max(die + action.getBonus(), 1), 10);
        Result result = CommonUtil.findFirst(getTables().getResults().stream(),
                r -> r.getColumn().equals(action.getColumn()) && r.getDie().equals(modifiedDie));

        action.setResult(result.getResult());

        if (result.getResult() == ResultEnum.FUMBLE || result.getResult() == ResultEnum.FAILED) {
            return false;
        }

        if (result.getResult() == ResultEnum.AVERAGE || result.getResult() == ResultEnum.AVERAGE_PLUS) {
            die = oeUtil.rollDie(game, country);
            action.setSecondaryDie(die);

            if (die > country.getFti()) {
                action.setSecondaryResult(false);
                return false;
            } else {
                action.setSecondaryResult(true);
            }
        }
        return true;
    }

    /**
     * Execute a planned administrative action of type manufacture.
     *
     * @param action  the planned administrative action.
     * @param game    the game.
     * @param country the country doing the action.
     * @param costs   various costs that could change with the action.
     * @return a List of Diff related to the action.
     */
    private DiffEntity executeManufacture(AdministrativeActionEntity action, GameEntity game, PlayableCountryEntity country, Map<String, Integer> costs) {
        addInMap(costs, COST_ACTION, action.getCost());
        if (rollDie(action, game, country)) {
            if (action.getIdObject() != null) {
                CounterEntity counter = game.getStacks().stream()
                        .flatMap(s -> s.getCounters().stream())
                        .filter(c -> c.getId().equals(action.getIdObject()))
                        .findAny()
                        .orElse(null);
                return counterDomain.switchCounter(counter, CounterUtil.getManufactureLevel2(action.getCounterFaceType()), null, game);
            } else {
                // FIXME regroup mnu counter with other economic counters ?
                return counterDomain.createCounter(CounterUtil.getManufactureLevel1(action.getCounterFaceType()), country.getName(), action.getProvince(), null, game);
            }
        } else {
            return null;
        }
    }

    /**
     * Execute a planned administrative action of type enhance DTI/FTI.
     *
     * @param action  the planned administrative action.
     * @param game    the game.
     * @param country the country doing the action.
     * @param costs   various costs that could change with the action.
     * @return a List of Diff related to the action.
     */
    private DiffEntity executeFtiDti(AdministrativeActionEntity action, GameEntity game, PlayableCountryEntity country, Map<String, Integer> costs) {
        addInMap(costs, COST_ACTION, action.getCost());
        if (rollDie(action, game, country)) {
            DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.COUNTRY, country.getId());
            if (action.getType() == AdminActionTypeEnum.DTI) {
                country.setDti(country.getDti() + 1);

                DiffAttributesEntity diffAttributes = DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.DTI, country.getDti());
                diffAttributes.setDiff(diff);
                diff.getAttributes().add(diffAttributes);
            } else {
                Map<LimitTypeEnum, Integer> maxi = getTables().getLimits().stream().filter(
                        limit -> StringUtils.equals(limit.getCountry(), country.getName()) &&
                                (limit.getType() == LimitTypeEnum.MAX_FTI ||
                                        limit.getType() == LimitTypeEnum.MAX_FTI_ROTW) &&
                                limit.getPeriod().getBegin() <= game.getTurn() &&
                                limit.getPeriod().getEnd() >= game.getTurn()).collect(Collectors
                        .groupingBy(Limit::getType, Collectors.summingInt(Limit::getNumber)));

                if (country.getFti() < maxi.get(LimitTypeEnum.MAX_FTI)) {
                    country.setFti(country.getFti() + 1);

                    DiffAttributesEntity diffAttributes = DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.FTI, country.getFti());
                    diffAttributes.setDiff(diff);
                    diff.getAttributes().add(diffAttributes);
                }
                if ((maxi.get(LimitTypeEnum.MAX_FTI_ROTW) != null && country.getFtiRotw() < maxi.get(LimitTypeEnum.MAX_FTI_ROTW)) || country.getFtiRotw() < country.getFti()) {
                    country.setFtiRotw(country.getFtiRotw() + 1);

                    DiffAttributesEntity diffAttributes = DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.FTI_ROTW, country.getFtiRotw());
                    diffAttributes.setDiff(diff);
                    diff.getAttributes().add(diffAttributes);
                }
            }

            return diff;
        } else {
            return null;
        }
    }

    /**
     * Execute a planned administrative action of type exceptional levies.
     *
     * @param action  the planned administrative action.
     * @param game    the game.
     * @param country the country doing the action.
     * @param costs   various costs that could change with the action.
     * @return a List of Diff related to the action.
     */
    private DiffEntity executeExceptionalTaxes(AdministrativeActionEntity action, GameEntity game, PlayableCountryEntity country, Map<String, Integer> costs) {
        addInMap(costs, COST_EXC_LEVIES, action.getBonus());

        if (action.getColumn() == -1) {
            int stab = oeUtil.getStability(game, country.getName());
            stab--;
            String box = GameUtil.getStabilityBox(stab);

            return counterDomain.moveSpecialCounter(CounterFaceTypeEnum.STABILITY, country.getName(), box, game);
        } else {
            return null;
        }
    }

    /**
     * Execute a planned administrative action of type manufacture.
     *
     * @param action  the planned administrative action.
     * @param game    the game.
     * @param country the country doing the action.
     * @param costs   various costs that could change with the action.
     * @return a List of Diff related to the action.
     */
    private List<DiffEntity> executeEstablishment(AdministrativeActionEntity action, GameEntity game, PlayableCountryEntity country, Map<String, Integer> costs, Set<String> newEstablishments) {
        List<DiffEntity> diffs = new ArrayList<>();
        addInMap(costs, COST_ACTION, action.getCost());

        if (rollDie(action, game, country)) {
            // TODO check activation
            CounterEntity counter = null;
            if (action.getIdObject() != null) {
                counter = CommonUtil.findFirst(game.getStacks().stream().flatMap(s -> s.getCounters().stream()),
                        c -> c.getId().equals(action.getIdObject()));

            }

            if (counter == null) {
                // If establishment was not found, we try to find one of the good type belonging to the right country on the same province
                counter = CommonUtil.findFirst(game.getStacks().stream().filter(s -> StringUtils.equals(s.getProvince(), action.getProvince()))
                        .flatMap(s -> s.getCounters().stream()), c -> StringUtils.equals(c.getCountry(), country.getName()) &&
                        CounterUtil.getEstablishmentType(action.getType()) == CounterUtil.getFaceMinus(c.getType()));
            }

            if (counter == null) {
                newEstablishments.add(action.getProvince());
                List<CounterEntity> forts = game.getStacks().stream().filter(s -> StringUtils.equals(s.getProvince(), action.getProvince()))
                        .flatMap(s -> s.getCounters().stream()).filter(c -> c.getType() == CounterFaceTypeEnum.FORT).collect(Collectors.toList());
                for (CounterEntity fort : forts) {
                    diffs.add(counterDomain.removeCounter(fort));
                }

                // FIXME regroup mnu counter with other economic counters ?
                diffs.add(counterDomain.createCounter(CounterUtil.getEstablishmentType(action.getType()), country.getName(), action.getProvince(), 1, game));
            } else if (counter.getEstablishment() == null || counter.getEstablishment().getLevel() == null) {
                diffs.add(counterDomain.switchCounter(counter, counter.getType(), 1, game));
            } else if (counter.getEstablishment().getLevel() == 3) {
                diffs.add(counterDomain.switchCounter(counter, CounterUtil.getFacePlus(counter.getType()), 4, game));
            } else {
                diffs.add(counterDomain.switchCounter(counter, counter.getType(), counter.getEstablishment().getLevel() + 1, game));
            }

            // TODO exotic resources
        }
        return diffs;
    }

    /**
     * Execute a planned administrative action of type enhance technology (land or naval).
     *
     * @param action  the planned administrative action.
     * @param game    the game.
     * @param country the country doing the action.
     * @param costs   various costs that could change with the action.
     * @return a List of Diff related to the action.
     */
    private List<DiffEntity> executeTechnology(AdministrativeActionEntity action, GameEntity game, PlayableCountryEntity country, Map<String, Integer> costs) {
        List<DiffEntity> diffs = new ArrayList<>();
        boolean land = action.getType() == AdminActionTypeEnum.ELT;
        CounterFaceTypeEnum face;
        String actualTechName;
        Tech nextTech = null;
        if (land) {
            face = CounterFaceTypeEnum.TECH_LAND;
            actualTechName = country.getLandTech();
        } else {
            face = CounterFaceTypeEnum.TECH_NAVAL;
            actualTechName = country.getNavalTech();
        }
        addInMap(costs, COST_ACTION, action.getCost());

        rollDie(action, game, country);

        boolean nextTechKnown = false;
        Tech actualTech = CommonUtil.findFirst(getTables().getTechs(), t -> StringUtils.equals(t.getName(), actualTechName));
        List<Tech> higherTechs = getTables().getTechs().stream()
                .filter(t -> t.getBeginTurn() > actualTech.getBeginTurn() && t.isLand() == land)
                .collect(Collectors.toList());
        Collections.sort(higherTechs);

        if (higherTechs.size() > 0) {
            nextTech = higherTechs.get(0);
            nextTechKnown = nextTech.getBeginTurn() <= game.getTurn();
        }

        if (action.getResult() != ResultEnum.FUMBLE && action.getResult() != ResultEnum.FAILED &&
                (nextTechKnown || ((action.getResult() != ResultEnum.AVERAGE && action.getResult() != ResultEnum.AVERAGE_PLUS)
                        || (action.isSecondaryResult() != null && action.isSecondaryResult())))) {
            int boxesToImprove = 1;
            if (action.getResult() == ResultEnum.CRITICAL_HIT || (nextTechKnown && action.getResult() == ResultEnum.SUCCESS)) {
                boxesToImprove = 2;
            }
            action.setProvince(Integer.toString(boxesToImprove));

            int actualTechAdvance = oeUtil.getTechnologyAdvance(game, country.getName(), land);

            int targetBox = getAvailableTechBox(actualTechAdvance + boxesToImprove, land, game);
            diffs.add(counterDomain.moveSpecialCounter(face, country.getName(), GameUtil.getTechnologyBox(targetBox), game));

            if (nextTech != null) {
                String nextTechName = nextTech.getName();
                CounterEntity nextTechCounter = CommonUtil.findFirst(game.getStacks().stream().filter(s -> GameUtil.isTechnologyBox(s.getProvince()))
                                .flatMap(s -> s.getCounters().stream()),
                        c -> c.getType() == CounterUtil.getTechnologyType(nextTechName));

                int nextTechBox = GameUtil.getTechnology(nextTechCounter.getOwner().getProvince());

                if (nextTechBox < targetBox) {
                    if (land) {
                        country.setLandTech(nextTechName);
                    } else {
                        country.setNavalTech(nextTechName);
                    }

                    DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.COUNTRY, country.getId(),
                            DiffUtil.createDiffAttributes(land ? DiffAttributeTypeEnum.TECH_LAND : DiffAttributeTypeEnum.TECH_NAVAL, nextTechName));

                    diffs.add(diff);

                    // TODO TG-9 ignore fleet counters with only galleys
                    int upgradeCost = game.getStacks().stream().flatMap(s -> s.getCounters().stream())
                            .filter(c -> StringUtils.equals(c.getCountry(), country.getName()))
                            .collect(Collectors.summingInt(c -> CounterUtil.getUpgradeCost(c.getType(), land)));
                    addInMap(costs, COST_OTHER, upgradeCost);
                }
            }
        }

        return diffs;
    }

    /**
     * @param targetBox the number of technology box wanted.
     * @param land      type of technology.
     * @param game      the game.
     * @return the first available technology box greater or equals than targetBox.
     */
    private int getAvailableTechBox(int targetBox, boolean land, GameEntity game) {
        // TODO it is possible to go through box 70. Disable this behaviour when the conception of the 70+ will be made.
        targetBox = Math.min(targetBox, 70);
        String box = GameUtil.getTechnologyBox(targetBox);
        List<CounterEntity> neutralCounters = game.getStacks().stream().filter(s -> StringUtils.equals(s.getProvince(), box))
                .flatMap(s -> s.getCounters().stream()).filter(c -> !CounterUtil.canTechnologyStack(c.getType(), land, false))
                .collect(Collectors.toList());
        // A technology counter cannot stack with a neutral technology counter of same type. Go one box further if this would be the case.
        if (!neutralCounters.isEmpty()) {
            targetBox++;
        }
        return targetBox;
    }

    /**
     * @param counter whose we want the key.
     * @return the key used for computing fortress maintenance. It is a Pair consisting of level and location (<code>true</code> for ROTW).
     */
    private Pair<Integer, Boolean> getFortressKeyFromCounter(CounterEntity counter) {
        return new ImmutablePair<>(CounterUtil.getFortressLevelFromType(counter.getType()), GameUtil.isRotwProvince(counter.getOwner().getProvince()));
    }

    /**
     * Compute the automatic competitions for trade fleets. The trade fleet to destroy will be added to the newTfis.
     *
     * @param game    the game.
     * @param newTfis the trade fleets added during the administrative actions that will be updated with the destruction during automatic competition.
     */
    protected List<DiffEntity> computeAutomaticTfCompetitions(GameEntity game, Map<String, Map<String, Integer>> newTfis) {
        List<String> provinces = game.getTradeFleets().stream().map(TradeFleetEntity::getProvince).distinct().collect(Collectors.toList());
        newTfis.keySet().stream().filter(province -> !provinces.contains(province)).forEach(provinces::add);

        for (String province : provinces) {
            computeAutomaticTfCompetition(game, province, newTfis);
        }

        List<DiffEntity> diffs = new ArrayList<>();
        for (String tz : newTfis.keySet()) {
            for (String country : newTfis.get(tz).keySet()) {
                Integer levelChange = newTfis.get(tz).get(country);
                if (levelChange != null && levelChange != 0) {
                    CounterEntity counter = CommonUtil.findFirst(game.getStacks().stream()
                                    .filter(s -> StringUtils.equals(tz, s.getProvince()))
                                    .flatMap(s -> s.getCounters().stream()),
                            c -> StringUtils.equals(country, c.getCountry()) &&
                                    (c.getType() == CounterFaceTypeEnum.TRADING_FLEET_MINUS || c.getType() == CounterFaceTypeEnum.TRADING_FLEET_PLUS));
                    TradeFleetEntity tradeFleet = CommonUtil.findFirst(game.getTradeFleets(),
                            tf -> StringUtils.equals(tz, tf.getProvince()) && StringUtils.equals(country, tf.getCountry()));
                    int newLevel;
                    if (tradeFleet != null && tradeFleet.getLevel() != null) {
                        newLevel = tradeFleet.getLevel() + levelChange;
                    } else {
                        newLevel = levelChange;
                    }
                    CounterFaceTypeEnum newType;
                    if (newLevel >= 4) {
                        newType = CounterFaceTypeEnum.TRADING_FLEET_PLUS;
                    } else {
                        newType = CounterFaceTypeEnum.TRADING_FLEET_MINUS;
                    }

                    if (counter == null) {
                        // case newLevel is 0 ?
                        diffs.add(counterDomain.createCounter(newType, country, tz, newLevel, game));
                    } else if (newLevel != 0) {
                        diffs.add(counterDomain.switchCounter(counter, newType, newLevel, game));
                    } else {
                        diffs.add(counterDomain.removeCounter(counter));
                    }
                }
            }
        }
        return diffs;
    }

    /**
     * Compute the automatic competitions for establishments.
     *
     * @param game           the game.
     * @param establishments List of provinces where an establishment was created to check an automatic competition.
     */
    protected List<DiffEntity> computeAutomaticEstablishmentCompetitions(GameEntity game, Set<String> establishments) {
        List<DiffEntity> diffs = new ArrayList<>();
        for (String province : establishments) {
            diffs.addAll(computeAutomaticEstablishmentCompetition(game, province));
        }

        return diffs;
    }

    /**
     * Compute the automatic competitions for trade fleets for a single province. The trade fleet to destroy will be added to the newTfis.
     *
     * @param game     the game.
     * @param province the province where the competition occurs.
     * @param newTfis  the trade fleets added during the administrative actions that will be updated with the destruction during automatic competition.
     */
    private void computeAutomaticTfCompetition(GameEntity game, String province, Map<String, Map<String, Integer>> newTfis) {
        Map<String, Integer> tfPresents = game.getTradeFleets().stream()
                .filter(tf -> StringUtils.equals(tf.getProvince(), province) && tf.getLevel() != null &&
                        tf.getLevel() > 0)
                .collect(Collectors.groupingBy(TradeFleetEntity::getCountry, Collectors.summingInt(TradeFleetEntity::getLevel)));
        if (newTfis.get(province) != null) {
            for (String country : newTfis.get(province).keySet()) {
                if (tfPresents.containsKey(country)) {
                    tfPresents.put(country, tfPresents.get(country) + newTfis.get(province).get(country));
                } else {
                    tfPresents.put(country, newTfis.get(province).get(country));
                }
            }
        }

        AbstractProvinceEntity prov = provinceDao.getProvinceByName(province);

        computeSingleCompetition(game, prov, CompetitionTypeEnum.TF_6, tfPresents, map -> {
            boolean multiple = map.size() > 1;
            boolean has6 = map.values().stream().filter(level -> level == 6).count() >= 1;
            return multiple && has6;
        }, country -> additionalRemoveTfFromMaps(newTfis, province, country));

        Map<String, Integer> tfPresents4More = tfPresents.entrySet().stream()
                .filter(map -> map.getValue() >= 4)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        computeSingleCompetition(game, prov, CompetitionTypeEnum.TF_4, tfPresents4More, map -> map.size() > 1,
                country -> additionalRemoveTfFromMaps(newTfis, province, country));
    }

    /**
     * Compute the automatic competitions for establishments for a single province.
     *
     * @param game     the game.
     * @param province the province where the competition occurs.
     */
    private List<DiffEntity> computeAutomaticEstablishmentCompetition(GameEntity game, String province) {
        List<DiffEntity> diffs = new ArrayList<>();
        Map<String, Integer> estPresents = new HashMap<>();

        RotwProvinceEntity prov = (RotwProvinceEntity) provinceDao.getProvinceByName(province);

        Map<CounterFaceTypeEnum, List<CounterEntity>> establishmentsByType = game.getStacks().stream()
                .filter(s -> StringUtils.equals(s.getProvince(), province))
                .flatMap(s -> s.getCounters().stream()).filter(c -> CounterUtil.isEstablishment(c.getType()))
                .collect(Collectors.groupingBy(CounterEntity::getType, Collectors.toList()));

        List<CounterEntity> counterToDelete = new ArrayList<>();
        List<CounterEntity> counterInCompetition = new ArrayList<>();


        if (establishmentsByType.containsKey(CounterFaceTypeEnum.MINOR_ESTABLISHMENT_MINUS)
                || establishmentsByType.containsKey(CounterFaceTypeEnum.MINOR_ESTABLISHMENT_PLUS)) {
            addAll(counterToDelete, establishmentsByType.get(CounterFaceTypeEnum.TRADING_POST_MINUS));
            addAll(counterToDelete, establishmentsByType.get(CounterFaceTypeEnum.TRADING_POST_PLUS));
            addAll(counterToDelete, establishmentsByType.get(CounterFaceTypeEnum.COLONY_MINUS));
            addAll(counterToDelete, establishmentsByType.get(CounterFaceTypeEnum.COLONY_PLUS));
        } else if (establishmentsByType.containsKey(CounterFaceTypeEnum.COLONY_MINUS)
                || establishmentsByType.containsKey(CounterFaceTypeEnum.COLONY_PLUS)) {
            addAll(counterToDelete, establishmentsByType.get(CounterFaceTypeEnum.TRADING_POST_MINUS));
            addAll(counterToDelete, establishmentsByType.get(CounterFaceTypeEnum.TRADING_POST_PLUS));
            counterInCompetition.addAll(establishmentsByType.get(CounterFaceTypeEnum.COLONY_MINUS));
            counterInCompetition.addAll(establishmentsByType.get(CounterFaceTypeEnum.COLONY_PLUS));
        } else {
            counterInCompetition.addAll(establishmentsByType.get(CounterFaceTypeEnum.TRADING_POST_MINUS));
            counterInCompetition.addAll(establishmentsByType.get(CounterFaceTypeEnum.TRADING_POST_PLUS));
        }

        estPresents.putAll(counterInCompetition.stream()
                .collect(Collectors.groupingBy(CounterEntity::getCountry,
                        Collectors.summingInt(c -> c.getEstablishment() == null ? 1 : c.getEstablishment().getLevel()))));

        diffs.addAll(counterToDelete.stream()
                .map(establishment -> counterDomain.removeCounter(establishment))
                .collect(Collectors.toList()));

        Map<String, Integer> lostLevelInCompetition = new HashMap<>();

        computeSingleCompetition(game, prov, CompetitionTypeEnum.ESTABLISHMENT, estPresents,
                map -> map.size() > 1, country -> removeEstablishmentInCompetition(lostLevelInCompetition, country));

        for (String country : lostLevelInCompetition.keySet()) {
            Integer levelLost = lostLevelInCompetition.get(country);
            CounterEntity establishment = CommonUtil.findFirst(counterInCompetition, c -> StringUtils.equals(c.getCountry(), country));
            if (establishment != null && establishment.getEstablishment() != null && establishment.getEstablishment().getLevel() != null) {
                if (levelLost >= establishment.getEstablishment().getLevel()) {
                    diffs.add(counterDomain.removeCounter(establishment));
                } else {
                    int newLevel = establishment.getEstablishment().getLevel() - levelLost;
                    if (establishment.getEstablishment().getLevel() >= 4 && newLevel < 4) {
                        diffs.add(counterDomain.switchCounter(establishment, CounterUtil.getFaceMinus(establishment.getType()), newLevel, game));
                    } else {
                        diffs.add(counterDomain.switchCounter(establishment, establishment.getType(), newLevel, game));
                    }
                }

                // TODO loss of exotic resources
            } else {
                diffs.add(counterDomain.removeCounter(establishment));
                // TODO loss of exotic resources
            }
        }

        return diffs;
    }

    /**
     * Compute an automatic competition (given the type, the predicate and the func, it can be either
     * a partial trade fleet competition, a total trade fleet competition or an establishment competition.
     *
     * @param game       the game.
     * @param prov       the province where the competition occurs.
     * @param type       type of the competition.
     * @param tfPresents the trade fleets concerned by the competition, grouped by country.
     * @param predicate  the predicate to know the condition for continuing the competition.
     * @param func       additional function to call when a country loses a round of competition. Will be called with the parameter country.
     */
    private void computeSingleCompetition(GameEntity game, AbstractProvinceEntity prov, CompetitionTypeEnum type,
                                          Map<String, Integer> tfPresents, Predicate<Map<String, Integer>> predicate,
                                          Consumer<String> func) {
        if (predicate.test(tfPresents)) {
            CompetitionEntity competition = new CompetitionEntity();
            competition.setGame(game);
            competition.setProvince(prov.getName());
            competition.setTurn(game.getTurn());
            competition.setType(type);

            List<CompetitionInfo> infoList = new ArrayList<>();

            for (String country : tfPresents.keySet()) {
                CompetitionInfo info = new CompetitionInfo();

                info.country = country;
                info.fti = oeUtil.getFti(game, getTables(), country);
                if (prov instanceof TradeZoneProvinceEntity &&
                        StringUtils.equals(((TradeZoneProvinceEntity) prov).getCountryName(), country)) {
                    info.dti = oeUtil.getDti(game, getTables(), country);
                }

                infoList.add(info);
            }

            computeAutomaticCompetitionRound(competition, 1, infoList, game, tfPresents, predicate, func);

            game.getCompetitions().add(competition);
        }
    }

    /**
     * Computes a round of competition (trade fleet or establishment), if necessary.
     * A competition ends when the predicate returns <code>false</code>.
     *
     * @param competition the current competition.
     * @param roundNumber the number of the round to compute.
     * @param infoList    Info about the countries computing the competition.
     * @param game        the game.
     * @param predicate   Function that will return <code>true</code> if the competition should go on.
     * @param func        additional function to call when a country loses a round of competition. Will be called with the parameter country.
     */
    private void computeAutomaticCompetitionRound(CompetitionEntity competition, int roundNumber, List<CompetitionInfo> infoList,
                                                  GameEntity game, Map<String, Integer> tfPresents,
                                                  Predicate<Map<String, Integer>> predicate, Consumer<String> func) {
        List<String> countries = new ArrayList<>(tfPresents.keySet());
        List<CompetitionInfo> infoToRemove = new ArrayList<>();
        for (String country : countries) {
            CompetitionRoundEntity round = new CompetitionRoundEntity();
            round.setCountry(country);
            round.setCompetition(competition);
            competition.getRounds().add(round);
            round.setRound(roundNumber);

            CompetitionInfo info = CommonUtil.findFirst(infoList, inf -> StringUtils.equals(inf.country, country));
            int otherMaxFti = infoList.stream().filter(inf -> !StringUtils.equals(inf.country, country)).map(inf -> inf.fti).max((o1, o2) -> o1 - o2).get();
            int column = Math.min(Math.max(info.fti + info.dti - otherMaxFti, -4), 4);
            round.setColumn(column);

            Integer die = oeUtil.rollDie(game, country);
            round.setDie(die);

            Integer modifiedDie = Math.min(Math.max(die, 1), 10);
            Result result = CommonUtil.findFirst(getTables().getResults().stream(),
                    r -> r.getColumn().equals(column) && r.getDie().equals(modifiedDie));

            round.setResult(result.getResult());

            if (result.getResult() == ResultEnum.CRITICAL_HIT || result.getResult() == ResultEnum.SUCCESS) {
                continue;
            }

            if (result.getResult() == ResultEnum.AVERAGE || result.getResult() == ResultEnum.AVERAGE_PLUS) {
                die = oeUtil.rollDie(game, country);
                round.setSecondaryDie(die);

                if (die <= info.fti) {
                    round.setSecondaryResult(true);
                    continue;
                } else {
                    round.setSecondaryResult(false);
                }
            }

            if (tfPresents.get(country) == 1) {
                tfPresents.remove(country);
            } else {
                tfPresents.put(country, tfPresents.get(country) - 1);
            }
            if (func != null) {
                func.accept(country);
            }
            if ((competition.getType() == CompetitionTypeEnum.TF_6 || competition.getType() == CompetitionTypeEnum.ESTABLISHMENT)
                    && !tfPresents.containsKey(country)) {
                infoToRemove.add(info);
            } else if (competition.getType() == CompetitionTypeEnum.TF_4 && tfPresents.get(country) < 4) {
                tfPresents.remove(country);
                infoToRemove.add(info);
            }
        }

        infoToRemove.forEach(infoList::remove);
        if (predicate.test(tfPresents)) {
            computeAutomaticCompetitionRound(competition, roundNumber + 1, infoList, game, tfPresents, predicate, func);
        }
    }

    /**
     * Remove one trade fleet from both the current trade fleets map and the new trade fleets map.
     * Remove the key if the number become zero.
     *
     * @param newTfis  the trade fleets to add/remove grouped by province then by country.
     * @param province the trade zone where to remove the trade fleet.
     * @param country  the country loosing a trade fleet.
     */
    private void additionalRemoveTfFromMaps(Map<String, Map<String, Integer>> newTfis, String province, String country) {
        if (!newTfis.containsKey(province)) {
            newTfis.put(province, new HashMap<>());
        }
        if (!newTfis.get(province).containsKey(country)) {
            newTfis.get(province).put(country, -1);
        } else if (newTfis.get(province).get(country) == 1) {
            newTfis.get(province).remove(country);
        } else {
            newTfis.get(province).put(country, newTfis.get(province).get(country) - 1);
        }
    }

    /**
     * Add an estrablishment that will be removed at the end of the competition.
     *
     * @param lostLevelInCompetition tracks the summary of all establishment lost in the competition.
     * @param country                country losing a level of establishment.
     */
    private void removeEstablishmentInCompetition(Map<String, Integer> lostLevelInCompetition, String country) {
        if (!lostLevelInCompetition.containsKey(country)) {
            lostLevelInCompetition.put(country, 1);
        } else {
            lostLevelInCompetition.put(country, lostLevelInCompetition.get(country) + 1);
        }
    }

    /**
     * Computes all the automatic technology advances at the end of the administrative phase.
     * It consists of automatic progression of CultureGroup technology markers as well as
     * neutral technology adjustments.
     *
     * @param game the game.
     * @return The diffs involving all the technology advances.
     */
    protected List<DiffEntity> computeAutomaticTechnologyAdvances(GameEntity game) {
        List<DiffEntity> diffs = new ArrayList<>();

        // Each culture technology advance one box every few turns.
        for (CultureEnum culture : CultureEnum.values()) {
            if ((game.getTurn() - culture.getTechnologyShift()) % culture.getTechnologyFrequency() == 0) {
                DiffEntity diff = computeAutomaticCultureTechnology(culture, true, game);
                if (diff != null) {
                    diffs.add(diff);
                }
                diff = computeAutomaticCultureTechnology(culture, false, game);
                if (diff != null) {
                    diffs.add(diff);
                }
            }
        }

        // Then, each technology markers drops to the next marker of same type if the tech is already known
        // or drops one box if it is not already known but is reachable.
        for (Tech tech : getTables().getTechs()) {
            DiffEntity diff = computeAutomaticNeutralTechnology(tech, game);
            if (diff != null) {
                diffs.add(diff);
            }
        }

        return diffs;
    }

    /**
     * Computes the automatic progression of a CultureGroup technology marker.
     *
     * @param culture the culture to adjust.
     * @param land    either it is the land or naval technology.
     * @param game    the game.
     * @return the diff involving the technology adjustment. Can be <code>null</code>.
     */
    private DiffEntity computeAutomaticCultureTechnology(CultureEnum culture, boolean land, GameEntity game) {
        DiffEntity diff = null;
        CounterEntity counter = CommonUtil.findFirst(game.getStacks().stream().flatMap(s -> s.getCounters().stream())
                , c -> c.getType() == CounterUtil.getTechnologyGroup(culture, land));
        if (counter != null) {
            int actualBox = GameUtil.getTechnology(counter.getOwner().getProvince());
            int maxCountryTechBox = adminActionDao.getMaxTechBox(land, culture.getTechnologyCultures(), game.getId());

            int nextBox = Math.max(maxCountryTechBox - 6, actualBox + 1);

            nextBox = getAvailableTechBox(nextBox, land, game);

            diff = counterDomain.moveSpecialCounter(counter.getType(), null, GameUtil.getTechnologyBox(nextBox), game);
        }
        return diff;
    }

    /**
     * Computes the automatic progression of a neutral technology marker.
     *
     * @param tech the neutral technology to adjust.
     * @param game the game.
     * @return the diff involving the technology adjustment. Can be <code>null</code>.
     */
    private DiffEntity computeAutomaticNeutralTechnology(Tech tech, GameEntity game) {
        DiffEntity diff = null;

        // If the tech is not available, there is no adjustment
        if (tech.getBeginTurn() <= game.getTurn()) {
            CounterEntity counter = CommonUtil.findFirst(game.getStacks().stream().flatMap(s -> s.getCounters().stream())
                    , c -> c.getType() == CounterUtil.getTechnologyType(tech.getName()));
            if (counter != null) {
                int actualBox = GameUtil.getTechnology(counter.getOwner().getProvince());
                boolean techAlreadyKnown = game.getStacks().stream()
                        .filter(s -> GameUtil.isTechnologyBox(s.getProvince()) && GameUtil.getTechnology(s.getProvince()) > actualBox)
                        .flatMap(s -> s.getCounters().stream())
                        .filter(c -> !CounterUtil.canTechnologyStack(c.getType(), tech.isLand(), true))
                        .count() > 0;
                Optional<CounterEntity> previousCounter = game.getStacks().stream()
                        .filter(s -> GameUtil.isTechnologyBox(s.getProvince()) && GameUtil.getTechnology(s.getProvince()) < actualBox)
                        .flatMap(s -> s.getCounters().stream())
                        .filter(c -> !CounterUtil.canTechnologyStack(c.getType(), tech.isLand(), true))
                        .max((o1, o2) -> GameUtil.getTechnology(o1.getOwner().getProvince()) - GameUtil.getTechnology(o2.getOwner().getProvince()));
                Integer targetBox;
                if (previousCounter.isPresent()) {
                    CounterEntity previous = previousCounter.get();
                    targetBox = GameUtil.getTechnology(previous.getOwner().getProvince()) + 1;
                    if (CounterUtil.isNeutralTechnology(previous.getType())) {
                        targetBox++;
                    }
                    if (!techAlreadyKnown) {
                        // If tech is not known, the neutral counter drops at most
                        // of one box.
                        targetBox = Math.max(targetBox, actualBox - 1);
                    }
                } else {
                    // If there are no previous counter, then the neutral technology goes to the first box.
                    // It is not necessary.
                    targetBox = 1;
                }

                if (targetBox < actualBox) {
                    // If the neutral counter is already blocked, then we do nothing.
                    diff = counterDomain.moveSpecialCounter(counter.getType(), null, GameUtil.getTechnologyBox(targetBox), game);
                }
            }
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
     * Adds all the elements of the listToAdd to the listSource.
     *
     * @param listSource initial list.
     * @param listToAdd  list to add.
     * @param <E>        Generic parameter.
     */
    private <E> void addAll(List<E> listSource, List<E> listToAdd) {
        if (listSource != null && listToAdd != null) {
            listSource.addAll(listToAdd);
        }
    }

    /** Additional info when computing automatic competitions. */
    private class CompetitionInfo {
        /** Country on which the info are. */
        private String country;
        /** Fti of the country. */
        private int fti;
        /** Dti of the country if it is used. */
        private int dti;
    }
}
