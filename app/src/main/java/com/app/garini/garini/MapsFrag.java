package com.app.garini.garini;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.app.garini.garini.service.CleanService;
import com.app.garini.garini.service.DonnerService;
import com.app.garini.garini.service.attribuerService;
import com.app.garini.garini.utile.DirectionsJSONParser;
import com.app.garini.garini.utile.PermissionUtils;
import com.app.garini.garini.utile.StaticValue;
import com.app.garini.garini.utile.UserSessionManager;
import com.braintreepayments.cardform.view.CardForm;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MapsFrag extends Fragment implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    PolylineOptions polylineOptions;
    LocationRequest mLocationRequest;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private boolean zoom = false;
    private boolean zone = false;
    private String URL = StaticValue.URL;

    Button donner, btn_trouver_actuel, btn_trouver_search_annuler, btn_donner_search_annuler, btn_donner_now, btn_attribuer_annuler, btn_attribuer_annuler2;
    ImageButton btn_donner, btn_donner_annuler;
    TextView point;
    LinearLayout btn_layout, donner_layout, trouver_search, donner_search, attiruber_layout, attiruber_layout2;
    TimePicker time;
    LatLng currentLatLng, destinationLatLng;
    int id_user;
    int id_donner = 0, id_trouver = 0, id_attribuer = 0, attendu = 0;
    String marque, modele, couleur, matricule, heur_liberer;
    boolean isAttendu = false, isFinished = false;
    UserSessionManager pref;
    TimerTask taskDonner, taskTrouver;
    ProgressDialog dialog_attribuer;
    LovelyInfoDialog dialog_donner;
    TextView txt_attribuer, txt_attribuer2;
    public static boolean now = false;
    String currentAdresse = null;

    public MapsFrag() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        id_user = getArguments().getInt("id_user");

        id_trouver = getArguments().getInt("id_trouver");
        if (getArguments().containsKey("attendu")) {
            isAttendu = true;
            attendu = getArguments().getInt("attendu");
        }
        pref = new UserSessionManager(getActivity().getApplicationContext());
        mView = inflater.inflate(R.layout.frag_map, container, false);
        return mView;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMapView = (MapView) mView.findViewById(R.id.map);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }

        GradientDrawable gradientDrawableYellow = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#E88801"), Color.parseColor("#FFC200")}); // Gradient Color Codes

        gradientDrawableYellow.setGradientType(GradientDrawable.LINEAR_GRADIENT); // Gradient Type
        gradientDrawableYellow.setCornerRadius(15);

        GradientDrawable gradientDrawableRed = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#820233"), Color.parseColor("#EF4339")}); // Gradient Color Codes

        gradientDrawableRed.setGradientType(GradientDrawable.LINEAR_GRADIENT); // Gradient Type
        gradientDrawableRed.setCornerRadius(15);

        //-------------- Get point --------------------------
        point = (TextView) mView.findViewById(R.id.point);
        point.setText(Html.fromHtml("Points récoltés :<br/>"));
        point.setBackgroundDrawable(gradientDrawableYellow);
        new GetPointTask().execute(id_user);
        //--------------Fin Get point --------------------------

        //----------------Donner ---------------------------
        donner = (Button) mView.findViewById(R.id.donner);
        GradientDrawable gradientDrawableGreen = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#2E7D32"), Color.parseColor("#4CAF50"), Color.parseColor("#81C784")}); // Gradient Color Codes

        gradientDrawableGreen.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gradientDrawableGreen.setCornerRadius(15);
        donner.setBackgroundDrawable(gradientDrawableGreen);

        donner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SplashActivity.isDataConnectionAvailable(getContext())) {
                    donner_layout.setVisibility(View.VISIBLE);
                    btn_layout.setVisibility(View.GONE);
                } else {
                    Toast.makeText(getContext(), "Activez votre connexion internet !", Toast.LENGTH_SHORT).show();
                }

            }
        });


        donner_layout = (LinearLayout) mView.findViewById(R.id.donner_layout);

        btn_layout = (LinearLayout) mView.findViewById(R.id.btn_layout);

        btn_donner = (ImageButton) mView.findViewById(R.id.btn_donner);
        GradientDrawable gradientDrawableGreen2 = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#2E7D32"), Color.parseColor("#4CAF50"), Color.parseColor("#81C784")}); // Gradient Color Codes

        gradientDrawableGreen2.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gradientDrawableGreen2.setCornerRadius(15);
        btn_donner.setBackgroundDrawable(gradientDrawableGreen2);
        btn_donner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SplashActivity.isDataConnectionAvailable(getContext())) {
                    if (currentLatLng != null && currentAdresse != null) {
                        now = false;
                        donner_layout.setVisibility(View.GONE);
                        btn_layout.setVisibility(View.GONE);
                        double currentLat = currentLatLng.latitude;
                        double currentLng = currentLatLng.longitude;

                        String heur = time.getCurrentHour() + ":" + time.getCurrentMinute() + ":00";

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String date = dateFormat.format(new Date()) + " " + heur;

                        new DonnerTask().execute(id_user + "", currentLat + "", currentLng + "", date, currentAdresse);
                    } else {
                        Toast.makeText(getContext(), "En attente de géolocalisation...", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Activez votre connexion internet !", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btn_donner_now = (Button) mView.findViewById(R.id.btn_donner_now);
        GradientDrawable gradientDrawableGreen3 = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#2E7D32"), Color.parseColor("#4CAF50"), Color.parseColor("#81C784")}); // Gradient Color Codes

        gradientDrawableGreen3.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gradientDrawableGreen3.setCornerRadius(15);
        btn_donner_now.setBackgroundDrawable(gradientDrawableGreen3);
        btn_donner_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SplashActivity.isDataConnectionAvailable(getContext())) {
                    if (currentLatLng != null && currentAdresse != null) {
                        now = true;
                        donner_layout.setVisibility(View.GONE);
                        btn_layout.setVisibility(View.GONE);
                        double currentLat = currentLatLng.latitude;
                        double currentLng = currentLatLng.longitude;

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String date = dateFormat.format(new Date());

                        new DonnerTask().execute(id_user + "", currentLat + "", currentLng + "", date, currentAdresse);
                        getActivity().startService(new Intent(getContext(), CleanService.class));
                    } else {
                        Toast.makeText(getContext(), "En attente de géolocalisation...", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Activez votre connexion internet !", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_donner_annuler = (ImageButton) mView.findViewById(R.id.btn_donner_annuler);
        btn_donner_annuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                donner_layout.setVisibility(View.GONE);
                btn_layout.setVisibility(View.VISIBLE);
            }
        });

        time = (TimePicker) mView.findViewById(R.id.time);
        time.setIs24HourView(true);
        final Calendar c = Calendar.getInstance();
        time.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
        time.setCurrentMinute(c.get(Calendar.MINUTE));

        donner_search = (LinearLayout) mView.findViewById(R.id.donner_search);

        btn_donner_search_annuler = (Button) mView.findViewById(R.id.btn_donner_search_annuler);
        GradientDrawable gradientDrawableRed3 = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#820233"), Color.parseColor("#EF4339")}); // Gradient Color Codes

        gradientDrawableRed3.setGradientType(GradientDrawable.LINEAR_GRADIENT); // Gradient Type
        gradientDrawableRed3.setCornerRadius(15);
        btn_donner_search_annuler.setBackgroundDrawable(gradientDrawableRed3);
        btn_donner_search_annuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (now) {
                    if (taskDonner != null) {
                        taskDonner.cancel();
                    }
                } else {
                    AlarmManager alarmMgr = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(getContext(), DonnerService.class);
                    PendingIntent pIntent = PendingIntent.getService(getContext(), 0, intent, 0);
                    alarmMgr.cancel(pIntent);
                }
                pref.deleteDonner();
                donner_search.setVisibility(View.GONE);
                btn_layout.setVisibility(View.VISIBLE);
                new AnnulerDonnerTask().execute();
            }
        });

        //----------------Fin Donner ---------------------------

        //----------------Trouver ---------------------------
        btn_trouver_actuel = (Button) mView.findViewById(R.id.btn_trouver_actuel);
        GradientDrawable gradientDrawableBlue3 = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#183152"), Color.parseColor("#375D81"), Color.parseColor("#ABC8E2")}); // Gradient Color Codes

        gradientDrawableBlue3.setGradientType(GradientDrawable.LINEAR_GRADIENT); // Gradient Type
        gradientDrawableBlue3.setCornerRadius(15);
        btn_trouver_actuel.setBackgroundDrawable(gradientDrawableBlue3);
        btn_trouver_actuel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SplashActivity.isDataConnectionAvailable(getContext())) {
                    if (currentLatLng != null && currentAdresse != null) {
                        btn_layout.setVisibility(View.GONE);
                        double currentLat = currentLatLng.latitude;
                        double currentLng = currentLatLng.longitude;

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String date = dateFormat.format(new Date());

                        new TrouverTask().execute(id_user + "", currentLat + "", currentLng + "", currentLat + "", currentLng + "", date, currentAdresse, currentAdresse);
                        getActivity().startService(new Intent(getContext(), CleanService.class));
                    } else {
                        Toast.makeText(getContext(), "En attente de géolocalisation...", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Activez votre connexion internet !", Toast.LENGTH_SHORT).show();
                }
            }
        });

        trouver_search = (LinearLayout) mView.findViewById(R.id.trouver_search);

        btn_trouver_search_annuler = (Button) mView.findViewById(R.id.btn_trouver_search_annuler);
        GradientDrawable gradientDrawableRed2 = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#820233"), Color.parseColor("#EF4339")}); // Gradient Color Codes

        gradientDrawableRed2.setGradientType(GradientDrawable.LINEAR_GRADIENT); // Gradient Type
        gradientDrawableRed2.setCornerRadius(15);
        btn_trouver_search_annuler.setBackgroundDrawable(gradientDrawableRed2);
        btn_trouver_search_annuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (taskTrouver != null) {
                    taskTrouver.cancel();
                }
                pref.deleteTrouver();
                trouver_search.setVisibility(View.GONE);
                btn_layout.setVisibility(View.VISIBLE);
                new AnnulerTrouverTask().execute();
            }
        });


        //----------------Fin Trouver ---------------------------

        //----------------Attribuer donner ---------------------------

        attiruber_layout = (LinearLayout) mView.findViewById(R.id.attiruber_layout);

        txt_attribuer = (TextView) mView.findViewById(R.id.txt_attribuer);

        btn_attribuer_annuler = (Button) mView.findViewById(R.id.btn_attribuer_annuler);
        GradientDrawable gradientDrawableRed4 = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#820233"), Color.parseColor("#EF4339")}); // Gradient Color Codes

        gradientDrawableRed4.setGradientType(GradientDrawable.LINEAR_GRADIENT); // Gradient Type
        gradientDrawableRed4.setCornerRadius(15);
        btn_attribuer_annuler.setBackgroundDrawable(gradientDrawableRed4);
        btn_attribuer_annuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().stopService(new Intent(getActivity(), attribuerService.class));
                new AnnulerAttribuerTask().execute();
                new AnnulerDonnerTask().execute();
                btn_layout.setVisibility(View.VISIBLE);
                attiruber_layout.setVisibility(View.GONE);
            }
        });
        //----------------Fin Attribuer donner ---------------------------

        //----------------Attribuer Trouver ---------------------------
        attiruber_layout2 = (LinearLayout) mView.findViewById(R.id.attiruber_layout2);


        txt_attribuer2 = (TextView) mView.findViewById(R.id.txt_attribuer2);

        btn_attribuer_annuler2 = (Button) mView.findViewById(R.id.btn_attribuer_annuler2);
        GradientDrawable gradientDrawableRed5 = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#820233"), Color.parseColor("#EF4339")}); // Gradient Color Codes

        gradientDrawableRed5.setGradientType(GradientDrawable.LINEAR_GRADIENT); // Gradient Type
        gradientDrawableRed5.setCornerRadius(15);
        btn_attribuer_annuler2.setBackgroundDrawable(gradientDrawableRed5);
        btn_attribuer_annuler2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AnnulerAttribuerTask().execute();
                new AnnulerTrouverTask().execute();
                btn_layout.setVisibility(View.VISIBLE);
                attiruber_layout2.setVisibility(View.GONE);
                mGoogleMap.clear();
                Location location = new Location("");
                location.setLatitude(currentLatLng.latitude);
                location.setLongitude(currentLatLng.longitude);
                onLocationChanged(location);
            }
        });


        //----------------Fin Attribuer Trouver ---------------------------

        //----------------Périmetre de la map ------------------------------
        polylineOptions = new PolylineOptions().add(StaticValue.point1)
                .add(StaticValue.point2).add(StaticValue.point3).add(StaticValue.point3)
                .add(StaticValue.point4).add(StaticValue.point5).add(StaticValue.point6)
                .add(StaticValue.point7).add(StaticValue.point8).add(StaticValue.point9)
                .add(StaticValue.point10).add(StaticValue.point11).add(StaticValue.point12)
                .add(StaticValue.point13).add(StaticValue.point1);

        //----------------Fin Périmetre de la map ------------------------------
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //mGoogleMap.setTrafficEnabled(true);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        enableMyLocation();

        LocationManager locManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        boolean network_enabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Location location;

        if (network_enabled) {

            if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if(location!=null){
                onLocationChanged(location);
            }
        }

        if(pref.isDonner()){
            btn_layout.setVisibility(View.GONE);
            donner_search.setVisibility(View.VISIBLE);
            id_donner = pref.getIdDonner();
            if(id_trouver != 0 ){
                new LovelyStandardDialog(getContext())
                        .setTopColorRes(R.color.colorPrimary)
                        .setButtonsColorRes(R.color.colorAccent)
                        .setIcon(R.drawable.ic_directions_car_white_24dp)
                        .setTitle("Donner votre place")
                        .setMessage("un automibiliste a été trouver, confirmez-vous la libération de votre place ?")
                        .setPositiveButton("Oui", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new AttribuerTask().execute(id_donner+"",id_trouver+"",id_attribuer+"");
                            }
                        })
                        .setNegativeButton("Non", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                donner_search.setVisibility(View.GONE);
                                btn_layout.setVisibility(View.VISIBLE);
                                new AnnulerDonnerTask().execute();
                            }
                        }).show();
            }else{
                if (pref.isAttribuer()){
                    id_attribuer = pref.getIdAttribuer();
                    if(isAttendu){
                        if(attendu==3){
                            donner_search.setVisibility(View.GONE);
                            attiruber_layout.setVisibility(View.GONE);
                            btn_layout.setVisibility(View.VISIBLE);
                            pref.deleteDonner();
                            pref.deleteAttribuer();
                            pref.setIdAttribuer(0);
                            pref.setIdDonner(0);
                            id_donner = 0;
                            id_attribuer = 0;
                            id_trouver = 0;
                            Toast.makeText(getContext(),"Vous n'avez pas attendu",Toast.LENGTH_LONG).show();
                            new LovelyInfoDialog(getContext())
                                    .setTopColorRes(R.color.colorPrimary)
                                    .setIcon(R.drawable.ic_phone_android_white_24dp)
                                    //This will add Don't show again checkbox to the dialog. You can pass any ID as argument
                                    .setTitle("Information")
                                    .setMessage("Vous n'avez pas attendu")
                                    .show();

                        }else if(attendu == 1){
                            donner_search.setVisibility(View.GONE);
                            attiruber_layout.setVisibility(View.GONE);
                            btn_layout.setVisibility(View.VISIBLE);
                            pref.deleteDonner();
                            pref.deleteAttribuer();
                            pref.setIdAttribuer(0);
                            pref.setIdDonner(0);
                            id_donner = 0;
                            id_attribuer = 0;
                            id_trouver = 0;
                            Toast.makeText(getContext(),"Merci d'avoir attendu",Toast.LENGTH_LONG).show();
                            new LovelyInfoDialog(getContext())
                                    .setTopColorRes(R.color.colorPrimary)
                                    .setIcon(R.drawable.ic_phone_android_white_24dp)
                                    //This will add Don't show again checkbox to the dialog. You can pass any ID as argument
                                    .setTitle("Information")
                                    .setMessage("Merci d'avoir attendu")
                                    .show();

                        }else if(attendu == 2){
                            donner_search.setVisibility(View.GONE);
                            attiruber_layout.setVisibility(View.GONE);
                            btn_layout.setVisibility(View.VISIBLE);
                            pref.deleteAttribuer();
                            id_attribuer = 0;
                            id_trouver = 0;

                            new LovelyStandardDialog(getContext())
                                    .setTopColorRes(R.color.colorPrimary)
                                    .setButtonsColorRes(R.color.colorAccent)
                                    .setIcon(R.drawable.ic_directions_car_white_24dp)
                                    .setTitle("Donner votre place")
                                    .setMessage("L'automobiliste a annuler sa demande , voulez attendre une autre demande ?")
                                    .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            id_donner = pref.getIdDonner();
                                            setRepeatingAsyncTask(id_donner);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            new AnnulerDonnerTask().execute();
                                            donner_search.setVisibility(View.GONE);
                                            btn_layout.setVisibility(View.VISIBLE);
                                            pref.deleteDonner();
                                            pref.setIdAttribuer(0);
                                            pref.setIdDonner(0);
                                        }
                                    }).show();
                        }
                    }else{
                        new AttribuerTask().execute(id_donner+"",id_trouver+"",id_attribuer+"");
                    }
                }else{
                    if(DonnerService.IS_SERVICE_RUNNING == false){
                        setRepeatingAsyncTask(id_donner);
                    }
                }
            }
        }else{
            if(pref.getIdDonner()!=0){
                id_donner = pref.getIdDonner();
                new AnnulerDonnerTask().execute();
                if(pref.getIdAttribuer()!=0){
                    id_attribuer = pref.getIdAttribuer();
                    new AnnulerAttribuerTask().execute();
                }
            }
            btn_layout.setVisibility(View.VISIBLE);
            donner_search.setVisibility(View.GONE);
        }
        if(!pref.isTrouver()){
            if(pref.getIdTrouver()!=0){
                id_trouver = pref.getIdTrouver();
                new AnnulerTrouverTask().execute();
                if(pref.getIdAttribuer()!=0){
                    id_attribuer = pref.getIdAttribuer();
                    new AnnulerAttribuerTask().execute();
                }
            }
        }


    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getActivity().getSupportFragmentManager(), "dialog");
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mGoogleMap != null) {
            // Access to the location has been granted to the app.
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);

        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(10);
        if (ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void updateCameraBearing(GoogleMap googleMap, float bearing) {
        if ( googleMap == null) return;
        CameraPosition camPos = CameraPosition
                .builder(
                        googleMap.getCameraPosition() // current Camera
                )
                .bearing(bearing)
                //.zoom(15)
                //.tilt(90)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
    }
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        //Place current location marker
        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title("Vous êtes ici");
        //markerOptions.snippet("Vous êtes ici");

        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
        mCurrLocationMarker.showInfoWindow();

        /*if (currentLatLng != null){
            boolean contain = PolyUtil.containsLocation(currentLatLng, polylineOptions.getPoints(), true);
            if(!zone){
                if(!contain){
                    btn_trouver_actuel.setEnabled(false);
                    donner.setEnabled(false);
                    new LovelyInfoDialog(getContext())
                            .setTopColorRes(R.color.colorPrimary)
                            .setIcon(R.drawable.ic_phone_android_white_24dp)
                            //This will add Don't show again checkbox to the dialog. You can pass any ID as argument
                            .setTitle("Information")
                            .setMessage("Vous êtes hors de la zone de couverture , l'application ne fonctionne pas dans cette zone")
                            .show();

                    zone = true;
                }else{
                    btn_trouver_actuel.setEnabled(true);
                    donner.setEnabled(true);
                    zone = false;
                }
            }

        }*/


        //move map camera


        //updateCameraBearing(mGoogleMap, location.getBearing());
        if (!zoom) {
            new GetAddressTask().execute(location.getLatitude()+"",location.getLongitude()+"");
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(14));
            zoom = true;
        }
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                return false;
            }
        });
        if (!isFinished) {

            if (destinationLatLng != null) {
                final Location current = location;
                Log.e("Dest", destinationLatLng.toString());
                Location des = new Location("");
                des.setLatitude(destinationLatLng.latitude);
                des.setLongitude(destinationLatLng.longitude);

                float distanceBetween = current.distanceTo(des);
                Log.e("Distance :", distanceBetween + "");

                /*SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();

                try {
                    Date date_heur_liberer = dateFormat.parse(heur_liberer);
                    if(date.compareTo(date_heur_liberer)>=0){

                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }*/

                if (distanceBetween < 90) {
                    isFinished = true;
                    Toast.makeText(getContext(), "Vous ete arrivé !", Toast.LENGTH_LONG).show();
                    new LovelyStandardDialog(getContext())
                            .setTopColorRes(R.color.colorPrimary)
                            .setButtonsColorRes(R.color.colorAccent)
                            .setIcon(R.drawable.ic_directions_car_white_24dp)
                            .setTitle("Vous êtes arriver")
                            .setMessage("L'automobiliste vous a-t-il attendu ?")
                            .setPositiveButton("Oui", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mGoogleMap.clear();
                                    new finalTask().execute("oui", id_attribuer + "");
                                    destinationLatLng = null;
                                    pref.deleteTrouver();
                                    pref.deleteAttribuer();
                                    pref.setIdAttribuer(0);
                                    pref.setIdDonner(0);
                                    onLocationChanged(current);
                                    attiruber_layout2.setVisibility(View.GONE);
                                    btn_layout.setVisibility(View.VISIBLE);
                                }
                            })
                            .setNegativeButton("Non", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mGoogleMap.clear();
                                    new finalTask().execute("non", id_attribuer + "");
                                    destinationLatLng = null;
                                    pref.deleteTrouver();
                                    pref.deleteAttribuer();
                                    pref.setIdAttribuer(0);
                                    pref.setIdDonner(0);
                                    onLocationChanged(current);
                                    attiruber_layout2.setVisibility(View.GONE);
                                    btn_layout.setVisibility(View.VISIBLE);
                                }
                            }).show();

                }

            }
        }
        //stop location updates
        /*if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    //----------------Taches commune-----------------------------------

    class GetAddressTask extends AsyncTask<String, Void, String> {
        private static final String API_KEY = "AIzaSyDf3DA9XirVxkwtpSP0puWZgp0NnQUpa8E";
        ProgressDialog dialog;
        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            String json_string = null;

            try {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("https://maps.googleapis.com/maps/api/geocode/json?latlng="+params[0]+","+params[1]+"&sensor=true_or_false&language=fr&key="+ API_KEY)
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
            String status;

            if (s != null && !s.equals("null")) {
                try {
                    jsonObject = new JSONObject(s);
                    status = jsonObject.getString("status");
                    if(status.equals("OK")){

                        currentAdresse = jsonObject.getJSONArray("results").getJSONObject(0).getString("formatted_address");
                    }else{
                        Toast.makeText(getContext(),"Une erreur est survenue, veuillez recommencer",Toast.LENGTH_SHORT).show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(dialog.isShowing()){
                    dialog.dismiss();
                }

            }

        }
    }


    class GetPointTask extends AsyncTask<Integer, Void, String> {

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

                        int nb_point = jsonObject.getInt("nb_point");
                        point.setText(Html.fromHtml("Points récoltés :<br/>"+nb_point+" pts"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    public class AnnulerAttribuerTask extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... params) {
            if(id_attribuer != 0){
                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = new FormBody.Builder()
                            .add("id_attribuer",id_attribuer+"")
                            .build();
                    Request request = new Request.Builder()
                            .url(URL+"annuler_attribuer.php")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    response.body().string();

                } catch (@NonNull IOException e) {
                    Log.e("Json ErrorMarque", "" + e.getLocalizedMessage());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            id_attribuer = 0;
            pref.deleteAttribuer();
            if(attiruber_layout.getVisibility() == View.VISIBLE){
                attiruber_layout.setVisibility(View.GONE);
            }
            if(attiruber_layout2.getVisibility() == View.VISIBLE){
                attiruber_layout2.setVisibility(View.GONE);
            }
            pref.setIdAttribuer(0);
            btn_layout.setVisibility(View.VISIBLE);
        }
    }

    //----------------Fin Taches commune-------------------------------------

    //----------------------Donner----------------------------------------------

    private class GoogMatrixRequest extends AsyncTask<Double, Void, String>{

        private static final String API_KEY = "AIzaSyDf3DA9XirVxkwtpSP0puWZgp0NnQUpa8E";
        @Override
        protected String doInBackground(Double... params) {
            Response response = null;
            String json_string = null;
            String url_request = "https://maps.googleapis.com/maps/api/distancematrix/json?origins="+params[0]+","+params[1]+"&destinations="+params[2]+","+params[3]+"&language=fr-FR&key=" + API_KEY;
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url_request)
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
            JSONObject jsonRespRouteDistance = null,jsonRespRouteTime = null;
            if (s != null) {
                try {
                    jsonRespRouteDistance = new JSONObject(s)
                            .getJSONArray("rows")
                            .getJSONObject(0)
                            .getJSONArray ("elements")
                            .getJSONObject(0)
                            .getJSONObject("distance");
                    jsonRespRouteTime = new JSONObject(s)
                            .getJSONArray("rows")
                            .getJSONObject(0)
                            .getJSONArray ("elements")
                            .getJSONObject(0)
                            .getJSONObject("duration");

                    //String distance = jsonRespRouteDistance.get("text").toString();
                    //String duration = jsonRespRouteTime.get("text").toString();

                    int duration_second = (int) jsonRespRouteTime.get("value");

                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                    Date now = new Date();
                    long estimationMilli = now.getTime() + (duration_second*1000);
                    Date estimation = new Date(estimationMilli);
                    String heur_arriver = dateFormat.format(estimation);
                    dialog_attribuer.dismiss();
                    String html = "Votre place a été attribué a :<br/>" +
                            "<b>"+marque+" "+modele+" "+couleur+"</b><br/>" +
                            "Immatriculé : <b>"+matricule+"</b><br/>" +
                            "Heur d'arrivée estimée : <b>"+heur_arriver+"</b><br/>" +
                            "L'heur d'arrivée etant approximative, si l'attente vous semble trop longue, vous pouvez annuler l'opération en cliquant sur annuler<br/>";
                    txt_attribuer.setText(Html.fromHtml(html));
                    donner_search.setVisibility(View.GONE);
                    attiruber_layout.setVisibility(View.VISIBLE);
                    getActivity().startService(new Intent(getActivity(), attribuerService.class));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    class DonnerTask extends AsyncTask<String, Void, String> {

        ProgressDialog dialog;
        String currentLat;
        String currentLng;
        String heur;
        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            String json_string = null;
            currentLat = params[1];
            currentLng = params[2];
            heur =  params[3];
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateandTime = sdf.format(new Date());
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new FormBody.Builder()
                        .add("donner","ok")
                        .add("id_user",params[0])
                        .add("lat",params[1])
                        .add("lng",params[2])
                        .add("heur",params[3])
                        .add("adresse",params[4])
                        .add("date_heur",currentDateandTime)
                        .build();
                Request request = new Request.Builder()
                        .url(URL+"donner.php")
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
                Log.e("Donner",s);
                try {
                    jsonObject = new JSONObject(s);
                    error = jsonObject.getBoolean("error");
                    if(error){

                        Toast.makeText(getContext(),"Une erreur est survenue, veuillez recommencer",Toast.LENGTH_SHORT).show();
                    }else{
                        donner_search.setVisibility(View.VISIBLE);
                        btn_layout.setVisibility(View.GONE);
                        id_donner = jsonObject.getInt("id_donner");
                        pref.createDonner(id_donner);
                        if(now){
                            dialog_donner = new LovelyInfoDialog(getContext());
                            dialog_donner.setTopColorRes(R.color.colorPrimary)
                                    .setIcon(R.drawable.ic_directions_car_white_24dp)
                                    //This will add Don't show again checkbox to the dialog. You can pass any ID as argument
                                    .setTitle("Donner votre place")
                                    .setMessage("Nous allons vous mettre en relation avec un automobiliste qui cherche une place, temps d’attente 3mn.")
                                    .show();
                            setRepeatingAsyncTask(id_donner);
                        }else{
                            new LovelyInfoDialog(getContext())
                                    .setTopColorRes(R.color.colorPrimary)
                                    .setIcon(R.drawable.ic_directions_car_white_24dp)
                                    //This will add Don't show again checkbox to the dialog. You can pass any ID as argument
                                    .setTitle("Donner votre place")
                                    .setMessage("Votre place a été enregistrée,si un automobiliste est intérésser par votre place, " +
                                            "vous serrai notifié 15 minute avant l'heur prévu")
                                    .show();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            try {
                                Date date = sdf.parse(heur);
                                AlarmManager alarmMgr = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

                                Intent intent = new Intent(getContext(), DonnerService.class);
                                PendingIntent pIntent = PendingIntent.getService(getContext(), 0, intent, 0);

                                Calendar cal= Calendar.getInstance();
                                cal.setTime(date);
                                cal.add(Calendar.MINUTE, -15);
                                Toast.makeText(getContext(),sdf.format(cal.getTime()),Toast.LENGTH_SHORT).show();
                                alarmMgr.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pIntent);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(dialog.isShowing()){
                    dialog.dismiss();
                }

            }

        }
    }

    class AfterDonnerTask extends AsyncTask<String, Void, String> {

        int id_trouver;
        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            String json_string = null;
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new FormBody.Builder()
                        .add("id_donner",params[0])
                        .build();
                Request request = new Request.Builder()
                        .url(URL+"trouver_service.php")
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
                    boolean trouver = jsonObject.getBoolean("trouver");
                    if(trouver){
                            id_trouver = jsonObject.getInt("id");
                            new LovelyStandardDialog(getContext())
                                    .setTopColorRes(R.color.colorPrimary)
                                    .setButtonsColorRes(R.color.colorAccent)
                                    .setIcon(R.drawable.ic_directions_car_white_24dp)
                                    .setTitle("Donner votre place")
                                    .setMessage("un automibiliste a été trouver, confirmez-vous la libération de votre place ?")
                                    .setPositiveButton("Oui", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            new AttribuerTask().execute(id_donner+"",id_trouver+"",id_attribuer+"");
                                        }
                                    })
                                    .setNegativeButton("Non", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            new AnnulerDonnerTask().execute();
                                            donner_search.setVisibility(View.GONE);
                                            btn_layout.setVisibility(View.VISIBLE);

                                        }
                                    }).show();

                        taskDonner.cancel();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setRepeatingAsyncTask(final int id_donner) {

        final Handler handler = new Handler();
        final Timer timer = new Timer();

        taskDonner = new TimerTask() {
            long t0 = System.currentTimeMillis();
            @Override
            public void run() {
                if (System.currentTimeMillis() - t0 > 60 * 1000) {
                    cancel();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new LovelyStandardDialog(getContext())
                                    .setTopColorRes(R.color.colorPrimary)
                                    .setButtonsColorRes(R.color.colorAccent)
                                    .setIcon(R.drawable.ic_directions_car_white_24dp)
                                    .setTitle("Donner votre place")
                                    .setMessage("Aucun automobiliste trouver, voulez attendre encors ?")
                                    .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            setRepeatingAsyncTask(id_donner);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            new AnnulerDonnerTask().execute();
                                            donner_search.setVisibility(View.GONE);
                                            btn_layout.setVisibility(View.VISIBLE);
                                        }
                                    }).show();
                        }
                    });
                }else {
                    handler.post(new Runnable() {
                        public void run() {
                            try {
                                new AfterDonnerTask().execute(id_donner+"");
                            } catch (Exception e) {

                            }
                        }
                    });
                }
            }
        };
        timer.schedule(taskDonner, 0, 6*1000);  // interval of one minute

    }

    class AnnulerDonnerTask extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... params) {
            if(id_donner != 0){
                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = new FormBody.Builder()
                            .add("id_donner",id_donner+"")
                            .build();
                    Request request = new Request.Builder()
                            .url(URL+"annuler_donner.php")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    response.body().string();

                } catch (@NonNull IOException e) {
                    Log.e("Json ErrorMarque", "" + e.getLocalizedMessage());
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            id_donner = 0;
            pref.setIdDonner(0);
            pref.deleteDonner();
            donner_layout.setVisibility(View.GONE);
            btn_layout.setVisibility(View.VISIBLE);
        }
    }

    class AttribuerTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            String json_string = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateandTime = sdf.format(new Date());
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new FormBody.Builder()
                        .add("id_attribuer",params[2])
                        .add("id_donner",params[0])
                        .add("id_trouver",params[1])
                        .add("date_heur",currentDateandTime)
                        .build();
                Request request = new Request.Builder()
                        .url(URL+"attribuer.php")
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
            dialog_attribuer = new ProgressDialog(getContext());
            dialog_attribuer.setCancelable(false);
            dialog_attribuer.setTitle("Patientez un instant SVP...");
            dialog_attribuer.setMessage("traitement en cours ");
            dialog_attribuer.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            JSONObject jsonObject = null;
            boolean error = false;
            boolean annuler = false;

            if (s != null && !s.equals("null")) {
                try {
                    jsonObject = new JSONObject(s);
                    error = jsonObject.getBoolean("error");
                    annuler = jsonObject.getBoolean("annuler");
                    if(error){

                        Toast.makeText(getContext(),"Une erreur est survenue, veuillez recommencer",Toast.LENGTH_SHORT).show();
                        new AnnulerDonnerTask().execute(id_donner+"");
                        new AnnulerAttribuerTask().execute(id_attribuer+"");
                        dialog_attribuer.dismiss();
                    }else {
                        if (annuler){
                            dialog_attribuer.dismiss();
                            id_attribuer = jsonObject.getInt("id_attribuer");
                            new AnnulerAttribuerTask().execute(id_attribuer+"");
                            new LovelyStandardDialog(getContext())
                                    .setTopColorRes(R.color.colorPrimary)
                                    .setButtonsColorRes(R.color.colorAccent)
                                    .setIcon(R.drawable.ic_directions_car_white_24dp)
                                    .setTitle("Donner votre place")
                                    .setMessage("L'automobiliste a annuler sa demande , voulez attendre encors ?")
                                    .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            donner_search.setVisibility(View.VISIBLE);
                                            btn_layout.setVisibility(View.GONE);
                                            setRepeatingAsyncTask(id_donner);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            new AnnulerDonnerTask().execute();
                                            donner_search.setVisibility(View.GONE);
                                            btn_layout.setVisibility(View.VISIBLE);
                                        }
                                    }).show();
                        }else{
                            id_attribuer = jsonObject.getInt("id_attribuer");
                            marque = jsonObject.getString("marque");
                            modele = jsonObject.getString("modele");
                            couleur = jsonObject.getString("couleur");
                            matricule = jsonObject.getString("matricule");

                            double lat_d = jsonObject.getDouble("lat_d");
                            double lat_a = jsonObject.getDouble("lat_a");
                            double lng_a = jsonObject.getDouble("lng_a");
                            double lng_d = jsonObject.getDouble("lng_d");

                            if(dialog_donner != null){
                                dialog_donner.dismiss();
                            }
                            pref.createAttribuer(id_attribuer);
                            new GoogMatrixRequest().execute(lat_d,lng_d,lat_a,lng_a);
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    //-------------------Fin donner--------------------------------------------------

    //-------------------Touver------------------------------------------------------

    class TrouverTask extends AsyncTask<String, Void, String> {

        ProgressDialog dialog;
        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            String json_string = null;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateandTime = sdf.format(new Date());
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new FormBody.Builder()
                        .add("trouver","ok")
                        .add("id_user",params[0])
                        .add("lat_d",params[1])
                        .add("lng_d",params[2])
                        .add("lat_a",params[3])
                        .add("lng_a",params[4])
                        .add("adresse_a",params[5])
                        .add("adresse_d",params[6])
                        .add("date_heur",currentDateandTime)
                        .build();
                Request request = new Request.Builder()
                        .url(URL+"trouver.php")
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
                Log.e("Trouver",s);
                try {
                    jsonObject = new JSONObject(s);
                    error = jsonObject.getBoolean("error");
                    if(error){

                        Toast.makeText(getContext(),"Une erreur est survenue, veuillez recommencer",Toast.LENGTH_SHORT).show();
                    }else{
                        trouver_search.setVisibility(View.VISIBLE);
                        btn_layout.setVisibility(View.GONE);
                        id_trouver = jsonObject.getInt("id_trouver");
                        pref.createTrouver(id_trouver);
                        dialog_donner = new LovelyInfoDialog(getContext());
                        dialog_donner.setTopColorRes(R.color.colorPrimary)
                                    .setIcon(R.drawable.ic_directions_car_white_24dp)
                                    //This will add Don't show again checkbox to the dialog. You can pass any ID as argument
                                    .setTitle("Trouver une place")
                                    .setMessage("Nous allons trouver une place pour vous, temps d’attente 5mn.")
                                    .show();
                        setRepeatingAsyncTaskTrouver(id_trouver);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(dialog.isShowing()){
                    dialog.dismiss();
                }

            }

        }
    }

    private void setRepeatingAsyncTaskTrouver(final int id_trouver) {

        final Handler handler = new Handler();
        final Timer timer = new Timer();

        taskTrouver = new TimerTask() {
            long t0 = System.currentTimeMillis();
            @Override
            public void run() {
                if (System.currentTimeMillis() - t0 > 60 * 1000) {
                    cancel();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new LovelyStandardDialog(getContext())
                                    .setTopColorRes(R.color.colorPrimary)
                                    .setButtonsColorRes(R.color.colorAccent)
                                    .setIcon(R.drawable.ic_directions_car_white_24dp)
                                    .setTitle("Donner votre place")
                                    .setMessage("Aucune place trouver, voulez attendre encors ?")
                                    .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            setRepeatingAsyncTaskTrouver(id_trouver);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            new AnnulerTrouverTask().execute();
                                            trouver_search.setVisibility(View.GONE);
                                            btn_layout.setVisibility(View.VISIBLE);
                                        }
                                    }).show();
                        }
                    });
                }else {
                    handler.post(new Runnable() {
                        public void run() {
                            try {
                                new AfterTrouverTask().execute(id_trouver+"");
                            } catch (Exception e) {

                            }
                        }
                    });
                }
            }
        };
        timer.schedule(taskTrouver, 0, 6*1000);  // interval of one minute

    }

    class AfterTrouverTask extends AsyncTask<String, Void, String> {

        int id_donner;
        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            String json_string = null;
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new FormBody.Builder()
                        .add("id_trouver",params[0])
                        .build();
                Request request = new Request.Builder()
                        .url(URL+"donner_service.php")
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
            Log.e("Test",s);
            if (s != null && !s.equals("null")) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    boolean donner = jsonObject.getBoolean("donner");
                    if(donner){
                        id_donner = jsonObject.getInt("id");
                        new LovelyStandardDialog(getContext())
                                .setTopColorRes(R.color.colorPrimary)
                                .setButtonsColorRes(R.color.colorAccent)
                                .setIcon(R.drawable.ic_directions_car_white_24dp)
                                .setTitle("Trouver une place")
                                .setMessage("une place a été trouver, voulez-vous prendre cette place ?")
                                .setPositiveButton("Oui", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        new AttribuerTaskTrouver().execute(id_donner+"",id_trouver+"",id_attribuer+"");
                                    }
                                })
                                .setNegativeButton("Non", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        new AnnulerTrouverTask().execute();
                                        trouver_search.setVisibility(View.GONE);
                                        btn_layout.setVisibility(View.VISIBLE);

                                    }
                                }).show();

                        taskTrouver.cancel();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class AnnulerTrouverTask extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... params) {
            if(id_trouver != 0){
                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = new FormBody.Builder()
                            .add("id_trouver",id_trouver+"")
                            .build();
                    Request request = new Request.Builder()
                            .url(URL+"annuler_trouver.php")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    response.body().string();

                } catch (@NonNull IOException e) {
                    Log.e("Json ErrorMarque", "" + e.getLocalizedMessage());
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            id_trouver = 0;
            pref.deleteTrouver();
            pref.setIdTrouver(0);
            btn_layout.setVisibility(View.VISIBLE);
        }
    }

    class AttribuerTaskTrouver extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            String json_string = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateandTime = sdf.format(new Date());
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new FormBody.Builder()
                        .add("id_attribuer",params[2])
                        .add("id_donner",params[0])
                        .add("id_trouver",params[1])
                        .add("date_heur",currentDateandTime)
                        .build();
                Request request = new Request.Builder()
                        .url(URL+"attribuer_trouver.php")
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
            dialog_attribuer = new ProgressDialog(getContext());
            dialog_attribuer.setCancelable(false);
            dialog_attribuer.setTitle("Patientez un instant SVP...");
            dialog_attribuer.setMessage("traitement en cours ");
            dialog_attribuer.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            JSONObject jsonObject = null;
            boolean error = false;
            boolean annuler = false;

            if (s != null && !s.equals("null")) {
                Log.e("attribuer_trouver", s);
                try {
                    jsonObject = new JSONObject(s);
                    error = jsonObject.getBoolean("error");
                    annuler = jsonObject.getBoolean("annuler");
                    if(error){
                        Toast.makeText(getContext(),"Une erreur est survenue, veuillez recommencer",Toast.LENGTH_SHORT).show();
                        new AnnulerTrouverTask().execute(id_trouver+"");
                        new AnnulerAttribuerTask().execute(id_attribuer+"");
                        dialog_attribuer.dismiss();
                    }else {
                        id_attribuer = jsonObject.getInt("id_attribuer");
                        if(annuler){
                            dialog_attribuer.dismiss();
                            new AnnulerAttribuerTask().execute(id_attribuer+"");
                            new LovelyStandardDialog(getContext())
                                    .setTopColorRes(R.color.colorPrimary)
                                    .setButtonsColorRes(R.color.colorAccent)
                                    .setIcon(R.drawable.ic_directions_car_white_24dp)
                                    .setTitle("Trouver une place")
                                    .setMessage("La place a été supprimer , voulez rechercher une autre place ?")
                                    .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            setRepeatingAsyncTaskTrouver(id_trouver);

                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            new AnnulerDonnerTask().execute();
                                            trouver_search.setVisibility(View.GONE);
                                            btn_layout.setVisibility(View.VISIBLE);
                                        }
                                    }).show();
                        }else{
                            marque = jsonObject.getString("marque");
                            modele = jsonObject.getString("modele");
                            couleur = jsonObject.getString("couleur");
                            matricule = jsonObject.getString("matricule");

                            double lat_destination = jsonObject.getDouble("lat");
                            double lng_destination = jsonObject.getDouble("lng");
                            String heur_liberer = jsonObject.getString("heur_liberer");

                            destinationLatLng = new LatLng(lat_destination, lng_destination);

                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(destinationLatLng);
                            markerOptions.title(marque+" "+modele+" "+couleur);
                            markerOptions.snippet("En attente...");
                            Marker marker = mGoogleMap.addMarker(markerOptions);
                            marker.showInfoWindow();
                            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(14));

                            if(dialog_donner != null){
                                dialog_donner.dismiss();
                            }
                            pref.createAttribuer(id_attribuer);

                            String url = getDirectionsUrl(currentLatLng, destinationLatLng);
                            DownloadTask downloadTask = new DownloadTask();
                            downloadTask.execute(url);
                            new GoogMatrixRequestTrouver().execute(currentLatLng.latitude+"",currentLatLng.longitude+"",destinationLatLng.latitude+"",destinationLatLng.longitude+"",heur_liberer);
                        }


                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    private class DownloadTask extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(4);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            mGoogleMap.addPolyline(lineOptions);
        }
    }

    private class GoogMatrixRequestTrouver extends AsyncTask<String, Void, String>{

        private static final String API_KEY = "AIzaSyDf3DA9XirVxkwtpSP0puWZgp0NnQUpa8E";

        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            String json_string = null;
            heur_liberer = params[4];
            String url_request = "https://maps.googleapis.com/maps/api/distancematrix/json?origins="+params[0]+","+params[1]+"&destinations="+params[2]+","+params[3]+"&language=fr-FR&key=" + API_KEY;
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url_request)
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
            JSONObject jsonRespRouteDistance = null,jsonRespRouteTime = null;
            if (s != null) {
                try {
                    jsonRespRouteDistance = new JSONObject(s)
                            .getJSONArray("rows")
                            .getJSONObject(0)
                            .getJSONArray ("elements")
                            .getJSONObject(0)
                            .getJSONObject("distance");
                    jsonRespRouteTime = new JSONObject(s)
                            .getJSONArray("rows")
                            .getJSONObject(0)
                            .getJSONArray ("elements")
                            .getJSONObject(0)
                            .getJSONObject("duration");

                    String distance = jsonRespRouteDistance.get("text").toString();
                    String duration = jsonRespRouteTime.get("text").toString();

                    dialog_attribuer.dismiss();
                    String html = "Une place vous a été attribué, siuvez le tracer rouge pour vous y rendre <br/>" +
                            "<b>"+marque+" "+modele+" "+couleur+"</b>" +
                            " Immatriculé : <b>"+matricule+"</b><br/>" +
                            "la place sera libre a : <b>"+heur_liberer+"</b><br/>" +
                            "Distance : <b>"+distance+"</b>" +
                            " Durée du trajet estimée : <b>"+duration+"</b><br/>" +
                            "Vous pouvez annuler l'opération en cliquant sur annuler";
                    txt_attribuer2.setText(Html.fromHtml(html));
                    trouver_search.setVisibility(View.GONE);
                    attiruber_layout2.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    class finalTask extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... params) {

                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = new FormBody.Builder()
                            .add("attendu",params[0])
                            .add("id_attribuer",params[1])
                            .build();
                    Request request = new Request.Builder()
                            .url(URL+"final.php")
                            .post(body)
                            .build();
                    client.newCall(request).execute();


                } catch (@NonNull IOException e) {
                    Log.e("Json ErrorMarque", "" + e.getLocalizedMessage());
                }

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }

    //-------------------Fin Trouver--------------------------------------------------

}

