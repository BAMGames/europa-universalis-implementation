package com.mkl.eu.front.client.event;

/**
 * Container of listeners of diffs made during the update of a game.
 *
 * @author MKL.
 */
public interface IDiffResponseListenerContainer {

    /**
     * Add a diff listener.
     *
     * @param diffListener to add.
     */
    void addDiffListener(IDiffResponseListener diffListener);

    /**
     * Process a DiffResponseEvent.
     *
     * @param event to process.
     */
    void processDiffEvent(DiffResponseEvent event);

    /**
     * Process a ExceptionEvent.
     *
     * @param event to process.
     */
    void processExceptionEvent(ExceptionEvent event);
}
