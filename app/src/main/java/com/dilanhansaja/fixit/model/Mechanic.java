package com.dilanhansaja.fixit.model;

public class Mechanic {

    private String mechanicId;
    private String fname;
    private String lname;
    private String email;
    private String password;
    private String registered_date;
    private Double rate;
    private String email_verification;
    private String account_verification;
    private String geohash;
    private double lat;
    private double lng;

    public Mechanic() {
    }

    public Mechanic(String fname, String lname, String registered_date, Double rate,String mechanicId,String account_verification) {
        this.fname = fname;
        this.lname = lname;
        this.registered_date = registered_date;
        this.rate = rate;
        this.mechanicId=mechanicId;
        this.account_verification=account_verification;
    }

    public Mechanic(String fname, String lname, String email, String password,Double rate, String registered_date,String email_verification,String account_verification,String geohash,double lat, double lng) {
        this.fname = fname;
        this.lname = lname;
        this.email = email;
        this.password = password;
        this.rate=rate;
        this.registered_date = registered_date;
        this.email_verification=email_verification;
        this.account_verification=account_verification;
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

    public String getRegistered_date() {
        return registered_date;
    }

    public void setRegistered_date(String registered_date) {
        this.registered_date = registered_date;
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

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public String getAccount_verification() {
        return account_verification;
    }

    public void setAccount_verification(String account_verification) {
        this.account_verification = account_verification;
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

    public String getMechanicId() {
        return mechanicId;
    }

    public void setMechanicId(String mechanicId) {
        this.mechanicId = mechanicId;
    }
}
