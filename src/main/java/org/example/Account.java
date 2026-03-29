package org.example;

/**
 * Represents one user account in the system.
 *
 * Why this class exists:
 * - Your login page needs a real object to represent a user
 * - SessionManager should store the currently logged-in account
 * - Later, this class can grow to include statistics, progress, role permissions, etc.
 */
public class Account {

    // Unique username used to log in
    private String username;

    // Plain text password for now
    // This is okay for your course project because the spec explicitly says
    // strong password protection is not required for this assignment.
    private String password;

    // Simple admin flag for now
    // Later, this can become an enum like STUDENT / PARENT / TEACHER / ADMIN
    private boolean admin;

    /**
     * No-argument constructor.
     *
     * Why this exists:
     * - Useful for loading data from file
     * - Makes the class easier to instantiate in a flexible way
     */
    public Account() {
    }

    /**
     * Main constructor for creating a complete account.
     *
     * @param username the login username
     * @param password the login password
     * @param admin whether this account is an admin account
     */
    public Account(String username, String password, boolean admin) {
        this.username = username;
        this.password = password;
        this.admin = admin;
    }

    /**
     * Returns the username.
     *
     * @return the account username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the stored password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password new password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns whether this account is an admin.
     *
     * @return true if admin, false otherwise
     */
    public boolean isAdmin() {
        return admin;
    }

    /**
     * Sets whether this account is an admin.
     *
     * @param admin admin flag
     */
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    /**
     * Checks whether a typed password matches this account's password.
     *
     * Why this method exists:
     * - Keeps password comparison logic inside the Account class
     * - Makes AccountManager code cleaner
     *
     * @param attemptedPassword password entered by user
     * @return true if passwords match
     */
    public boolean verifyPassword(String attemptedPassword) {
        return password != null && password.equals(attemptedPassword);
    }
}