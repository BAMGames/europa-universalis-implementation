package com.mkl.eu.client.service.vo.eco;

import com.mkl.eu.client.service.vo.EuObject;

/**
 * Economical sheet of a given player at a given turn.
 *
 * @author MKL
 */
public class EconomicalSheet extends EuObject<Long> {
    /** Turn of the sheet. */
    private Integer turn;

    /*********************************************************************************************************
     *                           Economic record sheet A - Royal Treasure                                    *
     *********************************************************************************************************/
    /** Royal treasure at start of turn. Line 1 of sheet A. */
    private Integer rtStart;
    /** Royal treasure after events. Line 2 of sheet A. */
    private Integer rtEvents;
    /** Gifts and loans between players. Line 3 of sheet A. */
    private Integer loans;
    /** Wood and slaves (negative if bought, positive if sold). Line 4 of sheet A. */
    private Integer woodSlaves;
    /** Diplomatic actions. Line 5 of sheet A. */
    private Integer diploActions;
    /** Diplomatic reactions. Line 6 of sheet A. */
    private Integer diploReactions;
    /** Subsidies and dowries. Line 7 of sheet A. */
    private Integer subsidies;
    /** Royal treasure after diplomacy. Line 8 of sheet A. */
    private Integer roDiplo;
    /** Pilages and privateers. Line 9 of sheet A. */
    private Integer pillages;
    /** Gold from ROTW and convoys. Line 10 of sheet A. */
    private Integer goldRotw;
    /** Exceptionl taxes. Line 12 of sheet A. */
    private Integer excTaxes;
    /** Royal treasure before Exchequer. Line 13 of sheet A. */
    private Integer rtBefExch;
    /** Regular income. Line 15 of sheet A. */
    private Integer regularIncome;
    /** Prestige income. Line 16 of sheet A. */
    private Integer prestigeIncome;
    /** Maximum national loan. Line 17 of sheet A. */
    private Integer maxNatLoan;
    /** Maximum international loan. Line 18 of sheet A. */
    private Integer maxInterLoan;
    /** Remaining expenses. Line 20 of sheet A. */
    private Integer remainingExpenses;
    /** Prestige spent on expenses. Line 21 of sheet A. */
    private Integer prestigeSpent;
    /** National loan spent on expenses. Line 22 of sheet A. */
    private Integer natLoan;
    /** International loan spent on expenses. Line 23 of sheet A. */
    private Integer interLoan;
    /** Royal treasure balance. Line 24 of sheet A. */
    private Integer rtBalance;
    /** Royal treasure after Exchequer test. Line 25 of sheet A. */
    private Integer rtAftExch;
    /** Prestige spent in victory points. Line 26 of sheet A. */
    private Integer prestigeVP;
    /** Wealth. Line 27 of sheet A. */
    private Integer wealth;
    /** Period wealth. Line 28 of sheet A. */
    private Integer periodWealth;
    /** Stability improvement expense. Line 29 of sheet A. */
    private Integer stab;
    /** Ransom, peace treaties (negative if expense, positive if income). */
    private Integer peace;
    /** Royal treasure after peace. */
    private Integer rtPeace;
    /** Inflation. X% of |RT|, minimum X. Line 32 of sheet A. */
    private Integer inflation;
    /** Royal treasure at the end of the turn. Line 33 of sheet A. */
    private Integer rtEnd;

    /*********************************************************************************************************
     *                           Economic record sheet C - Loans                                             *
     *********************************************************************************************************/

    /** New international loans. Line 1 of sheet C. */
    private Integer interLoanNew;
    /** International loans interests. Line 2 of sheet C. */
    private Integer interLoanInterests;
    /** International loans refunds. Line 3 of sheet C. */
    private Integer interLoanRefund;
    /** International bankruptcy. Line 4 of sheet C. */
    private Integer interBankrupt;
    /** National loans at start of turn. Line 5 of sheet C. */
    private Integer natLoanStart;
    /** National loans interests. Line 6 of sheet C. */
    private Integer natLoanInterest;
    /** National loans bankruptcy. Line 7 of sheet C. */
    private Integer natLoanBankrupt;
    /** National loans refunds. Line 8 of sheet C. */
    private Integer natLoanRefund;
    /** New national loans. Line 9 of sheet C. */
    private Integer natLoanNew;
    /** National loans at end of turn. Line 10 of sheet C. */
    private Integer natLoanEnd;

