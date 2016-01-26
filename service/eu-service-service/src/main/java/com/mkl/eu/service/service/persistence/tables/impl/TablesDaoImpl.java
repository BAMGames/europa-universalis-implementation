package com.mkl.eu.service.service.persistence.tables.impl;

import com.mkl.eu.service.service.persistence.impl.GenericDaoImpl;
import com.mkl.eu.service.service.persistence.oe.tables.BasicForceTableEntity;
import com.mkl.eu.service.service.persistence.oe.tables.LimitTableEntity;
import com.mkl.eu.service.service.persistence.oe.tables.TradeIncomeEntity;
import com.mkl.eu.service.service.persistence.oe.tables.UnitEntity;
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
    public Integer getTradeIncome(Integer inputValue, Integer countryValue, boolean foreignTrade) {
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
}
