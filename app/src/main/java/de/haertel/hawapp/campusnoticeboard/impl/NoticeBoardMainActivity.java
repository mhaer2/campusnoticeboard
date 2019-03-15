package de.haertel.hawapp.campusnoticeboard.impl;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.haertel.hawapp.campusnoticeboard.R;
import de.haertel.hawapp.campusnoticeboard.impl.navigationMenu.data.NavigationMenuDataHandler;
import de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data.Announcement;
import de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data.AnnouncementDao;
import de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data.AnnouncementViewModel;
import de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.presentation.AnnouncementAdapter;
import de.haertel.hawapp.campusnoticeboard.util.AnnouncementTopic;
import de.haertel.hawapp.campusnoticeboard.util.CurrentUser;
import de.haertel.hawapp.campusnoticeboard.util.FirstStart;


public class NoticeBoardMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    private AnnouncementViewModel announcementViewModel;
    private SharedPreferences sharedPreferences;
    private SharedPreferences currentUserPref;
    private SharedPreferences userPref;
    private final static String CURRENT_USER_PREF = "CurrentUserPref";
    private String currentUser;

    private String userName;
    private String announcementBoard;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_board_main);

        currentUser = CurrentUser.getUsername();
        userPref = getSharedPreferences(currentUser + "Pref", MODE_PRIVATE);
        userName = userPref.getString("Username", "none");
        announcementBoard = userPref.getString("AnnouncementBoard", "none");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //set own Toolbar as ActionBar
        setSupportActionBar(toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //create HamburgerButton to toggle the NavigationDrawer and add it to the Toolbar
        ActionBarDrawerToggle toggleButton = new ActionBarDrawerToggle(this, drawer,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.addDrawerListener(toggleButton);
        toggleButton.syncState();

        RecyclerView recyclerView = findViewById(R.id.announcement_preview_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //recyclerView.setHasFixedSize(true);
        final AnnouncementAdapter announcementAdapter = new AnnouncementAdapter();
        recyclerView.setAdapter(announcementAdapter);

        announcementViewModel = ViewModelProviders.of(this).get(AnnouncementViewModel.class);

        AnnouncementTopic.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (FirstStart.isFirstStart()){
                    if (evt.getOldValue().equals("none")){
                        FirstStart.setFirstStart(false);
                        sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.preferenceName), MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(getString(R.string.preferenceKeyFirstStart), false);
                        // Save the changes in SharedPreferences
                        editor.apply();
                    }
                }

                announcementViewModel.getAllAnnouncementsForTopic(AnnouncementTopic.getTopic()).observe(NoticeBoardMainActivity.this, new Observer<List<Announcement>>() {
                    @Override
                    public void onChanged(@Nullable List<Announcement> pAnnouncements) {
                        announcementAdapter.setAnnouncements(pAnnouncements);
                        if (drawer.isDrawerOpen(GravityCompat.START)) {
                            drawer.closeDrawer(GravityCompat.START);
                        }
                    }
                });
            }

        });


        // Falls die Shared Preference noch nicht angelegt wurde
        // (nur der Fall, wenn Datenbank davor noch nicht existent),
        // soll die Datenbank initialisiert werden.

       // sharedPreferences = getSharedPreferences(getString(R.string.preferenceName), MODE_PRIVATE);
        if (FirstStart.isFirstStart()){ //!sharedPreferences.contains(getString(R.string.preferenceKeyFirstStart))) {
            _performActionForDatabaseInit();
        } else {
            // Ansonsten Default-Topic einstellen.
            AnnouncementTopic.setTopic(announcementBoard);
            //_addNewDatabaseEntryListener();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        ViewGroup navigationViewHeader = (ViewGroup) navigationView.getHeaderView(0);
        View parentView = navigationViewHeader.getRootView();
        TextView navigationHeaderUsername = (TextView) parentView.findViewById(R.id.navigation_header_username);
        Button navigationHeaderDefaultDepartmentButton = (Button) parentView.findViewById(R.id.navigation_header_defaultDepartment_button);


        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
        NavigationMenuDataHandler navigationMenuDataHandler = new NavigationMenuDataHandler(this);
        navigationMenuDataHandler.handleNavigationMenuData();
        navigationMenuDataHandler.initNavigationListListener();

        navigationHeaderUsername.setText(userName);
        navigationHeaderDefaultDepartmentButton.setText(announcementBoard);


    }


    /**
     * Um die Room Datenbank mittel Callback zu initialisieren und somit mit Daten zu befüllen,
     * muss eine Datenbank Aktion durchgeführt werden,
     * da ansonsten die onCreate Methode des Callbacks nicht aufgerufen wird.
     * Daher wird ein Insert mit nachfolgendem Delete eines DummyEntrys durchgeführt.
     */
    private void _performActionForDatabaseInit() {


        Announcement initEntry = new Announcement("none", "none", "none", new Date(), "none");
        announcementViewModel.insert(initEntry);
        announcementViewModel.delete(initEntry);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        //revision: this don't works, use setOnChildClickListener() and setOnGroupClickListener() above instead
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        drawer.closeDrawers();
                        return true;
                    }
                }
        );
    }


    /**
     * Falls NavigationBar offen,
     * wird beim benutzen des Android-Back-Buttons nicht die Anwendung geschlossen,
     * sondern die NavigationBar wieder zugeklappt.
     */
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notice_board_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

//    private void _addNewDatabaseEntryListener() {
//        DatabaseReference mDatabase = FirebaseDatabase.getInstance()
//                .getReference("flamelink/environments/production/content/announcements/en-US");
//        mDatabase.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                String pattern = "yyyy-MM-dd'T'HH:mm";
//                DateFormat dateFormat = new SimpleDateFormat(pattern, new Locale("de", "DE"));
//                Map<String, String> map = (Map) dataSnapshot.getValue();
//
//                String authorOfNewInsert = map.get("author");
//                String headlineOfNewInsert = map.get("headline");
//                String messageOfNewInsert = map.get("message");
//                Date dateOfNewInsert = null;
//                try {
//                    dateOfNewInsert = dateFormat.parse(map.get("date"));
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//                String noticeboardOfNewInsert = map.get("noticeboard");
//
//                Announcement newInsert = new Announcement
//                        (headlineOfNewInsert, authorOfNewInsert, messageOfNewInsert, dateOfNewInsert, noticeboardOfNewInsert);
//                announcementViewModel.insert(newInsert);
//                //new InsertNewEntryAsyncTask(announcementViewModel).execute(newInsert);
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }



//    private static class InsertNewEntryAsyncTask extends AsyncTask<Announcement, Void, Void> {
//        private AnnouncementViewModel announcementViewModel;
//
//        private InsertNewEntryAsyncTask(AnnouncementViewModel pAnnouncementViewModel) {
//            announcementViewModel = pAnnouncementViewModel;
//        }
//        @Override
//        protected Void doInBackground(Announcement... pAnnouncements) {
//            announcementViewModel.insert(pAnnouncements[0]);
//            return null;
//        }
//    }
}
