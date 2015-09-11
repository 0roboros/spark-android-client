package com.sparklounge.client.fragments;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sparklounge.client.R;
import com.sparklounge.client.SparkApplication;
import com.sparklounge.client.database.FriendsDb;
import com.sparklounge.client.interfaces.OnItemClickListener;
import com.sparklounge.client.models.UserInfo;
import com.sparklounge.client.views.UserInfoAdapter;

import java.net.URL;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private FriendsDb friendsDb;
    private List<UserInfo> mFriends;
    private RecyclerView mFriendsRecycler;
    private UserInfoAdapter mUserInfoAdapter;
    private LinearLayoutManager mLayoutManager;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        friendsDb = new FriendsDb(getActivity());
        mFriends = friendsDb.getAllFriends();
        // TODO remove this, only for testing purposes
        if (mFriends.size() == 0) {
            friendsDb.insertTestData();
            mFriends = friendsDb.getAllFriends();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendsRecycler = (RecyclerView) view.findViewById(R.id.friends_recycler);
        mUserInfoAdapter = new UserInfoAdapter();
        mUserInfoAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                Toast.makeText(getActivity(), "You want to chat with: " + mFriends.get(position).getUserId(), Toast.LENGTH_SHORT).show();
            }
        });
        mLayoutManager = new LinearLayoutManager(getActivity());
        mFriendsRecycler.setLayoutManager(mLayoutManager);
        mFriendsRecycler.setAdapter(mUserInfoAdapter);

        // TODO: remove this, it is only for testing purposes
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap bitmap = SparkApplication.getCache().get("testProfilePic");
                    if (bitmap == null) {
                        bitmap = BitmapFactory.decodeStream(new URL("http://barkpost-assets.s3.amazonaws.com/wp-content/uploads/2013/11/grumpy-dog-11.jpg").openConnection().getInputStream());
                        SparkApplication.getCache().put("testProfilePic", bitmap);
                    }
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            mUserInfoAdapter.updateUserInfo(mFriends);
                        }
                    });
                } catch (Exception e) {

                }
            }
        }).start();
        return view;
    }
}
