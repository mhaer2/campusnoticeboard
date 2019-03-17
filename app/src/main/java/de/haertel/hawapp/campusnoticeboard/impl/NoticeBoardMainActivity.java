package de.haertel.hawapp.campusnoticeboard.impl;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import de.haertel.hawapp.campusnoticeboard.R;
import de.haertel.hawapp.campusnoticeboard.impl.navigationMenu.data.NavigationMenuDataHandler;
import de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data.Announcement;
import de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data.AnnouncementViewModel;
import de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.presentation.AnnouncementAdapter;
import de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.presentation.SwipeToDeleteCallback;
import de.haertel.hawapp.campusnoticeboard.util.AnnouncementTopic;
import de.haertel.hawapp.campusnoticeboard.util.CurrentUser;
import de.haertel.hawapp.campusnoticeboard.util.FirstStart;
import de.haertel.hawapp.campusnoticeboard.util.LastInsert;

import static de.haertel.hawapp.campusnoticeboard.impl.BaseApp.CHANNEL_1_ID;


public class NoticeBoardMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    public static AnnouncementViewModel announcementViewModel;
    private SharedPreferences sharedPreferences;

    private String announcementBoard;
    private DatabaseReference mDatabase;
    private ChildEventListener childEventListener;
    private NotificationManagerCompat notificationManager;

    private RecyclerView recyclerView;
    private AnnouncementAdapter announcementAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_board_main);

        _addLogoutReceiver();

        mDatabase = FirebaseDatabase.getInstance()
                .getReference("flamelink/environments/production/content/announcements/en-US");

        String currentUser = CurrentUser.getUsername();
        SharedPreferences userPref = getSharedPreferences(currentUser + "Pref", MODE_PRIVATE);
        String userName = userPref.getString("Username", "none");
        announcementBoard = userPref.getString("AnnouncementBoard", "none");

        notificationManager = NotificationManagerCompat.from(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        //set own Toolbar as ActionBar
        setSupportActionBar(toolbar);
        this.setTitle(announcementBoard);
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


        recyclerView = findViewById(R.id.announcement_preview_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        announcementAdapter = new AnnouncementAdapter();
        recyclerView.setAdapter(announcementAdapter);

        announcementViewModel = ViewModelProviders.of(this).get(AnnouncementViewModel.class);

        AnnouncementTopic.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (FirstStart.isFirstStart()) {
                    if (evt.getOldValue().equals("none")) {
                        FirstStart.setFirstStart(false);

                        sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.preferenceName), MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove(getString(R.string.lastInsert)).commit();
                        editor.putLong(getString(R.string.lastInsert), LastInsert.getLastInsert().getTime()).commit();

                        editor.putBoolean(getString(R.string.preferenceKeyFirstStart), false);
                        editor.apply();
                        _addNewDatabaseEntryListener();
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

        // Hinzufügen von Swipe Funktionalität (SwipeToDelete) zu den Items
        new ItemTouchHelper(new SwipeToDeleteCallback(this, announcementViewModel, announcementAdapter)).attachToRecyclerView(recyclerView);

        // wenn auf Item aus der Liste geklickt wird, woll die Detailansicht geöffnet werden
        announcementAdapter.setOnItemClickListener(new AnnouncementAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Announcement announcement) {
                Intent intent = new Intent(NoticeBoardMainActivity.this, DetailViewActivity.class);
                intent.putExtra(DetailViewActivity.EXTRA_ID, announcement.getId());
                intent.putExtra(DetailViewActivity.EXTRA_HEADLINE, announcement.getHeadline());
                intent.putExtra(DetailViewActivity.EXTRA_AUTHOR, announcement.getAuthor());
                intent.putExtra(DetailViewActivity.EXTRA_MESSAGE, announcement.getMessage());
                intent.putExtra(DetailViewActivity.EXTRA_DATE, LastInsert.getDateFormat().format(announcement.getDate()));
                intent.putExtra(DetailViewActivity.EXTRA_DATE_COUNT, announcement.getDayCountSincePosted());
                intent.putExtra(DetailViewActivity.EXTRA_NOTICEBOARD, announcement.getNoticeBoard());
                startActivity(intent);
            }
        });

        // Falls die Shared Preference noch nicht angelegt wurde
        // (nur der Fall, wenn Datenbank davor noch nicht existent),
        // soll die Datenbank initialisiert werden.

        if (FirstStart.isFirstStart()) {
            _performActionForDatabaseInit();
        } else {
            // Ansonsten Default-Topic einstellen.
            _performActionForDatabaseInit();
            AnnouncementTopic.setTopic(userPref.getString("AnnouncementBoard", "none"));
            _addNewDatabaseEntryListener();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        ViewGroup navigationViewHeader = (ViewGroup) navigationView.getHeaderView(0);
        View parentView = navigationViewHeader.getRootView();
        TextView navigationHeaderUsername = (TextView) parentView.findViewById(R.id.navigation_header_username);
        Button navigationHeaderDefaultDepartmentButton = (Button) parentView.findViewById(R.id.navigation_header_defaultDepartment_button);
        navigationHeaderDefaultDepartmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnnouncementTopic.setTopic(announcementBoard);
                NoticeBoardMainActivity.this.setTitle(announcementBoard);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
            }
        });

        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
        NavigationMenuDataHandler navigationMenuDataHandler = new NavigationMenuDataHandler(this);
        navigationMenuDataHandler.handleNavigationMenuData();
        navigationMenuDataHandler.initNavigationListListener();

        navigationHeaderUsername.setText(userName);
        navigationHeaderDefaultDepartmentButton.setText(announcementBoard);


    }

    @Override
    protected void onStart() {
        super.onStart();
        _setNavigationHeaderButtonText();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!FirstStart.isFirstStart()) {
            SharedPreferences currentUserPref = getSharedPreferences(CurrentUser.getUsername() + "Pref", MODE_PRIVATE);
            AnnouncementTopic.initTopic(currentUserPref.getString("AnnouncementBoard", "none"));
        }
        this.setTitle(AnnouncementTopic.getTopic());
        _setNavigationHeaderButtonText();
    }


    private void _setNavigationHeaderButtonText() {
        String currentUser = CurrentUser.getUsername();
        final SharedPreferences userPref = getSharedPreferences(currentUser + "Pref", MODE_PRIVATE);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        ViewGroup navigationViewHeader = (ViewGroup) navigationView.getHeaderView(0);
        View parentView = navigationViewHeader.getRootView();
        Button navigationHeaderDefaultDepartmentButton = (Button) parentView.findViewById(R.id.navigation_header_defaultDepartment_button);
        navigationHeaderDefaultDepartmentButton.setText(userPref.getString("AnnouncementBoard", "none"));

        navigationHeaderDefaultDepartmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnnouncementTopic.setTopic(userPref.getString("AnnouncementBoard", "none"));
                NoticeBoardMainActivity.this.setTitle(userPref.getString("AnnouncementBoard", "none"));
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
            }
        });
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
        switch (item.getItemId()) {
            case R.id.open_settings:
                Intent intent = new Intent(NoticeBoardMainActivity.this, SettingsActivity.class);
                //intent.putExtra(SettingsActivity.EXTRA_ANNOUNCEMENT_VIEW_MODEL, this);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    private void _addNewDatabaseEntryListener() {

        if (childEventListener != null) {
            mDatabase.removeEventListener(childEventListener);
        }

        childEventListener = mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {

                String pattern = "yyyy-MM-dd'T'HH:mm";
                DateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
                dateFormat.setTimeZone(TimeZone.getDefault());
                Map<String, String> map = (Map) dataSnapshot.getValue();

                Date dateOfNewInsert = null;
                try {
                    dateOfNewInsert = dateFormat.parse(Objects.requireNonNull(map).get("date"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                TimeZone timeZone = TimeZone.getDefault();
                int offsetMillis = -1 * timeZone.getRawOffset();
                Calendar cal = new GregorianCalendar(); // creates calendar
                cal.setTime(LastInsert.getLastInsert()); // sets calendar time/date
                cal.add(Calendar.MILLISECOND, offsetMillis);
                cal.add(Calendar.HOUR_OF_DAY, 1); // adds one hour

                Date lastInsert = cal.getTime();//LastInsert.getLastInsert();

                if (Objects.requireNonNull(dateOfNewInsert).after(lastInsert)) {
                    String authorOfNewInsert = map.get("author");
                    String headlineOfNewInsert = map.get("headline");
                    String messageOfNewInsert = map.get("message");
                    String noticeboardOfNewInsert = map.get("noticeboard");
                    Announcement newInsert = new Announcement
                            (headlineOfNewInsert, authorOfNewInsert, messageOfNewInsert, dateOfNewInsert, noticeboardOfNewInsert);
                    new InsertNewEntryAsyncTask(announcementViewModel).execute(newInsert);


                    SharedPreferences shared = getApplicationContext().getSharedPreferences(getString(R.string.preferenceName), MODE_PRIVATE);
                    SharedPreferences.Editor editor = shared.edit();
                    editor.remove(getString(R.string.lastInsert));
                    editor.putLong(getString(R.string.lastInsert), new Date().getTime()).commit();
                    LastInsert.setLastInsert(new Date(shared.getLong(getString(R.string.lastInsert), new Date().getTime())));

                    sendOnChannel1(headlineOfNewInsert, messageOfNewInsert);
                    //_showNotification(headlineOfNewInsert, messageOfNewInsert);
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


    private static class InsertNewEntryAsyncTask extends AsyncTask<Announcement, Void, Void> {
        private AnnouncementViewModel announcementViewModel;

        private InsertNewEntryAsyncTask(AnnouncementViewModel pAnnouncementViewModel) {
            announcementViewModel = pAnnouncementViewModel;
        }

        @Override
        protected Void doInBackground(Announcement... pAnnouncements) {
            announcementViewModel.insert(pAnnouncements[0]);
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabase.removeEventListener(childEventListener);
    }


    public void sendOnChannel1(String pTitle, String pMessage) {
        String title = pTitle;
        String message = pMessage;

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.mipmap.ic_logo_round)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        notificationManager.notify(1, notification);
    }

    private void _showNotification(String pTitle, String pMessage) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default",
                    "YOUR_CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DISCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "default")
                .setSmallIcon(R.mipmap.ic_logo_round) // notification icon
                .setContentTitle(pTitle) // title for notification
                .setContentText(pMessage)// message for notification
                //.setSound(alarmSound) // set alarm sound for notification
                .setAutoCancel(true); // clear notification after click
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, mBuilder.build());
    }

    private void _setToolbarTitle(String pTitle) {
        NoticeBoardMainActivity.this.setTitle(pTitle);
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
