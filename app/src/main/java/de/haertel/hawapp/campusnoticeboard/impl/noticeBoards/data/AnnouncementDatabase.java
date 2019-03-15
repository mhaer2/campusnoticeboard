package de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.haertel.hawapp.campusnoticeboard.impl.LoginActivity;
import de.haertel.hawapp.campusnoticeboard.impl.NoticeBoardMainActivity;
import de.haertel.hawapp.campusnoticeboard.util.AnnouncementTopic;
import de.haertel.hawapp.campusnoticeboard.util.LastInsert;

import static android.content.Context.MODE_PRIVATE;


@Database(entities = {Announcement.class}, version = 2, exportSchema = false)
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

            final ArrayList<Announcement> announcements = new ArrayList<>();
            DatabaseReference mDatabase;
            mDatabase = FirebaseDatabase.getInstance().getReference("flamelink/environments/production/content/announcements/en-US");
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                @SuppressWarnings("unchecked")
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    HashMap<String,HashMap<String,String>> outerMap = (HashMap<String,HashMap<String,String>>) dataSnapshot.getValue();
                    String pattern = "yyyy-MM-dd'T'HH:mm";
                    DateFormat dateFormat = new SimpleDateFormat(pattern, new Locale("de", "DE"));
                    String author;
                    String headline;
                    String message;
                    String noticeboard;
                    Date date;
                    for (HashMap<String, String> middleMap: Objects.requireNonNull(outerMap).values()) {
                        author = null;
                        headline = null;
                        message = null;
                        noticeboard = null;
                        date = null;
                        for (Map.Entry<String, String> entry : middleMap.entrySet()) {
                            String key = String.valueOf(entry.getKey());
                            String value = String.valueOf(entry.getValue());

                            switch (key) {
                                case "author":
                                    author = value;
                                    break;
                                case "headline":
                                    headline = value;
                                    break;
                                case "message":
                                    message = value;
                                    break;
                                case "noticeboard":
                                    noticeboard = value;
                                case "date":
                                    try {
                                        date = dateFormat.parse(value);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                            }
                        }
                        if (author != null || headline != null || message != null || noticeboard != null || date != null) {
                            announcements.add(new Announcement(headline, author, message, date, noticeboard));
                            //announcementDao.insert(new Announcement(headline, author, message, date, noticeboard));
                        }
                    }
                    announcements.add(new Announcement("populateTest", "Martin Härtel", "populiere", new Date(), "Leichtbau und Simulation, M.Sc."));
                    announcements.add(new Announcement("populateTest", "Martin Härtel", "populiere IF", new Date(), "Informatik, B.Sc."));

                    new PopulateDbAsyncTask(instance).execute(announcements);


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }
    };

    private static class PopulateDbAsyncTask extends AsyncTask<List<Announcement>, Void, Void> {
        private AnnouncementDao announcementDao;

        private PopulateDbAsyncTask(AnnouncementDatabase db) {
            announcementDao = db.announcementDao();
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(List<Announcement>... pAnnouncements) {
            for (Announcement announcement : pAnnouncements[0]) {
                announcementDao.insert(announcement);
            }
            LastInsert.setLastInsert(new Date());
            AnnouncementTopic.initTopic(AnnouncementTopic.getTopic());
            return null;
        }

    }

}
