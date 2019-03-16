package de.haertel.hawapp.campusnoticeboard.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LastInsert {
    private static Date lastInsert = null;
    private static final String PATTERN = "dd/MM/yyyy HH:mm";
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(PATTERN, Locale.getDefault());

    public static synchronized Date getLastInsert() {
        return lastInsert;
    }

    public static synchronized void setLastInsert(Date lastInsert) {
        LastInsert.lastInsert = lastInsert;
    }

    public static String getPattern() {
        return PATTERN;
    }

    public static DateFormat getDateFormat() {
        return DATE_FORMAT;
    }
}
