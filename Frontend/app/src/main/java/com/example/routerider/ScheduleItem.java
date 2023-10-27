package com.example.routerider;

public class ScheduleItem {
    private String title;
    private String location;
    private String startTime;
    private String endTime;

    public ScheduleItem(String eventSummary, String eventLocation, String startTimeString, String endTimeString) {
        title = eventSummary;
        location = eventLocation;
        startTime = startTimeString;
        endTime = endTimeString;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
}
