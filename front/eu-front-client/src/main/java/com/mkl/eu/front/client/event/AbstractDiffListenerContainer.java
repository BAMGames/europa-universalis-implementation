package com.mkl.eu.front.client.event;

import java.util.ArrayList;
import java.util.List;

/**
 * Description of file.
 *
 * @author MKL.
 */
public class AbstractDiffListenerContainer implements IDiffListenerContainer {
    /** Listeners for diffs event. */
    private List<IDiffListener> diffListeners = new ArrayList<>();

    /**
     * Add a diff listener.
     *
     * @param diffListener to add.
     */
    public void addDiffListener(IDiffListener diffListener) {
        if (!diffListeners.contains(diffListener)) {
            diffListeners.add(diffListener);
        }
    }

    /**
     * Process a DiffEvent.
     *
     * @param event to process.
     */
    protected void processDiffEvent(DiffEvent event) {
        for (IDiffListener diffListener : diffListeners) {
            diffListener.update(event);
        }
    }
}
