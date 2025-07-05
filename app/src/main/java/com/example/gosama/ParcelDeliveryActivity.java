package com.example.gosama;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParcelDeliveryActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "ParcelDeliveryActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private ActivityResultLauncher<Intent> pickupAutocompleteLauncher;
    private ActivityResultLauncher<Intent> dropoffAutocompleteLauncher;

    private EditText pickupLocationInput, dropoffLocationInput, scheduleInput, weightInput, instructionsInput;
    private ChipGroup parcelSizeGroup;
    private Button findDriverButton;

    private LatLng pickupLatLng, dropoffLatLng;
    private Polyline currentRoutePolyline;
    private GeoApiContext geoApiContext = null;
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parcel_delivery);

        initViews();
        initMapAndServices();
        setupListeners();
    }

    private void initViews() {
        pickupLocationInput = findViewById(R.id.pickupLocation);
        dropoffLocationInput = findViewById(R.id.dropoffLocation);
        scheduleInput = findViewById(R.id.schedule);
        weightInput = findViewById(R.id.weight);
        instructionsInput = findViewById(R.id.instructions);
        parcelSizeGroup = findViewById(R.id.parcel_size_group);
        findDriverButton = findViewById(R.id.find_driver_button);
    }

    private void initMapAndServices() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        if (geoApiContext == null) {
            geoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.maps_api_key))
                    .build();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupListeners() {
        setupAutocompleteFields();
        setupDateTimePicker();
        findDriverButton.setOnClickListener(v -> saveParcelToFirestore());
    }

    private void setupAutocompleteFields() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);

        pickupAutocompleteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());
                        pickupLocationInput.setText(place.getAddress());
                        pickupLatLng = place.getLatLng();
                        addMarkerAndDrawRoute(pickupLatLng, "Pickup Location");
                    }
                });

        dropoffAutocompleteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());
                        dropoffLocationInput.setText(place.getAddress());
                        dropoffLatLng = place.getLatLng();
                        addMarkerAndDrawRoute(dropoffLatLng, "Drop-off Location");
                    }
                });

        pickupLocationInput.setOnClickListener(v -> {
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this);
            pickupAutocompleteLauncher.launch(intent);
        });

        dropoffLocationInput.setOnClickListener(v -> {
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this);
            dropoffAutocompleteLauncher.launch(intent);
        });
    }

    private void setupDateTimePicker() {
        scheduleInput.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Date")
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(selection);
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
                String date = dateFormat.format(calendar.getTime());

                MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                        .setTitleText("Select Time")
                        .setTimeFormat(TimeFormat.CLOCK_12H)
                        .build();

                timePicker.addOnPositiveButtonClickListener(dialog -> {
                    calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                    calendar.set(Calendar.MINUTE, timePicker.getMinute());
                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    String time = timeFormat.format(calendar.getTime());
                    scheduleInput.setText(String.format("%s, %s", date, time));
                });
                timePicker.show(getSupportFragmentManager(), "TIME_PICKER");
            });
            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });
    }

    private void addMarkerAndDrawRoute(LatLng latLng, String title) {
        if (googleMap != null && latLng != null) {
            googleMap.addMarker(new MarkerOptions().position(latLng).title(title));
            if (pickupLatLng != null && dropoffLatLng != null) {
                drawRoute();
            }
        }
    }

    private void drawRoute() {
        if (currentRoutePolyline != null) {
            currentRoutePolyline.remove();
        }

        executorService.execute(() -> {
            try {
                DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                        .origin(new com.google.maps.model.LatLng(pickupLatLng.latitude, pickupLatLng.longitude))
                        .destination(new com.google.maps.model.LatLng(dropoffLatLng.latitude, dropoffLatLng.longitude))
                        .await();

                if (result.routes != null && result.routes.length > 0) {
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(result.routes[0].overviewPolyline.getEncodedPath());
                    mainHandler.post(() -> {
                        PolylineOptions polylineOptions = new PolylineOptions().width(10).color(Color.BLUE);
                        for (com.google.maps.model.LatLng point : decodedPath) {
                            polylineOptions.add(new LatLng(point.lat, point.lng));
                        }
                        currentRoutePolyline = googleMap.addPolyline(polylineOptions);
                        zoomToRoute();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error drawing route", e);
            }
        });
    }

    private void zoomToRoute() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(pickupLatLng);
        builder.include(dropoffLatLng);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
    }

    private void saveParcelToFirestore() {
        if (!validateInputs()) return;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedChipText = "";
        int checkedChipId = parcelSizeGroup.getCheckedChipId();
        if (checkedChipId != -1) {
            Chip selectedChip = findViewById(checkedChipId);
            selectedChipText = selectedChip.getText().toString();
        }

        Map<String, Object> parcel = new HashMap<>();
        parcel.put("userId", currentUser.getUid());
        parcel.put("pickupAddress", pickupLocationInput.getText().toString());
        parcel.put("dropoffAddress", dropoffLocationInput.getText().toString());
        parcel.put("pickupLocation", new GeoPoint(pickupLatLng.latitude, pickupLatLng.longitude));
        parcel.put("dropoffLocation", new GeoPoint(dropoffLatLng.latitude, dropoffLatLng.longitude));
        parcel.put("schedule", scheduleInput.getText().toString());
        parcel.put("parcelSize", selectedChipText);
        parcel.put("weightKg", Double.parseDouble(weightInput.getText().toString()));
        parcel.put("instructions", instructionsInput.getText().toString());
        parcel.put("timestamp", new Date());

        FirebaseFirestore.getInstance().collection("parcels")
                .add(parcel)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Parcel details submitted!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error adding parcel", e);
                });
    }

    private boolean validateInputs() {
        if (pickupLatLng == null || TextUtils.isEmpty(pickupLocationInput.getText())) {
            Toast.makeText(this, "Please select a pickup location", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (dropoffLatLng == null || TextUtils.isEmpty(dropoffLocationInput.getText())) {
            Toast.makeText(this, "Please select a drop-off location", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(scheduleInput.getText())) {
            Toast.makeText(this, "Please select a schedule", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (parcelSizeGroup.getCheckedChipId() == -1) {
            Toast.makeText(this, "Please select a parcel size", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(weightInput.getText())) {
            Toast.makeText(this, "Please enter the parcel weight", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
