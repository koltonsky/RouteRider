package com.example.routerider;

public class TimeGapRecommendation {
    private String type;
    private String address;
    private String name;

    public TimeGapRecommendation(String type, String name, String address) {
        this.type = type;
        this.address = address;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }
}
