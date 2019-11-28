package com.mkl.eu.front.client.event;

import com.mkl.eu.client.service.vo.chat.MessageDiff;
import com.mkl.eu.client.service.vo.diff.Diff;

import java.util.List;

/**
 * Listener of diff spread during the update of a game.
 *
 * @author MKL.
 */
public interface IDiffListener {
    /**
     * Update the client with the given diff.
     *
     * @param diff the diff.
     */
    void update(Diff diff);

    /**
     * Method called when all diffs of a DiffResponseEvent have been computed.
     */
    default void updateComplete() {
    }

    /**
     * Update the messages in the chat.
     *
     * @param messages new messages.
     */
    default void updateMessages(List<MessageDiff> messages) {

    }
}
