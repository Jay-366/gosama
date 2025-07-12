package com.example.gosama;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.cardview.widget.CardView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LiveTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    // UI Elements
    private ProgressBar progressBar;
    private TextView progressText;
    private Button sosButton;
    private Button btnShareRide;
    private Button btnEmergencyContacts;
    private GoogleMap googleMap;

    // Ride Information
    private String driverName = "John Lee";
    private String vehicleInfo = "Toyota Camry (SGX1234A)";
    private String driverRating = "4.8★";
    private String pickupLocation = "Changi Airport Terminal 1";
    private String dropoffLocation = "Marina Bay Sands";

    // Emergency Contacts
    private static class EmergencyContact {
        String name;
        String relationship;
        String phone;

        EmergencyContact(String name, String relationship, String phone) {
            this.name = name;
            this.relationship = relationship;
            this.phone = phone;
        }
    }

    private EmergencyContact[] emergencyContacts = {
            new EmergencyContact("Jane Lee", "Mother", "91234567"),
            new EmergencyContact("Michael Lee", "Father", "91234568"),
            new EmergencyContact("Sarah Lee", "Sister", "91234569")
    };

    // Tracking variables
    private Handler handler = new Handler(Looper.getMainLooper());
    private int progress = 0;
    private Random random = new Random();
    private boolean isVehicleStopped = false;
    private boolean userConfirmedSafe = false;
    private static final int TOTAL_DURATION_MS = 10000; // 10 seconds for testing
    private static final int UPDATE_INTERVAL_MS = 100; // Update every 0.1 seconds
    private static final int PROGRESS_INCREMENT = 5; // Increment progress by 5% each update
    private static final int SAFETY_CHECK_DELAY_MS = 1000; // 1 second delay before safety check

    private AlertDialog safetyDialog;
    private CountDownTimer safetyTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_live_tracking);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        initializeViews();
        setupButtonListeners();

        // Initially hide the safety check card
        // safetyCheckCard.setVisibility(View.GONE); // This line is removed

        // Set up progress tracking
        progressBar.setMax(100);
        startTracking();
    }

    private void initializeViews() {
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
        sosButton = findViewById(R.id.sosButton);
        btnShareRide = findViewById(R.id.btnShareRide);
        btnEmergencyContacts = findViewById(R.id.btnEmergencyContacts);
    }

    private void setupButtonListeners() {
        // Set up SOS button
        sosButton.setOnClickListener(v -> showSOSConfirmation(false));

        // Set up safety confirmation
        // confirmSafetyButton.setOnClickListener(v -> { // This line is removed
        //     safetyCheckCard.setVisibility(View.GONE); // This line is removed
        //     isVehicleStopped = false; // This line is removed
        //     userConfirmedSafe = true;  // User confirmed they're safe, no more safety checks // This line is removed
        //     handler.removeCallbacksAndMessages(null);  // Remove any pending safety check countdowns // This line is removed
        //     startTracking();  // Resume tracking without safety checks // This line is removed
        // }); // This line is removed

        // Set up not safe button
        // notSafeButton.setOnClickListener(v -> showSOSConfirmation(true)); // This line is removed

        // Share ride button
        btnShareRide.setOnClickListener(v -> shareRideDetails());

        // Emergency contacts button
        btnEmergencyContacts.setOnClickListener(v -> showEmergencyContactsDialog());
    }

    private void showEmergencyContactsDialog() {
        // Create items array for the dialog
        String[] contactItems = new String[emergencyContacts.length];
        for (int i = 0; i < emergencyContacts.length; i++) {
            EmergencyContact contact = emergencyContacts[i];
            contactItems[i] = String.format("%s (%s)", contact.name, contact.relationship);
        }

        // Show contact selection dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Emergency Contact")
                .setItems(contactItems, (dialog, which) -> {
                    // Show confirmation dialog for the selected contact
                    showEmergencyContactConfirmation(emergencyContacts[which]);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEmergencyContactConfirmation(EmergencyContact contact) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Call")
                .setMessage(String.format("Do you want to call %s (%s)?", contact.name, contact.relationship))
                .setIcon(android.R.drawable.ic_menu_call)
                .setPositiveButton("Call", (dialog, which) -> {
                    // Make the call
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + contact.phone));
                    startActivity(intent);

                    // Also send them a message with ride details
                    sendEmergencyMessage(contact);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendEmergencyMessage(EmergencyContact contact) {
        // Calculate estimated arrival time
        int remainingSeconds = (int) ((100 - progress) * 0.6);

        // Create emergency message
        String emergencyMessage = String.format(
                "🚨 Emergency Alert from GoSana!\n\n" +
                        "Your family member is currently in a ride:\n" +
                        "Driver: %s\n" +
                        "Vehicle: %s\n" +
                        "Current Location: Between %s and %s\n" +
                        "Trip Progress: %d%%\n" +
                        "Estimated Arrival: %d seconds\n\n" +
                        "Please maintain contact and ensure their safety.",
                driverName,
                vehicleInfo,
                pickupLocation,
                dropoffLocation,
                progress,
                remainingSeconds
        );

        // Create SMS intent
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + contact.phone));
        smsIntent.putExtra("sms_body", emergencyMessage);
        startActivity(smsIntent);
    }

    private void shareRideDetails() {
        // Calculate estimated arrival time
        int remainingSeconds = (int) ((100 - progress) * 0.6);
        String estimatedArrival = remainingSeconds + " seconds";

        // Create share message
        String shareMessage = String.format(
                "🚗 Tracking my ride with GoSana!\n\n" +
                        "Driver: %s (%s)\n" +
                        "Vehicle: %s\n" +
                        "From: %s\n" +
                        "To: %s\n" +
                        "Estimated Arrival: %s\n" +
                        "Trip Progress: %d%%\n\n" +
                        "Track my journey in real-time for safety! 🛡️\n" +
                        "Download GoSana: [App Store Link]",
                driverName,
                driverRating,
                vehicleInfo,
                pickupLocation,
                dropoffLocation,
                estimatedArrival,
                progress
        );

        // Create share intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Track My GoSana Ride 🚗");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

        // Show share dialog
        startActivity(Intent.createChooser(shareIntent, "Share Ride Details Via"));
    }

    private void showSOSConfirmation(boolean fromSafetyCheck) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Emergency Confirmation")
                .setMessage("Do you want to contact emergency services?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes, Call Police", (dialog, which) -> {
                    if (fromSafetyCheck) {
                        // safetyCheckCard.setVisibility(View.GONE); // This line is removed
                    }
                    triggerSOS();
                    // Call emergency number
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:911"));
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    if (fromSafetyCheck) {
                        // If cancelled from safety check, show the safety check again
                        // startSafetyCheckCountdown(); // This line is removed
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void startTracking() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Always update progress if not stopped
                if (!isVehicleStopped) {
                    progress += PROGRESS_INCREMENT;
                    updateProgress();
                }

                if (progress >= 100) {
                    // Trip completed, go directly to rating page
                    goToRatingPage();
                    return;
                }

                // Only check for stops if user hasn't confirmed they're safe
                if (!userConfirmedSafe && !isVehicleStopped && random.nextFloat() < 0.3) {
                    isVehicleStopped = true;
                    vehicleStopped();
                }

                if (progress < 100) {
                    handler.postDelayed(this, UPDATE_INTERVAL_MS);
                }
            }
        }, UPDATE_INTERVAL_MS);
    }

    private void vehicleStopped() {
        // Only show safety check if user hasn't confirmed they're safe
        if (!userConfirmedSafe) {
            handler.postDelayed(() -> {
                if (isVehicleStopped && !userConfirmedSafe) {
                    showSafetyCheckDialog();
                }
            }, SAFETY_CHECK_DELAY_MS);
        }
    }

    private void showSafetyCheckDialog() {
        if (safetyDialog != null && safetyDialog.isShowing()) return;
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_safety_check, null);
        TextView tvTimer = dialogView.findViewById(R.id.tvSafetyTimer);
        Button btnSafe = dialogView.findViewById(R.id.btnSafe);
        Button btnNotSafe = dialogView.findViewById(R.id.btnNotSafe);

        safetyDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        final int[] countdown = {30};
        tvTimer.setText(countdown[0] + " seconds to respond");
        safetyTimer = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                countdown[0] = (int) (millisUntilFinished / 1000);
                tvTimer.setText(countdown[0] + " seconds to respond");
            }
            public void onFinish() {
                tvTimer.setText("Time's up!");
                if (safetyDialog != null && safetyDialog.isShowing()) {
                    safetyDialog.dismiss();
                }
                showSOSConfirmation(true);
            }
        };
        safetyTimer.start();

        btnSafe.setOnClickListener(v -> {
            userConfirmedSafe = true;
            isVehicleStopped = false;
            if (safetyTimer != null) safetyTimer.cancel();
            if (safetyDialog != null && safetyDialog.isShowing()) safetyDialog.dismiss();
            handler.removeCallbacksAndMessages(null);
            startTracking();
        });
        btnNotSafe.setOnClickListener(v -> {
            if (safetyTimer != null) safetyTimer.cancel();
            if (safetyDialog != null && safetyDialog.isShowing()) safetyDialog.dismiss();
            showSOSConfirmation(true);
        });

        safetyDialog.show();
    }

    private void startSafetyCheckCountdown() {
        // No longer needed, handled in dialog
    }

    private void updateProgress() {
        progressBar.setProgress(progress);
        // Show progress as 1 minute total duration
        int remainingSeconds = (int) ((100 - progress) * 0.6);
        progressText.setText("Estimated arrival: " + remainingSeconds + " seconds");
    }

    private void triggerSOS() {
        // Update UI to show SOS is triggered
        sosButton.setText("SOS Triggered!");
        sosButton.setEnabled(false);
        // Here you would typically:
        // 1. Contact emergency services - Now handled in showSOSConfirmation()
        // 2. Send location to authorities
        // 3. Alert emergency contacts
    }

    private void showTripCompletionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Trip Completed!")
                .setMessage("You've arrived at your destination.\nPlease rate your experience.")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setCancelable(false)
                .setPositiveButton("Rate Now", (dialog, which) -> {
                    goToRatingPage();
                })
                .show();
    }

    private void goToRatingPage() {
        Intent intent = new Intent(this, RateRideActivity.class);
        // Pass relevant trip information to rating page
        intent.putExtra("DRIVER_NAME", driverName);
        intent.putExtra("VEHICLE_INFO", vehicleInfo);
        intent.putExtra("DRIVER_RATING", driverRating);
        intent.putExtra("PICKUP_LOCATION", pickupLocation);
        intent.putExtra("DROPOFF_LOCATION", dropoffLocation);
        startActivity(intent);
        finish(); // Close the tracking activity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (safetyTimer != null) safetyTimer.cancel();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        // Implement map setup
    }
}