package com.example.android.vehiclecompanion.model;



public class User {

    //private variables
    int id;
    String name;
    boolean owner;
    String phone_no;
    String email;

    // Empty constructor
    public User(){
    }

    // constructor
    public User(int id ,String email){
        this.id = id;
        this.email = email;
    }

    // constructor
    public User(String name, boolean owner, String phone_no, String email){
        this.name = name;
        this.owner = owner;
        this.phone_no = phone_no;
        this.email = email;
    }

    // constructor
    public User(int id, String name, boolean owner,  String email, String phone_no){
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.phone_no = phone_no;
        this.email = email;
    }


    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    public void setPhone_no(String phone_no) {
        this.phone_no = phone_no;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isOwner() {
        return owner;
    }

    public String getPhone_no() {
        return phone_no;
    }
}
