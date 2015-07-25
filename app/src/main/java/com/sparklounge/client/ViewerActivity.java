package com.sparklounge.client;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;


public class ViewerActivity extends ActionBarActivity {

    private SharedPreferences prefs;
    public String s3AccessKeyId;
    public String s3SecretAccessKey;
    public String s3SessionToken;
    private List<String> queue;
    private String currentItem;
    private File currentFile;
    private Bitmap bitmap;
    private TransferManager s3Client;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);
        imageView = (ImageView) findViewById(R.id.imageView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        s3AccessKeyId = prefs.getString("s3_access_key_id", null);
        s3SecretAccessKey = prefs.getString("s3_secret_access_key", null);
        s3SessionToken = prefs.getString("s3_session_token", null);
        String expiryTimeString = prefs.getString("s3_expiration", null);
        if ((s3AccessKeyId == null) || (s3SecretAccessKey == null) || (s3SessionToken == null)
                || (expiryTimeString == null) || (Long.valueOf(expiryTimeString) <= System.currentTimeMillis())) {
            new S3CredentialsRequestTask().execute();
        }

        if (s3Client == null) {
            s3Client = new TransferManager(new BasicSessionCredentials(
                    s3AccessKeyId, s3SecretAccessKey, s3SessionToken));
        }

        if (queue == null || queue.isEmpty()) {
            new QueueRequestTask().execute();
        }
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        s3AccessKeyId = prefs.getString("s3_access_key_id", null);
        s3SecretAccessKey = prefs.getString("s3_secret_access_key", null);
        s3SessionToken = prefs.getString("s3_session_token", null);
        String expiryTimeString = prefs.getString("s3_expiration", null);
        if ((s3AccessKeyId == null) || (s3SecretAccessKey == null) || (s3SessionToken == null)
                || (expiryTimeString == null) || (Long.valueOf(expiryTimeString) <= System.currentTimeMillis())){
            new S3CredentialsRequestTask().execute();
        }

        if (s3Client == null) {
            s3Client = new TransferManager(new BasicSessionCredentials(
                    s3AccessKeyId, s3SecretAccessKey, s3SessionToken));
        }

        if (queue == null || queue.isEmpty()){
            new QueueRequestTask().execute();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_viewer, menu);
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

    public void dislikeImage(View view){
        queue.remove(currentItem);
        new PushItemRequestTask().execute(currentItem, "0");
        new GetAndShowNextImage().execute();
        if (queue.isEmpty()){
            new QueueRequestTask().execute();
        }
    }
    public void likeImage(View view){
        queue.remove(currentItem);
        new PushItemRequestTask().execute(currentItem, "1");
        new GetAndShowNextImage().execute();
        if (queue.isEmpty()){
            new QueueRequestTask().execute();
        }

    }

    class GetAndShowNextImage extends AsyncTask<Void, Void, String>{
        @Override
        protected String doInBackground(Void... params) {
            try {
                if (s3Client == null) {
                    s3Client = new TransferManager(new BasicSessionCredentials(
                            s3AccessKeyId, s3SecretAccessKey, s3SessionToken));
                }

                Iterator<String> queueItr = queue.iterator();
                if (queueItr.hasNext()) {
                    currentItem = queueItr.next();
                    try {
                        InputStream inputStream = s3Client.getAmazonS3Client().getObject(getResources().getString(R.string.s3_bucket_name), currentItem).getObjectContent();
                        bitmap = BitmapFactory.decodeStream(inputStream);//                        bitmap = BitmapFactory.decodeFile(imageFile.getPath());
//                IOUtils.copy(s3Client.getObject(getResources().getString(R.string.s3_bucket_name), currentItem).getObjectContent(), new FileOutputStream(currentFile));
                    } catch (Exception e) {
                        Log.e("ViewerActivity", e.getMessage(), e);
                    }
                }
                if (imageView == null) {
                    imageView = (ImageView) findViewById(R.id.imageView);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        imageView.setImageBitmap(bitmap);
                    }
                });

            } catch (Exception e){
                Log.e("ViewerActivity", e.getMessage(), e);
            }
            return "success";
        }
    }


    class PushItemRequestTask extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... params) {
            try {
                final String pushItemUrl = getResources().getString(R.string.base_api_url) + "/pushitem";
                RestTemplate restTemplate = new RestTemplate();
                MappingJackson2HttpMessageConverter jsonToPojo = new MappingJackson2HttpMessageConverter();
                jsonToPojo.getObjectMapper().setPropertyNamingStrategy(
                        PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
                restTemplate.getMessageConverters().add(jsonToPojo);
                String accessToken = prefs.getString("access_token", null);
                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", "Bearer " + accessToken);
                headers.add("Item", params[0]);
                headers.add("Like", params[1]);
                HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
                ResponseEntity<String> responseEntity;
                responseEntity = restTemplate.exchange(pushItemUrl, HttpMethod.GET, httpEntity, String.class);
                String response = responseEntity.getBody();
                return response;
            } catch (Exception e) {
                Log.e("ViewerActivity", e.getMessage(), e);
            }
            return null;
        }
    }
    class S3CredentialsRequestTask extends AsyncTask<Void, Void, AwsCredentials> {

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
            }
        }
    }

    class QueueRequestTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... params){
            try {
                final String getQueueUrl = getResources().getString(R.string.base_api_url) + "/getitems";
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
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<String> queueList){
            if (queueList != null) {
                queue = queueList;
                new GetAndShowNextImage().execute();
            }
        }
    }
}
