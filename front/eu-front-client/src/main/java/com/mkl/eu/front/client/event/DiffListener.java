package com.mkl.eu.front.client.event;

/**
 * Listener of diffs made during the update of a game.
 *
 * @author MKL.
 */
public interface DiffListener {
    /**
     * Update the client with the given event.
     *
     * @param event with the diffs and the new version of the game.
     */
    void update(DiffEvent event);
}
