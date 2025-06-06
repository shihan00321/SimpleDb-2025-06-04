package com.back.simpleDb;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface QueryExecutor<T> {
    T execute(PreparedStatement pstmt) throws SQLException;
}
