/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.loginregistrationsystem;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author taetjilehutso
 */
public class UserStore {
    private static final String SEPARATOR = "::";
    private final Path storagePath;

    public UserStore() {
        this.storagePath = Paths.get("data", "users.txt");
    }

    public synchronized boolean registerUser(String username, String fullName, String password) throws IOException {
        List<User> users = loadUsers();
        boolean exists = users.stream().anyMatch(user -> user.getUsername().equalsIgnoreCase(username));
        if (exists) {
            return false;
        }

        User user = new User(username.trim(), fullName.trim(), hashPassword(password));
        Path parent = storagePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(
                storagePath,
                encode(user) + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
        return true;
    }

    public synchronized Optional<User> authenticate(String username, String password) throws IOException {
        String normalizedUsername = username == null ? "" : username.trim();
        String passwordHash = hashPassword(password == null ? "" : password);

        return loadUsers().stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(normalizedUsername))
                .filter(user -> user.getPasswordHash().equals(passwordHash))
                .findFirst();
    }

    private List<User> loadUsers() throws IOException {
        List<User> users = new ArrayList<>();
        if (!Files.exists(storagePath)) {
            return users;
        }

        for (String line : Files.readAllLines(storagePath, StandardCharsets.UTF_8)) {
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.split(SEPARATOR, -1);
            if (parts.length == 3) {
                users.add(new User(parts[0], parts[1], parts[2]));
            }
        }
        return users;
    }

    private String encode(User user) {
        return user.getUsername() + SEPARATOR + user.getFullName() + SEPARATOR + user.getPasswordHash();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available.", exception);
        }
    }
}
    

