package com.example.routerider;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class PushNotificationService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String token) {
        // Get the updated FCM registration token
        // You can send this token to your server for later use
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // Send the token to your Azure server or store it locally
        // You may associate it with the user's account if needed
    }

}
