package de.haertel.hawapp.campusnoticeboard.impl.activities;

import android.app.ActivityManager;
import android.app.Notification;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
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


/**
 * Activity für die Hauptansicht der Anwendung. Sie stellt een Schwarzes Brett dar
 */
public class NoticeBoardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final static String PREF_PREFIX = "Pref";
    private final static String ANNOUNCEMENT_BOARD = "AnnouncementBoard";
    private final static String USERNAME = "Username";
    private final static String PUSHENABLED = "PushEnabled";
    private final static String DEFAULT_VALUE_BOARD = "none";
    private final static String FIREBASE_REFERENCE = "flamelink/environments/production/content/announcements/en-US";
    private static final String HEADLINE = "headline";
    private static final String AUTHOR = "author";
    private static final String MESSAGE = "message";
    private static final String NOTICEBOARD = "noticeboard";
    private static final String DATE = "date";
    private final static String LOGOUT_ACTION = "de.haertel.hawapp.campusnoticeboard.impl.ACTION_LOGOUT";
    private static final String PATTERN = "yyyy-MM-dd'T'HH:mm";

    private DrawerLayout drawer;
    public static AnnouncementViewModel announcementViewModel;
    private SharedPreferences sharedPreferences;

    private String announcementBoard;
    private DatabaseReference mDatabase;
    private ChildEventListener childEventListener;
    private NotificationManagerCompat notificationManager;

    private AnnouncementAdapter announcementAdapter;

    /**
     * Holt sich alle Buttons und Views der Settings und befüllt sie mit Funktionen.
     * Initialisiert den NavigationDrawer und befüllt ihn mit Funktionen.
     * Befüllt die Listen mit den LiveDaten der Datenbank.
     *
     * @param savedInstanceState Bundle mit gespeichertem Zustand
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_board);

        _addLogoutReceiver();

        mDatabase = FirebaseDatabase.getInstance()
                .getReference(FIREBASE_REFERENCE);

        String currentUser = CurrentUser.getUsername();
        SharedPreferences userPref = getSharedPreferences(currentUser + PREF_PREFIX, MODE_PRIVATE);
        String userName = userPref.getString(USERNAME, DEFAULT_VALUE_BOARD);
        announcementBoard = userPref.getString(ANNOUNCEMENT_BOARD, DEFAULT_VALUE_BOARD);

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


        //Recycler View für die anzeige der Previews im Schwarzen Brett
        RecyclerView recyclerView = findViewById(R.id.announcement_preview_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        announcementAdapter = new AnnouncementAdapter();
        recyclerView.setAdapter(announcementAdapter);

        announcementViewModel = ViewModelProviders.of(this).get(AnnouncementViewModel.class);

        AnnouncementTopic.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (FirstStart.isFirstStart()) {
                    //Falls noch nicht initialisiert
                    if (evt.getOldValue().equals(DEFAULT_VALUE_BOARD)) {
                        FirstStart.setFirstStart(false);
                        sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.preferenceName), MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove(getString(R.string.lastInsert));
                        editor.putLong(getString(R.string.lastInsert), LastInsert.getLastInsert().getTime()).apply();
                        editor.putBoolean(getString(R.string.preferenceKeyFirstStart), false);
                        editor.apply();
                        _addNewDatabaseEntryListener();
                    }
                }

                announcementViewModel.getAllAnnouncementsForTopic(AnnouncementTopic.getTopic()).observe(NoticeBoardActivity.this, new Observer<List<Announcement>>() {
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
                Intent intent = new Intent(NoticeBoardActivity.this, DetailViewActivity.class);
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
            AnnouncementTopic.setTopic(userPref.getString(ANNOUNCEMENT_BOARD, DEFAULT_VALUE_BOARD));
            _addNewDatabaseEntryListener();
        }

        // initialisieren des Navigation-Views
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        ViewGroup navigationViewHeader = (ViewGroup) navigationView.getHeaderView(0);
        View parentView = navigationViewHeader.getRootView();
        TextView navigationHeaderUsername = (TextView) parentView.findViewById(R.id.navigation_header_username);
        Button navigationHeaderDefaultDepartmentButton = (Button) parentView.findViewById(R.id.navigation_header_defaultDepartment_button);
        navigationHeaderDefaultDepartmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnnouncementTopic.setTopic(announcementBoard);
                NoticeBoardActivity.this.setTitle(announcementBoard);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
            }
        });

        setupDrawerContent(navigationView);
        NavigationMenuDataHandler navigationMenuDataHandler = new NavigationMenuDataHandler(this);
        navigationMenuDataHandler.handleNavigationMenuData();
        navigationMenuDataHandler.initNavigationListListener();

        navigationHeaderUsername.setText(userName);
        navigationHeaderDefaultDepartmentButton.setText(announcementBoard);


    }

    /**
     * OnStart methode der Activity
     */
    @Override
    protected void onStart() {
        super.onStart();
        _setNavigationHeaderButtonText();
    }

    /**
     * OnResume Methode der Activity.
     * setzt das Topic der Announcements und den Titel der Toolbar.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (!FirstStart.isFirstStart()) {
            SharedPreferences currentUserPref = getSharedPreferences(CurrentUser.getUsername() + PREF_PREFIX, MODE_PRIVATE);
            AnnouncementTopic.initTopic(currentUserPref.getString(ANNOUNCEMENT_BOARD, DEFAULT_VALUE_BOARD));
        }
        this.setTitle(AnnouncementTopic.getTopic());
        _setNavigationHeaderButtonText();
    }


    /**
     * Ändert den ButtonText des Buttons auf das aktuelle Topic, der immer auf das Default Department lenk.
     */
    private void _setNavigationHeaderButtonText() {
        String currentUser = CurrentUser.getUsername();
        final SharedPreferences userPref = getSharedPreferences(currentUser + PREF_PREFIX, MODE_PRIVATE);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        ViewGroup navigationViewHeader = (ViewGroup) navigationView.getHeaderView(0);
        View parentView = navigationViewHeader.getRootView();
        Button navigationHeaderDefaultDepartmentButton = (Button) parentView.findViewById(R.id.navigation_header_defaultDepartment_button);
        navigationHeaderDefaultDepartmentButton.setText(userPref.getString(ANNOUNCEMENT_BOARD, DEFAULT_VALUE_BOARD));

        navigationHeaderDefaultDepartmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnnouncementTopic.setTopic(userPref.getString(ANNOUNCEMENT_BOARD, DEFAULT_VALUE_BOARD));
                NoticeBoardActivity.this.setTitle(userPref.getString(ANNOUNCEMENT_BOARD, DEFAULT_VALUE_BOARD));
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
        Announcement initEntry = new Announcement(DEFAULT_VALUE_BOARD, DEFAULT_VALUE_BOARD, DEFAULT_VALUE_BOARD, new Date(), DEFAULT_VALUE_BOARD);
        announcementViewModel.insert(initEntry);
        announcementViewModel.delete(initEntry);
    }

    /**
     * Hinzufügen eines Listener zum Drawer, der den Drawer schließt wenn auf ein Item geklickt wird
     *
     * @param navigationView die View mit den Items
     */
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
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

    /**
     * Erzeugen des OptionMenüs.
     *
     * @param menu das Menu
     * @return true wenn erfolgreich.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notice_board_scrolling, menu);
        return true;
    }

    /**
     * Legt fest, was beim Klicken auf die Icons in der Toolbar.
     *
     * @param item das MenüItem auf das geklickt wurde
     * @return true wenn Item selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.open_settings:
                Intent intent = new Intent(NoticeBoardActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /**
     * nicht implementiert
     *
     * @param menuItem das Item
     * @return immer False
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }


    /**
     * Fügt einen Listener hinzu, der darauf horcht, ob neue Einträge zur Datenbank hinzugefügt worden sind.
     * Falls neue Einträge in Firebase gemacht wurden, werden sie auch in die SQLite DB gespeichert.
     * Falls ein neuer Eintrag gemacht wurde und Push aktiviert ist wird eine Notification losgesendet.
     */
    private void _addNewDatabaseEntryListener() {

        if (childEventListener != null) {
            mDatabase.removeEventListener(childEventListener);
        }

        childEventListener = mDatabase.addChildEventListener(new ChildEventListener() {
            /**
             * Aufgerufen wenn ein Kind hinzugefügt wurde.
             *
             * @param dataSnapshot der Snapschot der Daten
             * @param s der key
             */
            @Override
            @SuppressWarnings("unchecked")
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {

                DateFormat dateFormat = new SimpleDateFormat(PATTERN, Locale.getDefault());
                dateFormat.setTimeZone(TimeZone.getDefault());
                Map<String, String> map = (Map) dataSnapshot.getValue();

                Date dateOfNewInsert = null;
                try {
                    dateOfNewInsert = dateFormat.parse(Objects.requireNonNull(map).get(DATE));
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
                    String authorOfNewInsert = map.get(AUTHOR);
                    String headlineOfNewInsert = map.get(HEADLINE);
                    String messageOfNewInsert = map.get(MESSAGE);
                    String noticeboardOfNewInsert = map.get(NOTICEBOARD);
                    Announcement newInsert = new Announcement
                            (headlineOfNewInsert, authorOfNewInsert, messageOfNewInsert, dateOfNewInsert, noticeboardOfNewInsert);
                    new InsertNewEntryAsyncTask(announcementViewModel).execute(newInsert);


                    SharedPreferences shared = getApplicationContext().getSharedPreferences(getString(R.string.preferenceName), MODE_PRIVATE);
                    SharedPreferences.Editor editor = shared.edit();
                    editor.remove(getString(R.string.lastInsert));
                    editor.putLong(getString(R.string.lastInsert), new Date().getTime()).apply();
                    LastInsert.setLastInsert(new Date(shared.getLong(getString(R.string.lastInsert), new Date().getTime())));


                    SharedPreferences currentUserPref = getSharedPreferences(CurrentUser.getUsername() + PREF_PREFIX, MODE_PRIVATE);

                    if (currentUserPref.getBoolean(PUSHENABLED, true)) {
                        sendOnChannel1(headlineOfNewInsert, messageOfNewInsert);
                    }

                }
            }

            /**
             * nicht implementiert
             */
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            /**
             * nicht implementiert
             */
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            /**
             * nicht implementiert
             */
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            /**
             * nicht implementiert
             */
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    /**
     * Asynchroner Task, der das Insert ausführt wenn ein neuer Eintrag gemacht wurde
     */
    private static class InsertNewEntryAsyncTask extends AsyncTask<Announcement, Void, Void> {
        private AnnouncementViewModel announcementViewModel;

        /**
         * Konstruktor
         *
         * @param pAnnouncementViewModel das ViewModel
         */
        private InsertNewEntryAsyncTask(AnnouncementViewModel pAnnouncementViewModel) {
            announcementViewModel = pAnnouncementViewModel;
        }

        /**
         * Führt den Insert aus
         *
         * @param pAnnouncements die Bekanntmachung
         * @return void
         */
        @Override
        protected Void doInBackground(Announcement... pAnnouncements) {
            announcementViewModel.insert(pAnnouncements[0]);
            return null;
        }
    }

    /**
     * Entfernt den Listener beim Zerstören der Activity.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabase.removeEventListener(childEventListener);
    }

    /**
     * Sendet die Push Benachrichtigung los.
     *
     * @param pTitle   den Titel der Nachricht.
     * @param pMessage die Message der Nachricht.
     */
    public void sendOnChannel1(String pTitle, String pMessage) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.mipmap.ic_logo_round)
                .setContentTitle(pTitle)
                .setContentText(pMessage)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        notificationManager.notify(1, notification);
    }

    /**
     * Checks if the application is being sent in the background (i.e behind
     * another application's Activity).
     *
     * @param context the context
     * @return <code>true</code> if another application will be above this one.
     */
    public static boolean isApplicationSentToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        @SuppressWarnings("deprecation")
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            return !topActivity.getPackageName().equals(context.getPackageName());
        }
        return false;
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
