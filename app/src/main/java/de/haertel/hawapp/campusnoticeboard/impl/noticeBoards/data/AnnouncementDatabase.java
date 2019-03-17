package de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.haertel.hawapp.campusnoticeboard.util.AnnouncementTopic;
import de.haertel.hawapp.campusnoticeboard.util.LastInsert;

/**
 * Datenbank, welche die Bekanntmachungen inne hat. Diese Klasse greift über das DAO aauf die SQLite DB zu.
 */
@Database(entities = {Announcement.class}, version = 2, exportSchema = false)
@TypeConverters(DateTypeConverter.class)
public abstract class AnnouncementDatabase extends RoomDatabase {
    private static AnnouncementDatabase instance;
    private static final String ANNOUNCEMENT_DATABASE_NAME = "announcement_database";
    private static final String ANNOUNCEMENTS_REFERENCE_FIREBASE = "flamelink/environments/production/content/announcements/en-US";
    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm";
    private static final String HEADLINE = "headline";
    private static final String AUTHOR = "author";
    private static final String MESSAGE = "message";
    private static final String NOTICEBOARD = "noticeboard";
    private static final String DATE = "date";


    /**
     * @return das DAO der Announcements
     */
    public abstract AnnouncementDao announcementDao();

    /**
     * Liefert eine Instanz der Datanbank der Bekanntmachungen
     *
     * @param context der Kontext, über den der Applikationskontext geholt werden kann
     * @return die Datenbank der Bekanntmachungen
     */
    public static synchronized AnnouncementDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AnnouncementDatabase.class, ANNOUNCEMENT_DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    /**
     * CallBack, welches die Datenbank initial befüllt mit den Daten aus Firebase.
     */
    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        /**
         * Methode die Aufgerufen wird beim kreieren der Datenbank
         * @param db die Datenbank
         */
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            final HashSet<Announcement> announcements = new HashSet<>();
            final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(ANNOUNCEMENTS_REFERENCE_FIREBASE);
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                /**
                 * Bei Änderung der Daten in der Firebase Datenbank wird die SQLite Datenbank befüllt.
                 *
                 * @param dataSnapshot snapshot von den Daten der angegebnen Referenz.
                 */
                @Override
                @SuppressWarnings("unchecked")
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // HashSet anstelle von ArrayList, da die containsMethode bei HashSet deutlich bessere Performance hat
                    HashMap<String, HashMap<String, String>> outerMap = (HashMap<String, HashMap<String, String>>) dataSnapshot.getValue();
                    DateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.getDefault());
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
                                case AUTHOR:
                                    author = value;
                                    break;
                                case HEADLINE:
                                    headline = value;
                                    break;
                                case MESSAGE:
                                    message = value;
                                    break;
                                case NOTICEBOARD:
                                    noticeboard = value;
                                case DATE:
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
                        }
                    }
                    //noinspection unchecked
                    new PopulateDbAsyncTask(instance).execute(announcements);
                }

                /**
                 * keine Funktion da nicht implementiert
                 * @param databaseError der Fehler
                 */
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }
    };

    /**
     * Der Asynchrone Task, der Die SQLite DB befüllt.
     */
    private static class PopulateDbAsyncTask extends AsyncTask<HashSet<Announcement>, Void, Void> {
        private AnnouncementDao announcementDao;

        private PopulateDbAsyncTask(AnnouncementDatabase db) {
            announcementDao = db.announcementDao();
        }

        /**
         * Befüllt die Datenbank ehe die Zeit für den aktuell letztn Insert gespeichert wird.
         * Auch das Announcement Topic wird initial gesetzt.
         *
         * @param pAnnouncements die Bekanntmachungen als Set
         * @return void
         */
        @SafeVarargs
        @Override
        protected final Void doInBackground(HashSet<Announcement>... pAnnouncements) {
            for (Announcement announcement : pAnnouncements[0]) {
                announcementDao.insert(announcement);
            }
            LastInsert.setLastInsert(new Date());
            AnnouncementTopic.initTopic(AnnouncementTopic.getTopic());

            return null;
        }


    }


}
