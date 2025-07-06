package com.example.gosama;

public class User {
    private String uid; // <-- match Firestore field
    private String displayName;
    private String email;
    private int rideCount;

    // Required empty constructor for Firestore
    public User() {}

    public User(String userId, String displayName, String email, int rideCount) {
        this.uid = userId; // Changed from this.userId to this.uid
        this.displayName = displayName;
        this.email = email;
    }

    public String getUid() { return uid; } // Added getUid()
    public void setUid(String uid) { this.uid = uid; } // Added setUid()

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
}
