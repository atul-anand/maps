package com.zemoso.atul.maps.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.zemoso.atul.maps.R;
import com.zemoso.atul.maps.javabeans.RegistryUser;
import com.zemoso.atul.maps.singletons.VolleyRequests;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;

/**
 * A login screen that offers login via email/password.
 */
//implements LoaderCallbacks<Cursor>
public class LoginActivity extends AppCompatActivity {

    //region Variable Declaration
    private static final String TAG = LoginActivity.class.getSimpleName();
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    @Nullable
    private UserLoginTask mAuthTask = null;

    private SharedPreferences.Editor mEditor;
    private String mHostname;

    // region UI references
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private TextView mRegistryHeadingView;
    private View mProgressView;
    private View mLoginFormView;
    private Button mSignInButton;
    //endregion

    //endregion

    //region Overridden Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        mHostname = getResources().getString(R.string.url_base_address);
        mEditor.putString("Hostname", mHostname);
        mEditor.apply();

        // Set up the login form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);
//        populateAutoComplete();

        mRegistryHeadingView = (TextView) findViewById(R.id.heading_utm_registry);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });


        mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUsernameView.setError(null);
        mPasswordView.setError(null);
        mUsernameView.setText("");
        mPasswordView.setText("");
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    //endregion

    //region Private Methods
    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }
        Log.d(TAG, "Trying to Log in");
        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
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
            showProgress(true);

            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.loginRequest();
        }
    }

    private boolean isUsernameValid(@NonNull String username) {
        //TODO: Replace this with your own logic
        return !username.contains("@");
    }

    private boolean isPasswordValid(@NonNull String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
//        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
//                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//            }
//        });

        mRegistryHeadingView.setVisibility(show ? View.GONE : View.VISIBLE);
//        mRegistryHeadingView.animate().setDuration(shortAnimTime).alpha(
//                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                mRegistryHeadingView.setVisibility(show ? View.GONE : View.VISIBLE);
//            }
//        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//        mProgressView.animate().setDuration(shortAnimTime).alpha(
//                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            }
//        });
    }
    //endregion

    private class UserLoginTask {
        private final String mUsername;
        private final String mPassword;
        private String client_id;
        private String client_secret;
        private String grant_type;
        private String access_token;
        private String refresh_token;

        UserLoginTask(String username, String password) {
            this.client_id = "utm-android";
            this.client_secret = "utm-android-secret";
            this.grant_type = "password";
            mUsername = username;
            mPassword = password;
        }

        @Override
        public String toString() {
            return "?client_id=" + client_id +
                    "&client_secret=" + client_secret +
                    "&grant_type=" + grant_type +
                    "&username=" + mUsername +
                    "&password=" + mPassword;
        }

        private void loginRequest() {
            String extension = getResources().getString(R.string.url_authentication);
            String url = mHostname + extension;
            url += this.toString();
            Log.d(TAG, url);
            Response.Listener<JSONObject> loginListener = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, String.valueOf(response));
                    access_token = response.optString("access_token");
                    refresh_token = response.optString("refresh_token");
                    Log.d(TAG, "access" + access_token);
                    Log.d(TAG, "refresh" + refresh_token);
                    mEditor.putString("access_token", access_token);
                    mEditor.putString("refresh_token", refresh_token);
                    mEditor.apply();
                    getUserData();
                    mAuthTask = null;
                }
            };
            Response.ErrorListener loginErrorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, String.valueOf(error));
                    showProgress(false);
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                    mAuthTask = null;
//                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                }
            };
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null
                    , loginListener, loginErrorListener);
            VolleyRequests.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest,
                    getResources().getString(R.string.action_sign_in));
        }

        private void getUserData() {
            String extension = getResources().getString(R.string.url_user_profile);
            String url = mHostname + extension;
            url += "?access_token=" + access_token;
            Log.d(TAG, url);
            Response.Listener<JSONObject> userProfileListener = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, String.valueOf(response));
                    RegistryUser registryUser = new RegistryUser(response);
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    realm.insertOrUpdate(registryUser);
                    realm.commitTransaction();
                    realm.close();
                    mEditor.putString("pilot_id", registryUser.getId());
                    mEditor.apply();
                    Log.d(TAG, String.valueOf(registryUser));
                    showProgress(false);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            };
            Response.ErrorListener userProfileErrorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    showProgress(false);
                    mUsernameView.setError(getString(R.string.error_user_profile));
                    mUsernameView.requestFocus();
                }
            };
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    userProfileListener, userProfileErrorListener) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("Content-Type", "application/json");
                    return params;
                }
            };
            VolleyRequests.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest,
                    getResources().getString(R.string.nav_profile));
        }

    }
}

