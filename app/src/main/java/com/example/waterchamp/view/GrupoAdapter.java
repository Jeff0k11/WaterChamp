package com.example.waterchamp.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waterchamp.R;
import com.example.waterchamp.model.Group;

import java.util.List;

/**
 * Adapter para exibir lista de grupos em RecyclerView
 */
public class GrupoAdapter extends RecyclerView.Adapter<GrupoAdapter.GrupoViewHolder> {

    private List<Group> grupos;
    private OnGroupClickListener listener;

    public interface OnGroupClickListener {
        void onGroupClick(Group group);
    }

    public GrupoAdapter(List<Group> grupos) {
        this.grupos = grupos;
    }

    @NonNull
    @Override
    public GrupoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grupo, parent, false);
        return new GrupoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GrupoViewHolder holder, int position) {
        Group group = grupos.get(position);
        holder.bind(group);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGroupClick(group);
            }
        });
    }

    @Override
    public int getItemCount() {
        return grupos.size();
    }

    public void updateGroups(List<Group> newGroups) {
        this.grupos = newGroups;
        notifyDataSetChanged();
    }

    public void setOnGroupClickListener(OnGroupClickListener listener) {
        this.listener = listener;
    }

    /**
     * ViewHolder para cada item de grupo
     */
    public static class GrupoViewHolder extends RecyclerView.ViewHolder {

        private TextView tvGroupName;
        private TextView tvGroupDescription;
        private TextView tvMemberCount;
        private TextView tvCreatedDate;

        public GrupoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvGroupDescription = itemView.findViewById(R.id.tvGroupDescription);
            tvMemberCount = itemView.findViewById(R.id.tvMemberCount);
            tvCreatedDate = itemView.findViewById(R.id.tvCreatedDate);
        }

        public void bind(Group group) {
            tvGroupName.setText(group.getNome());
            tvGroupDescription.setText(group.getDescricao());
            tvMemberCount.setText(group.getTotalMembros() + " membros");
            tvCreatedDate.setText("Criado em: " + group.getDataCriacao());
        }
    }
}
