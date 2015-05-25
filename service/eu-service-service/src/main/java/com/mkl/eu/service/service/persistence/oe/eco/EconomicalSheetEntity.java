package com.mkl.eu.service.service.persistence.oe.eco;

import com.mkl.eu.service.service.persistence.oe.IEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Economical sheet of a given player at a given turn.
 *
 * @author MKL
 */
@Entity
@Table(name = "ECONOMICAL_SHEET")
public class EconomicalSheetEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Owner of the sheet. */
    private PlayableCountryEntity country;
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
    private Integer rtDiplo;
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
    private Integer mnuIncome;
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

    /** @return the id. */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    /** @param id the id to set. */

    public void setId(Long id) {
        this.id = id;
    }

    /** @return the country. */
    @ManyToOne
    @JoinColumn(name = "ID_COUNTRY")
    public PlayableCountryEntity getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(PlayableCountryEntity country) {
        this.country = country;
    }

    /** @return the turn. */
    @Column(name = "TURN")
    public Integer getTurn() {
        return turn;
    }

    /** @param turn the turn to set. */
    public void setTurn(Integer turn) {
        this.turn = turn;
    }

    /** @return the rtStart. */
    @Column(name = "RT_START")
    public Integer getRtStart() {
        return rtStart;
    }

    /** @param rtStart the rtStart to set. */
    public void setRtStart(Integer rtStart) {
        this.rtStart = rtStart;
    }

    /** @return the rtEvents. */
    @Column(name = "RT_EVENTS")
    public Integer getRtEvents() {
        return rtEvents;
    }

    /** @param rtEvents the rtEvents to set. */
    public void setRtEvents(Integer rtEvents) {
        this.rtEvents = rtEvents;
    }

    /** @return the loans. */
    @Column(name = "LOANS")
    public Integer getLoans() {
        return loans;
    }

    /** @param loans the loans to set. */
    public void setLoans(Integer loans) {
        this.loans = loans;
    }

    /** @return the woodSlaves. */
    @Column(name = "WOOD_SLAVES")
    public Integer getWoodSlaves() {
        return woodSlaves;
    }

    /** @param woodSlaves the woodSlaves to set. */
    public void setWoodSlaves(Integer woodSlaves) {
        this.woodSlaves = woodSlaves;
    }

    /** @return the diploActions. */
    @Column(name = "DIPLO_ACTIONS")
    public Integer getDiploActions() {
        return diploActions;
    }

    /** @param diploActions the diploActions to set. */
    public void setDiploActions(Integer diploActions) {
        this.diploActions = diploActions;
    }

    /** @return the diploReactions. */
    @Column(name = "DIPLO_REACTIONS")
    public Integer getDiploReactions() {
        return diploReactions;
    }

    /** @param diploReactions the diploReactions to set. */
    public void setDiploReactions(Integer diploReactions) {
        this.diploReactions = diploReactions;
    }

    /** @return the subsidies. */
    @Column(name = "SUBSIDIES")
    public Integer getSubsidies() {
        return subsidies;
    }

    /** @param subsidies the subsidies to set. */
    public void setSubsidies(Integer subsidies) {
        this.subsidies = subsidies;
    }

    /** @return the rtDiplo. */
    @Column(name = "RT_DIPLO")
    public Integer getRtDiplo() {
        return rtDiplo;
    }

    /** @param rtDiplo the rtDiplo to set. */
    public void setRtDiplo(Integer rtDiplo) {
        this.rtDiplo = rtDiplo;
    }

    /** @return the pillages. */
    @Column(name = "PILLAGES")
    public Integer getPillages() {
        return pillages;
    }

    /** @param pillages the pillages to set. */
    public void setPillages(Integer pillages) {
        this.pillages = pillages;
    }

    /** @return the goldRotw. */
    @Column(name = "GOLD_ROTW")
    public Integer getGoldRotw() {
        return goldRotw;
    }

    /** @param goldRotw the goldRotw to set. */
    public void setGoldRotw(Integer goldRotw) {
        this.goldRotw = goldRotw;
    }

    /** @return the excTaxes. */
    @Column(name = "EXC_TAXES")
    public Integer getExcTaxes() {
        return excTaxes;
    }

    /** @param excTaxes the excTaxes to set. */
    public void setExcTaxes(Integer excTaxes) {
        this.excTaxes = excTaxes;
    }

    /** @return the rtBefExch. */
    @Column(name = "RT_BEF_EXCH")
    public Integer getRtBefExch() {
        return rtBefExch;
    }

    /** @param rtBefExch the rtBefExch to set. */
    public void setRtBefExch(Integer rtBefExch) {
        this.rtBefExch = rtBefExch;
    }

    /** @return the regularIncome. */
    @Column(name = "REGULAR_INCOME")
    public Integer getRegularIncome() {
        return regularIncome;
    }

    /** @param regularIncome the regularIncome to set. */
    public void setRegularIncome(Integer regularIncome) {
        this.regularIncome = regularIncome;
    }

    /** @return the prestigeIncome. */
    @Column(name = "PRESTIGE_INCOME")
    public Integer getPrestigeIncome() {
        return prestigeIncome;
    }

    /** @param prestigeIncome the prestigeIncome to set. */
    public void setPrestigeIncome(Integer prestigeIncome) {
        this.prestigeIncome = prestigeIncome;
    }

    /** @return the maxNatLoan. */
    @Column(name = "MAX_NAT_LOAN")
    public Integer getMaxNatLoan() {
        return maxNatLoan;
    }

    /** @param maxNatLoan the maxNatLoan to set. */
    public void setMaxNatLoan(Integer maxNatLoan) {
        this.maxNatLoan = maxNatLoan;
    }

    /** @return the maxInterLoan. */
    @Column(name = "MAX_INTER_LOAN")
    public Integer getMaxInterLoan() {
        return maxInterLoan;
    }

    /** @param maxInterLoan the maxInterLoan to set. */
    public void setMaxInterLoan(Integer maxInterLoan) {
        this.maxInterLoan = maxInterLoan;
    }

    /** @return the remainingExpenses. */
    @Column(name = "REMAINING_EXPENSES")
    public Integer getRemainingExpenses() {
        return remainingExpenses;
    }

    /** @param remainingExpenses the remainingExpenses to set. */
    public void setRemainingExpenses(Integer remainingExpenses) {
        this.remainingExpenses = remainingExpenses;
    }

    /** @return the prestigeSpent. */
    @Column(name = "PRESTIGE_SPENT")
    public Integer getPrestigeSpent() {
        return prestigeSpent;
    }

    /** @param prestigeSpent the prestigeSpent to set. */
    public void setPrestigeSpent(Integer prestigeSpent) {
        this.prestigeSpent = prestigeSpent;
    }

    /** @return the natLoan. */
    @Column(name = "NAT_LOAN")
    public Integer getNatLoan() {
        return natLoan;
    }

    /** @param natLoan the natLoan to set. */
    public void setNatLoan(Integer natLoan) {
        this.natLoan = natLoan;
    }

    /** @return the interLoan. */
    @Column(name = "INTER_LOAN")
    public Integer getInterLoan() {
        return interLoan;
    }

    /** @param interLoan the interLoan to set. */
    public void setInterLoan(Integer interLoan) {
        this.interLoan = interLoan;
    }

    /** @return the rtBalance. */
    @Column(name = "RT_BALANCE")
    public Integer getRtBalance() {
        return rtBalance;
    }

    /** @param rtBalance the rtBalance to set. */
    public void setRtBalance(Integer rtBalance) {
        this.rtBalance = rtBalance;
    }

    /** @return the rtAftExch. */
    @Column(name = "RT_AFT_EXCH")
    public Integer getRtAftExch() {
        return rtAftExch;
    }

    /** @param rtAftExch the rtAftExch to set. */
    public void setRtAftExch(Integer rtAftExch) {
        this.rtAftExch = rtAftExch;
    }

    /** @return the prestigeVP. */
    @Column(name = "PRESTIGE_VP")
    public Integer getPrestigeVP() {
        return prestigeVP;
    }

    /** @param prestigeVP the prestigeVP to set. */
    public void setPrestigeVP(Integer prestigeVP) {
        this.prestigeVP = prestigeVP;
    }

    /** @return the wealth. */
    @Column(name = "WEALTH")
    public Integer getWealth() {
        return wealth;
    }

    /** @param wealth the wealth to set. */
    public void setWealth(Integer wealth) {
        this.wealth = wealth;
    }

    /** @return the periodWealth. */
    @Column(name = "PERIOD_WEALTH")
    public Integer getPeriodWealth() {
        return periodWealth;
    }

    /** @param periodWealth the periodWealth to set. */
    public void setPeriodWealth(Integer periodWealth) {
        this.periodWealth = periodWealth;
    }

    /** @return the stab. */
    @Column(name = "STAB")
    public Integer getStab() {
        return stab;
    }

    /** @param stab the stab to set. */
    public void setStab(Integer stab) {
        this.stab = stab;
    }

    /** @return the peace. */
    @Column(name = "PEACE")
    public Integer getPeace() {
        return peace;
    }

    /** @param peace the peace to set. */
    public void setPeace(Integer peace) {
        this.peace = peace;
    }

    /** @return the rtPeace. */
    @Column(name = "RT_PEACE")
    public Integer getRtPeace() {
        return rtPeace;
    }

    /** @param rtPeace the rtPeace to set. */
    public void setRtPeace(Integer rtPeace) {
        this.rtPeace = rtPeace;
    }

    /** @return the inflation. */
    @Column(name = "INFLATION")
    public Integer getInflation() {
        return inflation;
    }

    /** @param inflation the inflation to set. */
    public void setInflation(Integer inflation) {
        this.inflation = inflation;
    }

    /** @return the rtEnd. */
    @Column(name = "RT_ENT")
    public Integer getRtEnd() {
        return rtEnd;
    }

    /** @param rtEnd the rtEnd to set. */
    public void setRtEnd(Integer rtEnd) {
        this.rtEnd = rtEnd;
    }

    /** @return the interLoanNew. */
    @Column(name = "INTER_LOAN_NEW")
    public Integer getInterLoanNew() {
        return interLoanNew;
    }

    /** @param interLoanNew the interLoanNew to set. */
    public void setInterLoanNew(Integer interLoanNew) {
        this.interLoanNew = interLoanNew;
    }

    /** @return the interLoanInterests. */
    @Column(name = "INTER_LOAN_INTERESTS")
    public Integer getInterLoanInterests() {
        return interLoanInterests;
    }

    /** @param interLoanInterests the interLoanInterests to set. */
    public void setInterLoanInterests(Integer interLoanInterests) {
        this.interLoanInterests = interLoanInterests;
    }

    /** @return the interLoanRefund. */
    @Column(name = "INTER_LOAN_REFUND")
    public Integer getInterLoanRefund() {
        return interLoanRefund;
    }

    /** @param interLoanRefund the interLoanRefund to set. */
    public void setInterLoanRefund(Integer interLoanRefund) {
        this.interLoanRefund = interLoanRefund;
    }

    /** @return the interBankrupt. */
    @Column(name = "INTER_BANKRUPT")
    public Integer getInterBankrupt() {
        return interBankrupt;
    }

    /** @param interBankrupt the interBankrupt to set. */
    public void setInterBankrupt(Integer interBankrupt) {
        this.interBankrupt = interBankrupt;
    }

    /** @return the natLoanStart. */
    @Column(name = "NAT_LOAN_START")
    public Integer getNatLoanStart() {
        return natLoanStart;
    }

    /** @param natLoanStart the natLoanStart to set. */
    public void setNatLoanStart(Integer natLoanStart) {
        this.natLoanStart = natLoanStart;
    }

    /** @return the natLoanInterest. */
    @Column(name = "NAT_LOAN_INTERESTS")
    public Integer getNatLoanInterest() {
        return natLoanInterest;
    }

    /** @param natLoanInterest the natLoanInterest to set. */
    public void setNatLoanInterest(Integer natLoanInterest) {
        this.natLoanInterest = natLoanInterest;
    }

    /** @return the natLoanBankrupt. */
    @Column(name = "NAT_LOAN_BANKRUPT")
    public Integer getNatLoanBankrupt() {
        return natLoanBankrupt;
    }

    /** @param natLoanBankrupt the natLoanBankrupt to set. */
    public void setNatLoanBankrupt(Integer natLoanBankrupt) {
        this.natLoanBankrupt = natLoanBankrupt;
    }

    /** @return the natLoanRefund. */
    @Column(name = "NAT_LOAN_REFUND")
    public Integer getNatLoanRefund() {
        return natLoanRefund;
    }

    /** @param natLoanRefund the natLoanRefund to set. */
    public void setNatLoanRefund(Integer natLoanRefund) {
        this.natLoanRefund = natLoanRefund;
    }

    /** @return the natLoanNew. */
    @Column(name = "NAT_LOAN_NEW")
    public Integer getNatLoanNew() {
        return natLoanNew;
    }

    /** @param natLoanNew the natLoanNew to set. */
    public void setNatLoanNew(Integer natLoanNew) {
        this.natLoanNew = natLoanNew;
    }

    /** @return the natLoanEnd. */
    @Column(name = "NAT_LOAN_END")
    public Integer getNatLoanEnd() {
        return natLoanEnd;
    }

    /** @param natLoanEnd the natLoanEnd to set. */
    public void setNatLoanEnd(Integer natLoanEnd) {
        this.natLoanEnd = natLoanEnd;
    }

    /** @return the provincesIncome. */
    @Column(name = "PROVINCES_INCOME")
    public Integer getProvincesIncome() {
        return provincesIncome;
    }

    /** @param provincesIncome the provincesIncome to set. */
    public void setProvincesIncome(Integer provincesIncome) {
        this.provincesIncome = provincesIncome;
    }

    /** @return the vassalIncome. */
    @Column(name = "VASSAL_INCOME")
    public Integer getVassalIncome() {
        return vassalIncome;
    }

    /** @param vassalIncome the vassalIncome to set. */
    public void setVassalIncome(Integer vassalIncome) {
        this.vassalIncome = vassalIncome;
    }

    /** @return the lostIncome. */
    @Column(name = "LOST_INCOME")
    public Integer getLostIncome() {
        return lostIncome;
    }

    /** @param lostIncome the lostIncome to set. */
    public void setLostIncome(Integer lostIncome) {
        this.lostIncome = lostIncome;
    }

    /** @return the eventLandIncome. */
    @Column(name = "EVENT_LAND_INCOME")
    public Integer getEventLandIncome() {
        return eventLandIncome;
    }

    /** @param eventLandIncome the eventLandIncome to set. */
    public void setEventLandIncome(Integer eventLandIncome) {
        this.eventLandIncome = eventLandIncome;
    }

    /** @return the landIncome. */
    @Column(name = "LAND_INCOME")
    public Integer getLandIncome() {
        return landIncome;
    }

    /** @param landIncome the landIncome to set. */
    public void setLandIncome(Integer landIncome) {
        this.landIncome = landIncome;
    }

    /** @return the mnuIncome. */
    @Column(name = "MNU_INCOME")
    public Integer getMnuIncome() {
        return mnuIncome;
    }

    /** @param mnuIncome the mnuIncome to set. */
    public void setMnuIncome(Integer mnuIncome) {
        this.mnuIncome = mnuIncome;
    }

    /** @return the goldIncome. */
    @Column(name = "GOLD_INCOME")
    public Integer getGoldIncome() {
        return goldIncome;
    }

    /** @param goldIncome the goldIncome to set. */
    public void setGoldIncome(Integer goldIncome) {
        this.goldIncome = goldIncome;
    }

    /** @return the industrialIncome. */
    @Column(name = "INDUSTRIAL_INCOME")
    public Integer getIndustrialIncome() {
        return industrialIncome;
    }

    /** @param industrialIncome the industrialIncome to set. */
    public void setIndustrialIncome(Integer industrialIncome) {
        this.industrialIncome = industrialIncome;
    }

    /** @return the domTradeIncome. */
    @Column(name = "DOM_TRADE_INCOME")
    public Integer getDomTradeIncome() {
        return domTradeIncome;
    }

    /** @param domTradeIncome the domTradeIncome to set. */
    public void setDomTradeIncome(Integer domTradeIncome) {
        this.domTradeIncome = domTradeIncome;
    }

    /** @return the forTradeIncome. */
    @Column(name = "FOR_TRADE_INCOME")
    public Integer getForTradeIncome() {
        return forTradeIncome;
    }

    /** @param forTradeIncome the forTradeIncome to set. */
    public void setForTradeIncome(Integer forTradeIncome) {
        this.forTradeIncome = forTradeIncome;
    }

    /** @return the fleetLevelIncome. */
    @Column(name = "FLEET_LEVEL_INCOME")
    public Integer getFleetLevelIncome() {
        return fleetLevelIncome;
    }

    /** @param fleetLevelIncome the fleetLevelIncome to set. */
    public void setFleetLevelIncome(Integer fleetLevelIncome) {
        this.fleetLevelIncome = fleetLevelIncome;
    }

    /** @return the fleetMonopIncome. */
    @Column(name = "FLEET_MONOP_INCOME")
    public Integer getFleetMonopIncome() {
        return fleetMonopIncome;
    }

    /** @param fleetMonopIncome the fleetMonopIncome to set. */
    public void setFleetMonopIncome(Integer fleetMonopIncome) {
        this.fleetMonopIncome = fleetMonopIncome;
    }

    /** @return the tradeCenterIncome. */
    @Column(name = "TRADE_CENTER_INCOME")
    public Integer getTradeCenterIncome() {
        return tradeCenterIncome;
    }

    /** @param tradeCenterIncome the tradeCenterIncome to set. */
    public void setTradeCenterIncome(Integer tradeCenterIncome) {
        this.tradeCenterIncome = tradeCenterIncome;
    }

    /** @return the tradeCenterLoss. */
    @Column(name = "TRADE_CENTER_LOSS")
    public Integer getTradeCenterLoss() {
        return tradeCenterLoss;
    }

    /** @param tradeCenterLoss the tradeCenterLoss to set. */
    public void setTradeCenterLoss(Integer tradeCenterLoss) {
        this.tradeCenterLoss = tradeCenterLoss;
    }

    /** @return the tradeIncome. */
    @Column(name = "TRADE_INCOME")
    public Integer getTradeIncome() {
        return tradeIncome;
    }

    /** @param tradeIncome the tradeIncome to set. */
    public void setTradeIncome(Integer tradeIncome) {
        this.tradeIncome = tradeIncome;
    }

    /** @return the colIncome. */
    @Column(name = "COL_INCOME")
    public Integer getColIncome() {
        return colIncome;
    }

    /** @param colIncome the colIncome to set. */
    public void setColIncome(Integer colIncome) {
        this.colIncome = colIncome;
    }

    /** @return the tpIncome. */
    @Column(name = "TP_INCOME")
    public Integer getTpIncome() {
        return tpIncome;
    }

    /** @param tpIncome the tpIncome to set. */
    public void setTpIncome(Integer tpIncome) {
        this.tpIncome = tpIncome;
    }

    /** @return the exoResIncome. */
    @Column(name = "EXO_RES_INCOME")
    public Integer getExoResIncome() {
        return exoResIncome;
    }

    /** @param exoResIncome the exoResIncome to set. */
    public void setExoResIncome(Integer exoResIncome) {
        this.exoResIncome = exoResIncome;
    }

    /** @return the rotwIncome. */
    @Column(name = "ROTW_INCOME")
    public Integer getRotwIncome() {
        return rotwIncome;
    }

    /** @param rotwIncome the rotwIncome to set. */
    public void setRotwIncome(Integer rotwIncome) {
        this.rotwIncome = rotwIncome;
    }

    /** @return the specialIncome. */
    @Column(name = "SPECIAL_INCOME")
    public Integer getSpecialIncome() {
        return specialIncome;
    }

    /** @param specialIncome the specialIncome to set. */
    public void setSpecialIncome(Integer specialIncome) {
        this.specialIncome = specialIncome;
    }

    /** @return the income. */
    @Column(name = "INCOME")
    public Integer getIncome() {
        return income;
    }

    /** @param income the income to set. */
    public void setIncome(Integer income) {
        this.income = income;
    }

    /** @return the eventIncome. */
    @Column(name = "EVENT_INCOME")
    public Integer getEventIncome() {
        return eventIncome;
    }

    /** @param eventIncome the eventIncome to set. */
    public void setEventIncome(Integer eventIncome) {
        this.eventIncome = eventIncome;
    }

    /** @return the grossIncome. */
    @Column(name = "GROSS_INCOME")
    public Integer getGrossIncome() {
        return grossIncome;
    }

    /** @param grossIncome the grossIncome to set. */
    public void setGrossIncome(Integer grossIncome) {
        this.grossIncome = grossIncome;
    }

    /** @return the interestExpense. */
    @Column(name = "INTEREST_EXPENSE")
    public Integer getInterestExpense() {
        return interestExpense;
    }

    /** @param interestExpense the interestExpense to set. */
    public void setInterestExpense(Integer interestExpense) {
        this.interestExpense = interestExpense;
    }

    /** @return the mandRefundExpense. */
    @Column(name = "MAND_REFUND_EXPENSE")
    public Integer getMandRefundExpense() {
        return mandRefundExpense;
    }

    /** @param mandRefundExpense the mandRefundExpense to set. */
    public void setMandRefundExpense(Integer mandRefundExpense) {
        this.mandRefundExpense = mandRefundExpense;
    }

    /** @return the rtCollapse. */
    @Column(name = "RT_COLLAPSE")
    public Integer getRtCollapse() {
        return rtCollapse;
    }

    /** @param rtCollapse the rtCollapse to set. */
    public void setRtCollapse(Integer rtCollapse) {
        this.rtCollapse = rtCollapse;
    }

    /** @return the optRefundExpense. */
    @Column(name = "OPT_REFUND_EXPENSE")
    public Integer getOptRefundExpense() {
        return optRefundExpense;
    }

    /** @param optRefundExpense the optRefundExpense to set. */
    public void setOptRefundExpense(Integer optRefundExpense) {
        this.optRefundExpense = optRefundExpense;
    }

    /** @return the unitMaintExpense. */
    @Column(name = "UNIT_MAINT_EXPENSE")
    public Integer getUnitMaintExpense() {
        return unitMaintExpense;
    }

    /** @param unitMaintExpense the unitMaintExpense to set. */
    public void setUnitMaintExpense(Integer unitMaintExpense) {
        this.unitMaintExpense = unitMaintExpense;
    }

    /** @return the fortMaintExpense. */
    @Column(name = "FORT_MAINT_EXPENSE")
    public Integer getFortMaintExpense() {
        return fortMaintExpense;
    }

    /** @param fortMaintExpense the fortMaintExpense to set. */
    public void setFortMaintExpense(Integer fortMaintExpense) {
        this.fortMaintExpense = fortMaintExpense;
    }

    /** @return the missMaintExpense. */
    @Column(name = "MISS_MAINT_EXPENSE")
    public Integer getMissMaintExpense() {
        return missMaintExpense;
    }

    /** @param missMaintExpense the missMaintExpense to set. */
    public void setMissMaintExpense(Integer missMaintExpense) {
        this.missMaintExpense = missMaintExpense;
    }

    /** @return the unitPurchExpense. */
    @Column(name = "UNIT_PURCH_EXPENSE")
    public Integer getUnitPurchExpense() {
        return unitPurchExpense;
    }

    /** @param unitPurchExpense the unitPurchExpense to set. */
    public void setUnitPurchExpense(Integer unitPurchExpense) {
        this.unitPurchExpense = unitPurchExpense;
    }

    /** @return the fortPurchExpense. */
    @Column(name = "FORT_PURCH_EXPENSE")
    public Integer getFortPurchExpense() {
        return fortPurchExpense;
    }

    /** @param fortPurchExpense the fortPurchExpense to set. */
    public void setFortPurchExpense(Integer fortPurchExpense) {
        this.fortPurchExpense = fortPurchExpense;
    }

    /** @return the adminActExpense. */
    @Column(name = "ADMIN_ACT_EXPENSE")
    public Integer getAdminActExpense() {
        return adminActExpense;
    }

    /** @param adminActExpense the adminActExpense to set. */
    public void setAdminActExpense(Integer adminActExpense) {
        this.adminActExpense = adminActExpense;
    }

    /** @return the adminReactExpense. */
    @Column(name = "ADMIN_REACT_EXPENSE")
    public Integer getAdminReactExpense() {
        return adminReactExpense;
    }

    /** @param adminReactExpense the adminReactExpense to set. */
    public void setAdminReactExpense(Integer adminReactExpense) {
        this.adminReactExpense = adminReactExpense;
    }

    /** @return the otherExpense. */
    @Column(name = "OTHER_EXPENSE")
    public Integer getOtherExpense() {
        return otherExpense;
    }

    /** @param otherExpense the otherExpense to set. */
    public void setOtherExpense(Integer otherExpense) {
        this.otherExpense = otherExpense;
    }

    /** @return the admTotalExpense. */
    @Column(name = "ADM_TOTAL_EXPENSE")
    public Integer getAdmTotalExpense() {
        return admTotalExpense;
    }

    /** @param admTotalExpense the admTotalExpense to set. */
    public void setAdmTotalExpense(Integer admTotalExpense) {
        this.admTotalExpense = admTotalExpense;
    }

    /** @return the excTaxesMod. */
    @Column(name = "EXC_TAXES_MOD")
    public Integer getExcTaxesMod() {
        return excTaxesMod;
    }

    /** @param excTaxesMod the excTaxesMod to set. */
    public void setExcTaxesMod(Integer excTaxesMod) {
        this.excTaxesMod = excTaxesMod;
    }

    /** @return the passCampExpense. */
    @Column(name = "PASS_CAMP_EXPENSE")
    public Integer getPassCampExpense() {
        return passCampExpense;
    }

    /** @param passCampExpense the passCampExpense to set. */
    public void setPassCampExpense(Integer passCampExpense) {
        this.passCampExpense = passCampExpense;
    }

    /** @return the actCampExpense. */
    @Column(name = "ACT_CAMP_EXPENSE")
    public Integer getActCampExpense() {
        return actCampExpense;
    }

    /** @param actCampExpense the actCampExpense to set. */
    public void setActCampExpense(Integer actCampExpense) {
        this.actCampExpense = actCampExpense;
    }

    /** @return the majCampExpense. */
    @Column(name = "MAJ_CAMP_EXPENSE")
    public Integer getMajCampExpense() {
        return majCampExpense;
    }

    /** @param majCampExpense the majCampExpense to set. */
    public void setMajCampExpense(Integer majCampExpense) {
        this.majCampExpense = majCampExpense;
    }

    /** @return the multCampExpense. */
    @Column(name = "MULT_CAMP_EXPENSE")
    public Integer getMultCampExpense() {
        return multCampExpense;
    }

    /** @param multCampExpense the multCampExpense to set. */
    public void setMultCampExpense(Integer multCampExpense) {
        this.multCampExpense = multCampExpense;
    }

    /** @return the excRecruitExpense. */
    @Column(name = "EXC_RECRUIT_EXPENSE")
    public Integer getExcRecruitExpense() {
        return excRecruitExpense;
    }

    /** @param excRecruitExpense the excRecruitExpense to set. */
    public void setExcRecruitExpense(Integer excRecruitExpense) {
        this.excRecruitExpense = excRecruitExpense;
    }

    /** @return the navalRefitExpense. */
    @Column(name = "NAVAL_REFIT_EXPENSE")
    public Integer getNavalRefitExpense() {
        return navalRefitExpense;
    }

    /** @param navalRefitExpense the navalRefitExpense to set. */
    public void setNavalRefitExpense(Integer navalRefitExpense) {
        this.navalRefitExpense = navalRefitExpense;
    }

    /** @return the praesidioExpense. */
    @Column(name = "PRAESIDIO_EXPENSE")
    public Integer getPraesidioExpense() {
        return praesidioExpense;
    }

    /** @param praesidioExpense the praesidioExpense to set. */
    public void setPraesidioExpense(Integer praesidioExpense) {
        this.praesidioExpense = praesidioExpense;
    }

    /** @return the militaryExpense. */
    @Column(name = "MILITARY_EXPENSE")
    public Integer getMilitaryExpense() {
        return militaryExpense;
    }

    /** @param militaryExpense the militaryExpense to set. */
    public void setMilitaryExpense(Integer militaryExpense) {
        this.militaryExpense = militaryExpense;
    }

    /** @return the expenses. */
    @Column(name = "EXPENSES")
    public Integer getExpenses() {
        return expenses;
    }

    /** @param expenses the expenses to set. */
    public void setExpenses(Integer expenses) {
        this.expenses = expenses;
    }
}
