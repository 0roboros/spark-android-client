package com.sparklounge.client.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Chuang on 8/1/2015.
 */
public class ChatLogger {

    final long CHAT_START_TIME = System.currentTimeMillis();

    String [] cols;
    String friendName, where, sortOrder;
    int take = 10;
    int skip = 0;

    SparkDbHelper dbHelper;
    SQLiteDatabase db;

    public ChatLogger(Context context, String friendName) {
        this.dbHelper = SparkDbHelper.getInstance(context);
        initialize(friendName);
    }

    public void initialize(String friendName) {
        this.friendName = friendName;
        cols = new String[] {
                SparkContract.Conversation.KEY_SENDER,
                SparkContract.Conversation.KEY_FRIEND,
                SparkContract.Conversation.KEY_MSG,
                SparkContract.Conversation.KEY_TIME
        };
        where = SparkContract.Conversation.KEY_SENDER + "='" + friendName + "'"
                + " OR " + SparkContract.Conversation.KEY_FRIEND + "='" + friendName + "'"
                + " AND " + SparkContract.Conversation.KEY_TIME + ">" + CHAT_START_TIME;
        sortOrder = SparkContract.Conversation.KEY_TIME + " DESC";
    }

    public Cursor retreiveChatHistory() {
        db = dbHelper.getReadableDatabase();
        String limit = skip + ", " + take;
        Cursor history = db.query(SparkContract.Conversation.TABLE_NAME, cols, where, null, null, null, sortOrder, limit);
        skip += take;
        return history;
    }

    public void logChatHistory(String sender, String receiver, String msg) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SparkContract.Conversation.KEY_SENDER, sender);
        values.put(SparkContract.Conversation.KEY_FRIEND, receiver);
        values.put(SparkContract.Conversation.KEY_MSG, msg);
        db.insert(SparkContract.Conversation.TABLE_NAME, null, values);
    }
}
