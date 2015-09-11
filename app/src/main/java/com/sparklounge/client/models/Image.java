package com.sparklounge.client.models;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by Chuang on 8/24/2015.
 */
public class Image  implements Serializable{

    private String uri;
    private Bitmap image;
    private String link;
    private String caption;

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getCaption() {
        // test caption
        //if (caption == null || caption == "") {
            return "This is a beautiful image of my poop";
        //}
        //return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getUri() {return this.uri;}
    public void setUri(String uri) {this.uri = uri;}

    public String getLink() {return this.link;}
    public void setLink(String link) {this.link = link;}
}
