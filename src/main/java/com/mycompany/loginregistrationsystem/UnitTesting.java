/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.loginregistrationsystem;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


/**
 *
 * @author taetjilehutso
 */
public class UnitTesting {


class LoginRegistrationSystemTest {

}
/**
 * Unit tests for the UserStore class.
 * Uses a temporary directory so tests are fully isolated and leave no side-effects.
 */
class UserStoreTest {


    private UserStore userStore;


    // ─── registerUser ────────────────────────────────────────────────────────────

    @Test
    public void registerUser_ShouldReturnTrue_WhenNewUserIsRegistered() throws IOException {
        boolean result = userStore.registerUser("alice", "Alice Smith", "secret");
        assertTrue(result);
    }

    @Test
    public void registerUser_ShouldReturnFalse_WhenUsernameAlreadyExists() throws IOException {
        userStore.registerUser("alice", "Alice Smith", "secret");
        boolean result = userStore.registerUser("alice", "Alice Again", "otherpass");
        assertFalse(result);
    }

    @Test
    public void registerUser_ShouldBeCaseInsensitive_ForDuplicateCheck() throws IOException {
        userStore.registerUser("alice", "Alice Smith", "secret");
        boolean result = userStore.registerUser("ALICE", "Alice Upper", "secret");
        assertFalse(result);
    }

    @Test
    public void registerUser_ShouldPersistUserToFile() throws IOException {
        Field pathField;
        try {
            pathField = UserStore.class.getDeclaredField("storagePath");
            pathField.setAccessible(true);
            Path storagePath = (Path) pathField.get(userStore);

            userStore.registerUser("bob", "Bob Jones", "pass");

            assertTrue(Files.exists(storagePath));
            String content = Files.readString(storagePath, StandardCharsets.UTF_8);
            assertTrue(content.contains("bob"));
            assertTrue(content.contains("Bob Jones"));
        } catch (IOException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

      
    @Test
    public void registerUser_ShouldAllowMultipleDifferentUsers() throws IOException {
        assertTrue(userStore.registerUser("alice", "Alice", "pass1"));
        assertTrue(userStore.registerUser("bob", "Bob", "pass2"));
        assertTrue(userStore.registerUser("charlie", "Charlie", "pass3"));
    }

    @Test
    public void registerUser_ShouldTrimUsernameAndFullName() throws IOException {
        boolean registered = userStore.registerUser("  dave  ", "  Dave Brown  ", "pass");
        assertTrue(registered);

        // Authenticating with trimmed username should succeed
        Optional<User> user = userStore.authenticate("dave", "pass");
        assertTrue(user.isPresent());
        assertEquals("Dave Brown", user.get().getFullName());
    }

    // ─── authenticate ─────────────────────────────────────────────────────────────

    @Test
    public void authenticate_ShouldReturnUser_WhenCredentialsAreCorrect() throws IOException {
        userStore.registerUser("alice", "Alice Smith", "secret");
        Optional<User> result = userStore.authenticate("alice", "secret");

        assertTrue(result.isPresent());
        assertEquals("Alice Smith", result.get().getFullName());
    }

    @Test
    public void authenticate_ShouldReturnEmpty_WhenPasswordIsWrong() throws IOException {
        userStore.registerUser("alice", "Alice Smith", "secret");
        Optional<User> result = userStore.authenticate("alice", "wrongpass");

        assertFalse(result.isPresent());
    }

    @Test
    public void authenticate_ShouldReturnEmpty_WhenUsernameDoesNotExist() throws IOException {
        Optional<User> result = userStore.authenticate("nobody", "pass");
        assertFalse(result.isPresent());
    }

    @Test
    public void authenticate_ShouldBeCaseInsensitive_ForUsername() throws IOException {
        userStore.registerUser("alice", "Alice Smith", "secret");
        Optional<User> result = userStore.authenticate("ALICE", "secret");

        assertTrue(result.isPresent());
    }

    @Test
    public void authenticate_ShouldReturnEmpty_WhenUsernameIsNull() throws IOException {
        userStore.registerUser("alice", "Alice Smith", "secret");
        Optional<User> result = userStore.authenticate(null, "secret");

        assertFalse(result.isPresent());
    }

    @Test
    public void authenticate_ShouldReturnEmpty_WhenPasswordIsNull() throws IOException {
        userStore.registerUser("alice", "Alice Smith", "secret");
        Optional<User> result = userStore.authenticate("alice", null);

        assertFalse(result.isPresent());
    }

    @Test
    public void authenticate_ShouldReturnEmpty_WhenBothFieldsAreNull() throws IOException {
        Optional<User> result = userStore.authenticate(null, null);
        assertFalse(result.isPresent());
    }

    @Test
    public void authenticate_ShouldReturnEmpty_WhenFileDoesNotExist() throws IOException {
        // No users registered, so no file exists yet
        Optional<User> result = userStore.authenticate("alice", "secret");
        assertFalse(result.isPresent());
    }

    @Test
    public void authenticate_ShouldNotAcceptEmptyPassword_AsValidCredential() throws IOException {
        userStore.registerUser("alice", "Alice", "secret");
        Optional<User> result = userStore.authenticate("alice", "");
        assertFalse(result.isPresent());
    }

    // ─── password hashing ─────────────────────────────────────────────────────────

    @Test
    public void authenticate_ShouldHashPasswordBeforeComparing() throws IOException {
        // Registers with plain text; internally hashed. The stored value must not be plain text.
        userStore.registerUser("alice", "Alice", "mypassword");

        try {
            Field pathField = UserStore.class.getDeclaredField("storagePath");
            pathField.setAccessible(true);
            Path storagePath = (Path) pathField.get(userStore);
            String content = Files.readString(storagePath, StandardCharsets.UTF_8);

            // The plain-text password must NOT appear in the file
            assertFalse(content.contains("mypassword"), "Plain-text password should not be stored");
        } catch (IOException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }


    @Test
    public void registerUser_ShouldStoreSameHashForSamePassword() throws IOException {
        userStore.registerUser("alice", "Alice", "samepass");
        userStore.registerUser("bob", "Bob", "samepass");

        try {
            Field pathField = UserStore.class.getDeclaredField("storagePath");
            pathField.setAccessible(true);
            Path storagePath = (Path) pathField.get(userStore);
            java.util.List<String> lines = Files.readAllLines(storagePath, StandardCharsets.UTF_8);

            // Extract hash portion (3rd segment split by "::")
            String hashAlice = lines.get(0).split("::")[2];
            String hashBob   = lines.get(1).split("::")[2];

            assertEquals(hashAlice, hashBob, "Same password must produce the same hash");
        } catch (IOException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException e) {
            fail("Reflection failed: " + e.getMessage());
                       }
        }
 
    } // end UserStoreTest
 
} // end UnitTesting
 
      
    
        


    
