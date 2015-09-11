package com.sparklounge.client.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chuang on 8/24/2015.
 */
public class ImageList {
    private List<String> queue;

    public ImageList(List<String> queue){
        this.queue = queue;
    }

    public List<String> getImageList(){
        return queue;
    }

    public void setImageList(List<String> queue){
        this.queue = queue;
    }


}
