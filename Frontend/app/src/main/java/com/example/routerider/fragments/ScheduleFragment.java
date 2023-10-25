package com.example.routerider.fragments;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.routerider.R;
import com.example.routerider.User;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class ScheduleFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        Button getCalendar = view.findViewById(R.id.connectCalendar);
        GoogleSignInAccount account = User.getCurrentAccount();

        getCalendar.setOnClickListener(v -> {
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    requireContext(), Collections.singleton(CalendarScopes.CALENDAR_READONLY));
            credential.setSelectedAccount(account.getAccount());

            Calendar service = null;
            try {
                service = new Calendar.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), credential)
                        .setApplicationName("RouteRider")
                        .build();
            } catch (GeneralSecurityException | IOException e) {
                throw new RuntimeException(e);
            }
            new CalendarAsyncTask().execute(service);
        });


        return view;
    }
}

class CalendarAsyncTask extends AsyncTask<Calendar, Void, Void> {
    @Override
    protected Void doInBackground(Calendar... calendars) {
        Calendar service = calendars[0];
        try {
            CalendarList calendarList = service.calendarList().list().execute();
            List<CalendarListEntry> items = calendarList.getItems();

            if (items.isEmpty()) {
                System.out.println("No calendars found.");
            } else {
                System.out.println("Calendars:");

                for (CalendarListEntry calendarEntry : items) {
                    String calendarId = calendarEntry.getId();
                    String summary = calendarEntry.getSummary();

                    System.out.println("Calendar ID: " + calendarId);
                    System.out.println("Summary: " + summary);
                    System.out.println();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the network-related exception
            // You can also use a handler to update the UI with any results or error messages
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        // This method runs on the UI thread and can be used to update the UI with results
        // For example, you can show a toast message or update UI components here
    }
}