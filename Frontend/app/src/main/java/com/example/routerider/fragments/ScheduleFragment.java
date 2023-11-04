package com.example.routerider.fragments;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.routerider.APICaller;
import com.example.routerider.HelperFunc;
import com.example.routerider.R;
import com.example.routerider.ScheduleItem;
import com.example.routerider.TimeGapRecommendation;
import com.example.routerider.User;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleFragment extends Fragment {
    private LinearLayout scheduleView;
    private Date currentDay;
    private TextView currentDayText;
    private CalendarAsyncTask calendarAsyncTask;
    private DateFormat formatter;
    private Button getPreviousDay;
    private Button getNextDay;
    private FloatingActionButton addEvent;
    private List<ScheduleItem> eventList;

    // YES CHATGPT
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        getPreviousDay = view.findViewById(R.id.previousDay);
        getPreviousDay.setEnabled(false);
        getNextDay = view.findViewById(R.id.nextDay);
        getNextDay.setEnabled(false);
        GoogleSignInAccount account = User.getCurrentAccount();
        currentDay = new Date();
        formatter = new SimpleDateFormat("E, dd MMM");
        currentDayText = view.findViewById(R.id.currentDayText);
        currentDayText.setText(formatter.format(currentDay));
        scheduleView = view.findViewById(R.id.scheduleView);
        addEvent = view.findViewById(R.id.floatingActionButton);

        APICaller apiCall = new APICaller();
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                requireContext(), Collections.singleton(CalendarScopes.CALENDAR));
        credential.setSelectedAccount(account.getAccount());

        Calendar service;
        try {
            service = new Calendar.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), credential)
                    .setApplicationName("RouteRider")
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        calendarAsyncTask = (CalendarAsyncTask) new CalendarAsyncTask(this.getActivity(), this.getContext(), view, account).execute(service);


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

        addEvent.setOnClickListener(v -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());
            alertDialogBuilder.setTitle("Add Event");

            // Inflate the custom view for the dialog
            LayoutInflater dialogInflater = LayoutInflater.from(requireContext());
            View dialogView = dialogInflater.inflate(R.layout.add_event_dialog, null);
            alertDialogBuilder.setView(dialogView);

            final EditText eventNameEditText = dialogView.findViewById(R.id.eventName);
            final EditText eventAddressEditText = dialogView.findViewById(R.id.eventAddress);
            final EditText eventStartTimeEditText = dialogView.findViewById(R.id.eventStartTime);
            final EditText eventEndTimeEditText = dialogView.findViewById(R.id.eventEndTime);

            final EditText eventDateMonthEditText = dialogView.findViewById(R.id.eventDateMonth);
            final EditText eventDateDayEditText = dialogView.findViewById(R.id.eventDateDay);
            final EditText eventDateYearEditText = dialogView.findViewById(R.id.eventDateYear);


            alertDialogBuilder.setPositiveButton("OK", null);

            alertDialogBuilder.setNegativeButton("Cancel", (dialog, which) -> {
                dialog.dismiss();
            });

            final AlertDialog dialog = alertDialogBuilder.create();
            dialog.setOnShowListener(dialogInterface -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view1 -> {
                String eventName = eventNameEditText.getText().toString();
                String eventAddress = eventAddressEditText.getText().toString();
                String eventDate = eventDateYearEditText.getText().toString() + "-"
                        + eventDateMonthEditText.getText().toString() + "-"
                        + eventDateDayEditText.getText().toString();
                String eventStartTime = eventStartTimeEditText.getText().toString();
                String eventEndTime = eventEndTimeEditText.getText().toString();

                if (ScheduleFragment.EventInputListener.onEventInput(eventName, eventAddress, eventDate, eventStartTime, eventEndTime)) {
                    new CreateEventTask(service, eventName, eventAddress, eventDate, eventStartTime, eventEndTime).execute();

                    ScheduleItem newEvent = new ScheduleItem(
                            eventName,
                            eventAddress,
                            eventDate + "T" + eventStartTime + ":00",
                            eventDate + "T" + eventEndTime + ":00",
                            HelperFunc.generateRandomString(64),
                            "primary");

                    String jsonUpdateEvent = new Gson().toJson(newEvent);

                    apiCall.APICall("api/schedulelist/" + account.getEmail(), jsonUpdateEvent, APICaller.HttpMethod.POST, new APICaller.ApiCallback() {
                        @Override
                        public void onResponse(String responseBody) {
                            System.out.println("new event added to db");
                            System.out.println("BODY: " + responseBody);
                            calendarAsyncTask = (CalendarAsyncTask) new CalendarAsyncTask(getActivity(), getContext(), view, account).execute(service);
//                                eventName.setText(newEvent.getTitle());
//                                eventLocation.setText(newEvent.getLocation());
//                                startTime.setText(newEvent.getStartTime().substring(11,16));
//                                endTime.setText(newEvent.getEndTime().substring(11,16));
                        }

                        @Override
                        public void onError(String errorMessage) {
                            System.out.println("Error " + errorMessage);
                        }
                    });

                    dialog.dismiss();
                } else {
                    Toast.makeText(requireContext(), "Invalid inputs, please try again", Toast.LENGTH_SHORT).show();
                }
            }));

            dialog.show();

        });

        return view;
    }

    // NO CHATGPT
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

    // YES CHATGPT
    public interface EventInputListener {
        static boolean onEventInput(String eventName, String eventAddress, String eventDate, String eventStartTime, String eventEndTime) {
            if(validateInputs(eventName, eventAddress, eventDate, eventStartTime, eventEndTime)) {
                return true;
            }

            return false;
        }
    }

    // YES CHATGPT
    private static boolean validateInputs(String eventName, String eventAddress, String eventDate, String eventStartTime, String eventEndTime) {
        if (eventName.isEmpty() || eventAddress.isEmpty() || eventDate.isEmpty() || eventStartTime.isEmpty() || eventEndTime.isEmpty()) {
            // Check if any field is empty
            return false;
        }

        if (!isDateValid((eventDate + " " + eventStartTime)) || !isTimeValid(eventStartTime) || !isTimeValid(eventEndTime)) {
            // Check if date and time formats are valid
            return false;
        }

        if (!isStartTimeBeforeEndTime(eventStartTime, eventEndTime)) {
            return false;
        }

        return true;
    }

    // YES CHATGPT
    private static boolean isDateValid(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        dateFormat.setLenient(false); // Set lenient to false to enforce strict date validation

        try {
            Date parsedDate = dateFormat.parse(date);
            Date currentDate = new Date(); // Get the current date and time

            assert parsedDate != null;
            return parsedDate.after(currentDate); // Date is valid and ahead of the current date and time
        } catch (ParseException e) {
            // Parsing failed, the date is not valid
            return false;
        }
    }


    // YES CHATGPT
    private static boolean isTimeValid(String time) {
        String timePattern = "([01]\\d|2[0-3]):[0-5]\\d";
        return time.matches(timePattern);
    }

    // YES CHATGPT
    private static boolean isStartTimeBeforeEndTime(String startTime, String endTime) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        timeFormat.setLenient(false);

        try {
            Date start = timeFormat.parse(startTime);
            Date end = timeFormat.parse(endTime);

            assert start != null;
            return start.before(end);
        } catch (java.text.ParseException e) {
            // Parsing failed, the times are not valid
            return false;
        }
    }

}


