package com.sparklounge.client.models;

import java.io.Serializable;

/**
 * Created by Chuang on 9/1/2015.
 */
public class UserInfo implements Serializable {

    private String userId;
    private String caption;
    private String profilePic;
    private String gcmRegId;

    public UserInfo(String userId, String caption, String profilePic, String gcmRegId) {
        this.userId = userId;
        this.caption = caption;
        this.profilePic = profilePic;
        this.gcmRegId = gcmRegId;
    }

    public String getUserId() {
        return this.userId;
    }

    // TODO: remove
    public String getCaption() {
        return this.caption;
    }

    public String getProfilePic() {
        return this.profilePic;
    }

    public String getGcmRegId() {
        return this.gcmRegId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

}
