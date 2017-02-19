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
     * It can either switch to logistic of minor powers or
     * directly to military phase if no minors are at war.
     * <p>
     * If it switch to military phase, it will create military turn order.
     *
     * @param game the game.
     * @return the diffs related to the creation of the counter.
     */
    List<DiffEntity> computeEndAdministrativeActions(GameEntity game);
}
