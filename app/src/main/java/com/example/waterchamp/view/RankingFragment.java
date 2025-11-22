package com.example.waterchamp.view;

import android.os.Bundle;
import android.util.Log;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.waterchamp.R;
import com.example.waterchamp.controller.RankingController;
import com.example.waterchamp.data.remote.RankingRealtimeService;
import com.example.waterchamp.model.User;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class RankingFragment extends Fragment implements RankingController.RankingView {

    private RecyclerView recyclerViewRanking;
    private RankingAdapter rankingAdapter;
    private RankingController controller;
    private TabLayout tabLayout;
    private TextView tvEmptyRanking;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RankingRealtimeService realtimeService;

    private static final int TAB_GROUP = 0;
    private static final int TAB_GLOBAL = 1;

    private static final long AUTO_REFRESH_INTERVAL = 2000; // 2 segundos
    private Runnable autoRefreshRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking, container, false);

        recyclerViewRanking = view.findViewById(R.id.recyclerViewRanking);
        recyclerViewRanking.setLayoutManager(new LinearLayoutManager(getContext()));

        tabLayout = view.findViewById(R.id.tabLayout);
        tvEmptyRanking = view.findViewById(R.id.tvEmptyRanking);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        controller = new RankingController(this, getContext());
        realtimeService = new RankingRealtimeService();

        // Configurar Pull-to-Refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (tabLayout.getSelectedTabPosition() == TAB_GROUP) {
                controller.updateGroupRanking();
            } else {
                controller.updateGlobalRanking();
            }
        });

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
        Log.d("RankingFragment", "onResume() - Iniciando auto-refresh do ranking");
        // Carregar ranking do grupo por padrão
        controller.updateGroupRanking();

        // Iniciar auto-refresh a cada 2 segundos
        startAutoRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("RankingFragment", "onPause() - Parando auto-refresh do ranking");
        // Parar auto-refresh quando fragment sai de vista
        stopAutoRefresh();
    }

    /**
     * Inicia atualização automática do ranking a cada 2 segundos
     */
    private void startAutoRefresh() {
        if (autoRefreshRunnable == null) {
            autoRefreshRunnable = new Runnable() {
                @Override
                public void run() {
                    if (recyclerViewRanking != null && isResumed()) {
                        Log.d("RankingFragment", "startAutoRefresh() - Atualizando ranking...");
                        if (tabLayout.getSelectedTabPosition() == TAB_GROUP) {
                            controller.updateGroupRanking();
                        } else {
                            controller.updateGlobalRanking();
                        }
                        // Agendar próxima atualização
                        recyclerViewRanking.postDelayed(this, AUTO_REFRESH_INTERVAL);
                    }
                }
            };
        }
        // Iniciar primeira atualização
        recyclerViewRanking.postDelayed(autoRefreshRunnable, AUTO_REFRESH_INTERVAL);
    }

    /**
     * Para atualização automática do ranking
     */
    private void stopAutoRefresh() {
        if (autoRefreshRunnable != null && recyclerViewRanking != null) {
            recyclerViewRanking.removeCallbacks(autoRefreshRunnable);
        }
    }

    @Override
    public void displayRanking(List<User> rankingList) {
        // Parar indicador de carregamento
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

        // Log para verificar valores recebidos do servidor
        if (rankingList != null && !rankingList.isEmpty()) {
            Log.d("RankingFragment", "displayRanking() - Ranking recebido com " + rankingList.size() + " usuários:");
            for (int i = 0; i < Math.min(rankingList.size(), 3); i++) {
                User user = rankingList.get(i);
                Log.d("RankingFragment", "  [" + (i + 1) + "] " + user.getName() + " - " + user.getWaterIntake() + "ml");
            }
        }

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
                rankingAdapter.updateRanking(rankingList);
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
