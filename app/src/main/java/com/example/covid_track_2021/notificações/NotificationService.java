package com.example.covid_track_2021.notificações;

import static android.app.NotificationManager.IMPORTANCE_HIGH;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import com.example.covid_track_2021.R;

public class NotificationService implements NotificationHandler {
    private NotificationManager mManager;
    public static final String ANDROID_CHANNEL_ID = "Beacon Reference Notifications";
    public static final String ANDROID_CHANNEL_NAME = "Beacon Reference Notifications";
    public String message = "";

    public NotificationService(NotificationManager mManager) {
        this.mManager = mManager;

        createNotificationChannel();
    }

    @Override
    public void sendNotification(Context context) {
        Notification nb = createNotification(context, message);
        mManager.notify(101, nb);
    }

    public Notification createNotification(Context context, String notificationText) {
        NotificationService createNotification = new NotificationService(mManager);
        //Notification.Builder nb = createNotification.getAndroidChannelNotification(notificationText, "", context).setSmallIcon(R.drawable.ic_action_warning);
        Notification.Builder nb = createNotification.getAndroidChannelNotification(notificationText, "", context);
        return nb.build();
    }

    @Override
    public Notification.Builder getAndroidChannelNotification(String title, String body, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Notification.Builder(context, ANDROID_CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setSmallIcon(android.R.drawable.stat_notify_more)
                    .setAutoCancel(true);

        }
        return null;
    }

    private void createNotificationChannel() {
        // create android channel
        NotificationChannel androidChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            androidChannel = new NotificationChannel(ANDROID_CHANNEL_ID,
                    ANDROID_CHANNEL_NAME, IMPORTANCE_HIGH);
            // Sets whether notifications posted to this channel should display notification lights
            androidChannel.enableLights(true);
            // Sets whether notification posted to this channel should vibrate.
            androidChannel.enableVibration(true);
            // Sets the notification light color for notifications posted to this channel
            androidChannel.setLightColor(Color.GREEN);
            // Sets whether notifications posted to this channel appear on the lockscreen or not
            androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            mManager.createNotificationChannel(androidChannel);
        }
    }

    public void setMessage(String message) {
        this.message = message;
    }
}