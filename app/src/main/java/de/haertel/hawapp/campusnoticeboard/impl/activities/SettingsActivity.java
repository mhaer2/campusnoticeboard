package de.haertel.hawapp.campusnoticeboard.impl.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import de.haertel.hawapp.campusnoticeboard.R;
import de.haertel.hawapp.campusnoticeboard.impl.navigationMenu.data.MenuEntry;
import de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data.Announcement;
import de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data.AnnouncementDao;
import de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data.AnnouncementDatabase;
import de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data.AnnouncementViewModel;
import de.haertel.hawapp.campusnoticeboard.util.CurrentUser;
import de.haertel.hawapp.campusnoticeboard.util.LastInsert;

/**
 * Activity für die Settings der Anwendung
 */
public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private final String MENU_ENTRY_STORE_KEY = "menuEntryPreference";
    private final String MY_PREFERENCES = "MyPreferences";
    private final static String PREF_PREFIX = "Pref";
    private final static String ANNOUNCEMENT_BOARD = "AnnouncementBoard";
    private final static String USERNAME = "Username";
    private final static String PUSHENABLED = "PushEnabled";
    private final static String DEFAULT_VALUE_BOARD = "none";
    private final static String PATTERN = "yyyy-MM-dd'T'HH:mm";
    private final static String FLAMELINK_REFERENCE = "flamelink/environments/production/content/announcements/en-US";
    private final static String LOGOUT_ACTION = "de.haertel.hawapp.campusnoticeboard.impl.ACTION_LOGOUT";
    private static final String HEADLINE = "headline";
    private static final String AUTHOR = "author";
    private static final String MESSAGE = "message";
    private static final String NOTICEBOARD = "noticeboard";
    private static final String DATE = "date";

    private String userName;
    private String announcementBoard;
    private boolean pushEnabled;

    private String retrnTopic;
    private boolean returnDeleteOlder = false;

    private Button settingsDeleteOlderEntriesButton;
    private Button settingsRestoreEntriesButton;
    private Spinner settingsDefaultDepartmentSpinner;
    private Switch settingsNotificationSwitch;
    private Button settingsLogoutButton;

    private SharedPreferences userPref;

    /**
     * Holt sich alle Buttons und Views der Settings und befüllt sie mit Funktionen.
     *
     * @param savedInstanceState Bundle mit gespeichertem Zustand
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _addLogoutReceiver();
        setContentView(R.layout.activity_settings);
        //init Retun boolean
        returnDeleteOlder = false;

        settingsDeleteOlderEntriesButton = findViewById(R.id.settings_delete_older_entries);
        settingsRestoreEntriesButton = findViewById(R.id.settings_restore_entries);
        settingsDefaultDepartmentSpinner = findViewById(R.id.settings_default_department_spinner);
        settingsNotificationSwitch = findViewById(R.id.settings_push_notification_switch);
        settingsLogoutButton = findViewById(R.id.settings_logout_btn);

        android.support.v7.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        //set own Toolbar as ActionBar
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle(R.string.action_settings);

        String currentUser = CurrentUser.getUsername();
        userPref = getSharedPreferences(currentUser + PREF_PREFIX, MODE_PRIVATE);
        userName = userPref.getString(USERNAME, DEFAULT_VALUE_BOARD);
        announcementBoard = userPref.getString(ANNOUNCEMENT_BOARD, DEFAULT_VALUE_BOARD);
        pushEnabled = userPref.getBoolean(PUSHENABLED, true);


        AnnouncementDatabase database = AnnouncementDatabase.getInstance(this);
        final AnnouncementDao announcementDao = database.announcementDao();

        final List<String> menuEntryTitles = _fetchChildEntryTitles();
        Collections.sort(menuEntryTitles, new Comparator<String>() {
            @Override
            public int compare(String string1, String string2) {
                return string1.toLowerCase().compareTo(string2.toLowerCase());
            }
        });

        // Set up Spinner with Default Department list
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, menuEntryTitles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        settingsDefaultDepartmentSpinner.setAdapter(adapter);
        settingsDefaultDepartmentSpinner.setOnItemSelectedListener(this);
        settingsDefaultDepartmentSpinner.setSelection(0);
        settingsDefaultDepartmentSpinner.setSelection(menuEntryTitles.indexOf(announcementBoard));

        //set op Button to delete Older Entries
        settingsDeleteOlderEntriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimeZone timeZone = TimeZone.getDefault();
                long sevenDaysInMillis = 7 * 24 * 3600 * 1000;
                long timeZoneOffset = timeZone.getRawOffset();
                long offsetMillis = -1 * (sevenDaysInMillis + timeZoneOffset);
                Calendar cal = new GregorianCalendar(); // creates calendar
                cal.setTime(LastInsert.getLastInsert()); // sets calendar time/date
                cal.add(Calendar.MILLISECOND, (int) offsetMillis);

                Date deleteBeforeDate = cal.getTime();
                NoticeBoardActivity.announcementViewModel.deleteOlderAnnouncements(deleteBeforeDate);
            }
        });

        //set op Button to restore all Entries
        settingsRestoreEntriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_isNetworkAvailable()) {
                    NoticeBoardActivity.announcementViewModel.deleteAllNotes();
                    _repopulateEntries();
                }
            }
        });

        //set op Button to set Push Notification on/Off
        settingsNotificationSwitch.setChecked(pushEnabled);
        settingsNotificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String currentUser = CurrentUser.getUsername();
                userPref = getSharedPreferences(currentUser + PREF_PREFIX, MODE_PRIVATE);
                SharedPreferences.Editor editor = userPref.edit();
                editor.putBoolean(PUSHENABLED, isChecked).apply();
            }
        });

        //set op Button to Logout
        settingsLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CurrentUser.setUsername(null);
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(LOGOUT_ACTION);
                sendBroadcast(broadcastIntent);
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * Methode, die alle Announcement Einträge von der Firebase Datenbank holt und so die Listen neu befüllt.
     */
    private void _repopulateEntries() {
        final HashSet<Announcement> announcements = new HashSet<>();
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(FLAMELINK_REFERENCE);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // HashSet anstelle von ArrayList, da die containsMethode bei HashSet deutlich bessere Performance hat
                HashMap<String, HashMap<String, String>> outerMap = (HashMap<String, HashMap<String, String>>) dataSnapshot.getValue();
                DateFormat dateFormat = new SimpleDateFormat(PATTERN, Locale.getDefault());
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

                new RepoulateEntriesAsyncTask(NoticeBoardActivity.announcementViewModel).execute(announcements);

                SharedPreferences shared = getApplicationContext().getSharedPreferences(getString(R.string.preferenceName), MODE_PRIVATE);
                SharedPreferences.Editor editor = shared.edit();
                editor.remove(getString(R.string.lastInsert));
                editor.putLong(getString(R.string.lastInsert), new Date().getTime()).apply();
                LastInsert.setLastInsert(new Date(shared.getLong(getString(R.string.lastInsert), new Date().getTime())));
            }

            /**
             * nicht implementiert
             * @param databaseError Der Fehler
             */
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Legt fest, was beim Klicken auf die Icons in der Toolbar.
     *
     * @param item das MenüItem auf das geklickt wurde
     * @return true wenn Item selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Falls der User im Stack nach oben navigieren will wird die OnBackPressed Methode aufgerufen.
     *
     * @return true wenn erfolgreich
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Definiert was beim Drückern der zurück taste passiert.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * liefert alle Kindknoten des Menübaums
     *
     * @return die Liste der Kind-Menüeinträge
     */
    private List<String> _fetchChildEntryTitles() {
        List<String> titleList = new ArrayList<String>();
        List<MenuEntry> menuEntries = _fetchMenuEntries();
        HashSet<Integer> parentIds = new HashSet<Integer>();
        // get all Parent IDs
        for (MenuEntry menuEntry : Objects.requireNonNull(menuEntries)) {
            parentIds.add(menuEntry.getMenuParentId());
        }
        for (MenuEntry entry : menuEntries) {
            if (!parentIds.contains(entry.getId())) {
                titleList.add(entry.getTitle());
            }
        }
        return titleList;
    }

    /**
     * Liefert alle Menüeinträge des Navigation Baumes
     *
     * @return die Einträge
     */
    private List<MenuEntry> _fetchMenuEntries() {
        SharedPreferences sharedPreferences;
        sharedPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            Gson gson = new Gson();
            String response = sharedPreferences.getString(MENU_ENTRY_STORE_KEY, "");
            return gson.<ArrayList<MenuEntry>>fromJson(response,
                    new TypeToken<List<MenuEntry>>() {
                    }.getType());
        } else {
            return null;
        }
    }

    /**
     * Wird ausgeführt wenn auf ein Item in der Liste des Spinners gedrückt wird.
     *
     * @param parent   der PArent Knoten
     * @param view     die View
     * @param position die Position des Items
     * @param id       die ID
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();
        String currentUser = CurrentUser.getUsername();
        userPref = getSharedPreferences(currentUser + PREF_PREFIX, MODE_PRIVATE);
        SharedPreferences.Editor editor = userPref.edit();
        editor.remove(ANNOUNCEMENT_BOARD);
        editor.putString(ANNOUNCEMENT_BOARD, item).apply();
        retrnTopic = item;
    }

    /**
     * nicht implementiert
     *
     * @param parent der parent view
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * Checkt ob Netzwerk verfügbar
     *
     * @return true wenn verfügbar.
     */
    private boolean _isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Asyncroner Task, der die Datenbank befüllt.
     */
    private static class RepoulateEntriesAsyncTask extends AsyncTask<HashSet<Announcement>, Void, Void> {
        private AnnouncementViewModel announcementViewModel;

        private RepoulateEntriesAsyncTask(AnnouncementViewModel pAnnouncementViewModel) {
            announcementViewModel = pAnnouncementViewModel;
        }

        /**
         * Führt die inserts aus
         *
         * @param pAnnouncements Liste der Bekanntmachungen
         * @return void
         */
        @SafeVarargs
        @Override
        protected final Void doInBackground(HashSet<Announcement>... pAnnouncements) {
            for (Announcement announcement : pAnnouncements[0]) {
                announcementViewModel.insert(announcement);
            }
            LastInsert.setLastInsert(new Date());

            return null;
        }
    }

    /**
     * Fügt einen BroadcastReceiver hinzu, der die Anwendung beim Logout beendet, falls sie noch im Stack ist.
     */
    private void _addLogoutReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LOGOUT_ACTION);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                finish();
            }
        }, intentFilter);
    }
}
