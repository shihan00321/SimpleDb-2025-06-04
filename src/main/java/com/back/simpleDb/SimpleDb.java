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
    private final ThreadLocal<Connection> connectionThreadLocal;

    public SimpleDb(String host, String username, String password, String database) {
        this.url = "jdbc:mysql://" + host + ":3306/" + database;
        this.username = username;
        this.password = password;
        connectionThreadLocal = new ThreadLocal<>();
    }

    public int run(String query, Object... params) {
        Connection connection = getConnection();
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (!devMode) {
                close();
            }
        }
    }

    public Sql genSql() {
        return new Sql(getConnection());
    }

    private Connection getConnection() {
        Connection connection = connectionThreadLocal.get();
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, username, password);
                connectionThreadLocal.set(connection);
            }
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            Connection connection = connectionThreadLocal.get();
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void startTransaction() {
        try {
            Connection connection = getConnection();
            connection.setAutoCommit(false);
            devMode = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void rollback() {
        try {
            Connection connection = getConnection();
            connection.rollback();
            devMode = false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void commit() {
        try {
            Connection connection = getConnection();
            connection.commit();
            connection.setAutoCommit(true);
            devMode = false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}