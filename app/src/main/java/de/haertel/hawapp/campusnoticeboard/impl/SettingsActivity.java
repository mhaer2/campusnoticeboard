package de.haertel.hawapp.campusnoticeboard.impl;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import de.haertel.hawapp.campusnoticeboard.R;
import de.haertel.hawapp.campusnoticeboard.impl.navigationMenu.data.MenuEntry;
import de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data.AnnouncementDao;
import de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data.AnnouncementDatabase;
import de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data.AnnouncementViewModel;
import de.haertel.hawapp.campusnoticeboard.util.AnnouncementTopic;
import de.haertel.hawapp.campusnoticeboard.util.CurrentUser;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static final String EXTRA_ANNOUNCEMENT_VIEW_MODEL = "de.haertel.hawapp.campusnoticeboard.impl.EXTRA_DATE";
    private final String MENU_ENTRY_STORE_KEY = "menuEntryPreference";
    private final String MY_PREFERENCES = "MyPreferences";

    private String userName;
    private String announcementBoard;
    private boolean pushEnabled;

    private String retrnTopic;

    private Button settingsDeleteOlderEntriesButton;
    private Button settingsRestoreEntriesButton;
    private Spinner settingsDefaultDepartmentSpinner;
    private Switch settingsNotificationSwitch;
    private Button settingsLogoutButton;

    private SharedPreferences userPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsDeleteOlderEntriesButton = findViewById(R.id.settings_delete_older_entries);
        settingsRestoreEntriesButton = findViewById(R.id.settings_restore_entries);
        settingsDefaultDepartmentSpinner = findViewById(R.id.settings_default_department_spinner);
        settingsNotificationSwitch = findViewById(R.id.settings_push_notification_switch);
        settingsLogoutButton = findViewById(R.id.settings_logout_btn);

        android.support.v7.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        //set own Toolbar as ActionBar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle(R.string.action_settings);

        String currentUser = CurrentUser.getUsername();
        userPref = getSharedPreferences(currentUser + "Pref", MODE_PRIVATE);
        userName = userPref.getString("Username", "none");
        announcementBoard = userPref.getString("AnnouncementBoard", "none");
        pushEnabled = userPref.getBoolean("PushEnabled", true);


        AnnouncementDatabase database = AnnouncementDatabase.getInstance(this);
        final AnnouncementDao announcementDao = database.announcementDao();

        List<String> menuEntryTitles = _fetchChildEntryTitles();
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
                long sevenDaysInMillis = 7 * 24 * 3600 * 1000;
                Date deleteBefore = new Date(new Date().getTime() - sevenDaysInMillis);
                announcementDao.deleteOlderAnnouncements(deleteBefore);
            }
        });
        settingsRestoreEntriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        settingsLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CurrentUser.setUsername(null);
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
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
        Intent myIntent = new Intent(SettingsActivity.this, NoticeBoardMainActivity.class);
        SettingsActivity.this.startActivity(myIntent);
        finish();
    }

    private List<String> _fetchChildEntryTitles() {
        List<String> titleList = new ArrayList<String>();
        List<MenuEntry> menuEntries = _fetchMenuEntries();
        HashSet<Integer> parentIds = new HashSet<Integer>();
        // get all Parent IDs
        for (MenuEntry menuEntry: menuEntries) {
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
        editor.remove("AnnouncementBoard").commit();
        editor.putString("AnnouncementBoard", item).commit();
        //AnnouncementTopic.setTopic(item);
        retrnTopic = item;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
