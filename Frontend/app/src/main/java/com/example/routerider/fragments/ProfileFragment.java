package com.example.routerider.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.routerider.APICaller;
import com.example.routerider.FriendsActivity;
import com.example.routerider.R;
import com.example.routerider.User;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    public static JSONArray friendList = new JSONArray();
    public static JSONArray friendRequestList = new JSONArray();

    // NO CHATGPT
    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        GoogleSignInAccount account = User.getCurrentAccount();
        APICaller apiCall = new APICaller();
        TextView address = view.findViewById(R.id.profile_address);

        apiCall.APICall("api/userlist/" + account.getEmail(), "", APICaller.HttpMethod.GET, new APICaller.ApiCallback() {
            @Override
            public void onResponse(final String responseBody) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        System.out.println("BODY: " + responseBody);
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            address.setText("Address: " + json.getString("address"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                System.out.println("Error " + errorMessage);
            }
        });

        apiCall.APICall("api/userlist/" + account.getEmail() + "/friends", "", APICaller.HttpMethod.GET, new APICaller.ApiCallback() {
            @Override
            public void onResponse(final String responseBody) throws JSONException {
                System.out.println("BODY: " + responseBody);
                try {
                    JSONObject json = new JSONObject(responseBody);

                    // Check if the "friendsWithNames" and "friendRequestsWithNames" keys exist in the JSON
                    if (json.has("friendsWithNames") && json.has("friendRequestsWithNames")) {
                        friendList = json.getJSONArray("friendsWithNames");
                        friendRequestList = json.getJSONArray("friendRequestsWithNames");

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


        TextView friends = view.findViewById(R.id.friend_list);
        friends.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), FriendsActivity.class);
            startActivity(intent);
        });


        TextView name = view.findViewById(R.id.name_text);
        name.setText("Name: " + account.getDisplayName());
        TextView email = view.findViewById(R.id.email);
        email.setText("Email: " + account.getEmail());

        address.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            View dialogView = getLayoutInflater().inflate(R.layout.update_address_layout, null);
            builder.setView(dialogView);

            builder.setTitle("Enter New Address");
            builder.setPositiveButton("OK", (dialog, which) -> {
                EditText editText = dialogView.findViewById(R.id.update_address);
                String userInput = editText.getText().toString();

                Map<String, Object> newAddress = new HashMap<>();
                newAddress.put("address", userInput);

                apiCall.APICall("api/userlist/" + account.getEmail() + "/address", new Gson().toJson(newAddress), APICaller.HttpMethod.PUT, new APICaller.ApiCallback() {
                    @Override
                    public void onResponse(String responseBody) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                System.out.println("BODY: " + responseBody);
                                try {
                                    JSONObject json = new JSONObject(responseBody);
                                    System.out.println(json.getString("message"));
                                    if(json.getString("message").equals("User address updated successfully")) {
                                        address.setText("Address: " + userInput);
                                    } else {
                                        Toast.makeText(requireContext(), "Failed to get address", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
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


        Button logoutButton = view.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> {
            SharedPreferences preferences = requireActivity().getSharedPreferences("routeRider", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isLoggedIn", false);
            editor.apply();

            requireActivity().finish();
        });

        return view;
    }
}