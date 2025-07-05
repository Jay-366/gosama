package com.example.gosama;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;                                                                                                                       
import com.google.android.material.timepicker.TimeFormat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.gms.maps.model.LatLngBounds;
import java.util.Arrays;
import java.util.List;
import android.content.Intent;
import android.graphics.Color;
import com.google.maps.GeoApiContext;
import com.google.maps.DirectionsApi;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.widget.RadioButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import java.util.HashMap;
import java.util.Map;

public class OfferRideActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private ActivityResultLauncher<Intent> pickupAutocompleteLauncher;
    private ActivityResultLauncher<Intent> dropoffAutocompleteLauncher;
    private LatLng pickupLatLng;
    private LatLng dropOffLatLng;
    private GeoApiContext geoApiContext = null;
    private Polyline currentRoutePolyline = null;
    
    private TextInputEditText pickupLocationInput;
    private TextInputEditText dropOffLocationInput;
    private TextInputEditText dateInput;
    private TextInputEditText timeInput;
    private RadioGroup seatsRadioGroup;
    private Button offerRideButton;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_ride);

        // Initialize GeoApiContext for Directions API
        if (geoApiContext == null) {
            geoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.maps_api_key))
                    .build();
        }
        
        // Initialize background thread executor and main thread handler
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Offer a Ride");
        }

        // Initialize views
        pickupLocationInput = findViewById(R.id.pickupLocationInput);
        dropOffLocationInput = findViewById(R.id.dropOffLocationInput);
        dateInput = findViewById(R.id.dateInput);
        timeInput = findViewById(R.id.timeInput);
        seatsRadioGroup = findViewById(R.id.seatsRadioGroup);
        offerRideButton = findViewById(R.id.offerRideButton);

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        
        // Set up date picker
        setupDatePicker();
        
        // Set up time picker
        setupTimePicker();
        
        // Set up offer ride button
        setupOfferRideButton();

        // Set up autocomplete fields
        setupAutocompleteFields();
    }

    private void setupDatePicker() {
        dateInput.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                dateInput.setText(sdf.format(new Date(selection)));
            });

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });
    }
    
    private void setupTimePicker() {
        timeInput.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(12)
                    .setMinute(0)
                    .setTitleText("Select time")
                    .build();

            timePicker.addOnPositiveButtonClickListener(dialog -> {
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();
                String amPm = hour >= 12 ? "PM" : "AM";
                hour = hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour);
                timeInput.setText(String.format(Locale.getDefault(), "%d:%02d %s", hour, minute, amPm));
            });

            timePicker.show(getSupportFragmentManager(), "TIME_PICKER");
        });
    }
    
    private void setupOfferRideButton() {
        offerRideButton.setOnClickListener(v -> {
            if (validateInputs()) {
                saveRideToFirestore();
            }
        });
    }

    private void saveRideToFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to offer a ride.", Toast.LENGTH_SHORT).show();
            return;
        }

        String driverId = currentUser.getUid();
        String pickupAddress = pickupLocationInput.getText().toString();
        String dropoffAddress = dropOffLocationInput.getText().toString();
        String date = dateInput.getText().toString();
        String time = timeInput.getText().toString();

        int selectedSeatId = seatsRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedSeatId);
        // Extract just the number from the button's text
        int seats = Integer.parseInt(selectedRadioButton.getText().toString().replaceAll("[^0-9]", ""));

        Map<String, Object> ride = new HashMap<>();
        ride.put("driverId", driverId);
        ride.put("pickupAddress", pickupAddress);
        ride.put("dropoffAddress", dropoffAddress);
        ride.put("pickupLocation", new GeoPoint(pickupLatLng.latitude, pickupLatLng.longitude));
        ride.put("dropoffLocation", new GeoPoint(dropOffLatLng.latitude, dropOffLatLng.longitude));
        ride.put("departureDate", date);
        ride.put("departureTime", time);
        ride.put("availableSeats", seats);
        ride.put("timestamp", FieldValue.serverTimestamp());

        db.collection("Rides")
                .add(ride)
                .addOnSuccessListener(documentReference -> {
                    Log.d("OfferRideActivity", "Ride offered with ID: " + documentReference.getId());
                    Toast.makeText(OfferRideActivity.this, "Ride offered successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to the previous screen
                })
                .addOnFailureListener(e -> {
                    Log.w("OfferRideActivity", "Error adding ride", e);
                    Toast.makeText(OfferRideActivity.this, "Error offering ride. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown executor service to prevent memory leaks
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        
        // Set default location to a central location (can be updated based on user's location)
        final LatLng defaultLocation = new LatLng(3.1390, 101.6869); // Kuala Lumpur

        // Enable zoom controls and other UI settings
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false); // Disable directions button to prevent crashes

        // Move camera to default location
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12));

        // Check for location permission
        enableMyLocation();
    }
    
    private void enableMyLocation() {
        // Check if permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (googleMap != null) {
                googleMap.setMyLocationEnabled(true);
                fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, new CancellationTokenSource().getToken())
                    .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        Log.d("OfferRideActivity", "Current location fetched: " + location.getLatitude() + ", " + location.getLongitude());
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.addMarker(new MarkerOptions().position(currentLatLng).title("Current Location"));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                    } else {
                        Log.d("OfferRideActivity", "Current location is null.");
                        Toast.makeText(OfferRideActivity.this, "Unable to get current location.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupAutocompleteFields() {
        // Make inputs non-focusable to trigger click listener instead of keyboard
        pickupLocationInput.setFocusable(false);
        dropOffLocationInput.setFocusable(false);

        // Define the fields to be returned from the autocomplete request
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);

        // Initialize launcher for pickup location
        pickupAutocompleteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());
                        pickupLocationInput.setText(place.getAddress());
                        pickupLocationInput.setError(null); // Clear error
                        pickupLatLng = place.getLatLng();
                        if (pickupLatLng != null) {
                            googleMap.addMarker(new MarkerOptions().position(pickupLatLng).title("Pickup Location"));
                            zoomToMarkers();
                            drawRoute();
                        }
                    } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
                        Toast.makeText(this, "Error selecting location", Toast.LENGTH_SHORT).show();
                    }
                });

        // Initialize launcher for drop-off location
        dropoffAutocompleteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());
                        dropOffLocationInput.setText(place.getAddress());
                        dropOffLocationInput.setError(null); // Clear error
                        dropOffLatLng = place.getLatLng();
                        if (dropOffLatLng != null) {
                            googleMap.addMarker(new MarkerOptions().position(dropOffLatLng).title("Drop-off Location"));
                            zoomToMarkers();
                            drawRoute();
                        }
                    } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
                        Toast.makeText(this, "Error selecting location", Toast.LENGTH_SHORT).show();
                    }
                });

        // Set click listeners to launch autocomplete
        pickupLocationInput.setOnClickListener(v -> {
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .setCountry("MY") // Optional: Restrict search to a specific country (e.g., Malaysia)
                    .build(this);
            pickupAutocompleteLauncher.launch(intent);
        });

        dropOffLocationInput.setOnClickListener(v -> {
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .setCountry("MY") // Optional: Restrict search to a specific country (e.g., Malaysia)
                    .build(this);
            dropoffAutocompleteLauncher.launch(intent);
        });
    }

    private void drawRoute() {
        if (pickupLatLng == null || dropOffLatLng == null) {
            return; // Not enough info to draw a route
        }

        // Clear any existing route before drawing a new one
        if (currentRoutePolyline != null) {
            currentRoutePolyline.remove();
        }

        // Run network request on a background thread
        executorService.execute(() -> {
            try {
                com.google.maps.model.LatLng origin = new com.google.maps.model.LatLng(pickupLatLng.latitude, pickupLatLng.longitude);
                com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(dropOffLatLng.latitude, dropOffLatLng.longitude);

                DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                        .mode(TravelMode.DRIVING)
                        .origin(origin)
                        .destination(destination)
                        .await();

                if (result.routes != null && result.routes.length > 0) {
                    String encodedPolyline = result.routes[0].overviewPolyline.getEncodedPath();
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(encodedPolyline);

                    List<com.google.android.gms.maps.model.LatLng> newDecodedPath = new ArrayList<>();
                    for(com.google.maps.model.LatLng latlng : decodedPath){
                        newDecodedPath.add(new com.google.android.gms.maps.model.LatLng(latlng.lat, latlng.lng));
                    }

                    // Draw on map on the main thread
                    mainHandler.post(() -> {
                        if (googleMap != null) {
                            PolylineOptions polylineOptions = new PolylineOptions().addAll(newDecodedPath);
                            polylineOptions.color(Color.BLUE);
                            polylineOptions.width(15);
                            currentRoutePolyline = googleMap.addPolyline(polylineOptions);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("OfferRideActivity", "Error fetching directions", e);
                mainHandler.post(() -> Toast.makeText(OfferRideActivity.this, "Error drawing route", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void zoomToMarkers() {
        if (googleMap == null) return;

        if (pickupLatLng != null && dropOffLatLng != null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(pickupLatLng);
            builder.include(dropOffLatLng);
            LatLngBounds bounds = builder.build();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150)); // 150 is padding in pixels
        } else if (pickupLatLng != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLatLng, 15));
        } else if (dropOffLatLng != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dropOffLatLng, 15));
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (pickupLocationInput.getText().toString().trim().isEmpty()) {
            pickupLocationInput.setError("Pickup location is required");
            isValid = false;
        } else {
            pickupLocationInput.setError(null);
        }

        if (dropOffLocationInput.getText().toString().trim().isEmpty()) {
            dropOffLocationInput.setError("Drop-off location is required");
            isValid = false;
        } else {
            dropOffLocationInput.setError(null);
        }

        if (dateInput.getText().toString().trim().isEmpty()) {
            dateInput.setError("Date is required");
            isValid = false;
        } else {
            dateInput.setError(null);
        }

        if (timeInput.getText().toString().trim().isEmpty()) {
            timeInput.setError("Time is required");
            isValid = false;
        } else {
            timeInput.setError(null);
        }

        if (seatsRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select the number of available seats", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (pickupLatLng == null) {
            Toast.makeText(this, "Please select a valid pickup location from the suggestions", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (dropOffLatLng == null) {
            Toast.makeText(this, "Please select a valid drop-off location from the suggestions", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }
}
