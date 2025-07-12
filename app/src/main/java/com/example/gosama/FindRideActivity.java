package com.example.gosama;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;

public class FindRideActivity extends AppCompatActivity implements ParcelAdapter.OnParcelListener {

    private static final String TAG = "FindRideActivity";
    private RecyclerView ridesRecyclerView;
    private RideAdapter rideAdapter;
    private List<Ride> rideList;

    private RecyclerView parcelsRecyclerView;
    private ParcelAdapter parcelAdapter;
    private List<Parcel> parcelList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_ride);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Find a Ride");
        }

        ImageButton btnAddRide = findViewById(R.id.btnAddRide);
        btnAddRide.setOnClickListener(v -> {
            Intent intent = new Intent(this, PostRideRequestActivity.class);
            startActivity(intent);
        });

        db = FirebaseFirestore.getInstance();
        ridesRecyclerView = findViewById(R.id.ridesRecyclerView);
        ridesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        rideList = new ArrayList<>();
        rideAdapter = new RideAdapter(rideList, this);
        ridesRecyclerView.setAdapter(rideAdapter);

        parcelsRecyclerView = findViewById(R.id.parcelsRecyclerView);
        parcelsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        parcelList = new ArrayList<>();
        parcelAdapter = new ParcelAdapter(parcelList, this);
        parcelsRecyclerView.setAdapter(parcelAdapter);

        fetchRides();
        fetchParcels();
    }

    private void fetchRides() {
        db.collection("Rides")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    rideList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Ride ride = document.toObject(Ride.class);
                        ride.setDocumentId(document.getId());
                        rideList.add(ride);
                    }
                    rideAdapter.notifyDataSetChanged();
                } else {
                    android.util.Log.d(TAG, "Error getting documents: ", task.getException());
                }
            });
    }

    private void fetchParcels() {
        db.collection("parcels")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        parcelList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Parcel parcel = document.toObject(Parcel.class);
                            parcel.setDocumentId(document.getId());
                            parcelList.add(parcel);
                        }
                        parcelAdapter.notifyDataSetChanged();
                    } else {
                        android.util.Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }



    @Override
    public void onTakeTaskClick(int position) {
        Parcel parcel = parcelList.get(position);
        Toast.makeText(this, "Task taken for parcel: " + parcel.getPickupAddress(), Toast.LENGTH_SHORT).show();
        // TODO: Implement ParcelDetailActivity and navigate to it
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
