package com.example.waterchamp;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.RankingViewHolder> {

    private List<User> userList;

    public RankingAdapter(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public RankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ranking, parent, false);
        return new RankingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankingViewHolder holder, int position) {
        User user = userList.get(position);
        int rank = position + 1;

        String rankText;
        if (rank == 1) {
            rankText = "🥇";
        } else if (rank == 2) {
            rankText = "🥈";
        } else if (rank == 3) {
            rankText = "🥉";
        } else {
            rankText = rank + "º";
        }

        holder.tvRank.setText(rankText);
        holder.tvName.setText(user.getName());
        holder.tvIntake.setText(user.getWaterIntake() + "ml");
        
        if (user.getProfilePictureUri() != null) {
            try {
                holder.imgProfile.setImageURI(Uri.parse(user.getProfilePictureUri()));
                holder.imgProfile.setPadding(0, 0, 0, 0); // Remove padding if image is set
            } catch (Exception e) {
                holder.imgProfile.setImageResource(R.drawable.ic_profile);
                holder.imgProfile.setPadding(8, 8, 8, 8);
            }
        } else {
            holder.imgProfile.setImageResource(R.drawable.ic_profile);
            holder.imgProfile.setPadding(8, 8, 8, 8);
        }
        
        // Highlight current user
        if (UserDatabase.currentUser != null && user.getEmail().equals(UserDatabase.currentUser.getEmail())) {
             holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.button_dark_teal));
        } else {
             holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.background_main));
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class RankingViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvIntake;
        ImageView imgProfile;

        public RankingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvName = itemView.findViewById(R.id.tvName);
            tvIntake = itemView.findViewById(R.id.tvIntake);
            imgProfile = itemView.findViewById(R.id.imgProfile);
        }
    }
}