package de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.haertel.hawapp.campusnoticeboard.util.AnnouncementTopic;
import de.haertel.hawapp.campusnoticeboard.util.FirstStart;


@Database(entities = {Announcement.class}, version = 2, exportSchema = false)
@TypeConverters(DateTypeConverter.class)
public abstract class AnnouncementDatabase extends RoomDatabase {
    private static boolean isAnnouncementListPopulated = false;
    private static boolean isDatabasePopulated = false;
    private static AnnouncementDatabase instance;

    public abstract AnnouncementDao announcementDao();


    public static synchronized AnnouncementDatabase getInstance(Context context) {
        if (instance == null) {
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

            final HashSet<Announcement> announcements = new HashSet<>();
            final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("flamelink/environments/production/content/announcements/en-US");
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // HashSet anstelle von ArrayList, da die containsMethode bei HashSet deutlich bessere Performance hat
                    HashMap<String, HashMap<String, String>> outerMap = (HashMap<String, HashMap<String, String>>) dataSnapshot.getValue();
                    String pattern = "yyyy-MM-dd'T'HH:mm";
                    DateFormat dateFormat = new SimpleDateFormat(pattern, new Locale("de", "DE"));
                    String author;
                    String headline;
                    String message;
                    String noticeboard;
                    Date date;
                    for (HashMap<String, String> middleMap : Objects.requireNonNull(outerMap).values()) {
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
                    Announcement leichtbauAnnouncement = new Announcement("populateTest", "Martin Härtel", "populiere", new Date(), "Leichtbau und Simulation, M.Sc.");
                    announcements.add(leichtbauAnnouncement);
                    announcements.add(new Announcement("populateTest", "Martin Härtel", "populiere IF", new Date(), "Informatik, B.Sc."));

                    AnnouncementDatabase.isAnnouncementListPopulated = true;


                    mDatabase.addChildEventListener(new ChildEventListener() {
                        @Override
                        @SuppressWarnings("unchecked")
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {

                            if (FirstStart.isFirstStart()) {
                                if (AnnouncementDatabase.isAnnouncementListPopulated && !AnnouncementDatabase.isDatabasePopulated) {
                                    new PopulateDbAsyncTask(instance).execute(announcements);
                                    AnnouncementDatabase.isDatabasePopulated = true;
                                }
                            } else {
                                String pattern = "yyyy-MM-dd'T'HH:mm";
                                DateFormat dateFormat = new SimpleDateFormat(pattern, new Locale("de", "DE"));
                                Map<String, String> map = (Map) dataSnapshot.getValue();

                                String authorOfNewInsert = map.get("author");
                                String headlineOfNewInsert = map.get("headline");
                                String messageOfNewInsert = map.get("message");
                                Date dateOfNewInsert = null;
                                try {
                                    dateOfNewInsert = dateFormat.parse(map.get("date"));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                String noticeboardOfNewInsert = map.get("noticeboard");

                                Announcement newInsert = new Announcement
                                        (headlineOfNewInsert, authorOfNewInsert, messageOfNewInsert, dateOfNewInsert, noticeboardOfNewInsert);
                                if (!announcements.contains(newInsert)) {
                                    new InsertNewEntryAsyncTask(instance).execute(newInsert);
                                }
                            }
                        }
                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });





        }
    };




    private static class InsertNewEntryAsyncTask extends AsyncTask<Announcement, Void, Void> {
        private AnnouncementDao announcementDao;

        private InsertNewEntryAsyncTask(AnnouncementDatabase db) {
            announcementDao = db.announcementDao();
        }

        @Override
        protected Void doInBackground(Announcement... pAnnouncements) {
            announcementDao.insert(pAnnouncements[0]);
            return null;
        }
    }

    private static class PopulateDbAsyncTask extends AsyncTask<HashSet<Announcement>, Void, Void> {
        private AnnouncementDao announcementDao;

        private PopulateDbAsyncTask(AnnouncementDatabase db) {
            announcementDao = db.announcementDao();
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(HashSet<Announcement>... pAnnouncements) {
            for (Announcement announcement : pAnnouncements[0]) {
                announcementDao.insert(announcement);
            }
            AnnouncementTopic.initTopic(AnnouncementTopic.getTopic());

            return null;
        }


    }
    private static class DeleteAnnouncementAsyncTask extends AsyncTask<Announcement, Void, Void> {
        private AnnouncementDao announcementDao;

        private DeleteAnnouncementAsyncTask(AnnouncementDao pAnnouncementDao) {
            this.announcementDao = pAnnouncementDao;
        }

        @Override
        protected Void doInBackground(Announcement... pAnnouncements) {
            announcementDao.delete(pAnnouncements[0]);
            return null;
        }
    }


}
