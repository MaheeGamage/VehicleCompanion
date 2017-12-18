package com.example.android.vehiclecompanion.model;

/**
 * Created by Mahee on 2017-12-08.
 */

public class Vehicle {
    int id;
    String model;
    String reg_no;

    public Vehicle(){}

    public Vehicle(int id, String name, String reg_no) {
        this.id = id;
        this.model = name;
        this.reg_no = reg_no;
    }

    public String getReg_no() {
        return reg_no;
    }

    public void setReg_no(String reg_no) {
        this.reg_no = reg_no;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
