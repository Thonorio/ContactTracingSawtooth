package com.example.covid_track_2021.notificações;

import android.app.Notification;
import android.content.Context;

public interface NotificationHandler {
    void sendNotification(Context context);

    Notification.Builder getAndroidChannelNotification(String title, String body, Context context);
}
