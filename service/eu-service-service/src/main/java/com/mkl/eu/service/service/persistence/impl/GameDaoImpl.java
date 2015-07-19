package com.mkl.eu.service.service.persistence.impl;

import com.mkl.eu.client.common.vo.AuthentInfo;
import com.mkl.eu.client.service.service.board.FindGamesRequest;
import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Implementation of the Game DAO.
 *
 * @author MKL.
 */
@Repository
public class GameDaoImpl extends GenericDaoImpl<GameEntity, Long> implements IGameDao {
    /**
     * Constructor.
     */
    public GameDaoImpl() {
        super(GameEntity.class);
    }

    /** {@inheritDoc} */
    @Override
    public GameEntity lock(Long idGame) {
        GameEntity game = load(idGame);

        lock(game);

        return game;
    }

    /** {@inheritDoc} */
    @Override
    public void lock(GameEntity game) {
        if (game != null) {
            getSession().buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_FORCE_INCREMENT)).lock(game);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<GameEntity> findGames(FindGamesRequest findGames) {
        Criteria criteria = getSession().createCriteria(GameEntity.class);

        if (findGames != null) {
            if (!StringUtils.isEmpty(findGames.getUsername()) && !StringUtils.equals(AuthentInfo.USERNAME_ANONYMOUS, findGames.getUsername())) {
                Criteria criteriaCountry = criteria.createCriteria("countries", "countries");
                criteriaCountry.add(Restrictions.eq("username", findGames.getUsername()));
            }
        }

        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        return listAndCast(criteria);
    }
}
