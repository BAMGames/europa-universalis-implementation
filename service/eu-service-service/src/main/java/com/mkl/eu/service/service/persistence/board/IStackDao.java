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
     * @param idGame id of the game.
     * @return the stacks currently moving in the game.
     */
    List<StackEntity> getMovingStacks(Long idGame);
}
