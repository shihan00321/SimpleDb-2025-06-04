package com.back.simpleDb;

import lombok.Setter;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public int run(String query, Object... params) {
        return execute(pstmt -> pstmt.executeUpdate(), query, params);
    }

    public Map<String, Object> select(String query) {
        return execute(pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return resultSetToMap(rs);
                }
                return null;
            }
        }, query);
    }

    public List<Map<String, Object>> selectAll(String query) {
        return execute(pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Map<String, Object>> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(resultSetToMap(rs));
                }
                return result;
            }
        }, query);
    }

    public LocalDateTime selectDatetime(String query) {
        return execute(pstmt -> {
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getTimestamp(resultSet.getRow()).toLocalDateTime();
                }
                return null;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, query);
    }

    public Long selectLong(String query) {
        return execute(pstmt -> {
            try (ResultSet resultSet = pstmt.executeQuery()) {
                return resultSet.next() ? resultSet.getLong(1) : null;
            }
        }, query);
    }

    public String selectString(String query) {
        return execute(pstmt -> {
            try (ResultSet resultSet = pstmt.executeQuery()) {
                return resultSet.next() ? resultSet.getString(1) : null;
            }
        }, query);
    }

    private Map<String, Object> resultSetToMap(ResultSet rs) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            row.put(columnName, rs.getObject(i));
        }
        return row;
    }

    public Sql genSql() {
        return new Sql(this);
    }

    private <T> T execute(QueryExecutor<T> queryExecutor, String query, Object... params) {
        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            return queryExecutor.execute(pstmt);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
