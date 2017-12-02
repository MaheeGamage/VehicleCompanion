package com.example.android.vehiclecompanion.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.android.vehiclecompanion.R;
import com.example.android.vehiclecompanion.TestActivity;
import com.example.android.vehiclecompanion.app.Config;
import com.example.android.vehiclecompanion.helper.SessionManager;
import com.example.android.vehiclecompanion.util.NotificationUtils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessaging;

import android.view.View.OnClickListener;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, NavigationView.OnNavigationItemSelectedListener {

    Button btnActivityService, btnActivityBreakdown,btnLastParkedLocation/*,btnTest,btnTest2*/;

    //For Firebase
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private SessionManager session;

    //Requesting Permission
    private static final int ACCESS_FINE_LOCATION_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;

/*********************************  Location *********************************/
    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters
/*****************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        btnActivityService = (Button)findViewById(R.id.btnActivityService);
        btnActivityBreakdown = (Button)findViewById(R.id.btnActivityBreakdown);
        btnLastParkedLocation = (Button)findViewById(R.id.btnLastParked);
//        btnTest = (Button)findViewById(R.id.btnTest);
//        btnTest2 = (Button)findViewById(R.id.btnTest2);

        //for Location Permission
        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);

        //Check availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
            createLocationRequest();
        }

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

/*********************************  Firbase     **********************************/

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);

                    displayFirebaseRegId();

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received

                    String message = intent.getStringExtra("message");

                    Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();

//                    txtMessage.setText(message);
                }
            }
        };

        displayFirebaseRegId();

/*************************************************************************************/

        btnActivityService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityService();
            }
        });

        btnActivityBreakdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityBreakdown();
            }
        });

        btnLastParkedLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastParkedLocation();
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_order_vehicle) {
            // Handle the camera action
        } else if (id == R.id.nav_document) {

        } else if (id == R.id.nav_spareparts) {

        } else if (id == R.id.nav_video_tut) {

        } else if (id == R.id.nav_offers) {

        } else if (id == R.id.nav_setting) {

        } else if (id == R.id.nav_test) {
            Intent intent = new Intent(getApplicationContext(), TestActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);

        Log.e(TAG, "Firebase reg id: " + regId);

//        if (!TextUtils.isEmpty(regId))
//            txtRegId.setText("Firebase Reg Id: " + regId);
//        else
//            txtRegId.setText("Firebase Reg Id is not received yet!");
    }

//    public void activityService(){
//
//        GpsTracker gt = new GpsTracker(getApplicationContext());
//        Location l = gt.getLocation();
//        if( l == null){
//            Toast.makeText(getApplicationContext(),"GPS unable to get Value",Toast.LENGTH_SHORT).show();
//        }else {
//            double lat = l.getLatitude();
//            double lon = l.getLongitude();
//            Toast.makeText(getApplicationContext(),"GPS Lat = "+lat+"\n lon = "+lon,Toast.LENGTH_SHORT).show();
//        }
//
//        Intent intent = new Intent(getApplicationContext(), ServiceAppointmentActivity.class);
////        intent.putExtra("lat",l.getLatitude());
////        intent.putExtra("lng",l.getLongitude());
//        intent.putExtra("lat",6.802235);
//        intent.putExtra("lng",79.8897987);
//        startActivity(intent);
//    }

    private void activityService() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            getPermission();
        else
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

//            lblLocation.setText(latitude + ", " + longitude);
            Toast.makeText(this,latitude + ", " + longitude,Toast.LENGTH_LONG).show();

            Intent intent = new Intent(getApplicationContext(), ServiceAppointmentActivity.class);
            intent.putExtra("lat",latitude);
            intent.putExtra("lng",longitude);
            startActivity(intent);

        } else {

//            lblLocation.setText("(Couldn't get the location. Make sure location is enabled on the device)");
            Toast.makeText(this,"Couldn't get the location. Make sure location is enabled on the device",Toast.LENGTH_LONG).show();
        }
    }

    private void activityBreakdown() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            getPermission();
        else
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

