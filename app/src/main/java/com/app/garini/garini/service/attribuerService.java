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

import com.app.garini.garini.MainActivity;
import com.app.garini.garini.MapActivity;
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

public class attribuerService extends Service {
    private static final String LOG_TAG = "DonnerService";
    public static boolean IS_SERVICE_RUNNING = false;
    TimerTask task;
    int attendu = 0;
    int id_attribuer = 0;
    private String URL = StaticValue.URL;
    double lat,lng;
    int nb_point;
    UserSessionManager pref;


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IS_SERVICE_RUNNING = true;
        pref = new UserSessionManager(this);
        if(intent!=null && intent.getExtras()!=null){
            Bundle inBundle = intent.getExtras();
            if(inBundle != null) {
                lat = inBundle.getDouble("lat");
                lng = inBundle.getDouble("lng");
                nb_point = inBundle.getInt("nb_point");
                id_attribuer = inBundle.getInt("id_attribuer");
                setRepeatingAsyncTask(id_attribuer);

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
        //Toast.makeText(this, "Service Detroyed!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }


    class AfterAttribuerTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            String json_string = null;
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new FormBody.Builder()
                        .add("id_attribuer",params[0])
                        .build();
                Request request = new Request.Builder()
                        .url(URL+"attendu.php")
                        .post(body)
                        .build();
                response = client.newCall(request).execute();
                json_string = response.body().string();

            } catch (@NonNull IOException e) {
                Log.e("Json ErrorMarque", "" + e.getLocalizedMessage());
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
                    if(!error){
                            attendu = jsonObject.getInt("attendu");
                            if(attendu != 0){
                                pref.deleteDonner();
                                stopSelf();
                                task.cancel();
                                if(MapActivity.active){
                                    //Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                                    Intent intent = new Intent("attribuerService");
                                    intent.putExtra("attendu",attendu);
                                    sendLocationBroadcast(intent);

                                }else{
                                    String message= null;
                                    if(attendu==1){
                                        message = "L'automobiliste est arrivé,Merci de l'avoir attendu !";
                                    }
                                    if(attendu==2){
                                        message = "Demande de place annuler !";
                                    }
                                    if(attendu==3){
                                        message = "L'automobiliste est arrivé mais vous ne l'avez pas attendu !";
                                    }
                                    if(attendu==4){
                                        message = "L'automobiliste a signaler son départ";
                                    }
                                    NewMessageNotification notif =  new NewMessageNotification();
                                    notif.notify2(getApplicationContext(),"Gari",id_attribuer,message,attendu);
                                }
                            }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setRepeatingAsyncTask(final int id_attribuer) {

        final Handler handler = new Handler();
        Timer timer = new Timer();

        task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            new AfterAttribuerTask().execute(id_attribuer+"");
                        } catch (Exception e) {
                            // error, do something
                        }
                    }
                });
            }
        };

        timer.schedule(task, 0, 6*1000);  // interval of one minute

    }

    private void sendLocationBroadcast(Intent intent){
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}