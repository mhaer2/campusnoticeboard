package de.haertel.hawapp.campusnoticeboard.impl.navigationMenu.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Data Access Object, for access to Menu Entries.
 */
@Dao
public interface MenuEntryDao {

    @Insert
    void insert(MenuEntry menuEntry);

    @Update
    void update(MenuEntry menuEntry);

    @Delete
    void delete(MenuEntry menuEntry);

    @Query("DELETE FROM menuEntry_table")
    void deleteAllMenuEntries();

    @Query("SELECT * FROM menuEntry_table")
    LiveData<List<MenuEntry>> getAllMenuEntries();

}
