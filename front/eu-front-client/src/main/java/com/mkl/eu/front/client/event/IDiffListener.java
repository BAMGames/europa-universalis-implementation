package com.mkl.eu.front.client.event;

import com.mkl.eu.client.service.vo.diff.Diff;

/**
 * Listener of diff spread during the update of a game.
 *
 * @author MKL.
 */
public interface IDiffListener {
    /**
     * Update the client with the given diff.
     *
     * @param diff the diff.
     */
    void update(Diff diff);

    /**
     * Method called when all diffs of a DiffResponseEvent have been computed.
     */
    default void updateComplete() {
    }
}
