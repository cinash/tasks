/**
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.andlib.sql;

public class SqlTable extends DBObject<SqlTable> {

    protected SqlTable(String expression) {
        super(expression);
    }

    public static SqlTable table(Query query) {
        return new SqlTable("(" + query.toString() +")");
    }

    public static SqlTable table(UnionQuery query) {
        return new SqlTable("(" + query.toString() +")");
    }
}
