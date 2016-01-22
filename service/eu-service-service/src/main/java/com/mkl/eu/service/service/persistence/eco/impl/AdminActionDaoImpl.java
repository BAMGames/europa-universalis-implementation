package com.mkl.eu.service.service.persistence.eco.impl;

import com.mkl.eu.client.service.vo.enumeration.AdminActionStatusEnum;
import com.mkl.eu.client.service.vo.enumeration.AdminActionTypeEnum;
import com.mkl.eu.service.service.persistence.eco.IAdminActionDao;
import com.mkl.eu.service.service.persistence.impl.GenericDaoImpl;
import com.mkl.eu.service.service.persistence.oe.eco.AdministrativeActionEntity;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Implementation of the EconomicalSheet DAO.
 *
 * @author MKL.
 */
@Repository
public class AdminActionDaoImpl extends GenericDaoImpl<AdministrativeActionEntity, Long> implements IAdminActionDao {

    /**
     * Constructor.
     */
    public AdminActionDaoImpl() {
        super(AdministrativeActionEntity.class);
    }

    /** {@inheritDoc} */
    @Override
    public List<AdministrativeActionEntity> findAdminActions(Long idCountry, Integer turn, Long idObject, AdminActionTypeEnum... types) {
        Criteria criteria = getSession().createCriteria(AdministrativeActionEntity.class);

        if (turn != null) {
            criteria.add(Restrictions.eq("turn", turn));
        }
        if (idCountry != null) {
            criteria.add(Restrictions.eq("country.id", idCountry));
        }
        if (idObject != null) {
            criteria.add(Restrictions.eq("idObject", idObject));
        }
        if (types != null) {
            criteria.add(Restrictions.in("type", types));
        }
        criteria.add(Restrictions.eq("status", AdminActionStatusEnum.PLANNED));

        //noinspection unchecked
        return (List<AdministrativeActionEntity>) criteria.list();
    }
}
