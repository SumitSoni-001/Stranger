package com.example.stranger.Models;

public class Users {

    private String uid, name, profile, city;
    private long coins;

    public Users() {
    }

    public Users(String name, String city) {
        this.name = name;
        this.city = city;
    }

    public Users(String uid, String name, String profile, String city, long coins) {
        this.uid = uid;
        this.name = name;
        this.profile = profile;
        this.city = city;
        this.coins = coins;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public long getCoins() {
        return coins;
    }

    public void setCoins(long coins) {
        this.coins = coins;
    }
}
