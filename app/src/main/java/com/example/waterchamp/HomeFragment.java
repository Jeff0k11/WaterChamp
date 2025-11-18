package com.example.waterchamp;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Stack;

public class HomeFragment extends Fragment {

    private ProgressBar progressBarWater;
    private TextView tvProgress;
    private TextView tvPercentage;
    private Button btnAdd250;
    private Button btnAdd500;
    private Button btnAddCustom;
    private ImageButton btnUndo;

    private Stack<Integer> historyStack = new Stack<>();
    private int currentDefaultCupSize = 250; // Default

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        progressBarWater = view.findViewById(R.id.progressBarWater);
        tvProgress = view.findViewById(R.id.tvProgress);
        tvPercentage = view.findViewById(R.id.tvPercentage);
        btnAdd250 = view.findViewById(R.id.btnAdd250);
        btnAdd500 = view.findViewById(R.id.btnAdd500);
        btnAddCustom = view.findViewById(R.id.btnAddCustom);
        btnUndo = view.findViewById(R.id.btnUndo);

        updateUI();

        btnAdd250.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addWater(currentDefaultCupSize);
            }
        });

        btnAdd500.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addWater(500);
            }
        });

        btnAddCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomAmountDialog();
            }
        });

        btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undoLastAction();
            }
        });

        return view;
    }

    private void addWater(int amount) {
        if (UserDatabase.currentUser != null) {
            int currentWaterIntake = UserDatabase.currentUser.getWaterIntake();
            historyStack.push(amount);
            animateProgress(currentWaterIntake, currentWaterIntake + amount);
            UserDatabase.currentUser.setWaterIntake(currentWaterIntake + amount);
            
            // Record history
            UserDatabase.currentUser.addHistoryRecord(new HistoryRecord(System.currentTimeMillis(), amount, "Adicionado"));
            
            updateUI();
        }
    }

    private void undoLastAction() {
        if (UserDatabase.currentUser != null && !historyStack.isEmpty()) {
            int lastAmount = historyStack.pop();
            int currentWaterIntake = UserDatabase.currentUser.getWaterIntake();
            animateProgress(currentWaterIntake, currentWaterIntake - lastAmount);
            UserDatabase.currentUser.setWaterIntake(currentWaterIntake - lastAmount);
            
            // Record history
            UserDatabase.currentUser.addHistoryRecord(new HistoryRecord(System.currentTimeMillis(), lastAmount, "Removido"));
            
            updateUI();
        } else {
            Toast.makeText(getContext(), "Nada para desfazer!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        if (UserDatabase.currentUser != null) {
            int currentWaterIntake = UserDatabase.currentUser.getWaterIntake();
            int dailyGoal = UserDatabase.currentUser.getDailyGoal();
            currentDefaultCupSize = UserDatabase.currentUser.getDefaultCupSize();
            
            // Update button text
            btnAdd250.setText("+" + currentDefaultCupSize + "ml");

            // Update max in case it changed
            progressBarWater.setMax(dailyGoal);
            
            // ProgressBar update is handled by animation, but we set it here to ensure consistency
            progressBarWater.setProgress(currentWaterIntake);

            tvProgress.setText(currentWaterIntake + "ml / " + dailyGoal + "ml");

            int percentage = (int) (((double) currentWaterIntake / dailyGoal) * 100);
            tvPercentage.setText(percentage + "%");
        }
    }

    private void animateProgress(int from, int to) {
        ObjectAnimator animation = ObjectAnimator.ofInt(progressBarWater, "progress", from, to);
        animation.setDuration(500); // 0.5 second
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }

    private void showCustomAmountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Adicionar Quantidade (ml)");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Adicionar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = input.getText().toString();
                if (!text.isEmpty()) {
                    int amount = Integer.parseInt(text);
                    addWater(amount);
                }
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
        updateUI();
    }
}