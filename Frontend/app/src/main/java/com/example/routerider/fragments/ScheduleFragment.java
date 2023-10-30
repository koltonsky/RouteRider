package com.example.routerider.fragments;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.routerider.APICaller;
import com.example.routerider.R;
import com.example.routerider.ScheduleItem;
import com.example.routerider.TimeGapRecommendation;
import com.example.routerider.User;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.Time;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

public class ScheduleFragment extends Fragment {
    private LinearLayout scheduleView;
    private Button connectButton;
    private Date currentDay;
    private TextView currentDayText;
    private CalendarAsyncTask calendarAsyncTask;
    private DateFormat formatter;
    private Button getPreviousDay;
    private Button getNextDay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        Button getCalendar = view.findViewById(R.id.connectCalendar);
        getPreviousDay = view.findViewById(R.id.previousDay);
        getPreviousDay.setEnabled(false);
        getNextDay = view.findViewById(R.id.nextDay);
        getNextDay.setEnabled(false);
        GoogleSignInAccount account = User.getCurrentAccount();
        currentDay = new Date();
        formatter = new SimpleDateFormat("E, dd MMM");
        currentDayText = view.findViewById(R.id.currentDayText);
        currentDayText.setText(formatter.format(currentDay));

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
            scheduleView = view.findViewById(R.id.scheduleView);
            connectButton = view.findViewById(R.id.connectCalendar);
            calendarAsyncTask = (CalendarAsyncTask) new CalendarAsyncTask(this.getContext(),view,connectButton).execute(service);
        });

        getPreviousDay.setOnClickListener(v -> {
            java.util.Calendar calendar =  java.util.Calendar.getInstance();
            calendar.setTime(currentDay);
            calendar.add( java.util.Calendar.DAY_OF_YEAR, -1); // Subtract 1 day to get the previous day
            Date previousDay = calendar.getTime();
            changeDay(previousDay);
        });

        getNextDay.setOnClickListener(v -> {
            java.util.Calendar calendar =  java.util.Calendar.getInstance();
            calendar.setTime(currentDay);
            calendar.add( java.util.Calendar.DAY_OF_YEAR, 1); // Add 1 day to get the next day
            Date nextDay = calendar.getTime();
            changeDay(nextDay);
        });

        return view;
    }
    private void changeDay(Date day){
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (sdf.format(today).equals(sdf.format(day))) {
            getPreviousDay.setEnabled(false);
        } else {
            getPreviousDay.setEnabled(true);
        }
        currentDay = day;
        currentDayText.setText(formatter.format(day));
        calendarAsyncTask.updateDisplay(day);
    }
}

class CalendarAsyncTask extends AsyncTask<Calendar, Void, Void> {
    private List<ScheduleItem> eventList;
    private List<ScheduleItem> dayList;

    private Context scheduleContext;
    private View scheduleView;
    private Button connectButton;
    CalendarAsyncTask(Context context, View view, Button button){
        this.scheduleContext = context;
        this.scheduleView = view;
        this.connectButton = button;
    }
    @Override
    protected Void doInBackground(Calendar... calendars) {
        dayList =  new ArrayList<>();
            Calendar service = calendars[0];
            eventList = new ArrayList<>();
            APICaller apiCall = new APICaller();

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

                        // List events for the calendar
                        Events events = service.events().list(calendarId)
                                .setTimeMin(new DateTime(System.currentTimeMillis()))
                                //.setTimeMax(new DateTime(System.currentTimeMillis() + 86400000)) // Set the time range as needed
                                .execute();

                        List<Event> itemsEvents = events.getItems();
                        for (Event event : itemsEvents) {
                            String eventId = event.getId();
                            String eventSummary = event.getSummary();
                            String eventLocation = (event.getLocation() != null) ? event.getLocation() : "N/A";
                            DateTime startTime = event.getStart().getDateTime();
                            DateTime endTime = event.getEnd().getDateTime();

                            // Format the start and end times as strings (you can customize the format)
                            String startTimeString = (startTime != null) ? startTime.toString() : "N/A";
                            String endTimeString = (endTime != null) ? endTime.toString() : "N/A";

                            System.out.println("Event ID: " + eventId);
                            System.out.println("Event Summary: " + eventSummary);
                            System.out.println("Event Location: " + eventLocation);
                            System.out.println("Start Time: " + startTimeString);
                            System.out.println("End Time: " + endTimeString);
                            System.out.println();
                            if (eventLocation.contains("Room")) {
                                eventLocation = "UBC " + eventLocation;
                            }

                            ScheduleItem newEvent = new ScheduleItem(
                                    eventSummary,
                                    eventLocation,
                                    startTimeString,
                                    endTimeString);

                            if (!startTimeString.equals("N/A")) {
                                eventList.add(newEvent);
                                System.out.println("adding event");
                            }
                        }
                    }
                }

                String jsonStr = new Gson().toJson(eventList);
                System.out.println("STRING " + jsonStr);

