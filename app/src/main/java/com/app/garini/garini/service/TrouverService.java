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

import com.app.garini.garini.MapActivityTrouver;
import com.app.garini.garini.utile.NewMessageNotification;
import com.app.garini.garini.utile.StaticValue;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

        if(intent!=null && intent.getExtras()!=null){
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateandTime = sdf.format(new Date());

            Log.e(LOG_TAG,lat+" "+lng+" "+id_trouver+" time :"+t0);
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new FormBody.Builder()
                        .add("id_trouver",params[0])
                        .add("date_heur",currentDateandTime)
                        .build();
                Request request = new Request.Builder()
                        .url(URL+"service_trouver.php")
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
                    boolean error = jsonObject.getBoolean("error");
                    boolean donner = jsonObject.getBoolean("donner");
                    if(donner){

                            int id_attribuer = jsonObject.getInt("id_attribuer");
                            String marque = jsonObject.getString("marque");
                            String modele = jsonObject.getString("modele");
                            String couleur = jsonObject.getString("couleur");
                            String matricule = jsonObject.getString("matricule");
                            String nom_user = jsonObject.getString("nom");
                            double lat_destination = jsonObject.getDouble("lat");
                            double lng_destination = jsonObject.getDouble("lng");
                            String heur_liberer = jsonObject.getString("heur_liberer");

                            stopSelf();
                            task.cancel();
                            if(MapActivityTrouver.active){
                                Intent intent = new Intent("trouverService");
                                intent.putExtra("id_attribuer",id_attribuer);
                                intent.putExtra("lat_destination",lat_destination);
                                intent.putExtra("lng_destination",lng_destination);
                                intent.putExtra("heur_liberer",heur_liberer);
                                intent.putExtra("marque",marque);
                                intent.putExtra("modele",modele);
                                intent.putExtra("couleur",couleur);
                                intent.putExtra("matricule",matricule);
                                intent.putExtra("nom_user",nom_user);
                                sendLocationBroadcast(intent);
                            }else{
                                NewMessageNotification notif =  new NewMessageNotification();
                                notif.notify4(getApplicationContext(),
                                        "Gari",id_attribuer,"une place a été trouver, voulez-vous prendre cette place ?",
                                        id_attribuer,lat,lng,nb_point,lat_destination,lng_destination,heur_liberer,
                                        marque,modele,couleur,matricule
                                ,nom_user);
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
            Intent intent = new Intent("trouverService");
            intent.putExtra("timelimit",true);
            sendLocationBroadcast(intent);

        }else{
            NewMessageNotification notif =  new NewMessageNotification();
            notif.notify5(getApplicationContext(),"Gari",id_trouver,"Aucun automobiliste trouver",true,lat,lng,nb_point);
        }
    }
    private void sendLocationBroadcast(Intent intent){
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}