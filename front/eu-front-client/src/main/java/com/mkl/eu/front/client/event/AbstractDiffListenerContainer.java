package com.mkl.eu.front.client.event;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.front.client.main.GameConfiguration;
import com.mkl.eu.front.client.vo.AuthentHolder;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Description of file.
 *
 * @author MKL.
 */
public class AbstractDiffListenerContainer implements IDiffListenerContainer {
    /** Logger. */
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    /** Listeners for diffs event. */
    private List<IDiffListener> diffListeners = new ArrayList<>();
    /** Game configuration. */
    protected GameConfiguration gameConfig;
    /** Component holding the authentication information. */
    @Autowired
    protected AuthentHolder authentHolder;

    public AbstractDiffListenerContainer(GameConfiguration gameConfig) {
        this.gameConfig = gameConfig;
    }

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

    /**
     * Create an event handler to give to an actionHandler that will call a back end service.
     *
     * @param service         the service to call.
     * @param requestSupplier the supplier that will create the request.
     * @param errorMessage    the error message to display if it fails.
     * @param <T>             the class of the request.
     * @return the event handler.
     */
    protected <T> EventHandler<ActionEvent> callService(IService<T> service, Supplier<T> requestSupplier, String errorMessage) {
        return event -> {
            Request<T> request = new Request<>();
            authentHolder.fillAuthentInfo(request);
            gameConfig.fillGameInfo(request);
            gameConfig.fillChatInfo(request);
            request.setRequest(requestSupplier.get());
            Long idGame = gameConfig.getIdGame();
            try {
                DiffResponse response = service.run(request);

                DiffEvent diff = new DiffEvent(response, idGame);
                processDiffEvent(diff);
            } catch (FunctionalException e) {
                LOGGER.error(errorMessage, e);

                processExceptionEvent(new ExceptionEvent(e));
            }
        };
    }

    /**
     * Interface that matches all back end services.
     *
     * @param <V> the type of the request.
     */
    protected interface IService<V> {
        DiffResponse run(Request<V> request) throws TechnicalException, FunctionalException;
    }
}
