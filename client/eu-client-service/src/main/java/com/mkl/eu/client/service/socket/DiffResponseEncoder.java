package com.mkl.eu.client.service.socket;

import com.google.gson.*;
import com.mkl.eu.client.service.vo.diff.DiffResponse;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Encoder for DiffResponse in order to communicate throw web sockets.
 *
 * @author MKL.
 */
public class DiffResponseEncoder implements Encoder.Text<DiffResponse> {
    /** Java 8 Date Time serializer. */
    public static final JsonSerializer<ZonedDateTime> ZDT_SERIALIZER = (zonedDateTime, type, jsonSerializationContext) -> new JsonPrimitive(DateTimeFormatter.ISO_DATE_TIME.format(zonedDateTime));
    /** Java 8 Date Time deserializer. */
    public static final JsonDeserializer<ZonedDateTime> ZDT_DESERIALIZER = (json, typeOfT, context) -> DateTimeFormatter.ISO_DATE_TIME.parse(json.getAsString(), ZonedDateTime::from);

    /** Configuration. */
    private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(ZonedDateTime.class, ZDT_DESERIALIZER)
            .registerTypeAdapter(ZonedDateTime.class, ZDT_SERIALIZER)
            .create();

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
