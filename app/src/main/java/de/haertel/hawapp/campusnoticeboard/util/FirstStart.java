package de.haertel.hawapp.campusnoticeboard.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class FirstStart {
    private static boolean firstStart = false;

    private static PropertyChangeSupport propertyChangeSupport =
            new PropertyChangeSupport(FirstStart.class);

    public static void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public static synchronized boolean isFirstStart() {
        return firstStart;
    }

    public static synchronized void setFirstStart(boolean pFirstStart) {
        boolean oldFirstStart= FirstStart.isFirstStart();
        FirstStart.firstStart = pFirstStart;
        propertyChangeSupport.firePropertyChange("firstStart", oldFirstStart, FirstStart.firstStart);
    }
}
