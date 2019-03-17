package de.haertel.hawapp.campusnoticeboard.impl.navigationMenu.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Simple POJO as Entity, for saving Menu Entries to the SQLite Database in this structur.
 */

@Entity(tableName = "menuEntry_table")
public class MenuEntry {

    @PrimaryKey
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "menuParentId")
    private int menuParentId;

    @ColumnInfo(name = "title")
    private String title;

    MenuEntry(int id, int menuParentId, String title) {
        this.id = id;
        this.menuParentId = menuParentId;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public int getMenuParentId() {
        return menuParentId;
    }

    public String getTitle() {
        return title;
    }

}
