package com.sparklounge.client.fragments;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.services.s3.AmazonS3Client;
import com.sparklounge.client.MainMenuActivity;
import com.sparklounge.client.R;
import com.sparklounge.client.SparkApplication;
import com.sparklounge.client.activities.ImagesActivity;
import com.sparklounge.client.apis.AmazonClient;
import com.sparklounge.client.apis.SparkApi;
import com.sparklounge.client.apis.TestApi;
import com.sparklounge.client.interfaces.OnItemClickListener;
import com.sparklounge.client.models.AwsCredentials;
import com.sparklounge.client.models.Image;
import com.sparklounge.client.models.ImageList;
import com.sparklounge.client.models.UserInfo;
import com.sparklounge.client.views.EndlessRecyclerOnScrollListener;
import com.sparklounge.client.views.ImageAdapter;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class ImagesFragment extends Fragment {

    private static final int MIN_LOADED_IMAGE_BEFORE_DISPLAY = 3;

    private Bundle savedState;
    private ArrayList<Image> mImages;
    private List<AsyncTask> mImagesTasks;
    private RecyclerView mImagesRecycler;
    private ImageAdapter mImageAdapter;
    private LinearLayoutManager gridLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mImagesLoadingProgressBar;
    private SparkApi mSparkApi;
    private TestApi mTestApi;
    private static AmazonClient s3Client;
    private OnMatchedUserListener mOnMatchedUser;
    public SharedPreferences prefs;

    public ImagesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImagesTasks = new ArrayList<>();
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_images, container, false);
        mImagesRecycler = (RecyclerView) rootView.findViewById(R.id.fragment_last_images_recycler);
        mImagesLoadingProgressBar = (ProgressBar) rootView.findViewById(R.id.fragment_images_progress);
       // mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);

        initiateRecyclerView();