//            apiCall.APICall("api/schedulelist", jsonStr, APICaller.HttpMethod.POST, new APICaller.ApiCallback() {
//                @Override
//                public void onResponse(String responseBody) {
//                    System.out.println("BODY: " + responseBody);
//                }
//
//                @Override
//                public void onError(String errorMessage) {
//                    System.out.println("Error " + errorMessage);
//                }
//            });

            } catch (IOException e) {
                e.printStackTrace();
                // Handle the network-related exception
                // You can also use a handler to update the UI with any results or error messages
            }

        //return eventList;
        return null;
    }

    public void updateDisplay(Date day) {
        dayList = new ArrayList<>();
        LinearLayout eventListView = scheduleView.findViewById(R.id.scheduleView);
        eventListView.removeAllViewsInLayout();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = dateFormat.format(day);
        for (ScheduleItem item: eventList){
            String itemDay = item.getStartTime().substring(0,10);
            if (dateString.equals(itemDay)){
                dayList.add(item);
            }
        }
        LayoutInflater inflater = LayoutInflater.from(scheduleContext);
//        for (ScheduleItem item: dayList) {
//            eventListView = scheduleView.findViewById(R.id.scheduleView);
//            View view  = inflater.inflate(R.layout.view_event, eventListView, false);
//            // set item content in view
//            TextView eventName = view.findViewById(R.id.eventName);
//            eventName.setText(item.getTitle());
//            TextView eventLocation = view.findViewById(R.id.eventLocation);
//            eventLocation.setText(item.getLocation());
//            TextView startTime = view.findViewById(R.id.startTime);
//            startTime.setText(item.getStartTime().substring(11,16));
//            TextView endTime = view.findViewById(R.id.endTime);
//            endTime.setText(item.getEndTime().substring(24));
//            System.out.println(item);
//            eventListView.addView(view);
//        }
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        long fifteenMinutesInMillis = 15 * 60 * 1000; // 15 minutes in milliseconds
        View previousEventView = null;

        for (ScheduleItem item : dayList) {
            eventListView = scheduleView.findViewById(R.id.scheduleView);
            View view = inflater.inflate(R.layout.view_event, eventListView, false);

            TextView eventName = view.findViewById(R.id.eventName);
            eventName.setText(item.getTitle());

            TextView eventLocation = view.findViewById(R.id.eventLocation);
            eventLocation.setText(item.getLocation());

            TextView startTime = view.findViewById(R.id.startTime);
            startTime.setText(item.getStartTime().substring(11, 16));

            TextView endTime = view.findViewById(R.id.endTime);
            endTime.setText(item.getEndTime().substring(11, 16)); // Adjust substring as needed

            // Check if there is a previous event view and the time gap is more than 15 minutes
            if (previousEventView != null) {
                try {
                    Date previousEndTime = fullDateFormat.parse(dayList.get(dayList.indexOf(item) - 1).getEndTime());
                    Date currentStartTime = fullDateFormat.parse(item.getStartTime());
                    long timeDifference = currentStartTime.getTime() - previousEndTime.getTime();

                    if (timeDifference > fifteenMinutesInMillis) {
                        View timeGapView = inflater.inflate(R.layout.timegap_chip, eventListView, false);
                        TextView timeGapTextView = timeGapView.findViewById(R.id.timeGapTextView);
                        timeGapTextView.setText("Gap: " + formatTimeDifference(timeDifference));
                        LinearLayout hiddenView = timeGapView.findViewById(R.id.hidden_view);
                        CardView cardView = timeGapView.findViewById(R.id.base_cardview);
                        Button viewRecommendationsButton = timeGapView.findViewById(R.id.viewRecommendationsButton);
                        viewRecommendationsButton.setOnClickListener(v -> {
                            ArrayList<TimeGapRecommendation> recs = new ArrayList<>();
                            for (int i = 0; i<5; i++){
                                TimeGapRecommendation rec1 = new TimeGapRecommendation("cafe", "Starbucks", "123 Main St H2Z 0D4 Vancouver BC Canada");
                                TimeGapRecommendation rec2 = new TimeGapRecommendation("library", "Irving K Barber Library", "123 Main St H2Z 0D4 Vancouver BC Canada");
                                TimeGapRecommendation rec3 = new TimeGapRecommendation("restaurant", "Cactus Club Cafe", "123 Main St H2Z 0D4 Vancouver BC Canada");
                                recs.add(rec1);
                                recs.add(rec2);
                                recs.add(rec3);
                            }
                            for (TimeGapRecommendation rec: recs) {
                                View timeGapRecView;
                                if (rec.getType() == "cafe") {
                                    timeGapRecView = inflater.inflate(R.layout.cafe_chip, hiddenView, false);
                                } else if (rec.getType() == "library") {
                                    timeGapRecView = inflater.inflate(R.layout.library_chip, hiddenView, false);

                                } else {
                                    timeGapRecView = inflater.inflate(R.layout.restaurant_chip, hiddenView, false);
                                }
                                TextView recNameText = timeGapRecView.findViewById(R.id.recName);
                                recNameText.setText(rec.getName());
                                TextView recAddressText = timeGapRecView.findViewById(R.id.recAddress);
                                recAddressText.setText(rec.getAddress());
                                ImageButton recMapsButton = timeGapRecView.findViewById(R.id.mapsButton);
                                recMapsButton.setOnClickListener(v2 -> {
                                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                            Uri.parse("google.navigation:q="+ rec.getAddress()));
                                    startActivity(scheduleContext,intent,null);
                                });
                                hiddenView.addView(timeGapRecView);
                            }
                            if (hiddenView.getVisibility() == View.VISIBLE) {
                                TransitionManager.beginDelayedTransition(cardView, new AutoTransition());
                                hiddenView.setVisibility(View.GONE);
                                viewRecommendationsButton.setText("Show Recommendations");
                            }
                            else {
                                TransitionManager.beginDelayedTransition(cardView, new AutoTransition());
                                hiddenView.setVisibility(View.VISIBLE);
                                viewRecommendationsButton.setText("Hide Recommendations");
                            }
                        });

                        eventListView.addView(timeGapView);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            eventListView.addView(view);
            previousEventView = view;
        }


    }

    private void mockRecs(ArrayList<TimeGapRecommendation> recs) {

    }

    // Helper method to format the time difference as HH:mm
    private String formatTimeDifference(long timeDifference) {
        long minutes = (timeDifference / (1000 * 60)) % 60;
        long hours = (timeDifference / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d", hours, minutes);
    }


    @Override
    protected void onPostExecute(Void result) {

        // This method runs on the UI thread and can be used to update the UI with results
        // For example, you can show a toast message or update UI components here
        System.out.println(eventList.size());
        connectButton.setVisibility(View.GONE);
        Button getNextDay = scheduleView.findViewById(R.id.nextDay);
        getNextDay.setEnabled(true);
        Date today = new Date();
        updateDisplay(today);
    }
}