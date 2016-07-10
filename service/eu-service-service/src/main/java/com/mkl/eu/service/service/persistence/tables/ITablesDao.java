package com.mkl.eu.service.service.persistence.tables;

import com.mkl.eu.service.service.persistence.IGenericDao;
import com.mkl.eu.service.service.persistence.oe.tables.*;

import java.util.List;

/**
 * Interface of the Tables DAO.
 *
 * @author MKL.
 */
public interface ITablesDao extends IGenericDao<TradeIncomeEntity, Long> {
    /**
     * Return the foreign or domestic trade income given the input value and the foreign DTI/FTI.
     *
     * @param inputValue   the blocked trade/land income.
     * @param countryValue the FTI/DTI.
     * @param foreignTrade true/false.
     * @return the foreign or domestic trade income given the input value and the foreign DTI/FTI.
     */
    int getTradeIncome(int inputValue, int countryValue, boolean foreignTrade);

    /**
     * @return the periods tables.
     */
    List<PeriodEntity> getPeriods();

    /**
     * @return the technologies tables.
     */
    List<TechEntity> getTechs();

    /**
     * @return the basic forces tables.
     */
    List<BasicForceTableEntity> getBasicForces();

    /**
     * @return the units tables.
     */
    List<UnitEntity> getUnits();

    /**
     * @return the actions limits tables.
     */
    List<LimitTableEntity> getLimits();

    /**
     * @return the results tables.
     */
    List<ResultEntity> getResults();
}
