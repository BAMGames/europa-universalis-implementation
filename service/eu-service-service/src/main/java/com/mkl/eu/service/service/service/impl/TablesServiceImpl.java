package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.service.service.ITablesService;
import com.mkl.eu.client.service.vo.tables.Tables;
import com.mkl.eu.service.service.mapping.tables.TablesMapping;
import com.mkl.eu.service.service.persistence.oe.tables.*;
import com.mkl.eu.service.service.persistence.tables.ITablesDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
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
public class TablesServiceImpl extends AbstractService implements ITablesService, ApplicationListener<ContextRefreshedEvent> {
    /** Tables DAO. */
    @Autowired
    private ITablesDao tablesDao;
    /** Tables Mapping. */
    @Autowired
    private TablesMapping tablesMapping;

    /** {@inheritDoc} */
    @Override
    public Tables getTables() {
        return super.getTables();
    }

    /** {@inheritDoc} */
    @Override
    public void refresh() {
        cacheTables();
    }

    /** {@inheritDoc} */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        cacheTables();
    }

    /**
     * Cache the tables.
     */
    protected void cacheTables() {
        TABLES = new Tables();

        Map<Class<?>, Map<Long, Object>> objectsCreated = new HashMap<>();

        List<PeriodEntity> periods = tablesDao.getPeriods();
        tablesMapping.fillPeriodsTables(periods, objectsCreated, TABLES);
        List<TradeIncomeEntity> tradeTables = tablesDao.readAll();
        tablesMapping.fillTradeIncomeTables(tradeTables, TABLES);
        List<TechEntity> techs = tablesDao.getTechs();
        tablesMapping.fillTechsTables(techs, objectsCreated, TABLES);
        List<BasicForceTableEntity> basicForces = tablesDao.getBasicForces();
        tablesMapping.fillBasicForcesTables(basicForces, objectsCreated, TABLES);
        List<UnitEntity> units = tablesDao.getUnits();
        tablesMapping.fillUnitsTables(units, objectsCreated, TABLES);
        List<LimitTableEntity> limits = tablesDao.getLimits();
        tablesMapping.fillLimitsTables(limits, objectsCreated, TABLES);
        List<ResultEntity> results = tablesDao.getResults();
        tablesMapping.fillResultsTables(results, TABLES);
    }
}
