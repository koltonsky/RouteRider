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
    private JSONArray friendRequestList;
    private LinearLayout friendListDisplay;
    private LinearLayout friendRequestDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        GoogleSignInAccount account = User.getCurrentAccount();

        Button addFriend = findViewById(R.id.addFriendButton);
        friendListDisplay = findViewById(R.id.friendList);
        friendRequestDisplay = findViewById(R.id.friendRequestList);
        APICaller apiCall = new APICaller();

        apiCall.APICall("api/userlist/" + account.getEmail() + "/friends", "", APICaller.HttpMethod.GET, new APICaller.ApiCallback() {
            @Override
            public void onResponse(final String responseBody) throws JSONException {
                System.out.println("BODY: " + responseBody);
                JSONObject json = new JSONObject(responseBody);
                friendList = json.getJSONArray("friendsWithNames");
                friendRequestList = json.getJSONArray("friendRequestsWithNames");
                System.out.println(friendRequestList);
                System.out.println(friendList);
                runOnUiThread(() -> {
                    generateFriendList();
                    generateFriendRequestList();
                });
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

//                apiCall.getRecipientFCMToken(recipientEmail, new APICaller.ApiCallback() {
//                    @Override
//                    public void onResponse(String recipientToken) {
//                        // Create a notification payload
//                        Map<String, String> notificationData = new HashMap<>();
//                        notificationData.put("title", "Friend Request");
//                        notificationData.put("body", "You have received a friend request!");
//
//                        // Create the FCM message
//                        Map<String, Object> messageData = new HashMap<>();
//                        messageData.put("data", notificationData);
//                        messageData.put("to", recipientToken);
//
//                        // Send the FCM notification
//                        apiCall.sendFCMNotification(messageData, new APICaller.ApiCallback() {
//                            @Override
//                            public void onResponse(String responseBody) {
//                                System.out.println("Notification sent: " + responseBody);
//                            }
//
//                            @Override
//                            public void onError(String errorMessage) {
//                                System.out.println("Error sending notification: " + errorMessage);
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onError(String errorMessage) {
//                        System.out.println("Error getting recipient's FCM token: " + errorMessage);
//                    }
//                });

                Map<String, Object> map = new HashMap<>();
                map.put("email", userInput);
                String jsonSchedule = new Gson().toJson(map);

                apiCall.APICall("api/userlist/" + account.getEmail() + "/friendRequest", jsonSchedule, APICaller.HttpMethod.POST, new APICaller.ApiCallback() {
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
        friendListDisplay.removeAllViews();
        if (friendList != null) {
            try {
                for (int i = 0; i < friendList.length(); i++) {
                    // Get the JSON object at the current position
                    JSONObject friend = friendList.getJSONObject(i);

                    // Extract email and name from the JSON object
                    String email = friend.getString("email");
                    String name = friend.getString("name");

                    // Create a TextView for each email and name
                    TextView friendTextView = new TextView(this);
                    friendTextView.setText("Name: " + name + " | "+ "Email: " + email);

                    // Add the TextView to the friendListDisplay
                    friendListDisplay.addView(friendTextView);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void generateFriendRequestList() {
        APICaller apiCall = new APICaller();
        friendRequestDisplay.removeAllViews();
        if (friendList != null) {
            try {
                for (int i = 0; i < friendRequestList.length(); i++) {
                    JSONObject friend = friendRequestList.getJSONObject(i);
                    final String email = friend.getString("email");
                    final String name = friend.getString("name");

                    LinearLayout friendLayout = new LinearLayout(this);
                    friendLayout.setOrientation(LinearLayout.VERTICAL);
                    TextView friendTextView = new TextView(this);
                    friendTextView.setText("Name: " + name + " | Email: " + email);
                    LinearLayout buttonLayout = new LinearLayout(this);
                    buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

                    Button acceptButton = new Button(this);
                    acceptButton.setText("Accept");
                    acceptButton.setOnClickListener(view -> {
                        JSONObject friendObject = new JSONObject();
                        try {
                            friendObject.put("name", name);
                            friendObject.put("email", email);
                            friendList.put(friendObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        friendRequestDisplay.removeView(friendLayout);
                        generateFriendList();
                        GoogleSignInAccount account = User.getCurrentAccount();

                        apiCall.APICall("api/userlist/" + account.getEmail() + "/" + email + "/accept", "", APICaller.HttpMethod.POST, new APICaller.ApiCallback() {
                            @Override
                            public void onResponse(String responseBody) {
                                System.out.println("BODY: " + responseBody);
                            }

                            @Override
                            public void onError(String errorMessage) {
                                System.out.println("Error: " + errorMessage);
                            }
                        });
                    });

                    Button declineButton = new Button(this);
                    declineButton.setText("Decline");
                    declineButton.setOnClickListener(view -> {
                        GoogleSignInAccount account = User.getCurrentAccount();
                        apiCall.APICall("api/userlist/" + account.getEmail() + "/" + email + "/decline", "", APICaller.HttpMethod.DELETE, new APICaller.ApiCallback() {
                            @Override
                            public void onResponse(String responseBody) {
                                System.out.println("BODY: " + responseBody);
                            }

                            @Override
                            public void onError(String errorMessage) {
                                System.out.println("Error: " + errorMessage);
                            }
                        });
                        friendRequestDisplay.removeView(friendLayout);
                    });

                    buttonLayout.addView(acceptButton);
                    buttonLayout.addView(declineButton);
                    friendLayout.addView(friendTextView);
                    friendLayout.addView(buttonLayout);
                    friendRequestDisplay.addView(friendLayout);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }




}