package com.example.waterchamp.controller;

import android.text.TextUtils;
import com.example.waterchamp.model.User;
import com.example.waterchamp.model.UserDatabase;

public class CadastroController {
    private CadastroView view;

    public CadastroController(CadastroView view) {
        this.view = view;
    }

    public void validarCadastro(String nome, String userEmail, String userSenha, String userConfirmarSenha) {
        if (TextUtils.isEmpty(nome)) {
            view.showNomeError("Nome é obrigatório.");
            return;
        }

        if (TextUtils.isEmpty(userEmail)) {
            view.showEmailError("Email é obrigatório.");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            view.showEmailError("Insira um email válido.");
            return;
        }

        if (UserDatabase.usuariosCadastrados.containsKey(userEmail)) {
            view.showEmailError("Este email já está cadastrado.");
            return;
        }

        if (TextUtils.isEmpty(userSenha)) {
            view.showSenhaError("Senha é obrigatória.");
            return;
        }

        if (userSenha.length() < 6) {
            view.showSenhaError("A senha deve ter pelo menos 6 caracteres.");
            return;
        }

        if (!userSenha.equals(userConfirmarSenha)) {
            view.showConfirmarSenhaError("As senhas não coincidem.");
            return;
        }

        // Cadastro bem-sucedido
        // Create new User object and add to database
        User newUser = new User(nome, userEmail, 0);
        UserDatabase.addUser(newUser);

        // Also add to the credentials map (addUser does this, but good to be explicit if logic changes)
        UserDatabase.usuariosCadastrados.put(userEmail, userSenha);

        view.onCadastroSuccess();
    }

    public interface CadastroView {
        void showNomeError(String message);
        void showEmailError(String message);
        void showSenhaError(String message);
        void showConfirmarSenhaError(String message);
        void onCadastroSuccess();
    }
}