    /*********************************************************************************************************
     *                           Economic record sheet B - Income                                            *
     *********************************************************************************************************/

    /*********************************************************************************************************
     *                                            Income                                                     *
     *********************************************************************************************************/

    /*********************************************************************************************************
     *                                            Land income                                                *
     *********************************************************************************************************/
    /** Provinces income. Line 1 of sheet B. */
    private Integer provincesIncome;
    /** VassalProvinces income. Line 2 of sheet B. */
    private Integer vassalIncome;
    /** Income lost due to pillages, revolts or pashas. Line 3 of sheet B. */
    private Integer lostIncome;
    /** Variation of land income due to events. Can be negative. Line 4 of sheet B. */
    private Integer eventLandIncome;
    /** Land income. Summary of the provinces, vassal, lost and event incomes. Line 5 of sheet B. */
    private Integer landIncome;

    /*********************************************************************************************************
     *                                        Domestic income                                                *
     *********************************************************************************************************/
    /** Manufactures income. Line 6 of sheet B. */
    private Integer MnuIncome;
    /** European gold mines income. Line 7 of sheet B. */
    private Integer goldIncome;
    /** Industrial income. Summary of the manufactures and european gold mines income. Line 8 of sheet B. */
    private Integer industrialIncome;

    /*********************************************************************************************************
     *                                           Trade income                                                *
     *********************************************************************************************************/
    /** Domestic trace income. Based on provinces and vassal income crossed with the DTI. Line 9 of sheet B. */
    private Integer domTradeIncome;
    /** Foreign trade income. Based on trade refusal crossed with the FTI. Line 10 of sheet B. */
    private Integer forTradeIncome;
    /** Fleet level income. Line 11 of sheet B. */
    private Integer fleetLevelIncome;
    /** Fleet monopoly income. Line 12 of sheet B. */
    private Integer fleetMonopIncome;
    /** Trace centres income. Line 14 of sheet B. */
    private Integer tradeCenterIncome;
    /** Trace centres losses. Line 15 of sheet B. */
    private Integer tradeCenterLoss;
    /** Trade income. Summary of the various trace income. Line 16 of sheet B. */
    private Integer tradeIncome;

    /*********************************************************************************************************
     *                                         ROTW Income                                                   *
     *********************************************************************************************************/

    /** Colonies income (without exotic resources). Line 17 of sheet B. */
    private Integer colIncome;
    /** Trading posts income (without exotic resources). Line 18 of sheet B. */
    private Integer tpIncome;
    /** Exotic resources income. Line 19 of sheet B. */
    private Integer exoResIncome;
    /** ROTW income. Summary of the colonies, trading posts and exotic resources income. Line 21 of sheet B. */
    private Integer rotwIncome;

    /*********************************************************************************************************
     *                                        Other Income                                                   *
     *********************************************************************************************************/

    /** Special income (Portugal in annexion for Spain for example). Line 22 of sheet B. */
    private Integer specialIncome;
    /** Sum of the other summaries income. Line 23 of sheet B. */
    private Integer income;
    /** Variation of income due to events. Can be negative. Line 24 of sheet B. */
    private Integer eventIncome;
    /** Gross income. Line 25 of sheet B. */
    private Integer grossIncome;

    /*********************************************************************************************************
     *                                            Expenses                                                   *
     *********************************************************************************************************/

    /** Loan interests. Line 26 of sheet B. */
    private Integer interestExpense;
    /** Mandatory loan refund. Line 27 of sheet B. */
    private Integer mandRefundExpense;
    /** RT Collapse if negative. Line 28 of sheet B. */
    private Integer rtCollapse;

    /*********************************************************************************************************
     *                                Administrative Expenses                                                *
     *********************************************************************************************************/

