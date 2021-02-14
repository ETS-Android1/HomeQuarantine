package com.unitbv.quarantineapp;

import java.io.Serializable;

public class Users implements Serializable {
    private String uid, fullName, email, birthday, phoneNumber, country, city, street, cnp, serieBuletin, idPic, profilePic, status;
    private boolean admin;


    // constructor
    public Users(String uid, String fullName, boolean admin, String email, String birthday, String phoneNumber, String country, String city, String street, String cnp, String serieBuletin, String idPic, String profilePic, String status) {
        this.uid = uid;
        this.fullName = fullName;
        this.admin = admin;
        this.email = email;
        this.birthday = birthday;
        this.phoneNumber = phoneNumber;
        this.country = country;
        this.city = city;
        this.street = street;
        this.cnp = cnp;
        this.serieBuletin = serieBuletin;
        this.idPic = idPic;
        this.profilePic = profilePic;
        this.status = status;
    }

    // getters & setters

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCnp() {
        return cnp;
    }

    public void setCnp(String cnp) {
        this.cnp = cnp;
    }

    public String getSerieBuletin() {
        return serieBuletin;
    }

    public void setSerieBuletin(String serieBuletin) {
        this.serieBuletin = serieBuletin;
    }

    public String getIdPic() {
        return idPic;
    }

    public void setIdPic(String idPic) {
        this.idPic = idPic;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
