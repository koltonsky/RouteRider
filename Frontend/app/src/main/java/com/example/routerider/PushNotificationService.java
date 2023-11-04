package com.example.routerider;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class PushNotificationService extends FirebaseMessagingService {
    // YES CHATGPT
    @Override
    public void onNewToken(String token) {
        // Get the updated FCM registration token
        // You can send this token to your server for later use
        sendRegistrationToServer(token);
    }

    // YES CHATGPT
    public void sendRegistrationToServer(String token) {
        GoogleSignInAccount account = User.getCurrentAccount();
        APICaller apiCall = new APICaller();
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("email", account.getEmail());
        String jsonToken = new Gson().toJson(map);

        apiCall.APICall("api/store_token", jsonToken, APICaller.HttpMethod.POST, new APICaller.ApiCallback() {
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
}
