package com.example.android.vehiclecompanion.model;

public class Document {

    int id;
    String expiry_date;
    int type; // 1-license, 2-insurance

    public Document(int id, String expiry_date, int type) {
        this.id = id;
        this.expiry_date = expiry_date;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getExpiry_date() {
        return expiry_date;
    }

    public void setExpiry_date(String expiry_date) {
        this.expiry_date = expiry_date;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
