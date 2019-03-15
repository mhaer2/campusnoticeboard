package de.haertel.hawapp.campusnoticeboard.util;

import java.util.Date;

public class LastInsert {
    private static Date lastInsert = null;

    public static synchronized Date getLastInsert() {
        return lastInsert;
    }

    public static synchronized void setLastInsert(Date lastInsert) {
        LastInsert.lastInsert = lastInsert;
    }
}
