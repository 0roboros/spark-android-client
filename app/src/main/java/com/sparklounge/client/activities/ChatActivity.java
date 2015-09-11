package com.sparklounge.client.activities;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sparklounge.client.R;
import com.sparklounge.client.database.ChatLogger;
import com.sparklounge.client.database.FriendsDb;
import com.sparklounge.client.database.SparkContract;
import com.sparklounge.client.interfaces.OnItemClickListener;
import com.sparklounge.client.models.Conversation;
import com.sparklounge.client.models.UserInfo;
import com.sparklounge.client.views.MessageAdapter;


import org.apache.http.NameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chuang on 7/28/2015.
 */
public class ChatActivity extends ActionBarActivity{

    private static int RESULT_LOAD_IMG = 1;

    static boolean isRunning;
    SharedPreferences prefs;
    private Toolbar toolbar;
    private List<Conversation> mMessages;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mChatRecycler;
    private LinearLayoutManager mLayoutManager;
    private MessageAdapter mMessageAdapter;
    private FriendsDb friendsDb;

    List<NameValuePair> params;
    EditText chat_msg;
    Button send_btn, upload_btn;
    Bundle bundle;
    Bitmap bitmap;
    String imagePath;
    ChatLogger logger;
    UserInfo friend;
    String userName;

    private static int originalHeightDiff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        isRunning = true;
        bundle = getIntent().getExtras();
        String friendName = bundle.getString("friend_name");
        userName = prefs.getString("USER_NAME", "");

        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("CURRENT_CHAT", friendName);
        edit.commit();

        logger = new ChatLogger(this, friendName);
        friendsDb = new FriendsDb(this);
        friend = friendsDb.getFriendInfo(friendName);

        mMessages = new ArrayList<>();
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mChatRecycler = (RecyclerView) findViewById(R.id.chat_recycler);
        mMessageAdapter = new MessageAdapter(friendName);
        mLayoutManager = new LinearLayoutManager(this);
        mChatRecycler.setLayoutManager(mLayoutManager);
        mChatRecycler.setAdapter(mMessageAdapter);
        mChatRecycler.setHasFixedSize(false);

        mMessageAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                //Toast.makeText(getActivity(), "You have clicked on image " + position, Toast.LENGTH_SHORT).show();
            }
        });

        // Swipe down to refresh
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(getApplicationContext(), "You want to refresh", Toast.LENGTH_SHORT).show();
                LoadHistoryOnTop();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        chat_msg = (EditText)findViewById(R.id.chat_msg);
        send_btn = (Button)findViewById(R.id.btn_send);
        upload_btn = (Button)findViewById(R.id.btn_upload);

        LoadHistoryOnTop();

        mChatRecycler.scrollToPosition(mMessages.size() - 1);

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //new Send().execute();
            }
        });

        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bitmap == null) {
                    Toast.makeText(getApplicationContext(),
                            "Please select image", Toast.LENGTH_SHORT).show();
                    Intent gallery = new Intent(Intent.ACTION_PICK);
                    //MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    gallery.setType("image/*");
                    startActivityForResult(gallery, 1);
                } else {

                }
            }
        });

        initActionBar();


        final View activityRootView = findViewById(R.id.chat_root_view);

        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                Rect r = new Rect();
                //r will be populated with the coordinates of your view that area still visible.
                activityRootView.getWindowVisibleDisplayFrame(r);

                int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
                if (originalHeightDiff == 0) originalHeightDiff = heightDiff;
                if (heightDiff > 700) { // if more than 100 pixels, its probably a keyboard...
                    RelativeLayout chatContent = (RelativeLayout)findViewById(R.id.chat_content);
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)chatContent.getLayoutParams();
                    params.setMargins(0,0,0,heightDiff - originalHeightDiff);
                    chatContent.setLayoutParams(params);
                    mChatRecycler.scrollToPosition(mMessages.size()-1);
                } else {
                    RelativeLayout chatContent = (RelativeLayout)findViewById(R.id.chat_content);
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)chatContent.getLayoutParams();
                    params.height += heightDiff-originalHeightDiff;
                    params.setMargins(0,0,0,0);
                    chatContent.setLayoutParams(params);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_upload, menu);
        return true;
    }

    private void LoadHistoryOnTop() {
        Cursor history = logger.retreiveChatHistory();
        while(history.moveToNext()) {
            Conversation message;
            String sender = history.getString(history.getColumnIndex(SparkContract.Conversation.KEY_SENDER));
            String msg = history.getString(history.getColumnIndex(SparkContract.Conversation.KEY_MSG));
            String sentTime = history.getString(history.getColumnIndex(SparkContract.Conversation.KEY_TIME));
            if (sender.equals(friend.getUserId())) {
                message = new Conversation(friend, msg, sentTime);
            } else {
                message = new Conversation(new UserInfo(userName, "", "", ""), msg, sentTime);
            }
            mMessages.add(0, message);
        }
        mMessageAdapter.updateData(mMessages);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data
                Toast.makeText(this, "got image", Toast.LENGTH_SHORT).show();
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imagePath = cursor.getString(columnIndex);
                cursor.close();
                Toast.makeText(this, "got data", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unregister broadcast listener
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("CURRENT_CHAT", null);
        edit.commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("CURRENT_CHAT", null);
        edit.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
    }

    private void initActionBar() {
        toolbar = (Toolbar) findViewById(R.id.activity_chat_toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle("Chat with " + friend.getUserId());
        setSupportActionBar(toolbar);
    }

    private BroadcastReceiver onNotice= new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("msg");
            String from = intent.getStringExtra("from");
            if(from.equals(friend.getUserId())){
                Conversation message = new Conversation(friend, msg, "");
                mMessages.add(message);
                mMessageAdapter.updateData(mMessages);

                if (intent.getStringExtra("imageURL") != "") {
                    bundle.putString("imageURL", intent.getStringExtra("imageURL"));
                    //new AddImageView().execute();
                }
            }
        }
    };

