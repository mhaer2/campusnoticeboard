package de.haertel.hawapp.campusnoticeboard.util;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Klasse, die syncronisierten Zugriff auf eine statische Variable ermöglicht
 * und somit von überall aus der Applikation Zugriff gewährt.
 * So auch außerhalb von Activities, wo kein Contexxt verfügbar ist.
 * In der Variable wird gespeichert, wann der letzte Insert in die Datenbank statt fand.
 */
public class LastInsert {
    private static Date lastInsert = null;
    private static final String PATTERN = "dd/MM/yyyy HH:mm";
    @SuppressLint("ConstantLocale")
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(PATTERN, Locale.getDefault());

    /**
     * @return das Datum des letzten Inserts
     */
    public static synchronized Date getLastInsert() {
        return lastInsert;
    }

    /**
     * setzt den Zeitpunkt des letzten Inserts
     *
     * @param lastInsert das Datum
     */
    public static synchronized void setLastInsert(Date lastInsert) {
        LastInsert.lastInsert = lastInsert;
    }

    /**
     * @return das DateFormat
     */
    public static DateFormat getDateFormat() {
        return DATE_FORMAT;
    }
}
