package com.sparklounge.client.msg;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.Toast;

import com.sparklounge.client.database.ChatLogger;

/**
 * Created by Chuang on 7/28/2015.
 */
public class MSGReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        String msg = extras.getString("msg");
        String sender = extras.getString("sender");
        String receiver = extras.getString("receiver");
        String imageLocation = extras.getString("imageLocation");

        Intent msgcv = new Intent("Msg");
        msgcv.putExtra("msg", msg);
        msgcv.putExtra("from", sender);
        msgcv.putExtra("to", receiver);
        msgcv.putExtra("imageURL", imageLocation);

        // Logs received message
        // TODO: check if app is in foreground
        if (false) {
            Toast.makeText(context, "Received message from: " + sender, Toast.LENGTH_SHORT).show();
        }
        new ChatLogger(context, sender).logChatHistory(sender, receiver, msg);
        LocalBroadcastManager.getInstance(context).sendBroadcast(msgcv);
        ComponentName comp = new ComponentName(context.getPackageName(), MSGService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}
