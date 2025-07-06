package com.example.gosama.model;

public class Reward {
    private String rewardName;
    private int pointsNeed;
    private int stock;
    private String id;
    private String pic;

    public Reward() {}

    public String getRewardName() { return rewardName; }
    public int getPointsNeed() { return pointsNeed; }
    public int getStock() { return stock; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPic() { return pic; }
    public void setPic(String pic) { this.pic = pic; }

    // Helper to display reward name without extra quotes
    public String getDisplayRewardName() {
        if (rewardName == null) return "";
        return rewardName.replaceAll("^\"+|\"+$", "").replaceAll("^'+|'+$", "");
    }
} 