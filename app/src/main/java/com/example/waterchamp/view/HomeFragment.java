package com.example.waterchamp.view;

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

import com.example.waterchamp.R;
import com.example.waterchamp.controller.HomeController;
import com.example.waterchamp.model.UserDatabase;
import com.google.android.material.snackbar.Snackbar;

public class HomeFragment extends Fragment implements HomeController.HomeView {

    private ProgressBar progressBarWater;
    private TextView tvProgress;
    private TextView tvPercentage;
    private Button btnAdd250;
    private Button btnAdd500;
    private Button btnAddCustom;
    private ImageButton btnUndo;

    private HomeController controller;

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

        controller = new HomeController(this, getContext());

        updateUI();

        btnAdd250.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserDatabase.currentUser != null) {
                    controller.addWater(UserDatabase.currentUser.getDefaultCupSize());
                }
            }
        });

        btnAdd500.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.addWater(500);
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
                controller.undoLastAction();
            }
        });

        return view;
    }

    @Override
    public void updateUI() {
        if (UserDatabase.currentUser != null) {
            int currentWaterIntake = UserDatabase.currentUser.getWaterIntake();
            int dailyGoal = UserDatabase.currentUser.getDailyGoal();
            int defaultCupSize = UserDatabase.currentUser.getDefaultCupSize();

            // Update button text
            btnAdd250.setText("+" + defaultCupSize + "ml");

            // Update max in case it changed
            progressBarWater.setMax(dailyGoal);

            // ProgressBar update is handled by animation, but we set it here to ensure consistency
            progressBarWater.setProgress(currentWaterIntake);

            tvProgress.setText(currentWaterIntake + "ml / " + dailyGoal + "ml");

            int percentage = (int) (((double) currentWaterIntake / dailyGoal) * 100);
            tvPercentage.setText(percentage + "%");
        }
    }

    @Override
    public void animateProgress(int from, int to) {
        ObjectAnimator animation = ObjectAnimator.ofInt(progressBarWater, "progress", from, to);
        animation.setDuration(500); // 0.5 second
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }

    @Override
    public void showCustomAmountDialog() {
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
                    try {
                        int amount = Integer.parseInt(text);
                        controller.addWater(amount);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Quantidade deve ser um número válido", Toast.LENGTH_SHORT).show();
                    }
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
    public void showToast(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, 800).show(); // 800ms - mais rápido
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        controller.updateUI();
    }
}
