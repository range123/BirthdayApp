package com.range.birthdayapp;

public class Users {
    private String DisplayName;
    private String Uid;

    private String phn;
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhn() {
        return phn;
    }

    public void setPhn(String phn) {
        this.phn = phn;
    }


    public Users(String displayName, String uid,String phone,String email) {
        DisplayName = displayName;
        Uid = uid;
        phn=phone;
        this.email=email;
    }

    public Users() {
    }

    public String getDisplayName() {
        return DisplayName;
    }

    public void setDisplayName(String displayName) {
        DisplayName = displayName;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }
}
