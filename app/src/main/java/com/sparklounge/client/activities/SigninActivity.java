package com.sparklounge.client.activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationServices;
import com.sparklounge.client.R;
import com.sparklounge.client.RegistrationResp;
import com.sparklounge.client.apis.SparkApi;

import java.io.IOException;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class SigninActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public EditText usernameForm;
    public EditText passwordForm;
    public Button submitLogin;
    public Button submitRegister;

    public TextView failedRequest;
    GoogleCloudMessaging gcm;
    String gcmRegId;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Double mLatitude;
    Double mLongitude;

    public LoginButton fbLogin;
    public CallbackManager callbackManager;
    public LoginManager loginManager;
    private AccessToken fbAccessToken;
    private AccessTokenTracker fbAccessTokenTracker;
    private Profile fbProfile;
    private ProfileTracker fbProfileTracker;
    private SharedPreferences prefs;
    private SparkApi mSparkApi;

/*
    private OnLoggedInListener mOnLoggedInListener;
*/

    public SigninActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initGoogleApi();
        initFacebook();

        setContentView(R.layout.fragment_login2);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        usernameForm = (EditText)findViewById(R.id.username_form);
        passwordForm = (EditText)findViewById(R.id.password_form);
        submitLogin = (Button)findViewById(R.id.submit_login);
        submitRegister = (Button)findViewById(R.id.submit_register);
        failedRequest = (TextView)findViewById(R.id.failed_request);

        submitLogin.setOnClickListener(onLoginClicked);
        submitRegister.setOnClickListener(onRegisterClicked);

    }

    private void goToImagesActivity() {
        Intent intent = new Intent(SigninActivity.this, ImagesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private View.OnClickListener onLoginClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String username = usernameForm.getText().toString();
            String password = passwordForm.getText().toString();
            mSparkApi = new SparkApi(username, password);
            mSparkApi.loginUser(new Callback<com.sparklounge.client.models.AccessToken>() {
                @Override
                public void success(com.sparklounge.client.models.AccessToken accessToken, Response response) {
                    prefs.edit().putString("access_token", accessToken.getAccessToken()).apply();
                    prefs.edit().putString("USER_NAME", usernameForm.getText().toString()).apply();
                    goToImagesActivity();
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e("", "Failed to login user");
                    error.printStackTrace();
                    failedRequest.setText(getResources().getString(R.string.failed_login));
                }
            });
        }
    };

    private View.OnClickListener onRegisterClicked = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            String username = usernameForm.getText().toString();
            String password = passwordForm.getText().toString();
            mSparkApi = new SparkApi();
            registerRequest();
        }
    };

    public void registerRequest() {
        if (checkPlayServices() == true){
            if (mLatitude != null && mLongitude != null) {
                new RegisterTask().execute();
            }
            else {
                Log.e("GoogleApiClient", "GPS Location Failed.");
            }
        } else {
            Log.e("GooglePlayServices", "GooglePlayServices Failed.");
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.e("GooglePlayService", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult){
        Log.e("GoogleApi", "Connection Suspended.");
    }
    @Override
    public void onConnectionSuspended(int i){
        Log.e("GoogleApi", "Connection Suspended.");
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitude = mLastLocation.getLatitude();
            mLongitude = mLastLocation.getLongitude();
        }
    }

    private void initGoogleApi() {
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void initFacebook() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        fbAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                fbAccessToken = currentAccessToken;
            }
        };

        fbAccessToken = AccessToken.getCurrentAccessToken();

        fbProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(
                    Profile oldProfile,
                    Profile currentProfile) {
                fbProfile = currentProfile;
            }
        };

        fbProfile = Profile.getCurrentProfile();
    }

    class RegisterTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... args) {
            String newGcmRegId = null;
            try {
                gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                newGcmRegId = gcm.register(getResources().getString(R.string.gcm_app_sender_id));
                Log.e("RegId", newGcmRegId);
                return newGcmRegId;
            } catch (IOException ex) {
                Log.e("Gcm Registration Error", ex.getMessage());
            }
            return null;
        }
        @Override
        protected void onPostExecute(String newGcmRegId) {
            if (newGcmRegId == null){
                failedRequest = (TextView) findViewById(R.id.failed_request);
                failedRequest.setText(getResources().getString(R.string.failed_register));
                failedRequest.setVisibility(View.VISIBLE);
            } else {
                prefs.edit().putString("GcmRegId", newGcmRegId).apply();
                gcmRegId = newGcmRegId;
                mSparkApi.signup(
                        usernameForm.getText().toString(),
                        passwordForm.getText().toString(),
                        String.valueOf(mLongitude),
                        String.valueOf(mLatitude),
                        gcmRegId, new Callback<RegistrationResp>() {
                            @Override
                            public void success(RegistrationResp registrationResp, Response response) {
                                if (registrationResp == null || registrationResp.getStatus().equals("duplicate")){
                                    failedRequest = (TextView) findViewById(R.id.failed_request);
                                    failedRequest.setText(getResources().getString(R.string.failed_register));
                                    failedRequest.setVisibility(View.VISIBLE);
                                }
                                else {
                                    onLoginClicked.onClick(findViewById(R.id.submit_login));
                                }
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                failedRequest = (TextView) findViewById(R.id.failed_request);
                                failedRequest.setText(getResources().getString(R.string.failed_register));
                                failedRequest.setVisibility(View.VISIBLE);
                            }
                        });
            }
        }
    }
}
