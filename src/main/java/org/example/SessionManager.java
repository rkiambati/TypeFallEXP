package org.example;


/**
 * Stores application session data for the currently logged-in user.
 *
 * Why this class exists:
 * - After login succeeds, the app needs to remember who is logged in
 * - Main page and later screens can read the current account from here
 */
public final class SessionManager {

    // Static reference to current logged-in user
    private static Account currentAccount;

    /**
     * Private constructor because this class should not be instantiated.
     */
    private SessionManager() {
    }

    /**
     * Sets the current logged-in account.
     *
     * @param account account to store in session
     */
    public static void setCurrentAccount(Account account) {
        currentAccount = account;
    }

    /**
     * Returns the current logged-in account.
     *
     * @return current account or null if none
     */
    public static Account getCurrentAccount() {
        return currentAccount;
    }

    /**
     * Clears the current session.
     *
     * Why this exists:
     * - Needed for logout
     * - Also useful before a new login attempt
     */
    public static void clearSession() {
        currentAccount = null;
    }

    /**
     * Returns whether someone is logged in.
     *
     * @return true if an account is active in session
     */
    public static boolean isLoggedIn() {
        return currentAccount != null;
    }
}