package com.example.waterchamp.data.repository;

import android.content.Context;
import com.example.waterchamp.data.local.PreferencesManager;
import com.example.waterchamp.data.remote.UserService;
import com.example.waterchamp.model.User;
import com.example.waterchamp.utils.CoroutineHelper;

import kotlin.Pair;

/**
 * Repository para gerenciar operações de usuário
 * Coordena entre serviço remoto (Supabase) e cache local (SharedPreferences)
 */
public class UserRepository {
    private final UserService userService;
    private final PreferencesManager prefsManager;

    public UserRepository(Context context) {
        this.userService = new UserService();
        this.prefsManager = new PreferencesManager(context);
    }

    /**
     * Interface para callbacks de operações assíncronas
     */
    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    public interface LogoutCallback {
        void onSuccess();
        void onError(String message);
    }

    /**
     * Registrar novo usuário
     */
    public void registerUser(String nome, String email, String senha, AuthCallback callback) {
        CoroutineHelper.runAsync(
                () -> userService.registerUserBlocking(nome, email, senha),
                new CoroutineHelper.CoroutineCallback<Pair<Integer, String>>() {
                    @Override
                    public void onComplete(Pair<Integer, String> result, String error) {
                        if (error != null) {
                            // Erro de coroutine/execução
                            String errorMsg = tratarErroDeConexao(error);
                            callback.onError(errorMsg);
                            return;
                        }

                        Integer userId = result.getFirst();
                        String errorMessage = result.getSecond();

                        if (errorMessage != null) {
                            // Erro retornado pelo Supabase (ex: usuário já existe)
                            String errorMsg = tratarErroDeConexao(errorMessage);
                            callback.onError(errorMsg);
                        } else if (userId != null) {
                            // Sucesso
                            prefsManager.setUserId(userId);
                            prefsManager.setUserName(nome);
                            prefsManager.setUserEmail(email);

                            User user = new User(nome, email, 0);
                            callback.onSuccess(user);
                        } else {
                            // Falha genérica
                            callback.onError("Ocorreu um erro desconhecido no cadastro.");
                        }
                    }
                }
        );
    }


    /**
     * Fazer login
     */
    public void login(String email, String senha, AuthCallback callback) {
        CoroutineHelper.runAsync(
            () -> {
                Pair<Integer, String> loginResult = userService.loginBlocking(email, senha);

                Integer userId = loginResult.getFirst();
                String loginError = loginResult.getSecond();

                if (loginError != null) {
                    // Tratar erro de conexão HTTP como "Sem internet"
                    String errorMsg = tratarErroDeConexao(loginError);
                    return new Pair<>(null, errorMsg);
                }

                if (userId != null) {
                    UserService.Usuario usuario = userService.getUserByIdBlocking(userId);
                    return new Pair<>(usuario, null);
                }

                return new Pair<>(null, "Erro desconhecido no login.");
            },
            new CoroutineHelper.CoroutineCallback<Pair<UserService.Usuario, String>>() {
                @Override
                public void onComplete(Pair<UserService.Usuario, String> result, String error) {
                    if (error != null) {
                        String errorMsg = tratarErroDeConexao(error);
                        callback.onError(errorMsg);
                        return;
                    }

                    UserService.Usuario usuario = result.getFirst();
                    String errorMsg = result.getSecond();

                    if (errorMsg != null) {
                        String mensagemAmigavel = tratarErroDeConexao(errorMsg);
                        callback.onError(mensagemAmigavel);
                    } else if (usuario != null) {
                        // Salvar dados localmente
                        prefsManager.setUserId(usuario.getId());
                        prefsManager.setUserName(usuario.getNome());
                        prefsManager.setUserEmail(usuario.getEmail());

                        // Criar objeto User
                        User user = new User(usuario.getNome(), usuario.getEmail(), 0);
                        callback.onSuccess(user);
                    } else {
                        callback.onError("Email ou senha inválidos.");
                    }
                }
            }
        );
    }

    /**
     * Trata erros de conexão e converte para mensagens amigáveis
     */
    private String tratarErroDeConexao(String erro) {
        if (erro == null || erro.isEmpty()) {
            return "Erro desconhecido.";
        }

        // Erros de conexão HTTP
        if (erro.contains("HttpRequest") ||
            erro.contains("HTTP") ||
            erro.contains("SocketException") ||
            erro.contains("SocketTimeoutException") ||
            erro.contains("UnknownHostException") ||
            erro.contains("ConnectException") ||
            erro.contains("EOFException") ||
            erro.contains("SSLException") ||
            erro.contains("SSLHandshakeException") ||
            erro.contains("CONNECT_TIMEOUT") ||
            erro.contains("Connection refused") ||
            erro.contains("Failed to connect")) {
            return "Sem internet. Verifique sua conexão e tente novamente.";
        }

        // Erros de timeout
        if (erro.contains("timeout") || erro.contains("Timeout")) {
            return "Conexão lenta ou expirada. Tente novamente.";
        }

        // Erros de certificado SSL
        if (erro.contains("SSL") || erro.contains("Certificate")) {
            return "Erro de segurança na conexão. Verifique a data e hora do seu dispositivo.";
        }

        // Erros de autenticação do Supabase
        if (erro.contains("Invalid login credentials")) {
            return "Email ou senha inválidos.";
        }

        if (erro.contains("Email not confirmed")) {
            return "Email não confirmado. Verifique sua caixa de entrada.";
        }

        // Retornar erro original se não se enquadrar em nenhuma categoria
        return erro;
    }

    /**
     * Fazer logout
     */
    public void logout(LogoutCallback callback) {
        CoroutineHelper.runAsync(
            () -> {
                userService.logoutBlocking();
                return true;
            },
            new CoroutineHelper.CoroutineCallback<Boolean>() {
                @Override
                public void onComplete(Boolean result, String error) {
                    if (error != null) {
                        callback.onError("Erro ao fazer logout: " + error);
                    } else {
                        // Limpar dados locais
                        prefsManager.clearUserData();
                        callback.onSuccess();
                    }
                }
            }
        );
    }

    /**
     * Obter usuário atualmente logado (do cache local)
     */
    public User getCurrentUser() {
        if (!prefsManager.isLoggedIn()) {
            return null;
        }

        String name = prefsManager.getUserName();
        String email = prefsManager.getUserEmail();
        int dailyGoal = prefsManager.getDailyGoal();
        int defaultCupSize = prefsManager.getDefaultCupSize();
        long totalConsumed = prefsManager.getTotalConsumedAllTime();

        User user = new User(name, email, 0);
        user.setDailyGoal(dailyGoal);
        user.setDefaultCupSize(defaultCupSize);
        user.setProfilePictureUri(prefsManager.getProfilePictureUri());
        user.setNotificationsEnabled(prefsManager.isNotificationsEnabled());

        return user;
    }

    /**
     * Atualizar configurações do usuário localmente
     */
    public void updateUserSettings(User user) {
        prefsManager.setDailyGoal(user.getDailyGoal());
        prefsManager.setDefaultCupSize(user.getDefaultCupSize());
        prefsManager.setProfilePictureUri(user.getProfilePictureUri());
        prefsManager.setNotificationsEnabled(user.isNotificationsEnabled());
    }

    /**
     * Verificar se usuário está logado
     */
    public boolean isLoggedIn() {
        return prefsManager.isLoggedIn();
    }

    /**
     * Obter ID do usuário logado
     */
    public int getCurrentUserId() {
        return prefsManager.getUserId();
    }

    public PreferencesManager getPreferencesManager() {
        return prefsManager;
    }
}
