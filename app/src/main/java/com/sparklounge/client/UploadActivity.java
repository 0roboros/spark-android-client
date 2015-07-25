package com.sparklounge.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;


public class UploadActivity extends ActionBarActivity {
    private static int RESULT_LOAD_IMG = 1;
    private SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
    }


    @Override
    public void onStart() {
        super.onStart();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    @Override
    public void onRestart() {
        super.onRestart();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_upload, menu);
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


    public void uploadFile(View view){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && data != null){
                Uri selectedImage = data.getData();
                String [] filePathColumn = { MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);

                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imagePath = cursor.getString(columnIndex);
                new uploadFile(imagePath).execute();
            }
        } catch (Exception e){
            Log.e("UploadActivity", e.getMessage(), e);
        }
    }

    private class uploadFile extends AsyncTask<String, String, String>{
        private String path;

        public uploadFile(String path){
            this.path = path;
        }

        @Override
        protected String doInBackground(String... args) {
            if (path != null && !path.isEmpty()) {
                String accessToken = prefs.getString("access_token", null);
                try {
                    final String getUploadFolderUrl = getResources().getString(R.string.base_api_url) + "/uploaditem";
                    RestTemplate restTemplate = new RestTemplate();
                    restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                    restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
                    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
                    map.add("file", new FileSystemResource(new File(path)));
//                    map.add("Content-Type", path.substring(path.lastIndexOf(".")));
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Authorization", "Bearer " + accessToken);
                    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

                    HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = new HttpEntity<>(
                            map, headers);
                    ResponseEntity<String> responseEntity = restTemplate.exchange(
                            getUploadFolderUrl, HttpMethod.POST, httpEntity,
                            String.class);

                    return responseEntity.getBody();
                } catch (Exception e) {
                    Log.e("UploadActivity", e.getMessage(), e);
                    return null;
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
        }
    }


}
