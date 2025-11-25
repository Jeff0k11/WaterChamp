package com.example.waterchamp.controller;

import android.content.Context;
import com.example.waterchamp.data.local.PreferencesManager;
import com.example.waterchamp.data.repository.UserRepository;
import com.example.waterchamp.model.User;
import com.example.waterchamp.model.UserDatabase;
import com.example.waterchamp.utils.NetworkUtils;
import java.security.MessageDigest;

public class LoginController {
    private LoginView view;
    private UserRepository userRepository;
    private PreferencesManager prefsManager;
    private Context context;

    public LoginController(LoginView view, Context context) {
        this.view = view;
        this.context = context;
        this.userRepository = new UserRepository(context);
        this.prefsManager = new PreferencesManager(context);
    }

    /**
     * Verifica se há sessão ativa (usuário já logado)
     * Se sim, pula a tela de login automaticamente
     */
    public boolean tryAutoLogin() {
        if (prefsManager.isLoggedIn()) {
            // Usuário já estava logado, permitir entrada offline
            loadCurrentUserData();
            return true;
        }
        return false;
    }

    /**
     * Carrega dados do usuário atualmente logado do cache local
     */
    private void loadCurrentUserData() {
        User user = userRepository.getCurrentUser();
        if (user != null) {
            UserDatabase.currentUser = user;
            view.onLoginSuccess();
        }
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

        // Verificar conexão de internet
        if (NetworkUtils.isNetworkAvailable(context)) {
            // Com internet: tentar login no Supabase
            loginNoSupabase(email, senha);
        } else {
            // Sem internet: tentar login local
            loginLocalmente(email, senha);
        }
    }

    /**
     * Realiza login no Supabase (requer internet)
     */
    private void loginNoSupabase(String email, String senha) {
        userRepository.login(email, senha, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                UserDatabase.currentUser = user;
                view.onLoginSuccess();
            }

            @Override
            public void onError(String message) {
                // Se falhar por falta de internet, oferecer login local
                if (message.contains("Sem internet")) {
                    view.showLoginOfflineOption(message);
                } else {
                    view.onLoginFailure(message);
                }
            }
        });
    }

    /**
     * Realiza login offline (modo local)
     * Valida credenciais contra hash armazenado localmente
     */
    private void loginLocalmente(String email, String senha) {
        try {
            // Verificar se existe conta local com este email
            String storedEmail = prefsManager.getUserEmail();
            String storedPasswordHash = prefsManager.getLocalPasswordHash();

            // Se não é uma conta local, não pode fazer login offline
            if (!prefsManager.isLocalAccount() && storedPasswordHash.isEmpty()) {
                view.onLoginFailure("Esta conta não foi criada offline. Conecte à internet para fazer login.");
                return;
            }

            // Validar email
            if (!email.equalsIgnoreCase(storedEmail)) {
                view.onLoginFailure("Email ou senha inválidos.");
                return;
            }

            // Criar hash da senha fornecida e comparar
            String providedPasswordHash = hashPassword(senha);
            if (!providedPasswordHash.equals(storedPasswordHash)) {
                view.onLoginFailure("Email ou senha inválidos.");
                return;
            }

            // Login bem-sucedido offline
            loadCurrentUserData();
            view.showLoginOfflineMessage("Conectado no modo offline. Seus dados serão sincronizados quando houver internet.");
            view.onLoginSuccess();

        } catch (Exception e) {
            view.onLoginFailure("Erro ao fazer login: " + e.getMessage());
        }
    }

    /**
     * Cria um hash simples da senha usando SHA-256
     * Deve ser o mesmo algoritmo usado em registrarLocalmente()
     */
    private String hashPassword(String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] messageDigest = md.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : messageDigest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public interface LoginView {
        void showEmailError(String message);
        void showPasswordError(String message);
        void onLoginSuccess();
        void onLoginFailure(String message);
        void showLoginOfflineOption(String message);
        void showLoginOfflineMessage(String message);
    }
}
