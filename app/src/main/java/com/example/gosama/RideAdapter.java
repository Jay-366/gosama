package com.example.gosama;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideViewHolder> {

    private List<Ride> rideList;
    private Context context;
    private FirebaseFirestore db;

    public RideAdapter(List<Ride> rideList, Context context) {
        this.rideList = rideList;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ride_item, parent, false);
        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        Ride ride = rideList.get(position);

        // Set basic info
        holder.rideRoute.setText("From " + ride.getPickupAddress() + " to " + ride.getDropoffAddress());
        holder.rideTime.setText(ride.getDepartureTime());
        // The image shows price, but we don't have it. I'll add a placeholder.
        holder.rideDetails.setText(ride.getAvailableSeats() + " seats | RM5"); // Dummy price

        // Fetch driver's name from Users collection
        db.collection("Users").document(ride.getDriverId()).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    holder.driverName.setText(name);
                } else {
                    holder.driverName.setText("Unknown Driver");
                }
            })
            .addOnFailureListener(e -> holder.driverName.setText("Unknown Driver"));
            
        holder.joinRideButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, RideDetailActivity.class);
            intent.putExtra(RideDetailActivity.EXTRA_RIDE, ride);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return rideList.size();
    }

    static class RideViewHolder extends RecyclerView.ViewHolder {
        ImageView profileIcon;
        TextView driverName, rideRoute, rideTime, rideDetails;
        Button joinRideButton;

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            profileIcon = itemView.findViewById(R.id.driverImage); // was profileIcon
            driverName = itemView.findViewById(R.id.driverName);
            rideRoute = itemView.findViewById(R.id.startLocation); // was rideRoute
            rideTime = itemView.findViewById(R.id.dateTime); // was rideTime
            rideDetails = itemView.findViewById(R.id.price); // was rideDetails
            joinRideButton = itemView.findViewById(R.id.statusButton); // was joinRideButton
        }
    }
}