    /** Optional loan refund. Line 29 of sheet B. */
    private Integer optRefundExpense;
    /** Unit maintenance. Line 30 of sheet B. */
    private Integer unitMaintExpense;
    /** Fortresses and praesidios maintenance. Line 31 of sheet B. */
    private Integer fortMaintExpense;
    /** Missions maintenance. Line 32 of sheet B. */
    private Integer missMaintExpense;
    /** Units purchase. Line 33 of sheet B. */
    private Integer unitPurchExpense;
    /** Fortresses purchase. Line 34 of sheet B. */
    private Integer fortPurchExpense;
    /** Administrative actions. Line 35 of sheet B. */
    private Integer adminActExpense;
    /** Adminitrative reactions. Line 36 of sheet B. */
    private Integer adminReactExpense;
    /** Other expenses. Line 37 of sheet B. */
    private Integer otherExpense;
    /** Administrative total. Summary of administrative expenses. Line 38 of sheet B. */
    private Integer admTotalExpense;
    /** Exceptional taxes modifier. Line 39 of sheet B. */
    private Integer excTaxesMod;

    /*********************************************************************************************************
     *                                  Military Expenses                                                    *
     *********************************************************************************************************/

    /** Passive campaigns. Line 40 of sheet B. */
    private Integer passCampExpense;
    /** Active campaigns. Line 41 of sheet B. */
    private Integer actCampExpense;
    /** Major campaigns. Line 42 of sheet B. */
    private Integer majCampExpense;
    /** Multiple campaigns. Line 43 of sheet B. */
    private Integer multCampExpense;
    /** Exceptional recruitments. Line 44 of sheet B. */
    private Integer excRecruitExpense;
    /** Naval refit. Line 45 of sheet B. */
    private Integer navalRefitExpense;
    /** Praesidios build/upgrade. Line 46 of sheet B. */
    private Integer praesidioExpense;
    /** Summary of the military expenses. Line 47 of sheet B. */
    private Integer militaryExpense;
    /** Total expenses. Summary of all the expenses. Line 48 of sheet B. */
    private Integer expenses;

    /** @return the turn. */
    public Integer getTurn() {
        return turn;
    }

    /** @param turn the turn to set. */
    public void setTurn(Integer turn) {
        this.turn = turn;
    }

    /** @return the rtStart. */
    public Integer getRtStart() {
        return rtStart;
    }

    /** @param rtStart the rtStart to set. */
    public void setRtStart(Integer rtStart) {
        this.rtStart = rtStart;
    }

    /** @return the rtEvents. */
    public Integer getRtEvents() {
        return rtEvents;
    }

    /** @param rtEvents the rtEvents to set. */
    public void setRtEvents(Integer rtEvents) {
        this.rtEvents = rtEvents;
    }

    /** @return the loans. */
    public Integer getLoans() {
        return loans;
    }

    /** @param loans the loans to set. */
    public void setLoans(Integer loans) {
        this.loans = loans;
    }

    /** @return the woodSlaves. */
    public Integer getWoodSlaves() {
        return woodSlaves;
    }

    /** @param woodSlaves the woodSlaves to set. */
    public void setWoodSlaves(Integer woodSlaves) {
        this.woodSlaves = woodSlaves;
    }

    /** @return the diploActions. */
    public Integer getDiploActions() {
        return diploActions;
    }

    /** @param diploActions the diploActions to set. */
    public void setDiploActions(Integer diploActions) {
        this.diploActions = diploActions;
    }

    /** @return the diploReactions. */
    public Integer getDiploReactions() {
        return diploReactions;
    }

    /** @param diploReactions the diploReactions to set. */
    public void setDiploReactions(Integer diploReactions) {
        this.diploReactions = diploReactions;
    }

    /** @return the subsidies. */
    public Integer getSubsidies() {
        return subsidies;
    }

    /** @param subsidies the subsidies to set. */
    public void setSubsidies(Integer subsidies) {
        this.subsidies = subsidies;
    }

    /** @return the roDiplo. */
    public Integer getRoDiplo() {
        return roDiplo;
    }

    /** @param roDiplo the roDiplo to set. */
    public void setRoDiplo(Integer roDiplo) {
        this.roDiplo = roDiplo;
    }

    /** @return the pillages. */
    public Integer getPillages() {
        return pillages;
    }

    /** @param pillages the pillages to set. */
    public void setPillages(Integer pillages) {
        this.pillages = pillages;
    }

    /** @return the goldRotw. */
    public Integer getGoldRotw() {
        return goldRotw;
    }

    /** @param goldRotw the goldRotw to set. */
    public void setGoldRotw(Integer goldRotw) {
        this.goldRotw = goldRotw;
    }

