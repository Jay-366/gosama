package com.example.gosama;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.gosama.viewmodel.UserViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {
    private UserViewModel userViewModel;
    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize ViewModel
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Initialize views
        welcomeText = findViewById(R.id.welcomeText);
        MaterialCardView findRideCard = findViewById(R.id.findRideCard);
        MaterialCardView offerRideCard = findViewById(R.id.offerRideCard);
        MaterialCardView sendParcelCard = findViewById(R.id.sendParcelCard);
        MaterialCardView scheduledCommutesCard = findViewById(R.id.scheduledCommutesCard);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // Observe user data
        userViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                welcomeText.setText("Welcome back, " + user.getUsername());
            }
        });

        // Load user data
        userViewModel.loadUserData();

        // Set up click listeners
        findRideCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FindRideActivity.class);
            startActivity(intent);
        });

        offerRideCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, OfferRideActivity.class);
            startActivity(intent);
        });

        sendParcelCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ParcelDeliveryActivity.class);
            startActivity(intent);
        });

        scheduledCommutesCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ScheduledCommutesActivity.class);
            startActivity(intent);
        });

        // Set up bottom navigation
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_rewards) {
                try {
                    startActivity(new Intent(HomeActivity.this, RewardsActivity.class));
                } catch (Exception e) {
                    android.util.Log.e("HomeActivity", "Error starting RewardsActivity: " + e.getMessage());
                    android.widget.Toast.makeText(HomeActivity.this, "Error opening rewards", android.widget.Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (itemId == R.id.nav_chat) {
                startActivity(new Intent(HomeActivity.this, AssistantChatActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.nav_home) {
                return true; // Already on home
            } else if (itemId == R.id.nav_activity) {
                startActivity(new Intent(HomeActivity.this, LiveTrackingActivity.class));
                return true;
            }
            return false;
        });

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Handle not logged in (redirect to login or show error)
            finish();
            return;
        }

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.getBooleanExtra("SHOW_MATCH_NOTIFICATION", false)) {
            // Clear the flag to prevent the dialog from showing again on configuration change
            getIntent().removeExtra("SHOW_MATCH_NOTIFICATION");

            new Handler(Looper.getMainLooper()).postDelayed(this::showMatchedRideDialog, 3000); // 3-second delay
        }
    }

    private void showMatchedRideDialog() {
        if (isFinishing() || isDestroyed()) {
            return; // Don't show dialog if activity is not running
        }

        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
        android.view.View dialogView = inflater.inflate(R.layout.dialog_matched_ride, null);

        final android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        dialogView.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnNotNow).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnAccept).setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(HomeActivity.this, ScheduledCommutesActivity.class);
            startActivity(intent);
        });

        // Make the dialog background transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        dialog.show();
    }
}
