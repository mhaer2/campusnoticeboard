package de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

/**
 * Da in der Datenbank kein Date-Objekt gespeichert werden kann,
 * konvertiert dieser DateTypeKonverter die Einheiten
 */
public class DateTypeConverter {

    /**
     * Konvertiert den Timestamp in ein Datum.
     *
     * @param value der Timestamp
     * @return das Datum
     */
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    /**
     * Konvertiert das Datum in einen Timestamp.
     *
     * @param date das Datum
     * @return der Timestamp
     */
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}