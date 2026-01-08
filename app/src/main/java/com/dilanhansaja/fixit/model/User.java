package com.dilanhansaja.fixit.model;

public class User {

    private String fname;
    private String lname;
    private String email;
    private String password;
    private String registered_date;
    private String email_verification;
    private String geohash;
    private double lat;
    private double lng;

    public User() {
    }

    public User(String fname, String lname, String email, String password, String registered_date,String email_verification,String geohash,double lat,double lng) {
        this.fname = fname;
        this.lname = lname;
        this.email = email;
        this.password = password;
        this.email_verification=email_verification;
        this.registered_date = registered_date;
        this.geohash=geohash;
        this.lat=lat;
        this.lng=lng;
    }

    public String getFname() {

        return fname;
    }

    public void setFname(String fname) {

        this.fname = fname;
    }

    public String getLname() {

        return lname;
    }

    public void setLname(String lname) {

        this.lname = lname;
    }

    public String getEmail() {

        return email;
    }

    public void setEmail(String email) {

        this.email = email;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail_verification() {
        return email_verification;
    }

    public void setEmail_verification(String email_verification) {
        this.email_verification = email_verification;
    }
    public String getRegistered_date() {
        return registered_date;
    }

    public void setRegistered_date(String registered_date) {
        this.registered_date = registered_date;
    }

    public String getGeohash() {
        return geohash;
    }

    public void setGeohash(String geohash) {
        this.geohash = geohash;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
