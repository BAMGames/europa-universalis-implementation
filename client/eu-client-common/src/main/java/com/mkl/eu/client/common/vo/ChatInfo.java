package com.mkl.eu.client.common.vo;

/**
 * Information on chat.
 *
 * @author MKL.
 */
public class ChatInfo {
    /** Maximum of the ids for the global messages. */
    private Long maxIdGlobalMessage;
    /** Maximum of the ids for the non global messages. */
    private Long maxIdMessage;
    /** Id of the country concerned by this messages or <code>null</code> for only global messages. */
    private Long idCountry;

    /**
     * Constructor.
     */
    public ChatInfo() {

    }

    /**
     * Constructor.
     *
     * @param maxIdGlobalMessage the maxIdGlobalMessage to set.
     * @param maxIdMessage       the maxIdMessage to set.
     * @param idCountry          the idCountry to set.
     */
    public ChatInfo(Long maxIdGlobalMessage, Long maxIdMessage, Long idCountry) {
        this.maxIdGlobalMessage = maxIdGlobalMessage;
        this.maxIdMessage = maxIdMessage;
        this.idCountry = idCountry;
    }

    /** @return the maxIdGlobalMessage. */
    public Long getMaxIdGlobalMessage() {
        return maxIdGlobalMessage;
    }

    /** @param maxIdGlobalMessage the maxIdGlobalMessage to set. */
    public void setMaxIdGlobalMessage(Long maxIdGlobalMessage) {
        this.maxIdGlobalMessage = maxIdGlobalMessage;
    }

    /** @return the maxIdMessage. */
    public Long getMaxIdMessage() {
        return maxIdMessage;
    }

    /** @param maxIdMessage the maxIdMessage to set. */
    public void setMaxIdMessage(Long maxIdMessage) {
        this.maxIdMessage = maxIdMessage;
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
