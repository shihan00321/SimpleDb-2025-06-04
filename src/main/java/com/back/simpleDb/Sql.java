package com.back.simpleDb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Sql {
    private final StringBuilder query;
    private final List<Object> params;
    private final Connection connection;

    private final ObjectMapper objectMapper;

    public Sql(Connection connection) {
        this.connection = connection;
        query = new StringBuilder();
        params = new ArrayList<>();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()); // LocalDateTime 처리를 위해 등록
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
        return selectSingle(resultSet -> resultSet.getTimestamp(1).toLocalDateTime());
    }

    public Long selectLong() {
        return selectSingle(resultSet -> resultSet.getLong(1));
    }

    public String selectString() {
        return selectSingle(resultSet -> resultSet.getString(1));
    }

    public Boolean selectBoolean() {
        return selectSingle(resultSet -> resultSet.getBoolean(1));
    }

    public List<Long> selectLongs() {
        return selectList(resultSet -> resultSet.getLong(1));
    }


    public Map<String, Object> selectRow() {
        return selectSingle(resultSet -> resultSetToMap(resultSet));
    }

    public <T> T selectRow(Class<T> clazz) {
        Map<String, Object> row = selectRow();
        return row != null ? objectMapper.convertValue(row, clazz) : null;
    }

    public List<Map<String, Object>> selectRows() {
        return execute(pstmt -> {
            try (ResultSet resultSet = pstmt.executeQuery()) {
                List<Map<String, Object>> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(resultSetToMap(resultSet));
                }
                return result;
            }
        });
    }

    public <T> List<T> selectRows(Class<T> clazz) {
        return selectRows().stream()
                .map(row -> objectMapper.convertValue(row, clazz))
                .collect(Collectors.toList());
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

    private int executeUpdate() {
        return execute(PreparedStatement::executeUpdate);
    }

    private <T> T selectSingle(ResultSetExecutor<T> extractor) {
        return execute(pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? extractor.execute(rs) : null;
            }
        });
    }

    private <T> List<T> selectList(ResultSetExecutor<T> extractor) {
        return execute(pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(extractor.execute(rs));
                }
                return result;
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
}