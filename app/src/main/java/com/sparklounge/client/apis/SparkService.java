package com.sparklounge.client.apis;

import com.sparklounge.client.RegistrationResp;
import com.sparklounge.client.models.AccessToken;
import com.sparklounge.client.models.AwsCredentials;
import com.sparklounge.client.models.ImageList;

import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PartMap;
import retrofit.mime.TypedFile;

/**
 * Created by Chuang on 9/6/2015.
 */
public interface SparkService {
    @GET("/app/getitems")
    rx.Observable<ImageList> getImageLinks();

    @GET("/app/gets3credentials")
    void getAwsCredentials(Callback<AwsCredentials> callback);

    @POST("/app/pushitem")
    rx.Observable<String> pushItem(
            @Header("item") String imageLink,
            @Header("like") int like
    );

    @GET("/app/getmatches")
    List<String> getMatches();

    @GET("/app/refreshtoken")
    void refreshAccessToken(Callback<String> callback);

    @GET("/logintoken")
    void login(Callback<AccessToken> callback);

    @Multipart
    @POST("/app/uploaditem")
    void uploadItem(
            @PartMap Map<String,TypedFile> files,
            Callback<String> callback
    );

    @GET("/signup")
    void signup(
            @Header("Username") String username,
            @Header("Password") String password,
            @Header("Longitude") String longitude,
            @Header("Latitude") String latitude,
            @Header("GcmRegId") String gcmRegId,
            Callback<RegistrationResp> callback
    );
}
