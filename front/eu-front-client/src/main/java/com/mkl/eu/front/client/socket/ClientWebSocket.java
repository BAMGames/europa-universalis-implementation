package com.mkl.eu.front.client.socket;

import com.mkl.eu.client.service.service.IGameService;
import com.mkl.eu.client.service.socket.DiffResponseEncoder;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.front.client.event.AbstractDiffResponseListenerContainer;
import com.mkl.eu.front.client.event.DiffResponseEvent;
import com.mkl.eu.front.client.main.GameConfiguration;
import com.mkl.eu.front.client.main.GlobalConfiguration;
import javafx.application.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.websocket.*;
import java.net.URI;

/**
 * Socket for the server client side.
 *
 * @author MKL.
 */
@Component
@Scope(value = "prototype")
@ClientEndpoint
public class ClientWebSocket extends AbstractDiffResponseListenerContainer {
    /** Delays between two unsuccessful tries to reconnect to the socket. */
    private final static long[] delays = new long[]{5000l, 10000l, 30000l, 60000l};
    /** Socket to communicate with server. */
    private Session userSession = null;
    /** Terminate this process. */
    private boolean terminate;
    /** Flag saying the client is currently connected to the server. */
    private boolean connected;
    /** Game service to update current game after a disconnection. */
    @Autowired
    private IGameService gameService;

    /**
     * Constructor.
     *
     * @param gameConfig the gameConfig to set.
     */
    public ClientWebSocket(GameConfiguration gameConfig) {
        super(gameConfig);
    }

    /**
     * Initialize the socket.
     */
    @PostConstruct
    public void init() {
        StringBuilder uri = new StringBuilder(GlobalConfiguration.getSocketHost());
        if (!uri.toString().startsWith("ws://")) {
            uri.insert(0, "ws://");
        }
        if (!uri.toString().endsWith("/")) {
            uri.append("/");
        }
        uri.append("diff/")
                .append(gameConfig.getIdGame())
                .append("/")
                .append(gameConfig.getIdCountry());
        try {
            URI endpointURI = new URI(uri.toString());
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
            LOGGER.info("Connected to server.");
            connected = true;
        } catch (Exception e) {
            LOGGER.error("Error when initializing with server " + uri.toString() + " for : " + e.getMessage());
        }
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        this.userSession = userSession;
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a
     * client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        try {
            DiffResponse response = DiffResponseEncoder.decode(message);

            LOGGER.info("Receiving a diff for game " + gameConfig.getIdGame());
            DiffResponseEvent event = new DiffResponseEvent(response, gameConfig.getIdGame());
            Platform.runLater(() -> processDiffEvent(event));
        } catch (Exception e) {
            if (!terminate) {
                LOGGER.error("Error when communicating with server : " + e.getMessage());
            }
        }
    }

    @OnClose
    public void onClose() {
        if (!terminate) {
            LOGGER.error("Server disconnected.");
            connected = false;
            tryToReconnect();
        }
    }

    /**
     * If the connection to the server was lost, then we try to reconnect it.
     */
    private void tryToReconnect() {
        int nbRetry = 0;
        while (!connected) {
            long delay = nbRetry >= delays.length ? delays[delays.length - 1] : delays[nbRetry];
            LOGGER.debug("Trying to reconnect for the " + (nbRetry + 1) + " times in " + delay + " ms.");
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                LOGGER.error("Error during sleep ?");
            }
            init();
            nbRetry++;
        }
        callService(gameService::updateGame, () -> null, "Error when updating the game.");
    }

    /** @param terminate the terminate to set. */
    public void setTerminate(boolean terminate) {
        this.terminate = terminate;

        try {
            userSession.close();
        } catch (Exception e) {
            LOGGER.error("Error when closing the socket : " + e.getMessage());
        }
    }
}
