package com.mkl.eu.client.service.service.chat;

/**
 * Sub request for loadRoom service.
 *
 * @author MKL.
 */
public class LoadRoomRequest {
    /** Id of the game. */
    private Long idGame;
    /** Id of the country requesting the room. */
    private Long idCountry;
    /** Id of the room to load. */
    private Long idRoom;

    /**
     * Constructor for jaxb.
     */
    public LoadRoomRequest() {
    }

    /**
     * Constructor.
     *
     * @param idGame    the idGame to set.
     * @param idCountry the idCountry to set.
     * @param idRoom    the idRoom to set.
     */
    public LoadRoomRequest(Long idGame, Long idCountry, Long idRoom) {
        this.idGame = idGame;
        this.idCountry = idCountry;
        this.idRoom = idRoom;
    }

    /** @return the idGame. */
    public Long getIdGame() {
        return idGame;
    }

    /** @param idGame the idGame to set. */
    public void setIdGame(Long idGame) {
        this.idGame = idGame;
    }

    /** @return the idCountry. */
    public Long getIdCountry() {
        return idCountry;
    }

    /** @param idCountry the idCountry to set. */
    public void setIdCountry(Long idCountry) {
        this.idCountry = idCountry;
    }

    /** @return the idRoom. */
    public Long getIdRoom() {
        return idRoom;
    }

    /** @param idRoom the idRoom to set. */
    public void setIdRoom(Long idRoom) {
        this.idRoom = idRoom;
    }
}
