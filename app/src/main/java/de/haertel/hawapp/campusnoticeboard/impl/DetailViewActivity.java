package de.haertel.hawapp.campusnoticeboard.impl;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.ParseException;
import java.util.Date;

import de.haertel.hawapp.campusnoticeboard.R;
import de.haertel.hawapp.campusnoticeboard.util.LastInsert;

public class DetailViewActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "de.haertel.hawapp.campusnoticeboard.impl.EXTRA_ID";
    public static final String EXTRA_HEADLINE = "de.haertel.hawapp.campusnoticeboard.impl.EXTRA_HEADLINE";
    public static final String EXTRA_AUTHOR = "de.haertel.hawapp.campusnoticeboard.impl.EXTRA_AUTHOR";
    public static final String EXTRA_MESSAGE = "de.haertel.hawapp.campusnoticeboard.impl.EXTRA_MESSAGE";
    public static final String EXTRA_DATE = "de.haertel.hawapp.campusnoticeboard.impl.EXTRA_DATE";
    public static final String EXTRA_NOTICEBOARD = "de.haertel.hawapp.campusnoticeboard.impl.EXTRA_NOTICEBOARD";
    public static final String EXTRA_DATE_COUNT = "de.haertel.hawapp.campusnoticeboard.impl.EXTRA_DATE_COUNT";
    private TextView detailViewHeadline;
    private TextView detailViewMessage;
    private TextView detailViewDaysCount;
    private TextView detailViewAuthor;
    private TextView detailViewDate;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);

        detailViewHeadline = findViewById(R.id.detail_title);
        detailViewAuthor = findViewById(R.id.detail_author);
        detailViewMessage = findViewById(R.id.detail_message);
        detailViewDaysCount = findViewById(R.id.detail_day_count);
        detailViewDate = findViewById(R.id.detail_date);

        android.support.v7.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_detail);
        //set own Toolbar as ActionBar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        intent = getIntent();

        if (intent.hasExtra(EXTRA_ID)){
            // Set Title in Toolbar
            setTitle(intent.getStringExtra(EXTRA_NOTICEBOARD));
            detailViewHeadline.setText(intent.getStringExtra(EXTRA_HEADLINE));
            detailViewAuthor.setText(intent.getStringExtra(EXTRA_AUTHOR));
            detailViewMessage.setText(intent.getStringExtra(EXTRA_MESSAGE));
            detailViewDaysCount.setText(intent.getStringExtra(EXTRA_DATE_COUNT));
            detailViewDate.setText(intent.getStringExtra(EXTRA_DATE));
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = intent.getStringExtra(EXTRA_MESSAGE);
                String shareSub = intent.getStringExtra(EXTRA_HEADLINE);
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share Announcement"));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.share_menu, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
