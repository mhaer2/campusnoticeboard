package de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import java.util.Date;
import java.util.Objects;

/**
 * POJO as Entity, for saving announcements to the SQLite Database in this structur.
 */
@Entity(tableName = "announcement_table")
public class Announcement implements Comparable<Announcement> {
    private static final String ZERO_DAY = "NEW";
    private static final String FIRST_DAY = "1T";
    private static final String SECOND_DAY = "2T";
    private static final String THIRD_DAY = "3T";
    private static final String FOURTH_DAY = "4T";
    private static final String FIFTH_DAY = "5T";
    private static final String SIXTH_DAY = "6T";
    private static final String SEVENTH_DAY = "7T";
    private static final String MORE_THAN_SEVEN_DAYS = ">7T";
    private static final String UNVALID_DAYCOUNT = "-1";


    @PrimaryKey(autoGenerate = true)
    private int id;

    private String headline;

    private String author;

    private String message;

    @TypeConverters(DateTypeConverter.class)
    private Date date;

    private String noticeBoard;

    public Announcement(String headline, String author, String message, Date date, String noticeBoard) {
        this.headline = headline;
        this.author = author;
        this.message = message;
        this.date = date;
        this.noticeBoard = noticeBoard;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getHeadline() {
        return headline;
    }

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }

    public Date getDate() {
        return date;
    }

    public String getNoticeBoard() {
        return noticeBoard;
    }

    /**
     * Liefert die Anzahl der Tage als Differenz zwischen 0 und 7. (
     * 'NEW' bei 0 und '>7T' bei größer 7 Tagen
     *
     * @return die Anzahl als lesbarer String
     */
    public String getDayCountSincePosted() {
        Date actualDate = new Date();
        long differenceMillis = actualDate.getTime() - getDate().getTime();
        int dayDifference = (int) (differenceMillis / (24 * 60 * 60 * 1000));
        if (dayDifference > 7) {
            return MORE_THAN_SEVEN_DAYS;
        }
        String dayCount;
        switch (dayDifference) {
            case 0:
                dayCount = ZERO_DAY;
                break;
            case 1:
                dayCount = FIRST_DAY;
                break;
            case 2:
                dayCount = SECOND_DAY;
                break;
            case 3:
                dayCount = THIRD_DAY;
                break;
            case 4:
                dayCount = FOURTH_DAY;
                break;
            case 5:
                dayCount = FIFTH_DAY;
                break;
            case 6:
                dayCount = SIXTH_DAY;
                break;
            case 7:
                dayCount = SEVENTH_DAY;
                break;
            default:
                dayCount = UNVALID_DAYCOUNT;
                break;
        }
        return dayCount;
    }

    /**
     * Der Komparator der zwei Bekanntmachungen nach Datum Sortiert
     *
     * @param announcementToCompare die zu vergleichene Bekanntmachung
     * @return größer gleich kleiner als Integer
     */
    @Override
    public int compareTo(Announcement announcementToCompare) {
        long time1 = this.date.getTime();
        long time2 = announcementToCompare.getDate().getTime();

        if (time1 > time2) {
            return -1;
        } else if (time1 < time2) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Die Equals Methode der Klasse
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Announcement that = (Announcement) o;
        return Objects.equals(headline, that.headline) &&
                Objects.equals(author, that.author) &&
                Objects.equals(message, that.message) &&
                Objects.equals(date, that.date) &&
                Objects.equals(noticeBoard, that.noticeBoard);
    }

}
