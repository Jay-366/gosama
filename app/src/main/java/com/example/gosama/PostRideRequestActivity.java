package com.example.gosama;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.CancellationTokenSource;
import java.util.Arrays;
import java.util.List;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.maps.GeoApiContext;
import com.google.maps.DirectionsApi;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import java.util.ArrayList;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

public class PostRideRequestActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private TextInputEditText pickupLocationInput, dropOffLocationInput, dateInput, timeInput;
    private GoogleMap googleMap;
    private LatLng pickupLatLng, dropOffLatLng;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private ActivityResultLauncher<Intent> pickupAutocompleteLauncher;
    private ActivityResultLauncher<Intent> dropoffAutocompleteLauncher;
    private Polyline currentRoutePolyline = null;
    private GeoApiContext geoApiContext = null;
    private ExecutorService executorService;
    private Handler mainHandler;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_ride_request);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Post Ride Request");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        pickupLocationInput = findViewById(R.id.pickupLocationInput);
        dropOffLocationInput = findViewById(R.id.dropOffLocationInput);
        dateInput = findViewById(R.id.dateInput);
        timeInput = findViewById(R.id.timeInput);
        Button postRideButton = findViewById(R.id.postRideButton);

        db = FirebaseFirestore.getInstance();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Initialize GeoApiContext for Directions API
        if (geoApiContext == null) {
            geoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.maps_api_key))
                    .build();
        }

        // Date and time pickers (reuse your previous logic)
        dateInput.setOnClickListener(v -> showDatePicker());
        timeInput.setOnClickListener(v -> showTimePicker());

        // Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Autocomplete
        setupAutocompleteFields();

        // Post button
        postRideButton.setOnClickListener(v -> postRideRequest());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
            dateInput.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String time = String.format("%02d:%02d", hourOfDay, minute);
            timeInput.setText(time);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
        dialog.show();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        LatLng defaultLocation = new LatLng(3.1390, 101.6869); // Kuala Lumpur
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12));
        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (googleMap != null) {
                googleMap.setMyLocationEnabled(true);
                fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, new CancellationTokenSource().getToken())
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                googleMap.addMarker(new MarkerOptions().position(currentLatLng).title("Current Location"));
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                            } else {
                                Toast.makeText(this, "Unable to get current location.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
        pickupLocationInput.setFocusable(false);
        dropOffLocationInput.setFocusable(false);
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
        pickupAutocompleteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());
                        pickupLocationInput.setText(place.getAddress());
                        pickupLocationInput.setError(null);
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
        dropoffAutocompleteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());
                        dropOffLocationInput.setText(place.getAddress());
                        dropOffLocationInput.setError(null);
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
        pickupLocationInput.setOnClickListener(v -> {
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .setCountry("MY")
                    .build(this);
            pickupAutocompleteLauncher.launch(intent);
        });
        dropOffLocationInput.setOnClickListener(v -> {
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .setCountry("MY")
                    .build(this);
            dropoffAutocompleteLauncher.launch(intent);
        });
    }

    private void drawRoute() {
        if (pickupLatLng == null || dropOffLatLng == null) {
            return;
        }
        if (currentRoutePolyline != null) {
            currentRoutePolyline.remove();
        }
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
                    mainHandler.post(() -> {
                        if (googleMap != null) {
                            PolylineOptions polylineOptions = new PolylineOptions().addAll(newDecodedPath);
                            polylineOptions.color(0xFF0057A8);
                            polylineOptions.width(15);
                            currentRoutePolyline = googleMap.addPolyline(polylineOptions);
                        }
                    });
                }
            } catch (Exception e) {
                mainHandler.post(() -> Toast.makeText(PostRideRequestActivity.this, "Error drawing route", Toast.LENGTH_SHORT).show());
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
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
        } else if (pickupLatLng != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLatLng, 15));
        } else if (dropOffLatLng != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dropOffLatLng, 15));
        }
    }

    private void postRideRequest() {
        String pickup = pickupLocationInput.getText() != null ? pickupLocationInput.getText().toString().trim() : "";
        String dropoff = dropOffLocationInput.getText() != null ? dropOffLocationInput.getText().toString().trim() : "";
        String date = dateInput.getText() != null ? dateInput.getText().toString().trim() : "";
        String time = timeInput.getText() != null ? timeInput.getText().toString().trim() : "";
        if (pickup.isEmpty() || dropoff.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> request = new HashMap<>();
        request.put("pickupAddress", pickup);
        request.put("dropoffAddress", dropoff);
        request.put("date", date);
        request.put("time", time);
        db.collection("request").add(request)
            .addOnSuccessListener(documentReference -> {
                showRequestPostedDialog();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to post ride request", Toast.LENGTH_SHORT).show();
            });
    }

    private void showRequestPostedDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Ride Plan Posted")
            .setMessage("Your ride plan is posted. Kindly wait for any driver to take your task. Once matched, we will inform you.")
            .setPositiveButton("OK", (dialog, which) -> {
                dialog.dismiss();
                Intent intent = new Intent(PostRideRequestActivity.this, HomeActivity.class);
                intent.putExtra("SHOW_MATCH_NOTIFICATION", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            })
            .setCancelable(false)
            .show();
    }
}