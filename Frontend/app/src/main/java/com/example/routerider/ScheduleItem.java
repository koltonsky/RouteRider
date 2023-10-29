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
    private String id;
    private String calendarID;

    public ScheduleItem(String eventSummary, String eventLocation, String startTimeString, String endTimeString, String id, String calendarId) {
        this.eventName = eventSummary;
        this.address = eventLocation;
        this.startTime = startTimeString;
        this.endTime = endTimeString;
        this.id = id;
        this.calendarID = calendarId;
        this.geolocation.put("latitude", 0.0);
        this.geolocation.put("longitude", 0.0);
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

    public String getId() {
        return id;
    }

    public String getCalendarId() {
        return calendarID;
    }

    public Void updateId(String newId) {
        this.id = newId;
        return null;
    }
}
