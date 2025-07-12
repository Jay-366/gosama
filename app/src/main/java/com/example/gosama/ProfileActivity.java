package com.example.gosama;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.gosama.viewmodel.UserViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.tabs.TabLayout;

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
        TabLayout tabLayout = findViewById(R.id.profileTabLayout);
        final View layoutDashboard = findViewById(R.id.layoutDashboard);
        final View layoutActivity = findViewById(R.id.layoutActivity);

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
            } else if (itemId == R.id.nav_chat) {
                startActivity(new Intent(ProfileActivity.this, AssistantChatActivity.class));
                return true;
            } else if (itemId == R.id.nav_activity) {
                startActivity(new Intent(ProfileActivity.this, LiveTrackingActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true; // Already on profile
            }
            return false;
        });

        // Set up tabs
        tabLayout.addTab(tabLayout.newTab().setText("Dashboard"));
        tabLayout.addTab(tabLayout.newTab().setText("Activity"));
        // Show dashboard by default
        layoutDashboard.setVisibility(View.VISIBLE);
        layoutActivity.setVisibility(View.GONE);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    layoutDashboard.setVisibility(View.VISIBLE);
                    layoutActivity.setVisibility(View.GONE);
                } else {
                    layoutDashboard.setVisibility(View.GONE);
                    layoutActivity.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
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
