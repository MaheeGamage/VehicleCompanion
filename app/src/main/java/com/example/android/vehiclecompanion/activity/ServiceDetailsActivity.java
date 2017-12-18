package com.example.android.vehiclecompanion.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.example.android.vehiclecompanion.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;

import android.support.v7.app.AlertDialog;

import com.example.android.vehiclecompanion.app.AppConfig;
import com.example.android.vehiclecompanion.helper.SessionManager;
import com.example.android.vehiclecompanion.model.Branch;
import com.example.android.vehiclecompanion.app.MySingleton;

public class ServiceDetailsActivity extends AppCompatActivity implements OnItemSelectedListener {

    private ProgressDialog pDialog;

    //For Date Picker
    private DatePicker datePicker;
    private Calendar calendar;
    private TextView dateView;
    private int year, month, day;

    //Time & Date
    private int time,timeReceived;
    private Integer timeSelected = 0;
    AlertDialog alertDialog1;

    //For Map
    private Double latitude = (double)0, longitude = (double)0;

    //For Dialog Box
    CharSequence[] values = {"AM","PM"};

    final String TAG = this.getClass().getSimpleName();
    TextView txtName,txtLocation;
    Button btnConfirmService,btnGetLocation,btnSelectDate;

    String url = "https://vehicle-companion.000webhostapp.com/Vehicle_Companion/getBranchDetails.php";
    String url2 = "https://vehicle-companion.000webhostapp.com/Vehicle_Companion/placeOrder.php";
    String url3 = "https://vehicle-companion.000webhostapp.com/Vehicle_Companion/AppointmentTime.json";

    //For Confirm Data
    String Package, Date,Id, branchName, branchLocation, sqlDate = "", appTime, packageName;

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_details);

        // get info from intent
        Bundle bundle = getIntent().getExtras();
        final String id = bundle.getString("id");
        final String name = bundle.getString("name");
        final String lat = bundle.getString("latitude");
        final String lng = bundle.getString("longitude");
        final String location = bundle.getString("location");

        Log.d(TAG, id);

        Id = id;
        branchName = name;
        branchLocation = location;
        latitude = Double.parseDouble(lat);
        longitude = Double.parseDouble(lng);

        txtName = (TextView)findViewById(R.id.txtBranchName);
        txtLocation = (TextView)findViewById(R.id.txtBranchLocation);
        btnSelectDate = (Button)findViewById(R.id.btnSelectDate);
        btnConfirmService = (Button)findViewById(R.id.btnConfirmService);
        btnGetLocation = (Button)findViewById(R.id.btnShowLocation);

        txtName.setText(branchName);
        txtLocation.setText(branchLocation);

        //Date Picker
        dateView = (TextView) findViewById(R.id.txtViewDate);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        // session manager
        session = new SessionManager(getApplicationContext());

        btnSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDate();
            }
        });

        btnConfirmService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (false/*Package != null && !Package.isEmpty()*/)
//                    Toast.makeText(getApplicationContext(), "Select a Package P:"+Package, Toast.LENGTH_SHORT).show();
                if (sqlDate == "")
                    Toast.makeText(getApplicationContext(), "Select a Date", Toast.LENGTH_SHORT).show();
                else if (timeSelected==0)
                    Toast.makeText(getApplicationContext(), "Select a Time", Toast.LENGTH_SHORT).show();
                else
                    confirmService();
            }
        });

        btnGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocation();
            }
        });

/*************************************************      Spinner     *************************************************/
        // Spinner element
        Spinner spnSelectPackage = (Spinner) findViewById(R.id.spnSelectPackage);

        // Spinner click listener
        spnSelectPackage.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("Full Service");
        categories.add("Interior Cleaning");
        categories.add("Exterior Cleaning");
        categories.add("Total Cleaning");
        categories.add("Waxing");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spnSelectPackage.setAdapter(dataAdapter);

