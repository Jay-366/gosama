package com.example.gosama.model;

public class ChatRequest {
    private String message;
    private String userId;
    private String model;
    private double temperature;
    private String rideType;
    private String location;

    public ChatRequest() {}

    public ChatRequest(String message, String userId) {
        this.message = message;
        this.userId = userId;
    }

    public ChatRequest(String message, String userId, String rideType, String location) {
        this.message = message;
        this.userId = userId;
        this.rideType = rideType;
        this.location = location;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public String getRideType() {
        return rideType;
    }

    public void setRideType(String rideType) {
        this.rideType = rideType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
} 