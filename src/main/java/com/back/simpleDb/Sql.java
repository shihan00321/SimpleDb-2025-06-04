package com.back.simpleDb;

import java.util.ArrayList;
import java.util.List;

public class Sql {
    private final StringBuilder query = new StringBuilder();
    private final List<Object> params = new ArrayList<>();
    private final SimpleDb simpleDb;

    public Sql(SimpleDb simpleDb) {
        this.simpleDb = simpleDb;
    }

    public Sql append(String part) {
        query.append(part).append("\n");
        return this;
    }

    public Sql append(String part, Object param) {
        query.append(part).append("\n");
        params.add(param);
        return this;
    }

    public long insert() {
        return simpleDb.run(query.toString(), params.toArray());
    }
}
