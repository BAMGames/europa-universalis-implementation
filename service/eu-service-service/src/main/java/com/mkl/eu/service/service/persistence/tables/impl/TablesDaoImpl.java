package com.mkl.eu.service.service.persistence.tables.impl;

import com.mkl.eu.service.service.persistence.impl.GenericDaoImpl;
import com.mkl.eu.service.service.persistence.oe.tables.TradeIncomeEntity;
import com.mkl.eu.service.service.persistence.tables.ITablesDao;
import org.springframework.stereotype.Repository;

/**
 * Implementation of the Tables DAO.
 *
 * @author MKL.
 */
@Repository
public class TablesDaoImpl extends GenericDaoImpl<TradeIncomeEntity, Long> implements ITablesDao {
    /**
     * Constructor.
     */
    public TablesDaoImpl() {
        super(TradeIncomeEntity.class);
    }
}
