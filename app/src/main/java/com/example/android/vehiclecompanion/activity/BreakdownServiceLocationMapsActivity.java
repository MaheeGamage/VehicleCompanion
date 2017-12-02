package com.example.android.vehiclecompanion.activity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.example.android.vehiclecompanion.R;
import com.example.android.vehiclecompanion.app.MySingleton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BreakdownServiceLocationMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public Marker mUserLocation;

    //For ProgressDialog
    private ProgressDialog pDialog;

    // Log tag
    private static final String TAG = MainActivity.class.getSimpleName();

    // Branches json url
    private static final String url3 = "https://vehicle-companion.000webhostapp.com/Vehicle_Companion/location2.php";

    //
    double latUserLocation,lngUserLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breakdown_service_location_maps);

        pDialog = new ProgressDialog(this);

        // Showing progress dialog before making http request
        pDialog.setMessage("Loading...");
        pDialog.show();

        // get info from intent
        Bundle bundle = getIntent().getExtras();
        final double latitude = bundle.getDouble("lat");
        final double longtitude = bundle.getDouble("lng");
        latUserLocation = latitude;
        lngUserLocation = longtitude;

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng userLocation = new LatLng(latUserLocation, lngUserLocation);
        mUserLocation = mMap.addMarker(new MarkerOptions().position(userLocation).title("You are here"));
        fetchLocations();

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,12));
    }

    public void fetchLocations(){


        StringRequest stringRequest = new StringRequest(Request.Method.POST,url3, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);
                hidePDialog();

                try {
                    JSONArray jarr = new JSONArray(response);

                    for(int i=0;i<jarr.length();i++) {
                        JSONObject obj = jarr.getJSONObject(i);
                        LatLng latLng = new LatLng(obj.getDouble("latitude"), obj.getDouble("longitude"));
                        mMap.addMarker(new MarkerOptions()
//                                .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("branch_icon",72,64)))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                .position(latLng));
                    }

                } catch (Throwable tx) {
                    Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Log.d(TAG, error.getMessage());
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),"Err "+error.getMessage(),Toast.LENGTH_LONG).show();
                hidePDialog();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
//                params.put("lat",String.valueOf(lat));
//                params.put("lng",String.valueOf(lng));
                params.put("lat",String.valueOf(latUserLocation));
                params.put("lng",String.valueOf(lngUserLocation));
                return params;
            }
        };

        // Adding request to request queue
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    public Bitmap resizeBitmap(String drawableName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(drawableName, "drawable", getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hidePDialog();
    }

    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }
}
