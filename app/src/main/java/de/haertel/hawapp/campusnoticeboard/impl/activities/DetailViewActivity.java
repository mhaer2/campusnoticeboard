package de.haertel.hawapp.campusnoticeboard.impl.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Objects;

import de.haertel.hawapp.campusnoticeboard.R;

/**
 * Activity für die Detailansicht einer Bekanntmachung
 */
public class DetailViewActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "de.haertel.hawapp.campusnoticeboard.impl.EXTRA_ID";
    public static final String EXTRA_HEADLINE = "de.haertel.hawapp.campusnoticeboard.impl.EXTRA_HEADLINE";
    public static final String EXTRA_AUTHOR = "de.haertel.hawapp.campusnoticeboard.impl.EXTRA_AUTHOR";
    public static final String EXTRA_MESSAGE = "de.haertel.hawapp.campusnoticeboard.impl.EXTRA_MESSAGE";
    public static final String EXTRA_DATE = "de.haertel.hawapp.campusnoticeboard.impl.EXTRA_DATE";
    public static final String EXTRA_NOTICEBOARD = "de.haertel.hawapp.campusnoticeboard.impl.EXTRA_NOTICEBOARD";
    public static final String EXTRA_DATE_COUNT = "de.haertel.hawapp.campusnoticeboard.impl.EXTRA_DATE_COUNT";
    public static final String SHARE_ANNOUNCEMENT = "Share Announcement";
    public static final String SHARE_TEXT_TYPE = "text/plain";
    private final static String LOGOUT_ACTION = "de.haertel.hawapp.campusnoticeboard.impl.ACTION_LOGOUT";
    private Intent intent;

    /**
     * In der Methode werden die Views mit Inhalten befüllt, die via Intent übergeben wurden.
     *
     * @param savedInstanceState Bundle mit gespeichertem Zustand
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);

        _addLogoutReceiver();
        TextView detailViewHeadline = findViewById(R.id.detail_title);
        TextView detailViewAuthor = findViewById(R.id.detail_author);
        TextView detailViewMessage = findViewById(R.id.detail_message);
        TextView detailViewDaysCount = findViewById(R.id.detail_day_count);
        TextView detailViewDate = findViewById(R.id.detail_date);

        android.support.v7.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_detail);
        //set own Toolbar as ActionBar
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        intent = getIntent();

        if (intent.hasExtra(EXTRA_ID)) {
            // Set Title in Toolbar
            setTitle(intent.getStringExtra(EXTRA_NOTICEBOARD));
            detailViewHeadline.setText(intent.getStringExtra(EXTRA_HEADLINE));
            detailViewAuthor.setText(intent.getStringExtra(EXTRA_AUTHOR));
            detailViewMessage.setText(intent.getStringExtra(EXTRA_MESSAGE));
            detailViewDaysCount.setText(intent.getStringExtra(EXTRA_DATE_COUNT));
            detailViewDate.setText(intent.getStringExtra(EXTRA_DATE));
        }


    }

    /**
     * Legt fest, was beim Klicken auf die ICons in der Toolbar.
     * Möglichkeiten hier sind das Zurücknavigieren und das Teilen des Inhalts.
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
            case R.id.share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType(SHARE_TEXT_TYPE);
                String shareBody = intent.getStringExtra(EXTRA_MESSAGE);
                String shareSub = intent.getStringExtra(EXTRA_HEADLINE);
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, SHARE_ANNOUNCEMENT));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Erzeugen des OptionMenüs, welches nur aus dem Share-Button besteht.
     *
     * @param menu das Menu
     * @return true wenn erfolgreich.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.share_menu, menu);
        return true;
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
