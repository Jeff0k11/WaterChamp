package com.example.waterchamp.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);

        // Configurar RecyclerView
        grupoAdapter = new GrupoAdapter(new ArrayList<>());
        recyclerViewGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewGroups.setAdapter(grupoAdapter);

        // Inicializar controller
        grupoController = new GrupoController(this, requireContext());

        // Listeners
        btnCreateGroup.setOnClickListener(v -> showCreateGroupDialog());

        // Carregar grupos
        loadGroups();

        return view;
    }

    private void loadGroups() {
        grupoController.loadUserGroups();
    }

    private void showCreateGroupDialog() {
        // TODO: Implementar dialog para criar grupo
        Toast.makeText(getContext(), "Criar novo grupo - em desenvolvimento", Toast.LENGTH_SHORT).show();
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
    }

    @Override
    public void showError(String message) {
        Toast.makeText(getContext(), "Erro: " + message, Toast.LENGTH_SHORT).show();
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
