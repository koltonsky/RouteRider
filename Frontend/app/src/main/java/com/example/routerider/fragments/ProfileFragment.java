package com.example.routerider.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.routerider.R;
import com.example.routerider.User;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.Objects;

public class ProfileFragment extends Fragment {


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        GoogleSignInAccount account = User.getCurrentAccount();
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