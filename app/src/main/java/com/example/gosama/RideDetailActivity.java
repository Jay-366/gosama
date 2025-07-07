package com.example.gosama;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RideDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RIDE = "com.example.gosama.RIDE";

    private TextView rideDate, ridePrice, pickupTime, pickupAddress, dropoffTime, dropoffAddress, driverName, driverRating, contactDriver, seatsInfo;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        db = FirebaseFirestore.getInstance();
        initViews();

        Ride ride = getIntent().getParcelableExtra(EXTRA_RIDE);
        if (ride != null) {
            populateRideDetails(ride);

            Button requestRideButton = findViewById(R.id.request_ride_button);
            requestRideButton.setOnClickListener(v -> {
                bookRide(ride);
            });
        }
    }

    private void initViews() {
        rideDate = findViewById(R.id.rideDate);
        ridePrice = findViewById(R.id.ridePrice);
        pickupTime = findViewById(R.id.pickupTime);
        pickupAddress = findViewById(R.id.pickupAddress);
        dropoffTime = findViewById(R.id.dropoffTime);
        dropoffAddress = findViewById(R.id.dropoffAddress);
        driverName = findViewById(R.id.driverName);
        driverRating = findViewById(R.id.driverRating);
        contactDriver = findViewById(R.id.contactDriver);
        seatsInfo = findViewById(R.id.seatsInfo);
    }

    private void bookRide(Ride ride) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to book a ride", Toast.LENGTH_SHORT).show();
            return;
        }

        String passengerId = currentUser.getUid();

        Map<String, Object> booking = new HashMap<>();
        booking.put("rideId", ride.getDocumentId());
        booking.put("driverId", ride.getDriverId());
        booking.put("passengerId", passengerId);
        booking.put("pickupAddress", ride.getPickupAddress());
        booking.put("dropoffAddress", ride.getDropoffAddress());
        booking.put("pickupLocation", ride.getPickupLocation());
        booking.put("dropoffLocation", ride.getDropoffLocation());
        booking.put("departureDate", ride.getDepartureDate());
        booking.put("departureTime", ride.getDepartureTime());
        booking.put("bookingTimestamp", FieldValue.serverTimestamp());

        db.collection("confirmed_rides")
                .add(booking)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(RideDetailActivity.this, "Ride booked successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RideDetailActivity.this, BookingConfirmationActivity.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RideDetailActivity.this, "Error booking ride: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void populateRideDetails(Ride ride) {
        rideDate.setText(ride.getDepartureDate());
        pickupTime.setText(ride.getDepartureTime());
        pickupAddress.setText(ride.getPickupAddress());
        dropoffAddress.setText(ride.getDropoffAddress());
        seatsInfo.setText("Max " + ride.getAvailableSeats() + " seats available");

        // Placeholders for data not yet in our model
        ridePrice.setText("RM 15"); // Placeholder
        dropoffTime.setText(ride.getDepartureTime()); // Placeholder, we only have one time
        driverRating.setText("No ratings yet"); // Placeholder

        // Fetch driver name
        db.collection("Users").document(ride.getDriverId()).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    driverName.setText(name);
                    contactDriver.setText("Contact " + name);
                } else {
                    driverName.setText("Unknown Driver");
                    contactDriver.setText("Contact driver");
                }
            });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