/*        if(savedInstanceState != null && savedState == null) {
            savedState = savedInstanceState.getBundle("savedState");
        }
        if (savedState != null) {
            mImages = (ArrayList<Image>) savedState.getSerializable("mImages");
        }*/
        savedState = null;

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSparkApi = ((ImagesActivity)getActivity()).getSparkApi();

        if (mImages != null) {
            //updateAdapter(mImages);
            mImagesLoadingProgressBar.setVisibility(View.GONE);
            mImagesRecycler.setVisibility(View.VISIBLE);
            mImageAdapter.updateData(mImages);
        } else {
            //newly created, compute data
            mImages = new ArrayList<>();
            mImagesRecycler.setVisibility(View.GONE);
            mImagesLoadingProgressBar.setVisibility(View.VISIBLE);
            validateAws();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mOnMatchedUser = (OnMatchedUserListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for(AsyncTask getImageTask: mImagesTasks) {
            if(!getImageTask.isCancelled()) {
                getImageTask.cancel(true);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //savedState = saveState(); /* vstup defined here for sure */
        //mImages = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's state here
        //outState.putBundle("savedState", (savedState != null) ? savedState : saveState());
        //outState.putSerializable("mImages", mImages);

    }

    private Bundle saveState() {
        Bundle state = new Bundle();
        state.putSerializable("mImages", mImages);
        return state;
    }

    public void loadImages() {
        mSparkApi.fetchImageList().subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(imageListObserver);
    }

    private void initiateRecyclerView() {

        // data adapter
        mImageAdapter = new ImageAdapter();

        mImageAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                // TODO: add activity for onClick event on an item
                Toast.makeText(getActivity(), "You have clicked on image " + position, Toast.LENGTH_SHORT).show();
            }
        });

        gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        mImagesRecycler.setLayoutManager(gridLayoutManager);
        mImagesRecycler.setAdapter(mImageAdapter);

        mImagesRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        // add swipe to refresh
        swipeToDismissTouchHelper.attachToRecyclerView(mImagesRecycler);

        // Endless scroll
        mImagesRecycler.addOnScrollListener(new EndlessRecyclerOnScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                Log.d("THREAD", "Fetching more images");
                loadImages();
            }
        });

        /*  // Swipe down to refresh
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSparkApi.fetchImageList().subscribeOn(Schedulers.newThread())
                        .observeOn(Schedulers.newThread())
                        .subscribe(imageListObserver);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        */

    }

    private void validateAws() {
        String s3AccessKeyId = prefs.getString("s3_access_key_id", null);
        String s3SecretAccessKey = prefs.getString("s3_secret_access_key", null);
        String s3SessionToken = prefs.getString("s3_session_token", null);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String expiryTimeString = prefs.getString("s3_expiration", null);
        if (expiryTimeString == null || Long.valueOf(expiryTimeString) <= System.currentTimeMillis()){
            mSparkApi.authenticateAws(new Callback<AwsCredentials>() {
                @Override
                public void success(AwsCredentials awsCredentials, Response response) {
                    prefs.edit().putString("s3_expiration", awsCredentials.getExpiration().toString()).apply();
                    prefs.edit().putString("s3_access_key_id", awsCredentials.getAccessKeyId()).apply();
                    prefs.edit().putString("s3_secret_access_key", awsCredentials.getSecretAccessKey()).apply();
                    prefs.edit().putString("s3_session_token", awsCredentials.getSessionToken()).apply();
                    s3Client = new AmazonClient(awsCredentials);
                    loadImages();
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e("", "Failed to authenticate aws client");
                    error.printStackTrace();
                }
            });
        } else {
            s3Client = new AmazonClient(new AwsCredentials(s3AccessKeyId, s3SecretAccessKey, s3SessionToken, Long.valueOf(expiryTimeString)));
            loadImages();
        }
    }

    //TODO: Swipe is too sensitive, need to adjust sensitivity
    ItemTouchHelper swipeToDismissTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            // callback for drag-n-drop, false to skip this feature
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            // callback for swipe to dismiss, removing item from data and adapter
            Image imageRemoved = mImageAdapter.removeImage(viewHolder.getAdapterPosition());
            String imageLink = imageRemoved.getUri();
            SparkApplication.getCache().remove(imageLink);
            if(direction == ItemTouchHelper.LEFT) {
                // displike image
                dislikeImage(imageLink);
            } else {
                // like image
                likeImage(imageLink);
            }
        }
    });

    private void likeImage(String imageLink) {
        Toast.makeText(getActivity(), "Liked image: " + imageLink, Toast.LENGTH_SHORT).show();
        mSparkApi.pushItem(imageLink, 0);
    }

    private void dislikeImage(String imageLink) {
        Toast.makeText(getActivity(), "Disliked image: " + imageLink, Toast.LENGTH_SHORT).show();
        mSparkApi.pushItem(imageLink, 1);
    }

    private Observer<ImageList> imageListObserver = new Observer<ImageList>(){
        @Override
        public void onCompleted() {}
        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Failed to retrieve from amazon", Toast.LENGTH_LONG).show();
        }
        @Override
        public void onNext(ImageList imageLinks) {
            Log.d("THREAD", "Received new images on thread: " + Thread.currentThread().getName() + " : " + Thread.currentThread().getId());

            if (imageLinks.getImageList() == null) return;
            Observable.from(imageLinks.getImageList())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String imageLink) {
                            if(imageLink.startsWith("match")) {
                                mOnMatchedUser.onMatched();
                            } else {
                                GetImageTask newTask = new GetImageTask();
                                mImagesTasks.add(newTask);
                                newTask.execute(imageLink);
                            }
                        }
                    });
        }
    };

    class GetImageTask extends AsyncTask<String, Void, Image> {

        @Override
        public Image doInBackground(String... params){
            try {
                String imageLink = params[0];
//                Bitmap bitmap = BitmapFactory.decodeStream(new URL(imageLink).openConnection().getInputStream());
                Bitmap bitmap = SparkApplication.getCache().get(imageLink);
                if (bitmap == null) {
                    bitmap = s3Client.getBitmap(imageLink);
                    SparkApplication.getCache().put(imageLink, bitmap);
                }
                Image img = new Image();
                img.setUri(imageLink);
                return img;
            } catch (Exception e) {
                Log.e("", "Faild to retrieve image");
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Image image) {
            if (image != null) {
                if (mImages == null) {
                    mImages = new ArrayList<>();
                }
                mImages.add(image);

                Log.e("", "***********Loaded the " + mImages.size() + "th image***********");

                mImageAdapter.updateData(mImages);
                if (mImages.size() >= MIN_LOADED_IMAGE_BEFORE_DISPLAY && mImagesRecycler.getVisibility() != View.VISIBLE) {
                    mImagesRecycler.setVisibility(View.VISIBLE);
                    mImagesLoadingProgressBar.setVisibility(View.GONE);
                }
            }
        }

    }

    public interface OnMatchedUserListener{
        void onMatched();
    }

}
