package de.haertel.hawapp.campusnoticeboard.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class CurrentUser {

    private static String username;

    private static PropertyChangeSupport propertyChangeSupport =
            new PropertyChangeSupport(AnnouncementTopic.class);

    public static void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public static synchronized String getUsername() {
        return username;
    }

    public static synchronized void setUsername(String pUserName) {
        String oldUsername = CurrentUser.getUsername();
        CurrentUser.username = pUserName;
        propertyChangeSupport.firePropertyChange("username", oldUsername, CurrentUser.username);
    }
}