    /** @return the excTaxes. */
    public Integer getExcTaxes() {
        return excTaxes;
    }

    /** @param excTaxes the excTaxes to set. */
    public void setExcTaxes(Integer excTaxes) {
        this.excTaxes = excTaxes;
    }

    /** @return the rtBefExch. */
    public Integer getRtBefExch() {
        return rtBefExch;
    }

    /** @param rtBefExch the rtBefExch to set. */
    public void setRtBefExch(Integer rtBefExch) {
        this.rtBefExch = rtBefExch;
    }

    /** @return the regularIncome. */
    public Integer getRegularIncome() {
        return regularIncome;
    }

    /** @param regularIncome the regularIncome to set. */
    public void setRegularIncome(Integer regularIncome) {
        this.regularIncome = regularIncome;
    }

    /** @return the prestigeIncome. */
    public Integer getPrestigeIncome() {
        return prestigeIncome;
    }

    /** @param prestigeIncome the prestigeIncome to set. */
    public void setPrestigeIncome(Integer prestigeIncome) {
        this.prestigeIncome = prestigeIncome;
    }

    /** @return the maxNatLoan. */
    public Integer getMaxNatLoan() {
        return maxNatLoan;
    }

    /** @param maxNatLoan the maxNatLoan to set. */
    public void setMaxNatLoan(Integer maxNatLoan) {
        this.maxNatLoan = maxNatLoan;
    }

    /** @return the maxInterLoan. */
    public Integer getMaxInterLoan() {
        return maxInterLoan;
    }

    /** @param maxInterLoan the maxInterLoan to set. */
    public void setMaxInterLoan(Integer maxInterLoan) {
        this.maxInterLoan = maxInterLoan;
    }

    /** @return the remainingExpenses. */
    public Integer getRemainingExpenses() {
        return remainingExpenses;
    }

    /** @param remainingExpenses the remainingExpenses to set. */
    public void setRemainingExpenses(Integer remainingExpenses) {
        this.remainingExpenses = remainingExpenses;
    }

    /** @return the prestigeSpent. */
    public Integer getPrestigeSpent() {
        return prestigeSpent;
    }

    /** @param prestigeSpent the prestigeSpent to set. */
    public void setPrestigeSpent(Integer prestigeSpent) {
        this.prestigeSpent = prestigeSpent;
    }

    /** @return the natLoan. */
    public Integer getNatLoan() {
        return natLoan;
    }

    /** @param natLoan the natLoan to set. */
    public void setNatLoan(Integer natLoan) {
        this.natLoan = natLoan;
    }

    /** @return the interLoan. */
    public Integer getInterLoan() {
        return interLoan;
    }

    /** @param interLoan the interLoan to set. */
    public void setInterLoan(Integer interLoan) {
        this.interLoan = interLoan;
    }

    /** @return the rtBalance. */
    public Integer getRtBalance() {
        return rtBalance;
    }

    /** @param rtBalance the rtBalance to set. */
    public void setRtBalance(Integer rtBalance) {
        this.rtBalance = rtBalance;
    }

    /** @return the rtAftExch. */
    public Integer getRtAftExch() {
        return rtAftExch;
    }

    /** @param rtAftExch the rtAftExch to set. */
    public void setRtAftExch(Integer rtAftExch) {
        this.rtAftExch = rtAftExch;
    }

    /** @return the prestigeVP. */
    public Integer getPrestigeVP() {
        return prestigeVP;
    }

    /** @param prestigeVP the prestigeVP to set. */
    public void setPrestigeVP(Integer prestigeVP) {
        this.prestigeVP = prestigeVP;
    }

    /** @return the wealth. */
    public Integer getWealth() {
        return wealth;
    }

    /** @param wealth the wealth to set. */
    public void setWealth(Integer wealth) {
        this.wealth = wealth;
    }

    /** @return the periodWealth. */
    public Integer getPeriodWealth() {
        return periodWealth;
    }

    /** @param periodWealth the periodWealth to set. */
    public void setPeriodWealth(Integer periodWealth) {
        this.periodWealth = periodWealth;
    }

    /** @return the stab. */
    public Integer getStab() {
        return stab;
    }

    /** @param stab the stab to set. */
    public void setStab(Integer stab) {
        this.stab = stab;
    }

