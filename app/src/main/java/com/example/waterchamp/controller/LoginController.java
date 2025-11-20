package com.example.waterchamp.controller;

import android.content.Context;
import com.example.waterchamp.data.repository.UserRepository;
import com.example.waterchamp.model.User;
import com.example.waterchamp.model.UserDatabase;

public class LoginController {
    private LoginView view;
    private UserRepository userRepository;

    public LoginController(LoginView view, Context context) {
        this.view = view;
        this.userRepository = new UserRepository(context);
    }

    public void validateLogin(String email, String senha) {
        if (email.isEmpty()) {
            view.showEmailError("Email é obrigatório.");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view.showEmailError("Insira um email válido.");
            return;
        }

        if (senha.isEmpty()) {
            view.showPasswordError("Senha é obrigatória.");
            return;
        }

        // Fazer login usando o repository
        userRepository.login(email, senha, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                // Salvar no UserDatabase para compatibilidade com código existente
                UserDatabase.currentUser = user;
                view.onLoginSuccess();
            }

            @Override
            public void onError(String message) {
                view.onLoginFailure(message);
            }
        });
    }

    public interface LoginView {
        void showEmailError(String message);
        void showPasswordError(String message);
        void onLoginSuccess();
        void onLoginFailure(String message);
    }
}
