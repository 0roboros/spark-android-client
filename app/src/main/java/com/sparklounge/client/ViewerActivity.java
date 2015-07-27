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

import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
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
        String currentItem = MainMenuActivity.queue.get(0);
        MainMenuActivity.queue.remove(currentItem);
        MainMenuActivity.memoryCache.remove(currentItem);
        new PushItemRequestTask().execute(currentItem, "0");
        new GetAndShowNextImage().execute();
        checkAndUpdateQueueAndCache();
    }
    public void likeImage(View view){
        likeButton.setEnabled(false);
        dislikeButton.setEnabled(false);
        String currentItem = MainMenuActivity.queue.get(0);
        MainMenuActivity.queue.remove(currentItem);
        MainMenuActivity.memoryCache.remove(currentItem);
        new PushItemRequestTask().execute(currentItem, "1");
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
                            if (nextItem.contains("/")) {
                                inputStream = MainMenuActivity.s3Client.getAmazonS3Client().getObject(MainMenuActivity.resources.getString(R.string.s3_upload_bucket_name), nextItem).getObjectContent();
                            } else {
                                inputStream = MainMenuActivity.s3Client.getAmazonS3Client().getObject(MainMenuActivity.resources.getString(R.string.s3_bucket_name), nextItem).getObjectContent();
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

}
