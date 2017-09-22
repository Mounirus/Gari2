package com.app.garini.garini;

import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.app.garini.garini.login.User;
import com.app.garini.garini.service.CleanService;
import com.app.garini.garini.service.DonnerService;
import com.app.garini.garini.service.attribuerService;
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
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MapActivity extends AppCompatActivity implements
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
    //private boolean zone = false;
    private String URL = StaticValue.URL;

    Button btn_donner_search_annuler, btn_donner_now, btn_attribuer_annuler;
    ImageButton btn_donner, btn_donner_annuler;
    TextView point;
    LinearLayout donner_layout, donner_search, attiruber_layout;
    TimePicker time;
    LatLng currentLatLng;
    int id_user;
    int id_donner = 0, id_attribuer = 0,attendu =0;
    String marque, modele, couleur, matricule,nom_user;
    double lat_a,lng_a,lat_d,lng_d;
    UserSessionManager pref;
    LovelyInfoDialog dialog_donner;
    TextView txt_attribuer;
    public static boolean now = false;
    String currentAdresse = null;
    double lat,lng;
    boolean timelimit = false;
    int nb_point = 0;
    PowerManager pm;
    PowerManager.WakeLock wl;
    private BroadcastReceiver mMessageReceiverDonnerService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            timelimit = intent.getBooleanExtra("timelimit",false);
            if(timelimit){
                new AnnulerDonnerTask().execute();
                donner_search.setVisibility(View.GONE);
                new LovelyStandardDialog(MapActivity.this)
                        .setTopColorRes(R.color.colorPrimary)
                        .setButtonsColorRes(R.color.colorAccent)
                        .setIcon(R.drawable.ic_directions_car_white_24dp)
                        .setTitle("Donner votre place")
                        .setMessage("Aucun automobiliste trouver, voulez affectuer une nouvelle recherche ?")
                        .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                donner_layout.setVisibility(View.VISIBLE);
                                donner_search.setVisibility(View.GONE);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent intent = new Intent(MapActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();

                            }
                        }).show();
            }else{
                id_attribuer = intent.getIntExtra("id_attribuer",0);
                if(id_attribuer!=0){
                    marque = intent.getStringExtra("marque");
                    modele = intent.getStringExtra("modele");
                    couleur = intent.getStringExtra("couleur");
                    matricule = intent.getStringExtra("matricule");
                    nom_user = intent.getStringExtra("nom_user");

                    lat_a = intent.getDoubleExtra("lat_a",0);
                    lat_d = intent.getDoubleExtra("lat_d",0);
                    lng_a = intent.getDoubleExtra("lng_a",0);
                    lng_d = intent.getDoubleExtra("lng_d",0);
                    donner_layout.setVisibility(View.GONE);
                    new LovelyStandardDialog(MapActivity.this)
                            .setTopColorRes(R.color.colorPrimary)
                            .setButtonsColorRes(R.color.colorAccent)
                            .setIcon(R.drawable.ic_directions_car_white_24dp)
                            .setTitle("Donner votre place")
                            .setMessage("un automibiliste a été trouver, confirmez-vous la libération de votre place ?")
                            .setPositiveButton("Oui", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    if(dialog_donner != null){
                                        dialog_donner.dismiss();
                                    }
                                    pref.createAttribuer(id_attribuer);
                                    new GoogMatrixRequest().execute(lat_d,lng_d,lat_a,lng_a);
                                }
                            })
                            .setNegativeButton("Non", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    donner_search.setVisibility(View.GONE);
                                    new AnnulerDonnerTask().execute();

                                }
                            }).show();
                }
            }
        }
    };

    private BroadcastReceiver mMessageReceiverAttribuerService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            attendu = intent.getIntExtra("attendu",0);
            if(attendu!=0){
                Intent i = new Intent(MapActivity.this, MainActivity.class);
                i.putExtra("attendu",attendu);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_donner);

        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Gari");
        wl.acquire();


        pref = new UserSessionManager(this);

        User user = pref.getUserDetails();

        id_user = user.getIdUser();

        Bundle inBundle = getIntent().getExtras();
        if(inBundle != null){
            //id_user = inBundle.getInt("id_user");
            lat = inBundle.getDouble("lat");
            lng = inBundle.getDouble("lng");
            nb_point = inBundle.getInt("nb_point");
            if(inBundle.get("id_attribuer") !=null){

                id_attribuer = inBundle.getInt("id_attribuer");
                marque = inBundle.getString("marque");
                modele = inBundle.getString("modele");
                couleur = inBundle.getString("couleur");
                matricule = inBundle.getString("matricule");
                nom_user = inBundle.getString("nom_user");

                lat_a = inBundle.getDouble("lat_a");
                lat_d = inBundle.getDouble("lat_d");
                lng_a = inBundle.getDouble("lng_a");
                lng_d = inBundle.getDouble("lng_d");

            }

            if(inBundle.get("timelimit")!=null){
                timelimit = inBundle.getBoolean("timelimit");
            }
            if(inBundle.get("attendu")!=null){
                attendu = inBundle.getInt("attendu");
                if (attendu!=0){
                    Intent i = new Intent(MapActivity.this, MainActivity.class);
                    i.putExtra("attendu",attendu);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
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
        //new GetPointTask().execute(id_user);
        //--------------Fin Get point --------------------------

        //----------------Donner ---------------------------


        donner_layout = (LinearLayout) findViewById(R.id.donner_layout);

        btn_donner = (ImageButton) findViewById(R.id.btn_donner);
        GradientDrawable gradientDrawableGreen2 = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#2E7D32"), Color.parseColor("#4CAF50"), Color.parseColor("#81C784")}); // Gradient Color Codes

        gradientDrawableGreen2.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gradientDrawableGreen2.setCornerRadius(15);
        btn_donner.setBackgroundDrawable(gradientDrawableGreen2);
        btn_donner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SplashActivity.isDataConnectionAvailable(MapActivity.this)) {
                    if (currentLatLng != null && currentAdresse != null) {
                        now = false;
                        donner_layout.setVisibility(View.GONE);

                        double currentLat = currentLatLng.latitude;
                        double currentLng = currentLatLng.longitude;

                        String heur = time.getCurrentHour() + ":" + time.getCurrentMinute() + ":00";

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String date = dateFormat.format(new Date()) + " " + heur;

                        new DonnerTask().execute(id_user + "", currentLat + "", currentLng + "", date, currentAdresse);
                        startService(new Intent(MapActivity.this, CleanService.class));
                    } else {
                        Toast.makeText(MapActivity.this, "En attente de géolocalisation...", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MapActivity.this, "Activez votre connexion internet !", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btn_donner_now = (Button) findViewById(R.id.btn_donner_now);
        GradientDrawable gradientDrawableGreen3 = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#2E7D32"), Color.parseColor("#4CAF50"), Color.parseColor("#81C784")}); // Gradient Color Codes

        gradientDrawableGreen3.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gradientDrawableGreen3.setCornerRadius(15);
        btn_donner_now.setBackgroundDrawable(gradientDrawableGreen3);
        btn_donner_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SplashActivity.isDataConnectionAvailable(MapActivity.this)) {
                    if (currentLatLng != null && currentAdresse != null) {
                        now = true;
                        donner_layout.setVisibility(View.GONE);

                        double currentLat = currentLatLng.latitude;
                        double currentLng = currentLatLng.longitude;

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String date = dateFormat.format(new Date());

                        new DonnerTask().execute(id_user + "", currentLat + "", currentLng + "", date, currentAdresse);
                        startService(new Intent(MapActivity.this, CleanService.class));
                    } else {
                        Toast.makeText(MapActivity.this, "En attente de géolocalisation...", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MapActivity.this, "Activez votre connexion internet !", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_donner_annuler = (ImageButton) findViewById(R.id.btn_donner_annuler);
        btn_donner_annuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

            }
        });

        time = (TimePicker) findViewById(R.id.time);
        time.setIs24HourView(true);
        final Calendar c = Calendar.getInstance();
        time.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
        time.setCurrentMinute(c.get(Calendar.MINUTE));

        donner_search = (LinearLayout) findViewById(R.id.donner_search);

        btn_donner_search_annuler = (Button) findViewById(R.id.btn_donner_search_annuler);
        GradientDrawable gradientDrawableRed3 = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#820233"), Color.parseColor("#EF4339")}); // Gradient Color Codes

        gradientDrawableRed3.setGradientType(GradientDrawable.LINEAR_GRADIENT); // Gradient Type
        gradientDrawableRed3.setCornerRadius(15);
        btn_donner_search_annuler.setBackgroundDrawable(gradientDrawableRed3);
        btn_donner_search_annuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LovelyStandardDialog(MapActivity.this)
                        .setTopColorRes(R.color.colorPrimary)
                        .setButtonsColorRes(R.color.colorAccent)
                        .setIcon(R.drawable.ic_directions_car_white_24dp)
                        .setTitle("Annuler la recherche")
                        .setMessage("vous êtes certain de vouloir annuler la recherche ?")
                        .setPositiveButton("Oui", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                /*if (now) {
                                    if (taskDonner != null) {
                                        taskDonner.cancel();
                                    }
                                } else {
                                    AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                                    Intent intent = new Intent(MapActivity.this, DonnerService.class);
                                    intent.putExtra("lat",lat);
                                    intent.putExtra("lng",lng);
                                    PendingIntent pIntent = PendingIntent.getService(MapActivity.this, 0, intent, 0);
                                    alarmMgr.cancel(pIntent);
                                }*/
                                stopService(new Intent(MapActivity.this, DonnerService.class));

                                AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                                Intent intent = new Intent(MapActivity.this, DonnerService.class);
                                intent.putExtra("id_donner",id_donner);
                                intent.putExtra("lat",lat);
                                intent.putExtra("lng",lng);
                                intent.putExtra("now",now);
                                PendingIntent pIntent = PendingIntent.getService(MapActivity.this, id_donner, intent, 0);
                                alarmMgr.cancel(pIntent);

                                pref.deleteDonner();
                                donner_search.setVisibility(View.GONE);

                                new AnnulerDonnerTask().execute();

                            }
                        })
                        .setNegativeButton("Non", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {


                            }
                        }).show();
            }
        });

        //----------------Fin Donner ---------------------------


        //----------------Attribuer donner ---------------------------

        attiruber_layout = (LinearLayout) findViewById(R.id.attiruber_layout);

        txt_attribuer = (TextView) findViewById(R.id.txt_attribuer);

        btn_attribuer_annuler = (Button) findViewById(R.id.btn_attribuer_annuler);
        GradientDrawable gradientDrawableRed4 = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#820233"), Color.parseColor("#EF4339")}); // Gradient Color Codes

        gradientDrawableRed4.setGradientType(GradientDrawable.LINEAR_GRADIENT); // Gradient Type
        gradientDrawableRed4.setCornerRadius(15);
        btn_attribuer_annuler.setBackgroundDrawable(gradientDrawableRed4);
        btn_attribuer_annuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new LovelyStandardDialog(MapActivity.this)
                        .setTopColorRes(R.color.colorPrimary)
                        .setButtonsColorRes(R.color.colorAccent)
                        .setIcon(R.drawable.ic_directions_car_white_24dp)
                        .setTitle("Annuler")
                        .setMessage("vous êtes certain de vouloir annuler ?")
                        .setPositiveButton("Oui", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                stopService(new Intent(MapActivity.this, attribuerService.class));
                                new AnnulerDonnerTask().execute();

                            }
                        })
                        .setNegativeButton("Non", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {


                            }
                        }).show();


            }
        });
        //----------------Fin Attribuer donner ---------------------------

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



        if(pref.isDonner()){
            donner_layout.setVisibility(View.GONE);
            donner_search.setVisibility(View.VISIBLE);
            id_donner = pref.getIdDonner();
            if(timelimit){
                new AnnulerDonnerTask().execute();
                donner_search.setVisibility(View.GONE);
                new LovelyStandardDialog(MapActivity.this)
                        .setTopColorRes(R.color.colorPrimary)
                        .setButtonsColorRes(R.color.colorAccent)
                        .setIcon(R.drawable.ic_directions_car_white_24dp)
                        .setTitle("Donner votre place")
                        .setMessage("Aucun automobiliste trouver, voulez affectuer une nouvelle recherche ?")
                        .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                donner_layout.setVisibility(View.VISIBLE);
                                donner_search.setVisibility(View.GONE);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent intent = new Intent(MapActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);

                            }
                        }).show();
            }else
            if(id_attribuer != 0 ){
                donner_layout.setVisibility(View.GONE);
                new LovelyStandardDialog(MapActivity.this)
                        .setTopColorRes(R.color.colorPrimary)
                        .setButtonsColorRes(R.color.colorAccent)
                        .setIcon(R.drawable.ic_directions_car_white_24dp)
                        .setTitle("Donner votre place")
                        .setMessage("un automibiliste a été trouver, confirmez-vous la libération de votre place ?")
                        .setPositiveButton("Oui", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if(dialog_donner != null){
                                    dialog_donner.dismiss();
                                }
                                pref.createAttribuer(id_attribuer);
                                new GoogMatrixRequest().execute(lat_d,lng_d,lat_a,lng_a);
                            }
                        })
                        .setNegativeButton("Non", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                donner_search.setVisibility(View.GONE);
                                new AnnulerDonnerTask().execute();

                            }
                        }).show();
            }
        }else{
            if(pref.getIdDonner()!=0){
                donner_layout.setVisibility(View.VISIBLE);
                id_donner = pref.getIdDonner();
                new AnnulerDonnerTask().execute();
            }

            donner_search.setVisibility(View.GONE);
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

        new GetAddressTask().execute(lat+"",lng+"");



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
        //NewMessageNotification.cancel(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiverDonnerService, new IntentFilter("donnerService"));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiverAttribuerService, new IntentFilter("attribuerService"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        active = false;
        wl.release();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverDonnerService);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverAttribuerService);
    }

    @Override
    protected void onStop() {
        super.onStop();
        active = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverDonnerService);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverAttribuerService);
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
            dialog = new ProgressDialog(MapActivity.this);
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
                        Toast.makeText(MapActivity.this,"Une erreur est survenue, veuillez recommencer",Toast.LENGTH_SHORT).show();

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
                Log.e("GoogMatrixRequest", "" + e.getLocalizedMessage());
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

                    String text = getString(R.string.donner_attribuer, marque+" "+modele, couleur,matricule,heur_arriver,nom_user);
                    /*String html = "Votre place a été attribué a :<br/>" +
                            "<b>"+marque+" "+modele+" "+couleur+"</b><br/>" +
                            "Immatriculé : <b>"+matricule+"</b><br/>" +
                            "Heur d'arrivée estimée : <b>"+heur_arriver+"</b><br/>" +
                            "L'heur d'arrivée etant approximative, si l'attente vous semble trop longue, vous pouvez annuler l'opération en cliquant sur annuler<br/>";*/
                    //txt_attribuer.setText(Html.fromHtml(html));
                    txt_attribuer.setText(text);
                    donner_search.setVisibility(View.GONE);
                    attiruber_layout.setVisibility(View.VISIBLE);

                    Intent intent = new Intent(MapActivity.this, attribuerService.class);
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
                Log.e("Json DonnerTask", "" + e.getLocalizedMessage());
            }

            return json_string;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MapActivity.this);
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

                        Toast.makeText(MapActivity.this,"Une erreur est survenue, veuillez recommencer",Toast.LENGTH_SHORT).show();
                    }else{
                        donner_search.setVisibility(View.VISIBLE);

                        id_donner = jsonObject.getInt("id_donner");
                        pref.createDonner(id_donner);
                        if(now){
                            /*dialog_donner = new LovelyInfoDialog(MapActivity.this);
                            dialog_donner.setTopColorRes(R.color.colorPrimary)
                                    .setIcon(R.drawable.ic_directions_car_white_24dp)
                                    //This will add Don't show again checkbox to the dialog. You can pass any ID as argument
                                    .setTitle("Donner votre place")
                                    .setMessage("Nous allons vous mettre en relation avec un automobiliste qui cherche une place, temps d’attente 3mn.")
                                    .show();*/
                            //setRepeatingAsyncTask(id_donner);

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            try {
                                Date date = sdf.parse(heur);
                                AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                                Intent intent = new Intent(MapActivity.this, DonnerService.class);
                                intent.putExtra("id_donner",id_donner);
                                intent.putExtra("lat",lat);
                                intent.putExtra("lng",lng);
                                intent.putExtra("nb_point",nb_point);
                                intent.putExtra("now",now);
                                PendingIntent pIntent = PendingIntent.getService(MapActivity.this, id_donner, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                                Log.e("map datas",lat+" "+lng+" "+now+" "+id_donner);


                                Calendar cal= Calendar.getInstance();
                                cal.setTime(date);
                                //cal.add(Calendar.MINUTE, -15);
                                //Toast.makeText(MapActivity.this,sdf.format(cal.getTime()),Toast.LENGTH_SHORT).show();
                                alarmMgr.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pIntent);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }else{
                            new LovelyInfoDialog(MapActivity.this)
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
                                AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                                Intent intent = new Intent(MapActivity.this, DonnerService.class);
                                intent.putExtra("id_donner",id_donner);
                                intent.putExtra("lat",lat);
                                intent.putExtra("lng",lng);
                                intent.putExtra("nb_point",nb_point);
                                PendingIntent pIntent = PendingIntent.getService(MapActivity.this, id_donner, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                                Calendar cal= Calendar.getInstance();
                                cal.setTime(date);
                                cal.add(Calendar.MINUTE, -15);
                                //Toast.makeText(MapActivity.this,sdf.format(cal.getTime()),Toast.LENGTH_SHORT).show();
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

    class AnnulerDonnerTask extends AsyncTask<String, Void, Void> {

        ProgressDialog diag;
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
                    Log.e("Json AnnulerDonnerTask", "" + e.getLocalizedMessage());
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            diag = new ProgressDialog(MapActivity.this);
            diag.setTitle("Patientez un instant SVP...");
            diag.setMessage("traitement en cours ");
            diag.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            id_donner = 0;
            pref.setIdDonner(0);
            pref.deleteDonner();
            donner_search.setVisibility(View.GONE);
            attiruber_layout.setVisibility(View.GONE);
            donner_layout.setVisibility(View.VISIBLE);
            diag.dismiss();


        }
    }

    //-------------------Fin donner--------------------------------------------------


    @Override
    public void onBackPressed() {
        if(pref.isDonner() && MapActivity.now) {
            Toast.makeText(this, "Vous devez annuler la recherche avant de quitter la map !",
                    Toast.LENGTH_SHORT).show();
        }else {
            super.onBackPressed();
            Intent intent = new Intent(MapActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        }
    }

}

