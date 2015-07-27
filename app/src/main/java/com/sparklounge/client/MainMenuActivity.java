package com.sparklounge.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.facebook.login.LoginManager;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.util.List;

public class MainMenuActivity extends ActionBarActivity {


    public static SharedPreferences prefs;
    public static LruCache<String, Bitmap> memoryCache;
    public static List<String> queue;
    public static String s3AccessKeyId;
    public static String s3SecretAccessKey;
    public static String s3SessionToken;
    public static TransferManager s3Client;
    public static Resources resources;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        resources = getResources();
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize =  maxMemory / 2;
        memoryCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap bitmap){
                return bitmap.getByteCount()/ 1024;
            }
        };
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
            return;
        }

        s3AccessKeyId = prefs.getString("s3_access_key_id", null);
        s3SecretAccessKey = prefs.getString("s3_secret_access_key", null);
        s3SessionToken = prefs.getString("s3_session_token", null);
        String expiryTimeString = prefs.getString("s3_expiration", null);
        if ((s3AccessKeyId == null) || (s3SecretAccessKey == null) || (s3SessionToken == null)
                || (expiryTimeString == null) || (Long.valueOf(expiryTimeString) <= System.currentTimeMillis())) {
            new S3CredentialsRequestTask().execute();
        }

    }
    @Override
    protected void onStart(){
        super.onStart();
//        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        String accessTokenString = prefs.getString("access_token", null);
//
//        //If no access token
//        if (accessTokenString == null) {
//            logoutRequest(findViewById(R.id.main_menu_view));
//            return;
//        }

        //get s3TemporaryCredentials



    }
    @Override
    protected void onRestart() {
        super.onRestart();
//        if (prefs == null){
//            prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        }
//        String accessTokenString = prefs.getString("access_token", null);
//        if (accessTokenString == null) {
//            logoutRequest(findViewById(R.id.main_menu_view));
//            return;
//        }

        //get s3TemporaryCredentials

//        s3AccessKeyId = prefs.getString("s3_access_key_id", null);
//        s3SecretAccessKey = prefs.getString("s3_secret_access_key", null);
//        s3SessionToken = prefs.getString("s3_session_token", null);
//        String expiryTimeString = prefs.getString("s3_expiration", null);
//        if ((s3AccessKeyId == null) || (s3SecretAccessKey == null) || (s3SessionToken == null)
//                || (expiryTimeString == null) || (Long.valueOf(expiryTimeString) <= System.currentTimeMillis())) {
//            new S3CredentialsRequestTask().execute();
//        }

    }

    public void startContent(View view){
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
        if (prefs != null) {
            prefs.edit().clear().apply();
        }
        if (memoryCache != null) {
            memoryCache.evictAll();
        }
        prefs = null;
        memoryCache = null;
        queue = null;
        s3AccessKeyId = null;
        s3SecretAccessKey = null;
        s3SessionToken = null;
        s3Client = null;


        Intent intent = new Intent(MainMenuActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public static class S3CredentialsRequestTask extends AsyncTask<Void, Void, AwsCredentials> {

        @Override
        protected AwsCredentials doInBackground(Void... params){
            try {
                final String getS3CredentialsUrl = resources.getString(R.string.base_api_url) + "/gets3credentials";
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
                Long expTime = System.currentTimeMillis() + Long.valueOf(resources.getString(R.string.s3_credentials_duration));
                prefs.edit().putString("s3_expiration", String.valueOf(expTime)).apply();
                prefs.edit().putString("s3_access_key_id", s3Credentials.getAccessKeyId()).apply();
                prefs.edit().putString("s3_secret_access_key", s3Credentials.getSecretAccessKey()).apply();
                prefs.edit().putString("s3_session_token", s3Credentials.getSessionToken()).apply();
                s3AccessKeyId = prefs.getString("s3_access_key_id", null);
                s3SecretAccessKey = prefs.getString("s3_secret_access_key", null);
                s3SessionToken = prefs.getString("s3_session_token", null);
                s3Client = new TransferManager(new BasicSessionCredentials(
                        s3AccessKeyId, s3SecretAccessKey, s3SessionToken));
                if (queue == null || queue.isEmpty()) {
                    AsyncTaskQueue.QueueTask();
                }
            }
        }
    }




//    class S3CredentialsAndStartRequestTask extends AsyncTask<Void, Void, AwsCredentials> {
//
//        @Override
//        protected AwsCredentials doInBackground(Void... params){
//            try {
//                final String getS3CredentialsUrl = getResources().getString(R.string.base_api_url) + "/gets3credentials";
//                RestTemplate restTemplate = new RestTemplate();
//                MappingJackson2HttpMessageConverter jsonToPojo = new MappingJackson2HttpMessageConverter();
////                jsonToPojo.getObjectMapper().setPropertyNamingStrategy(
////                        PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
//                restTemplate.getMessageConverters().add(jsonToPojo);
//                String accessToken = prefs.getString("access_token", null);
//                HttpHeaders headers = new HttpHeaders();
//                headers.add("Authorization", "Bearer " + accessToken);
//                HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
//                ResponseEntity<AwsCredentials> responseEntity;
//                responseEntity = restTemplate.exchange(getS3CredentialsUrl, HttpMethod.GET, httpEntity, AwsCredentials.class);
//                AwsCredentials s3Credentials = responseEntity.getBody();
//                return s3Credentials;
//            } catch (Exception e){
//                Log.e("ViewerActivity", e.getMessage(), e);
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(AwsCredentials s3Credentials){
//            if (s3Credentials != null) {
//                Long expTime = System.currentTimeMillis() + Long.valueOf(getResources().getString(R.string.s3_credentials_duration));
//                prefs.edit().putString("s3_expiration", String.valueOf(expTime)).apply();
//                prefs.edit().putString("s3_access_key_id", s3Credentials.getAccessKeyId()).apply();
//                prefs.edit().putString("s3_secret_access_key", s3Credentials.getSecretAccessKey()).apply();
//                prefs.edit().putString("s3_session_token", s3Credentials.getSessionToken()).apply();
//                Intent intent = new Intent(MainMenuActivity.this, ViewerActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//            }
//        }
//    }

    public static class AsyncTaskQueue{

        public static int tasks = 0;
        public static void QueueTask(){
            tasks++;
            if (tasks == 1){
                if (MainMenuActivity.queue == null || MainMenuActivity.queue.size() < 5) {
                    new MainMenuActivity.QueueRequestTask().execute();
                } else {
                    new MainMenuActivity.UpdateCache().execute();
                }
            }
        }

        public static void nextTask(){
            tasks--;
            if (tasks > 0){
                if (MainMenuActivity.queue.size() < 5) {
                    new MainMenuActivity.QueueRequestTask().execute();
                } else {
                    new MainMenuActivity.UpdateCache().execute();
                }
            }

        }
    }

    public static class UpdateCache extends AsyncTask<Void, Void, String>{
        @Override
        protected String doInBackground(Void... params) {
            try {
                if (s3Client == null) {
                    s3Client = new TransferManager(new BasicSessionCredentials(
                            s3AccessKeyId, s3SecretAccessKey, s3SessionToken));
                }

                int numQueue = queue.size();
                    for (int cacheItr = 0; cacheItr < Math.min(numQueue, 5); cacheItr++) {
                        String nextItem = queue.get(cacheItr);
                        if (memoryCache.get(nextItem) != null){
                            continue;
                        }
                        try {
                            InputStream inputStream = null;
                            if (nextItem.contains("/")) {
                                inputStream = s3Client.getAmazonS3Client().getObject(resources.getString(R.string.s3_upload_bucket_name), nextItem).getObjectContent();
                            } else {
                                inputStream = s3Client.getAmazonS3Client().getObject(resources.getString(R.string.s3_bucket_name), nextItem).getObjectContent();
                            }
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);//                        bitmap = BitmapFactory.decodeFile(imageFile.getPath());
                            if (memoryCache.size() + (bitmap.getByteCount() / 1024) >= memoryCache.maxSize()) {
                                if (cacheItr == 0){
                                    memoryCache.put(nextItem, bitmap);
                                    continue;
                                } else {
                                    return "success";
                                }
                            }

                            memoryCache.put(nextItem, bitmap);
    //                IOUtils.copy(s3Client.getObject(getResources().getString(R.string.s3_bucket_name), currentItem).getObjectContent(), new FileOutputStream(currentFile));
                        } catch (Exception e) {
                            Log.e("MainActivity", e.getMessage(), e);
                            return "failure";
                        }
                    }
            } catch (Exception e){
                Log.e("MainActivity", e.getMessage(), e);
                return "failure";
            }
            return "success";
        }

        @Override
        public void onPostExecute(String status){
            AsyncTaskQueue.nextTask();
        }
    }


    public static class QueueRequestTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... params){
            try {
                final String getQueueUrl = resources.getString(R.string.base_api_url) + "/getitems";
                RestTemplate restTemplate = new RestTemplate();
                MappingJackson2HttpMessageConverter jsonToPojo = new MappingJackson2HttpMessageConverter();
                jsonToPojo.getObjectMapper().setPropertyNamingStrategy(
                        PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
                restTemplate.getMessageConverters().add(jsonToPojo);
                String accessToken = prefs.getString("access_token", null);
                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", "Bearer " + accessToken);
                HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
                ResponseEntity<QueueResp> responseEntity;
                responseEntity = restTemplate.exchange(getQueueUrl, HttpMethod.GET, httpEntity, QueueResp.class);
                List<String> queueList = responseEntity.getBody().getQueue();
                return queueList;
            } catch (Exception e){
                Log.e("ViewerActivity", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<String> queueList){
            if (queueList != null) {
                queue = queueList;
                new UpdateCache().execute();
            } else {
                AsyncTaskQueue.nextTask();
            }
        }
    }


}
