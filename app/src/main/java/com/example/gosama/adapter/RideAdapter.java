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
import com.example.gosama.model.Ride;
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
        String dateTimeStr = ride.getDate() + ", " + ride.getTime();
        holder.dateTime.setText(dateTimeStr);
        holder.driverName.setText(ride.getDriverName());
        holder.driverCompany.setText("At company: " + ride.getDriverCompany());
        holder.driverRides.setText("Rides: " + ride.getDriverRides());
        holder.startLocation.setText(ride.getDeparture());
        holder.endLocation.setText(ride.getDestination());
        holder.price.setText(String.format("$%.2f", ride.getPrice()));
        holder.statusButton.setText(ride.getStatus());
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
            price = itemView.findViewById(R.id.price);
            statusButton = itemView.findViewById(R.id.statusButton);
            driverImage = itemView.findViewById(R.id.driverImage);
            optionsMenu = itemView.findViewById(R.id.optionsMenu);
        }
    }
}
