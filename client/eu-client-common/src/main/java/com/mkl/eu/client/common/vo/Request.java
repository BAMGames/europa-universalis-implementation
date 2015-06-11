package com.mkl.eu.client.common.vo;

/**
 * Request Wrapper with authentication, game and chat info.
 *
 * @author MKL.
 */
public class Request<T> extends SimpleRequest<T> {
    /** Game info. */
    private GameInfo game;
    /** Chat info. */
    private ChatInfo chat;

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