// YES CHATGPT
class CalendarAsyncTask extends AsyncTask<Calendar, Void, Void> {
    private List<ScheduleItem> eventList;
    private List<ScheduleItem> dayList;
    private GoogleSignInAccount account;
    private Calendar service;

    private Context scheduleContext;
    private Activity scheduleActivity;
    private View scheduleView;
    CalendarAsyncTask(Activity activity, Context context, View view,
                      GoogleSignInAccount account){
        this.scheduleActivity = activity;
        this.scheduleContext = context;
        this.scheduleView = view;
        this.account = account;
    }
    @Override
    protected Void doInBackground(Calendar... calendars) {
        dayList =  new ArrayList<>();
        service = calendars[0];
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

                    // List events for the calendar
                    long sevenDaysInMillis = 30L * 24 * 60 * 60 * 1000;
                    Events events = service.events().list(calendarId)
                            .setSingleEvents(true)
                            .setTimeMin(new DateTime(System.currentTimeMillis()))
                            .setTimeMax(new DateTime(System.currentTimeMillis() + sevenDaysInMillis)) // Set the time range as needed
                            .execute();

                    List<Event> itemsEvents = events.getItems();
                    Comparator<Event> eventComparator = new Comparator<Event>() {
                        @Override
                        public int compare(Event event1, Event event2) {
                            // Assuming the start time is in ISO8601 format
                            String startTime1 = String.valueOf(event1.getStart().getDateTime());
                            String startTime2 = String.valueOf(event2.getStart().getDateTime());

                            if (startTime1 == null || startTime2 == null) {
                                return 0; // Handle null values if needed
                            }

                            // Compare events by start time (in ISO8601 format)
                            return startTime1.compareTo(startTime2);
                        }
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

                        System.out.println("Event ID: " + eventId);
//                            System.out.println("Event Summary: " + eventSummary);
//                            System.out.println("Event Location: " + eventLocation);
//                            System.out.println("Start Time: " + startTimeString);
//                            System.out.println("End Time: " + endTimeString);
//                            System.out.println();
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
                            eventList.add(newEvent);
                            System.out.println("adding event");
                        }
                    }
                }
            }

            Map<String, Object> test = new HashMap<>();
            test.put("email", account.getEmail());
            test.put("events", eventList);
            String jsonSchedule = new Gson().toJson(test);

            System.out.println(jsonSchedule);
            System.out.println("avbout to get schedule");


            apiCall.APICall("api/schedulelist/" + account.getEmail(), "", APICaller.HttpMethod.GET, new APICaller.ApiCallback() {
                @Override
                public void onResponse(String responseBody) {
                    System.out.println("BODY: " + responseBody);
                    if(responseBody.equals("\"Schedule found\"")) {
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
                    if( (errorMessage.split(",")[0]).equals("Error: 404")){
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
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    // NO CHATGPT
    @Override
    protected void onPostExecute(Void result) {
        System.out.println(eventList.size());
        Button getNextDay = scheduleView.findViewById(R.id.nextDay);
        getNextDay.setEnabled(true);
        Date today = new Date();
        updateDisplay(today);
    }


    // YES CHATGPT
    public void updateDisplay(Date day) {
        dayList = new ArrayList<>();
        LinearLayout eventListView = scheduleView.findViewById(R.id.scheduleView);
        TextView emptyListText = scheduleView.findViewById(R.id.emptyText);
        eventListView.removeAllViewsInLayout();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = dateFormat.format(day);
        for (ScheduleItem item: eventList){
            String itemDay = item.getStartTime().substring(0,10);
            if (dateString.equals(itemDay)){
                dayList.add(item);
            }
        }
        if(dayList.size() > 0) {
            emptyListText.setVisibility(View.GONE);
        } else {
            emptyListText.setVisibility(View.VISIBLE);
        }

        LayoutInflater inflater = LayoutInflater.from(scheduleContext);

        SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

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
//                            for (int i = 0; i<5; i++){
//                                TimeGapRecommendation rec1 = new TimeGapRecommendation("cafe", "Starbucks", "123 Main St H2Z 0D4 Vancouver BC Canada");
//                                TimeGapRecommendation rec2 = new TimeGapRecommendation("library", "Irving K Barber Library", "123 Main St H2Z 0D4 Vancouver BC Canada");
//                                TimeGapRecommendation rec3 = new TimeGapRecommendation("restaurant", "Cactus Club Cafe", "123 Main St H2Z 0D4 Vancouver BC Canada");
//                                recs.add(rec1);
//                                recs.add(rec2);
//                                recs.add(rec3);
//                            }


                            if (hiddenView.getVisibility() == View.VISIBLE) {
                                TransitionManager.beginDelayedTransition(cardView, new AutoTransition());
                                hiddenView.setVisibility(View.GONE);
                                viewRecommendationsButton.setText("Show Recommendations");
                            }
                            else {
                                ArrayList<TimeGapRecommendation> recs = new ArrayList<>();
                                hiddenView.removeAllViewsInLayout();
                                APICaller apiCall = new APICaller();
                                apiCall.APICall("api/recommendation/timegap/" + item.getLocation() + "/" + dayList.get(dayList.indexOf(item) - 1).getLocation(), "", APICaller.HttpMethod.GET, new APICaller.ApiCallback() {

                                    @Override
                                    public void onResponse(String responseBody) throws JSONException {
                                        scheduleActivity.runOnUiThread(() -> {
                                            System.out.println("BODY: " + responseBody);
                                            try {
                                                JSONObject json = new JSONObject(responseBody);
                                                JSONArray recOutput = json.getJSONArray("suggestions");
                                                System.out.println(recOutput);
                                                for (int i = 0; i < recOutput.length(); i++) {
                                                    JSONObject item = (JSONObject) recOutput.get(i);
                                                    System.out.println(item);
                                                    JSONArray types = item.getJSONArray("types");
                                                    String type = "";
                                                    for (int j = 0; j < types.length(); j++) {
                                                        String element = types.getString(j);
                                                        if (element.equals("restaurant") || element.equals("cafe") || element.equals("library")) {
                                                            type = element;
                                                            break; // You can exit the loop early if you find a match
                                                        }
                                                    }

                                                    String name = item.getString("name");
                                                    String address = item.getString("vicinity");
                                                    TimeGapRecommendation timeGapRecommendation = new TimeGapRecommendation(type, name, address);
                                                    recs.add(timeGapRecommendation);
                                                }
                                                for (TimeGapRecommendation rec : recs) {
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
                                                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                                                Uri.parse("google.navigation:q=" + rec.getAddress()));
                                                        startActivity(scheduleContext, intent, null);
                                                    });
                                                    hiddenView.addView(timeGapRecView);
                                                }
                                                TransitionManager.beginDelayedTransition(cardView, new AutoTransition());
                                                hiddenView.setVisibility(View.VISIBLE);
                                                viewRecommendationsButton.setText("Hide Recommendations");
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        System.out.println("Error " + errorMessage);
                                    }
                                });
                            }
                        });

                        eventListView.addView(timeGapView);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            endTime.setText(item.getEndTime().substring(11,16));
            System.out.println(item);

            view.setOnClickListener(v -> {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(scheduleContext);
                alertDialogBuilder.setTitle("Update Event");

                // Inflate the custom view for the dialog
                LayoutInflater dialogInflater = LayoutInflater.from(scheduleContext);
                View dialogView = dialogInflater.inflate(R.layout.add_event_dialog, null);
                alertDialogBuilder.setView(dialogView);

                final EditText eventNameEditText = dialogView.findViewById(R.id.eventName);
                final EditText eventAddressEditText = dialogView.findViewById(R.id.eventAddress);
                final EditText eventStartTimeEditText = dialogView.findViewById(R.id.eventStartTime);
                final EditText eventEndTimeEditText = dialogView.findViewById(R.id.eventEndTime);

                final EditText eventDateMonthEditText = dialogView.findViewById(R.id.eventDateMonth);
                final EditText eventDateDayEditText = dialogView.findViewById(R.id.eventDateDay);
                final EditText eventDateYearEditText = dialogView.findViewById(R.id.eventDateYear);

                eventNameEditText.setText(item.getTitle());
                eventAddressEditText.setText(item.getLocation());
                eventStartTimeEditText.setText(item.getStartTime().substring(11,16));
                eventEndTimeEditText.setText(item.getEndTime().substring(11,16));
                eventDateMonthEditText.setText(item.getStartTime().substring(5,7));
                eventDateDayEditText.setText(item.getStartTime().substring(8,10));
                eventDateYearEditText.setText(item.getStartTime().substring(0,4));


                alertDialogBuilder.setPositiveButton("OK", null);

                alertDialogBuilder.setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                });

                final AlertDialog dialog = alertDialogBuilder.create();
                dialog.setOnShowListener(dialogInterface -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view1 -> {
                    String updateEventName = eventNameEditText.getText().toString();
                    String eventAddress = eventAddressEditText.getText().toString();
                    String eventDate = eventDateYearEditText.getText().toString() + "-"
                            + eventDateMonthEditText.getText().toString() + "-"
                            + eventDateDayEditText.getText().toString();
                    String eventStartTime = eventStartTimeEditText.getText().toString();
                    String eventEndTime = eventEndTimeEditText.getText().toString();

                    // 2023-11-01T15:00:00.000-07:00
                    System.out.println(eventDate + "T" + eventStartTime + ":00");
                    if (ScheduleFragment.EventInputListener.onEventInput(updateEventName, eventAddress, eventDate, eventStartTime, eventEndTime)) {
                        APICaller apiCall = new APICaller();
                        ScheduleItem newEvent = new ScheduleItem(
                                updateEventName,
                                eventAddress,
                                eventDate + "T" + eventStartTime + ":00.000-07:00",
                                eventDate + "T" + eventEndTime + ":00.000-07:00",
                                item.getId(),
                                item.getCalendarId());

                        String jsonUpdateEvent = new Gson().toJson(newEvent);
                        apiCall.APICall("api/schedulelist/" + account.getEmail() + "/" + item.getId(), "", APICaller.HttpMethod.DELETE, new APICaller.ApiCallback() {
                            @Override
                            public void onResponse(String responseBody) {
                                System.out.println("BODY: " + responseBody);
                                apiCall.APICall("api/schedulelist/" + account.getEmail() + "/", jsonUpdateEvent, APICaller.HttpMethod.POST, new APICaller.ApiCallback() {
                                    @Override
                                    public void onResponse(String responseBody) {

                                        System.out.println("BODY: " + responseBody);
                                        Handler handler = new Handler(Looper.getMainLooper());
                                        handler.post(() -> {
                                            eventName.setText(newEvent.getTitle());
                                            eventLocation.setText(newEvent.getLocation());
                                            startTime.setText(newEvent.getStartTime().substring(11, 16));
                                            endTime.setText(newEvent.getEndTime().substring(11, 16));
                                        });
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        System.out.println("Error " + errorMessage);
                                    }
                                });
                            }

                            @Override
                            public void onError(String errorMessage) {
                                System.out.println("Error " + errorMessage);
                            }
                        });

                        new UpdateEventTask(service, newEvent).execute();

                        dialog.dismiss();
                    } else {
                        Toast.makeText(scheduleContext, "Invalid inputs, please try again", Toast.LENGTH_SHORT).show();
                    }
                }));

                dialog.show();
            });

            LinearLayout finalEventListView = eventListView;
            view.setOnLongClickListener(v -> {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(scheduleContext);
                alertDialogBuilder.setTitle("Delete Event");
                alertDialogBuilder.setMessage("Are you sure you want to delete this event?");

                alertDialogBuilder.setPositiveButton("OK", null);

                alertDialogBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                final AlertDialog dialog = alertDialogBuilder.create();
                dialog.setOnShowListener(dialogInterface -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view1 -> {
                    APICaller apiCall = new APICaller();

                    apiCall.APICall("api/schedulelist/" + account.getEmail() + "/" + item.getId(), "", APICaller.HttpMethod.DELETE, new APICaller.ApiCallback() {
                        @Override
                        public void onResponse(String responseBody) {
                            System.out.println("BODY: " + responseBody);

                        }

                        @Override
                        public void onError(String errorMessage) {
                            System.out.println("Error " + errorMessage);
                        }
                    });
                    finalEventListView.removeView(view);
                    new DeleteEventTask(service, item).execute();
                    dialog.dismiss();
                }));

                dialog.show();

                return true;
            });

            eventListView.addView(view);
            previousEventView = view;
        }


    }

    // YES CHATGPT
    private String formatTimeDifference(long timeDifference) {
        long minutes = (timeDifference / (1000 * 60)) % 60;
        long hours = (timeDifference / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d", hours, minutes);
    }
}

// NO CHATGPT
class UpdateEventTask extends AsyncTask<Void, Void, Event> {
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

// NO CHATGPT
class CreateEventTask extends AsyncTask<Void, Void, Event> {
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

            DateTime startDateTime = new DateTime(eventDate + "T" + eventStartTime + ":00.000-07:00");
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone(timeZone);
            event.setStart(start);

            // Set the end time
            DateTime endDateTime = new DateTime(eventDate + "T" + eventEndTime + ":00.000-07:00");
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

// NO CHATGPT
class DeleteEventTask extends AsyncTask<Void, Void, Event> {
    private Calendar service;
    private ScheduleItem item;

    public DeleteEventTask(Calendar service,ScheduleItem item) {
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



