package de.haertel.hawapp.campusnoticeboard.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private final String MENU_ENTRY_STORE_KEY = "menuEntryPreference";
    private final String MY_PREFERENCES = "MyPreferences";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _addLogoutReceiver();
        setContentView(R.layout.activity_settings);
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
        userPref = getSharedPreferences(currentUser + "Pref", MODE_PRIVATE);
        userName = userPref.getString("Username", "none");
        announcementBoard = userPref.getString("AnnouncementBoard", "none");
        pushEnabled = userPref.getBoolean("PushEnabled", true);


        AnnouncementDatabase database = AnnouncementDatabase.getInstance(this);
        final AnnouncementDao announcementDao = database.announcementDao();

        final List<String> menuEntryTitles = _fetchChildEntryTitles();
        Collections.sort(menuEntryTitles, new Comparator<String>() {
            @Override
            public int compare(String string1, String string2) {
                return string1.toLowerCase().compareTo(string2.toLowerCase());
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, menuEntryTitles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        settingsDefaultDepartmentSpinner.setAdapter(adapter);
        settingsDefaultDepartmentSpinner.setOnItemSelectedListener(this);
        settingsDefaultDepartmentSpinner.setSelection(0);
        settingsDefaultDepartmentSpinner.setSelection(menuEntryTitles.indexOf(announcementBoard));

        settingsDeleteOlderEntriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimeZone timeZone = TimeZone.getDefault();
                long sevenDaysInMillis = 7 * 24 * 3600 * 1000;
                long timeZoneOffset =  timeZone.getRawOffset();
                long offsetMillis = -1 * (sevenDaysInMillis + timeZoneOffset);
                Calendar cal = new GregorianCalendar(); // creates calendar
                cal.setTime(LastInsert.getLastInsert()); // sets calendar time/date
                cal.add(Calendar.MILLISECOND, (int) offsetMillis);

                Date deleteBeforeDate = cal.getTime();
                NoticeBoardMainActivity.announcementViewModel.deleteOlderAnnouncements(deleteBeforeDate);
            }
        });
        settingsRestoreEntriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_isNetworkAvailable()){
                    NoticeBoardMainActivity.announcementViewModel.deleteAllNotes();
                    _repopulateEntries();
                }

            }
        });

        settingsNotificationSwitch.setChecked(pushEnabled);
        settingsNotificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String currentUser = CurrentUser.getUsername();
                userPref = getSharedPreferences(currentUser + "Pref", MODE_PRIVATE);
                SharedPreferences.Editor editor = userPref.edit();
                editor.putBoolean("PushEnabled", isChecked).apply();
            }
        });

        settingsLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CurrentUser.setUsername(null);
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction("de.haertel.hawapp.campusnoticeboard.impl.ACTION_LOGOUT");
                sendBroadcast(broadcastIntent);
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void _repopulateEntries() {
        final HashSet<Announcement> announcements = new HashSet<>();
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("flamelink/environments/production/content/announcements/en-US");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // HashSet anstelle von ArrayList, da die containsMethode bei HashSet deutlich bessere Performance hat
                HashMap<String, HashMap<String, String>> outerMap = (HashMap<String, HashMap<String, String>>) dataSnapshot.getValue();
                String pattern = "yyyy-MM-dd'T'HH:mm";
                DateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
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
                    }
                }

                new RepoulateEntriesAsyncTask(NoticeBoardMainActivity.announcementViewModel).execute(announcements);

                SharedPreferences shared = getApplicationContext().getSharedPreferences(getString(R.string.preferenceName), MODE_PRIVATE);
                SharedPreferences.Editor editor = shared.edit();
                editor.remove(getString(R.string.lastInsert));
                editor.putLong(getString(R.string.lastInsert), new Date().getTime()).apply();
                LastInsert.setLastInsert(new Date(shared.getLong(getString(R.string.lastInsert), new Date().getTime())));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private List<String> _fetchChildEntryTitles() {
        List<String> titleList = new ArrayList<String>();
        List<MenuEntry> menuEntries = _fetchMenuEntries();
        HashSet<Integer> parentIds = new HashSet<Integer>();
        // get all Parent IDs
        for (MenuEntry menuEntry: Objects.requireNonNull(menuEntries)) {
            parentIds.add(menuEntry.getMenuParentId());
        }
        for (MenuEntry entry: menuEntries) {
            if (!parentIds.contains(entry.getId())){
                titleList.add(entry.getTitle());
            }
        }
        return titleList;
    }
    private List<MenuEntry> _fetchMenuEntries(){
        SharedPreferences sharedPreferences;
        sharedPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences != null){
            Gson gson = new Gson();
            String response= sharedPreferences.getString(MENU_ENTRY_STORE_KEY , "");
            return gson.<ArrayList<MenuEntry>>fromJson(response,
                    new TypeToken<List<MenuEntry>>(){}.getType());
        }else {
            return null;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();
        String currentUser = CurrentUser.getUsername();
        userPref = getSharedPreferences(currentUser + "Pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = userPref.edit();
        editor.remove("AnnouncementBoard");
        editor.putString("AnnouncementBoard", item).apply();
        retrnTopic = item;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    private boolean _isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private static class RepoulateEntriesAsyncTask extends AsyncTask<HashSet<Announcement>, Void, Void> {
        private AnnouncementViewModel announcementViewModel;

        private RepoulateEntriesAsyncTask(AnnouncementViewModel pAnnouncementViewModel) {
            announcementViewModel = pAnnouncementViewModel;
        }

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

    private void _addLogoutReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("de.haertel.hawapp.campusnoticeboard.impl.ACTION_LOGOUT");
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("onReceive","Logout in progress");
                //At this point you should start the login activity and finish this one
                finish();
            }
        }, intentFilter);
    }
}
