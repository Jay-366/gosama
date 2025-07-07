package com.example.mad_asgn;

import static com.example.mad_asgn.R.layout.*;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ScheduledCommutesActivity extends AppCompatActivity {

    private TextView tvNoScheduledRides;
    private LinearLayout layoutScheduleHistory;
    private Button btnNewSchedule;
    
    // This is a demo flag to toggle between having rides and not having rides
    // In a real app, this would be determined by data from a database
    private boolean hasScheduledRides = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scheduled_commutes);
        
        // Apply window insets to prevent overlap with system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Initialize views
        tvNoScheduledRides = findViewById(R.id.tvNoScheduledRides);
        layoutScheduleHistory = findViewById(R.id.layoutScheduleHistory);
        btnNewSchedule = findViewById(R.id.btnNewSchedule);
        
        // Set up the UI based on whether there are scheduled rides
        updateUI();
        
        // Set up button click listener
        btnNewSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch the schedule booking activity
                Intent intent = new Intent(ScheduledCommutesActivity.this, ScheduleBookingActivity.class);
                startActivity(intent);
            }
        });
    }
    
    private void updateUI() {
        if (hasScheduledRides) {
            // Hide "No Scheduled Rides" message
            tvNoScheduledRides.setVisibility(View.GONE);
            // Show Schedule History section
            layoutScheduleHistory.setVisibility(View.VISIBLE);
        } else {
            // Show "No Scheduled Rides" message
            tvNoScheduledRides.setVisibility(View.VISIBLE);
            // Hide Schedule History section
            layoutScheduleHistory.setVisibility(View.GONE);
        }
    }
}