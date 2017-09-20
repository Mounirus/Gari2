package com.app.garini.garini;


import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.garini.garini.CarsFragment;
import com.app.garini.garini.R;
import com.app.garini.garini.login.User;
import com.app.garini.garini.utile.StaticValue;
import com.app.garini.garini.utile.UserSessionManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ProfileFragment extends Fragment {

    private View mView;
    FloatingActionButton fab;
    int id_user = 0;
    String name,email,mobile;
    UserSessionManager userSessionManager;
    ImageView profileImage;
    TextView txnom,txemail,txmobile,txpoint;
    AutoCompleteTextView txtMobile;
    private ProgressDialog dialog;
    private String URL = StaticValue.URL;
    AlertDialog addDialog;


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        id_user = getArguments().getInt("id_user");
        userSessionManager = new UserSessionManager(getContext());
        mView = inflater.inflate(R.layout.frag_profile, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String imageUrl = null;
        User user = userSessionManager.getUserDetails();
        if (userSessionManager.isUserLoggedInFb()) {
            imageUrl = user.getImage();
        }
        name = user.getName();
        email = user.getEmail();

        txnom = (TextView) mView.findViewById(R.id.nom);
        txemail = (TextView) mView.findViewById(R.id.email);
        txmobile = (TextView) mView.findViewById(R.id.mobile);
        txpoint = (TextView) mView.findViewById(R.id.txpoint);

        txnom.setText(name);
        txemail.setText(email);

        new GetMobileTask().execute(id_user);

        if (imageUrl != null) {
            profileImage = (ImageView) mView.findViewById(R.id.profileImage);

            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getContext())
                    .build();

            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.init(config);

            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.ic_autorenew_black_24px) // resource or drawable
                    .showImageForEmptyUri(R.drawable.ic_menu_gallery) // resource or drawable
                    .showImageOnFail(R.drawable.ic_menu_camera) // resource or drawable
                    .delayBeforeLoading(1000)
                    .resetViewBeforeLoading(true)  // default
                    .cacheInMemory(true) // default => false
                    .cacheOnDisk(true) // default => false
                    .build();

            imageLoader.displayImage(imageUrl, profileImage, options, new ImageLoadingListener() {

                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    dialog = new ProgressDialog(getContext());
                    dialog.setTitle("Patientez un instant SVP...");
                    dialog.setMessage("traitement en cours ");
                    dialog.show();
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    Toast.makeText(getContext(), "Loading Failed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    Toast.makeText(getContext(), "Loading Cancelled", Toast.LENGTH_SHORT).show();
                }
            });
        }

        fab = (FloatingActionButton) mView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
                View v = getActivity().getLayoutInflater().inflate(R.layout.editmobile_dialog,null);

                txtMobile = (AutoCompleteTextView) v.findViewById(R.id.txtmobile);

                Button btnOk = (Button) v.findViewById(R.id.btnOk);
                btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mobile = txtMobile.getText().toString();

                        if(mobile.trim().equals(""))
                            txtMobile.setError(getResources().getString(R.string.err_mobile));
                        else {
                            new EditMobileTask().execute(id_user+"",mobile);
                        }
                    }
                });

                mBuilder.setView(v);

                addDialog = mBuilder.create();
                addDialog.show();
            }
        });



    }


    @Override
    public void onResume() {
        super.onResume();
    }

    class GetMobileTask extends AsyncTask<Integer, Void, String> {

        ProgressDialog diag;
        @Override
        protected String doInBackground(Integer... params) {
            Response response = null;
            String json_string = null;

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new FormBody.Builder()
                        .add("id_user",params[0]+"")
                        .build();
                Request request = new Request.Builder()
                        .url(URL+"getmobile.php")
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
            diag = new ProgressDialog(getContext());
            diag.setTitle("Patientez un instant SVP...");
            diag.setMessage("traitement en cours ");
            diag.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            JSONObject jsonObject = null;
            boolean error = false;

            if (s != null && !s.equals("null")) {
                Log.e("test",s);
                try {
                    jsonObject = new JSONObject(s);
                    error = jsonObject.getBoolean("error");
                    if(error){
                        Toast.makeText(getContext(),"Une erreur est survenue, veuillez recommencer",Toast.LENGTH_SHORT).show();
                    }else{

                        mobile = jsonObject.getString("mobile");
                        int nb_point = jsonObject.getInt("nb_point");
                        txmobile.setText("Mobile : "+mobile);
                        txpoint.setText("Nombre de points : "+nb_point);
                        diag.dismiss();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }
    }


    class EditMobileTask extends AsyncTask<String, Void, String> {

        ProgressDialog dialog;

        protected String doInBackground(String... params) {
            Response response = null;
            String json_string = null;

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new FormBody.Builder()
                        .add("id_user",params[0])
                        .add("mobile",params[1])
                        .build();
                Request request = new Request.Builder()
                        .url(URL+"editmobile.php")
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
            dialog = new ProgressDialog(getContext());
            dialog.setTitle("Patientez un instant SVP...");
            dialog.setMessage("traitement en cours ");
            dialog.show();

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            JSONObject jsonObject = null;
            boolean error = false;

            if (s != null && !s.equals("null")) {
                Log.e("test",s);
                try {
                    jsonObject = new JSONObject(s);
                    error = jsonObject.getBoolean("error");
                    if(error){
                        Toast.makeText(getContext(),"Une erreur est survenue, veuillez recommencer",Toast.LENGTH_SHORT).show();
                    }else{
                        txmobile.setText("Mobile : "+mobile);
                        addDialog.dismiss();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            dialog.dismiss();

        }
    }
}
