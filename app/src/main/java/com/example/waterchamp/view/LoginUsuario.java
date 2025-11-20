package com.example.waterchamp.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.waterchamp.R;
import com.example.waterchamp.controller.LoginController;
import com.example.waterchamp.model.UserDatabase;
import com.google.android.material.snackbar.Snackbar;

public class LoginUsuario extends AppCompatActivity implements LoginController.LoginView {

    private EditText loginUsuario;
    private EditText senhaUsuario;
    private Button btnLogin;
    private TextView criarCadastro;
    private LoginController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_usuario);

        loginUsuario = findViewById(R.id.loginUsuario);
        senhaUsuario = findViewById(R.id.senhaUsuario);
        btnLogin = findViewById(R.id.btnLogin);
        criarCadastro = findViewById(R.id.criarCadastro);

        controller = new LoginController(this);

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
