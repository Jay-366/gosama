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

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.util.Arrays;
import java.util.List;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.GeoApiContext;
import com.google.maps.DirectionsApi;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import com.google.maps.internal.PolylineEncoding;
import java.util.ArrayList;
import java.util.Random;

public class RideDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_RIDE = "com.example.gosama.RIDE";

    private TextView rideDate, ridePrice, pickupTime, pickupAddress, dropoffAddress, driverName, driverRating, contactDriver, seatsInfo;
    private FirebaseFirestore db;
    private TextInputEditText editPickupAddress, editDropoffAddress;
    private GoogleMap rideDetailMap;
    private ActivityResultLauncher<Intent> pickupAutocompleteLauncher;
    private ActivityResultLauncher<Intent> dropoffAutocompleteLauncher;
    private LatLng pickupLatLng;
    private LatLng dropoffLatLng;
    private Polyline currentRoutePolyline;
    private GeoApiContext geoApiContext;

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

        // Initialize editable address fields
        editPickupAddress = findViewById(R.id.editPickupAddress);
        editDropoffAddress = findViewById(R.id.editDropoffAddress);
        // Make inputs non-focusable to trigger click listener instead of keyboard
        editPickupAddress.setFocusable(false);
        editDropoffAddress.setFocusable(false);
        setupAutocompleteFields();

        // Add listeners to update price when addresses change
        editPickupAddress.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updatePriceIfReady(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
        editDropoffAddress.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updatePriceIfReady(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.rideDetailMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Ride ride = getIntent().getParcelableExtra(EXTRA_RIDE);
        if (ride != null) {
            populateRideDetails(ride);

            // Set editable fields

            Button requestRideButton = findViewById(R.id.request_ride_button);
            requestRideButton.setOnClickListener(v -> {
                // Use edited values if changed
                String pickupAddr = editPickupAddress.getText() != null ? editPickupAddress.getText().toString() : ride.getPickupAddress();
                String dropoffAddr = editDropoffAddress.getText() != null ? editDropoffAddress.getText().toString() : ride.getDropoffAddress();
                ride.setPickupAddress(pickupAddr);
                ride.setDropoffAddress(dropoffAddr);
                bookRide(ride);
            });
        }
        geoApiContext = new GeoApiContext.Builder()
            .apiKey(getString(R.string.maps_api_key))
            .build();
    }

    private void initViews() {
        rideDate = findViewById(R.id.rideDate);
        ridePrice = findViewById(R.id.ridePrice);
        pickupTime = findViewById(R.id.pickupTime);
        pickupAddress = findViewById(R.id.pickupAddress);
        dropoffAddress = findViewById(R.id.dropoffAddress);
        driverName = findViewById(R.id.driverName);
        driverRating = findViewById(R.id.driverRating);
        contactDriver = findViewById(R.id.contactDriver);
        seatsInfo = findViewById(R.id.seatsInfo);
    }

    private void setupAutocompleteFields() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
        pickupAutocompleteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    editPickupAddress.setText(place.getAddress());
                    pickupLatLng = place.getLatLng();
                    updateMapMarkersAndRoute();
                } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
                    Toast.makeText(this, "Error selecting location", Toast.LENGTH_SHORT).show();
                }
            });
        dropoffAutocompleteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    editDropoffAddress.setText(place.getAddress());
                    dropoffLatLng = place.getLatLng();
                    updateMapMarkersAndRoute();
                } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
                    Toast.makeText(this, "Error selecting location", Toast.LENGTH_SHORT).show();
                }
            });
        editPickupAddress.setOnClickListener(v -> {
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setCountry("MY")
                .build(this);
            pickupAutocompleteLauncher.launch(intent);
        });
        editDropoffAddress.setOnClickListener(v -> {
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setCountry("MY")
                .build(this);
            dropoffAutocompleteLauncher.launch(intent);
        });
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

    private double generateRandomPrice() {
        // Generate a random price between 5.00 and 8.00 (inclusive)
        double min = 5.00;
        double max = 8.00;
        double price = min + (new Random().nextDouble() * (max - min));
        return Math.round(price * 100.0) / 100.0;
    }

    private void updatePriceIfReady() {
        String pickup = editPickupAddress.getText() != null ? editPickupAddress.getText().toString().trim() : "";
        String dropoff = editDropoffAddress.getText() != null ? editDropoffAddress.getText().toString().trim() : "";
        if (!pickup.isEmpty() && !dropoff.isEmpty()) {
            double randomPrice = generateRandomPrice();
            ridePrice.setText(String.format("RM %.2f", randomPrice));
        } else {
            ridePrice.setText("");
        }
    }

    private void populateRideDetails(Ride ride) {
        rideDate.setText(ride.getDepartureDate());
        pickupTime.setText(ride.getDepartureTime());
        pickupAddress.setText(ride.getPickupAddress());
        dropoffAddress.setText(ride.getDropoffAddress());
        seatsInfo.setText("Max " + ride.getAvailableSeats() + " seats available");

        // Only show price if both addresses are filled
        updatePriceIfReady();
        driverRating.setText("4.8/5 - 13 ratings"); // Placeholder

        // Fetch driver name
        db.collection("users").document(ride.getDriverId()).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("username");
                    driverName.setText(name);
                    contactDriver.setText("Contact driver");
                } else {
                    driverName.setText("Kangyan Ong");
                    contactDriver.setText("Contact driver");
                }
            });
    }

    private void updateMapMarkersAndRoute() {
        if (rideDetailMap == null) return;
        rideDetailMap.clear();
        boolean hasPickup = pickupLatLng != null;
        boolean hasDropoff = dropoffLatLng != null;
        if (hasPickup) {
            rideDetailMap.addMarker(new MarkerOptions().position(pickupLatLng).title("Pickup Location"));
        }
        if (hasDropoff) {
            rideDetailMap.addMarker(new MarkerOptions().position(dropoffLatLng).title("Drop-off Location"));
        }
        if (hasPickup && hasDropoff) {
            // Draw route
            drawRoute();
            // Zoom to fit both markers
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(pickupLatLng);
            builder.include(dropoffLatLng);
            LatLngBounds bounds = builder.build();
            rideDetailMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
        } else if (hasPickup) {
            rideDetailMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLatLng, 15));
        } else if (hasDropoff) {
            rideDetailMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dropoffLatLng, 15));
        }
    }

    private void drawRoute() {
        if (pickupLatLng == null || dropoffLatLng == null) return;
        if (currentRoutePolyline != null) currentRoutePolyline.remove();
        new Thread(() -> {
            try {
                com.google.maps.model.LatLng origin = new com.google.maps.model.LatLng(pickupLatLng.latitude, pickupLatLng.longitude);
                com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(dropoffLatLng.latitude, dropoffLatLng.longitude);
                DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                        .mode(TravelMode.DRIVING)
                        .origin(origin)
                        .destination(destination)
                        .await();
                if (result.routes != null && result.routes.length > 0) {
                    String encodedPolyline = result.routes[0].overviewPolyline.getEncodedPath();
                    java.util.List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(encodedPolyline);
                    java.util.List<LatLng> newDecodedPath = new ArrayList<>();
                    for (com.google.maps.model.LatLng latlng : decodedPath) {
                        newDecodedPath.add(new LatLng(latlng.lat, latlng.lng));
                    }
                    runOnUiThread(() -> {
                        if (rideDetailMap != null) {
                            PolylineOptions polylineOptions = new PolylineOptions().addAll(newDecodedPath);
                            polylineOptions.color(android.graphics.Color.BLUE);
                            polylineOptions.width(15);
                            currentRoutePolyline = rideDetailMap.addPolyline(polylineOptions);
                        }
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(RideDetailActivity.this, "Error drawing route", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        rideDetailMap = googleMap;
        updateMapMarkersAndRoute();
    }
}
