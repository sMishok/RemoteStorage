package ru.msakhterov.rs_server.db;

import java.io.File;
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
        System.out.println("Авторизация пользователя: " + login);
        String request = "SELECT login FROM Users WHERE login='" +
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
        System.out.println("Регистрация пользователя: " + login);
        String request = "SELECT count(*) as countRow FROM Users WHERE email='" +
                email + "'";
        int count = 0;
        try (ResultSet set = statement.executeQuery(request)) {
            if (set.next())
                count = set.getInt("countRow");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (count == 0) {
            request = "INSERT INTO Users (login, password, email) VALUES ('" + login + "', '" + password + "', '" + email + "')";
            try {
                statement.executeUpdate(request);
                File userDir = new File("RemoteStorageFiles\\"+login);
                userDir.mkdir();
                return login;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else return null;
    }

    public synchronized static String showAllUsers() {
        System.out.println("Показать данные всех пользователей");
        String request = "SELECT login, password, email FROM Users";
        StringBuilder tempData = new StringBuilder();
        int userNumber = 1;
        String userData = null;
        try (ResultSet set = statement.executeQuery(request)) {
            while (set.next()) {
                tempData.append(userNumber++).append(". Login: ").append(set.getString(1))
                        .append(" Password: ").append(set.getString(2))
                        .append(" Email: ").append(set.getString(3)).append("\n");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        userData = tempData.toString();
        return userData;
    }
}
