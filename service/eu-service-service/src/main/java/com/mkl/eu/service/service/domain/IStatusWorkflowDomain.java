package com.mkl.eu.service.service.domain;

import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;

import java.util.List;

/**
 * Interface for cross service methods around status workflow.
 *
 * @author MKL.
 */
public interface IStatusWorkflowDomain {
    /**
     * Compute the end of the administrative actions phase.
     * It will then switch to minor logistics phase except
     * if no minor is involved in a war, in which case it will
     * call computeEndMinorLogistics.
     *
     * @param game the game.
     * @return the diffs related to the change of phase.
     */
    List<DiffEntity> computeEndAdministrativeActions(GameEntity game);

    /**
     * Compute the end of the minor logistics phase.
     * It will then switch to military phase, and creates
     * the turn order accordingly.
     *
     * @param game the game.
     * @return the diffs related to the change of phase.
     */
    List<DiffEntity> computeEndMinorLogistics(GameEntity game);

    /**
     * Method to call at the end of the military phase of a group of playing countries.
     * Or when battles are completed.
     * Or when sieges are finished.
     *
     * @param game the game.
     * @return the diff corresponding to the switch of the next round (or phase).
     */
    List<DiffEntity> endMilitaryPhase(GameEntity game);

    /**
     * Compute the end of a round.
     * Go to next round, or next phase if it was the last round.
     *
     * @param game the game.
     * @return the diffs related to the change of round.
     */
    List<DiffEntity> nextRound(GameEntity game);

    /**
     * Method to call at the end of the redeployment phase of a group of playing countries.
     *
     * @param game the game.
     * @return the diff corresponding to the switch of the next phase.
     */
    List<DiffEntity> endRedeploymentPhase(GameEntity game);

    /**
     * Method to call at the end of the exchequer phase.
     *
     * @param game the game.
     * @return the diff corresponding to the switch of the next phase.
     */
    List<DiffEntity> endExchequerPhase(GameEntity game);

    /**
     * Method to call at the end of the stability phase.
     *
     * @param game the game.
     * @return the diff corresponding to the switch of the next phase.
     */
    List<DiffEntity> endStabilityPhase(GameEntity game);

    /**
     * Compute the economical sheets of the active countries for the game turn.
     *
     * @param game the game.
     * @return the diff corresponding to the new economical sheets.
     */
    DiffEntity computeEconomicalSheets(GameEntity game);

    /**
     * Put the new leaders of the turn of playing countries on the turn box.
     * Then, for each playing country, check if leader limit is not reach and then
     * randomly put anonymous leaders on the turn box.
     *
     * @param game the game.
     * @return the diffs of the leaders change.
     */
    List<DiffEntity> deployLeaders(GameEntity game);
}
