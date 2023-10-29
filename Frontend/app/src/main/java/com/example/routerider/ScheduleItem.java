package com.example.routerider;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ScheduleItem {
    private String eventName;
    private String address;
    private String startTime;
    private String endTime;
    private Map<String, Object> geolocation = new LinkedHashMap<>();

    public ScheduleItem(String eventSummary, String eventLocation, String startTimeString, String endTimeString) {
        eventName = eventSummary;
        address = eventLocation;
        startTime = startTimeString;
        endTime = endTimeString;
        geolocation.put("latitude", 0.0);
        geolocation.put("longitude", 0.0);
    }

    public String getTitle() {
        return eventName;
    }

    public String getLocation() {
        return address;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
}
