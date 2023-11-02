package com.example.routerider;

import static java.util.Collections.emptyList;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private final int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //FirebaseMessaging.getInstance().subscribeToTopic("RouteRider");
        googleSignIn(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        googleSignIn(false);
    }

    private void googleSignIn(boolean autoSignIn) {
        SharedPreferences pref = getSharedPreferences("routeRider", Context.MODE_PRIVATE);
        if(pref.getBoolean("isLoggedIn", false) && autoSignIn) {
            GoogleSignInOptions googleSignIn = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(new Scope(CalendarScopes.CALENDAR))
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignIn);
            signIn();
        } else {
            RelativeLayout mainLayout = findViewById(R.id.main_display);
            mainLayout.setVisibility(View.VISIBLE);
            ProgressBar loadingAnimation = findViewById(R.id.loadingBar);
            loadingAnimation.setVisibility(View.GONE);

            Button loginButton = findViewById(R.id.login_button);
            loginButton.setOnClickListener(v -> {
                GoogleSignInOptions googleSignIn = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(new Scope(CalendarScopes.CALENDAR))
                        .requestEmail()
                        .build();

                mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignIn);
                signIn();
            });
        }
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

    private void handleSignInSuccess(GoogleSignInAccount account) {
        APICaller apiCall = new APICaller();
        User.updateGoogleAccount(account);
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("email", account.getEmail());
        user.put("name", account.getDisplayName());
        user.put("address", "");
        user.put("friends", emptyList());
        user.put("friendRequests", emptyList());
        String jsonStr = new Gson().toJson(user);
        //System.out.println(jsonStr);


        apiCall.APICall("api/userlist", jsonStr, APICaller.HttpMethod.POST, new APICaller.ApiCallback() {
            @Override
            public void onResponse(String responseBody) {
                System.out.println("BODY: " + responseBody);
            }

            @Override
            public void onError(String errorMessage) {
                System.out.println("Error: " + errorMessage);
            }
        });


        SharedPreferences preferences = getSharedPreferences("routeRider", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.apply();

        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
}