    /** @return the peace. */
    public Integer getPeace() {
        return peace;
    }

    /** @param peace the peace to set. */
    public void setPeace(Integer peace) {
        this.peace = peace;
    }

    /** @return the rtPeace. */
    public Integer getRtPeace() {
        return rtPeace;
    }

    /** @param rtPeace the rtPeace to set. */
    public void setRtPeace(Integer rtPeace) {
        this.rtPeace = rtPeace;
    }

    /** @return the inflation. */
    public Integer getInflation() {
        return inflation;
    }

    /** @param inflation the inflation to set. */
    public void setInflation(Integer inflation) {
        this.inflation = inflation;
    }

    /** @return the rtEnd. */
    public Integer getRtEnd() {
        return rtEnd;
    }

    /** @param rtEnd the rtEnd to set. */
    public void setRtEnd(Integer rtEnd) {
        this.rtEnd = rtEnd;
    }

    /** @return the interLoanNew. */
    public Integer getInterLoanNew() {
        return interLoanNew;
    }

    /** @param interLoanNew the interLoanNew to set. */
    public void setInterLoanNew(Integer interLoanNew) {
        this.interLoanNew = interLoanNew;
    }

    /** @return the interLoanInterests. */
    public Integer getInterLoanInterests() {
        return interLoanInterests;
    }

    /** @param interLoanInterests the interLoanInterests to set. */
    public void setInterLoanInterests(Integer interLoanInterests) {
        this.interLoanInterests = interLoanInterests;
    }

    /** @return the interLoanRefund. */
    public Integer getInterLoanRefund() {
        return interLoanRefund;
    }

    /** @param interLoanRefund the interLoanRefund to set. */
    public void setInterLoanRefund(Integer interLoanRefund) {
        this.interLoanRefund = interLoanRefund;
    }

    /** @return the interBankrupt. */
    public Integer getInterBankrupt() {
        return interBankrupt;
    }

    /** @param interBankrupt the interBankrupt to set. */
    public void setInterBankrupt(Integer interBankrupt) {
        this.interBankrupt = interBankrupt;
    }

    /** @return the natLoanStart. */
    public Integer getNatLoanStart() {
        return natLoanStart;
    }

    /** @param natLoanStart the natLoanStart to set. */
    public void setNatLoanStart(Integer natLoanStart) {
        this.natLoanStart = natLoanStart;
    }

    /** @return the natLoanInterest. */
    public Integer getNatLoanInterest() {
        return natLoanInterest;
    }

    /** @param natLoanInterest the natLoanInterest to set. */
    public void setNatLoanInterest(Integer natLoanInterest) {
        this.natLoanInterest = natLoanInterest;
    }

    /** @return the natLoanBankrupt. */
    public Integer getNatLoanBankrupt() {
        return natLoanBankrupt;
    }

    /** @param natLoanBankrupt the natLoanBankrupt to set. */
    public void setNatLoanBankrupt(Integer natLoanBankrupt) {
        this.natLoanBankrupt = natLoanBankrupt;
    }

    /** @return the natLoanRefund. */
    public Integer getNatLoanRefund() {
        return natLoanRefund;
    }

    /** @param natLoanRefund the natLoanRefund to set. */
    public void setNatLoanRefund(Integer natLoanRefund) {
        this.natLoanRefund = natLoanRefund;
    }

    /** @return the natLoanNew. */
    public Integer getNatLoanNew() {
        return natLoanNew;
    }

    /** @param natLoanNew the natLoanNew to set. */
    public void setNatLoanNew(Integer natLoanNew) {
        this.natLoanNew = natLoanNew;
    }

    /** @return the natLoanEnd. */
    public Integer getNatLoanEnd() {
        return natLoanEnd;
    }

    /** @param natLoanEnd the natLoanEnd to set. */
    public void setNatLoanEnd(Integer natLoanEnd) {
        this.natLoanEnd = natLoanEnd;
    }

    /** @return the provincesIncome. */
    public Integer getProvincesIncome() {
        return provincesIncome;
    }

    /** @param provincesIncome the provincesIncome to set. */
    public void setProvincesIncome(Integer provincesIncome) {
        this.provincesIncome = provincesIncome;
    }

    /** @return the vassalIncome. */
    public Integer getVassalIncome() {
        return vassalIncome;
    }

