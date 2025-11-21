package com.example.waterchamp.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waterchamp.R;
import com.example.waterchamp.data.remote.GrupoService;
import com.example.waterchamp.data.remote.UserService;
import com.example.waterchamp.model.Group;
import com.example.waterchamp.utils.CoroutineHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter para exibir lista de grupos em RecyclerView
 */
public class GrupoAdapter extends RecyclerView.Adapter<GrupoAdapter.GrupoViewHolder> {

    private List<Group> grupos;
    private OnGroupClickListener listener;
    private OnLeaveGroupListener leaveListener;

    public interface OnGroupClickListener {
        void onGroupClick(Group group);
    }

    public interface OnLeaveGroupListener {
        void onLeaveGroup(Group group);
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

        holder.btnLeaveGroup.setOnClickListener(v -> {
            if (leaveListener != null) {
                leaveListener.onLeaveGroup(group);
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

    public void setOnLeaveGroupListener(OnLeaveGroupListener listener) {
        this.leaveListener = listener;
    }

    /**
     * ViewHolder para cada item de grupo
     */
    public static class GrupoViewHolder extends RecyclerView.ViewHolder {

        private TextView tvGroupName;
        private TextView tvGroupDescription;
        private TextView tvGroupCode;
        private TextView tvMemberCount;
        private TextView tvCreatedDate;
        private LinearLayout llMembersList;
        public Button btnLeaveGroup;
        private GrupoService grupoService;
        private UserService userService;

        public GrupoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvGroupDescription = itemView.findViewById(R.id.tvGroupDescription);
            tvGroupCode = itemView.findViewById(R.id.tvGroupCode);
            tvMemberCount = itemView.findViewById(R.id.tvMemberCount);
            tvCreatedDate = itemView.findViewById(R.id.tvCreatedDate);
            llMembersList = itemView.findViewById(R.id.llMembersList);
            btnLeaveGroup = itemView.findViewById(R.id.btnLeaveGroup);
            grupoService = new GrupoService();
            userService = new UserService();
        }

        public void bind(Group group) {
            tvGroupName.setText(group.getNome());
            tvGroupDescription.setText(group.getDescricao());
            tvGroupCode.setText(String.valueOf(group.getId())); // Usar ID como código
            tvMemberCount.setText(group.getTotalMembros() + " membro" + (group.getTotalMembros() != 1 ? "s" : ""));
            tvCreatedDate.setText(formatDate(group.getDataCriacao()));

            // Carregar membros do grupo
            loadAndDisplayMembers(group.getId());
        }

        private String formatDate(String dateString) {
            if (dateString == null || dateString.isEmpty()) {
                return "";
            }
            try {
                // Esperado formato ISO: 2024-01-15T10:30:00
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = isoFormat.parse(dateString);

                // Formatar para: 15/01/2024
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return outputFormat.format(date);
            } catch (Exception e) {
                // Se falhar, tentar formato simples
                return dateString;
            }
        }

        private void loadAndDisplayMembers(int grupoId) {
            // Limpar lista anterior
            llMembersList.removeAllViews();

            // Carregar membros em background
            CoroutineHelper.runAsync(
                () -> grupoService.getGroupMembersBlocking(grupoId),
                new CoroutineHelper.CoroutineCallback<List<Integer>>() {
                    @Override
                    public void onComplete(List<Integer> memberIds, String error) {
                        if (error == null && memberIds != null && !memberIds.isEmpty()) {
                            // Para cada membro, buscar o nome
                            for (Integer memberId : memberIds) {
                                CoroutineHelper.runAsync(
                                    () -> userService.getUserByIdBlocking(memberId),
                                    new CoroutineHelper.CoroutineCallback<UserService.Usuario>() {
                                        @Override
                                        public void onComplete(UserService.Usuario usuario, String userError) {
                                            if (userError == null && usuario != null) {
                                                TextView tvMember = new TextView(itemView.getContext());
                                                tvMember.setText("• " + usuario.getNome());
                                                tvMember.setTextColor(itemView.getContext().getResources().getColor(R.color.text_secondary));
                                                tvMember.setTextSize(12);
                                                tvMember.setPadding(0, 4, 0, 4);
                                                llMembersList.addView(tvMember);
                                            }
                                        }
                                    }
                                );
                            }
                        }
                    }
                }
            );
        }
    }
}
