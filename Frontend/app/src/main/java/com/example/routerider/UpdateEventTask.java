package com.example.routerider;

import android.os.AsyncTask;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;

// NO CHATGPT
public class UpdateEventTask extends AsyncTask<Void, Void, Event> {
    private Calendar service;
    private ScheduleItem newEvent;

    public UpdateEventTask(Calendar service, ScheduleItem newEvent) {
        this.service = service;
        this.newEvent = newEvent;
    }

    @Override
    protected Event doInBackground(Void... voids) {
        try {
            Event event = service.events().get(newEvent.getCalendarId(), newEvent.getId()).execute();
            event.setSummary(newEvent.getTitle());

            DateTime updatedStartDateTime = new DateTime(newEvent.getStartTime());
            DateTime updatedEndDateTime = new DateTime(newEvent.getEndTime());

            EventDateTime updatedStart = new EventDateTime()
                    .setDateTime(updatedStartDateTime)
                    .setTimeZone("America/Denver"); // UTC-7 (Mountain Daylight Time)

            EventDateTime updatedEnd = new EventDateTime()
                    .setDateTime(updatedEndDateTime)
                    .setTimeZone("America/Denver");

            event.setStart(updatedStart);
            event.setEnd(updatedEnd);

            Event updatedEvent = service.events().update(newEvent.getCalendarId(), newEvent.getId(), event).execute();
            System.out.println("Event updated: " + updatedEvent.getHtmlLink());
            return updatedEvent;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
