package com.mkl.eu.front.client.main;

import com.mkl.eu.client.common.vo.GameInfo;
import com.mkl.eu.client.common.vo.Request;

/**
 * Configuration of a game.
 *
 * @author MKL.
 */
public class GameConfiguration {
    /** Id of the game. */
    private Long idGame;
    /** Version of the game. */
    private Long versionGame;
    /** Id of the country loading the game. */
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

    /**
     * Fill the game info of a request.
     *
     * @param <T> type of the request.
     */
    public <T> void fillGameInfo(Request<T> request) {
        request.setGame(new GameInfo());
        request.getGame().setIdGame(idGame);
        request.getGame().setVersionGame(versionGame);
    }
}
