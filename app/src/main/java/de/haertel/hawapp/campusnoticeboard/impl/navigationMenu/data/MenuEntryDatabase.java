package de.haertel.hawapp.campusnoticeboard.impl.navigationMenu.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Datenbank, welche die Menü Einträge inne hat. Diese Klasse greift über das DAO aauf die SQLite DB zu.
 */

@Database(entities = {MenuEntry.class}, version = 3, exportSchema = false)
public abstract class MenuEntryDatabase extends RoomDatabase {
    private static final String MENUENTRY_DATABASE_NAME = "menuEntry_database";
    private static MenuEntryDatabase instance;

    public abstract MenuEntryDao menuEntryDao();

    /**
     * Liefert eine Instanz der Datanbank der Menüeinträge
     *
     * @param context der Kontext, über den der Applikationskontext geholt werden kann
     * @return die Datenbank der Menüeinträge
     */
    static synchronized MenuEntryDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    MenuEntryDatabase.class, MENUENTRY_DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
