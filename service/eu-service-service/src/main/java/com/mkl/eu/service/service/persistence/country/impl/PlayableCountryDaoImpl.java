package com.mkl.eu.service.service.persistence.country.impl;

import com.mkl.eu.service.service.persistence.country.IPlayableCountryDao;
import com.mkl.eu.service.service.persistence.impl.GenericDaoImpl;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import org.apache.commons.collections.CollectionUtils;
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
 * Implementation of the PlayableCountry DAO.
 *
 * @author MKL.
 */
@Repository
public class PlayableCountryDaoImpl extends GenericDaoImpl<PlayableCountryEntity, Long> implements IPlayableCountryDao {
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
    public PlayableCountryDaoImpl() {
        super(PlayableCountryEntity.class);
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getOwnedProvinces(String name, Long idGame) {
        List<String> provinces = new ArrayList<>();

        String sql = queryProps.getProperty("game.ownedProvinces");

        sql = sql.replace(":countryName", name);
        sql = sql.replace(":idGame", Long.toString(idGame));
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

        results.stream().forEach(input -> provinces.add((String) input.get("PROVINCE")));

        return provinces;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFatherlandInDanger(String countryName, List<String> enemies, Long idGame) {
        // No enemy, no danger.
        if (CollectionUtils.isEmpty(enemies)) {
            return false;
        }

        String enemyNames = enemies.stream().collect(Collectors.joining("','", "('", "')"));

        String sql = queryProps.getProperty("game.fatherlandInDanger");

        sql = sql.replace(":countryName", countryName);
        sql = sql.replace(":enemies", enemyNames);
        sql = sql.replace(":idGame", Long.toString(idGame));

        return jdbcTemplate.queryForList(sql).size() > 0;
    }
}
