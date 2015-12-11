package com.mkl.eu.client.service.vo.diff;

import com.mkl.eu.client.service.vo.chat.MessageDiff;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Default response of services that involve modifications.
 *
 * @author MKL.
 */
public class DiffResponse implements Serializable {
    /** New version of the game. */
    private Long versionGame;
    /** Diff involved in the modification. */
    private List<Diff> diffs = new ArrayList<>();
    /** Messages received since last time. */
    private List<MessageDiff> messages = new ArrayList<>();

    /** @return the versionGame. */
    public Long getVersionGame() {
        return versionGame;
    }

    /** @param versionGame the versionGame to set. */
    public void setVersionGame(Long versionGame) {
        this.versionGame = versionGame;
    }

    /** @return the diffs. */
    public List<Diff> getDiffs() {
        return diffs;
    }

    /** @param diffs the diffs to set. */
    public void setDiffs(List<Diff> diffs) {
        this.diffs = diffs;
    }

    /** @return the messages. */
    public List<MessageDiff> getMessages() {
        return messages;
    }

    /** @param messages the messages to set. */
    public void setMessages(List<MessageDiff> messages) {
        this.messages = messages;
    }
}
