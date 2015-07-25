package com.sparklounge.client;

/**
 * Created by James on 2015-07-17.
 */
public class AwsCredentials {

    private String accessKeyId;
    private String secretAccessKey;
    private String sessionToken;
    private Long expiration;

    public AwsCredentials(){}
    public AwsCredentials(String accessKeyId, String secretAccessKey, String sessionToken, Long expiration){
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.sessionToken = sessionToken;
        this.expiration = expiration;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public Long getExpiration() {
        return expiration;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }


}
