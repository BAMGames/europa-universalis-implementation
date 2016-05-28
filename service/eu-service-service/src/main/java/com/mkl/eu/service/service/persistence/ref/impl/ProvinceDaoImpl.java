package com.mkl.eu.service.service.persistence.ref.impl;

import com.mkl.eu.service.service.persistence.impl.GenericDaoImpl;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.GoldEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.RegionEntity;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * Implementation of the Province DAO.
 *
 * @author MKL.
 */
@Repository
public class ProvinceDaoImpl extends GenericDaoImpl<AbstractProvinceEntity, Long> implements IProvinceDao {
    /**
     * Constructor.
     */
    public ProvinceDaoImpl() {
        super(AbstractProvinceEntity.class);
    }

    /** {@inheritDoc} */
    @Override
    public AbstractProvinceEntity getProvinceByName(String name) {
        Criteria criteria = getSession().createCriteria(AbstractProvinceEntity.class);

        criteria.add(Restrictions.eq("name", name));

        return (AbstractProvinceEntity) criteria.uniqueResult();
    }

    /** {@inheritDoc} */
    @Override
    public RegionEntity getRegionByName(String name) {
        Criteria criteria = getSession().createCriteria(RegionEntity.class);

        criteria.add(Restrictions.eq("name", name));

        return (RegionEntity) criteria.uniqueResult();
    }

    /** {@inheritDoc} */
    @Override
    public GoldEntity getGoldInProvince(String name) {
        Criteria criteria = getSession().createCriteria(GoldEntity.class);

        criteria.add(Restrictions.eq("province", name));

        return (GoldEntity) criteria.uniqueResult();
    }
}
