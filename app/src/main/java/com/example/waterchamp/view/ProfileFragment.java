package com.example.waterchamp.view;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.waterchamp.R;
import com.example.waterchamp.controller.ProfileController;

public class ProfileFragment extends Fragment implements ProfileController.ProfileView {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etName, etPassword, etDailyGoal, etCupSize;
    private TextView tvEmail, tvTotalIntake, tvAverage, tvStreak;
    private ImageView imgProfile;
    private Button btnSave, btnLogout;
    private SwitchCompat switchNotifications;
    private Uri selectedImageUri;
    private ProfileController controller;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        etName = view.findViewById(R.id.etName);
        etPassword = view.findViewById(R.id.etPassword);
        etDailyGoal = view.findViewById(R.id.etDailyGoal);
        etCupSize = view.findViewById(R.id.etCupSize);

        tvEmail = view.findViewById(R.id.tvEmail);
        tvTotalIntake = view.findViewById(R.id.tvTotalIntake);
        tvAverage = view.findViewById(R.id.tvAverage);
        tvStreak = view.findViewById(R.id.tvStreak);

        imgProfile = view.findViewById(R.id.imgProfile);
        switchNotifications = view.findViewById(R.id.switchNotifications);

        btnSave = view.findViewById(R.id.btnSave);
        btnLogout = view.findViewById(R.id.btnLogout);

        controller = new ProfileController(this, getContext());
        controller.loadUserData();

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open file chooser directly from the Fragment (UI action)
                openFileChooser();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = etName.getText().toString().trim();
                String newPass = etPassword.getText().toString().trim();
                String newGoalStr = etDailyGoal.getText().toString().trim();
                String newCupStr = etCupSize.getText().toString().trim();
                boolean notificationsEnabled = switchNotifications.isChecked();
                controller.saveUserData(newName, newPass, newGoalStr, newCupStr, notificationsEnabled, selectedImageUri);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.logout();
            }
        });

        return view;
    }

    @Override
    public void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            // Persist permission
            try {
                getContext().getContentResolver().takePersistableUriPermission(selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception e) {
                e.printStackTrace();
            }

            imgProfile.setImageURI(selectedImageUri);
            imgProfile.setPadding(0, 0, 0, 0); // Remove padding when image is set
            imgProfile.setColorFilter(null); // Remove tint
        }
    }

    @Override
    public void setName(String name) {
        etName.setText(name);
    }

    @Override
    public void setEmail(String email) {
        tvEmail.setText(email);
    }

    @Override
    public void setDailyGoal(String goal) {
        etDailyGoal.setText(goal);
    }

    @Override
    public void setCupSize(String cupSize) {
        etCupSize.setText(cupSize);
    }

    @Override
    public void setNotificationsEnabled(boolean enabled) {
        switchNotifications.setChecked(enabled);
    }

    @Override
    public void setProfileImage(Uri uri) {
        imgProfile.setImageURI(uri);
        imgProfile.setPadding(0, 0, 0, 0);
        imgProfile.setColorFilter(null);
    }

    @Override
    public void setDefaultProfileImage() {
        imgProfile.setImageResource(R.drawable.ic_profile);
    }

    @Override
    public void setTotalIntake(String intake) {
        tvTotalIntake.setText(intake);
    }

    @Override
    public void setAverage(String average) {
        tvAverage.setText(average);
    }

    @Override
    public void setStreak(String streak) {
        tvStreak.setText(streak);
    }

    @Override
    public void showNameError(String message) {
        etName.setError(message);
    }

    @Override
    public void showGoalError(String message) {
        etDailyGoal.setError(message);
    }

    @Override
    public void showCupError(String message) {
        etCupSize.setError(message);
    }

    @Override
    public void showSaveSuccess() {
        Toast.makeText(getContext(), "Perfil atualizado!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToLogin() {
        Intent intent = new Intent(getActivity(), LoginUsuario.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
        startActivity(intent);
        getActivity().finish();
    }
}
