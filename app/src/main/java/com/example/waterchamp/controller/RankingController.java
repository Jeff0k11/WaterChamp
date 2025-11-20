package com.example.waterchamp.controller;

import com.example.waterchamp.model.User;
import com.example.waterchamp.model.UserDatabase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RankingController {
    private RankingView view;

    public RankingController(RankingView view) {
        this.view = view;
    }

    public void updateRanking() {
        List<User> rankingList = new ArrayList<>(UserDatabase.usersList);

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

        view.displayRanking(rankingList);
    }

    public interface RankingView {
        void displayRanking(List<User> rankingList);
    }
}
