package com.example.waterchamp.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waterchamp.R;
import com.example.waterchamp.controller.HistoryController;
import com.example.waterchamp.event.HistoryUpdateEvent;
import com.example.waterchamp.model.HistoryRecord;

import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class HistoryFragment extends Fragment implements HistoryController.HistoryView {

    private RecyclerView recyclerViewHistory;
    private HistoryAdapter historyAdapter;
    private HistoryController controller;
    private Button btnClearHistory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerViewHistory = view.findViewById(R.id.recyclerViewHistory);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        btnClearHistory = view.findViewById(R.id.btnClearHistory);
        btnClearHistory.setOnClickListener(v -> showClearConfirmationDialog());

        controller = new HistoryController(this, getContext());

        return view;
    }

    /**
     * Mostrar diálogo de confirmação antes de limpar histórico
     */
    private void showClearConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Limpar Histórico");
        builder.setMessage("Tem certeza que deseja limpar todo o histórico de consumo de hoje? Esta ação não pode ser desfeita.");

        builder.setPositiveButton("Limpar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                controller.clearHistory();
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        controller.updateHistory();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    /**
     * Escuta eventos de atualização do histórico
     * Disposto quando addWater() ou undoLastAction() são chamados
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHistoryUpdate(HistoryUpdateEvent event) {
        // Recarregar histórico quando há mudanças
        controller.updateHistory();
    }

    @Override
    public void displayHistory(List<HistoryRecord> historyList) {
        if (historyAdapter == null) {
            historyAdapter = new HistoryAdapter(historyList);
            recyclerViewHistory.setAdapter(historyAdapter);
        } else {
            // Atualizar adapter em vez de recriar
            historyAdapter.updateHistory(historyList);
        }
    }
}
