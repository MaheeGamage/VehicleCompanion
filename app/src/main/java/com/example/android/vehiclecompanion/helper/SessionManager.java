package com.example.android.vehiclecompanion.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.example.android.vehiclecompanion.model.User;
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

	//User ID
	private static final String KEY_USER_ID = "user_id";

	//Last Parked Location
	private static final String KEY_LAT_LAST_LOCATION = "latLastParked";
	private static final String KEY_LNG_LAST_LOCATION = "lngLastParked";

	public SessionManager(Context context) {
		this._context = context;
		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}

	public void setLogin(boolean isUserLoggedIn, User user) {

		editor.putBoolean(KEY_IS_LOGGED_IN, isUserLoggedIn);
		editor.putInt(KEY_USER_ID, user.getId());

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
}
