package com.example.gosama;

import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

public class Parcel {
    private String documentId;
    private String userId;
    private String pickupAddress;
    private String dropoffAddress;
    private GeoPoint pickupLocation;
    private GeoPoint dropoffLocation;
    private String schedule;
    private String parcelSize;
    private double weightKg;
    private String instructions;
    private Date timestamp;

    public Parcel() {}

    // Getters and Setters
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public String getDropoffAddress() { return dropoffAddress; }
    public void setDropoffAddress(String dropoffAddress) { this.dropoffAddress = dropoffAddress; }

    public GeoPoint getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(GeoPoint pickupLocation) { this.pickupLocation = pickupLocation; }

    public GeoPoint getDropoffLocation() { return dropoffLocation; }
    public void setDropoffLocation(GeoPoint dropoffLocation) { this.dropoffLocation = dropoffLocation; }

    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }

    public String getParcelSize() { return parcelSize; }
    public void setParcelSize(String parcelSize) { this.parcelSize = parcelSize; }

    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
