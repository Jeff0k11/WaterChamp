package com.example.waterchamp.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.waterchamp.R;
import com.example.waterchamp.controller.CadastroController;

public class CadastroUsuario extends AppCompatActivity implements CadastroController.CadastroView {

    private EditText nomeCompleto;
    private EditText email;
    private EditText senha;
    private EditText confirmarSenha;
    private Button btnCriar;
    private CadastroController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cadastro_usuario);

        nomeCompleto = findViewById(R.id.nomeCompleto);
        email = findViewById(R.id.email);
        senha = findViewById(R.id.senha);
        confirmarSenha = findViewById(R.id.confirmarSenha);
        btnCriar = findViewById(R.id.btnCriar);

        controller = new CadastroController(this, this);

        btnCriar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nome = nomeCompleto.getText().toString().trim();
                String userEmail = email.getText().toString().trim();
                String userSenha = senha.getText().toString().trim();
                String userConfirmarSenha = confirmarSenha.getText().toString().trim();
                controller.validarCadastro(nome, userEmail, userSenha, userConfirmarSenha);
            }
        });
    }

    @Override
    public void showNomeError(String message) {
        nomeCompleto.setError(message);
    }

    @Override
    public void showEmailError(String message) {
        email.setError(message);
    }

    @Override
    public void showSenhaError(String message) {
        senha.setError(message);
    }

    @Override
    public void showConfirmarSenhaError(String message) {
        confirmarSenha.setError(message);
    }

    @Override
    public void onCadastroSuccess() {
        Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(CadastroUsuario.this, LoginUsuario.class);
        startActivity(intent);
        finish();
    }
}
