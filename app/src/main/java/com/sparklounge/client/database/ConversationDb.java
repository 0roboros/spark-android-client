package com.sparklounge.client.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sparklounge.client.models.Conversation;
import com.sparklounge.client.models.UserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chuang on 7/30/2015.
 */
public class ConversationDb {

    private SparkDbHelper mDbHelper;
    private static SQLiteDatabase db;

    public ConversationDb(Context context) {
        mDbHelper = SparkDbHelper.getInstance(context);
    }

    public List<Conversation> getAllConversations() {
        db = mDbHelper.getReadableDatabase();

        String query = String.format("SELECT c.%s, c.%s, a.%s, a.%s " +
                "FROM %s a " +
                "INNER JOIN (" +
                        "SELECT %s, %s, max(%s) " +
                        "FROM %s GROUP BY %s " +
                ") b " +
                "ON a.%s = b.%s " +
                "LEFT JOIN %s c " +
                "ON a.%s = c.%s " +
                "GROUP BY a.%s " +
                "ORDER BY a.%s DESC;",
                SparkContract.Friends.KEY_NAME, SparkContract.Friends.KEY_PROFILE_PIC, SparkContract.Conversation.KEY_MSG, SparkContract.Conversation.KEY_TIME,
                SparkContract.Conversation.TABLE_NAME,
                SparkContract.Conversation.KEY_FRIEND, SparkContract.Conversation.KEY_MSG, SparkContract.Conversation.KEY_TIME,
                SparkContract.Conversation.TABLE_NAME, SparkContract.Conversation.KEY_FRIEND,
                SparkContract.Conversation.KEY_FRIEND, SparkContract.Conversation.KEY_FRIEND,
                SparkContract.Friends.TABLE_NAME, SparkContract.Conversation.KEY_FRIEND, SparkContract.Friends.KEY_NAME,
                SparkContract.Conversation.KEY_FRIEND,
                SparkContract.Conversation.KEY_TIME);                ;


        List<Conversation> conversations = new ArrayList<>();

        Cursor cursor = db.rawQuery(query, new String[]{});
        while(cursor.moveToNext()){
            conversations.add(cursorToConversation(cursor));
        }
        return conversations;
    }

    private Conversation cursorToConversation(Cursor c) {
        return new Conversation(
                new UserInfo(
                        c.getString(c.getColumnIndex(SparkContract.Friends.KEY_NAME)), "",
                        c.getString(c.getColumnIndex(SparkContract.Friends.KEY_PROFILE_PIC)), ""),
                c.getString(c.getColumnIndex(SparkContract.Conversation.KEY_MSG)),
                c.getString(c.getColumnIndex(SparkContract.Conversation.KEY_TIME))
        );
    }

    private void insertTestMsg(String friend, String sender, String msg) {
        db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SparkContract.Conversation.KEY_FRIEND, friend);
        values.put(SparkContract.Conversation.KEY_SENDER, sender);
        values.put(SparkContract.Conversation.KEY_MSG, msg);

        db.insert(SparkContract.Conversation.TABLE_NAME, null, values);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public  void insertTestData() {
        insertTestMsg("Ridiculous Joe", "Ridiculous Joe", "akljsdflaskf");
        insertTestMsg("Chubby Paul", "me", "gagadfasdf");
        insertTestMsg("Kim Kard", "me", "asdfasdfasdf");
        insertTestMsg("Chubby Paul", "me", "should be last");
        insertTestMsg("Ridiculous Joe", "Ridiculous Joe", "akljsdflaskf");
        insertTestMsg("Ridiculous Joe", "me", "rqwerwergfn");
        insertTestMsg("Ridiculous Joe", "Ridiculous Joe", "akljsdflaskf");
        insertTestMsg("Chubby Paul", "Chubby Paul", "gagadfasdf");
        insertTestMsg("Ridiculous Joe", "me", "bfrjyjhfdh");
        insertTestMsg("Kim Kard", "Kim Kard", "akljsdflaskf");
        insertTestMsg("Kim Kard", "Kim Kard", "asdfhtrjasdf");
        insertTestMsg("Ridiculous Joe", "me", "asdfababgg");
        insertTestMsg("Chubby Paul", "Chubby Paul", "gagadfasdf");
        insertTestMsg("Kayne West", "Kayne West", "akljsdflaskf");
        insertTestMsg("Kayne West", "Kayne West", "akljsdflaskf");
        insertTestMsg("Kayne West", "me", "shoudl be third");
        insertTestMsg("Kim Kard", "me", "asdfafsdaf");
        insertTestMsg("Kayne West", "Kayne West", "akljsdflaskf");
        insertTestMsg("Kayne West", "Kayne West", "akljsdflaskf");
        insertTestMsg("Kim Kard", "me", "should be second");
        insertTestMsg("Ridiculous Joe", "me", "should be first");
        insertTestMsg("Kim Kard", "Kim Kard", "akljsdflaskf");
        insertTestMsg("Ridiculous Joe", "Ridiculous Joe", "akljsdflaskf");
        insertTestMsg("Kayne West", "Kayne West", "akljsdflaskf");
        insertTestMsg("Chubby Paul", "Chubby Paul", "gagadfasdf");
        insertTestMsg("Ridiculous Joe", "Ridiculous Joe", "akljsdflaskf");
        insertTestMsg("Chubby Paul", "me", "gagadfasdf");
        insertTestMsg("Kim Kard", "me", "asdfasdfasdf");
        insertTestMsg("Chubby Paul", "me", "should be last");
        insertTestMsg("Ridiculous Joe", "Ridiculous Joe", "akljsdflaskf");
        insertTestMsg("Ridiculous Joe", "me", "rqwerwergfn");
        insertTestMsg("Ridiculous Joe", "Ridiculous Joe", "akljsdflaskf");
        insertTestMsg("Chubby Paul", "Chubby Paul", "gagadfasdf");
        insertTestMsg("Ridiculous Joe", "me", "bfrjyjhfdh");
        insertTestMsg("Kim Kard", "Kim Kard", "akljsdflaskf");
        insertTestMsg("Kim Kard", "Kim Kard", "asdfhtrjasdf");
        insertTestMsg("Ridiculous Joe", "me", "asdfababgg");
        insertTestMsg("Chubby Paul", "Chubby Paul", "gagadfasdf");
        insertTestMsg("Kayne West", "Kayne West", "akljsdflaskf");
        insertTestMsg("Kayne West", "Kayne West", "akljsdflaskf");
        insertTestMsg("Kayne West", "me", "shoudl be third");
        insertTestMsg("Kim Kard", "me", "asdfafsdaf");
        insertTestMsg("Kayne West", "Kayne West", "akljsdflaskf");
        insertTestMsg("Kayne West", "Kayne West", "akljsdflaskf");
        insertTestMsg("Kim Kard", "me", "should be second");
        insertTestMsg("Ridiculous Joe", "me", "should be first");
        insertTestMsg("Kim Kard", "Kim Kard", "akljsdflaskf");
        insertTestMsg("Ridiculous Joe", "Ridiculous Joe", "akljsdflaskf");
        insertTestMsg("Kayne West", "Kayne West", "akljsdflaskf");
        insertTestMsg("Chubby Paul", "Chubby Paul", "gagadfasdf");
    }
}
