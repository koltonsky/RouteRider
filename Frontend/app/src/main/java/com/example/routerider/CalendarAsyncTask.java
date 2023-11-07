package com.example.routerider;

import android.os.AsyncTask;

import com.example.routerider.fragments.ScheduleFragment;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// YES CHATGPT
public class CalendarAsyncTask extends AsyncTask<Calendar, Void, Void> {
    private GoogleSignInAccount account;
    private String calendarId;


    public CalendarAsyncTask(GoogleSignInAccount account) {
        this.account = account;
    }

    @Override
    protected Void doInBackground(Calendar... calendars) {
        Calendar service = calendars[0];
        ScheduleFragment.eventList = new ArrayList<>();


        try {
            CalendarList calendarList = service.calendarList().list().execute();
            List<CalendarListEntry> items = calendarList.getItems();

            if (items.isEmpty()) {
                System.out.println("No calendars found.");
            } else {
                System.out.println("Calendars:");

                for (CalendarListEntry calendarEntry : items) {
                    calendarId = calendarEntry.getId();

                    // List events for the calendar
                    long sevenDaysInMillis = 30L * 24 * 60 * 60 * 1000;
                    Events events = service.events().list(calendarId)
                            .setSingleEvents(true)
                            .setTimeMin(new DateTime(System.currentTimeMillis()))
                            .setTimeMax(new DateTime(System.currentTimeMillis() + sevenDaysInMillis)) // Set the time range as needed
                            .execute();

                    List<Event> itemsEvents = events.getItems();
                    parseEvents(itemsEvents);
                }
            }

            Map<String, Object> test = new HashMap<>();
            test.put("email", account.getEmail());
            test.put("events", ScheduleFragment.eventList);
            String jsonSchedule = new Gson().toJson(test);
            calendarApiCall(jsonSchedule);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void parseEvents(List<Event> itemsEvents) {
        Comparator<Event> eventComparator = (event1, event2) -> {
            // Assuming the start time is in ISO8601 format
            String startTime1 = String.valueOf(event1.getStart().getDateTime());
            String startTime2 = String.valueOf(event2.getStart().getDateTime());

            if (startTime1 == null || startTime2 == null) {
                return 0; // Handle null values if needed
            }

            // Compare events by start time (in ISO8601 format)
            return startTime1.compareTo(startTime2);
        };

        // Sort the list of events using the custom comparator
        itemsEvents.sort(eventComparator);
        for (Event event : itemsEvents) {
            String eventId = event.getId();
            String eventSummary = event.getSummary();
            String eventLocation = (event.getLocation() != null) ? event.getLocation() : "N/A";
            DateTime startTime = event.getStart().getDateTime();
            DateTime endTime = event.getEnd().getDateTime();


            // Format the start and end times as strings (you can customize the format)
            String startTimeString = (startTime != null) ? startTime.toString() : "N/A";
            String endTimeString = (endTime != null) ? endTime.toString() : "N/A";
            if (eventLocation.contains("Room")) {
                eventLocation = "UBC " + eventLocation;
            }

            ScheduleItem newEvent = new ScheduleItem(
                    eventSummary,
                    eventLocation,
                    startTimeString,
                    endTimeString,
                    eventId,
                    calendarId);

            if (!startTimeString.equals("N/A")) {
                System.out.println(eventId);
                ScheduleFragment.eventList.add(newEvent);
                System.out.println("adding event");
            }
        }
    }

    private void calendarApiCall(String jsonSchedule) {
        APICaller apiCall = new APICaller();
        apiCall.APICall("api/schedulelist/" + account.getEmail(), "", APICaller.HttpMethod.GET, new APICaller.ApiCallback() {
            @Override
            public void onResponse(String responseBody) {
                System.out.println("BODY: " + responseBody);
                if (responseBody.equals("\"Schedule found\"")) {
                    System.out.println("TRUE");
                    apiCall.APICall("api/schedulelist/" + account.getEmail(), jsonSchedule, APICaller.HttpMethod.PUT, new APICaller.ApiCallback() {
                        @Override
                        public void onResponse(String responseBodyUpdate) {
                            System.out.println("Update schedule: " + responseBodyUpdate);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            System.out.println("Error: " + errorMessage);
                        }
                    });
                } else {
                    System.out.println("TEST");
                    apiCall.APICall("api/schedulelist/" + account.getEmail(), jsonSchedule, APICaller.HttpMethod.POST, new APICaller.ApiCallback() {
                        @Override
                        public void onResponse(String responseBodyPost) {
                            System.out.println("Created schedule: " + responseBodyPost);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            System.out.println("Error: " + errorMessage);
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                System.out.println("Error " + errorMessage);
                System.out.println(errorMessage.split(",")[0]);
                if ((errorMessage.split(",")[0]).equals("Error: 404")) {
                    System.out.println("schedule not found, creating schedule using Google Calendar data");
                    apiCall.APICall("api/schedulelist/", jsonSchedule, APICaller.HttpMethod.POST, new APICaller.ApiCallback() {
                        @Override
                        public void onResponse(String responseBodyPost) {
                            System.out.println("Created schedule: " + responseBodyPost);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            System.out.println("Error: " + errorMessage);
                        }
                    });
                }
            }
        });
    }


    // NO CHATGPT
    @Override
    protected void onPostExecute(Void result) {
        ScheduleFragment.displayGoogleSchedule();
    }
}
