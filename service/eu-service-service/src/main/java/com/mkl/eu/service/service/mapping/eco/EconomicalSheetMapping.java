package com.mkl.eu.service.service.mapping.eco;

import com.mkl.eu.client.service.service.eco.EconomicalSheetCountry;
import com.mkl.eu.client.service.vo.eco.EconomicalSheet;
import com.mkl.eu.service.service.mapping.AbstractMapping;
import com.mkl.eu.service.service.persistence.oe.eco.EconomicalSheetEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapping between VO and OE for an Economical sheet.
 *
 * @author MKL.
 */
@Component
public class EconomicalSheetMapping extends AbstractMapping {

    /**
     * OEs to VOs.
     *
     * @param sources        object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public List<EconomicalSheet> oesToVos(List<EconomicalSheetEntity> sources, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (sources == null) {
            return null;
        }

        List<EconomicalSheet> targets = new ArrayList<>();

        for (EconomicalSheetEntity source : sources) {
            EconomicalSheet target = storeVo(EconomicalSheet.class, source, objectsCreated, this::oeToVo);
            if (target != null) {
                targets.add(target);
            }
        }

        return targets;
    }

    /**
     * OE to VO.
     *
     * @param source         object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public EconomicalSheet oeToVo(EconomicalSheetEntity source, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (source == null) {
            return null;
        }

        EconomicalSheet target = new EconomicalSheet();

        target.setId(source.getId());
        target.setActCampExpense(source.getActCampExpense());
        target.setAdminActExpense(source.getAdminActExpense());
        target.setAdminReactExpense(source.getAdminReactExpense());
        target.setAdmTotalExpense(source.getAdmTotalExpense());
        target.setColIncome(source.getColIncome());
        target.setDiploActions(source.getDiploActions());
        target.setDiploReactions(source.getDiploReactions());
        target.setDomTradeIncome(source.getDomTradeIncome());
        target.setEventIncome(source.getEventIncome());
        target.setEventLandIncome(source.getEventLandIncome());
        target.setExcRecruitExpense(source.getExcRecruitExpense());
        target.setExcTaxes(source.getExcTaxes());
        target.setExcTaxesMod(source.getExcTaxesMod());
        target.setExoResIncome(source.getExoResIncome());
        target.setExpenses(source.getExpenses());
        target.setFleetLevelIncome(source.getFleetLevelIncome());
        target.setFleetMonopIncome(source.getFleetMonopIncome());
        target.setFortMaintExpense(source.getFortMaintExpense());
        target.setFortPurchExpense(source.getFortPurchExpense());
        target.setForTradeIncome(source.getForTradeIncome());
        target.setGoldIncome(source.getGoldIncome());
        target.setGoldRotw(source.getGoldRotw());
        target.setGrossIncome(source.getGrossIncome());
        target.setIncome(source.getIncome());
        target.setIndustrialIncome(source.getIndustrialIncome());
        target.setInflation(source.getInflation());
        target.setInterBankrupt(source.getInterBankrupt());
        target.setInterestExpense(source.getInterestExpense());
        target.setInterLoan(source.getInterLoan());
        target.setInterLoanInterests(source.getInterLoanInterests());
        target.setInterLoanNew(source.getInterLoanNew());
        target.setInterLoanRefund(source.getInterLoanRefund());
        target.setLandIncome(source.getLandIncome());
        target.setLoans(source.getLoans());
        target.setLostIncome(source.getLostIncome());
        target.setMajCampExpense(source.getMajCampExpense());
        target.setMandRefundExpense(source.getMandRefundExpense());
        target.setMaxInterLoan(source.getMaxInterLoan());
        target.setMaxNatLoan(source.getMaxNatLoan());
        target.setMilitaryExpense(source.getMilitaryExpense());
        target.setMissMaintExpense(source.getMissMaintExpense());
        target.setMnuIncome(source.getMnuIncome());
        target.setMultCampExpense(source.getMultCampExpense());
        target.setNatLoan(source.getNatLoan());
        target.setNatLoanBankrupt(source.getNatLoanBankrupt());
        target.setNatLoanEnd(source.getNatLoanEnd());
        target.setNatLoanInterest(source.getNatLoanInterest());
        target.setNatLoanNew(source.getNatLoanNew());
        target.setNatLoanRefund(source.getNatLoanRefund());
        target.setNatLoanStart(source.getNatLoanStart());
        target.setNavalRefitExpense(source.getNavalRefitExpense());
        target.setOptRefundExpense(source.getOptRefundExpense());
        target.setOtherExpense(source.getOtherExpense());
        target.setPassCampExpense(source.getPassCampExpense());
        target.setPeace(source.getPeace());
        target.setPeriodWealth(source.getPeriodWealth());
        target.setPillages(source.getPillages());
        target.setPraesidioExpense(source.getPraesidioExpense());
        target.setPrestigeIncome(source.getPrestigeIncome());
        target.setPrestigeSpent(source.getPrestigeSpent());
        target.setPrestigeVP(source.getPrestigeVP());
        target.setProvincesIncome(source.getProvincesIncome());
        target.setRegularIncome(source.getRegularIncome());
        target.setRemainingExpenses(source.getRemainingExpenses());
        target.setRotwIncome(source.getRotwIncome());
        target.setRtAftExch(source.getRtAftExch());
        target.setRtBalance(source.getRtBalance());
        target.setRtBefExch(source.getRtBefExch());
        target.setRtCollapse(source.getRtCollapse());
        target.setRtDiplo(source.getRtDiplo());
        target.setRtEnd(source.getRtEnd());
        target.setRtEvents(source.getRtEvents());
        target.setRtPeace(source.getRtPeace());
        target.setRtStart(source.getRtStart());
        target.setSpecialIncome(source.getSpecialIncome());
        target.setStab(source.getStab());
        target.setSubsidies(source.getSubsidies());
        target.setTpIncome(source.getTpIncome());
        target.setTradeCenterIncome(source.getTradeCenterIncome());
        target.setTradeCenterLoss(source.getTradeCenterLoss());
        target.setTradeIncome(source.getTradeIncome());
        target.setTurn(source.getTurn());
        target.setUnitMaintExpense(source.getUnitMaintExpense());
        target.setUnitPurchExpense(source.getUnitPurchExpense());
        target.setVassalIncome(source.getVassalIncome());
        target.setWealth(source.getWealth());
        target.setWoodSlaves(source.getWoodSlaves());

        return target;
    }

    /**
     * OEs to VOs.
     *
     * @param sources object source.
     * @return object mapped.
     */
    public List<EconomicalSheetCountry> oesToVosCountry(List<EconomicalSheetEntity> sources) {
        if (sources == null) {
            return null;
        }

        List<EconomicalSheetCountry> targets = new ArrayList<>();

        Map<Class<?>, Map<Long, Object>> objectsCreated = new HashMap<>();

        for (EconomicalSheetEntity source : sources) {
            EconomicalSheetCountry target = oeToVoCountry(source, objectsCreated);
            if (target != null) {
                targets.add(target);
            }
        }

        return targets;
    }

    /**
     * OE to VO.
     *
     * @param source         object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public EconomicalSheetCountry oeToVoCountry(EconomicalSheetEntity source, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (source == null) {
            return null;
        }

        EconomicalSheetCountry target = new EconomicalSheetCountry();
        target.setSheet(oeToVo(source, objectsCreated));
        if (source.getCountry() != null) {
            target.setIdCountry(source.getCountry().getId());
        }

        return target;
    }
}
