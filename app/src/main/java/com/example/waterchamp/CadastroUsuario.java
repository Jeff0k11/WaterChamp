package com.example.waterchamp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CadastroUsuario extends AppCompatActivity {

    private EditText nomeCompleto;
    private EditText email;
    private EditText senha;
    private EditText confirmarSenha;
    private Button btnCriar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cadastro_usuario);

        nomeCompleto = findViewById(R.id.nomeCompleto);
        email = findViewById(R.id.email);
        senha = findViewById(R.id.senha);
        confirmarSenha = findViewById(R.id.confirmarSenha);
        btnCriar = findViewById(R.id.btnCriar);

        btnCriar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarCadastro();
            }
        });
    }

    private void validarCadastro() {
        String nome = nomeCompleto.getText().toString().trim();
        String userEmail = email.getText().toString().trim();
        String userSenha = senha.getText().toString().trim();
        String userConfirmarSenha = confirmarSenha.getText().toString().trim();

        if (TextUtils.isEmpty(nome)) {
            nomeCompleto.setError("Nome é obrigatório.");
            return;
        }

        if (TextUtils.isEmpty(userEmail)) {
            email.setError("Email é obrigatório.");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            email.setError("Insira um email válido.");
            return;
        }

        if (UserDatabase.usuariosCadastrados.containsKey(userEmail)) {
            email.setError("Este email já está cadastrado.");
            return;
        }

        if (TextUtils.isEmpty(userSenha)) {
            senha.setError("Senha é obrigatória.");
            return;
        }

        if (userSenha.length() < 6) {
            senha.setError("A senha deve ter pelo menos 6 caracteres.");
            return;
        }

        if (!userSenha.equals(userConfirmarSenha)) {
            confirmarSenha.setError("As senhas não coincidem.");
            return;
        }

        // Cadastro bem-sucedido
        // Create new User object and add to database
        User newUser = new User(nome, userEmail, 0);
        UserDatabase.addUser(newUser);
        
        // Also add to the credentials map (addUser does this, but good to be explicit if logic changes)
        UserDatabase.usuariosCadastrados.put(userEmail, userSenha);
        
        Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(CadastroUsuario.this, LoginUsuario.class);
        startActivity(intent);
        finish();
    }
}