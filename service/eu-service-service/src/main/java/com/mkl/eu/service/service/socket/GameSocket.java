package com.mkl.eu.service.service.socket;

import com.mkl.eu.client.common.vo.SocketInfo;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

/**
 * Socket for a client server side.
 *
 * @author MKL.
 */
public class GameSocket implements Runnable {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SocketHandler.class);
    /** Socket to communicate with client. */
    private Socket socket;
    /** Handler to be referenced to after the init. */
    private SocketHandler handler;
    /** Information about this socket (game id, login, ...). */
    private SocketInfo info;
    /** Terminate this process. */
    private boolean terminate;

    /**
     * Constructor.
     *
     * @param socket  the socket to set.
     * @param handler the handler to set.
     */
    public GameSocket(Socket socket, SocketHandler handler) {
        this.socket = socket;
        this.handler = handler;

        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            info = (SocketInfo) in.readObject();

            LOGGER.info("New client on game " + info.getIdGame());

            handler.addActiveClient(this, info.getIdGame());
        } catch (Exception e) {
            LOGGER.info("Error in client socket before init.", e);
        }
    }

    /** {@inheritDoc */
    @Override
    public void run() {
        while (!terminate && !socket.isClosed()) {
            // do nothing
        }

        LOGGER.info("Closing client on game " + info.getIdGame());
        handler.removeActiveClient(this, info.getIdGame());
    }

    /**
     * Push a diff to a client.
     *
     * @param diff        to push.
     * @param idCountries List of countries that will receive this diff.
     */
    public void push(DiffResponse diff, List<Long> idCountries) {
        if (idCountries == null || idCountries.isEmpty() || idCountries.contains(info.getIdCountry())) {
            try {
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject(diff);
            } catch (SocketException e) {
                terminate = true;
            } catch (Exception e) {
                LOGGER.error("Error when sending diff to client.", e);
            }
        }
    }
}
