package com.sparklounge.client;

import java.util.List;

public class GetItemsResp {
    private List<String> queue;

    public GetItemsResp(List<String> queue){
        this.queue = queue;
    }

    public List<String> getQueue(){
        return queue;
    }

    public void setQueue(List<String> queue){
        this.queue = queue;
    }


}
