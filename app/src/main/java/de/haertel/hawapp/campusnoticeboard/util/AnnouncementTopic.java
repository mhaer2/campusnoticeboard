package de.haertel.hawapp.campusnoticeboard.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data.Announcement;

public class AnnouncementTopic {
    private static String topic;

    private static PropertyChangeSupport propertyChangeSupport =
            new PropertyChangeSupport(AnnouncementTopic.class);

    public static void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public static synchronized String getTopic() {
        return topic;
    }

    public static synchronized void setTopic(String topic) {
        String oldTopic = AnnouncementTopic.topic;
        AnnouncementTopic.topic = topic;
        propertyChangeSupport.firePropertyChange("topic", oldTopic, AnnouncementTopic.topic);
    }
}
