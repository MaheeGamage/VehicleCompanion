package com.example.android.vehiclecompanion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.example.android.vehiclecompanion.activity.MainActivity;
import com.example.android.vehiclecompanion.app.Config;
import com.example.android.vehiclecompanion.app.MySingleton;
import com.example.android.vehiclecompanion.model.Branch;
import com.example.android.vehiclecompanion.util.NotificationUtils;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TestActivity extends AppCompatActivity {

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    Button btnSendNotification,btnGetFirebaseId;

    TextView txtFire,txtFire2;

    // Log tag
    private static final String TAG = MainActivity.class.getSimpleName();

    // Branches json url
    private static final String url3 = "https://vehicle-companion.000webhostapp.com/firebase/firebase_request.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        btnSendNotification = (Button)findViewById(R.id.btnSendNotification);
        btnGetFirebaseId = (Button)findViewById(R.id.btnGetFirebaseId);
        txtFire = (TextView)findViewById(R.id.txtFire);
        txtFire2 = (TextView)findViewById(R.id.txtFire2);

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

                    txtFire.setText(message);
                }
            }
        };

        displayFirebaseRegId();

/*************************************************************************************/

        btnSendNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnTask();
            }
        });

        btnGetFirebaseId.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
                String regId = pref.getString("regId", null);

                Log.e(TAG, "Firebase reg id: " + regId);

                Toast.makeText(getApplicationContext(),regId,Toast.LENGTH_LONG).show();
            }
        }));

    }

    public void btnTask(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST,url3, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Log.d(TAG, error.getMessage());
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),"Err "+error.getMessage(),Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("title","Android Test");
                params.put("message","Android test message for All");
                params.put("push_type","topic");
                return params;
            }
        };

        // Adding request to request queue
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);

        Log.e(TAG, "Firebase reg id: " + regId);

        if (!TextUtils.isEmpty(regId))
            txtFire2.setText("Firebase Reg Id: " + regId);
        else
            txtFire2.setText("Firebase Reg Id is not received yet!");
    }

    @Override
    protected void onResume() {
        super.onResume();

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
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }
}
