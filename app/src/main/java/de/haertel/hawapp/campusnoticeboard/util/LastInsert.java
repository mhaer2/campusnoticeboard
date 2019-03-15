package de.haertel.hawapp.campusnoticeboard.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Date;

public class LastInsert {
    private static Date lastInsert;

    private static PropertyChangeSupport propertyChangeSupport =
            new PropertyChangeSupport(LastInsert.class);

    public static void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public static synchronized Date getLastInsert() {
        return lastInsert;
    }

    public static synchronized void setLastInsert(Date pLastInsert) {
        Date oldLastInsert = LastInsert.getLastInsert();
        LastInsert.lastInsert = pLastInsert;
        propertyChangeSupport.firePropertyChange("lastInsert", oldLastInsert, LastInsert.lastInsert);
    }
}
