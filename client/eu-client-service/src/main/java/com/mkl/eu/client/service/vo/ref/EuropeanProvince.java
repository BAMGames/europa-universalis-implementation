package com.mkl.eu.client.service.vo.ref;

/**
 * European province. A tile on the european board that can contains counters.
 * TODO check if still used later.
 *
 * @author MKL.
 */
public class EuropeanProvince extends AbstractProvince {
    /** Base income of the province. */
    private Integer income;
    /** Name of the country owner of the province if no owner counter is present. */
    private String defaultOwner;
    /** Flag saying that the province contains a port. */
    private boolean port;
    /** Flag saying that the province port can be praesidiable. */
    private boolean praesidiable;

    /** @return the income. */
    public Integer getIncome() {
        return income;
    }

    /** @param income the income to set. */
    public void setIncome(Integer income) {
        this.income = income;
    }

    /** @return the defaultOwner. */
    public String getDefaultOwner() {
        return defaultOwner;
    }

    /** @param defaultOwner the defaultOwner to set. */
    public void setDefaultOwner(String defaultOwner) {
        this.defaultOwner = defaultOwner;
    }

    /** @return the port. */
    public boolean isPort() {
        return port;
    }

    /** @param port the port to set. */
    public void setPort(boolean port) {
        this.port = port;
    }

    /** @return the praesidiable. */
    public boolean isPraesidiable() {
        return praesidiable;
    }

    /** @param praesidiable the praesidiable to set. */
    public void setPraesidiable(boolean praesidiable) {
        this.praesidiable = praesidiable;
    }
}
