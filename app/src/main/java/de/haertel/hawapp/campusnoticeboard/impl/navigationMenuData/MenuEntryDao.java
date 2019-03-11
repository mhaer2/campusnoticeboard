package de.haertel.hawapp.campusnoticeboard.impl.navigationMenuData;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface MenuEntryDao {

    @Insert
    void insert (MenuEntry menuEntry);

    @Update
    void update(MenuEntry menuEntry);

    @Delete
    void delete(MenuEntry menuEntry);

    @Query("DELETE FROM menuEntry_table")
    void deleteAllMenuEntries();

    @Query("SELECT * FROM menuEntry_table")
    LiveData<List<MenuEntry>> getAllMenuEntries();

//    @Query("SELECT * FROM menuEntry_table WHERE treeDepth = 0")
//    LiveData<List<MenuEntry>> getAllRootMenuEntries();
//
//    @Query("SELECT * FROM menuEntry_table WHERE (treeDepth = 1 AND menuParentId = (SELECT id FROM menuEntry_table WHERE title LIKE :pTopic))")
//    LiveData<List<MenuEntry>> getAllNonRootParentEntries(String pTopic);
//
//    @Query("SELECT * FROM menuEntry_table WHERE treeDepth = 2")
//    LiveData<List<MenuEntry>> getAllChildMenuEntries();
}
