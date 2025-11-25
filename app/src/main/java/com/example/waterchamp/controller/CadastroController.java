package com.example.waterchamp.controller;

import android.content.Context;
import android.text.TextUtils;
import com.example.waterchamp.data.repository.UserRepository;
import com.example.waterchamp.model.User;
import com.example.waterchamp.utils.NetworkUtils;
import java.security.MessageDigest;
import java.util.UUID;

public class CadastroController {
    private CadastroView view;
    private UserRepository userRepository;
    private Context context;

    public CadastroController(CadastroView view, Context context) {
        this.view = view;
        this.context = context;
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

        // Verificar conexão de internet
        if (NetworkUtils.isNetworkAvailable(context)) {
            // Com internet: tentar registrar no Supabase
            registrarNoSupabase(nome, userEmail, userSenha);
        } else {
            // Sem internet: criar conta local temporária
            registrarLocalmente(nome, userEmail, userSenha);
        }
    }

    /**
     * Registra o usuário no Supabase (requer internet)
     */
    private void registrarNoSupabase(String nome, String userEmail, String userSenha) {
        userRepository.registerUser(nome, userEmail, userSenha, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                view.onCadastroSuccess();
            }

            @Override
            public void onError(String message) {
                // Se falhar por falta de internet, oferecer opção de conta local
                if (message.contains("Sem internet")) {
                    view.showRegistroOfflineOption(message);
                } else {
                    view.showEmailError(message);
                }
            }
        });
    }

    /**
     * Registra o usuário localmente (modo offline)
     * Dados serão sincronizados quando houver internet
     */
    private void registrarLocalmente(String nome, String userEmail, String userSenha) {
        try {
            // Gerar ID local único
            String localId = UUID.randomUUID().toString();

            // Criar hash da senha (SHA-256 simples para validação offline)
            String passwordHash = hashPassword(userSenha);

            // Salvar dados localmente
            userRepository.createLocalAccount(nome, userEmail, passwordHash, localId, new UserRepository.AuthCallback() {
                @Override
                public void onSuccess(User user) {
                    view.onCadastroSuccess();
                    view.showOfflineRegistroMessage("Conta criada offline! Quando conectar, seu registro será sincronizado automaticamente.");
                }

                @Override
                public void onError(String message) {
                    view.showEmailError(message);
                }
            });
        } catch (Exception e) {
            view.showEmailError("Erro ao criar conta local: " + e.getMessage());
        }
    }

    /**
     * Cria um hash simples da senha usando SHA-256
     * Nota: Em produção, usar uma biblioteca como BCrypt ou Argon2
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

    public interface CadastroView {
        void showNomeError(String message);
        void showEmailError(String message);
        void showSenhaError(String message);
        void showConfirmarSenhaError(String message);
        void onCadastroSuccess();
        void showRegistroOfflineOption(String message);
        void showOfflineRegistroMessage(String message);
    }
}
