package com.example.gosama.model;

public class EcoStats {
    private java.util.Map<String, Integer> rideCount;
    private double totalCO2SavedKg;
    private double totalKmCarpooled;
    private int points;

    public EcoStats() {}

    public java.util.Map<String, Integer> getRideCount() { return rideCount; }
    public void setRideCount(java.util.Map<String, Integer> rideCount) { this.rideCount = rideCount; }
    public double getTotalCO2SavedKg() { return totalCO2SavedKg; }
    public double getTotalKmCarpooled() { return totalKmCarpooled; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    // Helper: Get last 6 months' ride counts (including current month)
    public java.util.List<Integer> getLast6MonthsRideCounts() {
        java.util.List<Integer> result = new java.util.ArrayList<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM yyyy");
        java.util.Calendar cal = java.util.Calendar.getInstance();
        for (int i = 5; i >= 0; i--) {
            java.util.Calendar temp = (java.util.Calendar) cal.clone();
            temp.add(java.util.Calendar.MONTH, -i);
            String key = sdf.format(temp.getTime()); // e.g., "Jul 2025"
            // Try both "Jul 2025" and "July2025" for compatibility
            Integer val = null;
            if (rideCount != null) {
                val = rideCount.get(key);
                if (val == null) {
                    String altKey = new java.text.SimpleDateFormat("MMMM yyyy").format(temp.getTime()).replace(" ", "");
                    val = rideCount.getOrDefault(altKey, 0);
                }
            }
            result.add(val != null ? val : 0);
        }
        return result;
    }
} 