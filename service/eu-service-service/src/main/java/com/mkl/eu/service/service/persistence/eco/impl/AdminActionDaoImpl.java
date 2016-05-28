package com.mkl.eu.service.service.persistence.eco.impl;

import com.mkl.eu.client.service.vo.enumeration.AdminActionStatusEnum;
import com.mkl.eu.client.service.vo.enumeration.AdminActionTypeEnum;
import com.mkl.eu.service.service.persistence.eco.IAdminActionDao;
import com.mkl.eu.service.service.persistence.impl.GenericDaoImpl;
import com.mkl.eu.service.service.persistence.oe.eco.AdministrativeActionEntity;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Implementation of the EconomicalSheet DAO.
 *
 * @author MKL.
 */
@Repository
public class AdminActionDaoImpl extends GenericDaoImpl<AdministrativeActionEntity, Long> implements IAdminActionDao {
    /** Template jdbc . */
    @Autowired
    private JdbcTemplate jdbcTemplate;
    /** Sql queries. */
    @Autowired
    @Qualifier("queryProps")
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private Properties queryProps;

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

    /** {@inheritDoc} */
    @Override
    public List<String> getCountriesInlandAdvance(String province, Long idGame) {
        List<String> countries = new ArrayList<>();

        String sql = queryProps.getProperty("colony.inlandAdvance.eu");

        sql = sql.replace(":province", province);
        sql = sql.replace(":idGame", Long.toString(idGame));
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

        results.stream().forEach(input -> countries.add((String) input.get("OWNER")));

        sql = queryProps.getProperty("colony.inlandAdvance.rotw");

        sql = sql.replace(":province", province);
        sql = sql.replace(":idGame", Long.toString(idGame));
        results = jdbcTemplate.queryForList(sql);

        results.stream().forEach(input -> countries.add((String) input.get("OWNER")));

        return countries;
    }
}
