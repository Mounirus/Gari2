package com.app.garini.garini;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.app.garini.garini.utile.StaticValue;
import com.app.garini.garini.utile.UserSessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ChoiseFragment extends Fragment {

    private View mView;
    FloatingActionButton btn_donner,btn_trouver;
    int id_user = 0,id_trouver = 0,attendu = 0;
    double lat,lng;
    boolean isAttendu = false;
    UserSessionManager pref;
    private String URL = StaticValue.URL;
    private int nb_point = 0;


    public ChoiseFragment() {
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
        id_trouver = getArguments().getInt("id_trouver");
        lat = getArguments().getDouble("lat");
        lng = getArguments().getDouble("lng");
        if (getArguments().containsKey("attendu")) {
            isAttendu = true;
            attendu = getArguments().getInt("attendu");
        }

        new GetPointTask().execute(id_user);

        pref = new UserSessionManager(getActivity().getApplicationContext());
        mView = inflater.inflate(R.layout.frag_choise, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        btn_donner = (FloatingActionButton) mView.findViewById(R.id.btn_donner);
        btn_donner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), MapActivity.class);
                intent.putExtra("attendu",attendu);
                intent.putExtra("isAttendu",isAttendu);
                intent.putExtra("id_user",id_user);
                intent.putExtra("id_trouver",id_trouver);
                intent.putExtra("lat",lat);
                intent.putExtra("lng",lng);
                intent.putExtra("nb_point",nb_point);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        btn_trouver = (FloatingActionButton) mView.findViewById(R.id.btn_trouver);
        btn_trouver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(nb_point>0){
                    Intent intent = new Intent(getContext(), MapActivityTrouver.class);
                    intent.putExtra("attendu",attendu);
                    intent.putExtra("isAttendu",isAttendu);
                    intent.putExtra("id_user",id_user);
                    intent.putExtra("id_trouver",id_trouver);
                    intent.putExtra("lat",lat);
                    intent.putExtra("lng",lng);
                    intent.putExtra("nb_point",nb_point);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }else {
                    Toast.makeText(getContext(),"Vous n'avez pas assez de point pour efféctué cette opération",Toast.LENGTH_LONG).show();
                }

            }
        });

    }



    class GetPointTask extends AsyncTask<Integer, Void, String> {
        ProgressDialog dialog;

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
                        .url(URL+"getpoint.php")
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
            dialog.setCancelable(false);
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

                        nb_point = jsonObject.getInt("nb_point");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            dialog.dismiss();

        }
    }



    @Override
    public void onResume() {
        super.onResume();
    }
}
