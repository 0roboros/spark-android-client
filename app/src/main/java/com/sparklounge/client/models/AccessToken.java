package com.sparklounge.client.models;

/**
 * Created by James on 2015-06-26.
 */
public class AccessToken {

    private static AccessToken instance = null;

    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String scope;

    public AccessToken() {}

    public AccessToken(String accessToken, String tokenType, Long expiresIn, String scope) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.scope = scope;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getAccessToken(){
        return accessToken;
    }

    public String getTokenType(){
        return tokenType;
    }

    public Long getExpiresIn(){
        return expiresIn;
    }

    public String getScope(){
        return scope;
    }
}