/*
    private class AddImageView extends AsyncTask<String, String, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... args) {
            try {
                String imageURL = getResources().getString(R.string.server_endpoint) + "/uploads/" + bundle.getString("imageURL");
                HttpClient httpclient = new DefaultHttpClient();
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("imageURL", bundle.getString("imageURL")));
                params.add(new BasicNameValuePair("reg_id", prefs.getString("REG_ID", "")));
                String paramsString = URLEncodedUtils.format(params, "UTF-8");
                HttpGet httpGet = new HttpGet(imageURL + "?" + paramsString);
                HttpResponse httpResponse = httpclient.execute(httpGet);
                //URL newurl = new URL(imageURL);
                Bitmap image = BitmapFactory.decodeStream(httpResponse.getEntity().getContent());
                return image;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap image) {
            TableRow imgrow = new TableRow(getApplicationContext());
            imgrow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
            ImageView imgView = new ImageView(getApplicationContext());
            imgView.setImageBitmap(image);
            imgrow.addView(imgView);
            tab.addView(imgrow);
        }
    }

    private class Send extends AsyncTask<String, String, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... args) {

            if (imagePath != null && !imagePath.isEmpty()) {

                //Ion.getDefault(ChatActivity.this).configure().setLogging("ion-sample", Log.DEBUG);

                File compressedFile = new File(Environment.getExternalStorageDirectory(),
                        "/SparkCompressed" + System.currentTimeMillis() + ".jpg");
                try {
                    FileOutputStream fos = new FileOutputStream(compressedFile);
                    Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imagePath), 960, 960);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);

                    HttpClient httpclient = new DefaultHttpClient();

                    HttpPost httppost = new HttpPost(getResources().getString(R.string.server_endpoint) + "/upload");
                    FileBody file = new FileBody(compressedFile);
                    HttpEntity reqEntity = MultipartEntityBuilder.create()
                            .addPart("image", file)
                            .addPart("msg", new StringBody("An image", ContentType.TEXT_PLAIN))
                            .addPart("from", new StringBody(prefs.getString("USER_NAME",""), ContentType.TEXT_PLAIN))
                            .addPart("to", new StringBody(bundle.getString("friend_name"), ContentType.TEXT_PLAIN))
                            .build();
                    httppost.setEntity(reqEntity);
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity resEntity = response.getEntity();
                    if (resEntity != null) {
                        System.out.println("Response content length: " + resEntity.getContentLength());
                    }
                    fos.flush();
                    fos.close();

                    return new JSONObject(EntityUtils.toString(resEntity, "UTF-8"));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch(JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (compressedFile != null)
                        compressedFile.delete();
                }
                return null;
            } else {

                JSONParser json = new JSONParser();
                params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("from", prefs.getString("USER_NAME", "")));
                params.add(new BasicNameValuePair("to", bundle.getString("friend_name")));
                params.add((new BasicNameValuePair("msg", chat_msg.getText().toString())));

                JSONObject jObj = json.getJSONFromUrl(getResources().getString(R.string.server_endpoint) + "/send", params);
                return jObj;
            }
        }
        @Override
        protected void onPostExecute(JSONObject json) {
            String res = null;
            try {
                res = json.getString("response");
                if(res.equals("Failure")){
                    Toast.makeText(getApplicationContext(), "The user has logged out. You cant send message anymore !", Toast.LENGTH_SHORT).show();
                } else {
                    logger.logChatHistory(prefs.getString("USER_NAME", ""), bundle.getString("friend_name"), chat_msg.getText().toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            chat_msg.setText("");
        }
    }
    */
}
