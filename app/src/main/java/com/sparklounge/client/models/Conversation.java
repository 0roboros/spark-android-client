package com.sparklounge.client.models;

/**
 * Created by Chuang on 9/8/2015.
 */
public class Conversation {

    private UserInfo userInfo;
    private String messageTime;
    private String message;

    public Conversation(UserInfo userInfo, String message, String messageTime) {
        this.userInfo = userInfo;
        this.message = message;
        this.messageTime = messageTime;
    }


    public String getMessageTime() {
        return messageTime;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public String getMessage() {
        return message;
    }
}
