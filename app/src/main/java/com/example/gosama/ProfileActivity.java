package com.example.gosama;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.gosama.viewmodel.UserViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {
    private UserViewModel userViewModel;
    private TextView tvUserName;
    private TextView tvUserStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize ViewModel
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Initialize views
        tvUserName = findViewById(R.id.tvUserName);
        tvUserStatus = findViewById(R.id.tvUserStatus);
        Button btnSettings = findViewById(R.id.btnSettings);
        Button btnEditProfile = findViewById(R.id.btnEditProfile);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // Set selected item in bottom navigation
        bottomNav.setSelectedItemId(R.id.nav_profile);

        // Set up bottom navigation
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                finish(); // Go back to HomeActivity
                return true;
            } else if (itemId == R.id.nav_rewards) {
                startActivity(new Intent(ProfileActivity.this, RewardsActivity.class));
                return true;
            } else if (itemId == R.id.nav_notifications) {
                // TODO: Navigate to notifications
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true; // Already on profile
            }
            return false;
        });

        // Observe user data
        userViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                tvUserName.setText(user.getUsername());
                tvUserStatus.setText("Verified User & Driver");
            }
        });

        // Load user data
        userViewModel.loadUserData();

        // Set up button click listeners
        btnSettings.setOnClickListener(v -> {
            // TODO: Implement settings functionality
        });

        btnEditProfile.setOnClickListener(v -> {
            // TODO: Implement edit profile functionality
        });
    }
}
