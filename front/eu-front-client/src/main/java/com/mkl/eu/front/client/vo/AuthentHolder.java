package com.mkl.eu.front.client.vo;

import com.mkl.eu.client.common.vo.AuthentInfo;
import com.mkl.eu.client.common.vo.SimpleRequest;
import org.springframework.stereotype.Component;

/**
 * Description of file.
 *
 * @author MKL.
 */
@Component
public class AuthentHolder {
    /** Username of the logged client. */
    private String username = "Sato";
    /** Password of the logged client. */
    private String password;

    /** @return the username. */
    public String getUsername() {
        return username;
    }

    /** @param username the username to set. */
    public void setUsername(String username) {
        this.username = username;
    }

    /** @return the password. */
    public String getPassword() {
        return password;
    }

    /** @param password the password to set. */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Fill the authent info of a request.
     *
     * @param <T> type of the request.
     */
    public <T> void fillAuthentInfo(SimpleRequest<T> request) {
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername(username);
        request.getAuthent().setPassword(password);
    }
}
