package com.example.android.vehiclecompanion.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.example.android.vehiclecompanion.model.Document;
import com.example.android.vehiclecompanion.model.User;
import com.example.android.vehiclecompanion.model.Vehicle;
import com.google.android.gms.maps.model.LatLng;

public class SessionManager {
	// LogCat tag
	private static String TAG = SessionManager.class.getSimpleName();

	// Shared Preferences
	SharedPreferences pref;

	Editor editor;
	Context _context;

	// Shared pref mode
	int PRIVATE_MODE = 0;

	// Shared preferences file name
	private static final String PREF_NAME = "Vehicle_Companion_Login";
	private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

	//User details
	private static final String KEY_USER_ID = "user_id";
	private static final String KEY_USER_NAME = "user_name";
	private static final String KEY_USER_OWNER = "user_vehicle_owner";
	private static final String KEY_USER_PH_NO = "user_phone_no";
	private static final String KEY_USER_EMAIL = "user_email";

	//Vehicle details
    private static final String KEY_VEHICLE_ID = "vehicle_id";
    private static final String KEY_VEHICLE_MODEL = "vehicle_model";
    private static final String KEY_VEHICLE_REG_NO = "vehicle_reg_no";

    //License details
    private static final String KEY_LICENSE_ID = "license_id";
    private static final String KEY_LICENSE_EXPIRY_DATE = "license_expiry_date";

    //Insurance details
    private static final String KEY_INSURANCE_ID = "insurance_id";
    private static final String KEY_INSURANCE_EXPIRY_DATE = "insurance_expiry_date";

	//Last Parked Location
	private static final String KEY_LAT_LAST_LOCATION = "latLastParked";
	private static final String KEY_LNG_LAST_LOCATION = "lngLastParked";

	//Firebase ID
	private static final String KEY_FIREBASE_ID = "regId";

	public SessionManager(Context context) {
		this._context = context;
		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}

    public void setLogin(boolean isUserLoggedIn, Vehicle vehicle, Document license, Document insurance) {

        editor.putBoolean(KEY_IS_LOGGED_IN, isUserLoggedIn);
        setVehicle(vehicle);
        setLicense(license);
        setInsurance(insurance);

        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

	public void setLogin(boolean isUserLoggedIn, User user) {

		editor.putBoolean(KEY_IS_LOGGED_IN, isUserLoggedIn);
		setUser(user);

		// commit changes
		editor.commit();

		Log.d(TAG, "User login session modified!");
	}

	public void setLogin(boolean isUserLoggedIn) {
		editor.putBoolean(KEY_IS_LOGGED_IN, isUserLoggedIn);
		// commit changes
		editor.commit();

		Log.d(TAG, "User login session modified!");
	}
	
	public boolean isLoggedIn(){
		return pref.getBoolean(KEY_IS_LOGGED_IN, false);
	}

	public int getKeyUserId() {
		return pref.getInt(KEY_USER_ID,0);
	}

	public void setLastParked(LatLng latLng){

		Double lat = latLng.latitude;
		Double lng = latLng.longitude;
		editor.putString(KEY_LAT_LAST_LOCATION,lat.toString());
		editor.putString(KEY_LNG_LAST_LOCATION,lng.toString());

		// commit changes
		editor.commit();

		Log.d(TAG, "Last parked location stored successfully!");
	}

	public String getLatLastParked(){
		return pref.getString(KEY_LAT_LAST_LOCATION,"0");
	}

	public String getLngLastParked(){
		return pref.getString(KEY_LNG_LAST_LOCATION,"0");
	}

    public String getUserName(){
        return pref.getString(KEY_USER_NAME,"NULL");
    }

    public String getUserEmail(){
        return pref.getString(KEY_USER_EMAIL,"NULL");
    }

    public String getVehicleModel(){
        return pref.getString(KEY_VEHICLE_MODEL,"NULL");
    }

    public String getVehicleRegNo(){
        return pref.getString(KEY_VEHICLE_REG_NO,"NULL");
    }

    public String getLicenseExpiryDate(){
        return pref.getString(KEY_LICENSE_EXPIRY_DATE,"NULL");
    }

    public String getInsuranceExpiryDate(){
        return pref.getString(KEY_INSURANCE_EXPIRY_DATE,"NULL");
    }

	public int getUserId(){
		return pref.getInt(KEY_USER_ID,0);
	}

	public String getFirebaseId(){
		return pref.getString(KEY_FIREBASE_ID,"NULL");
	}


    public void setUser(User user){
        editor.putInt(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_PH_NO, user.getPhone_no());
        editor.putBoolean(KEY_USER_OWNER, user.isOwner());
    }

    public void setVehicle(Vehicle vehicle){
        editor.putInt(KEY_VEHICLE_ID, vehicle.getId());
        editor.putString(KEY_VEHICLE_MODEL, vehicle.getModel());
        editor.putString(KEY_VEHICLE_REG_NO, vehicle.getReg_no());
    }

    public void setLicense(Document doc){
        editor.putInt(KEY_LICENSE_ID, doc.getId());
        editor.putString(KEY_LICENSE_EXPIRY_DATE, doc.getExpiry_date());
    }

    public void setInsurance(Document doc){
        editor.putInt(KEY_INSURANCE_ID, doc.getId());
        editor.putString(KEY_INSURANCE_EXPIRY_DATE, doc.getExpiry_date());
    }

    public void removeAll(){
		editor.clear();
		editor.commit();
	}

}
