package com.back.simpleDb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Sql {
    private final StringBuilder query = new StringBuilder();
    private final List<Object> parameterList = new ArrayList<>();
    private final SimpleDb simpleDb;

    public Sql(SimpleDb simpleDb) {
        this.simpleDb = simpleDb;
    }

    public Sql append(String part, Object... params) {
        query.append(part).append("\n");
        parameterList.addAll(Arrays.asList(params));
        return this;
    }

    public long insert() {
        return simpleDb.run(query.toString(), parameterList.toArray());
    }

    public int update() {
        return simpleDb.run(query.toString(), parameterList.toArray());
    }

    public int delete() {
        return simpleDb.run(query.toString(), parameterList.toArray());
    }

    public List<Map<String, Object>> selectRows() {
        return simpleDb.selectAll(query.toString());
    }

    public Map<String, Object> selectRow() {
        return simpleDb.select(query.toString());
    }
}
