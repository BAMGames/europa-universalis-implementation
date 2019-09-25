package com.mkl.eu.service.service.socket;

import com.mkl.eu.client.service.vo.diff.DiffResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles the socket to the clients mapping them by game.
 *
 * @author MKL.
 */
@Component
public class SocketHandler implements ApplicationListener<ContextRefreshedEvent> {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SocketHandler.class);
    /** Map of active clients by id game. */
    private Map<Long, List<GameSocket>> activeClients = new HashMap<>();
    /** Flag saying that we want to terminate the sockets. */
    private boolean terminate;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        /**
         * There is 2 application context created : a main one and one with cxf.
         * The one with cxf has the main one has parent.
         * Here, we want the socket server to be created after all web services have been created.
         * Hence, we wait for the applicationContext with cxf to be initialized to init the socket server.
         */
        if (event.getApplicationContext().getParent() != null) {
            init();
        }
    }

    /**
     * Initialize the server socket.
     */
    public void init() {
        try {
            ServerSocket server = new ServerSocket(2009);
            LOGGER.info("Socket handler launched server socket on " + server.getLocalPort());

            Thread t = new Thread(() -> {
                try {
                    while (!terminate) {
                        Socket socket = server.accept();
                        LOGGER.info("Connexion accepted.");

                        new Thread(new GameSocket(socket, this)).start();
                    }
                } catch (IOException e) {
                    LOGGER.error("Error when creating a socket from server.", e);
                }
            });
            t.start();
        } catch (IOException e) {
            LOGGER.error("Error when creating server socket.", e);
        }
    }

    /**
     * Add a client for the given game.
     *
     * @param client to add.
     * @param idGame id of the game.
     */
    public synchronized void addActiveClient(GameSocket client, Long idGame) {
        if (!activeClients.containsKey(idGame)) {
            activeClients.put(idGame, new ArrayList<>());
        }

        activeClients.get(idGame).add(client);
    }

    /**
     * Remove a client for the given game.
     *
     * @param client to remove.
     * @param idGame id of the game.
     */
    public synchronized void removeActiveClient(GameSocket client, Long idGame) {
        if (activeClients.containsKey(idGame)) {
            activeClients.get(idGame).remove(client);
        }
    }

    /**
     * Push a diff to all clients listening to this game.
     *
     * @param idGame      id of the game.
     * @param diff        to push.
     * @param idCountries List of countries that will receive this diff.
     */
    public synchronized void push(Long idGame, DiffResponse diff, List<Long> idCountries) {
        if (activeClients.containsKey(idGame)) {
            activeClients.get(idGame).forEach(gameSocket -> gameSocket.push(diff, idCountries));
        }
    }

    /** @param terminate the terminate to set. */
    public void setTerminate(boolean terminate) {
        this.terminate = terminate;
    }
}
