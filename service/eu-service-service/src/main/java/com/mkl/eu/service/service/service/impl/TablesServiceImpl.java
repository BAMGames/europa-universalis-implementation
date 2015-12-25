package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.service.service.ITablesService;
import com.mkl.eu.client.service.vo.tables.Tables;
import com.mkl.eu.service.service.mapping.tables.TablesMapping;
import com.mkl.eu.service.service.persistence.oe.tables.TradeIncomeEntity;
import com.mkl.eu.service.service.persistence.tables.ITablesDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for chat purpose.
 *
 * @author MKL.
 */
@Service
@Transactional(rollbackFor = {TechnicalException.class, FunctionalException.class})
public class TablesServiceImpl implements ITablesService {
    /** Tables DAO. */
    @Autowired
    private ITablesDao tablesDao;
    /** Tables Mapping. */
    @Autowired
    private TablesMapping tablesMapping;

    /** {@inheritDoc} */
    @Override
    public Tables getTables() {
        Tables tables = new Tables();

        List<TradeIncomeEntity> tradeTables = tablesDao.readAll();
        tablesMapping.fillTradeIncomeTables(tradeTables, tables);

        return tables;
    }
}
