package com.mkl.eu.client.service.service.eco;

/**
 * Sub request for loadEcoSheets service.
 *
 * @author MKL.
 */
public class LoadEcoSheetsRequest {
    /** Id of the game. */
    private Long idGame;
    /** Id of the country whose sheets should be returned. Can be <code>null</code> */
    private Long idCountry;
    /** Turn of the sheets to return. */
    private Integer turn;

    /**
     * Constructor for jaxb.
     */
    public LoadEcoSheetsRequest() {
    }

    /**
     * Constructor.
     *
     * @param idGame    the idGame to set.
     * @param idCountry the idCountry to set.
     * @param turn      the turn to set.
     */
    public LoadEcoSheetsRequest(Long idGame, Long idCountry, Integer turn) {
        this.idGame = idGame;
        this.idCountry = idCountry;
        this.turn = turn;
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

    /** @return the turn. */
    public Integer getTurn() {
        return turn;
    }

    /** @param turn the turn to set. */
    public void setTurn(Integer turn) {
        this.turn = turn;
    }
}
