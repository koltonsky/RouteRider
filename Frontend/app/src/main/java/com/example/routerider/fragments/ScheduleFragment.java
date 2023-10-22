package com.example.routerider.fragments;

import static com.example.routerider.CalendarQuickstart.APPLICATION_NAME;
import static com.example.routerider.CalendarQuickstart.JSON_FACTORY;
import static com.example.routerider.CalendarQuickstart.getCredentials;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.routerider.CalendarQuickstart;
import com.example.routerider.R;
import com.example.routerider.User;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

//        getCalendar.setOnClickListener(v -> {
//
//            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
//                    requireContext(), Collections.singleton(CalendarScopes.CALENDAR_READONLY));
//            credential.setSelectedAccount(account.getAccount());
//
//            Calendar service = null;
//            try {
//                service = new Calendar.Builder(
//                        GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), credential)
//                        .setApplicationName("RouteRider")
//                        .build();
//
//                // Attempt to access Google Calendar API
//                try {
//                    CalendarList calendarList = service.calendarList().list().execute();
//                    List<CalendarListEntry> items = calendarList.getItems();
//
//                    if (items.isEmpty()) {
//                        System.out.println("No calendars found.");
//                    } else {
//                        System.out.println("Calendars:");
//
//                        for (CalendarListEntry calendarEntry : items) {
//                            String calendarId = calendarEntry.getId();
//                            String summary = calendarEntry.getSummary();
//
//                            System.out.println("Calendar ID: " + calendarId);
//                            System.out.println("Summary: " + summary);
//                            System.out.println();
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    // Handle the network-related exception
//                    Toast.makeText(requireContext(), "Failed to retrieve calendar data. Please try again.", Toast.LENGTH_SHORT).show();
//                }
//            } catch (GeneralSecurityException | IOException e) {
//                e.printStackTrace();
//                // Handle the security exception
//                Toast.makeText(requireContext(), "Security exception. Please try again.", Toast.LENGTH_SHORT).show();
//            }
//        });

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
            new MyAsyncTask().execute(service);
        });


        return view;
    }
}

class MyAsyncTask extends AsyncTask<Calendar, Void, Void> {
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