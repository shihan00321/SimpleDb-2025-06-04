package com.back.simpleDb;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetExecutor<T> {
    T execute(ResultSet resultSet) throws SQLException;
}
