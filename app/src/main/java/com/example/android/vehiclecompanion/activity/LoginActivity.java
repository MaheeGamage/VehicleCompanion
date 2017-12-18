package com.example.android.vehiclecompanion.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


import com.example.android.vehiclecompanion.app.AppConfig;
import com.example.android.vehiclecompanion.app.AppController;
import com.example.android.vehiclecompanion.app.Config;
import com.example.android.vehiclecompanion.app.MySingleton;
import com.example.android.vehiclecompanion.helper.SQLiteHandler;
import com.example.android.vehiclecompanion.helper.SessionManager;
import com.example.android.vehiclecompanion.R;
import com.example.android.vehiclecompanion.model.Branch;
import com.example.android.vehiclecompanion.model.Document;
import com.example.android.vehiclecompanion.model.User;
import com.example.android.vehiclecompanion.model.Vehicle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();

//    private static final String TAG = "Server Response";

    private Button btnLogin;
    private Button btnLinkToRegister;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;

    String firebase_regId;

    SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                // Check for empty data in the form
                if (!email.isEmpty() && !password.isEmpty()) {
                    // login user
                    checkLogin(email, password);
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                            "Please enter the credentials!", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });

        // Link to Register Screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    private void checkLogin(final String email, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        firebase_regId = pref.getString("regId", null);



        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        // user successfully logged in
                        // Create login session

                        JSONObject userObj = jObj.getJSONObject("user");
                        int id = jObj.getInt("id");
                        String name = userObj.getString("name");
                        String email = userObj.getString("email");
                        Boolean owner = (Integer.parseInt(userObj.getString("owner")) > 0);
                        String phone_no = userObj.getString("phone_no");

                        JSONArray jarr = new JSONArray(jObj.getString("selected_user"));

                        for(int i=0;i<jarr.length();i++) {
                            JSONObject obj = jarr.getJSONObject(i);
                            User suser = new User();
                            suser.setId(obj.getInt("id"));
                            suser.setEmail(obj.getString("email"));

                            db = new SQLiteHandler(getApplicationContext());
                            db.addSelcetedUser(suser);
                        }

                        if(userObj.getInt("owner")==1) {
                            JSONObject vehicleObj = jObj.getJSONObject("vehicle");
                            Vehicle vehicle = new Vehicle(vehicleObj.getInt("id"), vehicleObj.getString("model"), vehicleObj.getString("reg_no"));

                            JSONObject licenseObj = jObj.getJSONObject("license");
                            Document license = new Document(licenseObj.getInt("id"), licenseObj.getString("expiry_date"), 1);

                            JSONObject insuranceObj = jObj.getJSONObject("insurance");
                            Document insurance = new Document(insuranceObj.getInt("id"), insuranceObj.getString("expiry_date"), 2);



                            Log.d(TAG, "Insurance exp: " + insurance.getExpiry_date());

                            session.setLogin(true, vehicle, license, insurance);
                        }

                            User user = new User(id, name, owner, email, phone_no);
                            session.setLogin(true, user);

                        // Launch main activity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
//                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Login Response: " + e.getMessage());
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);
                params.put("fid", firebase_regId);
                return params;
            }

        };

        // Adding request to request queue
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(strReq);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
