package com.example.gosama.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gosama.R;
import com.example.gosama.model.Reward;
import java.util.List;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.RewardViewHolder> {
    private List<Reward> rewardList;
    private OnRewardClickListener listener;

    public interface OnRewardClickListener {
        void onRewardClick(Reward reward);
    }

    public RewardAdapter(List<Reward> rewardList, OnRewardClickListener listener) {
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
        holder.tvRewardName.setText(reward.getRewardName());
        holder.tvPointsNeeded.setText(reward.getPointsNeed() + " Points");

        // Use local drawable for Starbucks and ZUS Coffee
        String name = reward.getRewardName() != null ? reward.getRewardName().toLowerCase() : "";
        if (name.contains("starbucks")) {
            holder.imageViewRewardPic.setImageResource(R.drawable.starbucksicon);
        } else if (name.contains("zus")) {
            holder.imageViewRewardPic.setImageResource(R.drawable.zusicon);
        } else if (reward.getPic() != null && !reward.getPic().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                 .load(reward.getPic())
                 .placeholder(R.drawable.bg_challenge_1)
                 .error(R.drawable.bg_challenge_1)
                 .centerCrop()
                 .timeout(10000)
                 .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                     @Override
                     public boolean onLoadFailed(GlideException e, Object model, Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                         Log.e("RewardAdapter", "Failed to load image for reward: " + reward.getRewardName() + " Error: " + e.getMessage());
                         return false;
                     }

                     @Override
                     public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, Target<android.graphics.drawable.Drawable> target, DataSource dataSource, boolean isFirstResource) {
                         Log.d("RewardAdapter", "Successfully loaded image for reward: " + reward.getRewardName());
                         return false;
                     }
                 })
                 .into(holder.imageViewRewardPic);
        } else {
            holder.imageViewRewardPic.setImageResource(R.drawable.bg_challenge_1);
        }

        // Make entire card clickable
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRewardClick(reward);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rewardList.size();
    }

    static class RewardViewHolder extends RecyclerView.ViewHolder {
        TextView tvRewardName, tvPointsNeeded;
        android.widget.ImageView imageViewRewardPic;

        RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRewardName = itemView.findViewById(R.id.tvRewardName);
            tvPointsNeeded = itemView.findViewById(R.id.tvPointsNeeded);
            imageViewRewardPic = itemView.findViewById(R.id.imageViewRewardPic);
        }
    }
} 