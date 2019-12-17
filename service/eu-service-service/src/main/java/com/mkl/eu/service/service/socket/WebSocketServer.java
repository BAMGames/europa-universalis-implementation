package com.mkl.eu.service.service.socket;

import com.mkl.eu.client.common.vo.SocketInfo;
import com.mkl.eu.client.service.socket.DiffResponseEncoder;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;

/**
 * Handles the socket to the clients mapping them by game.
 *
 * @author MKL.
 */
@ServerEndpoint(value = "/diff/{idGame}/{idCountry}",
        encoders = DiffResponseEncoder.class)
public class WebSocketServer {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketServer.class);
    /** Session of the client. */
    private Session session;
    /** Client connected information. */
    private SocketInfo info;
    /** Map of active clients by id game. */
    private static Map<Long, List<WebSocketServer>> activeClients = new HashMap<>();

    /** {@inheritDoc} */
    @OnOpen
    public void onOpen(Session session, @PathParam("idGame") Long idGame, @PathParam("idCountry") Long idCountry) throws IOException {
        this.session = session;
        info = new SocketInfo();
        // pathParams seem not to be thread safe. When server restarts, causing a mass reconnection
        // of all clients, then the pathParams of the LocalEndpoint of the session (the one visible
        // in the method parameters here) are wrong.
        idGame = Long.parseLong(session.getPathParameters().get("idGame"));
        idCountry = Long.parseLong(session.getPathParameters().get("idCountry"));
        info.setIdGame(idGame);
        info.setIdCountry(idCountry);

        addClient(this);

        LOGGER.info("New client on game " + info.getIdGame() + " for player " + info.getIdCountry());
    }

    /** {@inheritDoc} */
    @OnClose
    public void onClose() {
        removeClient(this);

        LOGGER.info("Closing client on game " + info.getIdGame() + " for player " + info.getIdCountry());
    }

    /** {@inheritDoc} */
    @OnError
    public void onError(Throwable throwable) {
        LOGGER.info("Error from client on game " + info.getIdGame() + " for player " + info.getIdCountry() + " : " + throwable.getMessage());
    }

    /**
     * Push a response to a client.
     *
     * @param response    to push.
     * @param idCountries List of countries that will receive this response.
     */
    public void push(DiffResponse response, List<Long> idCountries) {
        response.getDiffs().removeIf(diff -> diff.getIdCountry() != null && !Objects.equals(diff.getIdCountry(), info.getIdCountry()));
        if (idCountries == null || idCountries.isEmpty() || idCountries.contains(info.getIdCountry())) {
            try {
                session.getBasicRemote().sendObject(response);
            } catch (Exception e) {
                LOGGER.error("Error when sending response to client.", e);
            }
        }
    }

    /**
     * Add a client to the list of currently connected client.
     *
     * @param client the new active client.
     */
    private static synchronized void addClient(WebSocketServer client) {
        if (!activeClients.containsKey(client.info.getIdGame())) {
            activeClients.put(client.info.getIdGame(), new ArrayList<>());
        }

        activeClients.get(client.info.getIdGame()).add(client);
    }

    /**
     * Remove a client from the list of currently connected client.
     *
     * @param client the client that is not active anymore.
     */
    private static synchronized void removeClient(WebSocketServer client) {
        if (activeClients.containsKey(client.info.getIdGame())) {
            activeClients.get(client.info.getIdGame()).remove(client);
        }
    }

    /**
     * Push a diff to all clients listening to this game.
     *
     * @param idGame      id of the game.
     * @param diff        to push.
     * @param idCountries List of countries that will receive this diff.
     */
    public static synchronized void push(Long idGame, DiffResponse diff, List<Long> idCountries) {
        if (activeClients.containsKey(idGame)) {
            activeClients.get(idGame).forEach(webServer -> webServer.push(diff, idCountries));
        }
    }
}
