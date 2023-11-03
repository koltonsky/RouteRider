package com.example.routerider;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
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

    public static JSONArray friendList;
    private JSONArray friendRequestList;
    private LinearLayout friendListDisplay;
    private LinearLayout friendRequestDisplay;

    public static JSONArray getFriendList() {
        if (friendList != null) {
            return friendList;
        } else {
            return new JSONArray();
        }
    }

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
                try {
                    JSONObject json = new JSONObject(responseBody);

                    // Check if the "friendsWithNames" and "friendRequestsWithNames" keys exist in the JSON
                    if (json.has("friendsWithNames") && json.has("friendRequestsWithNames")) {
                        JSONArray friendList = json.getJSONArray("friendsWithNames");
                        JSONArray friendRequestList = json.getJSONArray("friendRequestsWithNames");

                        // Check if the arrays are empty
                        if (friendList.length() > 0) {
                            System.out.println(friendList);
                        } else {
                            System.out.println("Friend list is empty.");
                        }

                        if (friendRequestList.length() > 0) {
                            System.out.println(friendRequestList);
                        } else {
                            System.out.println("Friend request list is empty.");
                        }

                        runOnUiThread(() -> {
                            generateFriendList(friendList);
                            generateFriendRequestList();
                        });
                    } else {
                        System.out.println("The JSON object doesn't contain the expected keys.");
                    }
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
                String jsonEmail = new Gson().toJson(map);

                apiCall.APICall("api/userlist/" + account.getEmail() + "/friendRequest", jsonEmail, APICaller.HttpMethod.POST, new APICaller.ApiCallback() {
                    @Override
                    public void onResponse(final String responseBody) {
                        System.out.println("BODY: " + responseBody);

                        Map<String, Object> requestMap = new HashMap<>();
                        requestMap.put("receiverEmail", userInput);
                        requestMap.put("senderName", account.getDisplayName());
                        String requestJson = new Gson().toJson(requestMap);
                        apiCall.APICall("api/send-friend-notification", requestJson, APICaller.HttpMethod.POST, new APICaller.ApiCallback() {
                            @Override
                            public void onResponse(String responseBody) {
                                System.out.println("BODY: " + responseBody);
                            }

                            @Override
                            public void onError(String errorMessage) {
                                System.out.println("Error: " + errorMessage);
                            }
                        });
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

    public void generateFriendList(JSONArray newFriendList) {
        System.out.println("here");
        System.out.println(newFriendList);
        friendListDisplay.removeAllViews();
        if (newFriendList != null) {
            try {
                for (int i = 0; i < newFriendList.length(); i++) {
                    JSONObject friend = newFriendList.getJSONObject(i);

                    String email = friend.getString("email");
                    String name = friend.getString("name");
                    TextView friendTextView = new TextView(this);
                    System.out.println("displaying friend");
                    System.out.println ( "Name: " + name + " | "+ "Email: " + email);
                    friendTextView.setText("Name: " + name + " | "+ "Email: " + email);

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
                        generateFriendList(friendList);
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