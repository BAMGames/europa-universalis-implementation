package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.service.service.ITablesService;
import com.mkl.eu.client.service.vo.ref.Referential;
import com.mkl.eu.client.service.vo.tables.Tables;
import com.mkl.eu.service.service.mapping.ref.ReferentialMapping;
import com.mkl.eu.service.service.mapping.tables.TablesMapping;
import com.mkl.eu.service.service.persistence.oe.ref.country.CountryEntity;
import com.mkl.eu.service.service.persistence.oe.tables.*;
import com.mkl.eu.service.service.persistence.ref.ICountryDao;
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
    /** Country referential DAO. */
    @Autowired
    private ICountryDao countryDao;
    /** Tables Mapping. */
    @Autowired
    private TablesMapping tablesMapping;
    /** Referential Mapping. */
    @Autowired
    private ReferentialMapping referentialMapping;

    /** {@inheritDoc} */
    @Override
    public Tables getTables() {
        return super.getTables();
    }

    /** {@inheritDoc} */
    @Override
    public Referential getReferential() {
        return super.getReferential();
    }

    /** {@inheritDoc} */
    @Override
    public void refresh() {
        cacheData();
    }

    /** {@inheritDoc} */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        refresh();
    }

    protected void cacheData() {
        cacheTables();
        cacheReferential();
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
        List<BattleTechEntity> battleTechs = tablesDao.getBattleTechs();
        tablesMapping.fillBattleTechTables(battleTechs, TABLES);
        List<CombatResultEntity> combatResults = tablesDao.getCombatResults();
        tablesMapping.fillCombatResultTables(combatResults, TABLES);
        List<ArmyClasseEntity> armyClasses = tablesDao.getArmyClasses();
        tablesMapping.fillArmyClasseTables(armyClasses, TABLES);
        List<ArmyArtilleryEntity> armyArtilleries = tablesDao.getArmyArtilleries();
        tablesMapping.fillArmyArtilleryTables(armyArtilleries, TABLES);
    }

    /**
     * Cache the referential.
     */
    protected void cacheReferential() {
        REFERENTIAL = new Referential();

        List<CountryEntity> countries = countryDao.readAll();
        referentialMapping.fillCountriesReferential(countries, REFERENTIAL);
    }
}
