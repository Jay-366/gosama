package com.example.gosama;

public class User {
    private String uid;
    private String username;
    private String email;
    private int rideCount;

    // IMPORTANT: Firestore requires an empty constructor for deserialization
    public User() {
    }

    public User(String uid, String username, String email, int rideCount) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.rideCount = rideCount;
    }

    // Getters are required for Firestore to serialize the object
    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public int getRideCount() {
        return rideCount;
    }

    // Setters are good practice
    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRideCount(int rideCount) {
        this.rideCount = rideCount;
    }
}
