package com.sparklounge.client;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.facebook.login.LoginManager;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.sparklounge.client.activities.ImagesActivity;
import com.sparklounge.client.activities.UploadActivity;
import com.sparklounge.client.models.AwsCredentials;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private static int RESULT_LOAD_IMG = 1;
    private static int RESULT_CROP_IMG = 2;
    private File croppedFile;


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
        //if ((s3AccessKeyId == null) || (s3SecretAccessKey == null) || (s3SessionToken == null)
          //      || (expiryTimeString == null) || (Long.valueOf(expiryTimeString) <= System.currentTimeMillis())) {
           // new S3CredentialsRequestTask().execute();
        //}

        boolean hasProfilePicture = prefs.getBoolean("has_profile_picture", false);
        if (hasProfilePicture == false) {
            new checkProfilePicture().execute();
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

    public void startNewView(View view) {
        Intent intent = new Intent(MainMenuActivity.this, ImagesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void uploadFile(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RESULT_LOAD_IMG);
        prefs.edit().putBoolean("has_profile_picture", true).apply();
    }

    public static class S3CredentialsRequestTask extends AsyncTask<Void, Void, AwsCredentials> {

        @Override
        protected AwsCredentials doInBackground(Void... params){
            try {
                final String getS3CredentialsUrl = resources.getString(R.string.base_api_url) + "/app/gets3credentials";
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

    public static class AsyncTaskQueue{

        public static int tasks = 0;
        public static void QueueTask(){
            tasks++;
            if (tasks == 1){
                if (MainMenuActivity.queue == null || MainMenuActivity.queue.size() < 5) {
                    new MainMenuActivity.QueueRequestTask().execute();
                } else {
                    //new MainMenuActivity.UpdateCache().execute();
                }
            }
        }

        public static void nextTask(){
            tasks--;
            if (tasks > 0){
                if (MainMenuActivity.queue.size() < 5) {
                    new MainMenuActivity.QueueRequestTask().execute();
                } else {
                    //new MainMenuActivity.UpdateCache().execute();
                }
            }

        }
    }

    public static class UpdateCache extends AsyncTask<Void, Void, String>{
        @Override
        protected String doInBackground(Void... params) {
            try {
                if (s3Client == null) {
                    s3Client = new TransferManager(new BasicSessionCredentials(s3AccessKeyId, s3SecretAccessKey, s3SessionToken));
                }

                int numQueue = queue.size();
                for (int cacheItr = 0; cacheItr < Math.min(numQueue, 5); cacheItr++) {
                    String nextItem = queue.get(cacheItr);
                    if (memoryCache.get(nextItem) != null) continue;

                    InputStream inputStream = null;
                    if (nextItem.startsWith("item/")) {
                        String nextItemNoPrefix = nextItem.substring(nextItem.indexOf("/") + 1);
                        if (nextItemNoPrefix.contains("/")) {
                            inputStream = s3Client.getAmazonS3Client().getObject(resources.getString(R.string.s3_upload_bucket_name), nextItemNoPrefix).getObjectContent();
                        } else {
                            inputStream = s3Client.getAmazonS3Client().getObject(resources.getString(R.string.s3_bucket_name), nextItemNoPrefix).getObjectContent();
                        }
                    } else if (nextItem.startsWith("matched/")){
                        String nextItemNoPrefix = nextItem.substring(nextItem.indexOf("/") + 1);
                        inputStream = MainMenuActivity.s3Client.getAmazonS3Client().getObject(MainMenuActivity.resources.getString(R.string.s3_profile_bucket_name), nextItemNoPrefix + "/profilepic.jpg").getObjectContent();
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
                final String getQueueUrl = resources.getString(R.string.base_api_url) + "/app/getitems";
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && data != null){
                Uri selectedImage = data.getData();


                try {

                    Intent cropIntent = new Intent("com.android.camera.action.CROP");
                    // indicate image type and Uri
                    cropIntent.setDataAndType(selectedImage, "image/*");
                    // set crop properties
                    cropIntent.putExtra("scaleType", "centerCrop");
                    cropIntent.putExtra("crop", "true");
                    // indicate aspect of desired crop
                    cropIntent.putExtra("aspectX", 1);
                    cropIntent.putExtra("aspectY", 1);
                    // indicate output X and Y
//                cropIntent.putExtra("outputX", 960);
//                cropIntent.putExtra("outputY", 1200);
                    // retrieve data on return
//                cropIntent.putExtra("return-data", true);
//                cropIntent.putExtra("outputFormat",Bitmap.CompressFormat.JPEG.toString());
                    if (croppedFile != null){
                        croppedFile.delete();
                    }
                    croppedFile = new File(Environment.getExternalStorageDirectory(),
                            "/Spark" + System.currentTimeMillis() + ".jpg");
                    try {
                        croppedFile.createNewFile();
                    } catch (IOException ex) {
                        Log.e("io", ex.getMessage());
                    }

                    Uri croppedFileUri = Uri.fromFile(croppedFile);

                    cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, croppedFileUri);
                    // start the activity - we handle returning in onActivityResult
                    startActivityForResult(cropIntent, RESULT_CROP_IMG);
                } catch (ActivityNotFoundException anfe) {
                    // respond to users whose devices do not support the crop action
                    // display an error message
                    String errorMessage = "Whoops - your device doesn't support the crop action!";
                    Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
                    toast.show();

                    String [] filePathColumn = { MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);

                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String imagePath = cursor.getString(columnIndex);

                    new uploadProfilePicture(imagePath, false).execute();
                }
            }
            else if (requestCode == RESULT_CROP_IMG && resultCode == RESULT_OK && data != null){

                if (croppedFile != null){
                    FileOutputStream fos = null;
                    try {
                        File compressedFile = new File(Environment.getExternalStorageDirectory(),
                                "/SparkCompressed" + System.currentTimeMillis() + ".jpg");
    //                        try {
    //                            compressedFile.createNewFile();
    //                        } catch (IOException ex) {
    //                            Log.e("io", ex.getMessage());
    //                        }
                        fos = new FileOutputStream(compressedFile);
                        Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(croppedFile.getPath()), 960, 960);
                        //Bitmap bitmap = BitmapFactory.decodeFile(croppedFile.getPath());
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                        fos.flush();
                        fos.close();
                        Log.e("UploadActivity", "Original Size: " + String.valueOf(croppedFile.length()));
                        Log.e("UploadActivity", "Compressed Size: " + String.valueOf(compressedFile.length()));
                        new uploadProfilePicture(compressedFile.getPath(), true).execute();
    //                        // get the cropped bitmap
    //                        Bitmap selectedBitmap = extras.getParcelable("data");
    //
    //                        File file = null;
    //                        FileOutputStream fOut = null;
    //                        try {
    //                            String path = Environment.getExternalStorageDirectory().toString();
    //
    //                            file = new File();
    //                            fOut = new FileOutputStream(file);
    //                            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
    //                            fOut.flush();
    //                            fOut.close();
    //                            String imagePath = file.getPath();
    //                            new uploadProfilePicture(imagePath, true).execute();
    //                        } catch (Exception e){
    //                            Log.e("UploadActivity", e.getMessage(), e);
    //                        } finally {
    //                            if (fOut != null){
    //                                fOut.close();
    //                            }
    //                        }
                    } finally {
                        if (croppedFile != null){
                            croppedFile.delete();
                        } if (fos != null) {
                            fos.close();
                        }
                    }

                }
            } else {
                prefs.edit().putBoolean("has_profile_picture", false).apply();
            }
        } catch (Exception e){
            Log.e("MainMenuActivity", e.getMessage(), e);
        }
    }

    private class uploadProfilePicture extends AsyncTask<String, String, String>{
        private File file;
        private boolean tempFile;

        public uploadProfilePicture(String path, boolean tempFile){
            this.file = new File(path);
            this.tempFile = tempFile;
        }

        @Override
        protected String doInBackground(String... args) {
            if (file != null) {
                String accessToken = prefs.getString("access_token", null);
                try {
                    final String uploadProfilePictureUrl = getResources().getString(R.string.base_api_url) + "/app/uploadprofilepic";
                    RestTemplate restTemplate = new RestTemplate();
                    restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                    restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
                    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
                    FileSystemResource fsr = new FileSystemResource(this.file);
//                    Bitmap uploadBitmap = BitmapFactory.decodeFile(path);
//                    ByteArrayOutputStream byteArrOutput = new ByteArrayOutputStream();
//                    uploadBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrOutput);
//                    byte[] byteArr = byteArrOutput.toByteArray();
                    map.add("file", fsr);
//                    map.add("Content-Type", path.substring(path.lastIndexOf(".")));
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Authorization", "Bearer " + accessToken);
                    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

                    HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = new HttpEntity<>(
                            map, headers);
                    ResponseEntity<String> responseEntity = restTemplate.exchange(
                            uploadProfilePictureUrl, HttpMethod.POST, httpEntity,
                            String.class);

                    return responseEntity.getBody();
                } catch (Exception e) {
                    Log.e("MainMenuActivity", e.getMessage(), e);
                    return null;
                } finally {
                    if (tempFile == true){
                        if (file != null) {
                            this.file.delete();
                        }
                    }
                }
//
//                if (uploadFolder == null) {
//                    return null;
//                }
//
//                try {
//                    final String getUploadCredentialsUrl = getResources().getString(R.string.base_api_url) + "/gets3uploadcredentials";
//                    RestTemplate restTemplate = new RestTemplate();
//                    MappingJackson2HttpMessageConverter jsonToPojo = new MappingJackson2HttpMessageConverter();
////                    jsonToPojo.getObjectMapper().setPropertyNamingStrategy(
////                            PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
//                    restTemplate.getMessageConverters().add(jsonToPojo);
//                    HttpHeaders headers = new HttpHeaders();
//                    headers.add("Authorization", "Bearer " + accessToken);
//                    headers.add("Filename", path.substring(path.lastIndexOf("/")));
//                    HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
//                    ResponseEntity<AwsCredentials> responseEntity;
//                    responseEntity = restTemplate.exchange(getUploadCredentialsUrl, HttpMethod.GET, httpEntity, AwsCredentials.class);
//                    AwsCredentials uploadCredentials = responseEntity.getBody();
//
//                    TransferManager s3Client = new TransferManager(new BasicSessionCredentials(
//                            uploadCredentials.getAccessKeyId(), uploadCredentials.getSecretAccessKey(), uploadCredentials.getSessionToken()));
//
//                    s3Client.upload(new PutObjectRequest(getResources().getString(R.string.base_api_url), uploadFolder, file));
//                    return "success";
//                } catch (Exception e) {
//                    Log.e("UploadActivity", e.getMessage(), e);
//                }
//                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String status){
            if (status == null || !status.equals("success")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        Toast.makeText(getApplicationContext(), "Failed to Upload", Toast.LENGTH_SHORT).show();
                        prefs.edit().putBoolean("has_profile_picture", false).apply();
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        Toast.makeText(getApplicationContext(), "Successfully Uploaded", Toast.LENGTH_SHORT).show();

                    }
                });
            }
            prefs.edit().putBoolean("has_profile_picture", true).apply();
        }
    }

    public class checkProfilePicture extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                final String checkProfilePictureUrl = resources.getString(R.string.base_api_url) + "/app/checkprofilepic";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                String accessToken = prefs.getString("access_token", null);
                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", "Bearer " + accessToken);
                HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
                ResponseEntity<String> responseEntity;
                responseEntity = restTemplate.exchange(checkProfilePictureUrl, HttpMethod.GET, httpEntity, String.class);
                String profilePicture = responseEntity.getBody();
                return profilePicture;
            } catch (Exception e) {
                Log.e("ViewerActivity", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String profilePicture) {
            if (profilePicture == null) {
            }
            else if (!profilePicture.equals("true")){
                uploadFile();
            }
            else {
                prefs.edit().putBoolean("has_profile_picture", true).apply();
            }
        }

    }

}
