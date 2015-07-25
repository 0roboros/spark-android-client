package com.sparklounge.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;


/**
 * A placeholder fragment containing a simple view.
 */
public class LoginActivityFragment extends Fragment {
    public LoginButton fbLogin;
    public CallbackManager callbackManager;
    public LoginManager loginManager;
    private AccessToken fbAccessToken;
    private AccessTokenTracker fbAccessTokenTracker;
    private Profile fbProfile;
    private ProfileTracker fbProfileTracker;
    private SharedPreferences prefs;

    public LoginActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity());
        callbackManager = CallbackManager.Factory.create();
        fbAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                fbAccessToken = currentAccessToken;
            }
        };

        fbAccessToken = AccessToken.getCurrentAccessToken();

        fbProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(
                    Profile oldProfile,
                    Profile currentProfile) {
                fbProfile = currentProfile;
            }
        };

        fbProfile = Profile.getCurrentProfile();

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        fbLogin = (LoginButton) view.findViewById(R.id.fb_login);

        fbLogin.setReadPermissions("user_friends");
        // If using in a fragment
        fbLogin.setFragment(this);


        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                if (prefs.getString("access_token", null) == null) {
                    fbAccessToken = loginResult.getAccessToken();
                    new validateFbTask().execute();
                }
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });


        return view;
    }

    class validateFbTask extends AsyncTask<Void, Void, com.sparklounge.client.AccessToken>{

        @Override
        protected com.sparklounge.client.AccessToken doInBackground(Void... params){
            try {
                final String signUpUrl = getResources().getString(R.string.base_api_url) + "/signupfb";
                final String logInUrl = getResources().getString(R.string.base_api_url) + "/logintokenfb";
                RestTemplate restTemplate = new RestTemplate();
                MappingJackson2HttpMessageConverter jsonToPojo = new MappingJackson2HttpMessageConverter();
                jsonToPojo.getObjectMapper().setPropertyNamingStrategy(
                        PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
                restTemplate.getMessageConverters().add(jsonToPojo);

                HttpHeaders headers = new HttpHeaders();

                headers.add("FBAccessToken", fbAccessToken.getToken());
                headers.add("FBUserId", fbAccessToken.getUserId());
                HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
                ResponseEntity<RegistrationResp> responseEntity;
                responseEntity = restTemplate.exchange(signUpUrl, HttpMethod.GET, httpEntity, RegistrationResp.class);
                RegistrationResp registrationResp = responseEntity.getBody();
                if (registrationResp.getStatus().equals("unauthorized")) {
                    return null;
                }

                ResponseEntity<com.sparklounge.client.AccessToken> responseEntity2;
                responseEntity2 = restTemplate.exchange(logInUrl, HttpMethod.GET, httpEntity, com.sparklounge.client.AccessToken.class);
                com.sparklounge.client.AccessToken accessToken = responseEntity2.getBody();
                return accessToken;

            } catch (Exception e){
                Log.e("LoginActivityFragment", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(com.sparklounge.client.AccessToken accessToken){
            if (accessToken == null) {
                TextView failedRequest = (TextView) getActivity().findViewById(R.id.failed_request);
                failedRequest.setText(getResources().getString(R.string.failed_login));
                failedRequest.setVisibility(View.VISIBLE);
            } else {
                prefs.edit().putString("access_token", accessToken.getAccessToken()).apply();
                Intent intent = new Intent(getActivity(), MainMenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }
    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fbAccessTokenTracker.stopTracking();
    }



}
