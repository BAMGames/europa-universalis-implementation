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

    /** {@inheritDoc} */
    @Override
    public void addDiffListener(IDiffListener diffListener) {
        if (!diffListeners.contains(diffListener)) {
            diffListeners.add(diffListener);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void processDiffEvent(DiffEvent event) {
        for (IDiffListener diffListener : diffListeners) {
            diffListener.update(event);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void processExceptionEvent(ExceptionEvent event) {
        for (IDiffListener diffListener : diffListeners) {
            diffListener.handleException(event);
        }
    }
}
