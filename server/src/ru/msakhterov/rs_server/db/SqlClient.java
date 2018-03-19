package ru.msakhterov.rs_server.db;

import java.sql.*;

public class SqlClient {

    private static Connection connection;
    private static Statement statement;

    public synchronized static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:usersDB.db");
            statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS Users (\n" +
                    " id INTEGER PRIMARY KEY AUTOINCREMENT, \n" +
                    " login TEXT,\n" +
                    " password TEXT,\n" +
                    " email TEXT);");
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized static String checkAuth(String login, String password) {
        System.out.println("** Авторизация");
        String request = "SELECT login FROM users WHERE login='" +
                login + "' AND password='" + password + "'";
        try (ResultSet set = statement.executeQuery(request)) {
            if (set.next()) {
                return set.getString(1);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized static String makeReg(String login, String password, String email) {
        System.out.println("** Регистрация");
        String request = "SELECT count(*) FROM users WHERE email='" +
                email + "'";
        int count = 0;
        try (ResultSet set = statement.executeQuery(request)) {
            if (set.next())
                count++;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (count != 0) {
            request = "INSERT INTO users (login, password, email) VALUES ('" + login + "', '" + password + "', '" + email + "')";
            try {
                statement.executeUpdate(request);
                return login;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else return null;
    }
}
