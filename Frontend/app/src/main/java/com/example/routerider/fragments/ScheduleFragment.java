package com.example.routerider.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.routerider.APICaller;
import com.example.routerider.R;
import com.example.routerider.ScheduleItem;
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
import com.google.api.services.calendar.model.Events;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Map;

public class ScheduleFragment extends Fragment {
    private LinearLayout scheduleView;
    private Date currentDay;
    private TextView currentDayText;
    private CalendarAsyncTask calendarAsyncTask;
    private DateFormat formatter;
    private Button getPreviousDay;
    private Button getNextDay;
    private Button transitFriend;
    private FloatingActionButton addEvent;

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
        transitFriend = view.findViewById(R.id.transitFriendButton);

        APICaller apiCall = new APICaller();
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                requireContext(), Collections.singleton(CalendarScopes.CALENDAR_READONLY));
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

        calendarAsyncTask = (CalendarAsyncTask) new CalendarAsyncTask(this.getContext(), view, account).execute(service);


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
            currentDayText.setText(formatter.format(nextDay));
            calendarAsyncTask.updateDisplay(nextDay);
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

                if (EventInputListener.onEventInput(eventName, eventAddress, eventDate, eventStartTime, eventEndTime)) {
//                    apiCall.APICall("api/schedulelist/", "", APICaller.HttpMethod.POST, new APICaller.ApiCallback() {
//                        @Override
//                        public void onResponse(String responseBody) {
//                            System.out.println("BODY: " + responseBody);
//                        }
//
//                        @Override
//                        public void onError(String errorMessage) {
//                            System.out.println("Error " + errorMessage);
//                        }
//                    });
                    dialog.dismiss();
                } else {
                    Toast.makeText(requireContext(), "Invalid inputs, please try again", Toast.LENGTH_SHORT).show();
                }
            }));

            dialog.show();

        });

        transitFriend.setOnClickListener(v -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());
            alertDialogBuilder.setTitle("Friend List");

            // Define an array of items to display in the list
            String[] friendNames = {"Friend 1", "Friend 2", "Friend 3", "Friend 4", "Friend 5", "Friend 6", "Friend 7", "Friend 8", "Friend 9", "Friend 10"};

            // Set the item list and a click listener
            alertDialogBuilder.setItems(friendNames, (dialog, which) -> {
                String selectedFriend = friendNames[which];
                // Perform actions with the selected friend
//                apiCall.APICall("api/schedulelist/", "", APICaller.HttpMethod.POST, new APICaller.ApiCallback() {
//                    @Override
//                    public void onResponse(String responseBody) {
//                        System.out.println("BODY: " + responseBody);
//                    }
//
//                    @Override
//                    public void onError(String errorMessage) {
//                        System.out.println("Error " + errorMessage);
//                    }
//                });
            });

            // Create and show the AlertDialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

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

    public interface EventInputListener {
        static boolean onEventInput(String eventName, String eventAddress, String eventDate, String eventStartTime, String eventEndTime) {
            if(validateInputs(eventName, eventAddress, eventDate, eventStartTime, eventEndTime)) {
                return true;
            }

            return false;
        }
    }

    private static boolean validateInputs(String eventName, String eventAddress, String eventDate, String eventStartTime, String eventEndTime) {
        if (eventName.isEmpty() || eventAddress.isEmpty() || eventDate.isEmpty() || eventStartTime.isEmpty() || eventEndTime.isEmpty()) {
            // Check if any field is empty
            return false;
        }

        if (!isDateValid(eventDate) || !isTimeValid(eventStartTime) || !isTimeValid(eventEndTime)) {
            // Check if date and time formats are valid
            return false;
        }

        if (!isStartTimeBeforeEndTime(eventStartTime, eventEndTime)) {
            return false;
        }

        return true;
    }

    private static boolean isDateValid(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false); // Set lenient to false to enforce strict date validation

        try {
            Date parsedDate = dateFormat.parse(date);
            Date currentDate = new Date(); // Get the current date

            assert parsedDate != null;
            return parsedDate.after(currentDate); // Date is valid and ahead of the current date
        } catch (ParseException | java.text.ParseException e) {
            // Parsing failed, the date is not valid
            return false;
        }
    }

    private static boolean isTimeValid(String time) {
        String timePattern = "([01]\\d|2[0-3]):[0-5]\\d";
        return time.matches(timePattern);
    }

    private static boolean isStartTimeBeforeEndTime(String startTime, String endTime) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        timeFormat.setLenient(false);

        try {
            Date start = timeFormat.parse(startTime);
            Date end = timeFormat.parse(endTime);

            assert start != null;
            return start.before(end);
        } catch (ParseException | java.text.ParseException e) {
            // Parsing failed, the times are not valid
            return false;
        }
    }

}



