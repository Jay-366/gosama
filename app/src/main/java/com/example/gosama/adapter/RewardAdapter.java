package com.example.gosama.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gosama.R;
import com.example.gosama.model.Reward;
import java.util.List;

public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.RewardViewHolder> {
    private List<Reward> rewardList;
    private OnRedeemClickListener listener;

    public interface OnRedeemClickListener {
        void onRedeemClick(Reward reward);
    }

    public RewardAdapter(List<Reward> rewardList, OnRedeemClickListener listener) {
        this.rewardList = rewardList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reward, parent, false);
        return new RewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
        Reward reward = rewardList.get(position);
        holder.tvRewardName.setText(reward.getDisplayRewardName());
        holder.tvPointsNeeded.setText(reward.getPointsNeed() + " Points");
        holder.tvStock.setText("Stock: " + reward.getStock());
        holder.btnRewardRedeem.setOnClickListener(v -> listener.onRedeemClick(reward));
    }

    @Override
    public int getItemCount() {
        return rewardList.size();
    }

    static class RewardViewHolder extends RecyclerView.ViewHolder {
        TextView tvRewardName, tvPointsNeeded, tvStock;
        Button btnRewardRedeem;

        RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRewardName = itemView.findViewById(R.id.tvRewardName);
            tvPointsNeeded = itemView.findViewById(R.id.tvPointsNeeded);
            tvStock = itemView.findViewById(R.id.tvStock);
            btnRewardRedeem = itemView.findViewById(R.id.btnRewardRedeem);
        }
    }
} 