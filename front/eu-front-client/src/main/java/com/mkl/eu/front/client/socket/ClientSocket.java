package com.mkl.eu.front.client.socket;

import com.mkl.eu.client.common.vo.SocketInfo;
import com.mkl.eu.client.service.service.IGameService;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.front.client.event.AbstractDiffListenerContainer;
import com.mkl.eu.front.client.event.DiffEvent;
import com.mkl.eu.front.client.main.GameConfiguration;
import javafx.application.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Socket for the server client side.
 *
 * @author MKL.
 */
@Component
@Scope(value = "prototype")
public class ClientSocket extends AbstractDiffListenerContainer implements Runnable {
    /** Delays between two unsuccessful tries to reconnect to the socket. */
    private final static long[] delays = new long[]{5000l, 10000l, 30000l, 60000l};
    /** Socket to communicate with server. */
    private Socket socket;
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
    public ClientSocket(GameConfiguration gameConfig) {
        super(gameConfig);
    }

    /**
     * Initialize the socket.
     */
    @PostConstruct
    public void init() {
        try {
            socket = new Socket("127.0.0.1", 2009);

            SocketInfo info = new SocketInfo();
            info.setUsername(authentHolder.getUsername());
            info.setPassword(authentHolder.getPassword());
            info.setIdGame(gameConfig.getIdGame());
            info.setIdCountry(gameConfig.getIdCountry());

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(info);
            connected = true;
        } catch (Exception e) {
            LOGGER.error("Error when initializing with server.", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            DiffResponse response;

            while ((response = (DiffResponse) in.readObject()) != null) {
                LOGGER.info("Receiving a diff for game " + gameConfig.getIdGame());
                DiffEvent event = new DiffEvent(response, gameConfig.getIdGame());
                Platform.runLater(() -> processDiffEvent(event));
            }
        } catch (SocketException e) {
            connected = false;
            tryToReconnect();
        } catch (Exception e) {
            if (!terminate) {
                LOGGER.error("Error when communicating with server.", e);
            }
        }
    }

    /**
     * If the connection to the server was lost, then we try to reconnect it.
     */
    private void tryToReconnect() {
        setTerminate(terminate);
        int nbRetry = 0;
        while (!connected) {
            long delay = nbRetry >= delays.length ? delays[delays.length - 1] : delays[nbRetry];
            LOGGER.debug("Trying to reconnect for the " + (nbRetry + 1) + " times in " + delay + " ms.");
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            init();
            nbRetry++;
        }
        callService(gameService::updateGame, () -> null, "Error when updating the game.");
        run();
    }

    /** @param terminate the terminate to set. */
    public void setTerminate(boolean terminate) {
        this.terminate = terminate;

        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject("TERMINATE");
            socket.close();
        } catch (IOException e) {
            LOGGER.error("Error when closing the socket.", e);
        }
    }
}
