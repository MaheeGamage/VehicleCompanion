package com.example.android.vehiclecompanion.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.example.android.vehiclecompanion.R;
import com.example.android.vehiclecompanion.adapter.CustomListAdapter;
import com.example.android.vehiclecompanion.app.AppConfig;
import com.example.android.vehiclecompanion.app.MySingleton;
import com.example.android.vehiclecompanion.model.Branch;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ServiceAppointmentActivity extends AppCompatActivity {

    double lat,lng;

    // Log tag
    private static final String TAG = MainActivity.class.getSimpleName();

    // Branches json url
    private static final String url3 = "https://vehicle-companion.000webhostapp.com/Vehicle_Companion/location2.php";

    private ProgressDialog pDialog;
    private List<Branch> branchList = new ArrayList<Branch>();
    private ListView listView;
    private CustomListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_appointment);

        // get info from intent
        Bundle bundle = getIntent().getExtras();
        final double latitude = bundle.getDouble("lat");
        final double longtitude = bundle.getDouble("lng");

        lat = latitude;
        lng = longtitude;

        listView = (ListView) findViewById(R.id.list);
        adapter = new CustomListAdapter(this, branchList);
        listView.setAdapter(adapter);

        pDialog = new ProgressDialog(this);

        // Showing progress dialog before making http request
        pDialog.setMessage("Loading...");
        pDialog.show();

        // changing action bar color
//        getActionBar().setBackgroundDrawable(
//                new ColorDrawable(Color.parseColor("#1b1b1b")));

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.URL_SERVICE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);
                hidePDialog();

                try {
                    JSONArray jarr = new JSONArray(response);

                    for(int i=0;i<jarr.length();i++) {
                        JSONObject obj = jarr.getJSONObject(i);
                        Branch branch = new Branch();
                        branch.setId(obj.getString("id"));
                        branch.setName(obj.getString("name"));
                        branch.setLatitude(Double.parseDouble(obj.getString("latitude")));
                        branch.setLongitude(Double.parseDouble(obj.getString("longitude")));
                        branch.setLocation(obj.getString("location"));
                        branchList.add(branch);
                    }

                } catch (Throwable tx) {
                    Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                    Log.e("ERR", tx.getMessage());
                }

                adapter.notifyDataSetChanged();
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
                params.put("lat",String.valueOf(lat));
                params.put("lng",String.valueOf(lng));
                return params;
            }
        };

        // Adding request to request queue
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Branch branch = branchList.get(position);
                // retrieve from movie whatever you want
                branch.getId();
                Toast.makeText(getApplicationContext(),"id: "+branch.getId(), Toast.LENGTH_LONG).show();
                // create intent and add data to it.
                Intent intent = new Intent(ServiceAppointmentActivity.this,ServiceDetailsActivity.class);
                intent.putExtra("id",branch.getId());
                intent.putExtra("name",branch.getName());
                intent.putExtra("latitude",Double.toString(branch.getLatitude()) );
                intent.putExtra("longitude", Double.toString(branch.getLongitude()) );
                intent.putExtra("location",branch.getLocation());

                startActivity(intent);
            }
        });
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
