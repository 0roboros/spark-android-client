package com.sparklounge.client.database;

import android.provider.BaseColumns;

/**
 * Created by Chuang on 7/27/2015.
 */
public final class SparkContract {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "spark.db";
    public static final String AUTHORITY = "com.spark";
    public static final String SCHEME = "content://";
    public static final String SLASH = "/";

    public static final String[] SQL_CREATE_TABLE_ARRAY = {
            Friends.CREATE_TABLE,
            Conversation.CREATE_TABLE
            // add more Create_Table statements if needed!
    };

    public static final String[] SQL_DELETE_TABLE_ARRAY = {
            Friends.DELETE_TABLE,
            Conversation.DELETE_TABLE
    };

    /* don't allow this class to be instantiated */
    private SparkContract() {}

    public static final class Friends implements BaseColumns {

        private Friends() {}

        public static final String TABLE_NAME = "Friends";
        public static final String KEY_NAME = "Name";
        public static final String KEY_CAPTION = "Caption";
        public static final String KEY_PROFILE_PIC = "Profile_Pic";
        public static final String KEY_GCM_REG_ID = "GCM_ID";

        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY, "
                + KEY_NAME + " TEXT, "
                + KEY_CAPTION + " TEXT, "
                + KEY_PROFILE_PIC + " TEXT, "
                + KEY_GCM_REG_ID + " TEXT"
                + ");";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static final class Conversation implements BaseColumns {

        private Conversation() {}

        public static final String TABLE_NAME = "Conversation";
        public static final String KEY_SENDER = "Sender";
        public static final String KEY_FRIEND = "Friend";
        public static final String KEY_MSG = "Message";
        public static final String KEY_TIME = "TimeStamp";

        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY, "
                + KEY_SENDER + " TEXT NOT NULL, "
                + KEY_FRIEND + " TEXT NOT NULL, "
                + KEY_MSG + " TEXT NOT NULL, "
                + KEY_TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ");";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    }
}

