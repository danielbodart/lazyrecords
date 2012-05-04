package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.totallylazy.Function1;
import com.googlecode.lazyrecords.Keyword;

import java.net.URI;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.name;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;
import static java.lang.String.format;

public class TableDefinition extends TextOnlyExpression {
    public TableDefinition(Definition definition) {
        super(format("create table %s (%s)", name(definition), definition.fields().map(asColumn())));
    }

    public static TableDefinition tableDefinition(Definition definition) {
        return new TableDefinition(definition);
    }

    public static final Map<Class, String> mappings = new ConcurrentHashMap<Class, String>() {{
        put(Date.class, "timestamp");
        put(Integer.class, "integer");
        put(Long.class, "bigint");
        put(Timestamp.class, "timestamp");
        put(Boolean.class, "varchar(5)");
        put(UUID.class, "varchar(36)");
        put(URI.class, "varchar(4000)");
        put(String.class, "varchar(4000)");
        put(Object.class, "clob");
    }};

    public static Function1<? super Keyword<?>, String> asColumn() {
        return new Function1<Keyword<?>, String>() {
            public String call(Keyword<?> keyword) throws Exception {
                return format("%s %s", name(keyword), type(keyword.forClass()));
            }
        };
    }

    public static String type(Class<?> aClass) {
        if (!mappings.containsKey(aClass)) {
            return mappings.get(Object.class);
        }
        return mappings.get(aClass);
    }

    public static CompoundExpression dropTable(Definition definition) {
        return textOnly("drop table").join(textOnly(definition));
    }
}
