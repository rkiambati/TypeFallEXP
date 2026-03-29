// File: src/main/java/org/example/AccountManager.java
package org.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles account storage, loading, saving, and login authentication.
 *
 * Why this class exists:
 * - Your LoginController should not know how JSON files work
 * - All account-related logic should live in one place
 * - This makes the app easier to extend later with create-user, reset-password, etc.
 */
public class AccountManager {

    // Local folder where app data will be stored
    private static final String DATA_DIRECTORY = "data";

    // JSON file path for accounts
    private static final String ACCOUNTS_FILE = DATA_DIRECTORY + "/accounts.json";

    // In-memory list of accounts loaded from JSON
    private final List<Account> accounts = new ArrayList<>();

    /**
     * Creates the manager and immediately loads accounts from JSON.
     *
     * Why this constructor does work:
     * - We want the manager to always be ready to authenticate users
     * - If the file does not exist yet, we create it and seed a default admin
     */
    public AccountManager() {
        loadAccountsFromJson();
        ensureDefaultAdminExists();
    }

    /**
     * Authenticates a username/password login attempt.
     *
     * @param username the typed username
     * @param password the typed password
     * @return the matching Account if credentials are correct, otherwise null
     */
    public Account authenticateLogin(String username, String password) {
        for (Account account : accounts) {
            if (account.getUsername().equalsIgnoreCase(username)
                    && account.verifyPassword(password)) {
                return account;
            }
        }
        return null;
    }

    /**
     * Returns all loaded accounts.
     *
     * Why this exists:
     * - Useful later for parent/teacher tables and account management screens
     *
     * @return copy of account list
     */
    public List<Account> getAllAccounts() {
        return new ArrayList<>(accounts);
    }

    /**
     * Creates a new account and saves it to JSON.
     *
     * @param username new username
     * @param password new password
     * @param admin whether the account is admin
     * @return created account
     */
    public Account createAccount(String username, String password, boolean admin) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        if (findAccountByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists.");
        }

        Account account = new Account(username.trim(), password, admin);
        accounts.add(account);
        saveAccountsToJson();
        return account;
    }

    /**
     * Finds an account by username.
     *
     * @param username username to search for
     * @return matching account or null if not found
     */
    public Account findAccountByUsername(String username) {
        if (username == null) {
            return null;
        }

        for (Account account : accounts) {
            if (account.getUsername().equalsIgnoreCase(username.trim())) {
                return account;
            }
        }

        return null;
    }

    /**
     * Ensures that at least one admin account exists.
     *
     * Why this exists:
     * - You asked to create an admin account for now
     * - This makes first-run setup automatic
     * - If accounts.json is empty, the app is still usable immediately
     *
     * Default credentials for now:
     * username: admin
     * password: admin123
     */
    private void ensureDefaultAdminExists() {
        Account adminAccount = findAccountByUsername("admin");

        if (adminAccount == null) {
            accounts.add(new Account("admin", "admin123", true));
            saveAccountsToJson();
        }
    }

    /**
     * Loads accounts from the JSON file into memory.
     *
     * Why manual JSON:
     * - Keeps setup simple right now
     * - Avoids needing external libraries at this stage
     * - Good enough for a small controlled JSON format
     */
    private void loadAccountsFromJson() {
        accounts.clear();

        try {
            // Make sure the data folder exists
            Files.createDirectories(Path.of(DATA_DIRECTORY));

            Path filePath = Path.of(ACCOUNTS_FILE);

            // If file doesn't exist yet, create an empty JSON array
            if (!Files.exists(filePath)) {
                Files.writeString(filePath, "[]", StandardCharsets.UTF_8);
                return;
            }

            String json = Files.readString(filePath, StandardCharsets.UTF_8).trim();

            if (json.isEmpty() || json.equals("[]")) {
                return;
            }

            // Match each object like:
            // { "username":"admin", "password":"admin123", "admin":true }
            Pattern objectPattern = Pattern.compile("\\{(.*?)\\}", Pattern.DOTALL);
            Matcher objectMatcher = objectPattern.matcher(json);

            while (objectMatcher.find()) {
                String objectText = objectMatcher.group();

                String username = extractStringValue(objectText, "username");
                String password = extractStringValue(objectText, "password");
                boolean admin = extractBooleanValue(objectText, "admin");

                if (username != null && password != null) {
                    accounts.add(new Account(username, password, admin));
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load accounts from JSON.", e);
        }
    }

    /**
     * Saves all accounts to the JSON file.
     *
     * Why this exists:
     * - Whenever accounts change, we want the JSON file updated
     * - Keeps persistence centralized in one class
     */
    private void saveAccountsToJson() {
        try {
            Files.createDirectories(Path.of(DATA_DIRECTORY));

            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("[\n");

            for (int i = 0; i < accounts.size(); i++) {
                Account account = accounts.get(i);

                jsonBuilder.append("  {\n");
                jsonBuilder.append("    \"username\": \"").append(escapeJson(account.getUsername())).append("\",\n");
                jsonBuilder.append("    \"password\": \"").append(escapeJson(account.getPassword())).append("\",\n");
                jsonBuilder.append("    \"admin\": ").append(account.isAdmin()).append("\n");
                jsonBuilder.append("  }");

                if (i < accounts.size() - 1) {
                    jsonBuilder.append(",");
                }

                jsonBuilder.append("\n");
            }

            jsonBuilder.append("]");

            Files.writeString(
                    Path.of(ACCOUNTS_FILE),
                    jsonBuilder.toString(),
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {
            throw new RuntimeException("Failed to save accounts to JSON.", e);
        }
    }

    /**
     * Extracts a string field from a very small known JSON object.
     *
     * Example:
     * field = username
     * looks for "username": "value"
     *
     * @param jsonObject single object text
     * @param fieldName field to extract
     * @return string value or null
     */
    private String extractStringValue(String jsonObject, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(jsonObject);

        if (matcher.find()) {
            return unescapeJson(matcher.group(1));
        }

        return null;
    }

    /**
     * Extracts a boolean field from a small JSON object.
     *
     * Example:
     * "admin": true
     *
     * @param jsonObject single object text
     * @param fieldName field to extract
     * @return boolean value, defaults to false if missing
     */
    private boolean extractBooleanValue(String jsonObject, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*(true|false)");
        Matcher matcher = pattern.matcher(jsonObject);

        if (matcher.find()) {
            return Boolean.parseBoolean(matcher.group(1));
        }

        return false;
    }

    /**
     * Escapes special characters before writing JSON.
     *
     * @param input raw text
     * @return escaped text
     */
    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }

        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    /**
     * Reverses minimal escaping when reading JSON.
     *
     * @param input escaped text
     * @return unescaped text
     */
    private String unescapeJson(String input) {
        if (input == null) {
            return null;
        }

        return input
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}