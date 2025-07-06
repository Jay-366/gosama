package com.example.gosama;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id  .menu_home:
                    // Already on Home, do nothing
                    return true;
                case R.id.menu_rewards:
                    startActivity(new Intent(HomeActivity.this, RewardsActivity.class));
                    return true;
                case R.id.menu_notifications:
                    // TODO: Implement NotificationsActivity
                    // startActivity(new Intent(HomeActivity.this, NotificationsActivity.class));
                    return true;
                case R.id.menu_profile:
                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                    return true;
            }
            return false;
        });
        // Optionally, set the selected item to Home
        bottomNav.setSelectedItemId(R.id.menu_home);
    }
}
