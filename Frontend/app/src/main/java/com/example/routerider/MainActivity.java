package com.example.routerider;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private final int RC_SIGN_IN = 1;
    private Calendar service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref = getSharedPreferences("routeRider", Context.MODE_PRIVATE);
        if(pref.getBoolean("isLoggedIn", false)) {
            GoogleSignInOptions googleSignIn = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(new Scope(CalendarScopes.CALENDAR_READONLY))
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignIn);
            signIn();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(v -> {
            GoogleSignInOptions googleSignIn = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(new Scope(CalendarScopes.CALENDAR_READONLY))
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignIn);
            signIn();
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN) {
            if(resultCode == RESULT_OK) {
                GoogleSignIn.getSignedInAccountFromIntent(data)
                        .addOnSuccessListener(this::handleSignInSuccess);
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to login", Toast.LENGTH_SHORT).show();
        }
    }

//    private void handleSignInSuccess(GoogleSignInAccount account) {
//        User.updateGoogleAccount(account);
//        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
//                this, Collections.singleton(CalendarScopes.CALENDAR_READONLY));
//        credential.setSelectedAccount(account.getAccount());
//
//        Calendar service = null;
//        try {
//            service = new Calendar.Builder(
//                    GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), credential)
//                    .setApplicationName("RouteRider")
//                    .build();
//
//            // Attempt to access Google Calendar API
//            try {
//                CalendarList calendarList = service.calendarList().list().execute();
//                List<CalendarListEntry> items = calendarList.getItems();
//
//                if (items.isEmpty()) {
//                    System.out.println("No calendars found.");
//                } else {
//                    System.out.println("Calendars:");
//
//                    for (CalendarListEntry calendarEntry : items) {
//                        String calendarId = calendarEntry.getId();
//                        String summary = calendarEntry.getSummary();
//
//                        System.out.println("Calendar ID: " + calendarId);
//                        System.out.println("Summary: " + summary);
//                        System.out.println();
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                // Handle the network-related exception
//                runOnUiThread(() -> {
//                    Toast.makeText(getApplicationContext(), "Failed to retrieve calendar data. Please try again.", Toast.LENGTH_SHORT).show();
//                });
//            }
//        } catch (GeneralSecurityException | IOException e) {
//            e.printStackTrace();
//            // Handle the security exception
//            runOnUiThread(() -> {
//                Toast.makeText(getApplicationContext(), "Security exception. Please try again.", Toast.LENGTH_SHORT).show();
//            });
//        }
//
//        // The rest of your code
//        SharedPreferences preferences = getSharedPreferences("routeRider", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putBoolean("isLoggedIn", true);
//        editor.apply();
//
//        Intent intent = new Intent(this, HomeActivity.class);
//        startActivity(intent);
//    }

    private void handleSignInSuccess(GoogleSignInAccount account) {
        User.updateGoogleAccount(account);

        // The rest of your code
        SharedPreferences preferences = getSharedPreferences("routeRider", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.apply();

        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }



}