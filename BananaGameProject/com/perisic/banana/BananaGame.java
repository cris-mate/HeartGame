package com.perisic.banana;

import com.perisic.banana.engine.DBConfig;
import com.perisic.banana.engine.DBManager;
import com.perisic.banana.engine.SessionManager;
import com.perisic.banana.peripherals.LoginGUI;

import java.sql.Connection;

/**
 * Entry point for the Banana Game application.
 * Initializes the database configuration, session manager, and database manager,
 * then launches the login interface for the user.
 */
public class BananaGame {

    /**
     * Starts the Banana Game application.
     *
     * @param args command-line arguments (not used).
     */
    public static void main( String[] args ) {
        DBConfig config = new DBConfig();
        Connection connection = config.getConnection();

        SessionManager session = new SessionManager(connection);
        DBManager dbManager = new DBManager(connection);
        new LoginGUI(session, dbManager);
    }
}
