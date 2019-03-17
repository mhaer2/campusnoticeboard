package de.haertel.hawapp.campusnoticeboard.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Klasse, die syncronisierten Zugriff auf eine statische Variable ermöglicht
 * und somit von überall aus der Applikation Zugriff gewährt.
 * So auch außerhalb von Activities, wo kein Contexxt verfügbar ist.
 * Des Weiteren können Listener eingehangen werden, die darauf hören, ob sich die Variable ändert.
 * In der Variable wird gespeichert, welches Topic (Iot, Informatik, M.Sc. usw.) aktuell gesetzt ist.
 */
public class AnnouncementTopic {
    private static String topic;
    private static final String PROPERTY_NAME = "topic";

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
     * Entfernt einen hinzugefügten Listener
     *
     * @param listener der Listener der entfernt werden soll.
     */
    public static void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * @return das aktuelle Topic
     */
    public static synchronized String getTopic() {
        return topic;
    }

    /**
     * Setzt die Property und feuert die Listener.
     *
     * @param pTopic das Topic das gesetzt werden soll
     */
    public static synchronized void setTopic(String pTopic) {
        String oldTopic = AnnouncementTopic.topic;
        AnnouncementTopic.topic = pTopic;
        propertyChangeSupport.firePropertyChange(PROPERTY_NAME, oldTopic, AnnouncementTopic.topic);
    }

    /**
     * Methode die das Topic setzt. Wird benötigt beim initialen anlegen der Datenbank.
     * Hier wird eine Änderung des Topics simuliert, aufgrund dieser Listener getriggert werden.
     *
     * @param pTopic das Topic das gesetzt werden soll.
     */
    public static synchronized void initTopic(String pTopic) {
        AnnouncementTopic.topic = "none";
        AnnouncementTopic.setTopic(pTopic);
    }


}
