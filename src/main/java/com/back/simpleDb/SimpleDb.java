package com.back.simpleDb;

import lombok.Setter;

import java.sql.*;
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

    public List<Map<String, Object>> select(String query) {
        return execute(pstmt -> {
            try (ResultSet resultSet = pstmt.executeQuery();) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                List<Map<String, Object>> result = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        row.put(columnName, resultSet.getObject(columnName));
                    }
                    result.add(row);
                }
                return result;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, query);
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

    public Sql genSql() {
        return new Sql(this);
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
