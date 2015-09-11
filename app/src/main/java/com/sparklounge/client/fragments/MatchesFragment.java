package com.sparklounge.client.fragments;


import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.sparklounge.client.R;
import com.sparklounge.client.SparkApplication;
import com.sparklounge.client.activities.ImagesActivity;
import com.sparklounge.client.interfaces.OnItemClickListener;
import com.sparklounge.client.models.UserInfo;
import com.sparklounge.client.views.UserInfoAdapter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MatchesFragment extends Fragment {

    private ArrayList<UserInfo> mUserInfos;
    private RecyclerView mUserInfoRecycler;
    private UserInfoAdapter mUserInfoAdapter;
    private Button mNotInterestedBtn;

    public MatchesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO Remove this, its for test;
        Bundle args = getArguments();
        if (args != null) {
            mUserInfos = (ArrayList<UserInfo>) args.getSerializable("mUserInfos");
        } else {
            new GetUserInfo().execute();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_matches, container, false);

        mNotInterestedBtn = (Button)rootView.findViewById(R.id.btn_not_interested);
        mNotInterestedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().popBackStack();
            }
        });
        mUserInfoRecycler = (RecyclerView)rootView.findViewById(R.id.fragment_potential_matches_recycler);
        mUserInfoAdapter = new UserInfoAdapter();
        mUserInfoAdapter.setOnItemClickListener(this.mUserInfoClickListener);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mUserInfoRecycler.setLayoutManager(layoutManager);
        mUserInfoRecycler.setAdapter(mUserInfoAdapter);

        return rootView;
    }

    private OnItemClickListener mUserInfoClickListener = new OnItemClickListener() {
        @Override
        public void onClick(View v, int position) {
            UserInfo userInfo = mUserInfos.get(position);
            Toast.makeText(getActivity(), "Click on user: " + userInfo.getUserId(), Toast.LENGTH_SHORT).show();
        }
    };

    class GetUserInfo extends AsyncTask<Void, Void, ArrayList<UserInfo>> {

        @Override
        public ArrayList<UserInfo> doInBackground(Void... params) {
            ArrayList<UserInfo> userInfos = new ArrayList<>();
            try {
                List<String> userNames = new ArrayList<>();
                userNames.add("James");
                userNames.add("Paul");
                userNames.add("Fuck");
                //List<String> userNames = mSparkApi.getPotentialMatches();
                for(String userName : userNames){

                    Bitmap bitmap = SparkApplication.getCache().get(userName);
                    if (bitmap == null) {
                        bitmap = ((ImagesActivity)getActivity()).getS3Client().getBitmap("James", 100);
                        SparkApplication.getCache().put(userName, bitmap);
                    }

                    UserInfo userInfo = new UserInfo(userName, "", userName, "");
                    userInfos.add(userInfo);
                }
                return userInfos;
            } catch (Exception e)
            {
                Log.e("", "Failed to get profile picture from Amazon");
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<UserInfo> userInfos) {
            if (userInfos == null) {
                // TODO: what happends if failed to get profile picture?
            } else {
                mUserInfos = userInfos;
                mUserInfoAdapter.updateUserInfo(mUserInfos);
            }
        }
    }

}
