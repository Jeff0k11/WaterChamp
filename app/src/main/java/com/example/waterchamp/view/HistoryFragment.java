package com.example.waterchamp.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerViewHistory = view.findViewById(R.id.recyclerViewHistory);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        controller = new HistoryController(this, getContext());

        return view;
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
