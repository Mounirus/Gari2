package com.app.garini.garini.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.app.garini.garini.utile.StaticValue;
import com.app.garini.garini.utile.UserSessionManager;
import java.io.IOException;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by m.lagha on 21/03/2017.
 */

public class CleanServiceNet extends Service {

    UserSessionManager pref;
    private String URL = StaticValue.URL;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        pref = new UserSessionManager(getApplicationContext());
        if(pref.getIdDonner()!=0){
            int id_donner = pref.getIdDonner();
            new AnnulerDonnerTask().execute(id_donner+"");
            if(pref.getIdAttribuer()!=0){
                int id_attribuer = pref.getIdAttribuer();
                new AnnulerAttribuerTask().execute(id_attribuer+"");
            }
        }
        if(!pref.isTrouver()){
            if(pref.getIdTrouver()!=0){
                int id_trouver = pref.getIdTrouver();
                new AnnulerTrouverTask().execute(id_trouver+"");
                if(pref.getIdAttribuer()!=0){
                    int id_attribuer = pref.getIdAttribuer();
                    new AnnulerAttribuerTask().execute(id_attribuer+"");
                }
            }
        }

        return START_STICKY;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class AnnulerDonnerTask extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... params) {

                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = new FormBody.Builder()
                            .add("id_donner",params[0]+"")
                            .build();
                    Request request = new Request.Builder()
                            .url(URL+"annuler_donner.php")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    response.body().string();

                } catch (@NonNull IOException e) {
                    Log.e("Json ErrorMarque", "" + e.getLocalizedMessage());
                }

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pref.deleteDonner();
            pref.setIdDonner(0);
        }
    }

    public class AnnulerAttribuerTask extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... params) {

                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = new FormBody.Builder()
                            .add("id_attribuer",params[0]+"")
                            .build();
                    Request request = new Request.Builder()
                            .url(URL+"annuler_attribuer.php")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    response.body().string();

                } catch (@NonNull IOException e) {
                    Log.e("Json ErrorMarque", "" + e.getLocalizedMessage());
                }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pref.deleteAttribuer();
            pref.setIdAttribuer(0);
        }
    }

    public class AnnulerTrouverTask extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... params) {

                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = new FormBody.Builder()
                            .add("id_trouver",params[0]+"")
                            .build();
                    Request request = new Request.Builder()
                            .url(URL+"annuler_trouver.php")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    response.body().string();

                } catch (@NonNull IOException e) {
                    Log.e("Json ErrorMarque", "" + e.getLocalizedMessage());
                }

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pref.deleteTrouver();
            pref.setIdTrouver(0);
        }
    }
}
