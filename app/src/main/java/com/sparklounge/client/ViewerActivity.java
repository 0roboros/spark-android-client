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
import android.widget.Button;
import android.widget.ImageView;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;


public class ViewerActivity extends ActionBarActivity {

    private SharedPreferences prefs;
    private ImageView imageView;
    public static Button likeButton;
    public static Button dislikeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);
        imageView = (ImageView) findViewById(R.id.imageView);
        likeButton = (Button) findViewById(R.id.like_image);
        dislikeButton = (Button) findViewById(R.id.dislike_image);
    }

    @Override
    protected void onResume() {
        super.onResume();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String expiryTimeString = prefs.getString("s3_expiration", null);
        if (Long.valueOf(expiryTimeString) <= System.currentTimeMillis()){
            new MainMenuActivity.S3CredentialsRequestTask().execute();
        }
        new GetAndShowNextImage().execute();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart(){
        super.onRestart();
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
        likeButton.setEnabled(false);
        dislikeButton.setEnabled(false);
        String currentImageString = MainMenuActivity.queue.get(0);
        MainMenuActivity.queue.remove(currentImageString);
        MainMenuActivity.memoryCache.remove(currentImageString);
        if (currentImageString.startsWith("item/")) {
            new PushItemRequestTask().execute(currentImageString, "0");
        } else if (currentImageString.startsWith("matched/")){
            new PushMatchedRequestTask().execute(currentImageString, "0");
        }
        new GetAndShowNextImage().execute();
        checkAndUpdateQueueAndCache();
    }
    public void likeImage(View view){
        likeButton.setEnabled(false);
        dislikeButton.setEnabled(false);
        String currentImageString = MainMenuActivity.queue.get(0);
        MainMenuActivity.queue.remove(currentImageString);
        MainMenuActivity.memoryCache.remove(currentImageString);
        if (currentImageString.startsWith("item/")) {
            new PushItemRequestTask().execute(currentImageString, "1");
        } else if (currentImageString.startsWith("matched/")){
            new PushMatchedRequestTask().execute(currentImageString, "1");
        }
        new GetAndShowNextImage().execute();
        checkAndUpdateQueueAndCache();
    }

    public void checkAndUpdateQueueAndCache() {
        MainMenuActivity.AsyncTaskQueue.QueueTask();
    }

    class GetAndShowNextImage extends AsyncTask<Void, Void, String>{
        @Override
        protected String doInBackground(Void... params) {
            try {

                if (imageView == null) {
                    imageView = (ImageView) findViewById(R.id.imageView);
                }

                    String nextItem = MainMenuActivity.queue.get(0);

                        Bitmap tryGetBitmap = MainMenuActivity.memoryCache.get(nextItem);

                        if (tryGetBitmap == null) {
                            try {
                                InputStream inputStream = null;

                                if (nextItem.startsWith("item/")) {
                                    nextItem = nextItem.substring(nextItem.indexOf("/") + 1);
                                    if (nextItem.contains("/")) {
                                        inputStream = MainMenuActivity.s3Client.getAmazonS3Client().getObject(MainMenuActivity.resources.getString(R.string.s3_upload_bucket_name), nextItem).getObjectContent();
                                    } else {
                                        inputStream = MainMenuActivity.s3Client.getAmazonS3Client().getObject(MainMenuActivity.resources.getString(R.string.s3_bucket_name), nextItem).getObjectContent();
                                    }
                                } else if (nextItem.startsWith("matched/")){
                                    nextItem = nextItem.substring(nextItem.indexOf("/") + 1);
                                    inputStream = MainMenuActivity.s3Client.getAmazonS3Client().getObject(MainMenuActivity.resources.getString(R.string.s3_profile_bucket_name), nextItem + "/profilepic.jpg").getObjectContent();
                                }

                                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);//                        bitmap = BitmapFactory.decodeFile(imageFile.getPath());
                                setImageView(bitmap);
                            } catch (Exception e) {
                                Log.e("ViewerActivity", e.getMessage(), e);
                                return "failure";
                            }
                        } else {
                            setImageView(tryGetBitmap);
                        }




            } catch (Exception e){
                Log.e("ViewerActivity", e.getMessage(), e);
            }
            return "success";
        }
    }

    public void setImageView(final Bitmap bitmap){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                imageView.setImageBitmap(bitmap);
                likeButton.setEnabled(true);
                dislikeButton.setEnabled(true);
            }
        });
    }

    class PushItemRequestTask extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... params) {
            try {
                final String pushItemUrl = getResources().getString(R.string.base_api_url) + "/app/pushitem";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
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

    class PushMatchedRequestTask extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... params) {
            try {
                final String pushItemUrl = getResources().getString(R.string.base_api_url) + "/app/pushmatched";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                String accessToken = prefs.getString("access_token", null);
                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", "Bearer " + accessToken);
                headers.add("Matched", params[0]);
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

}
