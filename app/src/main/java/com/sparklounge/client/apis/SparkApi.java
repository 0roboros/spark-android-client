package com.sparklounge.client.apis;

import android.util.Base64;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sparklounge.client.RegistrationResp;
import com.sparklounge.client.models.AccessToken;
import com.sparklounge.client.models.AwsCredentials;
import com.sparklounge.client.models.ImageList;

import com.squareup.okhttp.OkHttpClient;

import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedFile;

/**
 * Created by Chuang on 8/24/2015.
 */
public class SparkApi {

//    public static final String ENDPOINT = "http://192.168.0.114:8080";
    public static final String ENDPOINT = "http://ec2-52-24-242-8.us-west-2.compute.amazonaws.com:8080";

    public static Gson gson;
    private SparkService mSparkService;

    public SparkApi() {
        gson = new GsonBuilder().create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT)
                .setClient(new OkClient(new OkHttpClient()))
                .setConverter(new GsonConverter(gson))
                .build();
        mSparkService = restAdapter.create(SparkService.class);
    }

    public SparkApi(final String accessTokenString) {
        gson = new GsonBuilder().create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT)
                .setClient(new OkClient(new OkHttpClient()))
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addHeader("Authorization", "Bearer " + accessTokenString);
                    }
                })
                .build();
        mSparkService = restAdapter.create(SparkService.class);    }

    public SparkApi(String username, String password) {

        gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        final String credentials = username + ":" + password;

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT)
                .setClient(new OkClient(new OkHttpClient()))
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        String string = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                        request.addHeader("Authorization", string);
                        request.addHeader("Accept", "application/json");
                    }
                })
                .build();
        mSparkService = restAdapter.create(SparkService.class);
    }

    public rx.Observable<ImageList> fetchImageList() {
        return mSparkService.getImageLinks();
    }

    public void authenticateAws(Callback<AwsCredentials> callback) {
        mSparkService.getAwsCredentials(callback);
    }

    public void pushItem(String imageLink, int like) {
        mSparkService.pushItem(imageLink, like);
    }

    public List<String> getPotentialMatches() {
        return mSparkService.getMatches();
    }

    public void refreshAccessToken(Callback<String> callback) {
        mSparkService.refreshAccessToken(callback);
    }

    public void loginUser(Callback<AccessToken> callback) {
        mSparkService.login(callback);
    }

    public void uploadItem(Map<String, TypedFile> files, Callback<String> callback) {
        mSparkService.uploadItem(files, callback);
    }

    public void signup(String username, String pw, String longitude, String latitude, String gcmRegId, Callback<RegistrationResp> callback) {
        mSparkService.signup(username, pw, longitude, latitude, gcmRegId, callback);
    }
}
