package com.back.simpleDb;

import lombok.Setter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Setter
public class SimpleDb {

    private final String url;
    private final String username;
    private final String password;
    private boolean devMode;

    public SimpleDb(String host, String username, String password, String database) {
        this.url = "jdbc:mysql://" + host + ":3306/" + database;
        this.username = username;
        this.password = password;
    }

    public void run(String query, Object... params) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement pstmt = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
