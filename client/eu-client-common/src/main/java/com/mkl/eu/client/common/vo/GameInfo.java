package com.mkl.eu.client.common.vo;

/**
 * Information on game.
 *
 * @author MKL.
 */
public class GameInfo {
    /** Id of the game. */
    private Long idGame;
    /** Version of the game. */
    private Long versionGame;
    /** Id of the country making a request. */
    private Long idCountry;

    /** @return the idGame. */
    public Long getIdGame() {
        return idGame;
    }

    /** @param idGame the idGame to set. */
    public void setIdGame(Long idGame) {
        this.idGame = idGame;
    }

    /** @return the versionGame. */
    public Long getVersionGame() {
        return versionGame;
    }

    /** @param versionGame the versionGame to set. */
    public void setVersionGame(Long versionGame) {
        this.versionGame = versionGame;
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
