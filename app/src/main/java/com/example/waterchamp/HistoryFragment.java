package com.example.waterchamp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerViewHistory;
    private HistoryAdapter historyAdapter;
    private List<HistoryRecord> historyList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerViewHistory = view.findViewById(R.id.recyclerViewHistory);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateHistory();
    }

    private void updateHistory() {
        if (UserDatabase.currentUser != null) {
            historyList = new ArrayList<>(UserDatabase.currentUser.getHistoryList());
            Collections.reverse(historyList); // Show newest first

            if (historyAdapter == null) {
                historyAdapter = new HistoryAdapter(historyList);
                recyclerViewHistory.setAdapter(historyAdapter);
            } else {
                historyAdapter = new HistoryAdapter(historyList);
                recyclerViewHistory.setAdapter(historyAdapter);
            }
        }
    }
}