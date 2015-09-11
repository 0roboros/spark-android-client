package com.sparklounge.client.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.sparklounge.client.models.UserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chuang on 9/8/2015.
 */
public class FriendsDb {

    private SparkDbHelper mDbHelper;
    private static SQLiteDatabase db;

    private static String[] allCols = new String[] {
        SparkContract.Friends.KEY_NAME,
        SparkContract.Friends.KEY_CAPTION,
        SparkContract.Friends.KEY_PROFILE_PIC,
        SparkContract.Friends.KEY_GCM_REG_ID,
    };

    public FriendsDb(Context context) {
        mDbHelper = SparkDbHelper.getInstance(context);
    }

    public UserInfo getFriendInfo(String name) {
        db = mDbHelper.getReadableDatabase();
        String where = String.format(SparkContract.Friends.KEY_NAME + "='%s'", name);
        Cursor cursor = db.query(SparkContract.Friends.TABLE_NAME, allCols, where, new String[]{}, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            return curosrToUserInfo(cursor);
        }
        return null;
    }

    public List<UserInfo> getAllFriends() {
        db = mDbHelper.getReadableDatabase();
        List<UserInfo> userInfos = new ArrayList<>();
        String orderby = SparkContract.Friends.KEY_NAME;
        Cursor cursor = db.query(SparkContract.Friends.TABLE_NAME, allCols, null, null, null, null, orderby);
        while(cursor.moveToNext()) {
            userInfos.add(curosrToUserInfo(cursor));
        }
        return userInfos;
    }

    public boolean saveFriendInfo(UserInfo userInfo) {
        try {
            db = mDbHelper.getWritableDatabase();
            ContentValues content = new ContentValues();
            content.put(SparkContract.Friends.KEY_NAME, userInfo.getUserId());
            content.put(SparkContract.Friends.KEY_CAPTION, userInfo.getCaption());
            content.put(SparkContract.Friends.KEY_PROFILE_PIC, userInfo.getProfilePic());
            content.put(SparkContract.Friends.KEY_GCM_REG_ID, userInfo.getGcmRegId());
            db.insert(SparkContract.Friends.TABLE_NAME, null, content);
            return true;
        } catch (SQLiteException ex) {
            Log.e("", "Failed to save user info");
            return false;
        }
    }

    private UserInfo curosrToUserInfo(Cursor c) {
        UserInfo userInfo = new UserInfo(
                c.getString(c.getColumnIndex(SparkContract.Friends.KEY_NAME)),
                c.getString(c.getColumnIndex(SparkContract.Friends.KEY_CAPTION)),
                c.getString(c.getColumnIndex(SparkContract.Friends.KEY_PROFILE_PIC)),
                c.getString(c.getColumnIndex(SparkContract.Friends.KEY_GCM_REG_ID)));
        return userInfo;
    }

    public void insertTestData() {
        UserInfo f1 = new UserInfo("Kayne West", "Life is a joke", "testProfilePic", "");
        UserInfo f2 = new UserInfo("Kim Kard", "I'm sooo sexxy", "testProfilePic", "");
        UserInfo f3 = new UserInfo("Ridiculous Joe", "You are ridiculous mate", "testProfilePic", "");
        UserInfo f4 = new UserInfo("Hilarious Darren", "Your face is a joke", "testProfilePic", "");
        UserInfo f5 = new UserInfo("Chubby Paul", "I like them Big Macs", "testProfilePic", "");
        saveFriendInfo(f1);
        saveFriendInfo(f2);
        saveFriendInfo(f3);
        saveFriendInfo(f4);
        saveFriendInfo(f5);
    }
}
