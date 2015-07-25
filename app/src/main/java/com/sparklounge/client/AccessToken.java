package com.sparklounge.client;

/**
 * Created by James on 2015-06-26.
 */
public class AccessToken {

    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String scope;

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
