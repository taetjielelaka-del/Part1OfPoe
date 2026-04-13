/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.loginregistrationsystem;
import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;
/**
 *
 * @author taetjilehutso
 */
public class LoginRegistrationSystem {

    public static void main(String[] args) {
        UserStore userStore = new UserStore();

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;

            while (running) {
                printMenu();
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        registerUser(scanner, userStore);
                        break;
                    case "2":
                        loginUser(scanner, userStore);
                        break;
                    case "3":
                        running = false;
                        System.out.println("Goodbye.");
                        break;
                    default:
                        System.out.println("Invalid choice. Please select 1, 2, or 3.");
                }

                System.out.println();
            }
        }
    }

    private static void printMenu() {
        System.out.println("=== Login and Registration System ===");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
    }

    private static void registerUser(Scanner scanner, UserStore userStore) {
        System.out.println();
        System.out.println("--- Register ---");
        System.out.print("Enter full name: ");
        String fullName = scanner.nextLine().trim();

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.print("Confirm password: ");
        String confirmPassword = scanner.nextLine();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            System.out.println("All fields are required.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            System.out.println("Passwords do not match.");
            return;
        }

        try {
            boolean created = userStore.registerUser(username, fullName, password);
            if (created) {
                System.out.println("Registration successful.");
            } else {
                System.out.println("Username already exists.");
            }
        } catch (IOException exception) {
            System.out.println("Error saving user: " + exception.getMessage());
        }
    }

    private static void loginUser(Scanner scanner, UserStore userStore) {
        System.out.println();
        System.out.println("--- Login ---");
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username and password are required.");
            return;
        }

        try {
            Optional<User> user = userStore.authenticate(username, password);
            if (user.isPresent()) {
                System.out.println("Login successful. Welcome, " + user.get().getFullName() + ".");
            } else {
                System.out.println("Invalid username or password.");
            }
        } catch (IOException exception) {
            System.out.println("Error reading users: " + exception.getMessage());
        }
    }
}
    
