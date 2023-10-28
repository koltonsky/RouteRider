package com.example.routerider.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.routerider.APICaller;
import com.example.routerider.FriendsActivity;
import com.example.routerider.HomeActivity;
import com.example.routerider.PreferencesActivity;
import com.example.routerider.R;
import com.example.routerider.User;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.Objects;

public class ProfileFragment extends Fragment {


    public ProfileFragment() {
        // Required empty public constructor
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        GoogleSignInAccount account = User.getCurrentAccount();
        APICaller apiCall = new APICaller();


        TextView friends = view.findViewById(R.id.friendList);
        friends.setOnClickListener(v -> {
            apiCall.APICall("/api/userlist", "", APICaller.HttpMethod.GET, new APICaller.ApiCallback() {

                @Override
                public void onResponse(String responseBody) {
                    System.out.println("BODY: " + responseBody);
                }

                @Override
                public void onError(String errorMessage) {
                    System.out.println("Error " + errorMessage);
                }
            });
//            Intent intent = new Intent(requireContext(), FriendsActivity.class);
//            startActivity(intent);
        });

        TextView userPreferences = view.findViewById(R.id.preferences);
        userPreferences.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PreferencesActivity.class);
            startActivity(intent);
        });

        TextView name = view.findViewById(R.id.name_text);
        name.setText("Name: " + account.getDisplayName());
        TextView email = view.findViewById(R.id.email);
        email.setText("Email: " + account.getEmail());

        Button logoutButton = view.findViewById(R.id.logoutButton);
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