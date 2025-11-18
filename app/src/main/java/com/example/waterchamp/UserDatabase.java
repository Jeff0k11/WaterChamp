package com.example.waterchamp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class UserDatabase {
    // This map is for authentication
    public static HashMap<String, String> usuariosCadastrados = new HashMap<>();
    
    // This list is for ranking
    public static List<User> usersList = new ArrayList<>();
    
    // The currently logged-in user
    public static User currentUser;

    static {
        // Dummy data for ranking
        usersList.add(new User("João Silva", "joao@email.com", 1800));
        usersList.add(new User("Maria Santos", "maria@email.com", 2200));
        usersList.add(new User("Pedro Oliveira", "pedro@email.com", 1500));
        usersList.add(new User("Ana Costa", "ana@email.com", 2500));
        usersList.add(new User("Lucas Pereira", "lucas@email.com", 1200));
    }

    public static void addUser(User user) {
        usersList.add(user);
        usuariosCadastrados.put(user.getEmail(), "123456"); // Default password for new users if not set
    }

    public static User getUserByEmail(String email) {
        for (User user : usersList) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }
}