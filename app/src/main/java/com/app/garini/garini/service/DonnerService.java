package com.app.garini.garini.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.app.garini.garini.MainActivity;
import com.app.garini.garini.MapActivity;
import com.app.garini.garini.MapsFrag;
import com.app.garini.garini.utile.NewMessageNotification;
import com.app.garini.garini.utile.StaticValue;
import com.app.garini.garini.utile.UserSessionManager;

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

public class DonnerService extends Service {
    private static final String LOG_TAG = "DonnerService";
    public static boolean IS_SERVICE_RUNNING = false;
    TimerTask task;
    int id_donner;
    private String URL = StaticValue.URL;
    double lat,lng;
    long t0;
    int nb_point = 0;


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IS_SERVICE_RUNNING = true;

        if(intent!=null && intent.getExtras()!=null){
            Bundle inBundle = intent.getExtras();
            t0 = System.currentTimeMillis();
            if(inBundle != null) {

                lat = inBundle.getDouble("lat");
                lng = inBundle.getDouble("lng");
                id_donner = inBundle.getInt("id_donner");
                nb_point = inBundle.getInt("nb_point");
                setRepeatingAsyncTaskNow(id_donner);

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


    class AfterDonnerTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            String json_string = null;
            Log.e("service donner",lat+" "+lng+" "+id_donner+" time :"+t0);
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new FormBody.Builder()
                        .add("id_donner",params[0])
                        .build();
                Request request = new Request.Builder()
                        .url(URL+"service_donner.php")
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
                    boolean trouver = jsonObject.getBoolean("trouver");
                    if(trouver){
                            //id_trouver = jsonObject.getInt("id");
                            int id_attribuer = jsonObject.getInt("id_attribuer");
                            String marque = jsonObject.getString("marque");
                            String modele = jsonObject.getString("modele");
                            String couleur = jsonObject.getString("couleur");
                            String matricule = jsonObject.getString("matricule");
                            String nom_user = jsonObject.getString("nom");
                            double lat_d = jsonObject.getDouble("lat_d");
                            double lat_a = jsonObject.getDouble("lat_a");
                            double lng_a = jsonObject.getDouble("lng_a");
                            double lng_d = jsonObject.getDouble("lng_d");
                            stopSelf();
                            task.cancel();
                            if(MapActivity.active){
                                //Intent intent = new Intent(getApplicationContext(), MapActivity.class);

                                Intent intent = new Intent("donnerService");
                                /*intent.putExtra("lat",lat);
                                intent.putExtra("lng",lng);
                                intent.putExtra("nb_point",nb_point);*/

                                intent.putExtra("id_attribuer",id_attribuer);
                                intent.putExtra("lat_d",lat_d);
                                intent.putExtra("lng_a",lng_a);
                                intent.putExtra("lat_a",lat_a);
                                intent.putExtra("lng_d",lng_d);
                                intent.putExtra("marque",marque);
                                intent.putExtra("modele",modele);
                                intent.putExtra("couleur",couleur);
                                intent.putExtra("matricule",matricule);
                                intent.putExtra("nom_user",nom_user);
                                sendLocationBroadcast(intent);

                                /*intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);*/

                            }else{
                                NewMessageNotification notif =  new NewMessageNotification();
                                notif.notify(getApplicationContext(),"Gari",id_attribuer,
                                        "un automobiliste a été trouver pour votre place",
                                        id_attribuer,lat,lng,nb_point,lat_a,lng_a,lat_d,lng_d,marque,modele,couleur,matricule,nom_user);
                            }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setRepeatingAsyncTaskNow(final int id_donner) {

        final Handler handler = new Handler();
        Timer timer = new Timer();

        task = new TimerTask() {

            @Override
            public void run() {
                if (System.currentTimeMillis() - t0 > 60 * 1000*3) {

                    timeLimit();
                }else{
                    handler.post(new Runnable() {
                        public void run() {
                            try {
                                new AfterDonnerTask().execute(id_donner + "");
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
        if(MapActivity.active){
            Intent intent = new Intent("donnerService");
            intent.putExtra("timelimit",true);
            sendLocationBroadcast(intent);
        }else{
            NewMessageNotification notif =  new NewMessageNotification();
            notif.notify3(getApplicationContext(),"Gari",id_donner,"Aucun automobiliste trouver",true,lat,lng,nb_point);
        }
    }

    private void sendLocationBroadcast(Intent intent){
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


}