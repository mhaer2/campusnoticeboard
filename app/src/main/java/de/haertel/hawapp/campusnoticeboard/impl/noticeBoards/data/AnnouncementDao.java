package de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Dao
public interface AnnouncementDao {

    @Insert
    void insert (Announcement announcement);

    @Update
    void update(Announcement announcement);

    @Delete
    void delete(Announcement announcement);

    @Query("DELETE FROM announcement_table")
    void deleteAllAnnouncements();

    @Query("DELETE FROM announcement_table WHERE date < :pDeleteBefore")
    void deleteOlderAnnouncements(Date pDeleteBefore);

    @Query("SELECT * FROM announcement_table WHERE noticeBoard LIKE :pTopic")
    LiveData<List<Announcement>> getAnnouncementsForTopic(String pTopic);

}
