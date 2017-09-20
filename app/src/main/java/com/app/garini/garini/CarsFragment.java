package com.app.garini.garini;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.app.garini.garini.utile.ColorAdapterSpinner;
import com.app.garini.garini.utile.ColorItem;
import com.app.garini.garini.utile.Marque;
import com.app.garini.garini.utile.MarqueAutoCompleteAdapter;
import com.app.garini.garini.utile.Modele;
import com.app.garini.garini.utile.ModeleAutoCompleteAdapter;
import com.app.garini.garini.utile.RvCarAdpater;
import com.app.garini.garini.utile.StaticValue;
import com.app.garini.garini.utile.TypeAdapterSpinner;
import com.app.garini.garini.utile.Types;
import com.app.garini.garini.utile.Voiture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class CarsFragment extends Fragment {

    private View mView;
    private int id_user;
    private int id_marque;
    private List<Voiture> voitures;
    private RecyclerView rvCars;
    FloatingActionButton fab;
    private AutoCompleteTextView txtMarque, txtModele, txtMatricule;
    private Button btnOk;
    TypeAdapterSpinner typeAdapterSpinner;
    private AppCompatSpinner typeSpinner;
    private ArrayList<Types> listeTypes = new ArrayList<>();
    private MarqueAutoCompleteAdapter marqueAutoCompleteAdapter;
    private ArrayList<Marque> liste = new ArrayList<>();
    private ModeleAutoCompleteAdapter modeleAutoCompleteAdapter;
    private ArrayList<Modele> listeModele = new ArrayList<>();
    AlertDialog addDialog;
    private String URL = StaticValue.URL;
    private ArrayList<ColorItem> listeColor = new ArrayList<>();
    private AppCompatSpinner color;
    ColorAdapterSpinner spinnerColorAdpater;
    String couleur ="";
    String type;

    public CarsFragment() {
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
        mView = inflater.inflate(R.layout.frag_cars, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvCars = (RecyclerView) mView.findViewById(R.id.rvCars);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rvCars.setLayoutManager(llm);
        rvCars.setHasFixedSize(true);

        fab = (FloatingActionButton) mView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
                View v = getActivity().getLayoutInflater().inflate(R.layout.addcar_dialog,null);

                txtMarque = (AutoCompleteTextView) v.findViewById(R.id.txtmarque);
                txtModele = (AutoCompleteTextView) v.findViewById(R.id.txtmodele);
                color = (AppCompatSpinner) v.findViewById(R.id.color);
                color.setSupportBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.blanc));
                listeColor.add(new ColorItem(0,""));
                listeColor.add(new ColorItem(Color.BLACK,"Noir"));
                listeColor.add(new ColorItem(Color.RED,"Rouge"));
                listeColor.add(new ColorItem(Color.BLUE,"Bleu"));
                listeColor.add(new ColorItem(Color.GREEN,"Vert"));
                listeColor.add(new ColorItem(Color.GRAY,"Gris"));
                listeColor.add(new ColorItem(Color.WHITE,"Blanc"));
                listeColor.add(new ColorItem(Color.YELLOW,"Jaune"));
                spinnerColorAdpater = new ColorAdapterSpinner(getActivity(),R.layout.activity_main,R.id.colortxt,listeColor);
                color.setAdapter(spinnerColorAdpater);

                color.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        couleur    = ((TextView) view.findViewById(R.id.colortxt)).getTag().toString();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
                txtMatricule = (AutoCompleteTextView) v.findViewById(R.id.txtmatricule);
                typeSpinner = (AppCompatSpinner) v.findViewById(R.id.type);
                typeSpinner.setSupportBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.blanc));
                listeTypes.add(new Types("Type de véhicule"));
                listeTypes.add(new Types("Citadine ( Ex: Polo, Ibiza, Fabia...)"));
                listeTypes.add(new Types("Petite citadine ( Ex: i10, Maruti, twingo..)"));
                listeTypes.add(new Types("Berline et 4x4 ( Ex: Octavia, Passat, Evoque...)"));

                typeAdapterSpinner = new TypeAdapterSpinner(getActivity(),R.layout.activity_main,R.id.typetxt,listeTypes);
                typeSpinner.setAdapter(typeAdapterSpinner);

                typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        type    = ((TextView) view.findViewById(R.id.typetxt)).getText().toString();
                        TextView selectedText = (TextView) view.findViewById(R.id.typetxt);
                        if (selectedText != null) {
                            selectedText.setTextColor(Color.parseColor("#ffffff"));
                        }
                        if (type.equals("Petite citadine ( Ex: i10, Maruti, twingo..)")) {
                            type = "Petite citadine";
                        }
                        if (type.equals("Citadine ( Ex: Polo, Ibiza, Fabia...)")) {
                            type = "Citadine";
                        }
                        if (type.equals("Berline et 4x4 ( Ex: Octavia, Passat, Evoque...)")) {
                            type = "Berline et 4x4";
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });


                new GetMarqueTask().execute();
                txtMarque.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Marque item = (Marque) parent.getItemAtPosition(position);
                        id_marque = item.getId_marque();
                        new GetModeleTask().execute(id_marque+"");
                    }
                });
                btnOk = (Button) v.findViewById(R.id.btnOk);
                btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String marque = txtMarque.getText().toString();
                        String modele = txtModele.getText().toString();
                        String matricule = txtMatricule.getText().toString();

                        if(marque.trim().equals(""))
                            txtMarque.setError(getResources().getString(R.string.err_marque));
                        else if (modele.trim().equals(""))
                            txtModele.setError(getResources().getString(R.string.err_modele));
                        else if (couleur.trim().equals("")) {
                            View viewSpinnerColor = color.getSelectedView();
                            spinnerColorAdpater.setError(viewSpinnerColor, getResources().getString(R.string.err_couleur));
                        }
                        else if (matricule.trim().equals(""))
                            txtMatricule.setError(getResources().getString(R.string.err_matricule));
                        else if(type.equals("Type de véhicule") | type.equals("")){
                            View v = color.getSelectedView();
                            spinnerColorAdpater.setError(v, getResources().getString(R.string.err_type));
                        }
                        else {
                            new AddCarTask().execute(marque,modele,couleur,matricule,id_user+"",type);
                        }
                    }
                });

                mBuilder.setView(v);

                addDialog = mBuilder.create();
                addDialog.show();

            }
        });
    }

    private void initData(){
        voitures = new ArrayList<>();
        new GetCarsTask().execute(id_user);
    }

    private void initAdapter(){
        RvCarAdpater adpater = new RvCarAdpater(voitures,getContext());
        rvCars.setAdapter(adpater);
    }

    class GetCarsTask extends AsyncTask<Integer, Void, String> {

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
                        .url(URL+"getCars.php")
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
            if (s != null && !s.equals("null")) {
                try {
                    jsonObject = new JSONObject(s);
                    JSONArray jsonArray = jsonObject.getJSONArray("voitures");
                    int count = 0;
                    String modele,marque,couleur;
                    int id_voiture,selected;
                    if(jsonArray != null){
                        while (count < jsonArray.length()) {
                            JSONObject jo = jsonArray.getJSONObject(count);
                            modele = jo.getString("modele");
                            marque = jo.getString("marque");
                            couleur = jo.getString("couleur");
                            id_voiture = jo.getInt("id");
                            selected = jo.getInt("selected");
                            Voiture voiture = new Voiture(marque,modele,couleur,selected,id_voiture);
                            voitures.add(voiture);
                            count++;
                        }

                        initAdapter();
                        if(voitures.size()>=4){
                            fab.setVisibility(View.GONE);
                        }
                        dialog.dismiss();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class GetMarqueTask extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... params) {
            Response response = null;
            String json_string = null;
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(URL+"getVoitures.php")
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
            JSONObject jsonObject = null;
            if (s != null) {
                try {
                    jsonObject = new JSONObject(s);
                    JSONArray jsonArray = jsonObject.getJSONArray("marques");
                    int count = 0;
                    String marque_name;
                    int id_marque;
                    while (count < jsonArray.length()) {
                        JSONObject jo = jsonArray.getJSONObject(count);
                        marque_name = jo.getString("marque");
                        id_marque = jo.getInt("id");
                        Marque marque = new Marque(id_marque, marque_name);
                        Log.e("Marque detail :", id_marque + " " + marque_name);
                        liste.add(marque);
                        count++;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                marqueAutoCompleteAdapter = new MarqueAutoCompleteAdapter(getActivity(), R.layout.activity_main, R.id.name_marque, liste);
                txtMarque.setAdapter(marqueAutoCompleteAdapter);
                txtMarque.setThreshold(1);

                txtMarque.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        boolean find = false;
                        if (!hasFocus) {
                            Iterator<Marque> it = liste.iterator();
                            while (it.hasNext()) {
                                Marque s = it.next();
                                String name = s.getName_marque();
                                if (txtMarque.getText().toString().equals(name)) {
                                    find = true;
                                    break;
                                }
                            }
                            if (find == false) {
                                txtMarque.setText(""); // clear your TextView
                                txtMarque.setError("Cette marque de voiture n'existe pas !");
                            }
                        }
                    }
                });
            }

        }
    }

    class GetModeleTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            String json_string = null;
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new FormBody.Builder()
                        .add("id_marque",params[0])
                        .build();
                Request request = new Request.Builder()
                        .url(URL+"getModele.php")
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
            JSONObject jsonObject = null;
            if (s != null && !s.equals("null")) {
                try {
                    jsonObject = new JSONObject(s);
                    JSONArray jsonArray = jsonObject.getJSONArray("modeles");
                    int count = 0;
                    String modele_name;
                    if(jsonArray != null){
                        while (count < jsonArray.length()) {
                            JSONObject jo = jsonArray.getJSONObject(count);
                            modele_name = jo.getString("modele");
                            Modele modele = new Modele(modele_name);
                            listeModele.add(modele);
                            count++;
                        }
                        modeleAutoCompleteAdapter = new ModeleAutoCompleteAdapter(getActivity(), R.layout.activity_main, R.id.name_marque, listeModele);
                        txtModele.setAdapter(modeleAutoCompleteAdapter);
                        txtModele.setThreshold(1);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    class AddCarTask extends AsyncTask<String, Void, String> {

        ProgressDialog dialog;
        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            String json_string = null;
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new FormBody.Builder()
                        .add("marque",params[0])
                        .add("modele",params[1])
                        .add("couleur",params[2])
                        .add("matricule",params[3])
                        .add("id_user",params[4])
                        .add("type",params[5])
                        .build();
                Request request = new Request.Builder()
                        .url(URL+"addCar.php")
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
            if (s != null && !s.equals("null")) {
                try {
                    jsonObject = new JSONObject(s);
                    boolean error = jsonObject.getBoolean("error");
                    if (!error) {
                        initData();
                        addDialog.dismiss();
                    }else{
                        Toast.makeText(getContext(),"Erreur d'ajout,veuillez recommencer,Merci",Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();

                } catch(JSONException e){
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }
}
