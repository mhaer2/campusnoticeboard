package de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.Date;

@Database(entities = {Announcement.class}, version = 1, exportSchema = false)
@TypeConverters(DateTypeConverter.class)
public abstract class AnnouncementDatabase extends RoomDatabase {

    private static AnnouncementDatabase instance;
    public abstract AnnouncementDao announcementDao();

    public static synchronized AnnouncementDatabase getInstance(Context context){
        if (instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AnnouncementDatabase.class, "announcement_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }
    };

    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private AnnouncementDao announcementDao;

        private PopulateDbAsyncTask(AnnouncementDatabase db) {
            announcementDao = db.announcementDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            announcementDao.insert(new Announcement("HeadlineTest", "Martin Härtel", "Lorem ipsum und so", new Date(), "Informatik, B. Sc"));
            announcementDao.insert(new Announcement("HeadlineTest2", "Martin Härtel", "Lorem ipsum 2 und so", new Date(), "Informatik, B. Sc"));
            announcementDao.insert(new Announcement("HeadlineTest3", "Martin Härtel", "Lorem ipsum 3 und so", new Date(), "Informatik, B. Sc"));

            announcementDao.insert(new Announcement("HeadlineTest4", "Martin Härtel", "Lorem ipsum 4 und so", new Date(), "Leichtbau und Simulation"));
            return null;
        }
    }
}
