package de.haertel.hawapp.campusnoticeboard.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


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

    public static synchronized void setTopic(String pTopic) {
        String oldTopic = AnnouncementTopic.topic;
        AnnouncementTopic.topic = pTopic;
        propertyChangeSupport.firePropertyChange("topic", oldTopic, AnnouncementTopic.topic);
    }
    public static synchronized void initTopic(String pTopic){
        AnnouncementTopic.topic = "none";
        AnnouncementTopic.setTopic(pTopic);
    }


}
