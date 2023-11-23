package com.example.routerider.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.routerider.APICaller;
import com.example.routerider.CalendarAsyncTask;
import com.example.routerider.CalendarException;
import com.example.routerider.CreateEventTask;
import com.example.routerider.DeleteEventTask;
import com.example.routerider.HelperFunc;
import com.example.routerider.R;
import com.example.routerider.ScheduleItem;
import com.example.routerider.TimeGapRecommendation;
import com.example.routerider.UpdateEventTask;
import com.example.routerider.User;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ScheduleFragment extends Fragment {
    public static List<ScheduleItem> eventList;
    private Date currentDay;
    private TextView currentDayText;
    private DateFormat formatter;
    private Button previousDayButton;
    private static View view;
    private static GoogleSignInAccount account;

    private static Calendar calendarService;
    private static List<ScheduleItem> dayList;

    private static FragmentActivity activity;

    public static void displayGoogleSchedule() {
        Button nextDayButton = view.findViewById(R.id.next_day);
        nextDayButton.setEnabled(true);
        Date today = new Date();
        updateDisplay(today);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    // YES CHATGPT
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_schedule, container, false);
        previousDayButton = view.findViewById(R.id.previous_day);
        previousDayButton.setEnabled(false);
        Button nextDayButton = view.findViewById(R.id.next_day);
        nextDayButton.setEnabled(false);
        account = User.getCurrentAccount();
        currentDay = new Date();
        formatter = new SimpleDateFormat("E, dd MMM");
        currentDayText = view.findViewById(R.id.current_day_text);
        currentDayText.setText(formatter.format(currentDay));
        FloatingActionButton addEvent = view.findViewById(R.id.floating_action_button);


        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                requireContext(), Collections.singleton(CalendarScopes.CALENDAR));
        credential.setSelectedAccount(account.getAccount());

        try {
            calendarService = new Calendar.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), credential)
                    .setApplicationName("RouteRider")
                    .build();
        } catch (GeneralSecurityException | IOException gse) {
            throw new CalendarException("An error occurred while setting up the calendar service", gse);
        }

        new CalendarAsyncTask(account).execute(calendarService);

        previousDayButton.setOnClickListener(v -> {
            getDay(-1);
        });

        nextDayButton.setOnClickListener(v -> {
            getDay(1);
        });

        addEvent.setOnClickListener(v -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());
            alertDialogBuilder.setTitle("Add Event");

            // Inflate the custom view for the dialog
            LayoutInflater dialogInflater = LayoutInflater.from(requireContext());
            View dialogView = dialogInflater.inflate(R.layout.add_event_dialog, null);
            alertDialogBuilder.setView(dialogView);

            final EditText eventNameEditText = dialogView.findViewById(R.id.event_name);
            final EditText eventAddressEditText = dialogView.findViewById(R.id.event_address);
            final EditText eventStartTimeEditText = dialogView.findViewById(R.id.event_start_time);
            final EditText eventEndTimeEditText = dialogView.findViewById(R.id.event_end_time);

            final EditText dateEditText = dialogView.findViewById(R.id.date_edit_text);
            dateEditText.setOnClickListener(vw -> showDatePicker(dateEditText, requireContext()));
            eventStartTimeEditText.setOnClickListener(vw -> showTimePicker(eventStartTimeEditText, requireContext()));
            eventEndTimeEditText.setOnClickListener(vw -> showTimePicker(eventEndTimeEditText, requireContext()));

            alertDialogBuilder.setPositiveButton("OK", null);

            alertDialogBuilder.setNegativeButton("Cancel", (dialog, which) -> {
                dialog.dismiss();
            });

            final AlertDialog dialog = alertDialogBuilder.create();
            dialog.setOnShowListener(dialogInterface -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view1 -> {
                String eventName = eventNameEditText.getText().toString();
                String eventAddress = eventAddressEditText.getText().toString();
                String eventDate = dateEditText.getText().toString();
                String eventStartTime = eventStartTimeEditText.getText().toString();
                String eventEndTime = eventEndTimeEditText.getText().toString();

                if (ScheduleFragment.EventInputListener.onEventInput(eventName, eventAddress, eventDate, eventStartTime, eventEndTime)) {
                    new CreateEventTask(calendarService, eventName, eventAddress, eventDate, eventStartTime, eventEndTime).execute();

                    ScheduleItem newEvent = new ScheduleItem(
                            eventName,
                            eventAddress,
                            eventDate + "T" + eventStartTime + ":00",
                            eventDate + "T" + eventEndTime + ":00",
                            HelperFunc.generateRandomString(64),
                            "primary");

                    String jsonUpdateEvent = new Gson().toJson(newEvent);
                    APICaller apiCall = new APICaller();
                    apiCall.APICall("api/schedulelist/" + account.getEmail(), jsonUpdateEvent, APICaller.HttpMethod.POST, new APICaller.ApiCallback() {
                        @Override
                        public void onResponse(String responseBody) {
                            System.out.println("BODY: " + responseBody);
                            new CalendarAsyncTask(account).execute(calendarService);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            System.out.println("Error " + errorMessage);
                        }
                    });

                    dialog.dismiss();
                } else {
                    //Toast.makeText(activity.getApplicationContext(), "Please input proper time", Toast.LENGTH_LONG).show();
                }
            }));

            dialog.show();

        });

        return view;
    }


    private void getDay(int gap){
        java.util.Calendar calendar =  java.util.Calendar.getInstance();
        calendar.setTime(currentDay);
        calendar.add( java.util.Calendar.DAY_OF_YEAR, gap);
        Date day = calendar.getTime();
        changeDay(day);
    }
    // NO CHATGPT
    private void changeDay(Date day){
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (sdf.format(today).equals(sdf.format(day))) {
            previousDayButton.setEnabled(false);
        } else {
            previousDayButton.setEnabled(true);
        }
        currentDay = day;
        currentDayText.setText(formatter.format(day));
        updateDisplay(day);
    }

    // YES CHATGPT
    public interface EventInputListener {
        static boolean onEventInput(String eventName, String eventAddress, String eventDate, String eventStartTime, String eventEndTime) {
            return validateInputs(eventName, eventAddress, eventDate, eventStartTime, eventEndTime);
        }
    }

    // YES CHATGPT
    private static boolean validateInputs(String eventName, String eventAddress, String eventDate, String eventStartTime, String eventEndTime) {
        if (eventName.isEmpty() || eventAddress.isEmpty() || eventDate.isEmpty() || eventStartTime.isEmpty() || eventEndTime.isEmpty()) {
            // Check if any field is empty
            Toast.makeText(activity.getApplicationContext(), "Please fill out all fields", Toast.LENGTH_LONG).show();

            return false;
        }

        if (!isDateValid((eventDate + " " + eventStartTime)) || !isTimeValid(eventStartTime) || !isTimeValid(eventEndTime)) {
            // Check if date and time formats are valid
            Toast.makeText(activity.getApplicationContext(), "Please input proper time", Toast.LENGTH_LONG).show();
            return false;
        }

        return isStartTimeBeforeEndTime(eventStartTime, eventEndTime);
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
        } catch (ParseException e) {
            // Parsing failed, the times are not valid
            return false;
        }
    }

    private static void parseDayList(String dateString) {
        dayList = new ArrayList<>();
        for (ScheduleItem item : eventList) {
            String itemDay = item.getStartTime().substring(0, 10);
            if (dateString.equals(itemDay)) {
                dayList.add(item);
            }
        }
    }
    public static void updateDisplay(Date day) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = dateFormat.format(day);
        parseDayList(dateString);
        LinearLayout eventListView = view.findViewById(R.id.schedule_view);
        TextView emptyListText = view.findViewById(R.id.empty_text);
        eventListView.removeAllViewsInLayout();
        if (dayList.size() > 0) {
            emptyListText.setVisibility(View.GONE);
        } else {
            emptyListText.setVisibility(View.VISIBLE);
        }

        LayoutInflater inflater = LayoutInflater.from(view.getContext());

        View previousEventView = null;

        for (ScheduleItem item : dayList) {
            eventListView = view.findViewById(R.id.schedule_view);
            View view = inflater.inflate(R.layout.view_event, eventListView, false);

            TextView eventName = view.findViewById(R.id.event_name);
            eventName.setText(item.getTitle());

            TextView eventLocation = view.findViewById(R.id.event_location);
            eventLocation.setText(item.getLocation());

            TextView startTime = view.findViewById(R.id.start_time);
            startTime.setText(item.getStartTime().substring(11, 16));

            TextView endTime = view.findViewById(R.id.end_time);
            endTime.setText(item.getEndTime().substring(11, 16));
            if (previousEventView != null) {
                fetchTimeGapRecommendations(eventListView, item);
            }

            endTime.setText(item.getEndTime().substring(11, 16));
            System.out.println(item);

            view.setOnClickListener(v -> {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
                alertDialogBuilder.setTitle("Update Event");

                // Inflate the custom view for the dialog
                LayoutInflater dialogInflater = LayoutInflater.from(view.getContext());
                View dialogView = dialogInflater.inflate(R.layout.add_event_dialog, null);
                alertDialogBuilder.setView(dialogView);

                final EditText eventNameEditText = dialogView.findViewById(R.id.event_name);
                final EditText eventAddressEditText = dialogView.findViewById(R.id.event_address);
                final EditText eventStartTimeEditText = dialogView.findViewById(R.id.event_start_time);
                final EditText eventEndTimeEditText = dialogView.findViewById(R.id.event_end_time);

                final EditText dateEditText = dialogView.findViewById(R.id.date_edit_text);
                dateEditText.setOnClickListener(vw -> showDatePicker(dateEditText, view.getContext()));
                eventStartTimeEditText.setOnClickListener(vw -> showTimePicker(eventStartTimeEditText, view.getContext()));
                eventEndTimeEditText.setOnClickListener(vw -> showTimePicker(eventEndTimeEditText, view.getContext()));
                dateEditText.setText(item.getStartTime().substring(0, 10));

                eventNameEditText.setText(item.getTitle());
                eventAddressEditText.setText(item.getLocation());
                eventStartTimeEditText.setText(item.getStartTime().substring(11, 16));
                eventEndTimeEditText.setText(item.getEndTime().substring(11, 16));


                alertDialogBuilder.setPositiveButton("OK", null);

                alertDialogBuilder.setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                });

                final AlertDialog dialog = alertDialogBuilder.create();
                dialog.setOnShowListener(dialogInterface -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view1 -> {
                    String updateEventName = eventNameEditText.getText().toString();
                    String eventAddress = eventAddressEditText.getText().toString();
                    String eventDate = dateEditText.getText().toString();
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

                        new UpdateEventTask(calendarService, newEvent).execute();

                        dialog.dismiss();
                    } else {
                        Toast.makeText(view.getContext(), "Invalid inputs, please try again", Toast.LENGTH_SHORT).show();
                    }
                }));

                dialog.show();
            });

            LinearLayout finalEventListView = eventListView;
            view.setOnLongClickListener(v -> {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
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
                    new DeleteEventTask(calendarService, item, activity.getApplicationContext()).execute();
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
    private static void fetchTimeGapRecommendations(LinearLayout eventListView, ScheduleItem item) {
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        LayoutInflater inflater = LayoutInflater.from(view.getContext());

        long fifteenMinutesInMillis = 15 * 60 * 1000; // 15 minutes in milliseconds
        try {
            Date previousEndTime = fullDateFormat.parse(dayList.get(dayList.indexOf(item) - 1).getEndTime());
            Date currentStartTime = fullDateFormat.parse(item.getStartTime());
            long timeDifference = currentStartTime.getTime() - previousEndTime.getTime();

            if (timeDifference > fifteenMinutesInMillis) {
                View timeGapView = inflater.inflate(R.layout.timegap_chip, eventListView, false);
                TextView timeGapTextView = timeGapView.findViewById(R.id.time_gap_text);
                timeGapTextView.setText("Gap: " + formatTimeDifference(timeDifference));
                LinearLayout hiddenView = timeGapView.findViewById(R.id.hidden_timegap);
                CardView cardView = timeGapView.findViewById(R.id.base_timegap);
                Button viewRecommendationsButton = timeGapView.findViewById(R.id.view_recommendations_button);
                viewRecommendationsButton.setOnClickListener(v -> {
                    if (hiddenView.getVisibility() == View.VISIBLE) {
                        TransitionManager.beginDelayedTransition(cardView, new AutoTransition());
                        hiddenView.setVisibility(View.GONE);
                        viewRecommendationsButton.setText("Show Recommendations");
                    } else {
                        ArrayList<TimeGapRecommendation> recs = new ArrayList<>();
                        hiddenView.removeAllViewsInLayout();
                        APICaller apiCall = new APICaller();
                        apiCall.APICall("api/recommendation/timegap/" + item.getLocation() + "/" + dayList.get(dayList.indexOf(item) - 1).getLocation(), "", APICaller.HttpMethod.GET, new APICaller.ApiCallback() {

                            @Override
                            public void onResponse(String responseBody) {
                                System.out.println("BODY: " + responseBody);
                                updateRecommendationLayout(responseBody, recs, inflater, viewRecommendationsButton, hiddenView, cardView);
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

    // NO CHATGPT
    public static void updateRecommendationLayout(String responseBody, ArrayList<TimeGapRecommendation> recs, LayoutInflater inflater, Button viewRecommendationsButton, LinearLayout hiddenView, CardView cardView) {
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
                        break;
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
                TextView recNameText = timeGapRecView.findViewById(R.id.rec_name);
                recNameText.setText(rec.getName());
                TextView recAddressText = timeGapRecView.findViewById(R.id.rec_address);
                recAddressText.setText(rec.getAddress());
                ImageButton recMapsButton = timeGapRecView.findViewById(R.id.maps_button);
                recMapsButton.setOnClickListener(v2 -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("google.navigation:q=" + rec.getAddress()));
                    view.getContext().startActivity(intent);
                });
                activity.runOnUiThread( () -> {
                    hiddenView.addView(timeGapRecView);
                });
            }
            TransitionManager.beginDelayedTransition(cardView, new AutoTransition());
            activity.runOnUiThread(() -> {
                        hiddenView.setVisibility(View.VISIBLE);
                    });
            viewRecommendationsButton.setText("Hide Recommendations");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    // YES CHATGPT
    private static String formatTimeDifference(long timeDifference) {
        long minutes = (timeDifference / (1000 * 60)) % 60;
        long hours = (timeDifference / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d", hours, minutes);
    }

    private static void showDatePicker(final EditText date, Context context) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int currentYear = calendar.get(java.util.Calendar.YEAR);
        int currentMonth = calendar.get(java.util.Calendar.MONTH);
        int currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog with the current date as the initial date
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, year, month, dayOfMonth) -> {
                    // Handle the selected date
                    String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                    date.setText(selectedDate);
                },
                currentYear,
                currentMonth,
                currentDay
        );

        // Set the minimum date to the current date
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        // Show the DatePickerDialog
        datePickerDialog.show();
    }

    private static void showTimePicker(final EditText time, Context context) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(java.util.Calendar.MINUTE);

        // Create a TimePickerDialog with the current time as the initial time
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                context,
                (view, hourOfDay, minute) -> {
                    // Handle the selected time
                    String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    time.setText(selectedTime);
                },
                currentHour,
                currentMinute,
                true // Set to true for 24-hour format
        );

        // Show the TimePickerDialog
        timePickerDialog.show();
    }


}