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

    /**
     * Process a DiffEvent.
     *
     * @param event to process.
     */
    void processDiffEvent(DiffEvent event);

    /**
     * Process a ExceptionEvent.
     *
     * @param event to process.
     */
    void processExceptionEvent(ExceptionEvent event);
}
