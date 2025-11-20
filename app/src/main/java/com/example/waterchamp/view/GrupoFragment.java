package com.example.waterchamp.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waterchamp.R;
import com.example.waterchamp.controller.GrupoController;
import com.example.waterchamp.model.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment para exibir e gerenciar grupos de usuários
 */
public class GrupoFragment extends Fragment implements GrupoController.GrupoView {

    private RecyclerView recyclerViewGroups;
    private Button btnCreateGroup;
    private Button btnJoinGroup;
    private TextView tvEmptyMessage;
    private GrupoController grupoController;
    private GrupoAdapter grupoAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grupo, container, false);

        // Inicializar componentes
        recyclerViewGroups = view.findViewById(R.id.recyclerViewGroups);
        btnCreateGroup = view.findViewById(R.id.btnCreateGroup);
        btnJoinGroup = view.findViewById(R.id.btnJoinGroup);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);

        // Configurar RecyclerView
        grupoAdapter = new GrupoAdapter(new ArrayList<>());
        recyclerViewGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewGroups.setAdapter(grupoAdapter);

        // Inicializar controller
        grupoController = new GrupoController(this, requireContext());

        // Listeners
        btnCreateGroup.setOnClickListener(v -> showCreateGroupDialog());
        btnJoinGroup.setOnClickListener(v -> showJoinGroupDialog());

        // Carregar grupos
        loadGroups();

        return view;
    }

    private void loadGroups() {
        grupoController.loadUserGroups();
    }

    private void showCreateGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_group, null);
        EditText etGroupName = dialogView.findViewById(R.id.etGroupName);
        EditText etGroupDescription = dialogView.findViewById(R.id.etGroupDescription);
        Button btnCancelCreate = dialogView.findViewById(R.id.btnCancelCreate);
        Button btnConfirmCreate = dialogView.findViewById(R.id.btnConfirmCreate);

        AlertDialog dialog = builder.setView(dialogView).create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnCancelCreate.setOnClickListener(v -> dialog.dismiss());

        btnConfirmCreate.setOnClickListener(v -> {
            String groupName = etGroupName.getText().toString().trim();
            String groupDescription = etGroupDescription.getText().toString().trim();

            // Validar nome
            if (TextUtils.isEmpty(groupName)) {
                Toast.makeText(getContext(), "Digite o nome do grupo", Toast.LENGTH_SHORT).show();
                return;
            }

            if (groupName.length() < 3) {
                Toast.makeText(getContext(), "Nome do grupo deve ter pelo menos 3 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            if (groupName.length() > 50) {
                Toast.makeText(getContext(), "Nome do grupo não pode exceder 50 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            // Se descrição estiver vazia, usar uma padrão
            if (TextUtils.isEmpty(groupDescription)) {
                groupDescription = "Grupo de amigos WaterChamp";
            }

            // Criar grupo
            grupoController.createGroup(groupName, groupDescription);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showJoinGroupDialog() {
        // Criar AlertDialog para entrar em grupo
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_join_group, null);
        EditText etGroupId = dialogView.findViewById(R.id.etGroupId);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnConfirmJoin = dialogView.findViewById(R.id.btnConfirmJoin);

        AlertDialog dialog = builder.setView(dialogView).create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirmJoin.setOnClickListener(v -> {
            String groupIdStr = etGroupId.getText().toString().trim();

            if (groupIdStr.isEmpty()) {
                Toast.makeText(getContext(), "Digite o ID do grupo", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int groupId = Integer.parseInt(groupIdStr);
                grupoController.joinGroup(groupId);
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "ID do grupo inválido", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    @Override
    public void showGroups(List<Group> groups) {
        if (groups == null || groups.isEmpty()) {
            recyclerViewGroups.setVisibility(View.GONE);
            tvEmptyMessage.setVisibility(View.VISIBLE);
        } else {
            recyclerViewGroups.setVisibility(View.VISIBLE);
            tvEmptyMessage.setVisibility(View.GONE);
            grupoAdapter.updateGroups(groups);
        }

        // Atualizar estado do botão de entrar em grupo
        updateJoinButtonState(groups);
    }

    private void updateJoinButtonState(List<Group> groups) {
        // Se o usuário já está em um grupo, desabilitar o botão
        if (groups != null && !groups.isEmpty()) {
            btnJoinGroup.setEnabled(false);
            btnJoinGroup.setAlpha(0.5f);
            btnJoinGroup.setText("Já está em grupo");
        } else {
            btnJoinGroup.setEnabled(true);
            btnJoinGroup.setAlpha(1.0f);
            btnJoinGroup.setText("Entrar em Grupo");
        }
    }

    @Override
    public void showError(String message) {
        Toast.makeText(getContext(), "Erro: " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSuccess(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoading() {
        // TODO: Mostrar indicador de carregamento
    }

    @Override
    public void hideLoading() {
        // TODO: Esconder indicador de carregamento
    }
}
