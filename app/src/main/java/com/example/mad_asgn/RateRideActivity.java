package com.example.mad_asgn;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class RateRideActivity extends AppCompatActivity {

    private TextView driverNameText;
    private TextView vehicleInfoText;
    private TextView tripDetailsText;
    private EditText feedbackText;
    private RatingBar ratingBar;
    private ChipGroup highlightChipGroup;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_ride);

        initializeViews();
        loadTripInformation();
        setupSubmitButton();
    }

    private void initializeViews() {
        driverNameText = findViewById(R.id.driverNameText);
        vehicleInfoText = findViewById(R.id.vehicleInfoText);
        tripDetailsText = findViewById(R.id.tripDetailsText);
        ratingBar = findViewById(R.id.ratingBar);
        feedbackText = findViewById(R.id.feedbackText);
        highlightChipGroup = findViewById(R.id.highlightChipGroup);
        submitButton = findViewById(R.id.submitButton);
    }

    private void loadTripInformation() {
        // Get trip information from intent
        Intent intent = getIntent();
        String driverName = intent.getStringExtra("DRIVER_NAME");
        String vehicleInfo = intent.getStringExtra("VEHICLE_INFO");
        String driverRating = intent.getStringExtra("DRIVER_RATING");
        String pickupLocation = intent.getStringExtra("PICKUP_LOCATION");
        String dropoffLocation = intent.getStringExtra("DROPOFF_LOCATION");

        // Display trip information
        driverNameText.setText(String.format("Driver: %s (%s)", driverName, driverRating));
        vehicleInfoText.setText(String.format("Vehicle: %s", vehicleInfo));
        tripDetailsText.setText(String.format("From: %s\nTo: %s", pickupLocation, dropoffLocation));
    }

    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> {
            // Get rating and feedback
            float rating = ratingBar.getRating();
            String feedback = feedbackText.getText().toString();
            
            // Get selected highlights
            StringBuilder highlights = new StringBuilder();
            for (int i = 0; i < highlightChipGroup.getChildCount(); i++) {
                Chip chip = (Chip) highlightChipGroup.getChildAt(i);
                if (chip.isChecked()) {
                    if (highlights.length() > 0) {
                        highlights.append(", ");
                    }
                    highlights.append(chip.getText());
                }
            }

            // Show thank you dialog
            showThankYouDialog(rating);
        });
    }

    private void showThankYouDialog(float rating) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Thank You!")
               .setMessage(String.format("Your %.1f star rating has been submitted.\nThank you for using GoSana!", rating))
               .setPositiveButton("Done", (dialog, which) -> {
                   // Return to main activity
                   Intent intent = new Intent(this, MainActivity.class);
                   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                   startActivity(intent);
                   finish();
               })
               .setCancelable(false)
               .show();
    }
} 