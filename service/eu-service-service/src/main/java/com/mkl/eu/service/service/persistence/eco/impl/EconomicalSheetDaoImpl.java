package com.mkl.eu.service.service.persistence.eco.impl;

import com.mkl.eu.service.service.persistence.eco.IEconomicalSheetDao;
import com.mkl.eu.service.service.persistence.impl.GenericDaoImpl;
import com.mkl.eu.service.service.persistence.oe.eco.EconomicalSheetEntity;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the EconomicalSheet DAO.
 *
 * @author MKL.
 */
@Repository
public class EconomicalSheetDaoImpl extends GenericDaoImpl<EconomicalSheetEntity, Long> implements IEconomicalSheetDao {
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
    public EconomicalSheetDaoImpl() {
        super(EconomicalSheetEntity.class);
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Integer> getOwnedAndControlledProvinces(String name, Long idGame) {
        Map<String, Integer> incomeByProvinces = new HashMap<>();

        String sql = queryProps.getProperty("income.NationalProvinces");

        sql = sql.replace(":countryName", name);
        sql = sql.replace(":idGame", Long.toString(idGame));
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

        results.stream().forEach(input -> incomeByProvinces.put((String) input.get("PROVINCE"), (Integer) input.get("INCOME")));

        sql = queryProps.getProperty("income.OwnedProvinces");

        sql = sql.replace(":countryName", name);
        sql = sql.replace(":idGame", Long.toString(idGame));
        results = jdbcTemplate.queryForList(sql);

        results.stream().forEach(input -> incomeByProvinces.put((String) input.get("PROVINCE"), (Integer) input.get("INCOME")));

        return incomeByProvinces;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getPillagedProvinces(List<String> provinces, Long idGame) {
        List<String> pillagedProvinces = new ArrayList<>();

        String sql = queryProps.getProperty("income.PillagedProvinces");

        String provinceNames = provinces.stream().collect(Collectors.joining("','", "('", "')"));
        sql = sql.replace(":provinceNames", provinceNames);
        sql = sql.replace(":idGame", Long.toString(idGame));
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        results.stream().forEach(input -> pillagedProvinces.add((String) input.get("PROVINCE")));

        return pillagedProvinces;
    }

    /** {@inheritDoc} */
    @Override
    public List<EconomicalSheetEntity> loadSheets(Long idGame, Long idCountry, Integer turn) {
        Criteria criteria = getSession().createCriteria(EconomicalSheetEntity.class);

        criteria.add(Restrictions.eq("turn", turn));
        if (idCountry != null) {
            criteria.add(Restrictions.eq("country.id", idCountry));
        } else {
            Criteria criteriaCountry = criteria.createCriteria("country", "country");
            criteriaCountry.add(Restrictions.eq("game.id", idGame));
        }

        //noinspection unchecked
        return (List<EconomicalSheetEntity>) criteria.list();
    }
}
