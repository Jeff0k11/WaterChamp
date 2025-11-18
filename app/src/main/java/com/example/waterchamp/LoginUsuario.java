package com.example.waterchamp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

public class LoginUsuario extends AppCompatActivity {

    private EditText loginUsuario;
    private EditText senhaUsuario;
    private Button btnLogin;
    private TextView criarCadastro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_usuario);

        loginUsuario = findViewById(R.id.loginUsuario);
        senhaUsuario = findViewById(R.id.senhaUsuario);
        btnLogin = findViewById(R.id.btnLogin);
        criarCadastro = findViewById(R.id.criarCadastro);

        // Ensure test user exists in both maps
        if (UserDatabase.usuariosCadastrados.isEmpty()) {
            String testEmail = "teste@email.com";
            UserDatabase.usuariosCadastrados.put(testEmail, "123456");
            
            // Check if test user is in usersList, if not add it
            boolean exists = false;
            for(User u : UserDatabase.usersList) {
                if(u.getEmail().equals(testEmail)) {
                    exists = true;
                    break;
                }
            }
            if(!exists) {
                UserDatabase.usersList.add(new User("Usuário Teste", testEmail, 0));
            }
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarLogin();
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

    private void validarLogin() {
        String email = loginUsuario.getText().toString().trim();
        String senha = senhaUsuario.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            loginUsuario.setError("Email é obrigatório.");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginUsuario.setError("Insira um email válido.");
            return;
        }

        if (TextUtils.isEmpty(senha)) {
            senhaUsuario.setError("Senha é obrigatória.");
            return;
        }

        if (UserDatabase.usuariosCadastrados.containsKey(email) && UserDatabase.usuariosCadastrados.get(email).equals(senha)) {
            // Login bem-sucedido
            User user = UserDatabase.getUserByEmail(email);
            if (user == null) {
                // Fallback if user exists in credentials but not in user list
                user = new User("Usuário", email, 0);
                UserDatabase.addUser(user);
            }
            UserDatabase.currentUser = user;

            Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginUsuario.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Credenciais inválidas
            Snackbar.make(findViewById(R.id.telaLogin), "Email ou senha inválidos.", Snackbar.LENGTH_LONG).show();
        }
    }
}