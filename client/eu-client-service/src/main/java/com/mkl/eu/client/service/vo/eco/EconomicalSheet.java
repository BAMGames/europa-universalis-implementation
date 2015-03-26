package com.mkl.eu.client.service.vo.eco;

import com.mkl.eu.client.service.vo.Country;
import org.apache.commons.lang3.math.Fraction;

/**
 * Economical sheet of a given player at a given turn.
 *
 * @author MKL
 */
public class EconomicalSheet {
    /** Owner of the sheet. */
    private Country owner;
    /** Turn of the sheet. */
    private Integer turn;



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
    /** Partial/Total monopolies. TODO still used ? Line 13 of sheet B. */
    private Fraction monopoly;
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
    /** Partial/Total monopolies. TODO still used ? Line 20 of sheet B. */
    private Fraction rotwMonopoly;
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

    /** @return the owner. */
    public Country getOwner() {
        return owner;
    }

    /** @param owner the owner to set. */
    public void setOwner(Country owner) {
        this.owner = owner;
    }

    /** @return the turn. */
    public Integer getTurn() {
        return turn;
    }

    /** @param turn the turn to set. */
    public void setTurn(Integer turn) {
        this.turn = turn;
    }
}
