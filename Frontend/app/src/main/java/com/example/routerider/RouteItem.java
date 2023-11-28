package com.example.routerider;

import java.util.List;

// NO CHATGPT
public class RouteItem {
    private List<TransitItem> transitItems;
    private List<String> steps;
    private String destination;


    public RouteItem(List<TransitItem> transitItems, List<String> steps, String destination){
        this.transitItems = transitItems;
        this.steps = steps;
        this.destination = destination;
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

    public String getDestination() { return destination; }
}

