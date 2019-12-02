package com.mkl.eu.front.client.event;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffAttributes;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeObjectEnum;
import com.mkl.eu.front.client.main.GameConfiguration;
import com.mkl.eu.front.client.map.component.INotJavaFxServiceCaller;
import com.mkl.eu.front.client.vo.AuthentHolder;
import com.mkl.eu.front.client.window.InteractiveMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Interfaces for component wishing to call back-end services.
 *
 * @author MKL.
 */
public interface IServiceCaller extends IDiffResponseListenerContainer {
    /** Logger. */
    Logger LOGGER = LoggerFactory.getLogger(IServiceCaller.class);

    /**
     * @return a GameConfiguration for configuration related to a game.
     */
    GameConfiguration getGameConfig();

    /**
     * @return a AuthentHolder for info about authentication.
     */
    AuthentHolder getAuthentHolder();

    /**
     * Create an event handler to give to an actionHandler that will call a back end service.
     *
     * @param service         the service to call.
     * @param requestSupplier the supplier that will create the request.
     * @param errorMessage    the error message to display if it fails.
     * @param <T>             the class of the request.
     * @return the event handler.
     */
    default <T> EventHandler<ActionEvent> callServiceAsEvent(IServiceCaller.IService<T> service, Supplier<T> requestSupplier, String errorMessage) {
        return callServiceAsEvent(service, requestSupplier, errorMessage, null, null);
    }

    /**
     * Call a back end service.
     *
     * @param service         the service to call.
     * @param requestSupplier the supplier that will create the request.
     * @param errorMessage    the error message to display if it fails.
     * @param <T>             the class of the request.
     */
    default <T> void callService(IServiceCaller.IService<T> service, Supplier<T> requestSupplier, String errorMessage) {
        callService(service, requestSupplier, errorMessage, null, null);
    }

    /**
     * Create an event handler to give to an actionHandler that will call a back end service.
     *
     * @param service         the service to call.
     * @param requestSupplier the supplier that will create the request.
     * @param errorMessage    the error message to display if it fails.
     * @param doIfSuccess     code to execute if service is successful.
     * @param doIfFailure     code to execute if service is in failure.
     * @param <T>             the class of the request.
     * @return the event handler.
     */
    default <T> EventHandler<ActionEvent> callServiceAsEvent(IService<T> service, Supplier<T> requestSupplier, String errorMessage, Runnable doIfSuccess, Runnable doIfFailure) {
        return event -> callService(service, requestSupplier, errorMessage, doIfSuccess, doIfFailure);
    }

    /**
     * Call a back end service.
     *
     * @param service         the service to call.
     * @param requestSupplier the supplier that will create the request.
     * @param errorMessage    the error message to display if it fails.
     * @param doIfSuccess     code to execute if service is successful.
     * @param doIfFailure     code to execute if service is in failure.
     * @param <T>             the class of the request.
     */
    default <T> void callService(IService<T> service, Supplier<T> requestSupplier, String errorMessage, Runnable doIfSuccess, Runnable doIfFailure) {
            Request<T> request = new Request<>();
            getAuthentHolder().fillAuthentInfo(request);
            getGameConfig().fillGameInfo(request);
            getGameConfig().fillChatInfo(request);
            request.setRequest(requestSupplier.get());
            Long idGame = getGameConfig().getIdGame();
            try {
                DiffResponse response = service.run(request);

                DiffResponseEvent diff = new DiffResponseEvent(response, idGame);
                if (doIfSuccess != null) {
                    doIfSuccess.run();
                }
                processDiffEvent(diff);
            } catch (Exception e) {
                LOGGER.error(errorMessage + e.getMessage());

                if (doIfFailure != null) {
                    doIfFailure.run();
                }
                InteractiveMap map = null;
                if (this instanceof INotJavaFxServiceCaller) {
                    map = ((INotJavaFxServiceCaller) this).getComponent();
                }
                processExceptionEvent(new ExceptionEvent(e, map));
            }
    }

    /**
     * Create an EventHandler that will notify the client.
     *
     * @param type       of notification.
     * @param attributes of the notification.
     * @return the event handler.
     */
    default EventHandler<ActionEvent> notifyClientAsEvent(DiffTypeObjectEnum type, DiffAttributes... attributes) {
        return event -> notifyClient(type, attributes);
    }

    /**
     * Notify the client to update the UI from one component to another.
     *
     * @param type       of notification.
     * @param attributes of the notification.
     */
    default void notifyClient(DiffTypeObjectEnum type, DiffAttributes... attributes) {
        DiffResponse fakeResponse = new DiffResponse();
        Diff diff = new Diff();
        diff.setVersionGame(getGameConfig().getVersionGame() + 1);
        diff.setType(DiffTypeEnum.NOTIFY);
        diff.setTypeObject(type);
        diff.getAttributes().addAll(Arrays.asList(attributes));
        fakeResponse.getDiffs().add(diff);
        processDiffEvent(new DiffResponseEvent(fakeResponse, getGameConfig().getIdGame()));
    }

    /**
     * Interface that matches all back end services.
     *
     * @param <V> the type of the request.
     */
    interface IService<V> {
        DiffResponse run(Request<V> request) throws TechnicalException, FunctionalException;
    }
}
