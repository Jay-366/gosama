package com.example.gosama.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gosama.R;
import com.example.gosama.Ride;
import java.util.List;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideViewHolder> {

    private List<Ride> rideList;

    public RideAdapter(List<Ride> rideList) {
        this.rideList = rideList;
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
        holder.dateTime.setText(dateTimeStr);
        // holder.rideRoute.setText("From " + ride.getPickupAddress() + " to " + ride.getDropoffAddress()); // REMOVE THIS LINE
        holder.startLocation.setText(ride.getPickupAddress()); // Pickup address (first dot)
        holder.endLocation.setText(ride.getDropoffAddress()); // Dropoff address (third dot)
        // holder.price.setText(...); // Set if available
        // holder.statusButton.setText(...); // Set if available

        // Set driver image based on driverId or another field if available
        // Example using driverId (if you want to use driverName, adjust accordingly)
        String driverId = ride.getDriverId() != null ? ride.getDriverId().toLowerCase() : "";
        if (driverId.contains("kangyan")) {
            holder.driverImage.setImageResource(R.drawable.kypic);
        } else if (driverId.contains("chehui")) {
            holder.driverImage.setImageResource(R.drawable.zhpic);
        } else if (driverId.contains("cedric")) {
            holder.driverImage.setImageResource(R.drawable.cedpic);
        } else {
            holder.driverImage.setImageResource(R.drawable.qjpic); // default
        }
    }

    @Override
    public int getItemCount() {
        return rideList.size();
    }

    static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView dateTime, driverName, driverCompany, driverRides, startLocation, endLocation, price;
        Button statusButton;
        ImageView driverImage, optionsMenu;

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTime = itemView.findViewById(R.id.dateTime);
            driverName = itemView.findViewById(R.id.driverName);
            driverCompany = itemView.findViewById(R.id.driverCompany);
            driverRides = itemView.findViewById(R.id.driverRides);
            startLocation = itemView.findViewById(R.id.startLocation);
            endLocation = itemView.findViewById(R.id.endLocation);
            statusButton = itemView.findViewById(R.id.statusButton);
            driverImage = itemView.findViewById(R.id.driverImage);
            optionsMenu = itemView.findViewById(R.id.optionsMenu);
        }
    }
}
