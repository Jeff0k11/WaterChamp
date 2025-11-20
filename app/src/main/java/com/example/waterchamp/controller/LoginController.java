package com.example.waterchamp.controller;

import com.example.waterchamp.model.User;
import com.example.waterchamp.model.UserDatabase;

public class LoginController {
    private LoginView view;

    public LoginController(LoginView view) {
        this.view = view;
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

        if (UserDatabase.usuariosCadastrados.containsKey(email) && UserDatabase.usuariosCadastrados.get(email).equals(senha)) {
            User user = UserDatabase.getUserByEmail(email);
            if (user == null) {
                user = new User("Usuário", email, 0);
                UserDatabase.addUser(user);
            }
            UserDatabase.currentUser = user;
            view.onLoginSuccess();
        } else {
            view.onLoginFailure("Email ou senha inválidos.");
        }
    }

    public interface LoginView {
        void showEmailError(String message);
        void showPasswordError(String message);
        void onLoginSuccess();
        void onLoginFailure(String message);
    }
}
