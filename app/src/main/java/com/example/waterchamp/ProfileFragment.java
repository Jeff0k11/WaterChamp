package com.example.waterchamp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etName, etPassword, etDailyGoal, etCupSize;
    private TextView tvEmail, tvTotalIntake, tvAverage, tvStreak;
    private ImageView imgProfile;
    private Button btnSave, btnLogout;
    private SwitchCompat switchNotifications;
    private Uri selectedImageUri;

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

        loadUserData();

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserDatabase.currentUser = null; // Clear session
                Intent intent = new Intent(getActivity(), LoginUsuario.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }

    private void openFileChooser() {
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

    private void loadUserData() {
        if (UserDatabase.currentUser != null) {
            User user = UserDatabase.currentUser;
            
            etName.setText(user.getName());
            tvEmail.setText(user.getEmail());
            etDailyGoal.setText(String.valueOf(user.getDailyGoal()));
            etCupSize.setText(String.valueOf(user.getDefaultCupSize()));
            switchNotifications.setChecked(user.isNotificationsEnabled());
            
            if (user.getProfilePictureUri() != null) {
                try {
                    Uri uri = Uri.parse(user.getProfilePictureUri());
                    imgProfile.setImageURI(uri);
                    imgProfile.setPadding(0, 0, 0, 0);
                    imgProfile.setColorFilter(null);
                } catch (Exception e) {
                    imgProfile.setImageResource(R.drawable.ic_profile);
                }
            } else {
                 // Reset to default if no URI
                imgProfile.setImageResource(R.drawable.ic_profile);
            }
            
            // Statistics
            long total = user.getTotalConsumedAllTime();
            if (total >= 1000) {
                tvTotalIntake.setText(String.format("%.1f L", total / 1000.0));
            } else {
                tvTotalIntake.setText(total + " ml");
            }
            
            // Average
            long daysSinceCreation = (System.currentTimeMillis() - user.getCreationDate()) / (1000 * 60 * 60 * 24);
            if (daysSinceCreation < 1) daysSinceCreation = 1;
            long average = total / daysSinceCreation;
            tvAverage.setText(average + " ml");
            
            tvStreak.setText(user.getStreak() + " 🔥");
        }
    }

    private void saveUserData() {
        if (UserDatabase.currentUser != null) {
            String newName = etName.getText().toString().trim();
            String newPass = etPassword.getText().toString().trim();
            String newGoalStr = etDailyGoal.getText().toString().trim();
            String newCupStr = etCupSize.getText().toString().trim();

            if (TextUtils.isEmpty(newName)) {
                etName.setError("Nome não pode ser vazio");
                return;
            }
            
            if (TextUtils.isEmpty(newGoalStr)) {
                etDailyGoal.setError("Meta inválida");
                return;
            }
            
            if (TextUtils.isEmpty(newCupStr)) {
                etCupSize.setError("Tamanho inválido");
                return;
            }

            UserDatabase.currentUser.setName(newName);
            UserDatabase.currentUser.setDailyGoal(Integer.parseInt(newGoalStr));
            UserDatabase.currentUser.setDefaultCupSize(Integer.parseInt(newCupStr));
            UserDatabase.currentUser.setNotificationsEnabled(switchNotifications.isChecked());
            
            if (selectedImageUri != null) {
                UserDatabase.currentUser.setProfilePictureUri(selectedImageUri.toString());
            }

            if (!TextUtils.isEmpty(newPass)) {
                // Update password in the main credentials map
                UserDatabase.usuariosCadastrados.put(UserDatabase.currentUser.getEmail(), newPass);
            }

            Toast.makeText(getContext(), "Perfil atualizado!", Toast.LENGTH_SHORT).show();
        }
    }
}