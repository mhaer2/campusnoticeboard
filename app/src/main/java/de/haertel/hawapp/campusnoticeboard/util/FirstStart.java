package de.haertel.hawapp.campusnoticeboard.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Klasse, die syncronisierten Zugriff auf eine statische Variable ermöglicht
 * und somit von überall aus der Applikation Zugriff gewährt.
 * So auch außerhalb von Activities, wo kein Contexxt verfügbar ist.
 * Des Weiteren können Listener eingehangen werden, die darauf hören, ob sich die Variable ändert.
 * In der Variable wird gespeichert, ob die Anwendung das erste Mal gestartet wird (Datenbankinitialisierung...).
 */
public class FirstStart {
    private static final String PROPERTY_NAME = "firstStart";
    private static boolean firstStart = false;
    private static PropertyChangeSupport propertyChangeSupport =
            new PropertyChangeSupport(FirstStart.class);

    /**
     * Fügt einen Listener hinzu.
     *
     * @param listener der Listener
     */
    public static void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * @return true falls erster Start
     */
    public static synchronized boolean isFirstStart() {
        return firstStart;
    }

    /**
     * Setzt die Property und feuert die Listener.
     *
     * @param pFirstStart boolean der die Property setzt.
     */
    public static synchronized void setFirstStart(boolean pFirstStart) {
        boolean oldFirstStart = FirstStart.isFirstStart();
        FirstStart.firstStart = pFirstStart;
        propertyChangeSupport.firePropertyChange(PROPERTY_NAME, oldFirstStart, FirstStart.firstStart);
    }
}
