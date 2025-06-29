package com.example.gosama;

public class User {
    private String userId;
    private String displayName;
    private String email;
    private int rideCount;

    // Required empty constructor for Firestore
    public User() {}

    public User(String userId, String displayName, String email, int rideCount) {
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
        this.rideCount = rideCount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getRideCount() {
        return rideCount;
    }

    public void setRideCount(int rideCount) {
        this.rideCount = rideCount;
    }
}
