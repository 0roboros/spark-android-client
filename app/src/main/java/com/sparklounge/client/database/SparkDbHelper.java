package com.sparklounge.client.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Chuang on 9/9/2015.
 */
public class SparkDbHelper extends SQLiteOpenHelper {

    private static SparkDbHelper instance;

    public static synchronized SparkDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SparkDbHelper(context);
        }
        return instance;
    }

    private SparkDbHelper(Context context) {
        super(context, SparkContract.DATABASE_NAME, null, SparkContract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        for(String CREATE_TABLE_QUERY : SparkContract.SQL_CREATE_TABLE_ARRAY) {
            sqLiteDatabase.execSQL(CREATE_TABLE_QUERY);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        for(String DELETE_TABLE_QUERY : SparkContract.SQL_DELETE_TABLE_ARRAY) {
            sqLiteDatabase.execSQL(DELETE_TABLE_QUERY);
        }
        onCreate(sqLiteDatabase);
    }
}
