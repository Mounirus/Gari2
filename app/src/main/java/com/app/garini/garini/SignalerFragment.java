package com.app.garini.garini;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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

import com.app.garini.garini.login.User;
import com.app.garini.garini.utile.StaticValue;
import com.app.garini.garini.utile.UserSessionManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by m.lagha on 14/09/2017.
 */
public class SignalerFragment extends Fragment {

    private View mView;
    int id_user = 0;
    private ProgressDialog dialog;
    private String URL = StaticValue.URL;
    private AutoCompleteTextView txtTitre, txtCom;
    private Button valider;


    public SignalerFragment() {
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
        mView = inflater.inflate(R.layout.frag_signaler, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtTitre = (AutoCompleteTextView) mView.findViewById(R.id.txttitre);
        txtCom = (AutoCompleteTextView) mView.findViewById(R.id.txtcom);

        valider = (Button) mView.findViewById(R.id.btnValider);
        GradientDrawable gradientDrawableGreen = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#2E7D32"), Color.parseColor("#4CAF50"), Color.parseColor("#81C784")}); // Gradient Color Codes

        gradientDrawableGreen.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        valider.setBackgroundDrawable(gradientDrawableGreen);
        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String titre = txtTitre.getText().toString();
                String com = txtCom.getText().toString();
                if(titre.trim().equals(""))
                    txtTitre.setError("Veuillez entrer un titre");
                else if (com.trim().equals(""))
                    txtCom.setError("Veuillez décrire le problème !");
                else{
                    new SignalerTask().execute(id_user+"",titre,com);
                }
            }
        });

    }

    class SignalerTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            String json_string = null;

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new FormBody.Builder()
                        .add("id_user",params[0])
                        .add("titre",params[1])
                        .add("text",params[2])
                        .build();
                Request request = new Request.Builder()
                        .url(URL+"signaler.php")
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
                        new LovelyInfoDialog(getContext())
                                .setTopColorRes(R.color.colorPrimary)
                                .setIcon(R.drawable.ic_bug_report_black_24dp)
                                //This will add Don't show again checkbox to the dialog. You can pass any ID as argument
                                .setTitle("Information")
                                .setMessage("Votre problème a bien été signaler, Merci.")
                                .show();
                        txtCom.setText("");
                        txtTitre.setText("");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }

        }
    }

}

