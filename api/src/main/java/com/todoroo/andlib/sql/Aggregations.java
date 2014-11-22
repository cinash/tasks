package com.todoroo.andlib.sql;


public final class Aggregations {

    public static Field sum(Field field){
        return new Field("SUM(" + field.toString() + ")");
    }

    public static Field min(Field field) {
        return new Field("min(" + field.toString() + ")");
    }
}
