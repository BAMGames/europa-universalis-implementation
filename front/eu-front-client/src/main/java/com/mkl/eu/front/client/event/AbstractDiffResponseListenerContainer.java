package com.mkl.eu.front.client.event;

import com.mkl.eu.front.client.main.GameConfiguration;
import com.mkl.eu.front.client.vo.AuthentHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Description of file.
 *
 * @author MKL.
 */
public class AbstractDiffResponseListenerContainer implements IDiffResponseListenerContainer, IServiceCaller {
    /** Logger. */
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    /** Listeners for diffs event. */
    private List<IDiffResponseListener> diffListeners = new ArrayList<>();
    /** Game configuration. */
    protected GameConfiguration gameConfig;
    /** Component holding the authentication information. */
    @Autowired
    protected AuthentHolder authentHolder;

    /**
     * Constructor.
     *
     * @param gameConfig the gameConfig to set.
     */
    public AbstractDiffResponseListenerContainer(GameConfiguration gameConfig) {
        this.gameConfig = gameConfig;
    }

    /** {@inheritDoc} */
    @Override
    public GameConfiguration getGameConfig() {
        return gameConfig;
    }

    /** {@inheritDoc} */
    @Override
    public AuthentHolder getAuthentHolder() {
        return authentHolder;
    }

    /** {@inheritDoc} */
    @Override
    public void addDiffListener(IDiffResponseListener diffListener) {
        if (!diffListeners.contains(diffListener)) {
            diffListeners.add(diffListener);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void processDiffEvent(DiffResponseEvent event) {
        for (IDiffResponseListener diffListener : diffListeners) {
            diffListener.update(event);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void processExceptionEvent(ExceptionEvent event) {
        for (IDiffResponseListener diffListener : diffListeners) {
            diffListener.handleException(event);
        }
    }
}
