package de.haertel.hawapp.campusnoticeboard.impl;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import de.haertel.hawapp.campusnoticeboard.R;

public class BaseApp extends Application {
    public static final String CHANNEL_1_ID = "channel1";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    getString(R.string.channel1_name),
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription(getString(R.string.channel1_description));

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }
}