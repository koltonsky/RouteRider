package com.example.routerider;

import android.os.AsyncTask;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;

// NO CHATGPT
public class DeleteEventTask extends AsyncTask<Void, Void, Event> {
    private Calendar service;
    private ScheduleItem item;

    public DeleteEventTask(Calendar service, ScheduleItem item) {
        this.service = service;
        this.item = item;
    }

    @Override
    protected Event doInBackground(Void... voids) {
        try {
            // Initialize the Google Calendar service
            // Create an event
            service.events().delete(item.getCalendarId(), item.getId()).execute();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
