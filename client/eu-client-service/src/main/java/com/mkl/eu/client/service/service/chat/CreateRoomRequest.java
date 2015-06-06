package com.mkl.eu.client.service.service.chat;

/**
 * Request for createRoom service.
 *
 * @author MKL.
 */
public class CreateRoomRequest {
    /** Id of the game. */
    private Long idGame;
    /** Name of the room to create. */
    private String name;

    /** @return the idGame. */
    public Long getIdGame() {
        return idGame;
    }

    /** @param idGame the idGame to set. */
    public void setIdGame(Long idGame) {
        this.idGame = idGame;
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
