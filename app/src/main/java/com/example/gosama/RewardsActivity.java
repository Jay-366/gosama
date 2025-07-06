package com.example.gosama;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gosama.adapter.RewardAdapter;
import com.example.gosama.model.EcoStats;
import com.example.gosama.model.Reward;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class RewardsActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String uid;
    private TextView tvPointsProgress;
    private ProgressBar progressBarPoints;
    private RecyclerView recyclerViewRewards;
    private RewardAdapter rewardAdapter;
    private List<Reward> rewardList = new ArrayList<>();
    private int userPoints = 0;
    private int nextRewardPoints = 500; // Default, will update dynamically

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        tvPointsProgress = findViewById(R.id.tvPointsProgress);
        progressBarPoints = findViewById(R.id.progressBarPoints);
        recyclerViewRewards = findViewById(R.id.recyclerViewRewards);

        recyclerViewRewards.setLayoutManager(new LinearLayoutManager(this));
        rewardAdapter = new RewardAdapter(rewardList, this::onRedeemClick);
        recyclerViewRewards.setAdapter(rewardAdapter);

        fetchUserData();
        fetchRewards();
    }

    private void fetchUserData() {
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Points
                if (documentSnapshot.contains("points")) {
                    userPoints = documentSnapshot.getLong("points").intValue();
                }
                // Eco Stats
                if (documentSnapshot.contains("ecoStats")) {
                    EcoStats ecoStats = documentSnapshot.toObject(EcoStats.class);
                    showEcoStats(ecoStats);
                }
                updatePointsProgress();
            }
        });
    }

    private void showEcoStats(EcoStats ecoStats) {
        LinearLayout ecoStatsContainer = findViewById(R.id.ecoStatsContainer);
        ecoStatsContainer.removeAllViews();

        addEcoStatCard(ecoStatsContainer, "KM Shared", ecoStats.getTotalKmCarpooled() + " KM");
        addEcoStatCard(ecoStatsContainer, "CO₂ Saved", ecoStats.getTotalCO2SavedKg() + " kg");
        addEcoStatCard(ecoStatsContainer, "Rides", String.valueOf(ecoStats.getRideCount()));
    }

    private void addEcoStatCard(LinearLayout container, String title, String value) {
        TextView card = new TextView(this);
        card.setText(title + "\n" + value);
        card.setPadding(24, 24, 24, 24);
        card.setBackgroundResource(android.R.drawable.dialog_holo_light_frame); // Use a simple frame, replace with custom drawable if needed
        card.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        card.setTextSize(16);
        card.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        container.addView(card);
    }

    private void fetchRewards() {
        db.collection("rewards").get().addOnSuccessListener(queryDocumentSnapshots -> {
            rewardList.clear();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Reward reward = doc.toObject(Reward.class);
                reward.setId(doc.getId());
                rewardList.add(reward);
                // Find the next reward points needed
                if (reward.getPointsNeed() > userPoints && (nextRewardPoints == 0 || reward.getPointsNeed() < nextRewardPoints)) {
                    nextRewardPoints = reward.getPointsNeed();
                }
            }
            rewardAdapter.notifyDataSetChanged();
            updatePointsProgress();
        });
    }

    private void updatePointsProgress() {
        tvPointsProgress.setText(userPoints + " / " + nextRewardPoints);
        int progress = (int) ((userPoints * 100.0f) / nextRewardPoints);
        progressBarPoints.setProgress(progress);
    }

    private void onRedeemClick(Reward reward) {
        if (userPoints < reward.getPointsNeed()) {
            Toast.makeText(this, "Not enough points to redeem.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Deduct points and update Firestore
        db.collection("users").document(uid)
                .update("points", userPoints - reward.getPointsNeed())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Reward redeemed!", Toast.LENGTH_SHORT).show();
                    userPoints -= reward.getPointsNeed();
                    updatePointsProgress();
                });
        // Optionally, update reward stock and add to user's redeemedRewards
    }
} 