    /** @param vassalIncome the vassalIncome to set. */
    public void setVassalIncome(Integer vassalIncome) {
        this.vassalIncome = vassalIncome;
    }

    /** @return the lostIncome. */
    public Integer getLostIncome() {
        return lostIncome;
    }

    /** @param lostIncome the lostIncome to set. */
    public void setLostIncome(Integer lostIncome) {
        this.lostIncome = lostIncome;
    }

    /** @return the eventLandIncome. */
    public Integer getEventLandIncome() {
        return eventLandIncome;
    }

    /** @param eventLandIncome the eventLandIncome to set. */
    public void setEventLandIncome(Integer eventLandIncome) {
        this.eventLandIncome = eventLandIncome;
    }

    /** @return the landIncome. */
    public Integer getLandIncome() {
        return landIncome;
    }

    /** @param landIncome the landIncome to set. */
    public void setLandIncome(Integer landIncome) {
        this.landIncome = landIncome;
    }

    /** @return the MnuIncome. */
    public Integer getMnuIncome() {
        return MnuIncome;
    }

    /** @param MnuIncome the MnuIncome to set. */
    public void setMnuIncome(Integer mnuIncome) {
        MnuIncome = mnuIncome;
    }

    /** @return the goldIncome. */
    public Integer getGoldIncome() {
        return goldIncome;
    }

    /** @param goldIncome the goldIncome to set. */
    public void setGoldIncome(Integer goldIncome) {
        this.goldIncome = goldIncome;
    }

    /** @return the industrialIncome. */
    public Integer getIndustrialIncome() {
        return industrialIncome;
    }

    /** @param industrialIncome the industrialIncome to set. */
    public void setIndustrialIncome(Integer industrialIncome) {
        this.industrialIncome = industrialIncome;
    }

    /** @return the domTradeIncome. */
    public Integer getDomTradeIncome() {
        return domTradeIncome;
    }

    /** @param domTradeIncome the domTradeIncome to set. */
    public void setDomTradeIncome(Integer domTradeIncome) {
        this.domTradeIncome = domTradeIncome;
    }

    /** @return the forTradeIncome. */
    public Integer getForTradeIncome() {
        return forTradeIncome;
    }

    /** @param forTradeIncome the forTradeIncome to set. */
    public void setForTradeIncome(Integer forTradeIncome) {
        this.forTradeIncome = forTradeIncome;
    }

    /** @return the fleetLevelIncome. */
    public Integer getFleetLevelIncome() {
        return fleetLevelIncome;
    }

    /** @param fleetLevelIncome the fleetLevelIncome to set. */
    public void setFleetLevelIncome(Integer fleetLevelIncome) {
        this.fleetLevelIncome = fleetLevelIncome;
    }

    /** @return the fleetMonopIncome. */
    public Integer getFleetMonopIncome() {
        return fleetMonopIncome;
    }

    /** @param fleetMonopIncome the fleetMonopIncome to set. */
    public void setFleetMonopIncome(Integer fleetMonopIncome) {
        this.fleetMonopIncome = fleetMonopIncome;
    }

    /** @return the tradeCenterIncome. */
    public Integer getTradeCenterIncome() {
        return tradeCenterIncome;
    }

    /** @param tradeCenterIncome the tradeCenterIncome to set. */
    public void setTradeCenterIncome(Integer tradeCenterIncome) {
        this.tradeCenterIncome = tradeCenterIncome;
    }

    /** @return the tradeCenterLoss. */
    public Integer getTradeCenterLoss() {
        return tradeCenterLoss;
    }

    /** @param tradeCenterLoss the tradeCenterLoss to set. */
    public void setTradeCenterLoss(Integer tradeCenterLoss) {
        this.tradeCenterLoss = tradeCenterLoss;
    }

    /** @return the tradeIncome. */
    public Integer getTradeIncome() {
        return tradeIncome;
    }

    /** @param tradeIncome the tradeIncome to set. */
    public void setTradeIncome(Integer tradeIncome) {
        this.tradeIncome = tradeIncome;
    }

    /** @return the colIncome. */
    public Integer getColIncome() {
        return colIncome;
    }

    /** @param colIncome the colIncome to set. */
    public void setColIncome(Integer colIncome) {
        this.colIncome = colIncome;
    }

