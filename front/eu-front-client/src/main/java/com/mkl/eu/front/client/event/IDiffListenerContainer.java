package com.mkl.eu.front.client.event;

/**
 * Container of listeners of diffs made during the update of a game.
 *
 * @author MKL.
 */
public interface IDiffListenerContainer {

    /**
     * Add a diff listener.
     *
     * @param diffListener to add.
     */
    void addDiffListener(IDiffListener diffListener);
}
