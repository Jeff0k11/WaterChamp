package com.example.waterchamp.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waterchamp.R;
import com.example.waterchamp.controller.RankingController;
import com.example.waterchamp.model.User;

import java.util.List;

public class RankingFragment extends Fragment implements RankingController.RankingView {

    private RecyclerView recyclerViewRanking;
    private RankingAdapter rankingAdapter;
    private RankingController controller;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking, container, false);

        recyclerViewRanking = view.findViewById(R.id.recyclerViewRanking);
        recyclerViewRanking.setLayoutManager(new LinearLayoutManager(getContext()));

        controller = new RankingController(this, getContext());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        controller.updateRanking();
    }

    @Override
    public void displayRanking(List<User> rankingList) {
        if (rankingAdapter == null) {
            rankingAdapter = new RankingAdapter(rankingList);
            recyclerViewRanking.setAdapter(rankingAdapter);
        } else {
            rankingAdapter = new RankingAdapter(rankingList);
            recyclerViewRanking.setAdapter(rankingAdapter);
        }
    }

    @Override
    public void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
