package de.haertel.hawapp.campusnoticeboard.impl.navigationMenu.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {MenuEntry.class}, version = 3, exportSchema = false)
public abstract class MenuEntryDatabase extends RoomDatabase {

    private static MenuEntryDatabase instance;
    public abstract MenuEntryDao menuEntryDao();

    public static synchronized MenuEntryDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    MenuEntryDatabase.class, "menuEntry_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
