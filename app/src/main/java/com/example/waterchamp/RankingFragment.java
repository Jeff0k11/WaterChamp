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

public class RankingFragment extends Fragment {

    private RecyclerView recyclerViewRanking;
    private RankingAdapter rankingAdapter;
    private List<User> rankingList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking, container, false);

        recyclerViewRanking = view.findViewById(R.id.recyclerViewRanking);
        recyclerViewRanking.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateRanking();
    }

    private void updateRanking() {
        rankingList = new ArrayList<>(UserDatabase.usersList);
        
        // Ensure current user is in the list (or updated)
        if (UserDatabase.currentUser != null) {
            boolean found = false;
            for (int i = 0; i < rankingList.size(); i++) {
                if (rankingList.get(i).getEmail().equals(UserDatabase.currentUser.getEmail())) {
                    rankingList.set(i, UserDatabase.currentUser); // Update current user data
                    found = true;
                    break;
                }
            }
            if (!found) {
                rankingList.add(UserDatabase.currentUser);
            }
        }

        // Sort by water intake (descending)
        Collections.sort(rankingList);

        if (rankingAdapter == null) {
            rankingAdapter = new RankingAdapter(rankingList);
            recyclerViewRanking.setAdapter(rankingAdapter);
        } else {
            // In a real app, we might want to update the data in the adapter instead of creating a new one
            // But for this simple case, re-setting or notifying data change is fine.
            // Since we created a new list, let's just re-create adapter to be safe/simple
            rankingAdapter = new RankingAdapter(rankingList);
            recyclerViewRanking.setAdapter(rankingAdapter);
        }
    }
}