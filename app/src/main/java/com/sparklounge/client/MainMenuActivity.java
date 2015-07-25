package com.sparklounge.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.facebook.login.LoginManager;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class MainMenuActivity extends ActionBarActivity {


    private SharedPreferences prefs;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
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
    protected void onResume(){
        super.onResume();
        if (prefs == null){
            prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        }

        String accessTokenString = prefs.getString("access_token", null);
        if (accessTokenString == null) {
            logoutRequest(findViewById(R.id.main_menu_view));
        }

    }
    @Override
    protected void onStart(){
        super.onStart();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String accessTokenString = prefs.getString("access_token", null);
        if (accessTokenString == null) {
            logoutRequest(findViewById(R.id.main_menu_view));
        }
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        if (prefs == null){
            prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        }
        String accessTokenString = prefs.getString("access_token", null);
        if (accessTokenString == null) {
            logoutRequest(findViewById(R.id.main_menu_view));
        }

    }

    public void startContent(View view){
        String expiryTimeString = prefs.getString("s3_expiration", null);
        if ((expiryTimeString == null) || (Long.valueOf(expiryTimeString) <= System.currentTimeMillis())){
            new S3CredentialsAndStartRequestTask().execute();
            return;
        }
        Intent intent = new Intent(MainMenuActivity.this, ViewerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    public void goToUpload(View view){
        Intent intent = new Intent(MainMenuActivity.this, UploadActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void logoutRequest(View view){
        LoginManager.getInstance().logOut();
        prefs.edit().remove("access_token").apply();
        Intent intent = new Intent(MainMenuActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    class S3CredentialsAndStartRequestTask extends AsyncTask<Void, Void, AwsCredentials> {

        @Override
        protected AwsCredentials doInBackground(Void... params){
            try {
                final String getS3CredentialsUrl = getResources().getString(R.string.base_api_url) + "/gets3credentials";
                RestTemplate restTemplate = new RestTemplate();
                MappingJackson2HttpMessageConverter jsonToPojo = new MappingJackson2HttpMessageConverter();
//                jsonToPojo.getObjectMapper().setPropertyNamingStrategy(
//                        PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
                restTemplate.getMessageConverters().add(jsonToPojo);
                String accessToken = prefs.getString("access_token", null);
                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", "Bearer " + accessToken);
                HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
                ResponseEntity<AwsCredentials> responseEntity;
                responseEntity = restTemplate.exchange(getS3CredentialsUrl, HttpMethod.GET, httpEntity, AwsCredentials.class);
                AwsCredentials s3Credentials = responseEntity.getBody();
                return s3Credentials;
            } catch (Exception e){
                Log.e("ViewerActivity", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(AwsCredentials s3Credentials){
            if (s3Credentials != null) {
                Long expTime = System.currentTimeMillis() + Long.valueOf(getResources().getString(R.string.s3_credentials_duration));
                prefs.edit().putString("s3_expiration", String.valueOf(expTime)).apply();
                prefs.edit().putString("s3_access_key_id", s3Credentials.getAccessKeyId()).apply();
                prefs.edit().putString("s3_secret_access_key", s3Credentials.getSecretAccessKey()).apply();
                prefs.edit().putString("s3_session_token", s3Credentials.getSessionToken()).apply();
                Intent intent = new Intent(MainMenuActivity.this, ViewerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }


}
