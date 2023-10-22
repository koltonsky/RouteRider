package com.example.routerider;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class MainActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private final int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref = getSharedPreferences("routeRider", Context.MODE_PRIVATE);
        if(pref.getBoolean("isLoggedIn", false)) {
            GoogleSignInOptions googleSignIn = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
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

    private void handleSignInSuccess(GoogleSignInAccount account) {
        User.updateGoogleAccount(account);

        SharedPreferences preferences = getSharedPreferences("routeRider", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.apply();

        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
}