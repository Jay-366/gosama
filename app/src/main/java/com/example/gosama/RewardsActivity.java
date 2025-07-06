package com.example.gosama;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gosama.adapter.RewardAdapter;
import com.example.gosama.model.EcoStats;
import com.example.gosama.model.Reward;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import androidx.core.widget.NestedScrollView;
import com.bumptech.glide.Glide;
import android.util.Log;
import android.widget.ProgressBar;
import com.github.mikephil.charting.components.YAxis;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import java.util.Map;
import android.animation.ValueAnimator;
import android.graphics.Typeface;
import android.view.ViewTreeObserver;
import android.view.Gravity;
import android.widget.Button;
import android.content.Context;

// Add model for RedeemedReward
class RedeemedReward {
    public String rewardId;
    public String redeemedAt;
    public RedeemedReward(Map<String, Object> map) {
        this.rewardId = map.get("rewardId") != null ? map.get("rewardId").toString() : "";
        this.redeemedAt = map.get("redeemedAt") != null ? map.get("redeemedAt").toString() : "";
    }
}

// Update RedeemedRewardAdapter to use item_reward.xml and fetch reward details
class RedeemedRewardAdapter extends RecyclerView.Adapter<RedeemedRewardAdapter.ViewHolder> {
    private List<RedeemedReward> redeemedList;
    RedeemedRewardAdapter(List<RedeemedReward> list) { this.redeemedList = list; }
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewRewardPic;
        TextView tvRewardName, tvPointsNeeded, tvRedeemedAt;
        ViewHolder(View v) {
            super(v);
            imageViewRewardPic = v.findViewById(R.id.imageViewRewardPic);
            tvRewardName = v.findViewById(R.id.tvRewardName);
            tvPointsNeeded = v.findViewById(R.id.tvPointsNeeded);
            tvRedeemedAt = new TextView(v.getContext());
            ((LinearLayout) v.findViewById(R.id.tvRewardName).getParent()).addView(tvRedeemedAt);
            tvRedeemedAt.setTextSize(12f);
            tvRedeemedAt.setTextColor(v.getResources().getColor(R.color.secondary_text));
            tvRedeemedAt.setPadding(0, 4, 0, 0);
        }
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reward, parent, false);
        return new ViewHolder(v);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RedeemedReward r = redeemedList.get(position);
        holder.tvPointsNeeded.setVisibility(View.GONE);
        holder.tvRedeemedAt.setText("Redeemed: " + r.redeemedAt);
        FirebaseFirestore.getInstance().document(r.rewardId).get().addOnSuccessListener(doc -> {
            Reward reward = doc.toObject(Reward.class);
            if (reward != null) {
                holder.tvRewardName.setText(reward.getDisplayRewardName());
                if (reward.getPic() != null && !reward.getPic().isEmpty()) {
                    Glide.with(holder.imageViewRewardPic.getContext())
                        .load(reward.getPic())
                        .placeholder(R.drawable.bg_stats_card)
                        .error(R.drawable.bg_stats_card)
                        .centerCrop()
                        .into(holder.imageViewRewardPic);
                } else {
                    holder.imageViewRewardPic.setImageResource(R.drawable.bg_stats_card);
                }
                // Set click listener for dialog
                holder.itemView.setOnClickListener(v -> {
                    Context context = v.getContext();
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                    builder.setTitle(null);
                    LinearLayout dialogLayout = new LinearLayout(context);
                    dialogLayout.setOrientation(LinearLayout.VERTICAL);
                    dialogLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                    dialogLayout.setPadding(48, 48, 48, 48);
                    // Reward image (use local resource)
                    ImageView rewardImage = new ImageView(context);
                    rewardImage.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
                    rewardImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    int imageRes = R.drawable.bg_stats_card;
                    if (reward.getPic() != null && !reward.getPic().isEmpty()) {
                        int resId = context.getResources().getIdentifier(reward.getPic(), "drawable", context.getPackageName());
                        if (resId != 0) imageRes = resId;
                    }
                    rewardImage.setImageResource(imageRes);
                    dialogLayout.addView(rewardImage);
                    // Reward name
                    TextView nameText = new TextView(context);
                    nameText.setText(reward.getDisplayRewardName());
                    nameText.setTextSize(20f);
                    nameText.setTypeface(null, Typeface.BOLD);
                    nameText.setGravity(Gravity.CENTER_HORIZONTAL);
                    nameText.setPadding(0, 24, 0, 24);
                    dialogLayout.addView(nameText);
                    // Redeem button (always show, but you can disable if needed)
                    Button redeemButton = new Button(context);
                    redeemButton.setText("Redeem");
                    redeemButton.setGravity(Gravity.CENTER_HORIZONTAL);
                    redeemButton.setOnClickListener(btnV -> {
                        // Optionally, call redeemReward() if you want to allow re-redeeming
                        Toast.makeText(context, "Already redeemed!", Toast.LENGTH_SHORT).show();
                    });
                    LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    btnParams.topMargin = 32;
                    redeemButton.setLayoutParams(btnParams);
                    dialogLayout.addView(redeemButton);
                    builder.setView(dialogLayout);
                    builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
                    builder.show();
                });
            }
        });
    }
    @Override
    public int getItemCount() { return redeemedList.size(); }
}

