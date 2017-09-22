package com.app.garini.garini;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.garini.garini.login.User;
import com.app.garini.garini.service.CleanService;
import com.app.garini.garini.service.TrouverService;
import com.app.garini.garini.service.checkTrouverService;
import com.app.garini.garini.utile.DirectionsJSONParser;
import com.app.garini.garini.utile.PermissionUtils;
import com.app.garini.garini.utile.StaticValue;
import com.app.garini.garini.utile.UserSessionManager;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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


public class MapActivityTrouver extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ActivityCompat.OnRequestPermissionsResultCallback{

    public static boolean active = true;
    GoogleMap mGoogleMap;
    MapView mMapView;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    PolylineOptions polylineOptions;
    LocationRequest mLocationRequest;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private boolean zoom = false;
    private String URL = StaticValue.URL;
    LovelyInfoDialog dialog_donner;
    Button  btn_trouver_search_annuler, btn_attribuer_annuler2;
    LinearLayout  trouver_search, attiruber_layout2;
    LatLng currentLatLng, destinationLatLng;
    int id_user;
    int id_trouver = 0, id_attribuer = 0;
    String marque, modele, couleur, matricule, heur_liberer,nom_user;
    boolean isFinished = false;
    UserSessionManager pref;
    TimerTask taskTrouverCheck;
    boolean check = false;
    TextView txt_attribuer2;
    String currentAdresse = null;
    double lat,lng;
    TextView point;
    int nb_point = 0;
    boolean timelimit = false;
    PowerManager pm;
    PowerManager.WakeLock wl;
    private BroadcastReceiver mMessageReceiverTrouverService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            timelimit = intent.getBooleanExtra("timelimit",false);
            if(timelimit){
                trouver_search.setVisibility(View.GONE);
                new LovelyStandardDialog(MapActivityTrouver.this)
                        .setTopColorRes(R.color.colorPrimary)
                        .setButtonsColorRes(R.color.colorAccent)
                        .setIcon(R.drawable.ic_directions_car_white_24dp)
                        .setTitle("Donner votre place")
                        .setMessage("Aucune place trouver, voulez affectuer une nouvelle recherche ?")
                        .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {


                                trouver_search.setVisibility(View.GONE);
                                new AnnulerTrouverTask().execute(true);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new AnnulerTrouverTask().execute(false);


                            }
                        }).show();

            }else{
                id_attribuer = intent.getIntExtra("id_attribuer",0);
                if(id_attribuer!=0){
                    pref.createAttribuer(id_attribuer);
                    double lat_destination = intent.getDoubleExtra("lat_destination",0);
                    double lng_destination = intent.getDoubleExtra("lng_destination",0);
                    destinationLatLng = new LatLng(lat_destination, lng_destination);
                    heur_liberer = intent.getStringExtra("heur_liberer");
                    marque = intent.getStringExtra("marque");
                    modele = intent.getStringExtra("modele");
                    couleur = intent.getStringExtra("couleur");
                    matricule = intent.getStringExtra("matricule");
                    nom_user = intent.getStringExtra("nom_user");
                    new LovelyStandardDialog(MapActivityTrouver.this)
                            .setTopColorRes(R.color.colorPrimary)
                            .setButtonsColorRes(R.color.colorAccent)
                            .setIcon(R.drawable.ic_directions_car_white_24dp)
                            .setTitle("Donner votre place")
                            .setMessage("un automibiliste a été trouver, confirmez-vous la libération de votre place ?")
                            .setPositiveButton("Oui", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(destinationLatLng);
                                    markerOptions.title(marque+" "+modele+" "+couleur);
                                    markerOptions.snippet("En attente...");
                                    Marker marker = mGoogleMap.addMarker(markerOptions);
                                    marker.showInfoWindow();
                                    mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(14));

                                    String url = getDirectionsUrl(currentLatLng, destinationLatLng);
                                    DownloadTask downloadTask = new DownloadTask();
                                    downloadTask.execute(url);
                                    new GoogMatrixRequestTrouver().execute(currentLatLng.latitude+"",currentLatLng.longitude+"",destinationLatLng.latitude+"",destinationLatLng.longitude+"",heur_liberer);
                                }
                            })
                            .setNegativeButton("Non", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    trouver_search.setVisibility(View.GONE);
                                    new AnnulerAttribuerTask().execute(false);

                                }
                            }).show();
                }
            }
        }
    };

    private BroadcastReceiver mMessageReceiverCheckService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            check = intent.getBooleanExtra("check",false);
            if(check){
                id_attribuer = intent.getIntExtra("id_attribuer",0);
                if(id_attribuer!=0){
                    new LovelyStandardDialog(MapActivityTrouver.this)
                            .setTopColorRes(R.color.colorPrimary)
                            .setButtonsColorRes(R.color.colorAccent)
                            .setIcon(R.drawable.ic_directions_car_white_24dp)
                            .setTitle("Trouver une place")
                            .setMessage("La place a été supprimer , voulez vous rechercher une autre place ?")
                            .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    trouver_search.setVisibility(View.GONE);
                                    new AnnulerAttribuerTask().execute(true);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    trouver_search.setVisibility(View.GONE);
                                    new AnnulerAttribuerTask().execute(false);

                                }
                            }).show();
                }

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_trouver);

        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Gari");
        wl.acquire();

        pref = new UserSessionManager(this);
        User user = pref.getUserDetails();
        id_user = user.getIdUser();

        Bundle inBundle = getIntent().getExtras();
        if(inBundle != null){

            nb_point = inBundle.getInt("nb_point");
            lat = inBundle.getDouble("lat");
            lng = inBundle.getDouble("lng");
            if(inBundle.get("id_trouver") !=null){
                id_trouver = inBundle.getInt("id_trouver");
            }
            if(inBundle.get("id_attribuer") !=null){
                pref.createAttribuer(id_attribuer);
                id_attribuer = inBundle.getInt("id_attribuer");
                double lat_destination = inBundle.getDouble("lat_destination");
                double lng_destination = inBundle.getDouble("lng_destination");
                destinationLatLng = new LatLng(lat_destination, lng_destination);
                heur_liberer = inBundle.getString("heur_liberer");
                marque = inBundle.getString("marque");
                modele = inBundle.getString("modele");
                couleur = inBundle.getString("couleur");
                matricule = inBundle.getString("matricule");
                nom_user = inBundle.getString("nom_user");
            }
            if(inBundle.get("timelimit")!=null){
                timelimit = inBundle.getBoolean("timelimit");
            }
            if(inBundle.get("check")!=null){
                check = inBundle.getBoolean("check");
                if(check){
                    id_attribuer = inBundle.getInt("id_attribuer");
                    if(id_attribuer!=0){
                        new LovelyStandardDialog(MapActivityTrouver.this)
                                .setTopColorRes(R.color.colorPrimary)
                                .setButtonsColorRes(R.color.colorAccent)
                                .setIcon(R.drawable.ic_directions_car_white_24dp)
                                .setTitle("Trouver une place")
                                .setMessage("La place a été supprimer , voulez vous rechercher une autre place ?")
                                .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        trouver_search.setVisibility(View.GONE);
                                        new AnnulerAttribuerTask().execute(true);
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        trouver_search.setVisibility(View.GONE);
                                        new AnnulerAttribuerTask().execute(false);

                                    }
                                }).show();
                    }

                }
            }
        }

        mMapView = (MapView) findViewById(R.id.map);
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
        point = (TextView) findViewById(R.id.point);
        point.setBackgroundDrawable(gradientDrawableYellow);
        point.setText(Html.fromHtml("Points récoltés :<br/>"+nb_point+" pts"));
        //--------------Fin Get point --------------------------

        //----------------Trouver ---------------------------

        trouver_search = (LinearLayout) findViewById(R.id.trouver_search);

        btn_trouver_search_annuler = (Button) findViewById(R.id.btn_trouver_search_annuler);
        GradientDrawable gradientDrawableRed2 = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#820233"), Color.parseColor("#EF4339")}); // Gradient Color Codes

        gradientDrawableRed2.setGradientType(GradientDrawable.LINEAR_GRADIENT); // Gradient Type
        gradientDrawableRed2.setCornerRadius(15);
        btn_trouver_search_annuler.setBackgroundDrawable(gradientDrawableRed2);
        btn_trouver_search_annuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new LovelyStandardDialog(MapActivityTrouver.this)
                        .setTopColorRes(R.color.colorPrimary)
                        .setButtonsColorRes(R.color.colorAccent)
                        .setIcon(R.drawable.ic_directions_car_white_24dp)
                        .setTitle("Annuler la recherche")
                        .setMessage("vous êtes certain de vouloir annuler la recherche ?")
                        .setPositiveButton("Oui", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                /*if (taskTrouver != null) {
                                    taskTrouver.cancel();
                                }*/
                                stopService(new Intent(MapActivityTrouver.this, TrouverService.class));
                                pref.deleteTrouver();
                                trouver_search.setVisibility(View.GONE);
                                new AnnulerTrouverTask().execute(false);
                            }
                        })
                        .setNegativeButton("Non", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {


                            }
                        }).show();
            }
        });


        //----------------Fin Trouver ---------------------------

        //----------------Attribuer Trouver ---------------------------
        attiruber_layout2 = (LinearLayout) findViewById(R.id.attiruber_layout2);


        txt_attribuer2 = (TextView) findViewById(R.id.txt_attribuer2);

        btn_attribuer_annuler2 = (Button) findViewById(R.id.btn_attribuer_annuler2);
        GradientDrawable gradientDrawableRed5 = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#820233"), Color.parseColor("#EF4339")}); // Gradient Color Codes

        gradientDrawableRed5.setGradientType(GradientDrawable.LINEAR_GRADIENT); // Gradient Type
        gradientDrawableRed5.setCornerRadius(15);
        btn_attribuer_annuler2.setBackgroundDrawable(gradientDrawableRed5);
        btn_attribuer_annuler2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new LovelyStandardDialog(MapActivityTrouver.this)
                        .setTopColorRes(R.color.colorPrimary)
                        .setButtonsColorRes(R.color.colorAccent)
                        .setIcon(R.drawable.ic_directions_car_white_24dp)
                        .setTitle("Annuler")
                        .setMessage("vous êtes certain de vouloir annuler ?")
                        .setPositiveButton("Oui", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                stopService(new Intent(MapActivityTrouver.this, checkTrouverService.class));
                                new AnnulerAttribuerTask().execute(false);
                            }
                        })
                        .setNegativeButton("Non", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {


                            }
                        }).show();
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
        MapsInitializer.initialize(this);
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //mGoogleMap.setTrafficEnabled(true);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        enableMyLocation();

        if(!pref.isTrouver()){
            new GetAddressTask().execute(lat+"",lng+"");
            if(pref.getIdTrouver()!=0){
                id_trouver = pref.getIdTrouver();
                if(pref.getIdAttribuer()!=0){
                    id_attribuer = pref.getIdAttribuer();
                    new AnnulerAttribuerTask().execute(false);
                }
            }
        }else{
            if(pref.isTrouver()){

                trouver_search.setVisibility(View.VISIBLE);
                id_trouver = pref.getIdTrouver();
                if(timelimit){

                    trouver_search.setVisibility(View.GONE);
                    new LovelyStandardDialog(MapActivityTrouver.this)
                            .setTopColorRes(R.color.colorPrimary)
                            .setButtonsColorRes(R.color.colorAccent)
                            .setIcon(R.drawable.ic_directions_car_white_24dp)
                            .setTitle("Donner votre place")
                            .setMessage("Aucune place trouver, voulez affectuer une nouvelle recherche ?")
                            .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {


                                    trouver_search.setVisibility(View.GONE);
                                    new AnnulerTrouverTask().execute(true);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new AnnulerTrouverTask().execute(false);

                                }
                            }).show();
                }else
                if(id_attribuer != 0 ){

                    new LovelyStandardDialog(MapActivityTrouver.this)
                            .setTopColorRes(R.color.colorPrimary)
                            .setButtonsColorRes(R.color.colorAccent)
                            .setIcon(R.drawable.ic_directions_car_white_24dp)
                            .setTitle("Donner votre place")
                            .setMessage("un automibiliste a été trouver, confirmez-vous la libération de votre place ?")
                            .setPositiveButton("Oui", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(destinationLatLng);
                                    markerOptions.title(marque+" "+modele+" "+couleur);
                                    markerOptions.snippet("En attente...");
                                    Marker marker = mGoogleMap.addMarker(markerOptions);
                                    marker.showInfoWindow();
                                    mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(14));

                                    String url = getDirectionsUrl(currentLatLng, destinationLatLng);
                                    DownloadTask downloadTask = new DownloadTask();
                                    downloadTask.execute(url);
                                    new GoogMatrixRequestTrouver().execute(currentLatLng.latitude+"",currentLatLng.longitude+"",destinationLatLng.latitude+"",destinationLatLng.longitude+"",heur_liberer);
                                }
                            })
                            .setNegativeButton("Non", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    trouver_search.setVisibility(View.GONE);
                                    new AnnulerAttribuerTask().execute(false);

                                }
                            }).show();
                }
            }
        }

        currentLatLng = new LatLng(lat, lng);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title("Vous êtes ici");
        //markerOptions.snippet("Vous êtes ici");

        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
        mCurrLocationMarker.showInfoWindow();
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(14));

    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(this.getSupportFragmentManager(), "dialog");
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
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
        mGoogleApiClient = new GoogleApiClient.Builder(this)
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
        if (ContextCompat.checkSelfPermission(this,
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

                if (distanceBetween < 4 ) {
                    isFinished = true;
                    Toast.makeText(this, "Vous ete arrivé !", Toast.LENGTH_LONG).show();
                    String text = getString(R.string.dite_merci,nom_user);
                    new LovelyStandardDialog(MapActivityTrouver.this)
                            .setTopColorRes(R.color.colorPrimary)
                            .setButtonsColorRes(R.color.colorAccent)
                            .setIcon(R.drawable.ic_directions_car_white_24dp)
                            .setTitle("Vous êtes arriver")
                            .setMessage(text)
                            .setPositiveButton("Merci", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mGoogleMap.clear();
                                    new finalTask().execute("oui", id_attribuer + "");
                                    destinationLatLng = null;
                                    pref.deleteTrouver();
                                    pref.deleteAttribuer();
                                    pref.setIdAttribuer(0);
                                    pref.setIdDonner(0);
                                    Intent intent = new Intent(MapActivityTrouver.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .setNegativeButton("Signaler", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mGoogleMap.clear();
                                    new finalTask().execute("non", id_attribuer + "");
                                    destinationLatLng = null;
                                    pref.deleteTrouver();
                                    pref.deleteAttribuer();
                                    pref.setIdAttribuer(0);
                                    pref.setIdDonner(0);
                                    Intent intent = new Intent(MapActivityTrouver.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
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
        active = true;
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiverTrouverService, new IntentFilter("trouverService"));

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiverCheckService, new IntentFilter("checkTrouverService"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        active = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverTrouverService);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverCheckService);
        wl.release();
    }

    @Override
    protected void onStop() {
        super.onStop();
        active = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverTrouverService);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverCheckService);
    }

    @Override
    protected void onStart() {
        super.onStart();
        active = true;
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
                Log.e("Json GetAddressTask", "" + e.getLocalizedMessage());
            }

            return json_string;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MapActivityTrouver.this);
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
                        if (currentLatLng != null && currentAdresse != null) {

                            double currentLat = currentLatLng.latitude;
                            double currentLng = currentLatLng.longitude;

                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String date = dateFormat.format(new Date());

                            new TrouverTask().execute(id_user + "", currentLat + "", currentLng + "", currentLat + "", currentLng + "", date, currentAdresse, currentAdresse);

                            startService(new Intent(MapActivityTrouver.this, CleanService.class));
                        } else {
                            Toast.makeText(MapActivityTrouver.this, "En attente de géolocalisation...", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(MapActivityTrouver.this,"Une erreur est survenue, veuillez recommencer",Toast.LENGTH_SHORT).show();

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

    public class AnnulerAttribuerTask extends AsyncTask<Boolean, Void, Boolean> {

        protected Boolean doInBackground(Boolean... params) {

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
                    Log.e("AnnulerAttribuerTask", "" + e.getLocalizedMessage());
                }
            }
            return params[0];
        }

        @Override
        protected void onPostExecute(Boolean repeat) {
            super.onPostExecute(repeat);
            id_attribuer = 0;
            pref.deleteAttribuer();

            if(attiruber_layout2.getVisibility() == View.VISIBLE){
                attiruber_layout2.setVisibility(View.GONE);
            }
            pref.setIdAttribuer(0);
            new AnnulerTrouverTask().execute(repeat);
        }
    }

    //----------------Fin Taches commune-------------------------------------


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
                Log.e("Json TrouverTask", "" + e.getLocalizedMessage());
            }

            return json_string;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MapActivityTrouver.this);
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

                        Toast.makeText(MapActivityTrouver.this,"Une erreur est survenue, veuillez recommencer",Toast.LENGTH_SHORT).show();
                    }else{
                        trouver_search.setVisibility(View.VISIBLE);

                        id_trouver = jsonObject.getInt("id_trouver");
                        pref.createTrouver(id_trouver);
                        /*dialog_donner = new LovelyInfoDialog(MapActivityTrouver.this);
                        dialog_donner.setTopColorRes(R.color.colorPrimary)
                                .setIcon(R.drawable.ic_directions_car_white_24dp)
                                //This will add Don't show again checkbox to the dialog. You can pass any ID as argument
                                .setTitle("Trouver une place")
                                .setMessage("Nous allons trouver une place pour vous, temps d’attente 5mn.")
                                .show();*/
                        //setRepeatingAsyncTaskTrouver(id_trouver);
                        Intent intent = new Intent(MapActivityTrouver.this, TrouverService.class);
                        intent.putExtra("id_trouver",id_trouver);
                        intent.putExtra("lat",lat);
                        intent.putExtra("lng",lng);
                        intent.putExtra("nb_point",nb_point);
                        startService(intent);
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


    public class AnnulerTrouverTask extends AsyncTask<Boolean, Void, Boolean> {

        ProgressDialog diag;
        protected Boolean doInBackground(Boolean... params) {

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
                    Log.e("Json AnnulerTrouverTask", "" + e.getLocalizedMessage());
                }
            }
            return params[0];
        }
        @Override
        protected void onPostExecute(Boolean repeat) {
            super.onPostExecute(repeat);
            id_trouver = 0;
            pref.deleteTrouver();
            pref.setIdTrouver(0);
            diag.dismiss();
            if(repeat){
                new GetAddressTask().execute(lat+"",lng+"");
            }else{
                Intent intent = new Intent(MapActivityTrouver.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            diag = new ProgressDialog(MapActivityTrouver.this);
            diag.setTitle("Patientez un instant SVP...");
            diag.setMessage("traitement en cours ");
            diag.show();
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
            java.net.URL url = new URL(strUrl);

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
                Log.e("GMatrixRequestTrouver", "" + e.getLocalizedMessage());
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

                    String text = getString(R.string.place_trouver, marque+" "+modele, couleur,matricule,heur_liberer,distance,duration);
                    /*String html = "Une place vous a été attribué, siuvez le tracer rouge pour vous y rendre <br/>" +
                            "<b>"+marque+" "+modele+" "+couleur+"</b>" +
                            " Immatriculé : <b>"+matricule+"</b><br/>" +
                            "la place sera libre a : <b>"+heur_liberer+"</b><br/>" +
                            "Distance : <b>"+distance+"</b>" +
                            " Durée du trajet estimée : <b>"+duration+"</b><br/>" +
                            "Vous pouvez annuler l'opération en cliquant sur annuler";
                    txt_attribuer2.setText(Html.fromHtml(html));*/
                    txt_attribuer2.setText(text);
                    trouver_search.setVisibility(View.GONE);
                    attiruber_layout2.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(MapActivityTrouver.this, checkTrouverService.class);
                    intent.putExtra("id_attribuer",id_attribuer);
                    intent.putExtra("lat",lat);
                    intent.putExtra("lng",lng);
                    intent.putExtra("nb_point",nb_point);
                    startService(intent);
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
                Log.e("Json FinalTask", "" + e.getLocalizedMessage());
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
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
                        .url(URL+"check_trouver.php")
                        .post(body)
                        .build();
                response = client.newCall(request).execute();
                json_string = response.body().string();

            } catch (@NonNull IOException e) {
                Log.e("Json AfterAttribuerTask", "" + e.getLocalizedMessage());
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
                Log.e("Check_trouver",s);
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    boolean error = jsonObject.getBoolean("error");
                    if(!error){
                        int valider = jsonObject.getInt("valider");
                        int etat = jsonObject.getInt("etat");
                        if(valider == 2 || etat==2){
                            taskTrouverCheck.cancel();
                            new LovelyStandardDialog(MapActivityTrouver.this)
                                    .setTopColorRes(R.color.colorPrimary)
                                    .setButtonsColorRes(R.color.colorAccent)
                                    .setIcon(R.drawable.ic_directions_car_white_24dp)
                                    .setTitle("Trouver une place")
                                    .setMessage("La place a été supprimer , voulez vous rechercher une autre place ?")
                                    .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            trouver_search.setVisibility(View.GONE);
                                            new AnnulerAttribuerTask().execute(true);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            trouver_search.setVisibility(View.GONE);
                                            new AnnulerAttribuerTask().execute(false);

                                        }
                                    }).show();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //-------------------Fin Trouver--------------------------------------------------

    @Override
    public void onBackPressed() {
        if(pref.isTrouver()) {
            Toast.makeText(this, "Vous devez annuler la recherche avant de quitter la map !",
                    Toast.LENGTH_SHORT).show();
        }else {
            super.onBackPressed();
            finish();
        }
    }
}

