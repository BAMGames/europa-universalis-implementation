package com.mkl.eu.client.common.vo;

/**
 * Request Wrapper with authentication, game and chat info.
 *
 * @author MKL.
 */
public class Request<T> extends SimpleRequest<T> {
    /** Id of the country making a request. */
    private Long idCountry;
    /** Game info. */
    private GameInfo game;
    /** Chat info. */
    private ChatInfo chat;

    /** @return the idCountry. */
    public Long getIdCountry() {
        return idCountry;
    }

    /** @param idCountry the idCountry to set. */
    public void setIdCountry(Long idCountry) {
        this.idCountry = idCountry;
    }

    /** @return the game. */
    public GameInfo getGame() {
        return game;
    }

    /** @param game the game to set. */
    public void setGame(GameInfo game) {
        this.game = game;
    }

    /** @return the chat. */
    public ChatInfo getChat() {
        return chat;
    }

    /** @param chat the chat to set. */
    public void setChat(ChatInfo chat) {
        this.chat = chat;
    }
}
