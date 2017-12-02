package com.example.android.vehiclecompanion.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.vehiclecompanion.model.User;

import java.util.ArrayList;
import java.util.List;


public class SQLiteHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "vehicleCompanion";

    // Contacts table names
    private static final String TABLE_USER = "user";
    private static final String TABLE_VEHICLE = "vehicle";
    private static final String TABLE_APPOINTMENT = "appointment";
    private static final String TABLE_DOCUMENT = "document";

    // Users Table Columns names
    private static final String KEY_USER_ID = "id";
    private static final String KEY_USER_NAME = "name";
    private static final String KEY_USER_OWNER = "vehicle_owner";
    private static final String KEY_USER_PH_NO = "phone_no";
    private static final String KEY_USER_EMAIL = "email";

    //Vehicle Table Columns names
    private static final String KEY_VEHICLE_ID = "id";
    private static final String KEY_VEHICLE_LATITUDE = "lat";
    private static final String KEY_VEHICLE_LONGITUDE = "lng";
    private static final String KEY_VEHICLE_USER_ID = "vehicle_owner";
    private static final String KEY_VEHICLE_TYPE_ID = "type_id";
    private static final String KEY_VEHICLE_TYPE_NAME = "type_name";

    //Appointment Table Columns names
    private static final String KEY_APPOINTMENT_ID = "id";
    private static final String KEY_APPOINTMENT_TYPE = "type";
    private static final String KEY_APPOINTMENT_DATE = "date";
    private static final String KEY_APPOINTMENT_TIME = "time";
    private static final String KEY_APPOINTMENT_USER_ID = "user_id";
    private static final String KEY_APPOINTMENT_BRANCH_ID = "branch_id";

    //Document Table Columns names
    private static final String KEY_DOCUMENT_ID = "id";
    private static final String KEY_DOCUMENT_TYPE = "type";
    private static final String KEY_DOCUMENT_EXPIRY_DATE = "expiry_date";
    private static final String KEY_DOCUMENT_USER_ID = "user_id";
    private static final String KEY_DOCUMENT_VEHICLE_ID = "vehicle_id";

    //User Table SQL
    private static final String CREATE_TABLE_TEST = "CREATE TABLE "
            + TABLE_USER + "(" + KEY_USER_ID + " INTEGER PRIMARY KEY," + KEY_USER_NAME
            + " TEXT," + KEY_USER_OWNER + " INTEGER," + KEY_USER_PH_NO
            + " TEXT" + KEY_USER_EMAIL + "TEXT" + ")";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TEST);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new user
    void AddorUpdateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, user.getId()); // User Id
        values.put(KEY_USER_NAME, user.getName()); // User Name
        values.put(KEY_USER_OWNER, user.isOwner()? 1 : 0); // Owner?
        values.put(KEY_USER_PH_NO, user.getPhone_no()); // User Phone
        values.put(KEY_USER_EMAIL, user.getPhone_no()); // User Email

        if(user.getId() == getUserId()){
            updateUser(user);
        }
        else {
            db.insert(TABLE_USER, null, values);
        }

        db.close(); // Closing database connection
    }

    void addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, user.getId()); // User Id
        values.put(KEY_USER_NAME, user.getName()); // User Name
        values.put(KEY_USER_OWNER, user.isOwner()? 1 : 0); // Owner?
        values.put(KEY_USER_PH_NO, user.getPhone_no()); // User Phone
        values.put(KEY_USER_EMAIL, user.getPhone_no()); // User Email

        if(user.getId() != getUserId())
            db.insert(TABLE_USER, null, values);

        db.close(); // Closing database connection
    }

    // Getting user details
    public User getUser() {
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null)
            cursor.moveToFirst();

        User user = new User();
        user.setId(Integer.parseInt(cursor.getString(0)));
        user.setName(cursor.getString(1));
        user.setOwner(Integer.parseInt(cursor.getString(2)) > 0);
        user.setPhone_no(cursor.getString(3));

        // return contact list
        return user;
    }

    // Updating single contact
    public int updateUser (User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, user.getId()); // User Id
        values.put(KEY_USER_NAME, user.getName()); // User Name
        values.put(KEY_USER_OWNER, user.isOwner()? 1 : 0); // Owner?
        values.put(KEY_USER_PH_NO, user.getPhone_no()); // User Phone
        values.put(KEY_USER_EMAIL, user.getPhone_no()); // User Email

        // updating row
        return db.update(TABLE_USER, values, KEY_USER_ID + " = ?",
                new String[] { String.valueOf(user.getId()) });
    }

    // Deleting user details
    public void deleteUser() {
        SQLiteDatabase db = this.getWritableDatabase();
        int response =  db.delete(TABLE_USER, null, null);
        db.close();
    }

    // Getting All Contacts
    public List<User> getAllContacts() {
        List<User> contactList = new ArrayList<User>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setId(Integer.parseInt(cursor.getString(0)));
                user.setName(cursor.getString(1));
                user.setOwner(Integer.parseInt(cursor.getString(2)) > 0);
                user.setPhone_no(cursor.getString(3));
                // Adding contact to list
                contactList.add(user);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }

    public int getUserCount() {
        int a;
        String countQuery = "SELECT  * FROM " + TABLE_USER;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        a=cursor.getCount();
        cursor.close();

        // return count
        return a;
    }

    public int getUserId() {
        // Select All Query
        String selectQuery = "SELECT  " + KEY_USER_ID + " FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor != null){
            cursor.moveToFirst();
            return Integer.parseInt(cursor.getString(0));

            }
            return 0;
        }


}