public class RewardsActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String uid;
    private TextView tvPointsProgress;
    private TextView tvKmShared;
    private TextView tvCo2Saved;
    private View progressBarPoints;
    private TextView tvTotalRides;
    private LineChart lineChart;
    private RecyclerView recyclerViewRewards;
    private RewardAdapter rewardAdapter;
    private List<Reward> rewardList = new ArrayList<>();
    private int userPoints = 0;
    private RecyclerView recyclerViewRedeemed;
    private List<RedeemedReward> redeemedList = new ArrayList<>();
    private RedeemedRewardAdapter redeemedAdapter;
    private View tabIndicator;
    private TextView tabRewardsText, tabRedeemedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize views
        tvPointsProgress = findViewById(R.id.tvPointsProgress);
        tvKmShared = findViewById(R.id.tvKmShared);
        tvCo2Saved = findViewById(R.id.tvCo2Saved);
        progressBarPoints = findViewById(R.id.progressBarPoints);
        tvTotalRides = findViewById(R.id.tvTotalRides);
        lineChart = findViewById(R.id.lineChart);
        recyclerViewRewards = findViewById(R.id.recyclerViewRewards);
        recyclerViewRedeemed = findViewById(R.id.recyclerViewRedeemed);
        tabRewardsText = findViewById(R.id.tabRewardsText);
        tabRedeemedText = findViewById(R.id.tabRedeemedText);
        tabIndicator = findViewById(R.id.tabIndicator);

        // Ensure NestedScrollView is properly configured
        NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollView);
        if (nestedScrollView != null) {
            nestedScrollView.setFillViewport(true);
            nestedScrollView.setNestedScrollingEnabled(true);
        }

        // Set up RecyclerView
        recyclerViewRewards.setLayoutManager(new GridLayoutManager(this, 2));
        rewardAdapter = new RewardAdapter(rewardList, this::onRewardClick);
        recyclerViewRewards.setAdapter(rewardAdapter);

        // Set up Redeemed Rewards RecyclerView
        recyclerViewRedeemed.setLayoutManager(new LinearLayoutManager(this));
        redeemedAdapter = new RedeemedRewardAdapter(redeemedList);
        recyclerViewRedeemed.setAdapter(redeemedAdapter);

        // Set up bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_rewards);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_rewards) {
                // Only show the rewards tab, do not finish or navigate
                showRewardsTab();
                return true;
            } else if (itemId == R.id.nav_notifications) {
                // TODO: Navigate to notifications
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
        bottomNav.setOnItemReselectedListener(item -> {});

        // Set up initial indicator position after layout
        ViewTreeObserver vto = tabRewardsText.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                tabRewardsText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                moveTabIndicator(tabRewardsText);
            }
        });

        tabRewardsText.setOnClickListener(v -> {
            showRewardsTab();
            moveTabIndicator(tabRewardsText);
        });
        tabRedeemedText.setOnClickListener(v -> {
            showRedeemedTab();
            moveTabIndicator(tabRedeemedText);
        });
        showRewardsTab(); // default

        fetchUserData();
        fetchRewards();
    }

    private void showRewardsTab() {
        tabRewardsText.setAlpha(1f);
        tabRedeemedText.setAlpha(0.5f);
        findViewById(R.id.recyclerViewRewards).setVisibility(View.VISIBLE);
        findViewById(R.id.recyclerViewRedeemed).setVisibility(View.GONE);
        moveTabIndicator(tabRewardsText);
    }

    private void showRedeemedTab() {
        tabRewardsText.setAlpha(0.5f);
        tabRedeemedText.setAlpha(1f);
        findViewById(R.id.recyclerViewRewards).setVisibility(View.GONE);
        findViewById(R.id.recyclerViewRedeemed).setVisibility(View.VISIBLE);
        moveTabIndicator(tabRedeemedText);
        // Do not call finish() or startActivity here
    }

    private void fetchUserData() {
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.contains("ecoStats")) {
                EcoStats ecoStats = documentSnapshot.get("ecoStats", EcoStats.class);
                showEcoStats(ecoStats);
            }
            // Fetch redeemed rewards
            if (documentSnapshot.contains("redeemedRewards")) {
                Object redeemedObj = documentSnapshot.get("redeemedRewards");
                redeemedList.clear();
                if (redeemedObj instanceof List) {
                    List<Map<String, Object>> redeemed = (List<Map<String, Object>>) redeemedObj;
                    for (Map<String, Object> item : redeemed) {
                        redeemedList.add(new RedeemedReward(item));
                    }
                } else if (redeemedObj instanceof Map) {
                    Map<String, Object> redeemed = (Map<String, Object>) redeemedObj;
                    redeemedList.add(new RedeemedReward(redeemed));
                }
                redeemedAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showEcoStats(EcoStats ecoStats) {
        if (ecoStats != null) {
            tvKmShared.setText(String.valueOf((int)ecoStats.getTotalKmCarpooled()));
            tvCo2Saved.setText(((int)ecoStats.getTotalCO2SavedKg()) + "kg");
            
            // Points/progress bar logic
            int points = ecoStats.getPoints();
            int nextStage = 1000;
            int percent = (int) (100f * points / nextStage);
            ProgressBar progressBar = findViewById(R.id.progressBarPoints);
            progressBar.setMax(100);
            progressBar.setProgress(percent);
            tvPointsProgress.setText(points + "/" + nextStage);
            
            // Rides Over Time chart
            List<Integer> rides = ecoStats.getLast6MonthsRideCounts();
            List<String> months = getLast6MonthsLabels();
            setRidesChart(rides, months, ecoStats);
        }
    }

    private List<String> getLast6MonthsLabels() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault());
        java.util.Calendar cal = java.util.Calendar.getInstance();
        List<String> labels = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            cal.setTime(new java.util.Date());
            cal.add(java.util.Calendar.MONTH, -i);
            labels.add(sdf.format(cal.getTime()));
        }
        return labels;
    }

    private void setRidesChart(List<Integer> rides, List<String> months, EcoStats ecoStats) {
        int totalRides = 0;
        if (ecoStats.getRideCount() != null) {
            for (Integer v : ecoStats.getRideCount().values()) {
                if (v != null) totalRides += v;
            }
        }
        tvTotalRides.setText(String.valueOf(totalRides));

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < rides.size(); i++) {
            entries.add(new Entry(i, Math.max(0, rides.get(i))));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Rides");
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setColor(Color.parseColor("#6B8E6B")); // Match gradient top color
        dataSet.setLineWidth(3f);
        dataSet.setDrawFilled(true);
        dataSet.setFillDrawable(ContextCompat.getDrawable(this, R.drawable.chart_fade_fill));
        dataSet.setHighlightEnabled(false);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // X-Axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#888888"));
        xAxis.setTextSize(14f);
        xAxis.setAxisLineColor(Color.TRANSPARENT);
        xAxis.setAxisMinimum(-0.5f); // space before first label
        xAxis.setAvoidFirstLastClipping(true);
        // Removed setAxisMaximum to let chart auto-calculate

        // Y-Axis
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setTextColor(Color.parseColor("#888888"));
        leftAxis.setTextSize(14f);
        leftAxis.setAxisLineColor(Color.TRANSPARENT);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawLabels(true);
        leftAxis.setSpaceTop(20f);
        leftAxis.setSpaceBottom(5f);
        leftAxis.setAxisMinimum(0f);           // Lock baseline at 0
        leftAxis.setGranularity(1f);
        leftAxis.setGranularityEnabled(true);

        int maxRides = 0;
        for (Integer ride : rides) {
            if (ride > maxRides) maxRides = ride;
        }
        int axisMax = Math.max(5, ((maxRides + 4) / 5) * 5);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(axisMax);
        leftAxis.setLabelCount(Math.min(axisMax + 1, 6), true);
        leftAxis.setGranularity(1f);

        lineChart.getAxisRight().setEnabled(false);

        // Final chart settings
        lineChart.setDrawBorders(false);
        lineChart.setDescription(null);
        lineChart.getLegend().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setViewPortOffsets(40f, 40f, 60f, 40f);
        lineChart.setExtraOffsets(16f, 16f, 16f, 30f);
        lineChart.setClipToPadding(false);
        lineChart.setClipChildren(false);
        lineChart.setHighlightPerTapEnabled(false);
        lineChart.setHighlightPerDragEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDoubleTapToZoomEnabled(false);

        lineChart.invalidate();
    }

    private int sumRides(EcoStats ecoStats) {
        int sum = 0;
        if (ecoStats.getRideCount() != null) {
            for (Integer v : ecoStats.getRideCount().values()) {
                if (v != null) sum += v;
            }
        }
        return sum;
    }

    private void fetchRewards() {
        db.collection("rewards")
          .get()
          .addOnSuccessListener(queryDocumentSnapshots -> {
              rewardList.clear();
              for (DocumentSnapshot doc : queryDocumentSnapshots) {
                  Reward reward = doc.toObject(Reward.class);
                  if (reward != null) {
                      reward.setId(doc.getId());
                      rewardList.add(reward);
                      
                      // Log image URLs for debugging
                      if (reward.getPic() != null && !reward.getPic().isEmpty()) {
                          Log.d("RewardsActivity", "Reward: " + reward.getRewardName() + " - Image URL: " + reward.getPic());
                      } else {
                          Log.w("RewardsActivity", "Reward: " + reward.getRewardName() + " - No image URL");
                      }
                  }
              }
              rewardAdapter.notifyDataSetChanged();
              
              // Show message if no rewards found
              if (rewardList.isEmpty()) {
                  Toast.makeText(this, "No rewards available at the moment.", Toast.LENGTH_SHORT).show();
              } else {
                  Log.d("RewardsActivity", "Loaded " + rewardList.size() + " rewards from Firestore");
              }
          })
          .addOnFailureListener(e -> {
              Log.e("RewardsActivity", "Failed to load rewards: " + e.getMessage());
              Toast.makeText(this, "Failed to load rewards: " + e.getMessage(), Toast.LENGTH_SHORT).show();
          });
    }

    private void onRewardClick(Reward reward) {
        if (tvPointsProgress == null) return;

        // Get current user points from the TextView
        final int userPoints;
        String pointsText = tvPointsProgress.getText().toString().split("/")[0].trim();
        int parsedPoints = 0;
        try {
            parsedPoints = Integer.parseInt(pointsText);
        } catch (NumberFormatException e) {
            Log.e("RewardsActivity", "Error parsing points: " + pointsText);
        }
        userPoints = parsedPoints;

        // Create and show dialog with reward details
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(null);

        // Create dialog content
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        dialogLayout.setPadding(48, 48, 48, 48);

        // Reward image (use local resource)
        ImageView rewardImage = new ImageView(this);
        rewardImage.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        rewardImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Use local drawable based on reward.getPic() (should be the resource name without extension)
        int imageRes = R.drawable.bg_stats_card; // default
        if (reward.getPic() != null && !reward.getPic().isEmpty()) {
            int resId = getResources().getIdentifier(reward.getPic(), "drawable", getPackageName());
            if (resId != 0) imageRes = resId;
        }
        rewardImage.setImageResource(imageRes);
        dialogLayout.addView(rewardImage);

        // Reward name
        TextView nameText = new TextView(this);
        nameText.setText(reward.getDisplayRewardName());
        nameText.setTextSize(20f);
        nameText.setTypeface(null, Typeface.BOLD);
        nameText.setGravity(Gravity.CENTER_HORIZONTAL);
        nameText.setPadding(0, 24, 0, 24);
        dialogLayout.addView(nameText);

        // Redeem button (only if available)
        if (userPoints >= reward.getPointsNeed() && reward.getStock() > 0) {
            Button redeemButton = new Button(this);
            redeemButton.setText("Redeem");
            redeemButton.setGravity(Gravity.CENTER_HORIZONTAL);
            redeemButton.setOnClickListener(v -> {
                redeemReward(reward, userPoints);
            });
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            btnParams.topMargin = 32;
            redeemButton.setLayoutParams(btnParams);
            dialogLayout.addView(redeemButton);
        } else {
            TextView notAvailable = new TextView(this);
            notAvailable.setText(userPoints < reward.getPointsNeed() ? "Not enough points" : "Not available");
            notAvailable.setTextColor(Color.GRAY);
            notAvailable.setGravity(Gravity.CENTER_HORIZONTAL);
            notAvailable.setPadding(0, 32, 0, 0);
            dialogLayout.addView(notAvailable);
        }

        builder.setView(dialogLayout);
        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    
    private void redeemReward(Reward reward, int userPoints) {
        // Update user points in Firestore
        db.collection("users").document(uid)
                .update("ecoStats.points", userPoints - reward.getPointsNeed())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Reward redeemed successfully!", Toast.LENGTH_SHORT).show();
                    // Update the points display
                    tvPointsProgress.setText(String.valueOf(userPoints - reward.getPointsNeed()));
                    // Refresh user data
                    fetchUserData();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to redeem reward. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void moveTabIndicator(final TextView selectedTab) {
        int[] parentLoc = new int[2];
        ((View)selectedTab.getParent()).getLocationOnScreen(parentLoc);
        int[] tabLoc = new int[2];
        selectedTab.getLocationOnScreen(tabLoc);
        int left = tabLoc[0] - parentLoc[0];
        int width = selectedTab.getWidth();
        // Animate indicator position and width
        tabIndicator.animate().x(selectedTab.getX()).setDuration(150).start();
        ValueAnimator anim = ValueAnimator.ofInt(tabIndicator.getWidth(), width);
        anim.addUpdateListener(animation -> {
            int val = (Integer) animation.getAnimatedValue();
            tabIndicator.getLayoutParams().width = val;
            tabIndicator.requestLayout();
        });
        anim.setDuration(150);
        anim.start();
        // Set tab styles
        tabRewardsText.setTypeface(null, selectedTab == tabRewardsText ? Typeface.BOLD : Typeface.NORMAL);
        tabRedeemedText.setTypeface(null, selectedTab == tabRedeemedText ? Typeface.BOLD : Typeface.NORMAL);
        tabRewardsText.setTextColor(getResources().getColor(selectedTab == tabRewardsText ? R.color.main_text : R.color.secondary_text));
        tabRedeemedText.setTextColor(getResources().getColor(selectedTab == tabRedeemedText ? R.color.main_text : R.color.secondary_text));
    }
} 