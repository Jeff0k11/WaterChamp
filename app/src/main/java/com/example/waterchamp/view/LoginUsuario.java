package com.example.waterchamp.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.waterchamp.R;
import com.example.waterchamp.controller.LoginController;
import com.example.waterchamp.data.local.PreferencesManager;
import com.example.waterchamp.model.UserDatabase;
import com.google.android.material.snackbar.Snackbar;

public class LoginUsuario extends AppCompatActivity implements LoginController.LoginView {

    private EditText loginUsuario;
    private EditText senhaUsuario;
    private CheckBox cbRememberLogin;
    private Button btnLogin;
    private TextView criarCadastro;
    private LoginController controller;
    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_usuario);

        loginUsuario = findViewById(R.id.loginUsuario);
        senhaUsuario = findViewById(R.id.senhaUsuario);
        cbRememberLogin = findViewById(R.id.cbRememberLogin);
        btnLogin = findViewById(R.id.btnLogin);
        criarCadastro = findViewById(R.id.criarCadastro);

        controller = new LoginController(this, this);
        preferencesManager = new PreferencesManager(this);

        // Carregar credenciais salvas se existirem
        loadSavedCredentials();

        // Ensure test user exists in both maps
        if (UserDatabase.usuariosCadastrados.isEmpty()) {
            String testEmail = "teste@email.com";
            UserDatabase.usuariosCadastrados.put(testEmail, "123456");

            // Check if test user is in usersList, if not add it
            boolean exists = false;
            for(com.example.waterchamp.model.User u : UserDatabase.usersList) {
                if(u.getEmail().equals(testEmail)) {
                    exists = true;
                    break;
                }
            }
            if(!exists) {
                UserDatabase.usersList.add(new com.example.waterchamp.model.User("Usuário Teste", testEmail, 0));
            }
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = loginUsuario.getText().toString().trim();
                String senha = senhaUsuario.getText().toString().trim();

                // Salvar credenciais se CheckBox estiver marcado
                if (cbRememberLogin.isChecked()) {
                    preferencesManager.setRememberLogin(true);
                    preferencesManager.setSavedEmail(email);
                    preferencesManager.setSavedPassword(senha);
                } else {
                    // Limpar credenciais salvas se desmarcar
                    preferencesManager.clearSavedCredentials();
                }

                controller.validateLogin(email, senha);
            }
        });

        criarCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginUsuario.this, CadastroUsuario.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Carrega as credenciais salvas e preenche os campos
     */
    private void loadSavedCredentials() {
        if (preferencesManager.isRememberLogin()) {
            String savedEmail = preferencesManager.getSavedEmail();
            String savedPassword = preferencesManager.getSavedPassword();

            if (!savedEmail.isEmpty() && !savedPassword.isEmpty()) {
                loginUsuario.setText(savedEmail);
                senhaUsuario.setText(savedPassword);
                cbRememberLogin.setChecked(true);
            }
        }
    }

    @Override
    public void showEmailError(String message) {
        loginUsuario.setError(message);
    }

    @Override
    public void showPasswordError(String message) {
        senhaUsuario.setError(message);
    }

    @Override
    public void onLoginSuccess() {
        Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginUsuario.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onLoginFailure(String message) {
        Snackbar.make(findViewById(R.id.telaLogin), message, Snackbar.LENGTH_LONG).show();
    }
}
