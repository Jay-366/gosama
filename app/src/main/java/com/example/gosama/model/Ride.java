package com.example.gosama.model;

public class Ride {
    private String departure;
    private String destination;
    private String date;
    private String time;
    private String driverName;
    private double price;
    private String driverCompany;
    private int driverRides;
    private String status;

    public Ride() {
        // Default constructor required for calls to DataSnapshot.getValue(Ride.class)
    }

    public Ride(String departure, String destination, String date, String time, String driverName, double price, String driverCompany, int driverRides, String status) {
        this.departure = departure;
        this.destination = destination;
        this.date = date;
        this.time = time;
        this.driverName = driverName;
        this.price = price;
        this.driverCompany = driverCompany;
        this.driverRides = driverRides;
        this.status = status;
    }

    public String getDriverCompany() {
        return driverCompany;
    }

    public void setDriverCompany(String driverCompany) {
        this.driverCompany = driverCompany;
    }

    public int getDriverRides() {
        return driverRides;
    }

    public void setDriverRides(int driverRides) {
        this.driverRides = driverRides;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
