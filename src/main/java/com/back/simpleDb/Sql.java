package com.back.simpleDb;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Sql {
    private final StringBuilder query = new StringBuilder();
    private final List<Object> params = new ArrayList<>();
    private final Connection connection;

    public Sql(Connection connection) {
        this.connection = connection;
    }

    public Sql append(String part, Object... params) {
        query.append(part).append("\n");
        this.params.addAll(List.of(params));
        return this;
    }

    public Sql appendIn(String part, Object... params) {
        String placeholders = Arrays.stream(params)
                .map(v -> "?")
                .collect(Collectors.joining(", "));
        String replaced = part.replace("?", placeholders);
        query.append(replaced).append("\n");
        this.params.addAll(Arrays.asList(params));
        return this;
    }

    public int insert() {
        return executeUpdate();
    }

    public int update() {
        return executeUpdate();
    }

    public int delete() {
        return executeUpdate();
    }

    public LocalDateTime selectDatetime() {
        return execute(pstmt -> {
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getTimestamp(resultSet.getRow()).toLocalDateTime();
                }
                return null;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Long selectLong() {
        return execute(pstmt -> {
            try (ResultSet resultSet = pstmt.executeQuery()) {
                return resultSet.next() ? resultSet.getLong(1) : null;
            }
        });
    }

    public String selectString() {
        return execute(pstmt -> {
            try (ResultSet resultSet = pstmt.executeQuery()) {
                return resultSet.next() ? resultSet.getString(1) : null;
            }
        });
    }

    public Map<String, Object> selectRow() {
        return execute(pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return resultSetToMap(rs);
                }
                return null;
            }
        });
    }

    public List<Map<String, Object>> selectRows() {
        return execute(pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Map<String, Object>> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(resultSetToMap(rs));
                }
                return result;
            }
        });
    }

    public Boolean selectBoolean() {
        return execute(pstmt -> {
            try (ResultSet resultSet = pstmt.executeQuery()) {
                return resultSet.next() ? resultSet.getBoolean(1) : null;
            }
        });
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

    private int executeUpdate() {
        return execute(PreparedStatement::executeUpdate);
    }

    private <T> T execute(QueryExecutor<T> queryExecutor) {
        try (PreparedStatement pstmt = connection.prepareStatement(query.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            return queryExecutor.execute(pstmt);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}