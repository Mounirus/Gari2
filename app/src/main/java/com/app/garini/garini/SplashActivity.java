package com.app.garini.garini;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.garini.garini.login.LoginActivity;
import com.app.garini.garini.service.DonnerService;
import com.app.garini.garini.utile.UserSessionManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    TextView connexion,logo;
    RelativeLayout relativeLayout;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "Splash";
    TimerTask task;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);

        relativeLayout = (RelativeLayout) findViewById(R.id.parent);
        GradientDrawable gradientDrawableBlue = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#183152"), Color.parseColor("#375D81"), Color.parseColor("#ABC8E2")}); // Gradient Color Codes

        gradientDrawableBlue.setGradientType(GradientDrawable.LINEAR_GRADIENT); // Gradient Type
        relativeLayout.setBackgroundDrawable(gradientDrawableBlue);
        connexion = (TextView) findViewById(R.id.connexion);

        logo = (TextView) findViewById(R.id.logo);
        Animation logoMoveAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce);
        logoMoveAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                setRepeatingAsyncTask();

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        logo.startAnimation(logoMoveAnimation);
    }

    private class backgroundTask extends AsyncTask{

        Intent intent;
        @Override
        protected Object doInBackground(Object[] params) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(checkPlayServices()){
                if (isDataConnectionAvailable(getApplicationContext())){
                    task.cancel();
                    UserSessionManager userSessionManager = new UserSessionManager(getApplicationContext());
                    if(userSessionManager.isUserLoggedIn()){
                        intent = new Intent(SplashActivity.this, MainActivity.class);
                    }else{
                        intent = new Intent(SplashActivity.this, LoginActivity.class);
                    }
                    connexion.setVisibility(View.GONE);
                    startActivity(intent);
                    finish();
                }else{
                    connexion.setVisibility(View.VISIBLE);
                    connexion.setText("Pas de connexion internet !");
                }
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    public static boolean isDataConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void setRepeatingAsyncTask() {

        final Handler handler = new Handler();
        Timer timer = new Timer();

        task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            new backgroundTask().execute();
                        } catch (Exception e) {
                            // error, do something
                        }
                    }
                });
            }
        };

        timer.schedule(task, 0, 6*1000);  // interval of one minute

    }

}
