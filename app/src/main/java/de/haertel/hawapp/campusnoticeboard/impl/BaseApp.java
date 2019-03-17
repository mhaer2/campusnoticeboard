package de.haertel.hawapp.campusnoticeboard.impl;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import de.haertel.hawapp.campusnoticeboard.R;

/**
 * Basis-Implementierung der Applikation, die NotificationsChannels hinzufügt,
 * welche für die Push-Benachrichtigungen benötigt werden.
 */
public class BaseApp extends Application {
    public static final String CHANNEL_1_ID = "channel1";

    /**
     * Die onCreate Methode, die neben den gewöhnlichen Aufrufen
     * auch das kreieren der Channels aufruft.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    /**
     * Methode, die einen NotificationChannel erstellt
     */
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