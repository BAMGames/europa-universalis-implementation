package com.mkl.eu.client.service.service.board;

/**
 * Request for moveLeader service.
 *
 * @author MKL.
 */
public class MoveLeaderRequest extends MoveCounterRequest {
    /** Province to move the leader in case of idStack is <code>null</code>. */
    private String province;

    /**
     * Constructor for jaxb.
     */
    public MoveLeaderRequest() {
    }

    /**
     * Constructor.
     *
     * @param idCounter the idCounter to set.
     * @param idStack   the idStack to set.
     */
    public MoveLeaderRequest(Long idCounter, Long idStack) {
        super(idCounter, idStack);
    }

    /**
     * Constructor.
     *
     * @param idCounter the idCounter to set.
     * @param province  the province to set.
     */
    public MoveLeaderRequest(Long idCounter, String province) {
        super(idCounter, null);
        this.province = province;
    }

    /** @return the province. */
    public String getProvince() {
        return province;
    }

    /** @param province the province to set. */
    public void setProvince(String province) {
        this.province = province;
    }
}
