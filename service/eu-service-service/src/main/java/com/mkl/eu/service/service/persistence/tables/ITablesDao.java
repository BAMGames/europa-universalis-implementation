package com.mkl.eu.service.service.persistence.tables;

import com.mkl.eu.service.service.persistence.IGenericDao;
import com.mkl.eu.service.service.persistence.oe.tables.TradeIncomeEntity;

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
    Integer getTradeIncome(Integer inputValue, Integer countryValue, boolean foreignTrade);
}
