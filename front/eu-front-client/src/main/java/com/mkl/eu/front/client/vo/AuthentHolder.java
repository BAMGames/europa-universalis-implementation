package com.mkl.eu.front.client.vo;

import com.mkl.eu.client.common.vo.AuthentRequest;
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
     * Creates a request with username and password filled.
     *
     * @param <T> type of the request.
     * @return the filled request.
     */
    public <T> AuthentRequest<T> createRequest() {
        AuthentRequest<T> request = new AuthentRequest<>();

        request.setUsername(username);
        request.setPassword(password);

        return request;
    }
}
