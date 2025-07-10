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
import android.util.Log;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideViewHolder> {

    private static final String TAG = "RideAdapter";

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
        String dateTimeStr = ride.getDepartureDate() + ", " + ride.getDepartureTime();
        holder.rideTime.setText(dateTimeStr);
        holder.startLocation.setText(ride.getPickupAddress());
        holder.endLocation.setText(ride.getDropoffAddress());
        // Optionally, show available seats somewhere else if needed
        // Remove any price/rideDetails usage

        // Fetch driver's data from Users collection
        db.collection("users").document(ride.getDriverId()).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("username");
                    String joinedDate = documentSnapshot.getString("joinedDate");
                    Long ridesCount = documentSnapshot.getLong("ridesCount");

                    holder.driverName.setText(name != null ? name : "Unknown Driver");
                    holder.driverJoined.setText(joinedDate != null ? "Joined: " + joinedDate : "");
                    holder.driverRides.setText(ridesCount != null ? "Rides: " + ridesCount : "");

                    // Set profile picture based on user name
                    int profilePicResId = R.drawable.circular_background; // Default image
                    if (name != null) {
                        switch (name) {
                            case "Kangyan Ong":
                                profilePicResId = R.drawable.kypic;
                                break;
                            case "Chehui Tan":
                                profilePicResId = R.drawable.zhpic;
                                break;
                            case "Cedric":
                                profilePicResId = R.drawable.cedpic;
                                break;
                            default:
                                profilePicResId = R.drawable.qjpic;
                                break;
                        }
                    }
                    Glide.with(context)
                        .load(profilePicResId)
                        .circleCrop()
                        .into(holder.driverImage);

                } else {
                    holder.driverName.setText("Unknown Driver");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to fetch user data for driverId: " + ride.getDriverId(), e);
                holder.driverName.setText("Unknown Driver");
                holder.driverImage.setImageResource(R.drawable.circular_background);
            });
            
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
        ImageView driverImage;
        TextView driverName, driverJoined, driverRides, startLocation, endLocation, rideTime;
        Button joinRideButton;

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            driverImage = itemView.findViewById(R.id.driverImage); 
            driverName = itemView.findViewById(R.id.driverName);
            driverJoined = itemView.findViewById(R.id.driverCompany); // Corresponds to 'Joined: 1 year'
            driverRides = itemView.findViewById(R.id.driverRides); // Corresponds to 'Rride: 1023'
            startLocation = itemView.findViewById(R.id.startLocation); // renamed from rideRoute
            endLocation = itemView.findViewById(R.id.endLocation);
            rideTime = itemView.findViewById(R.id.dateTime); // was rideTime
            joinRideButton = itemView.findViewById(R.id.statusButton); // was joinRideButton
        }
    }
}
