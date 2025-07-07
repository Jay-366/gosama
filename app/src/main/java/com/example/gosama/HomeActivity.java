package com.example.gosama;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.gosama.viewmodel.UserViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

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
        MaterialCardView scheduleCard = findViewById(R.id.scheduleCard);
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

        scheduleCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MyScheduleActivity.class);
            startActivity(intent);
        });

        // Set up bottom navigation
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_rewards) {
                // TODO: Navigate to rewards
                return true;
            } else if (itemId == R.id.nav_notifications) {
                // TODO: Navigate to notifications
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }
}
