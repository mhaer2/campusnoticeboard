package de.haertel.hawapp.campusnoticeboard.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Klasse, die syncronisierten Zugriff auf eine statische Variable ermöglicht
 * und somit von überall aus der Applikation Zugriff gewährt.
 * So auch außerhalb von Activities, wo kein Contexxt verfügbar ist.
 * Des Weiteren können Listener eingehangen werden, die darauf hören, ob sich die Variable ändert.
 * In der Variable wird gespeichert, welcher User der aktuell angemeldete ist.
 */
public class CurrentUser {


    private static final String PROPERTY_NAME = "username";
    private static String username;
    private static PropertyChangeSupport propertyChangeSupport =
            new PropertyChangeSupport(AnnouncementTopic.class);

    /**
     * Fügt einen Listener hinzu.
     *
     * @param listener der Listener
     */
    public static void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * @return den Namen des Users
     */
    public static synchronized String getUsername() {
        return username;
    }

    /**
     * Setzt die Property und feuert die Listener.
     *
     * @param pUserName der Name der gesetzt werden soll.
     */
    public static synchronized void setUsername(String pUserName) {
        String oldUsername = CurrentUser.getUsername();
        CurrentUser.username = pUserName;
        propertyChangeSupport.firePropertyChange(PROPERTY_NAME, oldUsername, CurrentUser.username);
    }
}
