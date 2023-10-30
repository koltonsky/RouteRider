package com.example.routerider;

import com.google.firebase.messaging.FirebaseMessagingService;

public class FirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onNewToken(String token) {
//        Log.d(TAG, "Refreshed token: " + token);
//
//         If you want to send messages to this application instance or
//         manage this apps subscriptions on the server side, send the
//         FCM registration token to your app server.
//        sendRegistrationToServer(token);
    }
}
