package controller;
/**
 * Basic class. To do:
 * link against external database.
 * signup mechanism to create account.
 * Encryption
 * Etc.
 */
public class LoginController {
    /**
     * Returns true if passwd matches the username given.
     * @param username
     * @param passwd
     * @return
     */
    boolean checkPassword(String username, String passwd) {
        if( username.equals("Jo") && passwd.equals("hello25")) return true;
        return false;

    }
}

