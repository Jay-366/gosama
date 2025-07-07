package com.example.gosama;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gosama.adapter.RideAdapter;
import com.example.gosama.model.Ride;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyScheduleActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RideAdapter rideAdapter;
    private List<Ride> rideList;
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_schedule);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        rideList = new ArrayList<>();
        rideAdapter = new RideAdapter(rideList);
        recyclerView.setAdapter(rideAdapter);

        db = FirebaseFirestore.getInstance();

        fetchScheduledRides();
    }

    private void fetchScheduledRides() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("confirmed_ride")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        rideList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Ride ride = document.toObject(Ride.class);
                            rideList.add(ride);
                        }
                        rideAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MyScheduleActivity.this, "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
