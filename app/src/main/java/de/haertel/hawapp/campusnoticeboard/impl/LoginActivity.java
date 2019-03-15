package de.haertel.hawapp.campusnoticeboard.impl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import de.haertel.hawapp.campusnoticeboard.R;
import de.haertel.hawapp.campusnoticeboard.util.AnnouncementTopic;
import de.haertel.hawapp.campusnoticeboard.util.CurrentUser;
import de.haertel.hawapp.campusnoticeboard.util.FirstStart;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mLoginFormView;
    SharedPreferences mypreferences;
    private final static String MARTIN = "Martin";
    private final static String KHELIL = "Khelil";
    private final static String JUSTUS = "Justus";
    private final static String CURRENT_USER_PREF = "CurrentUserPref";
    private final static String CURRENT_USER = "CurrentUser";
    SharedPreferences martinPref;
    SharedPreferences khelilPref;
    SharedPreferences justusPref;


    @SuppressLint("ApplySharedPref")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SharedPreferences myPref = getSharedPreferences(getString(R.string.preferenceName), MODE_PRIVATE);
        if (!myPref.contains(getString(R.string.preferenceKeyFirstStart))) {
            FirstStart.setFirstStart(true);
        }

        //mypreferences = getSharedPreferences(getString(R.string.preferenceName), MODE_PRIVATE);
        // Bei erstem Start der App werden die SharedPreferences initialisiert, die die User speichern.
        if (FirstStart.isFirstStart()) {
//            SharedPreferences sharedPref = getSharedPreferences(CURRENT_USER_PREF, MODE_PRIVATE);
//            SharedPreferences.Editor editor;
//            editor = sharedPref.edit();
//            editor.putString(CURRENT_USER, "none");
//            editor.commit();
            _createSharedPref(MARTIN);
            _createSharedPref(KHELIL);
            _createSharedPref(JUSTUS);
        }


        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
    }

    private void _createSharedPref(String pUsername) {

        String board = "IoT";
        switch (pUsername){
            case MARTIN:
                board = "Informatik, M.Sc.";
                break;
            case KHELIL:
                board = "IoT";
                break;
            case JUSTUS:
                board = "Betriebswirtschaft, B.Sc.";
                break;
        }
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;
        sharedPreferences = getSharedPreferences(pUsername + "Pref" , Context.MODE_PRIVATE);

        editor = sharedPreferences.edit();
        editor.putString("Username", pUsername);
        editor.putBoolean("PushEnabled", true);
        editor.putString("AnnouncementBoard", board);
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
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


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
            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.execute((Void) null);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mUsername = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            String announcementBoard;
            if (success) {
                CurrentUser.setUsername(mUsername);
                if (FirstStart.isFirstStart()) {
                    SharedPreferences userPref = getSharedPreferences(mUsername + "Pref", MODE_PRIVATE);
                    announcementBoard = userPref.getString("AnnouncementBoard", "none");
                    AnnouncementTopic.setTopic(announcementBoard);
                }
                Intent myIntent = new Intent(LoginActivity.this, NoticeBoardMainActivity.class);
                LoginActivity.this.startActivity(myIntent);
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}

