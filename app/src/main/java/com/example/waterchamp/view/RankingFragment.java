package com.example.waterchamp.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waterchamp.R;
import com.example.waterchamp.controller.RankingController;
import com.example.waterchamp.model.User;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class RankingFragment extends Fragment implements RankingController.RankingView {

    private RecyclerView recyclerViewRanking;
    private RankingAdapter rankingAdapter;
    private RankingController controller;
    private TabLayout tabLayout;
    private TextView tvEmptyRanking;

    private static final int TAB_GROUP = 0;
    private static final int TAB_GLOBAL = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking, container, false);

        recyclerViewRanking = view.findViewById(R.id.recyclerViewRanking);
        recyclerViewRanking.setLayoutManager(new LinearLayoutManager(getContext()));

        tabLayout = view.findViewById(R.id.tabLayout);
        tvEmptyRanking = view.findViewById(R.id.tvEmptyRanking);

        controller = new RankingController(this, getContext());

        // Configurar tabs
        tabLayout.addTab(tabLayout.newTab().setText("Meu Grupo"));
        tabLayout.addTab(tabLayout.newTab().setText("Global"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == TAB_GROUP) {
                    controller.updateGroupRanking();
                } else {
                    controller.updateGlobalRanking();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Carregar ranking do grupo por padrão
        controller.updateGroupRanking();
    }

    @Override
    public void displayRanking(List<User> rankingList) {
        if (rankingList == null || rankingList.isEmpty()) {
            recyclerViewRanking.setVisibility(View.GONE);
            tvEmptyRanking.setVisibility(View.VISIBLE);
            if (tabLayout.getSelectedTabPosition() == TAB_GROUP) {
                tvEmptyRanking.setText("Você ainda não está em nenhum grupo");
            } else {
                tvEmptyRanking.setText("Nenhum usuário encontrado");
            }
        } else {
            recyclerViewRanking.setVisibility(View.VISIBLE);
            tvEmptyRanking.setVisibility(View.GONE);

            if (rankingAdapter == null) {
                rankingAdapter = new RankingAdapter(rankingList);
                recyclerViewRanking.setAdapter(rankingAdapter);
            } else {
                rankingAdapter = new RankingAdapter(rankingList);
                recyclerViewRanking.setAdapter(rankingAdapter);
            }
        }
    }

    @Override
    public void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
