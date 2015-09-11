package com.sparklounge.client;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.appevents.AppEventsLogger;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationServices;
import com.sparklounge.client.models.AccessToken;

import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

public class LoginActivity extends ActionBarActivity implements
        ConnectionCallbacks, OnConnectionFailedListener {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public TextView usernameLabel;
    public TextView passwordLabel;
    public EditText usernameForm;
    public EditText passwordForm;
    public Button submitLogin;
    public Button submitRegister;

    public TextView failedRequest;
    private SharedPreferences prefs;
    GoogleCloudMessaging gcm;
    String gcmRegId;
    Context context;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Double mLatitude;
    Double mLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        setContentView(R.layout.activity_login);

//        try {
//            PackageInfo info = getPackageManager().getPackageInfo("com.sparklounge.client",
//                    PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        } catch (NameNotFoundException e) {
//
//        } catch (NoSuchAlgorithmException e) {
//
//        }


    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceBundle){
        super.onRestoreInstanceState(savedInstanceBundle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
    protected void onStart() {
        super.onStart();

        boolean showLogin = false;

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String accessTokenString = prefs.getString("access_token", null);
        if (accessTokenString == null || gcmRegId == null) {
            showLogin = true;
        } else {
            new loginWithTokenTask().execute();
        }

        if (showLogin == true) {
            setLoginPageVisible();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        //Logs "install" and "app activate" App Events
        AppEventsLogger.activateApp(this);
        context = getApplicationContext();

        if (mGoogleApiClient == null){
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }
    }
    @Override
    protected void onRestart(){
        super.onRestart();
        boolean showLogin = false;
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String accessTokenString = prefs.getString("access_token", null);
        if (accessTokenString == null) {
            showLogin = true;
        } else {
            new loginWithTokenTask().execute();
        }

        if (showLogin == true) {
            setLoginPageVisible();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();

        // Logs "app deactivate" App Event.
        AppEventsLogger.deactivateApp(this);
    }
    public void setLoginPageVisible(){
        usernameLabel = (TextView) findViewById(R.id.username_label);
        usernameLabel.setVisibility(View.VISIBLE);
        passwordLabel = (TextView) findViewById(R.id.password_label);
        passwordLabel.setVisibility(View.VISIBLE);
        usernameForm = (EditText) findViewById(R.id.username_form);
        usernameForm.setVisibility(View.VISIBLE);
        passwordForm = (EditText) findViewById(R.id.password_form);
        passwordForm.setVisibility(View.VISIBLE);
        submitLogin = (Button) findViewById(R.id.submit_login);
        submitLogin.setVisibility(View.VISIBLE);
        submitRegister = (Button) findViewById(R.id.submit_register);
        submitRegister.setVisibility(View.VISIBLE);
        findViewById(R.id.fb_login).setVisibility(View.VISIBLE);
    }


    public void loginRequest(View view){
        new LoginRequestTask().execute();
    }

    public void registerRequest(View view) {
        if (checkPlayServices() == true){
            if (mLatitude != null && mLongitude != null) {
                new GcmRegisterTask().execute();
            }
            else {
                Log.e("GoogleApiClient", "GPS Location Failed.");
            }
        } else {
            Log.e("GooglePlayServices", "GooglePlayServices Failed.");
        }
    }

    class loginWithTokenTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params){
            try{
                final String tokenUrl = getResources().getString(R.string.base_api_url) + "/app/refreshtoken";
                RestTemplate restTemplate = new RestTemplate();
                MappingJackson2HttpMessageConverter jsonToPojo = new MappingJackson2HttpMessageConverter();
                jsonToPojo.getObjectMapper().setPropertyNamingStrategy(
                        PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
                restTemplate.getMessageConverters().add(jsonToPojo);
                String accessToken = prefs.getString("access_token", null);
                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", "Bearer " + accessToken);
                HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
                ResponseEntity<String> responseEntity;
                responseEntity = restTemplate.exchange(tokenUrl, HttpMethod.GET, httpEntity, String.class);
                String responseString = responseEntity.getBody();
                return responseString;
            } catch (Exception e){
                Log.e("LoginActivity", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String responseString){
            if (responseString == null){
                setLoginPageVisible();
            } else {
                Intent intent = new Intent(LoginActivity.this, MainMenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

        }

    }

    class LoginRequestTask extends AsyncTask<Void, Void, AccessToken> {
        @Override
        protected AccessToken doInBackground(Void... params){
            try{
                final String loginUrl = getResources().getString(R.string.base_api_url) + "/logintoken";
                RestTemplate restTemplate = new RestTemplate();
                MappingJackson2HttpMessageConverter jsonToPojo = new MappingJackson2HttpMessageConverter();
                jsonToPojo.getObjectMapper().setPropertyNamingStrategy(
                        PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
                restTemplate.getMessageConverters().add(jsonToPojo);

                HttpHeaders headers = new HttpHeaders();

                headers.add("Authorization", new HttpBasicAuthentication(usernameForm.getText().toString(), passwordForm.getText().toString()).getHeaderValue());
                HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
                ResponseEntity<AccessToken> responseEntity;
                responseEntity = restTemplate.exchange(loginUrl, HttpMethod.GET, httpEntity, AccessToken.class);
                AccessToken accessToken = responseEntity.getBody();
                return accessToken;
            } catch (Exception e){
                Log.e("LoginActivity", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(AccessToken accessToken){
            if (accessToken == null){
                failedRequest = (TextView) findViewById(R.id.failed_request);
                failedRequest.setText(getResources().getString(R.string.failed_login));
                failedRequest.setVisibility(View.VISIBLE);
            }
            else {
                prefs.edit().putString("access_token", accessToken.getAccessToken()).apply();
                Intent intent = new Intent(LoginActivity.this, MainMenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

        }
    }

    class RegisterRequestTask extends AsyncTask<Void, Void, RegistrationResp> {

        @Override
        protected RegistrationResp doInBackground(Void... params){
            try{
                final String signUpUrl = getResources().getString(R.string.base_api_url) + "/signup";
                RestTemplate restTemplate = new RestTemplate();
                MappingJackson2HttpMessageConverter jsonToPojo = new MappingJackson2HttpMessageConverter();
                jsonToPojo.getObjectMapper().setPropertyNamingStrategy(
                        PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
                restTemplate.getMessageConverters().add(jsonToPojo);

                HttpHeaders headers = new HttpHeaders();

                headers.add("Username", usernameForm.getText().toString());
                headers.add("Password", passwordForm.getText().toString());
                headers.add("Longitude", String.valueOf(mLongitude));
                headers.add("Latitude", String.valueOf(mLatitude));
                headers.add("GcmRegId", gcmRegId);
                HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
                ResponseEntity<RegistrationResp> responseEntity;
                responseEntity = restTemplate.exchange(signUpUrl, HttpMethod.GET, httpEntity, RegistrationResp.class);
                RegistrationResp registrationResp = responseEntity.getBody();
                return registrationResp;
            } catch (Exception e){
                Log.e("LoginActivity", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(RegistrationResp registrationResp){
            if (registrationResp == null || registrationResp.getStatus().equals("duplicate")){
                failedRequest = (TextView) findViewById(R.id.failed_request);
                failedRequest.setText(getResources().getString(R.string.failed_register));
                        failedRequest.setVisibility(View.VISIBLE);
            }
            else {
                new LoginRequestTask().execute();
            }

        }
    }

    private class GcmRegisterTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... args) {
            String newGcmRegId = null;
            try {
                gcm = GoogleCloudMessaging.getInstance(context);
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
                new RegisterRequestTask().execute();
                }
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
}
