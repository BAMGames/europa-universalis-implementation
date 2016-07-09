package com.mkl.eu.service.service.domain;

import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;

/**
 * Interface for cross service methods around counter manipulation.
 *
 * @author MKL.
 */
public interface ICounterDomain {
    /**
     * Creates a counter in a new stack.
     *
     * @param type     of the counter to create.
     * @param country  owner of the counter to create.
     * @param province where the counter will be.
     * @param game     the game.
     * @return the diffs related to the creation of the counter.
     */
    DiffEntity createCounter(CounterFaceTypeEnum type, String country, String province, GameEntity game);

    /**
     * Removes a counter from a game.
     *
     * @param idCounter the id of the counter to remove.
     * @param game      the game.
     * @return the diffs related to the removal of the counter. Returns <code>null</code> if the idCounter is not found in the given game.
     */
    DiffEntity removeCounter(Long idCounter, GameEntity game);

    /**
     * Switch a counter from a game to a given type.
     *
     * @param idCounter the id of the counter to switch.
     * @param type      new type of the counter.
     * @param game      the game.
     * @return the diffs related to the switch of the counter. Returns <code>null</code> if the idCounter is not found in the given game.
     */
    DiffEntity switchCounter(Long idCounter, CounterFaceTypeEnum type, GameEntity game);
}
