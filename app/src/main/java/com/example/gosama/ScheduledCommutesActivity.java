package com.example.gosama;

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
import androidx.cardview.widget.CardView;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.ImageView;

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

        // Show matched ride card if there is a match (demo: always show)
        View matchedCard = findViewById(R.id.includeMatchedRideCardIncoming);
        matchedCard.setVisibility(View.VISIBLE);
        Button acceptButton = matchedCard.findViewById(R.id.acceptButton);
        Button rejectButton = matchedCard.findViewById(R.id.rejectButton);
        LinearLayout buttonArea = matchedCard.findViewById(R.id.buttonArea);
        CardView cardView = matchedCard.findViewById(R.id.matchedRideCard);
        ImageView chatIcon = matchedCard.findViewById(R.id.ivChat);
        TextView headerTitle = matchedCard.findViewById(R.id.tvIncomingOfferHeader);

        acceptButton.setOnClickListener(v -> {
            // Remove card shadow
            if (cardView != null) {
                cardView.setCardElevation(0f);
                cardView.setBackgroundResource(R.drawable.btn_border_gray_green);
            }
            // Change header title
            if (headerTitle != null) {
                headerTitle.setText("Confirmed Rides");
            }
            // Remove both buttons and add a single Cancel button
            buttonArea.removeAllViews();
            Button cancelButton = new Button(this);
            cancelButton.setText("Cancel");
            cancelButton.setTextColor(0xFF222222); // dark gray
            cancelButton.setBackgroundColor(0xFFFFFFFF); // white
            cancelButton.setPaintFlags(cancelButton.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 0;
            params.gravity = android.view.Gravity.END;
            cancelButton.setLayoutParams(params);
            buttonArea.addView(cancelButton);
            cancelButton.setOnClickListener(cancelV -> matchedCard.setVisibility(View.GONE));
        });
        rejectButton.setOnClickListener(v -> matchedCard.setVisibility(View.GONE));
        if (chatIcon != null) {
            chatIcon.setOnClickListener(v -> {
                Intent intent = new Intent(this, DriverChatActivity.class);
                startActivity(intent);
            });
        }

        // Hardcode matched ride info
        TextView driverNameRating = matchedCard.findViewById(R.id.driverNameRating);
        TextView tripPrice = matchedCard.findViewById(R.id.tripPrice);
        TextView pickupTime = matchedCard.findViewById(R.id.pickupTime);
        TextView pickupPoint = matchedCard.findViewById(R.id.pickupPoint);
        TextView destinationPoint = matchedCard.findViewById(R.id.destinationPoint);
        driverNameRating.setText("Kangyan Ong | 4.9 ★");
        tripPrice.setText("RM 8.50");
        pickupTime.setText("07 Jul 2025 2:15 PM");
        pickupPoint.setText("From: 1, Jalan Taylor's");
        destinationPoint.setText("To: IOI Mall Puchong");
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

    private void showBookedConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Booked!")
            .setMessage("Your ride has been confirmed!")
            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
            .show();
    }
} 