package com.mkl.eu.service.service.persistence.tables.impl;

import com.mkl.eu.service.service.persistence.impl.GenericDaoImpl;
import com.mkl.eu.service.service.persistence.oe.tables.*;
import com.mkl.eu.service.service.persistence.tables.ITablesDao;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    /** {@inheritDoc} */
    @Override
    public int getTradeIncome(int inputValue, int countryValue, boolean foreignTrade) {
        Criteria criteria = getSession().createCriteria(TradeIncomeEntity.class);

        criteria.add(Restrictions.and(
                Restrictions.or(Restrictions.isNull("minValue"), Restrictions.le("minValue", inputValue)),
                Restrictions.or(Restrictions.isNull("maxValue"), Restrictions.ge("maxValue", inputValue))
        ));
        criteria.add(Restrictions.eq("countryValue", countryValue));
        criteria.add(Restrictions.eq("foreignTrade", foreignTrade));

        //noinspection unchecked
        return ((TradeIncomeEntity) criteria.uniqueResult()).getValue();
    }

    /** {@inheritDoc} */
    @Override
    public List<PeriodEntity> getPeriods() {
        Criteria criteria = getSession().createCriteria(PeriodEntity.class);

        //noinspection unchecked
        return criteria.list();
    }

    /** {@inheritDoc} */
    @Override
    public List<TechEntity> getTechs() {
        Criteria criteria = getSession().createCriteria(TechEntity.class);

        //noinspection unchecked
        return criteria.list();
    }

    /** {@inheritDoc} */
    @Override
    public List<BasicForceTableEntity> getBasicForces() {
        Criteria criteria = getSession().createCriteria(BasicForceTableEntity.class);

        //noinspection unchecked
        return criteria.list();
    }

    /** {@inheritDoc} */
    @Override
    public List<UnitEntity> getUnits() {
        Criteria criteria = getSession().createCriteria(UnitEntity.class);

        //noinspection unchecked
        return criteria.list();
    }

    /** {@inheritDoc} */
    @Override
    public List<LimitTableEntity> getLimits() {
        Criteria criteria = getSession().createCriteria(LimitTableEntity.class);

        //noinspection unchecked
        return criteria.list();
    }

    /** {@inheritDoc} */
    @Override
    public List<ResultEntity> getResults() {
        Criteria criteria = getSession().createCriteria(ResultEntity.class);

        //noinspection unchecked
        return criteria.list();
    }

    /** {@inheritDoc} */
    @Override
    public List<BattleTechEntity> getBattleTechs() {
        Criteria criteria = getSession().createCriteria(BattleTechEntity.class);

        //noinspection unchecked
        return criteria.list();
    }

    /** {@inheritDoc} */
    @Override
    public List<CombatResultEntity> getCombatResults() {
        Criteria criteria = getSession().createCriteria(CombatResultEntity.class);

        //noinspection unchecked
        return criteria.list();
    }

    /** {@inheritDoc} */
    @Override
    public List<ArmyClasseEntity> getArmyClasses() {
        Criteria criteria = getSession().createCriteria(ArmyClasseEntity.class);

        //noinspection unchecked
        return criteria.list();
    }

    /** {@inheritDoc} */
    @Override
    public List<ArmyArtilleryEntity> getArmyArtilleries() {
        Criteria criteria = getSession().createCriteria(ArmyArtilleryEntity.class);

        //noinspection unchecked
        return criteria.list();
    }

    /** {@inheritDoc} */
    @Override
    public List<ArtillerySiegeEntity> getArtillerySieges() {
        Criteria criteria = getSession().createCriteria(ArtillerySiegeEntity.class);

        //noinspection unchecked
        return criteria.list();
    }
}
