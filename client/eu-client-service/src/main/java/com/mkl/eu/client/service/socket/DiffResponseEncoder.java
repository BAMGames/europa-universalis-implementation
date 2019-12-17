package com.mkl.eu.client.service.socket;

import com.google.gson.Gson;
import com.mkl.eu.client.service.vo.diff.DiffResponse;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
 * Encoder for DiffResponse in order to communicate throw web sockets.
 *
 * @author MKL.
 */
public class DiffResponseEncoder implements Encoder.Text<DiffResponse> {
    /** Configuration. */
    private static Gson gson = new Gson();

    /** {@inheritDoc} */
    @Override
    public String encode(DiffResponse object) throws EncodeException {
        return gson.toJson(object);
    }

    /** {@inheritDoc} */
    @Override
    public void init(EndpointConfig config) {

    }

    /** {@inheritDoc} */
    @Override
    public void destroy() {

    }

    /**
     * Decoder for DiffResponse to use by the client.
     *
     * @param message the diffResponse in json format.
     * @return a DiffResponse object.
     */
    public static DiffResponse decode(String message) {
        return gson.fromJson(message, DiffResponse.class);
    }
}
