package com.example.android.vehiclecompanion.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.example.android.vehiclecompanion.R;
import com.example.android.vehiclecompanion.app.AppConfig;
import com.example.android.vehiclecompanion.app.MySingleton;
import com.example.android.vehiclecompanion.helper.SQLiteHandler;
import com.example.android.vehiclecompanion.helper.SessionManager;
import com.example.android.vehiclecompanion.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectedUserActivity extends AppCompatActivity {

    private ProgressDialog pDialog;

    private Button btnAddFriend, btnRemoveAll;
    private ListView list;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;

    private SessionManager session;

    private String userEmail;

    final String TAG = this.getClass().getSimpleName();

    SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_user);

        // session manager
        session = new SessionManager(getApplicationContext());

        btnAddFriend = (Button) findViewById(R.id.btnAddFriend);
        btnRemoveAll = (Button) findViewById(R.id.btnRemoveAll);
        list = (ListView) findViewById(R.id.listSelectedUserList);
        arrayList = new ArrayList<String>();

        // Adapter: You need three parameters 'the context, id of the layout (it will be where the data is shown),
        // and the array that contains the data
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, arrayList);

        // Here, you set the data in your ListView
        list.setAdapter(adapter);

        getFromDatabase();

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                // TODO Auto-generated method stub

                // ListView Clicked item index
                int itemPosition = pos;

                // ListView Clicked item value
                userEmail = (String) list.getItemAtPosition(pos);

                Log.v("long clicked","pos: " + pos);
//                Toast.makeText(getApplicationContext(), userEmail,Toast.LENGTH_SHORT).show();
                deleteUser();

                return true;
            }
        });

        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend();
            }
        });

        btnRemoveAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getFromDatabase();
//
//                if( arrayList.size() >= 1) {
//                    adapter.remove(arrayList.get(0));
//                    adapter.notifyDataSetChanged();
//                }

            }
        });


//        db.deleteSelectedUser();

        /*User u1 = new User(105, "hello@fdakj.com");
        User u2 = new User(106, "diske@fdakj.com");

        // Inserting Contacts
        Log.d("Insert: ", "Inserting ..");
        db.addSelcetedUser(u1);
        db.addSelcetedUser(u2);

        // Reading all contacts
        Log.d("Reading: ", "Reading all contacts..");
        List<User> user = db.getAllSelectedUser();


        for (User cn : user) {
            String log = "Id: "+cn.getId()+" ,Email: " + cn.getEmail();
            // Writing Contacts to log
            Log.d("Selected_Users: ", log);*/


    }

    public void addFriend(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SelectedUserActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("Add a Friend");

        final EditText input = new EditText(SelectedUserActivity.this);
        input.setHeight(100);
        input.setWidth(340);
        input.getCompoundPaddingLeft();
        input.setGravity(Gravity.START);

        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        alertDialog.setView(input);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {

                db = new SQLiteHandler(getApplicationContext());

                userEmail = input.getText().toString().trim();

                // Write your code here to invoke YES event
//                Toast.makeText(getApplicationContext(), "You clicked on YES", Toast.LENGTH_SHORT).show();

                pDialog = new ProgressDialog(SelectedUserActivity.this);

                // Showing progress dialog before making http request
                pDialog.setMessage("Loading...");
                pDialog.show();

                StringRequest stringRequest2 = new StringRequest(Request.Method.POST, AppConfig.URL_SELECTED_USER, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("diske2", response);
                        hidePDialog();
//                        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();

                        try {
                            JSONObject userObj = new JSONObject(response);
                            boolean error = userObj.getBoolean("error");

                            if(!error){

                                int id = userObj.getInt("id");
                                String email = userObj.getString("email");

                                User user = new User(id, email);

                                // Inserting Contacts
                                db.addSelcetedUser(user);

                                getFromDatabase();

                            }
                            else
                                Toast.makeText(getApplicationContext(), userObj.getString("error_msg"), Toast.LENGTH_SHORT).show();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        Log.d(TAG, error.getMessage());
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                        Toast.makeText(getApplicationContext(),"Err "+error.getMessage(),Toast.LENGTH_LONG).show();
                        hidePDialog();
                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String> params = new HashMap<>();
                        params.put("email",userEmail);
                        params.put("uid",String.valueOf(session.getUserId()));
                        return params;
                    }
                };
                MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest2);

            }
        });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to invoke NO event
//                Toast.makeText(getApplicationContext(), "You clicked on NO", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    public void getFromDatabase(){

        arrayList.clear();
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, arrayList);
        list.setAdapter(adapter);

        db = new SQLiteHandler(getApplicationContext());

        // Reading all contacts
        Log.d("Reading: ", "Reading all contacts..");
        List<User> user = db.getAllSelectedUser();

        for (User cn : user) {
            String log = "Id: " + cn.getId() + " ,Email: " + cn.getEmail();
            // Writing Contacts to log
            Log.d("Selected_Users: ", log);

            // this line adds the data of your EditText and puts in your array
            arrayList.add(cn.getEmail());
            // next thing you have to do is check if your adapter has changed
            adapter.notifyDataSetChanged();
        }

    }

    public void deleteUser(){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SelectedUserActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("Confirm Service Appointment");

        // Setting Dialog Message
        alertDialog.setMessage("Do you want to delete this user?");

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {

                pDialog = new ProgressDialog(SelectedUserActivity.this);

                // Showing progress dialog before making http request
                pDialog.setMessage("Loading...");
                pDialog.show();

                StringRequest stringRequest2 = new StringRequest(Request.Method.POST, AppConfig.URL_DELETE_SELECTED_USER, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("delete_user_response", response);
                        hidePDialog();

                        try {
                            JSONObject jObj = new JSONObject(response);
                            boolean error = jObj.getBoolean("error");

                            if(!error){
                                User user = new User();
                                user.setId(jObj.getInt("id"));
                                int res = db.deleteSelectedUser(user);
                                if(res == 1)
                                    Toast.makeText(getApplicationContext(), "User deleted successfully", Toast.LENGTH_SHORT).show();
                                getFromDatabase();
                            }
                            else
                                Toast.makeText(getApplicationContext(), jObj.getString("error_msg"), Toast.LENGTH_SHORT).show();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        Log.d(TAG, error.getMessage());
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                        Toast.makeText(getApplicationContext(),"Err "+error.getMessage(),Toast.LENGTH_LONG).show();
                        hidePDialog();
                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String> params = new HashMap<>();
                        params.put("email",userEmail);
                        params.put("uid",String.valueOf(session.getUserId()));
                        return params;
                    }
                };
                MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest2);
                getFromDatabase();

            }
        });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to invoke NO event
//                Toast.makeText(getApplicationContext(), "You clicked on NO", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
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
