package com.sparklounge.client.msg;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.sparklounge.client.activities.ImagesActivity;

/**
 * Created by Chuang on 7/28/2015.
 */
public class MSGService extends IntentService {

    SharedPreferences prefs;
    NotificationCompat.Builder notification;
    NotificationManager manager;

    public MSGService() {
        super("MSGService");
    }

    @Override
    protected  void onHandleIntent(Intent intent) {

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);
        prefs = getSharedPreferences("Chat", 0);

        if (!extras.isEmpty()) {
            if (messageType.equals(GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR)) {
                Log.e("SparkChat", "Error");
            }
            else if (messageType.equals(GoogleCloudMessaging.MESSAGE_TYPE_DELETED)) {
                Log.e("SparkChat", "Error");
            }
            else if (messageType.equals(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE)) {
                if(!prefs.getString("CURRENT_CHAT", "").equals(extras.getString("from"))) {
                    sendNotification(extras.getString("msg"), extras.getString("from"));
                }
            }
        }
        MSGReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg,String name) {

        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("msg", msg);
        // TODO this was changed from ChatActivity to ImagesActivity
        Intent chat = new Intent(this, ImagesActivity.class);
        chat.putExtra("INFO", args);
        notification = new NotificationCompat.Builder(this);
        notification.setContentTitle(name);
        notification.setContentText(msg);
        notification.setTicker("New Message !");

        PendingIntent contentIntent = PendingIntent.getActivity(this, 1000,
                chat, PendingIntent.FLAG_CANCEL_CURRENT);
        notification.setContentIntent(contentIntent);
        notification.setAutoCancel(true);
        manager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, notification.build());
    }


}
