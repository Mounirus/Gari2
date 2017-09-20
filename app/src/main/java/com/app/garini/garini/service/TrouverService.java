package com.app.garini.garini.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.app.garini.garini.MapActivityTrouver;
import com.app.garini.garini.utile.NewMessageNotification;
import com.app.garini.garini.utile.StaticValue;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TrouverService extends Service {
    private static final String LOG_TAG = "TrouverService";
    public static boolean IS_SERVICE_RUNNING = false;
    TimerTask task;
    int id_trouver;
    int id_donner;
    private String URL = StaticValue.URL;
    double lat,lng;
    long t0;
    int nb_point = 0;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(LOG_TAG,"Service start!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IS_SERVICE_RUNNING = true;

        if(intent.getExtras()!=null){
            Bundle inBundle = intent.getExtras();
            t0 = System.currentTimeMillis();
            if(inBundle != null) {

                lat = inBundle.getDouble("lat");
                lng = inBundle.getDouble("lng");
                id_trouver = inBundle.getInt("id_trouver");
                nb_point = inBundle.getInt("nb_point");
                setRepeatingAsyncTaskNow(id_trouver);

            }else{
                stopSelf();
            }
        }else{
            stopSelf();
        }


        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(task != null){
            task.cancel();
        }
        IS_SERVICE_RUNNING = false;
        Log.e(LOG_TAG,"Service Detroyed!");
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }


    class AfterTrouverTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            String json_string = null;
            Log.e(LOG_TAG,lat+" "+lng+" "+id_trouver+" time :"+t0);
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new FormBody.Builder()
                        .add("id_trouver",params[0])
                        .build();
                Request request = new Request.Builder()
                        .url(URL+"donner_service.php")
                        .post(body)
                        .build();
                response = client.newCall(request).execute();
                json_string = response.body().string();

            } catch (@NonNull IOException e) {
                Log.e("service AfterDonnerTask", "" + e.getLocalizedMessage());
            }

            return json_string;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null && !s.equals("null")) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    boolean donner = jsonObject.getBoolean("donner");
                    if(donner){
                            id_donner = jsonObject.getInt("id");
                            stopSelf();
                            task.cancel();
                            if(MapActivityTrouver.active){
                                Intent intent = new Intent(getApplicationContext(), MapActivityTrouver.class);
                                intent.putExtra("id_donner",id_donner);
                                intent.putExtra("lat",lat);
                                intent.putExtra("lng",lng);
                                intent.putExtra("nb_point",nb_point);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);

                            }else{
                                NewMessageNotification notif =  new NewMessageNotification();
                                notif.notify4(getApplicationContext(),"Gari",id_donner,"une place a été trouver, voulez-vous prendre cette place ?",id_donner,lat,lng,nb_point);
                            }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setRepeatingAsyncTaskNow(final int id_trouver) {

        final Handler handler = new Handler();
        Timer timer = new Timer();

        task = new TimerTask() {

            @Override
            public void run() {
                if (System.currentTimeMillis() - t0 > 60 * 1000*5) {

                    timeLimit();
                }else{
                    handler.post(new Runnable() {
                        public void run() {
                            try {
                                new AfterTrouverTask().execute(id_trouver + "");
                            } catch (Exception e) {
                                // error, do something
                            }
                        }
                    });
                }
            }
        };


        timer.schedule(task, 0, 6*1000);  // interval of one minute

    }

    private void timeLimit(){
        stopSelf();
        if(MapActivityTrouver.active){
            Intent intent = new Intent(getApplicationContext(), MapActivityTrouver.class);
            intent.putExtra("timelimit",true);
            intent.putExtra("lat",lat);
            intent.putExtra("lng",lng);
            intent.putExtra("nb_point",nb_point);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        }else{
            NewMessageNotification notif =  new NewMessageNotification();
            notif.notify5(getApplicationContext(),"Gari",id_donner,"Aucun automobiliste trouver",true,lat,lng,nb_point);
        }
    }
}