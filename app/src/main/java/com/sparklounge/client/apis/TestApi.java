package com.sparklounge.client.apis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sparklounge.client.models.ImageList;
import com.squareup.okhttp.OkHttpClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Chuang on 8/25/2015.
 */
public class TestApi {
    public static final String ENDPOINT = "http://www.splashbase.co";

    private static Gson gson = new GsonBuilder().create();
    private static TestImageService mTestImageService;

    public TestApi() {
        OkHttpClient okHttpClient = null;
        try {
            okHttpClient = new OkHttpClient();
        } catch (Exception e) {
            e.printStackTrace();
        }

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT)
                .setClient(new OkClient(okHttpClient))
                .setConverter(new GsonConverter(gson))
                .build();

        mTestImageService = restAdapter.create(TestImageService.class);
    }


    public interface TestImageService{
        @GET("/api/v1/images/latest")
        Observable<Example> getImageLinks();
    }

    public Observable<ImageList> fetchImageList() {
        return mTestImageService.getImageLinks().map(onResponse);
    }

    private Func1<Example, ImageList> onResponse = new Func1<Example, ImageList> () {
        @Override
        public ImageList call(Example imageList) {
            List<String> imagelinks = new ArrayList<String>();
            for(Image img: imageList.getImages()) {
                imagelinks.add(img.getUrl());
            }
            return new ImageList(imagelinks);
        }
    };


    public class Example {

        private List<Image> images = new ArrayList<Image>();
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        /**
         *
         * @return
         * The images
         */
        public List<Image> getImages() {
            return images;
        }

        /**
         *
         * @param images
         * The images
         */
        public void setImages(List<Image> images) {
            this.images = images;
        }

        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }

        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }

    }

    public class Image {

        private Integer id;
        private String url;
        private String largeUrl;
        private Object sourceId;
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        /**
         *
         * @return
         * The id
         */
        public Integer getId() {
            return id;
        }

        /**
         *
         * @param id
         * The id
         */
        public void setId(Integer id) {
            this.id = id;
        }

        /**
         *
         * @return
         * The url
         */
        public String getUrl() {
            return url;
        }

        /**
         *
         * @param url
         * The url
         */
        public void setUrl(String url) {
            this.url = url;
        }

        /**
         *
         * @return
         * The largeUrl
         */
        public String getLargeUrl() {
            return largeUrl;
        }

        /**
         *
         * @param largeUrl
         * The large_url
         */
        public void setLargeUrl(String largeUrl) {
            this.largeUrl = largeUrl;
        }

        /**
         *
         * @return
         * The sourceId
         */
        public Object getSourceId() {
            return sourceId;
        }

        /**
         *
         * @param sourceId
         * The source_id
         */
        public void setSourceId(Object sourceId) {
            this.sourceId = sourceId;
        }

        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }

        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }

    }
}
