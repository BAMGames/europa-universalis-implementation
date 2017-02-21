package com.mkl.eu.client.service.service.game;

/**
 * Sub request for loadGame service.
 *
 * @author MKL.
 */
public class LoadGameRequest {
    /** Id of the game to load. */
    private Long idGame;
    /** Id of the country loading the game. */
    private Long idCountry;

    /**
     * Constructor for jaxb.
     */
    public LoadGameRequest() {
    }

    /**
     * Constructor.
     *
     * @param idGame the idGame to set.
     * @param idCountry the idCountry to set.
     */
    public LoadGameRequest(Long idGame, Long idCountry) {
        this.idGame = idGame;
        this.idCountry = idCountry;
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
}