/*******************************************************************************************************************/

    }

    @SuppressWarnings("deprecation")
    public void setDate() {
        showDialog(999);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 999) {
            return new DatePickerDialog(this,
                    myDateListener, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    // TODO Auto-generated method stub
                    // arg1 = year
                    // arg2 = month
                    // arg3 = day
                    showDate(arg1, arg2+1, arg3);
                    Toast.makeText(getApplicationContext(), arg3+"/"+arg2+"/"+arg1, Toast.LENGTH_SHORT).show();
                    timeSelected = 0;
                    checkDate();
                }
            };

    private void showDate(int year, int month, int day) {
        dateView.setText(new StringBuilder().append(day).append("/").append(month).append("/").append(year));
        Date = day+"/"+month+"/"+year;
        sqlDate = year+"-"+month+"-"+day;
    }

    public void showLocation(){
        Intent intent = new Intent(ServiceDetailsActivity.this,BranchLocationActivity.class);
        intent.putExtra("lat",latitude.toString());
        intent.putExtra("lng",longitude.toString());
        intent.putExtra("centerName",branchName);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();
        packageName = item;

        // Showing selected spinner item
        if(item.equals("Full Service"))
            Package = "full";
        else if(item.equals("Interior Cleaning"))
            Package = "interior";
        else if(item.equals("Exterior Cleaning"))
            Package = "exterior";
        else if(item.equals("Total Cleaning"))
            Package = "total";
        else if(item.equals("Waxing"))
            Package = "wax";

//        Toast.makeText(parent.getContext(), "Selected: " + Package, Toast.LENGTH_SHORT).show();
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    public void confirmService(){

        String timeSelectedDisplay = "Not selected";
        if(timeSelected == 1)
            timeSelectedDisplay = "8am";
        else if(timeSelected == 2)
            timeSelectedDisplay = "12pm";

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ServiceDetailsActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("Confirm Service Appointment");

        // Setting Dialog Message
        alertDialog.setMessage("Service center: " +branchName+"\n"+"Package: " +packageName+"\n"+"Date: " +Date+"\n"+"Time: " +timeSelectedDisplay);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {

                // Write your code here to invoke YES event
//                Toast.makeText(getApplicationContext(), "You clicked on YES", Toast.LENGTH_SHORT).show();

                if(timeSelected == 1)
                    appTime = "8am";
                else if(timeSelected == 2)
                    appTime = "12pm";

                pDialog = new ProgressDialog(ServiceDetailsActivity.this);

                // Showing progress dialog before making http request
                pDialog.setMessage("Loading...");
                pDialog.show();

                StringRequest stringRequest2 = new StringRequest(Request.Method.POST, AppConfig.URL_APPOINTMENT, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("diske2", response);
                        hidePDialog();
//                        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();

                        try {
                            JSONObject jObj = new JSONObject(response);
                            boolean error = jObj.getBoolean("error");

                            if(!error){
                                Toast.makeText(getApplicationContext(), "Appointment successful", Toast.LENGTH_SHORT).show();
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
                        params.put("type",Package);
                        params.put("date",sqlDate);
                        params.put("time",appTime);
                        params.put("user_id",String.valueOf(session.getUserId()));
                        params.put("branch_id",Id);
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

    public void selcetTime(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ServiceDetailsActivity.this);

        builder.setTitle("Select Your Choice");

        builder.setSingleChoiceItems(values, -1, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {
                if(timeReceived == 3){
                    timeSelected = 0;
                    Toast.makeText(ServiceDetailsActivity.this, "All Day Booked. Select another day", Toast.LENGTH_SHORT).show();
                }
                else {
                    switch (item) {
                        case 0:
                            timeSelected = 1;
                            break;
                        case 1:
                            timeSelected = 2;
                            break;
                    }
                    if(timeSelected == timeReceived) {
                        alertDialog1.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false); //BUTTON1 is positive button
                        Toast.makeText(ServiceDetailsActivity.this, "That time booked. Select another Time", Toast.LENGTH_SHORT).show();
                    }
                    else
                        alertDialog1.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true); //BUTTON1 is positive button
                }
                //alertDialog1.dismiss();
            }
        });

        builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if(timeSelected == 1);
//                    Toast.makeText(ServiceDetailsActivity.this, "8am - 12pm Clicked", Toast.LENGTH_LONG).show();
                if(timeSelected == 2);
//                    Toast.makeText(ServiceDetailsActivity.this, "12pm - 4pm Clicked", Toast.LENGTH_LONG).show();
                //DO TASK
            }
        });

        builder.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.cancel();
            }
        });

        alertDialog1 = builder.create();
        alertDialog1.show();

        alertDialog1.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false); //BUTTON1 is positive button

        if(timeReceived == 3) {
            Toast.makeText(ServiceDetailsActivity.this, "All Day Booked. Select another day", Toast.LENGTH_SHORT).show();
        }
    }

    public void checkDate(){
        pDialog = new ProgressDialog(this);

        // Showing progress dialog before making http request
        pDialog.setMessage("Loading...");
        pDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.URL_CHECK_TIME, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("diske1", response);
                hidePDialog();
//                ToastEasy(response);
                try {
                    JSONObject object = new JSONObject(response);
                    boolean error = object.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        timeReceived = object.getInt("time");
//                    if(object.getString("am").equals("1") && object.getString("pm").equals("1"))
//                        timeReceived = 3;
//                    else if(object.getString("am").equals("1"))
//                        timeReceived = 1;
//                    else if(object.getString("pm").equals("1"))
//                        timeReceived = 2;
//                    else
//                        timeReceived = 0;
                    }
                    else {
                        // Error in login. Get the error message
                        String errorMsg = object.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                ToastEasy(Integer.toString(timeReceived));
                if(timeReceived==3)
                    ToastEasy("All Day Booked. Select another day");
                else
                    selcetTime();

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
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("branch_id", Id);
                params.put("date", sqlDate);
                return params;
            }

        };

        // Adding request to request queue
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
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

    public void ToastEasy(String s){
        Toast.makeText(ServiceDetailsActivity.this, s, Toast.LENGTH_SHORT).show();
    }
}
