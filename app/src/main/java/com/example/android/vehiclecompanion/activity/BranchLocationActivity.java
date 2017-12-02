package com.example.android.vehiclecompanion.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.example.android.vehiclecompanion.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.view.View;
import android.widget.Toast;

public class BranchLocationActivity extends FragmentActivity
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    double latitude = (double)0, longitude = (double)0;
    String centerName;

    private GoogleMap mMap;
    private LatLng TOYOTA;

    private Marker mToyota;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branch_location);

        // get info from intent
        Bundle bundle = getIntent().getExtras();
        final String lat = bundle.getString("lat");
        final String lng = bundle.getString("lng");
        final String marker = bundle.getString("centerName");
        centerName = marker;
        TOYOTA = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        latitude = Double.parseDouble(lat);
        longitude = Double.parseDouble(lng);
//        TOYOTA = new LatLng(6.802235, 79.8897987);

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

        // Add lots of markers to the map.
        addMarkersToMap();

        // Set listeners for marker events.  See the bottom of this class for their behavior.
        mMap.setOnMarkerClickListener(this);

        // Override the default content description on the view, for accessibility mode.
        // Ideally this string would be localised.
        mMap.setContentDescription("Selected Map Location");

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(TOYOTA,10));
    }

    private void addMarkersToMap() {
        // Uses a colored icon.
        mToyota = mMap.addMarker(new MarkerOptions()
                .position(TOYOTA)
                .title("Toyota")
                .snippet("Population: 2,074,200"));
    }

    private boolean checkReady() {
        if (mMap == null) {
            Toast.makeText(this, "Map is NOT Ready yet", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /** Called when the Clear button is clicked. */
    public void onClearMap(View view) {
        if (!checkReady()) {
            return;
        }
        mMap.clear();
    }

    /** Called when the Reset button is clicked. */
    public void onResetMap(View view) {
        if (!checkReady()) {
            return;
        }
        // Clear the map because we don't want duplicates of the markers.
        mMap.clear();
        addMarkersToMap();
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        if (marker.equals(mToyota)) {
            Toast.makeText(this,"Diske",Toast.LENGTH_SHORT).show();
        }

        return false;
    }
}



