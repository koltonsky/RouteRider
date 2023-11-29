package com.example.routerider;

import java.util.List;

// NO CHATGPT
public class RouteItem {
    private String date;
    private List<TransitItem> transitItems;
    private List<String> steps;
    private String destination;
    private String friendEmail;


    public RouteItem(List<TransitItem> transitItems, List<String> steps, String destination){
        this.transitItems = transitItems;
        this.steps = steps;
        this.destination = destination;
        this.date = "";
        this.friendEmail = "";

    }
    public RouteItem(String date, List<TransitItem> transitItems, List<String> steps, String destination){
        this.transitItems = transitItems;
        this.steps = steps;
        this.destination = destination;
        this.date = date;
        this.friendEmail = "";
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

    public String getDate() { return date; }

    public String getFriendEmail() {
        return friendEmail;
    }

    public void setFriend(String email, List<TransitItem> transitItems, List<String> steps) {
        this.friendEmail = email;
        this.transitItems = transitItems;
        this.steps = steps;
    }

}

