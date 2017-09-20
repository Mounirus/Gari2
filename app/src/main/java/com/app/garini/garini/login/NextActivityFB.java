package com.app.garini.garini.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.garini.garini.MainActivity;
import com.app.garini.garini.R;
import com.app.garini.garini.utile.ColorAdapterSpinner;
import com.app.garini.garini.utile.ColorItem;
import com.app.garini.garini.utile.JSONParserRegister;
import com.app.garini.garini.utile.Marque;
import com.app.garini.garini.utile.MarqueAutoCompleteAdapter;
import com.app.garini.garini.utile.Modele;
import com.app.garini.garini.utile.ModeleAutoCompleteAdapter;
import com.app.garini.garini.utile.StaticValue;
import com.app.garini.garini.utile.TypeAdapterSpinner;
import com.app.garini.garini.utile.Types;
import com.app.garini.garini.utile.UserSessionManager;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NextActivityFB extends AppCompatActivity {

    private Boolean exit = false;
    private ProgressDialog dialog;
    ImageView myImage;
    private AutoCompleteTextView txtMobile, txtMarque, txtModele, txtMatricule;
    private Button btnOk;
    private ImageButton infoMobile;
    private String name, imageUrl,token_firebase,fb_id;
    private AccessToken token_fb;
    private int id_marque;
    private MarqueAutoCompleteAdapter marqueAutoCompleteAdapter;
    private ArrayList<Marque> liste = new ArrayList<>();
    private ModeleAutoCompleteAdapter modeleAutoCompleteAdapter;
    private ArrayList<Modele> listeModele = new ArrayList<>();
    private ArrayList<ColorItem> listeColor = new ArrayList<>();
    private ArrayList<Types> listeTypes = new ArrayList<>();
    ColorAdapterSpinner spinnerColorAdpater;
    TypeAdapterSpinner typeAdapterSpinner;
    private String URL = StaticValue.URL;
    private AppCompatSpinner color,typeSpinner;
    String mobile;
    String marque;
    String modele;
    String matricule;
    String couleur ="";
    String type;
    String email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        Fresco.initialize(this);
        setContentView(R.layout.activity_next);

        color = (AppCompatSpinner) findViewById(R.id.color);
        color.setSupportBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blanc));
        listeColor.add(new ColorItem(0,""));
        listeColor.add(new ColorItem(Color.BLACK,"Noir"));
        listeColor.add(new ColorItem(Color.RED,"Rouge"));
        listeColor.add(new ColorItem(Color.BLUE,"Bleu"));
        listeColor.add(new ColorItem(Color.GREEN,"Vert"));
        listeColor.add(new ColorItem(Color.GRAY,"Gris"));
        listeColor.add(new ColorItem(Color.WHITE,"Blanc"));
        listeColor.add(new ColorItem(Color.YELLOW,"Jaune"));
        spinnerColorAdpater = new ColorAdapterSpinner(this,R.layout.activity_main,R.id.colortxt,listeColor);
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

        typeSpinner = (AppCompatSpinner) findViewById(R.id.type);
        typeSpinner.setSupportBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blanc));
        listeTypes.add(new Types("Type de véhicule"));
        listeTypes.add(new Types("Citadine ( Ex: Polo, Ibiza, Fabia...)"));
        listeTypes.add(new Types("Petite citadine ( Ex: i10, Maruti, twingo..)"));
        listeTypes.add(new Types("Berline et 4x4 ( Ex: Octavia, Passat, Evoque...)"));

        typeAdapterSpinner = new TypeAdapterSpinner(this,R.layout.activity_main,R.id.typetxt,listeTypes);
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


        infoMobile = (ImageButton) findViewById(R.id.infomobile);
        infoMobile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LovelyInfoDialog(NextActivityFB.this)
                        .setTopColorRes(R.color.colorPrimary)
                        .setIcon(R.drawable.ic_phone_android_white_24dp)
                        //This will add Don't show again checkbox to the dialog. You can pass any ID as argument
                        .setTitle("Information")
                        .setMessage("Info mobile")
                        .show();
            }
        });

        txtMobile = (AutoCompleteTextView) findViewById(R.id.txtmobile);

        txtMarque = (AutoCompleteTextView) findViewById(R.id.txtmarque);
        txtModele = (AutoCompleteTextView) findViewById(R.id.txtmodele);
        txtMatricule = (AutoCompleteTextView) findViewById(R.id.txtmatricule);
        new GetMarqueTask().execute();
        txtMarque.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Marque item = (Marque) parent.getItemAtPosition(position);
                id_marque = item.getId_marque();
                new GetModeleTask().execute(id_marque+"");
            }
        });

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.parentLayout);

        GradientDrawable gradientDrawableBlue = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#183152"), Color.parseColor("#375D81"), Color.parseColor("#ABC8E2")}); // Gradient Color Codes

        gradientDrawableBlue.setGradientType(GradientDrawable.LINEAR_GRADIENT); // Gradient Type
        coordinatorLayout.setBackgroundDrawable(gradientDrawableBlue);

        TextView nameView = (TextView) findViewById(R.id.nameAndSurname);


        Bundle inBundle = getIntent().getExtras();
        UserSessionManager sessionManager = new UserSessionManager(getApplicationContext());
        token_firebase = sessionManager.getFirebaseToken();
        if(token_firebase==null){
            token_firebase = "";
        }

        if (inBundle != null) {
            name = inBundle.getString("name")+" "+inBundle.getString("surname");
            token_fb = (AccessToken) inBundle.get("accessToken");
            imageUrl = inBundle.getString("imageUrl");
            fb_id = inBundle.getString("fb_id");
            nameView.setText(name);

            if (imageUrl != null) {
                myImage = (ImageView) findViewById(R.id.profileImage);

                ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
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
                        .displayer(new RoundedBitmapDisplayer(1000))
                        .build();

                imageLoader.displayImage(imageUrl, myImage, options, new ImageLoadingListener() {

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        //Toast.makeText(getApplicationContext(), "Loading Started", Toast.LENGTH_SHORT).show();
                        dialog = new ProgressDialog(view.getContext());
                        dialog.setMessage("Chargement...");
                        dialog.show();
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        Toast.makeText(getApplicationContext(), "Loading Failed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        Toast.makeText(getApplicationContext(), "Loading Cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                imageUrl = "";
            }

        }

        btnOk = (Button) findViewById(R.id.btnOk);
        GradientDrawable gradientDrawableGreen = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#2E7D32"), Color.parseColor("#4CAF50"), Color.parseColor("#81C784")}); // Gradient Color Codes

        gradientDrawableGreen.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        btnOk.setBackgroundDrawable(gradientDrawableGreen);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mobile = txtMobile.getText().toString();
                marque = txtMarque.getText().toString();
                modele = txtModele.getText().toString();
                matricule = txtMatricule.getText().toString();

                if(mobile.length()!=0 && mobile.length()<10){
                    txtMobile.setError(getResources().getString(R.string.err_mobile));
                }else if(type.equals("Type de véhicule") | type.equals("")){
                    View view = color.getSelectedView();
                    spinnerColorAdpater.setError(view, getResources().getString(R.string.err_type));
                }
                else if(marque.trim().equals(""))
                    txtMarque.setError(getResources().getString(R.string.err_marque));
                else if (modele.trim().equals(""))
                    txtModele.setError(getResources().getString(R.string.err_modele));
                else if (couleur.trim().equals("")) {
                    View view = color.getSelectedView();
                    spinnerColorAdpater.setError(view, getResources().getString(R.string.err_couleur));
                }
                else if (matricule.trim().equals(""))
                    txtMatricule.setError(getResources().getString(R.string.err_matricule));
                else {
                    dialog = new ProgressDialog(NextActivityFB.this);
                    dialog.setTitle("Patientez un instant SVP...");
                    dialog.setMessage("traitement en cours ");
                    dialog.show();
                    getEmail(token_fb, new GraphRequest.Callback() {
                                @Override
                                public void onCompleted(GraphResponse response) {
                                    if (response.getError() != null) {
                                        Log.e("error email FB :", response.getError().toString());
                                    } else {
                                        JSONObject obj = response.getJSONObject();
                                        email = obj.optString("email");

                                        JSONArray posts = null;
                                        JSONArray likes = new JSONArray();
                                        try {
                                            posts = obj.getJSONObject("likes").optJSONArray("data");
                                            for (int i = 0; i < posts.length(); i++) {

                                                JSONObject post = posts.optJSONObject(i);
                                                String id = post.optString("id");
                                                String category = post.optString("category");
                                                String name = post.optString("name");
                                                int count = post.optInt("likes");

                                                JSONObject like = new JSONObject();
                                                like.put("id",id);
                                                like.put("name",name);
                                                like.put("category",category);
                                                like.put("count",count);
                                                like.put("fb_id",fb_id);
                                                likes.put(like);
                                            }

                                            Log.e("Log likes",likes.toString());
                                            new SendLikes().execute(likes.toString());

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        new RegisterTask().execute(name, email, mobile, marque, modele, couleur, matricule, token_firebase, "oui",fb_id,imageUrl,type);

                                    }
                                }

                            }
                    );
                }
            }
        });


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
                        liste.add(marque);
                        count++;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                marqueAutoCompleteAdapter = new MarqueAutoCompleteAdapter(getApplicationContext(), R.layout.activity_main, R.id.name_marque, liste);
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

                Log.e("Json String modele :", "" + json_string);


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
                        modeleAutoCompleteAdapter = new ModeleAutoCompleteAdapter(getApplicationContext(), R.layout.activity_main, R.id.name_marque, listeModele);
                        txtModele.setAdapter(modeleAutoCompleteAdapter);
                        txtModele.setThreshold(1);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

        }
    }

    public void getEmail(AccessToken accessToken , GraphRequest.Callback callback){
        GraphRequest requestt = new GraphRequest(accessToken, "/me/",
                null, HttpMethod.GET, callback);

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,email,likes{id,category,name,location,likes}");
        requestt.setParameters(parameters);
        requestt.executeAsync();
    }

    class RegisterTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            /**
             * Getting JSON Object from Web Using okHttp
             */
            JSONObject jsonObject = JSONParserRegister.registerFB(params[0],params[1],params[2],params[3],params[4],params[5],params[6],params[7],params[8],params[9],params[10],params[11]);
            int id = 0;
            boolean error = false;
            if(jsonObject != null){
                try {
                    id = jsonObject.getInt("user_id");
                    error = jsonObject.getBoolean("error");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(error == false){
                    UserSessionManager userSessionManager = new UserSessionManager(getApplicationContext());
                    userSessionManager.createUserLoginSession(id, name, params[1], imageUrl, true);
                }else{
                    id = 0;
                }

            }

            return id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Integer id) {
            super.onPostExecute(id);
            if(dialog.isShowing())
                dialog.dismiss();
            if(id==0){
                Toast.makeText(getApplicationContext(),"Enregistrement erreur !",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(),"Enregistrement reussi !",Toast.LENGTH_SHORT).show();
                Intent main = new Intent(NextActivityFB.this, MainActivity.class);
                startActivity(main);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }
    class SendLikes extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            String json_string = null;

            try {
                OkHttpClient client = new OkHttpClient();

                RequestBody body = new FormBody.Builder()
                        .add("likes",params[0])
                        .build();
                Request request = new Request.Builder()
                        .url(URL+"likes.php")
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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            JSONObject jsonObject = null;
            boolean error = false;

            if (s != null && !s.equals("null")) {

                try {
                    jsonObject = new JSONObject(s);
                    error = jsonObject.getBoolean("error");
                    Log.e("Test Json send",s);
                    if(!error){
                        //new RegisterTask().execute(name, email, mobile, marque, modele, couleur, matricule, token_firebase, "oui",fb_id,imageUrl,type);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                Log.e("Test Json send", "null");
            }

        }
    }
}
