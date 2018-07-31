package com.range.birthdayapp;

public class birthdaypost {
    private String name;
    private String dob;
    private String phone_number;
    private String photourl;
    private String uid;
    private String pid;
    public birthdaypost()
    {

    }

    public birthdaypost(String name, String dob, String phone_number, String photourl, String uid, String pid) {
        this.name = name;
        this.dob = dob;
        this.phone_number = phone_number;
        this.photourl = photourl;
        this.uid = uid;
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getPhotourl() {
        return photourl;
    }

    public void setPhotourl(String photourl) {
        this.photourl = photourl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }
}
