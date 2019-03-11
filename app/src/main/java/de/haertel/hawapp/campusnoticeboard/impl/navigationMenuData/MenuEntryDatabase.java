package de.haertel.hawapp.campusnoticeboard.impl.navigationMenuData;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Database(entities = {MenuEntry.class}, version = 2, exportSchema = false)
public abstract class MenuEntryDatabase extends RoomDatabase {

    private static MenuEntryDatabase instance;
    public abstract MenuEntryDao menuEntryDao();

    public static synchronized MenuEntryDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    MenuEntryDatabase.class, "menuEntry_database")
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
        private final static String FLAMELINK_NAVIGATION_ID = "id";
        private final static String FLAMELINK_NAVIGATION_PARENTID = "parentIndex";
        private final static String FLAMELINK_NAVIGATION_TITLE = "title";
//        private final static String FLAMELINK_NAVIGATION_ORDER = "order";

        private MenuEntryDao menuEntryDao;

        private PopulateDbAsyncTask(MenuEntryDatabase db) {
            menuEntryDao = db.menuEntryDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            DatabaseReference database = FirebaseDatabase.getInstance().getReference("flamelink/environments/production/navigation/noticeBoards/en-US/items");
            database.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ArrayList<HashMap<String, String>> arrayList = (ArrayList<HashMap<String, String>>) dataSnapshot.getValue();
                    int id;
                    int menuParentId;
                    String title ;

                    for (HashMap<String, String> hashMap: Objects.requireNonNull(arrayList)) {
                        id = -1;
                        menuParentId = -1;
                        title = null;
                        Long temp;
                        for (Map.Entry<String, String> entry : hashMap.entrySet() ) {
                            String key = String.valueOf(entry.getKey());
                            String value = String.valueOf(entry.getValue());
                            if (key.equals(FLAMELINK_NAVIGATION_ID)){
                                temp = (Long.parseLong(value));
                                id = temp.intValue();
                            }
                            if (key.equals(FLAMELINK_NAVIGATION_PARENTID)){
                                temp = (Long.parseLong(value));
                                menuParentId = temp.intValue();
                            }
                            if (key.equals(FLAMELINK_NAVIGATION_TITLE)){
                                title = value;
                            }
                        }
                        if (id != -1 || menuParentId != -1 || title != null ) {
                            menuEntryDao.insert(new MenuEntry(id, menuParentId, title));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            return null;
        }
    }
}
