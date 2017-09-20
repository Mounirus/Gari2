package com.app.garini.garini;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.garini.garini.login.User;
import com.app.garini.garini.utile.PermissionUtils;
import com.app.garini.garini.utile.UserSessionManager;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.login.LoginManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener

{

    private Boolean exit = false;
    private ProgressDialog dialog;
    ImageView myImage;
    Fragment fragment = null;
    Class fragmentClass = null;
    final FragmentManager manager = getSupportFragmentManager();

    boolean firstTime = true, isAttendu = false;
    int id_user, id_trouver = 0, attendu = 0;
    UserSessionManager userSessionManager;
    NavigationView navigationView;

    private LocationManager locationManager;
    private LocationListener listener;
    private Location mlocation;
    ProgressDialog dialog_location;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String reqString = Build.MANUFACTURER
                + " " + Build.MODEL + " " + Build.VERSION.RELEASE
                + " " + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();
        Log.e("Modele Mobile :", reqString);

        Fresco.initialize(this);
        userSessionManager = new UserSessionManager(getApplicationContext());
        User user = userSessionManager.getUserDetails();

        Bundle inBundle = getIntent().getExtras();
        if (inBundle != null) {
            if (inBundle.get("id_trouver") != null) {
                id_trouver = inBundle.getInt("id_trouver");
            }
            if (inBundle.get("attendu") != null) {
                attendu = inBundle.getInt("attendu");
                isAttendu = true;

                if(attendu == 1){

                    new LovelyInfoDialog(this)
                            .setTopColorRes(R.color.colorPrimary)
                            .setIcon(R.drawable.ic_phone_android_white_24dp)
                            //This will add Don't show again checkbox to the dialog. You can pass any ID as argument
                            .setTitle("Information")
                            .setMessage(R.string.merci)
                            .show();

                }
                if(attendu == 3){

                    new LovelyInfoDialog(this)
                            .setTopColorRes(R.color.colorPrimary)
                            .setIcon(R.drawable.ic_phone_android_white_24dp)
                            //This will add Don't show again checkbox to the dialog. You can pass any ID as argument
                            .setTitle("Information")
                            .setMessage(R.string.signaler)
                            .show();

                }
            }
        }

        String name = user.getName();
        String email = user.getEmail();
        id_user = user.getIdUser();
        String imageUrl = null;
        if (userSessionManager.isUserLoggedInFb()) {
            imageUrl = user.getImage();
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View hView = navigationView.getHeaderView(0);
        TextView profile = (TextView) hView.findViewById(R.id.profile);
        profile.setText(name);

        TextView txEmail = (TextView) hView.findViewById(R.id.email);
        txEmail.setText(email);

        LinearLayout navBg = (LinearLayout) hView.findViewById(R.id.nav_bg);
        GradientDrawable gradientDrawableBlue = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#183152"), Color.parseColor("#375D81"), Color.parseColor("#ABC8E2")}); // Gradient Color Codes

        gradientDrawableBlue.setGradientType(GradientDrawable.LINEAR_GRADIENT); // Gradient Type
        navBg.setBackgroundDrawable(gradientDrawableBlue);

        if (imageUrl != null) {
            myImage = (ImageView) hView.findViewById(R.id.imageView);

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
                    .build();

            imageLoader.displayImage(imageUrl, myImage, options, new ImageLoadingListener() {

                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    dialog = new ProgressDialog(MainActivity.this);
                    dialog.setTitle("Patientez un instant SVP...");
                    dialog.setMessage("traitement en cours ");
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
        }

        //enableLocation();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mlocation = location;
                if (firstTime){
                    if(mlocation!=null){
                        if(fragmentClass==null || !fragmentClass.getSimpleName().equals("ChoiseFragment")){
                            fragmentClass = ChoiseFragment.class;
                            try {
                                Bundle bundle = new Bundle();
                                bundle.putInt("id_user", id_user);
                                bundle.putInt("id_trouver", id_trouver);
                                bundle.putDouble("lat",mlocation.getLatitude());
                                bundle.putDouble("lng",mlocation.getLongitude());
                                if(isAttendu){
                                    bundle.putInt("attendu",attendu);
                                }
                                fragment = (Fragment) fragmentClass.newInstance();
                                fragment.setArguments(bundle);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Handler handler = new Handler();
                            final Fragment finalFragment = fragment;
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    manager.beginTransaction()
                                            .setCustomAnimations(R.anim.slide_in_from_left,R.anim.slide_out_from_left)
                                            .replace(R.id.content_frame, finalFragment).commit();
                                }
                            }, 300);
                        }
                        firstTime = false;
                        dialog_location.dismiss();
                    }
                }


            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                new LovelyStandardDialog(MainActivity.this)
                        .setTopColorRes(R.color.colorPrimary)
                        .setButtonsColorRes(R.color.colorAccent)
                        .setIcon(R.drawable.ic_gps_not_fixed_black_24dp)
                        .setTitle("Localisation")
                        .setMessage("Vous devez activer la localisation pour utiliser cette application")
                        .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(myIntent);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_MAIN);
                                intent.addCategory(Intent.CATEGORY_HOME);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                //Kills all activities of app
                                System.exit(0);
                            }
                        }).show();
            }
        };

        getLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
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
            getLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    void getLocation() {
        // first check for permissions

        // this code won'textView execute IF permissions are not allowed, because in the line above there is return statement.
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else {
            dialog_location = new ProgressDialog(MainActivity.this);
            dialog_location.setTitle("Patientez un instant SVP...");
            dialog_location.setMessage("Localisation en cours ");
            dialog_location.setCancelable(false);
            dialog_location.show();
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
            //mlocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if(fragmentClass==null || !fragmentClass.getSimpleName().equals("ChoiseFragment")){
            fragmentClass = ChoiseFragment.class;
            try {
                Bundle bundle = new Bundle();
                bundle.putInt("id_user", id_user);
                bundle.putInt("id_trouver", id_trouver);
                bundle.putDouble("lat",mlocation.getLatitude());
                bundle.putDouble("lng",mlocation.getLongitude());
                if(isAttendu){
                    bundle.putInt("attendu",attendu);
                }
                fragment = (Fragment) fragmentClass.newInstance();
                fragment.setArguments(bundle);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Handler handler = new Handler();
            final Fragment finalFragment = fragment;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    manager.beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_from_left,R.anim.slide_out_from_left)
                            .replace(R.id.content_frame, finalFragment).commit();
                }
            }, 300);
        }
        else {
            if(userSessionManager.isTrouver()){
                Toast.makeText(this, "Vous devez annuler la recherche avant de quitter l'application !",
                        Toast.LENGTH_SHORT).show();
            }else if(userSessionManager.isDonner() && MapsFrag.now){
                Toast.makeText(this, "Vous devez annuler la recherche avant de quitter l'application !",
                        Toast.LENGTH_SHORT).show();
            }else{
                if (exit) {
                    //Takes user to home screen
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    //Kills all activities of app
                    System.exit(0);
                } else {
                    Toast.makeText(this, "Pressez encore une fois pour quiter.",
                            Toast.LENGTH_SHORT).show();
                    exit = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            exit = false;
                        }
                    }, 3 * 1000);

                }
            }

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.


        int id = item.getItemId();

        if (id == R.id.choix) {

            if(fragmentClass==null || !fragmentClass.getSimpleName().equals("ChoiseFragment")){
                fragmentClass = ChoiseFragment.class;
                try {
                    Bundle bundle = new Bundle();
                    bundle.putInt("id_user", id_user);
                    bundle.putInt("id_trouver", id_trouver);
                    bundle.putDouble("lat",mlocation.getLatitude());
                    bundle.putDouble("lng",mlocation.getLongitude());
                    if(isAttendu){
                        bundle.putInt("attendu",attendu);
                    }
                    fragment = (Fragment) fragmentClass.newInstance();
                    fragment.setArguments(bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Handler handler = new Handler();
                final Fragment finalFragment = fragment;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        manager.beginTransaction()
                                .setCustomAnimations(R.anim.slide_in_from_left,R.anim.slide_out_from_left)
                                .replace(R.id.content_frame, finalFragment).commit();
                    }
                }, 300);
            }

        } else if (id == R.id.car) {
            if(fragmentClass==null || !fragmentClass.getSimpleName().equals("CarsFragment")){
                fragmentClass = CarsFragment.class;
                try {
                    Bundle bundle = new Bundle();
                    bundle.putInt("id_user", id_user);
                    fragment = (Fragment) fragmentClass.newInstance();
                    fragment.setArguments(bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Handler handler = new Handler();
                final Fragment finalFragment = fragment;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        manager.beginTransaction()
                                .setCustomAnimations(R.anim.slide_in_from_left,R.anim.slide_out_from_left)
                                .replace(R.id.content_frame, finalFragment).commit();
                    }
                }, 300);
            }
        }else if (id == R.id.profile) {
            if(fragmentClass==null || !fragmentClass.getSimpleName().equals("ProfileFragment")){
                fragmentClass = ProfileFragment.class;
                try {
                    Bundle bundle = new Bundle();
                    bundle.putInt("id_user", id_user);
                    fragment = (Fragment) fragmentClass.newInstance();
                    fragment.setArguments(bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Handler handler = new Handler();
                final Fragment finalFragment = fragment;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        manager.beginTransaction()
                                .setCustomAnimations(R.anim.slide_in_from_left,R.anim.slide_out_from_left)
                                .replace(R.id.content_frame, finalFragment).commit();
                    }
                }, 300);
            }
        }
        else if (id == R.id.logout) {
            LoginManager.getInstance().logOut();
            userSessionManager.logoutUser();
        }
        else if (id == R.id.code_promo) {
            if(fragmentClass==null || !fragmentClass.getSimpleName().equals("PromoFragment")){
                fragmentClass = PromoFragment.class;
                try {
                    Bundle bundle = new Bundle();
                    bundle.putInt("id_user", id_user);
                    fragment = (Fragment) fragmentClass.newInstance();
                    fragment.setArguments(bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Handler handler = new Handler();
                final Fragment finalFragment = fragment;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        manager.beginTransaction()
                                .setCustomAnimations(R.anim.slide_in_from_left,R.anim.slide_out_from_left)
                                .replace(R.id.content_frame, finalFragment).commit();
                    }
                }, 300);
            }
        }
        else if (id == R.id.signaler) {
            if(fragmentClass==null || !fragmentClass.getSimpleName().equals("SignalerFragment")){
                fragmentClass = SignalerFragment.class;
                try {
                    Bundle bundle = new Bundle();
                    bundle.putInt("id_user", id_user);
                    fragment = (Fragment) fragmentClass.newInstance();
                    fragment.setArguments(bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Handler handler = new Handler();
                final Fragment finalFragment = fragment;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        manager.beginTransaction()
                                .setCustomAnimations(R.anim.slide_in_from_left,R.anim.slide_out_from_left)
                                .replace(R.id.content_frame, finalFragment).commit();
                    }
                }, 300);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(this.getSupportFragmentManager(), "dialog");
    }
}
