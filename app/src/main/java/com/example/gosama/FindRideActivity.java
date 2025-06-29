package com.example.gosama;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FindRideActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    
    private TextInputEditText pickupLocationInput;
    private TextInputEditText dropOffLocationInput;
    private TextInputEditText dateInput;
    private TextInputEditText timeInput;
    private RadioGroup seatsRadioGroup;
    private Button findRideButton;
    private GoogleMap googleMap;
    
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_ride);
        
        // Initialize background thread executor and main thread handler
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Set up toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Find a Ride");
        }

        // Initialize views
        pickupLocationInput = findViewById(R.id.pickupLocationInput);
        dropOffLocationInput = findViewById(R.id.dropOffLocationInput);
        dateInput = findViewById(R.id.dateInput);
        timeInput = findViewById(R.id.timeInput);
        seatsRadioGroup = findViewById(R.id.seatsRadioGroup);
        findRideButton = findViewById(R.id.findRideButton);

        // Initialize map in a background thread
        executorService.execute(() -> {
            // Initialize map on the main thread after background work
            mainHandler.post(() -> {
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                if (mapFragment != null) {
                    mapFragment.getMapAsync(this);
                }
            });
        });
        
        // Set up date picker
        setupDatePicker();
        
        // Set up time picker
        setupTimePicker();
        
        // Set up find ride button
        setupFindRideButton();
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
    
    private void setupFindRideButton() {
        findRideButton.setOnClickListener(v -> {
            // Validate inputs
            if (validateInputs()) {
                // Run search in background thread
                executorService.execute(() -> {
                    // Simulate search process
                    try {
                        Thread.sleep(500); // Simulate network delay
                        
                        // Update UI on main thread
                        mainHandler.post(() -> {
                            Toast.makeText(FindRideActivity.this, "Searching for rides...", Toast.LENGTH_SHORT).show();
                            // TODO: Implement actual search functionality
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
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
        
        // Run map configuration in background thread
        executorService.execute(() -> {
            // Set default location to a central location (can be updated based on user's location)
            final LatLng defaultLocation = new LatLng(3.1390, 101.6869); // Kuala Lumpur
            
            // Update UI on main thread
            mainHandler.post(() -> {
                // Enable zoom controls and other UI settings
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setMapToolbarEnabled(false); // Disable directions button to prevent crashes
                
                // Move camera to default location
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12));
                
                // Check for location permission
                enableMyLocation();
            });
        });
    }
    
    private void enableMyLocation() {
        // Check if permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED) {
            if (googleMap != null) {
                googleMap.setMyLocationEnabled(true);
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

    private boolean validateInputs() {
        boolean isValid = true;

        if (pickupLocationInput.getText().toString().trim().isEmpty()) {
            pickupLocationInput.setError("Please enter pickup location");
            isValid = false;
        }

        if (dropOffLocationInput.getText().toString().trim().isEmpty()) {
            dropOffLocationInput.setError("Please enter drop-off location");
            isValid = false;
        }

        if (dateInput.getText().toString().trim().isEmpty()) {
            dateInput.setError("Please select a date");
            isValid = false;
        }

        if (timeInput.getText().toString().trim().isEmpty()) {
            timeInput.setError("Please select a time");
            isValid = false;
        }

        if (seatsRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select number of seats", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }
}
