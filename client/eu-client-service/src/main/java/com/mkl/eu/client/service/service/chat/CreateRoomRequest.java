package com.mkl.eu.client.service.service.chat;

/**
 * Request for createRoom service.
 *
 * @author MKL.
 */
public class CreateRoomRequest {
    /** Name of the room to create. */
    private String name;
    /** Id of the country creating the room. */
    private Long idCountry;

    /**
     * Constructor for jaxb.
     */
    public CreateRoomRequest() {

    }

    /**
     * Constructor.
     *
     * @param name      the name to set.
     * @param idCountry the name to set.
     */
    public CreateRoomRequest(String name, Long idCountry) {
        this.name = name;
        this.idCountry = idCountry;
    }

    /** @return the name. */
    public String getName() {
        return name;
    }

    /** @param name the name to set. */
    public void setName(String name) {
        this.name = name;
    }
}
