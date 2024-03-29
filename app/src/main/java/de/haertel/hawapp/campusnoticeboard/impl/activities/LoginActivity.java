package de.haertel.hawapp.campusnoticeboard.impl.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import java.util.Date;

import de.haertel.hawapp.campusnoticeboard.R;
import de.haertel.hawapp.campusnoticeboard.util.AnnouncementTopic;
import de.haertel.hawapp.campusnoticeboard.util.CurrentUser;
import de.haertel.hawapp.campusnoticeboard.util.FirstStart;
import de.haertel.hawapp.campusnoticeboard.util.LastInsert;

/**
 * A login screen that offers login via username.
 * Basic Android Activity just edited for custom behaviour.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    private EditText mUsernameView;
    private View mLoginFormView;
    private final static String MARTIN = "Martin";
    private final static String KHELIL = "Khelil";
    private final static String JUSTUS = "Justus";
    private final static String IOT = "IoT";
    private final static String IFM_SC = "Informatik, M.Sc.";
    private final static String BWL_BSC = "Betriebswirtschaft, B.Sc.";
    private final static String PREF_PREFIX = "Pref";
    private final static String ANNOUNCEMENT_BOARD = "AnnouncementBoard";
    private final static String USERNAME = "Username";
    private final static String PUSHENABLED = "PushEnabled";
    private final static String DEFAULT_VALUE_BOARD = "none";
    private final static String LOGOUT_ACTION = "de.haertel.hawapp.campusnoticeboard.impl.ACTION_LOGOUT";


    /**
     * In der Methode werden User als SharedPreferences angelegt, falls sie das noch nicht sind.
     * Zudem werden die Komponenten für das Login zugewiesen.
     *
     * @param savedInstanceState savedInstanceState Bundle mit gespeichertem Zustand
     */
    @SuppressLint("ApplySharedPref")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        _addLogoutReceiver();
        SharedPreferences myPref = getSharedPreferences(getString(R.string.preferenceName), MODE_PRIVATE);
        if (!myPref.contains(getString(R.string.preferenceKeyFirstStart))) {
            FirstStart.setFirstStart(true);
        }

        // Bei erstem Start der App werden die SharedPreferences initialisiert, die die User speichern.
        if (FirstStart.isFirstStart()) {
            _createSharedPref(MARTIN);
            _createSharedPref(KHELIL);
            _createSharedPref(JUSTUS);
        } else {
            Long millis = myPref.getLong(getString(R.string.lastInsert), new Date().getTime());
            Date date = new Date(millis);
            LastInsert.setLastInsert(date);
        }

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username);

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
    }

    /**
     * Legt die SharedPreferences für die 3 möglichen User an. und weißt ihnen Topics zu.
     *
     * @param pUsername der Name des Users
     */
    private void _createSharedPref(String pUsername) {


        String board = IOT;
        switch (pUsername) {
            case MARTIN:
                board = IFM_SC;
                break;
            case KHELIL:
                board = IOT;
                break;
            case JUSTUS:
                board = BWL_BSC;
                break;
        }
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;
        sharedPreferences = getSharedPreferences(pUsername + PREF_PREFIX, Context.MODE_PRIVATE);

        editor = sharedPreferences.edit();
        editor.putString(USERNAME, pUsername);
        editor.putBoolean(PUSHENABLED, true);
        editor.putString(ANNOUNCEMENT_BOARD, board);
        editor.apply();

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!(username.equals(MARTIN) || username.equals(KHELIL) || username.equals(JUSTUS))) {
            mUsernameView.setError(getString(R.string.error_incorrect_username));
            focusView = mUsernameView;
            cancel = true;
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mAuthTask = new UserLoginTask(username);
            mAuthTask.execute((Void) null);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    @SuppressLint("StaticFieldLeak")
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;

        /**
         * Konstruktor
         *
         * @param email username
         */
        UserLoginTask(String email) {
            mUsername = email;
        }

        /**
         * not implementet
         *
         * @param params void
         * @return always true
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            return true;
        }

        /**
         * Nach erfolgreicher Authentifizierung wird das Schwarze Brett geladen.
         * Hierfür werden Einstellungen aus den Shared Preferences geladen und gesetzt.
         *
         * @param success
         */
        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            String announcementBoard;
            if (success) {
                CurrentUser.setUsername(mUsername);
                if (FirstStart.isFirstStart()) {
                    SharedPreferences userPref = getSharedPreferences(mUsername + PREF_PREFIX, MODE_PRIVATE);
                    announcementBoard = userPref.getString(ANNOUNCEMENT_BOARD, DEFAULT_VALUE_BOARD);
                    AnnouncementTopic.setTopic(announcementBoard);
                }
                Intent myIntent = new Intent(LoginActivity.this, NoticeBoardActivity.class);
                LoginActivity.this.startActivity(myIntent);

                finish();
            } else {
                mUsernameView.setError(getString(R.string.error_incorrect_username));
                mUsernameView.requestFocus();
            }
        }

        /**
         * setzt AuthentifizierungsTask auf null
         */
        @Override
        protected void onCancelled() {
            mAuthTask = null;
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

