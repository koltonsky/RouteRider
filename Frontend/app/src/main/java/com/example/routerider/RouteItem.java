package com.example.routerider;

import com.google.gson.Gson;

import java.util.List;

// NO CHATGPT
public class RouteItem {
    private List<TransitItem> transitItems;
    private List<String> steps;


    public RouteItem(List<TransitItem> transitItems, List<String> steps){
        this.transitItems = transitItems;
        this.steps = steps;
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

