package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.SimpleRequest;
import com.mkl.eu.client.service.service.IEconomicService;
import com.mkl.eu.client.service.service.eco.EconomicalSheetCountry;
import com.mkl.eu.client.service.service.eco.LoadEcoSheetsRequest;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeObjectEnum;
import com.mkl.eu.service.service.mapping.eco.EconomicalSheetMapping;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.eco.IEconomicalSheetDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.eco.EconomicalSheetEntity;
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
    /** EconomicalSheet DAO. */
    @Autowired
    private IEconomicalSheetDao economicalSheetDao;
    /** Counter DAO. */
    @Autowired
    private ICounterDao counterDao;
    /** Game mapping. */
    @Autowired
    private EconomicalSheetMapping ecoSheetsMapping;

    /** {@inheritDoc} */
    @Override
    public List<EconomicalSheetCountry> loadEcnomicSheets(SimpleRequest<LoadEcoSheetsRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_LOAD_ECO_SHEETS).setParams(METHOD_LOAD_ECO_SHEETS));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_LOAD_ECO_SHEETS, PARAMETER_REQUEST).setParams(METHOD_LOAD_ECO_SHEETS));

        List<EconomicalSheetEntity> sheetEntities = economicalSheetDao.loadSheets(
                request.getRequest().getIdGame(),
                request.getRequest().getIdCountry(),
                request.getRequest().getTurn());

        return ecoSheetsMapping.oesToVosCountry(sheetEntities);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse computeEconomicalSheets(Long idGame) {
        GameEntity game = gameDao.lock(idGame);

        for (PlayableCountryEntity country : game.getCountries()) {
            computeEconomicalSheet(country, game);
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
     * @param country the country.
     * @param game    the game.
     */
    private void computeEconomicalSheet(PlayableCountryEntity country, GameEntity game) {
        EconomicalSheetEntity sheet = CommonUtil.findFirst(country.getEconomicalSheets(), economicalSheetEntity -> economicalSheetEntity.getTurn().equals(game.getTurn()));
        if (sheet == null) {
            sheet = new EconomicalSheetEntity();
            sheet.setCountry(country);
            sheet.setTurn(game.getTurn());

            economicalSheetDao.create(sheet);

            country.getEconomicalSheets().add(sheet);
        }

        Map<String, Integer> provinces = economicalSheetDao.getOwnedAndControlledProvinces(country.getName(), game.getId());
        sheet.setProvincesIncome(provinces.values().stream().collect(Collectors.summingInt(value -> value)));

        Map<String, Integer> vassalProvinces = new HashMap<>();
        List<String> vassals = counterDao.getVassals(country.getName(), game.getId());
        for (String vassal : vassals) {
            vassalProvinces.putAll(economicalSheetDao.getOwnedAndControlledProvinces(vassal, game.getId()));
        }
        sheet.setVassalIncome(vassalProvinces.values().stream().collect(Collectors.summingInt(value -> value)));

        List<String> provinceNames = new ArrayList<>();
        provinceNames.addAll(provinces.keySet());
        provinceNames.addAll(vassalProvinces.keySet());
        List<String> pillagedProvinces = economicalSheetDao.getPillagedProvinces(provinceNames, game.getId());

        Integer pillagedIncome = pillagedProvinces.stream().collect(Collectors.summingInt(provinces::get));

        sheet.setPillages(pillagedIncome);

        sheet.setLandIncome(add(sheet.getProvincesIncome(), sheet.getVassalIncome(), sheet.getPillages(), sheet.getEventLandIncome()));
    }

    /**
     * Add several Integer that can be <code>null</code>.
     *
     * @param numbers to add.
     * @return the sum of the numbers.
     */
    private Integer add(Integer... numbers) {
        Integer sum = null;

        for (Integer number : numbers) {
            if (sum == null) {
                sum = number;
            } else if (number != null) {
                sum = sum + number;
            }
        }

        return sum;
    }
}
