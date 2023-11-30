package com.example.routerider;

import android.os.AsyncTask;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;

// NO CHATGPT
public class CreateEventTask extends AsyncTask<Void, Void, Event> {
    private Calendar service;
    private final String eventName;
    private final String eventAddress;
    private final String eventDate;
    private final String eventStartTime;
    private final String eventEndTime;

    public CreateEventTask(Calendar service, String eventName, String eventAddress, String eventDate, String eventStartTime, String eventEndTime) {
        this.service = service;
        this.eventName = eventName;
        this.eventAddress = eventAddress;
        this.eventDate = eventDate;
        this.eventStartTime = eventStartTime;
        this.eventEndTime = eventEndTime;
    }

    @Override
    protected Event doInBackground(Void... voids) {
        try {
            Event event = new Event()
                    .setSummary(eventName)
                    .setLocation(eventAddress);

            String timeZone = "America/Denver"; // UTC-7 (Mountain Daylight Time)

            DateTime startDateTime = new DateTime(eventDate + "T" + eventStartTime + ":00.000-08:00");
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone(timeZone);
            event.setStart(start);

            // Set the end time
            DateTime endDateTime = new DateTime(eventDate + "T" + eventEndTime + ":00.000-08:00");
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone(timeZone);
            event.setEnd(end);

            String calendarId = "primary";
            event = service.events().insert(calendarId, event).execute();
            return event;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
