package com.app.garini.garini.service;

import android.util.Log;

import com.app.garini.garini.utile.StaticValue;
import com.app.garini.garini.utile.UserSessionManager;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by m.lagha on 29/01/2017.
 */

public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

    private String URL = StaticValue.URL;

    @Override
    public void onTokenRefresh() {

        String token = FirebaseInstanceId.getInstance().getToken();
        registerTokenPref(token);
    }

    private void registerToken(String token) {

        OkHttpClient client = new OkHttpClient();

        RequestBody body = new FormBody.Builder()
                .add("token",token)
                .build();
        Request request = new Request.Builder()
                .url(URL+"register.php")
                .post(body)
                .build();

        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerTokenPref(String token){
        UserSessionManager userSessionManager = new UserSessionManager(getApplicationContext());
        userSessionManager.FirebaseTokenSave(token);
    }
}
