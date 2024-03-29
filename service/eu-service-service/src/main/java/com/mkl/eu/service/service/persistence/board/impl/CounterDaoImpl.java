package com.mkl.eu.service.service.persistence.board.impl;

import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.impl.GenericDaoImpl;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of the Counter DAO.
 *
 * @author MKL.
 */
@Repository
public class CounterDaoImpl extends GenericDaoImpl<CounterEntity, Long> implements ICounterDao {
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
    public CounterDaoImpl() {
        super(CounterEntity.class);
    }

    /** {@inheritDoc} */
    @Override
    public CounterEntity getCounter(Long idCounter, Long idGame) {
        CounterEntity counter = load(idCounter);

        if (idGame == null || counter == null || counter.getOwner() == null
                || counter.getOwner().getGame() == null
                || counter.getOwner().getGame().getId().longValue() != idGame.longValue()) {
            counter = null;
        }

        return counter;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getPatrons(String country, Long idGame) {
        List<String> countries;
        Criteria criteria = getSession().createCriteria(CounterEntity.class);

        criteria.add(Restrictions.or(Restrictions.eq("type", CounterFaceTypeEnum.DIPLOMACY), Restrictions.eq("type", CounterFaceTypeEnum.DIPLOMACY_WAR)));
        criteria.add(Restrictions.eq("country", country));

        Criteria criteriaStack = criteria.createCriteria("owner", "owner");
        criteriaStack.add(Restrictions.eq("game.id", idGame));

        CounterEntity counter = (CounterEntity) criteria.uniqueResult();
        if (counter != null) {
            countries = new ArrayList<>();
            String box = counter.getOwner().getProvince();
            Matcher m = Pattern.compile("B_DE_([a-zA-Z]*)\\-.*").matcher(box);
            if (m.matches()) {
                countries.add(m.group(1));
            }
        } else {
            criteria = getSession().createCriteria(CounterEntity.class);

            criteria.add(Restrictions.or(Restrictions.eq("type", CounterFaceTypeEnum.ROTW_RELATION), Restrictions.eq("type", CounterFaceTypeEnum.ROTW_ALLIANCE)));
            criteria.add(Restrictions.eq("owner.province", "B_DR_" + country));

            criteriaStack = criteria.createCriteria("owner", "owner");
            criteriaStack.add(Restrictions.eq("game.id", idGame));

            @SuppressWarnings("unchecked") List<CounterEntity> counters = criteria.list();

            countries = counters.stream().map(CounterEntity::getCountry).collect(Collectors.toList());
        }
        countries.add(country);

        return countries;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getMinors(String country, Long idGame) {
        Criteria criteria = getSession().createCriteria(CounterEntity.class);

        criteria.add(Restrictions.or(Restrictions.eq("type", CounterFaceTypeEnum.DIPLOMACY), Restrictions.eq("type", CounterFaceTypeEnum.DIPLOMACY_WAR)));
        criteria.add(Restrictions.like("owner.province", "B_DE_" + country + "%"));

        Criteria criteriaStack = criteria.createCriteria("owner", "owner");
        criteriaStack.add(Restrictions.eq("game.id", idGame));

        List<CounterEntity> counters = criteria.list();
        List<String> countries = counters.stream()
                .map(CounterEntity::getCountry)
                .collect(Collectors.toList());

        criteria = getSession().createCriteria(CounterEntity.class);

        criteria.add(Restrictions.or(Restrictions.eq("type", CounterFaceTypeEnum.ROTW_RELATION), Restrictions.eq("type", CounterFaceTypeEnum.ROTW_ALLIANCE)));
        criteria.add(Restrictions.eq("country", country));

        counters = criteria.list();
        countries.addAll(counters.stream()
                .map(counter -> counter.getOwner().getProvince())
                .map(prov -> prov.substring(5))
                .collect(Collectors.toList()));

        return countries;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getVassals(String country, Long idGame) {
        List<String> countries = new ArrayList<>();
        Criteria criteria = getSession().createCriteria(CounterEntity.class);

        criteria.add(Restrictions.or(Restrictions.eq("type", CounterFaceTypeEnum.DIPLOMACY), Restrictions.eq("type", CounterFaceTypeEnum.DIPLOMACY_WAR)));

        Criteria criteriaStack = criteria.createCriteria("owner", "owner");
        criteriaStack.add(Restrictions.eq("game.id", idGame));
        criteriaStack.add(Restrictions.or(Restrictions.eq("province", "B_DE_" + country + "-VA"), Restrictions.eq("province", "B_DE_" + country + "-AN")));

        @SuppressWarnings("unchecked") List<CounterEntity> counters = (List<CounterEntity>) criteria.list();

        if (counters != null) {
            countries.addAll(counters.stream().map(CounterEntity::getCountry).collect(Collectors.toList()));
        }

        return countries;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getNeighboringOwners(String province, Long idGame) {
        List<String> countries = new ArrayList<>();

        String sql = queryProps.getProperty("game.neighbor_owner");

        sql = sql.replace(":province", province);
        sql = sql.replace(":idGame", Long.toString(idGame));
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

        results.stream().forEach(input -> countries.add((String) input.get("OWNER")));

        return countries;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getNationalTerritoriesUnderAttack(String country, List<String> enemies, Long idGame) {
        List<String> provinces = new ArrayList<>();

        String sql = queryProps.getProperty("game.national_territory_under_attack");

        sql = sql.replace(":country", country);
        String enemyNames = enemies.stream().collect(Collectors.joining("','", "('", "')"));
        sql = sql.replace(":enemies", enemyNames);
        sql = sql.replace(":idGame", Long.toString(idGame));
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

        results.stream().forEach(input -> provinces.add((String) input.get("name")));

        return provinces;
    }

    /** {@inheritDoc} */
    @Override
    public int getGoldExploitedRotw(Long idGame) {
        String sql = queryProps.getProperty("eco.gold_exploited");

        sql = sql.replace(":idGame", Long.toString(idGame));

        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getGoldExploitedAmerica(String country, Long idGame) {
        String sql = queryProps.getProperty("eco.gold_exploited_america");

        sql = sql.replace(":idGame", Long.toString(idGame));
        sql = sql.replace(":country", country);

        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isGovernorInSameRegion(String region, String country, Long idGame) {
        String sql = queryProps.getProperty("col_tp.governor_region");

        sql = sql.replace(":idGame", Long.toString(idGame));
        sql = sql.replace(":country", country);
        sql = sql.replace(":region", region);

        return jdbcTemplate.queryForObject(sql, Boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getColdAreaPenaltyRotw(Collection<String> provinces, String country, Long idGame) {
        String sql = queryProps.getProperty("province.rotw_cold_area_penalty");

        sql = sql.replace(":idGame", Long.toString(idGame));
        String provinceNames = provinces.stream().collect(Collectors.joining("','", "('", "')"));
        sql = sql.replace(":provinces", provinceNames);
        sql = sql.replace(":country", country);

        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
}