//            lblLocation.setText(latitude + ", " + longitude);
            Toast.makeText(this,latitude + ", " + longitude,Toast.LENGTH_LONG).show();

            Intent intent = new Intent(getApplicationContext(), BreakdownServiceLocationMapsActivity.class);
            intent.putExtra("lat",latitude);
            intent.putExtra("lng",longitude);

            startActivity(intent);

        } else {

//            lblLocation.setText("(Couldn't get the location. Make sure location is enabled on the device)");
            Toast.makeText(this,"Couldn't get the location. Make sure location is enabled on the device",Toast.LENGTH_LONG).show();
        }
    }

    public void lastParkedLocation(){
        // Create custom dialog object
        final Dialog dialog = new Dialog(MainActivity.this);
        // Include dialog.xml file
        dialog.setContentView(R.layout.custom_dialog);
        // Set dialog title
        dialog.setTitle("Custom Dialog");

        // set values for custom dialog components - text, image and button

        dialog.show();

        Button getLocation = (Button) dialog.findViewById(R.id.btnGetLocation);
        Button saveLocation = (Button) dialog.findViewById(R.id.btnSaveLocation);

        // if decline button is clicked, close the custom dialog
        getLocation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                session = new SessionManager(getApplicationContext());
                String lat = session.getLatLastParked(), lng = session.getLngLastParked();
                Toast.makeText(getApplicationContext(),"Last Parked Location is " + lat+" "+lng ,Toast.LENGTH_SHORT).show();
                dialog.dismiss();

                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", Float.parseFloat(lat), Float.parseFloat(lng));
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
            }
        });
        saveLocation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // session manager
                session = new SessionManager(getApplicationContext());
                session.setLastParked(displayLocation());
                Toast.makeText(getApplicationContext(),"Location Saved!",Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }



    private void logoutUser() {
        session.setLogin(false, null);

        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

/*************************************************  Location Retrieve    ********************************************************/
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();

        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
//            startLocationUpdates();

        }

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
        stopLocationUpdates();
    }

    /**
     * Method to display the location on UI
     * */
    private LatLng displayLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            getPermission();
            return null;
        }
        else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLastLocation != null) {
                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();

//                lblLocation.setText(latitude + ", " + longitude);
//                Toast.makeText(getBaseContext(), latitude + ", " + longitude, Toast.LENGTH_LONG).show();


                LatLng latLng = new LatLng(latitude,longitude);
                return latLng;

            } else {

//                lblLocation.setText("(Couldn't get the location. Make sure location is enabled on the device)");
                return null;

            }
        }


    }

    /**
     * Method to toggle periodic location updates
     * */
    private void togglePeriodicLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            getPermission();
        else {
            if (!mRequestingLocationUpdates) {
                // Changing the button text
//                btnStartLocationUpdates.setText(getString(R.string.btn_stop_location_updates));

                mRequestingLocationUpdates = true;

                // Starting the location updates
                startLocationUpdates();

                Log.d(TAG, "Periodic location updates started!");

            } else {
                // Changing the button text
//                btnStartLocationUpdates.setText(getString(R.string.btn_start_location_updates));

                mRequestingLocationUpdates = false;

                // Stopping the location updates
                stopLocationUpdates();

                Log.d(TAG, "Periodic location updates stopped!");
            }
        }
    }

    private void startPeriodicLocationUpdates() {
        Toast.makeText(getBaseContext(), "Diske", Toast.LENGTH_SHORT).show();

        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            getPermission();
        else {
            // Changing the button text
//          btnStartLocationUpdates.setText(getString(R.string.btn_stop_location_updates));

            mRequestingLocationUpdates = true;

            // Starting the location updates
            startLocationUpdates();

            Log.d(TAG, "Periodic location updates started!");


        }
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            getPermission();
        else {
            mRequestingLocationUpdates = true;
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            Toast.makeText(this, "Location Service Started", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
//        displayLocation();
        startPeriodicLocationUpdates();

        if (mRequestingLocationUpdates) {
//            startLocationUpdates();

        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

        Toast.makeText(getApplicationContext(), "Location changed!", Toast.LENGTH_SHORT).show();

        // Displaying the new location on UI
//        displayLocation();
    }

    public void getPermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            //Show Information about why you need the permission
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Need Storage Permission");
            builder.setMessage("This app needs storage permission.");
            builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_CONSTANT);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        } else if (permissionStatus.getBoolean(android.Manifest.permission.ACCESS_FINE_LOCATION,false)) {
            //Previously Permission Request was cancelled with 'Dont Ask Again',
            // Redirect to Settings after showing Information about why you need the permission
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Need Storage Permission");
            builder.setMessage("This app needs storage permission.");
            builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    sentToSettings = true;
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                    Toast.makeText(getBaseContext(), "Go to Permissions to Grant Storage", Toast.LENGTH_LONG).show();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        } else {
            //just request the permission
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_CONSTANT);
        }

        SharedPreferences.Editor editor = permissionStatus.edit();
        editor.putBoolean(android.Manifest.permission.ACCESS_FINE_LOCATION,true);
        editor.commit();

    }


}
