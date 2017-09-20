package com.app.garini.garini.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.app.garini.garini.MainActivity;
import com.app.garini.garini.R;
import com.app.garini.garini.utile.StaticValue;
import com.app.garini.garini.utile.UserSessionManager;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class LoginActivity extends AppCompatActivity {


    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;
    private Profile mprofile;
    private String test = null;
    AccessToken tokenFb;
    private String URL = StaticValue.URL;


    //Facebook login button
    private FacebookCallback<LoginResult> callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            test = extras.getString("test");
        }

        callbackManager = CallbackManager.Factory.create();


        setContentView(R.layout.activity_login);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.parentLayout);

        GradientDrawable gradientDrawableBlue = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#183152"), Color.parseColor("#375D81"), Color.parseColor("#ABC8E2")}); // Gradient Color Codes

        gradientDrawableBlue.setGradientType(GradientDrawable.LINEAR_GRADIENT); // Gradient Type
        linearLayout.setBackgroundDrawable(gradientDrawableBlue);


        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                tokenFb = currentAccessToken;
            }
        };
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                nextActivity(currentProfile,tokenFb);

            }
        };
        accessTokenTracker.startTracking();
        profileTracker.startTracking();

        LoginButton loginButton = (LoginButton)findViewById(R.id.login_button);

        callback = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                tokenFb = loginResult.getAccessToken();
                mprofile = Profile.getCurrentProfile();
                nextActivity(mprofile,tokenFb);

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        };
        loginButton.setReadPermissions("user_friends,email,user_likes");

        loginButton.registerCallback(callbackManager, callback);


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void onStop() {
        super.onStop();
        //Facebook login
        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void nextActivity(Profile profile, final AccessToken token_fb){
        if(profile == null) {
            tokenFb = token_fb;
            profileTracker = new ProfileTracker() {
                @Override
                protected void onCurrentProfileChanged(Profile oldprofile, Profile newprofile) {
                    // profile2 is the new profile
                    if(newprofile!=null){
                        mprofile = newprofile;
                        new CheckUserFB().execute(newprofile.getId());

                    }
                    profileTracker.stopTracking();
                }
            };
        }
        else {
            profile = Profile.getCurrentProfile();
            mprofile = profile;
            new CheckUserFB().execute(profile.getId());
        }
    }

    class pushNotification extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            OkHttpClient client = new OkHttpClient();

            RequestBody body = new FormBody.Builder()
                    .add("token","test")
                    .build();
            Request request = new Request.Builder()
                    .url(params[0])
                    .post(body)
                    .build();

            try {
                client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }
    }
    class CheckUserFB extends AsyncTask<String, Void, String> {
        ProgressDialog dialog;
        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            String json_string = null;
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new FormBody.Builder()
                        .add("login_fb","login")
                        .add("fb_id",params[0])
                        .build();
                Request request = new Request.Builder()
                        .url(URL+"login.php")
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
            dialog = new ProgressDialog(LoginActivity.this);
            dialog.setCancelable(false);
            dialog.setTitle("Patientez un instant SVP...");
            dialog.setMessage("traitement en cours ");
            dialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            JSONObject jsonObject = null;
            boolean error = false;
            int id;
            String email,image,nom;
            Log.e("Login",s);
            if (s != null && !s.equals("null")) {
                try {
                    jsonObject = new JSONObject(s);
                    error = jsonObject.getBoolean("error");
                    Log.e("fb id",mprofile.getId());
                    if(error){

                        Intent main = new Intent(LoginActivity.this, NextActivityFB.class);
                        main.putExtra("fb_id", mprofile.getId());
                        main.putExtra("name", mprofile.getFirstName());
                        main.putExtra("surname", mprofile.getLastName());
                        main.putExtra("imageUrl", mprofile.getProfilePictureUri(100,100).toString());
                        main.putExtra("accessToken", tokenFb);
                        startActivity(main);
                    }else{
                        id = jsonObject.getInt("id");
                        email = jsonObject.getString("email");
                        image = jsonObject.getString("image");
                        nom = jsonObject.getString("nom");
                        UserSessionManager userSessionManager = new UserSessionManager(getApplicationContext());
                        userSessionManager.createUserLoginSession(id, nom, email, image, true);
                        Intent main = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(main);
                    }
                    dialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public void onBackPressed() {
        //Takes user to home screen
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        //Kills all activities of app
        System.exit(0);
    }
}
