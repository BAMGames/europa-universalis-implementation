package com.mkl.eu.service.service.persistence.board;

import com.mkl.eu.service.service.persistence.IGenericDao;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;

import java.util.List;

/**
 * Interface of the Stack DAO.
 *
 * @author MKL.
 */
public interface IStackDao extends IGenericDao<StackEntity, Long> {
    /**
     * Returns the stacks on the province for a given game.
     *
     * @param province the province.
     * @param idGame   id of the game.
     * @return the stacks on the province.
     */
    List<StackEntity> getStacksOnProvince(String province, Long idGame);
}
