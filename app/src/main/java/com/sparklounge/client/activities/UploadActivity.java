package com.sparklounge.client.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.sparklounge.client.R;
import com.sparklounge.client.apis.SparkApi;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;


public class UploadActivity extends ActionBarActivity {
    private static int RESULT_LOAD_IMG = 1;
    private static int RESULT_CROP_IMG = 2;
    private SharedPreferences prefs;
    File croppedFile;
    private SparkApi mSparkApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        croppedFile = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String accessTokenString = extras.getString("access_token");
            mSparkApi = new SparkApi(accessTokenString);
        }
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
        startActivityForResult(intent, RESULT_LOAD_IMG);
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

                    new uploadFile(imagePath, false).execute();
                }
            }
            else if (requestCode == RESULT_CROP_IMG && resultCode == RESULT_OK && data != null){

                if (croppedFile != null){
                    FileOutputStream fos = null;
                    try {
                        File compressedFile = new File(Environment.getExternalStorageDirectory(),
                                "/SparkCompressed" + System.currentTimeMillis() + ".jpg");
                        fos = new FileOutputStream(compressedFile);
                        Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(croppedFile.getPath()), 960, 960);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                        fos.flush();
                        fos.close();
                        Log.e("UploadActivity", "Original Size: " + String.valueOf(croppedFile.length()));
                        Log.e("UploadActivity", "Compressed Size: " + String.valueOf(compressedFile.length()));


                        //new uploadFile(compressedFile.getPath(), true).execute();

                        Map<String, TypedFile> files = new HashMap<>();
                        files.put("file", new TypedFile("image/jpg", compressedFile));
                        uploadFile(files);

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
            }
        } catch (Exception e){
            Log.e("UploadActivity", e.getMessage(), e);
        }
    }

    private class uploadFile extends AsyncTask<String, String, String>{
        private File file;
        private boolean tempFile;

        public uploadFile(String path, boolean tempFile){
            this.file = new File(path);
            this.tempFile = tempFile;
        }

        @Override
        protected String doInBackground(String... args) {
            if (file != null) {
                String accessToken = prefs.getString("access_token", null);
                try {
                    final String getUploadFolderUrl = getResources().getString(R.string.base_api_url) + "/app/uploaditem";
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
                            getUploadFolderUrl, HttpMethod.POST, httpEntity,
                            String.class);

                    return responseEntity.getBody();
                } catch (Exception e) {
                    Log.e("UploadActivity", e.getMessage(), e);
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


    private void uploadFile(Map<String, TypedFile> files) {
        mSparkApi.uploadItem(files, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                if(s.equals("success")) {
                    Toast.makeText(getApplicationContext(), "Successfully Uploaded", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to Upload", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getApplicationContext(), "Failed to Upload", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        });
    }


}
