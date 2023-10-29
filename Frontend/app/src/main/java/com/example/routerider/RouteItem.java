package com.example.routerider;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RouteItem {
    private List<TransitItem> transitItems;
    private List<String> steps;
    private String distance;
    private String duration;


    public RouteItem(List<TransitItem> transitItems, List<String> steps, String distance, String duration){
        this.transitItems = transitItems;
        this.steps = steps;
        this.distance = distance;
        this.duration = duration;
    }

    public RouteItem(String json){
        new Gson().fromJson(json, RouteItem.class);
    }

    public String getLeaveBy() {
        return transitItems.get(0).getTime();
    }

    public List<TransitItem> getTransitItems() {
        return transitItems;
    }

    public List<String> getSteps() {
        return steps;
    }
}

