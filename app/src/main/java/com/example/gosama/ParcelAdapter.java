package com.example.gosama;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ParcelAdapter extends RecyclerView.Adapter<ParcelAdapter.ParcelViewHolder> {

    private List<Parcel> parcelList;
    private OnParcelListener onParcelListener;

    public ParcelAdapter(List<Parcel> parcelList, OnParcelListener onParcelListener) {
        this.parcelList = parcelList;
        this.onParcelListener = onParcelListener;
    }

    @NonNull
    @Override
    public ParcelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_parcel, parent, false);
        return new ParcelViewHolder(view, onParcelListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ParcelViewHolder holder, int position) {
        Parcel parcel = parcelList.get(position);
        holder.parcelSchedule.setText(parcel.getSchedule());
        holder.parcelRoute.setText(String.format("%s to %s", parcel.getPickupAddress(), parcel.getDropoffAddress()));
        holder.parcelDetails.setText(String.format("Size: %s, Weight: %.2f kg", parcel.getParcelSize(), parcel.getWeightKg()));
    }

    @Override
    public int getItemCount() {
        return parcelList.size();
    }

    static class ParcelViewHolder extends RecyclerView.ViewHolder {
        TextView parcelSchedule, parcelRoute, parcelDetails;
        Button takeTaskButton;

        public ParcelViewHolder(@NonNull View itemView, OnParcelListener onParcelListener) {
            super(itemView);
            parcelSchedule = itemView.findViewById(R.id.parcelSchedule);
            parcelRoute = itemView.findViewById(R.id.parcelRoute);
            parcelDetails = itemView.findViewById(R.id.parcelDetails);
            takeTaskButton = itemView.findViewById(R.id.takeTaskButton);

            takeTaskButton.setOnClickListener(v -> {
                if (onParcelListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onParcelListener.onTakeTaskClick(position);
                    }
                }
            });
        }
    }

    public interface OnParcelListener {
        void onTakeTaskClick(int position);
    }
}
