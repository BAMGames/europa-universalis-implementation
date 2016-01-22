package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.service.service.ITablesService;
import com.mkl.eu.client.service.vo.tables.Tables;
import com.mkl.eu.service.service.mapping.tables.TablesMapping;
import com.mkl.eu.service.service.persistence.oe.tables.BasicForceTableEntity;
import com.mkl.eu.service.service.persistence.oe.tables.TradeIncomeEntity;
import com.mkl.eu.service.service.persistence.oe.tables.UnitEntity;
import com.mkl.eu.service.service.persistence.tables.ITablesDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Map<Class<?>, Map<Long, Object>> objectsCreated = new HashMap<>();

        List<TradeIncomeEntity> tradeTables = tablesDao.readAll();
        tablesMapping.fillTradeIncomeTables(tradeTables, tables);
        List<BasicForceTableEntity> basicForces = tablesDao.getBasicForces();
        tablesMapping.fillBasicForcesTables(basicForces, objectsCreated, tables);
        List<UnitEntity> units = tablesDao.getUnits();
        tablesMapping.fillUnitsTables(units, objectsCreated, tables);

        return tables;
    }
}
