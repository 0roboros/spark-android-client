package com.sparklounge.client;

import org.springframework.http.HttpAuthentication;

/**
 * Created by James on 2015-06-27.
 */
public class HttpCustomAuthentication extends HttpAuthentication {

    private String headerValue;
    public HttpCustomAuthentication(String headerValue) {
        this.headerValue = headerValue;
    }

    /**
     * @return the value for the 'Authorization' HTTP header.
     */
    public String getHeaderValue() {
        return headerValue;
    }

    @Override
    public String toString() {
        String s = null;
        try {
            s = String.format("Authorization: %s", getHeaderValue());
        } catch (RuntimeException re) {
            return null;
        }
        return s;
    }
}
