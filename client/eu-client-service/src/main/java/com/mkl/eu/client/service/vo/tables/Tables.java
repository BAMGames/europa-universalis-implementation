package com.mkl.eu.client.service.vo.tables;

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing all tables described in the appendix.
 *
 * @author MKL.
 */
public class Tables {
    /** Foreign trade income tables. */
    private List<TradeIncome> foreignTrades = new ArrayList<>();
    /** Domestic trade incomes. */
    private List<TradeIncome> domesticTrades = new ArrayList<>();
    /** Periods of the game. */
//    private List<Period>
    /** Basic forces of major powers. */
    private List<BasicForce> basicForces = new ArrayList<>();
    /** Unit purchase and maintenance costs of major powers. */
    private List<Unit> units = new ArrayList<>();
    /** Actions limits of major powers. */
    private List<Limit> limits = new ArrayList<>();

    /** @return the foreignTrades. */
    public List<TradeIncome> getForeignTrades() {
        return foreignTrades;
    }

    /** @param foreignTrades the foreignTrades to set. */
    public void setForeignTrades(List<TradeIncome> foreignTrades) {
        this.foreignTrades = foreignTrades;
    }

    /** @return the domesticTrades. */
    public List<TradeIncome> getDomesticTrades() {
        return domesticTrades;
    }

    /** @param domesticTrades the domesticTrades to set. */
    public void setDomesticTrades(List<TradeIncome> domesticTrades) {
        this.domesticTrades = domesticTrades;
    }

    /** @return the basicForces. */
    public List<BasicForce> getBasicForces() {
        return basicForces;
    }

    /** @param basicForces the basicForces to set. */
    public void setBasicForces(List<BasicForce> basicForces) {
        this.basicForces = basicForces;
    }

    /** @return the units. */
    public List<Unit> getUnits() {
        return units;
    }

    /** @param units the units to set. */
    public void setUnits(List<Unit> units) {
        this.units = units;
    }

    /** @return the limits. */
    public List<Limit> getLimits() {
        return limits;
    }

    /** @param limits the limits to set. */
    public void setLimits(List<Limit> limits) {
        this.limits = limits;
    }
}
