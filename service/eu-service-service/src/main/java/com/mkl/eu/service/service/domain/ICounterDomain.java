package com.mkl.eu.service.service.domain;

import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import org.apache.commons.lang3.tuple.Pair;

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
     * @param level    new level of the trade fleet or establishment.
     * @param game     the game.
     * @return the diffs related to the creation of the counter.
     */
    DiffEntity createCounter(CounterFaceTypeEnum type, String country, String province, Integer level, GameEntity game);

    /**
     * Creates a counter in an existing stack.
     *
     * @param type    of the counter to create.
     * @param country owner of the counter to create.
     * @param idStack id of the stack the counter will be attached to.
     * @param game    the game.
     * @return the diffs related to the creation of the counter.
     */
    DiffEntity createCounter(CounterFaceTypeEnum type, String country, Long idStack, GameEntity game);

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
     * @param level     new level of the trade fleet or establishment.
     * @param game      the game.
     * @return the diffs related to the switch of the counter. Returns <code>null</code> if the idCounter is not found in the given game.
     */
    DiffEntity switchCounter(Long idCounter, CounterFaceTypeEnum type, Integer level, GameEntity game);

    /**
     * Switch a counter from a game to a given type.
     *
     * @param idCounter the id of the counter to switch.
     * @param type      new type of the counter.
     * @param level     new level of the trade fleet or establishment.
     * @param game      the game.
     * @return the diffs related to the switch of the counter and the counter switched. Returns <code>null</code> if the idCounter is not found in the given game.
     */
    Pair<DiffEntity, CounterEntity> switchAndGetCounter(Long idCounter, CounterFaceTypeEnum type, Integer level, GameEntity game);

    /**
     * Change the number of veterans of a counter from a game.
     *
     * @param idCounter the id of the counter to switch.
     * @param veterans  new number of veterans in the counter.
     * @param game      the game.
     * @return the diffs related to the switch of the counter. Returns <code>null</code> if the idCounter is not found in the given game.
     */
    DiffEntity changeVeteransCounter(Long idCounter, Double veterans, GameEntity game);

    /**
     * Moves a special counter. Type and country (can be <code>null</code> in case of neutral counters) should
     * identify an unique counter.
     * Then, the province is the box where the counter should move.
     * For special counters, each box has a unique stack which holds all counters of the box.
     * This method creates a stack if needed, or move the counter to an existing stack. If former stack has no counter left,
     * it will remove the former stack.
     *
     * @param type     of the counter to move.
     * @param country  owner of the counter to move.
     * @param province where the counter will be.
     * @param game     the game.
     * @return the diffs related to the move of the special counter.
     */
    DiffEntity moveSpecialCounter(CounterFaceTypeEnum type, String country, String province, GameEntity game);
}
