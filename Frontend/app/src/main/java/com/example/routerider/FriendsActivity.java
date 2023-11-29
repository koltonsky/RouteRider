package com.example.routerider;

import static com.example.routerider.FriendsActivity.sendFriendRequest;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.routerider.fragments.ProfileFragment;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FriendsActivity extends AppCompatActivity {


    private LinearLayout friendListDisplay;
    private LinearLayout friendRequestDisplay;

    // NO CHATGPT
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        Button addFriend = findViewById(R.id.add_friend_button);
        friendListDisplay = findViewById(R.id.friend_list);
        friendRequestDisplay = findViewById(R.id.friend_request_list);
        generateFriendList();
        generateFriendRequestList();

        addFriend.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.add_friend_layout, null);
            builder.setView(dialogView);

            builder.setTitle("Enter your friend's email");
            builder.setPositiveButton("OK", (dialog, which) -> {
                EditText editText = dialogView.findViewById(R.id.add_friend_email);
                String userInput = editText.getText().toString();

                sendFriendRequest(userInput, (String message) -> {
                    runOnUiThread(() -> {
                        Toast.makeText(FriendsActivity.this, message, Toast.LENGTH_SHORT).show();
                    });
                });
            });
                
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            AlertDialog dialog = builder.create();
            dialog.show();
        });
    };

    public static void sendFriendRequest(String userInput, FriendRequestErrorCallback errorCallback) {

        Map<String, Object> map = new HashMap<>();
        map.put("email", userInput);
        String jsonEmail = new Gson().toJson(map);
        APICaller apiCall = new APICaller();
        GoogleSignInAccount account = User.getCurrentAccount();
        apiCall.APICall("api/userlist/" + account.getEmail() + "/friendRequest", jsonEmail, APICaller.HttpMethod.POST, new APICaller.ApiCallback() {
            @Override
            public void onResponse(final String responseBody) throws JSONException {
                System.out.println("BODY: " + responseBody);
                JSONObject json = new JSONObject(responseBody);
                String message = json.getString("message");

                if(message.equals("Friend request sent successfully")) {
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
                } else {
                   errorCallback.execute(message);
                }
            }

            @Override
            public void onError(String errorMessage) {
                System.out.println("Error " + errorMessage);
            }
        });
    }

    // NO CHATGPT
    public void generateFriendList() {
        friendListDisplay.removeAllViews();
        if (ProfileFragment.friendList != null) {
            try {
                for (int i = 0; i < ProfileFragment.friendList.length(); i++) {
                    JSONObject friend = ProfileFragment.friendList.getJSONObject(i);

                    String email = friend.getString("email");
                    String name = friend.getString("name");
                    TextView friendTextView = new TextView(this);
                    friendTextView.setText("Name: " + name + " | "+ "Email: " + email);

                    friendListDisplay.addView(friendTextView);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // NO CHATGPT
    public void generateFriendRequestList() {
        APICaller apiCall = new APICaller();
        friendRequestDisplay.removeAllViews();
        if (ProfileFragment.friendList != null) {
            try {
                for (int i = 0; i < ProfileFragment.friendRequestList.length(); i++) {
                    JSONObject friend = ProfileFragment.friendRequestList.getJSONObject(i);
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
                            ProfileFragment.friendList.put(friendObject);
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