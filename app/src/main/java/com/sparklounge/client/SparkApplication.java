package com.sparklounge.client;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.multidex.MultiDex;
import android.util.LruCache;

import com.sparklounge.client.models.Image;

/**
 * Created by Chuang on 8/26/2015.
 */
public class SparkApplication extends Application {

    public static final int CACHE_SIZE = 2; // half if application memory
    public static LruCache<String, Bitmap> mCache;

    @Override
    public void onCreate() {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / CACHE_SIZE;

        mCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    //TODO: make a new class to handle cache logic
    public static LruCache<String, Bitmap> getCache() {
        return mCache;
    }
}
