package com.sparklounge.client.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;
import com.sparklounge.client.fragments.FriendsFragment;
import com.sparklounge.client.fragments.MessagesFragment;
import com.sparklounge.client.models.AwsCredentials;
import com.sparklounge.client.R;
import com.sparklounge.client.apis.AmazonClient;
import com.sparklounge.client.apis.SparkApi;
import com.sparklounge.client.fragments.ImagesFragment;
import com.sparklounge.client.fragments.MatchesFragment;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ImagesActivity extends ActionBarActivity implements
         ImagesFragment.OnMatchedUserListener{

    private Toolbar toolbar;
    private Drawer.Result drawerResult;
    private AccountHeader.Result headerResult;

    private SharedPreferences prefs;
    private static SparkApi sSparkApi;
    private static AmazonClient s3Client;
    private static boolean launching = true;


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        initActionBar();

    }

    @Override
    protected void onResume(){
        super.onResume();
        if (prefs == null){
            prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        }
        validateSparkClient();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);

        menu.findItem(R.id.action_take_photo).setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_camera).color(Color.WHITE).actionBarSize());
        menu.findItem(R.id.action_upload_photo).setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_cloud_upload).color(Color.WHITE).actionBarSize());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_take_photo) {
            Toast.makeText(this, "You want to take a photo", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ImagesActivity.this, CaptureImageActivity.class);
            String accessTokenString = prefs.getString("access_token", null);
            intent.putExtra("access_token", accessTokenString);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_upload_photo) {
            Toast.makeText(this, "You want to upload a photo", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ImagesActivity.this, UploadActivity.class);
            String accessTokenString = prefs.getString("access_token", null);
            intent.putExtra("access_token", accessTokenString);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        } else if(id == R.id.action_settings) {
            return true;
        }

        return false;//super.onOptionsItemSelected(item);
    }

    private Drawer.OnDrawerItemClickListener onDrawerItemClickListener = new Drawer.OnDrawerItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l, IDrawerItem iDrawerItem) {
            String name = ((Nameable)iDrawerItem).getName();
            Toast.makeText(getApplicationContext(), "You chose to go to: " + name, Toast.LENGTH_SHORT).show();

            if (name.equals("Log out")) {
            //    LoginManager.getInstance().logOut();
                logout();
            } else if (name.equals("Friends")) {
                showFriendsFragment();
            } else if (name.equals("Home")) {
                showImagesFragment();
            } else if (name.equals("Messages")) {
                showMessagesFragment();
            }
        }
    };

    private void initActionBar() {
        toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
    }

    private void initDrawer() {
        headerResult = new AccountHeader()
                .withActivity(this)
                .withHeaderBackground(R.color.drawer_background)
                .addProfiles(
                        new ProfileDrawerItem().withName("Mike Penz").withEmail("mikepenz@gmail.com")
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public void onProfileChanged(View view, IProfile iProfile) {
                        // do nothing
                    }
                })
                .build();

        drawerResult = new Drawer()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Home").withIcon(GoogleMaterial.Icon.gmd_home),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName("Profile").withIcon(GoogleMaterial.Icon.gmd_account_circle),
                        new PrimaryDrawerItem().withName("Friends").withIcon(GoogleMaterial.Icon.gmd_people),
                        new PrimaryDrawerItem().withName("Messages").withIcon(GoogleMaterial.Icon.gmd_message),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName("Log out").withIcon(GoogleMaterial.Icon.gmd_time_to_leave)
                )
                .withOnDrawerItemClickListener(onDrawerItemClickListener)
                .build();
        drawerResult.getListView().setVerticalScrollBarEnabled(false);
    }

    private void validateSparkClient() {
        String accessTokenString = prefs.getString("access_token", null);
        if (accessTokenString == null) {
            showLogin();
        } else {
            if (sSparkApi == null) {
                sSparkApi = new SparkApi(accessTokenString);
            }
            sSparkApi.refreshAccessToken(new Callback<String>() {
                @Override
                public void success(String responseString, Response response) {
                    if (responseString == null) {
                        Log.d("", "refresh token not successful;");
                        showLogin();
                    } else {
                        Log.d("", "Logged in with refresh token");
                        resumeFragments();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e("", "Failed to refresh token");
                    error.printStackTrace();
                    showLogin();
                }
            });
        }
    }

    public AmazonClient getS3Client() {
        return s3Client;
    }

    public SparkApi getSparkApi() {
        return sSparkApi;
    }

    private void showLogin() {
        Log.e("", "~~~~~~~~~~~~~~Launching login activity~~~~~~~~~~~~~~");
        Intent intent = new Intent(ImagesActivity.this, SigninActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showImagesFragment() {

        if (drawerResult == null) {
            initDrawer();
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ImagesFragment imagesFragment = (ImagesFragment) getFragmentManager().findFragmentByTag("imagesFragment");

        if (imagesFragment == null) {
            imagesFragment = new ImagesFragment();
        }
        if (imagesFragment.isAdded()) {
            ft.show(imagesFragment);
        } else {
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.replace(R.id.fragment_container, imagesFragment, "imagesFragment");
            ft.addToBackStack(null);
            ft.commitAllowingStateLoss();
        }
    }

    private void showMatchesFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();


        MatchesFragment matchesFragment = (MatchesFragment) getFragmentManager().findFragmentByTag("matchesFragment");
        if (matchesFragment == null) {
            matchesFragment = new MatchesFragment();
            Bundle args = new Bundle();
            matchesFragment.setArguments(args);
        }

        ft.replace(R.id.fragment_container, matchesFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();

    }

    private void showFriendsFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        FriendsFragment friendsFragment = (FriendsFragment) getFragmentManager().findFragmentByTag("friendsFragment");
        if (friendsFragment == null) {
            friendsFragment = new FriendsFragment();
        }
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.replace(R.id.fragment_container, friendsFragment, "friendsFragment");
        ft.addToBackStack(null);
        ft.commit();
    }

    private void logout() {

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ImagesFragment imagesFragment = (ImagesFragment) getFragmentManager().findFragmentByTag("imagesFragment");
        if(imagesFragment != null) {
            ft.remove(imagesFragment);
            ft.commit();
        }

        if (prefs != null) {
            prefs.edit().clear().apply();
        }
        launching = true;
        showLogin();
    }

    private void showMessagesFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        MessagesFragment messagesFragment = (MessagesFragment) getFragmentManager().findFragmentByTag("messagesFragment");
        if (messagesFragment == null) {
            messagesFragment = new MessagesFragment();
        }
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.replace(R.id.fragment_container, messagesFragment, "messagesFragment");
        ft.addToBackStack(null);
        ft.commit();
    }

    private void resumeFragments() {
        if (launching) {
            showImagesFragment();
            launching = false;
        } else {
            // do nothing
        }
    }

    @Override
    public void onMatched() {
        showMatchesFragment();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Spark?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        launching = true;
                        ImagesActivity.super.onBackPressed();
                    }
                }).create().show();
    }

}