    /** @return the tpIncome. */
    public Integer getTpIncome() {
        return tpIncome;
    }

    /** @param tpIncome the tpIncome to set. */
    public void setTpIncome(Integer tpIncome) {
        this.tpIncome = tpIncome;
    }

    /** @return the exoResIncome. */
    public Integer getExoResIncome() {
        return exoResIncome;
    }

    /** @param exoResIncome the exoResIncome to set. */
    public void setExoResIncome(Integer exoResIncome) {
        this.exoResIncome = exoResIncome;
    }

    /** @return the rotwIncome. */
    public Integer getRotwIncome() {
        return rotwIncome;
    }

    /** @param rotwIncome the rotwIncome to set. */
    public void setRotwIncome(Integer rotwIncome) {
        this.rotwIncome = rotwIncome;
    }

    /** @return the specialIncome. */
    public Integer getSpecialIncome() {
        return specialIncome;
    }

    /** @param specialIncome the specialIncome to set. */
    public void setSpecialIncome(Integer specialIncome) {
        this.specialIncome = specialIncome;
    }

    /** @return the income. */
    public Integer getIncome() {
        return income;
    }

    /** @param income the income to set. */
    public void setIncome(Integer income) {
        this.income = income;
    }

    /** @return the eventIncome. */
    public Integer getEventIncome() {
        return eventIncome;
    }

    /** @param eventIncome the eventIncome to set. */
    public void setEventIncome(Integer eventIncome) {
        this.eventIncome = eventIncome;
    }

    /** @return the grossIncome. */
    public Integer getGrossIncome() {
        return grossIncome;
    }

    /** @param grossIncome the grossIncome to set. */
    public void setGrossIncome(Integer grossIncome) {
        this.grossIncome = grossIncome;
    }

    /** @return the interestExpense. */
    public Integer getInterestExpense() {
        return interestExpense;
    }

    /** @param interestExpense the interestExpense to set. */
    public void setInterestExpense(Integer interestExpense) {
        this.interestExpense = interestExpense;
    }

    /** @return the mandRefundExpense. */
    public Integer getMandRefundExpense() {
        return mandRefundExpense;
    }

    /** @param mandRefundExpense the mandRefundExpense to set. */
    public void setMandRefundExpense(Integer mandRefundExpense) {
        this.mandRefundExpense = mandRefundExpense;
    }

    /** @return the rtCollapse. */
    public Integer getRtCollapse() {
        return rtCollapse;
    }

    /** @param rtCollapse the rtCollapse to set. */
    public void setRtCollapse(Integer rtCollapse) {
        this.rtCollapse = rtCollapse;
    }

    /** @return the optRefundExpense. */
    public Integer getOptRefundExpense() {
        return optRefundExpense;
    }

    /** @param optRefundExpense the optRefundExpense to set. */
    public void setOptRefundExpense(Integer optRefundExpense) {
        this.optRefundExpense = optRefundExpense;
    }

    /** @return the unitMaintExpense. */
    public Integer getUnitMaintExpense() {
        return unitMaintExpense;
    }

    /** @param unitMaintExpense the unitMaintExpense to set. */
    public void setUnitMaintExpense(Integer unitMaintExpense) {
        this.unitMaintExpense = unitMaintExpense;
    }

    /** @return the fortMaintExpense. */
    public Integer getFortMaintExpense() {
        return fortMaintExpense;
    }

    /** @param fortMaintExpense the fortMaintExpense to set. */
    public void setFortMaintExpense(Integer fortMaintExpense) {
        this.fortMaintExpense = fortMaintExpense;
    }

    /** @return the missMaintExpense. */
    public Integer getMissMaintExpense() {
        return missMaintExpense;
    }

    /** @param missMaintExpense the missMaintExpense to set. */
    public void setMissMaintExpense(Integer missMaintExpense) {
        this.missMaintExpense = missMaintExpense;
    }

    /** @return the unitPurchExpense. */
    public Integer getUnitPurchExpense() {
        return unitPurchExpense;
    }

    /** @param unitPurchExpense the unitPurchExpense to set. */
    public void setUnitPurchExpense(Integer unitPurchExpense) {
        this.unitPurchExpense = unitPurchExpense;
    }

    /** @return the fortPurchExpense. */
    public Integer getFortPurchExpense() {
        return fortPurchExpense;
    }

