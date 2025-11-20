package com.example.waterchamp.controller;

import android.content.Context;
import android.text.TextUtils;
import com.example.waterchamp.data.repository.UserRepository;
import com.example.waterchamp.model.User;
import com.example.waterchamp.model.UserDatabase;

public class CadastroController {
    private CadastroView view;
    private UserRepository userRepository;

    public CadastroController(CadastroView view, Context context) {
        this.view = view;
        this.userRepository = new UserRepository(context);
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

        // Registrar usuário usando o repository
        userRepository.registerUser(nome, userEmail, userSenha, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                // Salvar no UserDatabase para compatibilidade
                UserDatabase.currentUser = user;
                UserDatabase.addUser(user);
                UserDatabase.usuariosCadastrados.put(userEmail, userSenha);

                view.onCadastroSuccess();
            }

            @Override
            public void onError(String message) {
                if (message.contains("já existe")) {
                    view.showEmailError("Este email já está cadastrado.");
                } else {
                    view.showEmailError(message);
                }
            }
        });
    }

    public interface CadastroView {
        void showNomeError(String message);
        void showEmailError(String message);
        void showSenhaError(String message);
        void showConfirmarSenhaError(String message);
        void onCadastroSuccess();
    }
}