class CalendarAsyncTask extends AsyncTask<Calendar, Void, Void> {
    private List<ScheduleItem> eventList;
    private List<ScheduleItem> dayList;
    private GoogleSignInAccount account;

    private Context scheduleContext;
    private View scheduleView;
    CalendarAsyncTask(Context context, View view,
                      GoogleSignInAccount account){
        this.scheduleContext = context;
        this.scheduleView = view;
        this.account = account;
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

//                        System.out.println("Calendar ID: " + calendarId);
//                        System.out.println("Summary: " + summary);
//                        System.out.println();

                        // List events for the calendar
                        long sevenDaysInMillis = 365L * 24 * 60 * 60 * 1000;
                        Events events = service.events().list(calendarId)
                                .setSingleEvents(true)
                                .setTimeMin(new DateTime(System.currentTimeMillis()))
                                .setTimeMax(new DateTime(System.currentTimeMillis() + sevenDaysInMillis)) // Set the time range as needed
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

//                            System.out.println("Event ID: " + eventId);
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
                                    endTimeString);

                            if (!startTimeString.equals("N/A")) {
                                eventList.add(newEvent);
                                System.out.println("adding event");
                            }
                        }
                    }
                }

                Map<String, Object> test = new HashMap<>();
                test.put("email", "koltonluu@gmail.com");
                test.put("events", eventList);
                String jsonSchedule = new Gson().toJson(test);

                System.out.println(jsonSchedule);

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
                    }
                });



            } catch (IOException e) {
                e.printStackTrace();
                // Handle the network-related exception
                // You can also use a handler to update the UI with any results or error messages
            }

        //return eventList;
        return null;
    }


    @Override
    protected void onPostExecute(Void result) {
        System.out.println(eventList.size());
        Button getNextDay = scheduleView.findViewById(R.id.nextDay);
        getNextDay.setEnabled(true);
        Date today = new Date();
        updateDisplay(today);
    }



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
        for (ScheduleItem item: dayList) {
            eventListView = scheduleView.findViewById(R.id.scheduleView);
            View view  = inflater.inflate(R.layout.view_event, eventListView, false);
            // set item content in view
            TextView eventName = view.findViewById(R.id.eventName);
            eventName.setText(item.getTitle());
            TextView eventLocation = view.findViewById(R.id.eventLocation);
            eventLocation.setText(item.getLocation());
            TextView startTime = view.findViewById(R.id.startTime);
            startTime.setText(item.getStartTime().substring(11,16));
            TextView endTime = view.findViewById(R.id.endTime);
            endTime.setText(item.getEndTime().substring(24));
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
                eventEndTimeEditText.setText(item.getEndTime().substring(24));
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

                    if (ScheduleFragment.EventInputListener.onEventInput(updateEventName, eventAddress, eventDate, eventStartTime, eventEndTime)) {
//                    apiCall.APICall("api/schedulelist/", "", APICaller.HttpMethod.POST, new APICaller.ApiCallback() {
//                        @Override
//                        public void onResponse(String responseBody) {
//                            System.out.println("BODY: " + responseBody);
//                        }
//
//                        @Override
//                        public void onError(String errorMessage) {
//                            System.out.println("Error " + errorMessage);
//                        }
//                    });
                        dialog.dismiss();
                    } else {
                        Toast.makeText(scheduleContext, "Invalid inputs, please try again", Toast.LENGTH_SHORT).show();
                    }
                }));

                dialog.show();
            });

            eventListView.addView(view);
        }
    }
}