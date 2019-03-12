package de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import java.util.Date;

@Entity(tableName = "announcement_table")
public class Announcement implements Comparable<Announcement>{

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
}
