package com.app.garini.garini.utile;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by m.lagha on 12/02/2017.
 */

public class JSONParserRegister {
    /********
     * URLS
     *******/
    private static final String MAIN_URL = StaticValue.URL+"register.php";
    /**
     * TAGs Defined Here...
     */
    public static final String TAG = "TAG";
    /**
     * Key to Send
     */
    private static final String KEY_USER_ID = "user_id";
    /**
     * Response
     */
    private static Response response;
    /**
     * Get Table Booking Charge
     *
     * @return JSON Object
     */
    public static JSONObject register(
            String name,String email,String password,String mobile,String marque,String modele,String couleur,String matricule,String token_firebase,String is_fb) {
        try {
            OkHttpClient client = new OkHttpClient();

            RequestBody body = new FormBody.Builder()
                    .add("register","ok")
                    .add("nom",name)
                    .add("token_firebase",token_firebase)
                    .add("email",email)
                    .add("password",password)
                    .add("mobile",mobile)
                    .add("marque",marque)
                    .add("modele",modele)
                    .add("matricule",matricule)
                    .add("couleur",couleur)
                    .add("fb",is_fb)
                    .build();
            Request request = new Request.Builder()
                    .url(MAIN_URL)
                    .post(body)
                    .build();
            response = client.newCall(request).execute();
            String responseString = response.body().string();
            Log.e("Registration",responseString);
            return new JSONObject(responseString);
        } catch (@NonNull IOException | JSONException e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
        return null;
    }

    public static JSONObject registerFB(
            String name,String email,String mobile,String marque,String modele,String couleur,String matricule,String token_firebase,String is_fb,String fb_id,String image,String type) {
        try {
            OkHttpClient client = new OkHttpClient();

            RequestBody body = new FormBody.Builder()
                    .add("register","ok")
                    .add("nom",name)
                    .add("token_firebase",token_firebase)
                    .add("email",email)
                    .add("mobile",mobile)
                    .add("marque",marque)
                    .add("modele",modele)
                    .add("matricule",matricule)
                    .add("couleur",couleur)
                    .add("fb",is_fb)
                    .add("fb_id",fb_id)
                    .add("image",image)
                    .add("type",type)
                    .build();
            Request request = new Request.Builder()
                    .url(MAIN_URL)
                    .post(body)
                    .build();
            response = client.newCall(request).execute();
            String responseString = response.body().string();
            Log.e("Registration",responseString);
            return new JSONObject(responseString);
        } catch (@NonNull IOException | JSONException e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
        return null;
    }
}