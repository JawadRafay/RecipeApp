package com.example.reciperover.Models;

public class User {
    private String uId = "";
    private String name = "";
    private String email = "";
    private String profilePicUrl = "";
    private String phoneNo = "";
    private boolean block;
    public User() {
    }
    public User(String uId, String name, String email, String profilePicUrl, String phoneNo, boolean block) {
        this.uId = uId;
        this.name = name;
        this.email = email;
        this.profilePicUrl = profilePicUrl;
        this.phoneNo = phoneNo;
        this.block = block;
    }
    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public boolean isBlock() {
        return block;
    }

    public void setBlock(boolean block) {
        this.block = block;
    }
}
