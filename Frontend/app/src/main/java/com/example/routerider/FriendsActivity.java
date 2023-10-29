package com.example.routerider;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FriendsActivity extends AppCompatActivity {

    private JSONArray friendList;
    private LinearLayout friendListDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        GoogleSignInAccount account = User.getCurrentAccount();

        Button addFriend = findViewById(R.id.addFriendButton);
        friendListDisplay = findViewById(R.id.friendList);
        APICaller apiCall = new APICaller();

        apiCall.APICall("api/userlist/" + account.getEmail() + "/friends", "", APICaller.HttpMethod.GET, new APICaller.ApiCallback() {
            @Override
            public void onResponse(final String responseBody) {
                System.out.println("BODY: " + responseBody);
                try {
                    friendList = new JSONArray(responseBody);
                    runOnUiThread(() -> {
                        generateFriendList();
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String errorMessage) {
                System.out.println("Error " + errorMessage);
            }
        });


        addFriend.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.add_friend_layout, null);
            builder.setView(dialogView);

            builder.setTitle("Enter your friend's email");
            builder.setPositiveButton("OK", (dialog, which) -> {
                EditText editText = dialogView.findViewById(R.id.addFriendEmail);
                String userInput = editText.getText().toString();

                Map<String, Object> map = new HashMap<>();
                map.put("email", userInput);
                String jsonSchedule = new Gson().toJson(map);

                apiCall.APICall("api/userlist/" + account.getEmail() + "/friends", jsonSchedule, APICaller.HttpMethod.POST, new APICaller.ApiCallback() {
                    @Override
                    public void onResponse(final String responseBody) {
                        System.out.println("BODY: " + responseBody);

                    }

                    @Override
                    public void onError(String errorMessage) {
                        System.out.println("Error " + errorMessage);
                    }
                });

            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    public void generateFriendList() {
        // Clear the existing views in the friendListDisplay
        friendListDisplay.removeAllViews();

        if (friendList != null) {
            try {
                for (int i = 0; i < friendList.length(); i++) {
                    // Get the email from the JSON array
                    String email = friendList.getString(i);

                    // Create a TextView for each email
                    TextView emailTextView = new TextView(this);
                    emailTextView.setText(email);

                    // Add the TextView to the friendListDisplay
                    friendListDisplay.addView(emailTextView);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}