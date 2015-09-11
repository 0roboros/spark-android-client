package com.sparklounge.client.apis;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.sparklounge.client.models.AwsCredentials;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by Chuang on 9/7/2015.
 */
public class AmazonClient {

    private final String S3_UPLOAD_BUCKET_NAME = "com.spark.userupload";
    private final String S3_BUCKET_NAME = "com.spark";
    private final String S3_PROFILE_BUCKET_NAME = "com.spark.userprofile";

    private AmazonS3Client s3Client;

    public AmazonClient(AwsCredentials awsCredentials) {
        this.s3Client = new AmazonS3Client(new BasicSessionCredentials(
                awsCredentials.getAccessKeyId(),
                awsCredentials.getSecretAccessKey(),
                awsCredentials.getSessionToken()
        ));
    }

    public Bitmap getBitmap (String imageLink, final int SIZE) {
        InputStream inputStream = null;
        if (imageLink.startsWith("item/")) {
            String nextItemNoPrefix = imageLink.substring(imageLink.indexOf("/") + 1);
            if (nextItemNoPrefix.contains("/")) {
                inputStream = s3Client.getObject(S3_UPLOAD_BUCKET_NAME, nextItemNoPrefix).getObjectContent();
            } else {
                inputStream = s3Client.getObject(S3_BUCKET_NAME, nextItemNoPrefix).getObjectContent();
            }
        } else if (imageLink.startsWith("matched/")){
            String nextItemNoPrefix = imageLink.substring(imageLink.indexOf("/") + 1);
            inputStream = s3Client.getObject(S3_PROFILE_BUCKET_NAME, nextItemNoPrefix + "/profilepic.jpg").getObjectContent();
        } else {
            //inputStream = s3Client.getObject(getResources().getString(R.string.s3_profile_bucket_name), imageLink + "/profilepic.jpg").getObjectContent();
            try {
                inputStream = new URL("http://barkpost-assets.s3.amazonaws.com/wp-content/uploads/2013/11/grumpy-dog-11.jpg").openConnection().getInputStream();
            } catch (Exception e) {}
        }

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE=SIZE;

        // Find the correct scale value. It should be the power of 2.
        int scale = 1;
        while(o.outWidth / scale / 2 >= REQUIRED_SIZE) {
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(inputStream, null, o2);
    }

    public Bitmap getBitmap(String imageLink) {
        InputStream inputStream = null;
        if (imageLink.startsWith("item/")) {
            String nextItemNoPrefix = imageLink.substring(imageLink.indexOf("/") + 1);
            if (nextItemNoPrefix.contains("/")) {
                inputStream = s3Client.getObject(S3_UPLOAD_BUCKET_NAME, nextItemNoPrefix).getObjectContent();
            } else {
                inputStream = s3Client.getObject(S3_BUCKET_NAME, nextItemNoPrefix).getObjectContent();
            }
        } else if (imageLink.startsWith("matched/")){
            String nextItemNoPrefix = imageLink.substring(imageLink.indexOf("/") + 1);
            inputStream = s3Client.getObject(S3_PROFILE_BUCKET_NAME, nextItemNoPrefix + "/profilepic.jpg").getObjectContent();
        } else {
            //inputStream = s3Client.getObject(getResources().getString(R.string.s3_profile_bucket_name), imageLink + "/profilepic.jpg").getObjectContent();
            try {
                inputStream = new URL("http://barkpost-assets.s3.amazonaws.com/wp-content/uploads/2013/11/grumpy-dog-11.jpg").openConnection().getInputStream();
            } catch (Exception e) {}
        }

        return BitmapFactory.decodeStream(inputStream);
    }
}
