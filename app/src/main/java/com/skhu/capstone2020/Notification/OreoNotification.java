package com.skhu.capstone2020.Notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import com.skhu.capstone2020.R;

public class OreoNotification extends ContextWrapper {
    private static final String CHANNEL_ID = "skhu.projects.2020_1";
    private static final String CHANNEL_NAME = "LDY";

    private NotificationManager notificationManager;

    public OreoNotification(Context base) {
        super(base);
        createChannel();
    }

    public NotificationManager getManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return notificationManager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(false);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(channel);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getRequestNotificaton(String title, String body,
                                                      PendingIntent pendingIntent, Uri soundUri) {

        return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_main_logo)
                .setSound(soundUri)
                .setAutoCancel(true);
    }
}
