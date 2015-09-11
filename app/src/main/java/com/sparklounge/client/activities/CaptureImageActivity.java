package com.sparklounge.client.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.amazonaws.services.s3.internal.Mimetypes;
import com.sparklounge.client.R;
import com.sparklounge.client.apis.SparkApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

public class CaptureImageActivity extends ActionBarActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int RESULT_CROP_IMG = 2;
    File croppedFile;
    private String mCurrentPhotoPath;
    private SparkApi mSparkApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_image);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String accessTokenString = extras.getString("access_token");
            mSparkApi = new SparkApi(accessTokenString);
        }

        captureImage();
    }

    private void captureImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("CaptureImageActivity", ex.getMessage());
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            //Bitmap imageBitmap = (Bitmap) extras.get("data");
            //mImageView.setImageBitmap(imageBitmap);
            Uri selectedImage = Uri.fromFile(new File(mCurrentPhotoPath));

            try {

                Intent cropIntent = new Intent("com.android.camera.action.CROP");
                // indicate image type and Uri
                cropIntent.setDataAndType(selectedImage, "image/*");
                // set crop properties
                cropIntent.putExtra("scaleType", "centerCrop");
                cropIntent.putExtra("crop", "true");
                cropIntent.putExtra("aspectX", 1);
                cropIntent.putExtra("aspectY", 1);
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

                TypedFile typedFile = new TypedFile("image/jpg", new File(imagePath));
                Map<String, TypedFile> files = new HashMap<String, TypedFile>();
                files.put("files", typedFile);
                uploadFile(files);
            }
        }  else if (requestCode == RESULT_CROP_IMG && resultCode == RESULT_OK && data != null){
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

                    Map<String, TypedFile> files = new HashMap<>();
                    files.put("file", new TypedFile("image/jpg", compressedFile));

                    uploadFile(files);
                    if (croppedFile != null){
                        croppedFile.delete();
                    } if (fos != null) {
                        fos.close();
                    }
                } catch (Exception e) {
                    Log.e("CaptureActivity", "Faild to crop and upload");
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_capture_image, menu);
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

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStorageDirectory();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
