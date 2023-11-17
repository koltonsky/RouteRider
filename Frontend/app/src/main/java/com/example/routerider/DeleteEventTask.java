package com.example.routerider;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;

// NO CHATGPT
public class DeleteEventTask extends AsyncTask<Void, Void, Event> {
    private Calendar service;
    private ScheduleItem item;
    private Context context;

    public DeleteEventTask(Calendar service, ScheduleItem item, Context context) {
        this.service = service;
        this.item = item;
        this.context = context;
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

    @Override
    protected void onPostExecute(Event event) {
        // This method is executed on the UI thread
        Toast.makeText(context, "Successfully deleted event", Toast.LENGTH_SHORT).show();
    }
}
