package com.todoroo.andlib.sql;

import android.text.TextUtils;

import com.google.common.collect.Lists;
import com.todoroo.andlib.data.Property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UnionQuery {

    private List<Field> fields = new ArrayList<>();

    private List<Query> queries = new ArrayList<>();

    public static UnionQuery unionQuery(Query... queries){
        return new UnionQuery(queries);
    }

    private UnionQuery(Query... queries) {
        this(Arrays.asList(queries));
    }

    private UnionQuery(List<Query> queries) {
        union(queries);
    }

    public UnionQuery union(List<Query> queries) {
        if (queries != null && !queries.isEmpty()){
            this.queries.addAll(queries);
            if (fields.isEmpty()) {
                fields.addAll(Lists.newArrayList(queries.get(0).getFields()));
            }
        }
        return this;
    }

    public UnionQuery union(Query... queries){
        return union(Arrays.asList(queries));
    }

    public Property<?>[] getFields() {
        return fields.toArray(new Property[fields.size()]);
    }

    @Override
    public String toString() {
        return TextUtils.join(" union ", queries);
    }
}