    /** @param fortPurchExpense the fortPurchExpense to set. */
    public void setFortPurchExpense(Integer fortPurchExpense) {
        this.fortPurchExpense = fortPurchExpense;
    }

    /** @return the adminActExpense. */
    public Integer getAdminActExpense() {
        return adminActExpense;
    }

    /** @param adminActExpense the adminActExpense to set. */
    public void setAdminActExpense(Integer adminActExpense) {
        this.adminActExpense = adminActExpense;
    }

    /** @return the adminReactExpense. */
    public Integer getAdminReactExpense() {
        return adminReactExpense;
    }

    /** @param adminReactExpense the adminReactExpense to set. */
    public void setAdminReactExpense(Integer adminReactExpense) {
        this.adminReactExpense = adminReactExpense;
    }

    /** @return the otherExpense. */
    public Integer getOtherExpense() {
        return otherExpense;
    }

    /** @param otherExpense the otherExpense to set. */
    public void setOtherExpense(Integer otherExpense) {
        this.otherExpense = otherExpense;
    }

    /** @return the admTotalExpense. */
    public Integer getAdmTotalExpense() {
        return admTotalExpense;
    }

    /** @param admTotalExpense the admTotalExpense to set. */
    public void setAdmTotalExpense(Integer admTotalExpense) {
        this.admTotalExpense = admTotalExpense;
    }

    /** @return the excTaxesMod. */
    public Integer getExcTaxesMod() {
        return excTaxesMod;
    }

    /** @param excTaxesMod the excTaxesMod to set. */
    public void setExcTaxesMod(Integer excTaxesMod) {
        this.excTaxesMod = excTaxesMod;
    }

    /** @return the passCampExpense. */
    public Integer getPassCampExpense() {
        return passCampExpense;
    }

    /** @param passCampExpense the passCampExpense to set. */
    public void setPassCampExpense(Integer passCampExpense) {
        this.passCampExpense = passCampExpense;
    }

    /** @return the actCampExpense. */
    public Integer getActCampExpense() {
        return actCampExpense;
    }

    /** @param actCampExpense the actCampExpense to set. */
    public void setActCampExpense(Integer actCampExpense) {
        this.actCampExpense = actCampExpense;
    }

    /** @return the majCampExpense. */
    public Integer getMajCampExpense() {
        return majCampExpense;
    }

    /** @param majCampExpense the majCampExpense to set. */
    public void setMajCampExpense(Integer majCampExpense) {
        this.majCampExpense = majCampExpense;
    }

    /** @return the multCampExpense. */
    public Integer getMultCampExpense() {
        return multCampExpense;
    }

    /** @param multCampExpense the multCampExpense to set. */
    public void setMultCampExpense(Integer multCampExpense) {
        this.multCampExpense = multCampExpense;
    }

    /** @return the excRecruitExpense. */
    public Integer getExcRecruitExpense() {
        return excRecruitExpense;
    }

    /** @param excRecruitExpense the excRecruitExpense to set. */
    public void setExcRecruitExpense(Integer excRecruitExpense) {
        this.excRecruitExpense = excRecruitExpense;
    }

    /** @return the navalRefitExpense. */
    public Integer getNavalRefitExpense() {
        return navalRefitExpense;
    }

    /** @param navalRefitExpense the navalRefitExpense to set. */
    public void setNavalRefitExpense(Integer navalRefitExpense) {
        this.navalRefitExpense = navalRefitExpense;
    }

    /** @return the praesidioExpense. */
    public Integer getPraesidioExpense() {
        return praesidioExpense;
    }

    /** @param praesidioExpense the praesidioExpense to set. */
    public void setPraesidioExpense(Integer praesidioExpense) {
        this.praesidioExpense = praesidioExpense;
    }

    /** @return the militaryExpense. */
    public Integer getMilitaryExpense() {
        return militaryExpense;
    }

    /** @param militaryExpense the militaryExpense to set. */
    public void setMilitaryExpense(Integer militaryExpense) {
        this.militaryExpense = militaryExpense;
    }

    /** @return the expenses. */
    public Integer getExpenses() {
        return expenses;
    }

    /** @param expenses the expenses to set. */
    public void setExpenses(Integer expenses) {
        this.expenses = expenses;
    }
}
