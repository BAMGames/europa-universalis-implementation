package com.mkl.eu.service.service.domain;

import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;

import java.util.List;
import java.util.Optional;

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
     * Creates a counter.
     *
     * @param type     of the counter to create.
     * @param code     of the leader.
     * @param country  owner of the counter to create.
     * @param idStack  id of the stack the counter will be attached to.
     * @param province where the counter will be if idStack is <code>null</code>.
     * @param game     the game.
     * @return the diffs related to the creation of the counter.
     */
    DiffEntity createLeader(CounterFaceTypeEnum type, String code, String country, Long idStack, String province, GameEntity game);

    /**
     * Removes a counter from a game.
     *
     * @param counter the counter to remove.
     * @return the diffs related to the removal of the counter.
     */
    DiffEntity removeCounter(CounterEntity counter);

    /**
     * Switch a counter from a game to a given type.
     *
     * @param counter the counter to switch.
     * @param type    new type of the counter.
     * @param level   new level of the trade fleet or establishment.
     * @param game    the game.
     * @return the diffs related to the switch of the counter. Returns <code>null</code> if the idCounter is not found in the given game.
     */
    DiffEntity switchCounter(CounterEntity counter, CounterFaceTypeEnum type, Integer level, GameEntity game);

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

    /**
     * Moves a leader to a specific stack, or a province if no stack specified (and then creates a new stack).
     * The leader will lead the stack it is moved to if he is eligible.
     *
     * @param leader   the counter leader.
     * @param stackTo  the stack where the leader will be.
     * @param province the province where the leader will be if stackTo is <code>null</code>.
     * @param game     the game.
     * @return the diffs related to the move of the leader.
     */
    List<DiffEntity> moveLeader(CounterEntity leader, StackEntity stackTo, String province, GameEntity game);

    /**
     * Creates a stack.
     *
     * @param province where is the stack.
     * @param country  controlling the stack.
     * @param game     the game.
     * @return the created stack.
     */
    StackEntity createStack(String province, String country, GameEntity game);

    /**
     * Moves a counter to another stack in the same province.
     *
     * @param counter  the counter to move.
     * @param newOwner the stack where the counter will be.
     * @param game     the game.
     * @return the diffs related to the move of the counter.
     */
    DiffEntity changeCounterOwner(CounterEntity counter, StackEntity newOwner, GameEntity game);

    /**
     * Changes the country of a counter.
     *
     * @param counter    the counter to move.
     * @param newCountry the new country the counter will belong to.
     * @param game       the game.
     * @return the diffs related to the move of the counter.
     */
    DiffEntity changeCounterCountry(CounterEntity counter, String newCountry, GameEntity game);

    /**
     * Increate the inflation counter by one box.
     *
     * @param game the game.
     * @return the diff, if any, related to the inflation increase.
     */
    Optional<DiffEntity> increaseInflation(GameEntity game);

    /**
     * Increate the inflation counter by one box.
     *
     * @param game the game.
     * @return the diff, if any, related to the inflation increase.
     */
    Optional<DiffEntity> decreaseInflation(GameEntity game);
}
