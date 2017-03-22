package com.mkl.eu.service.service.persistence.eco.impl;

import com.mkl.eu.client.service.vo.enumeration.AdminActionStatusEnum;
import com.mkl.eu.client.service.vo.enumeration.AdminActionTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.CultureEnum;
import com.mkl.eu.service.service.persistence.eco.IAdminActionDao;
import com.mkl.eu.service.service.persistence.impl.GenericDaoImpl;
import com.mkl.eu.service.service.persistence.oe.eco.AdministrativeActionEntity;
import com.mkl.eu.service.service.persistence.oe.eco.CompetitionEntity;
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
import java.util.stream.Collectors;

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
    public List<AdministrativeActionEntity> findPlannedAdminActions(Long idCountry, Integer turn, Long idObject, AdminActionTypeEnum... types) {
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
        if (types != null && types.length > 0) {
            criteria.add(Restrictions.in("type", types));
        }
        criteria.add(Restrictions.eq("status", AdminActionStatusEnum.PLANNED));

        //noinspection unchecked
        return (List<AdministrativeActionEntity>) criteria.list();
    }

    /** {@inheritDoc} */
    @Override
    public List<AdministrativeActionEntity> findDoneAdminActions(Integer turn, Long idGame) {
        Criteria criteria = getSession().createCriteria(AdministrativeActionEntity.class);

        if (turn != null) {
            criteria.add(Restrictions.eq("turn", turn));
        }
        Criteria criteriaCountry = criteria.createCriteria("country", "country");
        criteriaCountry.add(Restrictions.eq("game.id", idGame));
        criteria.add(Restrictions.eq("status", AdminActionStatusEnum.DONE));

        //noinspection unchecked
        return (List<AdministrativeActionEntity>) criteria.list();
    }

    /** {@inheritDoc} */
    @Override
    public List<CompetitionEntity> findCompetitions(Integer turn, Long idGame) {
        Criteria criteria = getSession().createCriteria(CompetitionEntity.class);

        if (turn != null) {
            criteria.add(Restrictions.eq("turn", turn));
        }
        criteria.add(Restrictions.eq("game.id", idGame));

        //noinspection unchecked
        return (List<CompetitionEntity>) criteria.list();
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

        results.stream().forEach(input -> {
            String country = (String) input.get("OWNER");
            if (!countries.contains(country)) {
                countries.add(country);
            }
        });

        return countries;
    }

    /** {@inheritDoc} */
    @Override
    public int countOtherTpsInRegion(String country, String region, Long idGame) {
        String sql = queryProps.getProperty("tp.others_region");

        sql = sql.replace(":countryName", country);
        sql = sql.replace(":region", region);
        sql = sql.replace(":idGame", Long.toString(idGame));

        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getCountriesTradeFleetAccessRotw(String province, Long idGame) {
        List<String> countries = new ArrayList<>();

        String sql = queryProps.getProperty("tz.trade_fleet_access_rotw");

        sql = sql.replace(":province", province);
        sql = sql.replace(":idGame", Long.toString(idGame));
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

        results.stream().forEach(input -> countries.add((String) input.get("OWNER")));

        return countries;
    }

    @Override
    public Integer getMaxTechBox(boolean land, List<CultureEnum> cultures, Long idGame) {
        String sql = queryProps.getProperty("tech.culture");

        CounterFaceTypeEnum type;
        if (land) {
            type = CounterFaceTypeEnum.TECH_LAND;
        } else {
            type = CounterFaceTypeEnum.TECH_NAVAL;
        }
        String cultureNames = cultures.stream().map(Enum::name).collect(Collectors.joining("','", "('", "')"));
        sql = sql.replace(":techType", type.name());
        sql = sql.replace(":cultures", cultureNames);
        sql = sql.replace(":idGame", Long.toString(idGame));

        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
}
