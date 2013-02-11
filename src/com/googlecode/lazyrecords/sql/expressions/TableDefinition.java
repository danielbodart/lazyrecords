package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.totallylazy.Function1;

import java.util.Map;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.quote;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.quotedText;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.tableName;
import static java.lang.String.format;

public class TableDefinition extends TextOnlyExpression {
    public TableDefinition(Definition definition, Map<Class, String> mappings) {
        super(format("create table %s (%s)", tableName(definition), definition.fields().map(asColumn(mappings))));
    }

    public static TableDefinition createTable(Definition definition, Map<Class, String> mappings) {
        return new TableDefinition(definition, mappings);
    }

    public static Function1<? super Keyword<?>, String> asColumn(final Map<Class, String> mappings) {
        return new Function1<Keyword<?>, String>() {
            public String call(Keyword<?> keyword) throws Exception {
                return format("%s %s", Expressions.columnReference(keyword), type(keyword.forClass(), mappings));
            }
        };
    }

    public static String type(Class<?> aClass, Map<Class, String> mappings) {
        if (!mappings.containsKey(aClass)) {
            return mappings.get(Object.class);
        }
        return mappings.get(aClass);
    }

    public static CompoundExpression dropTable(Definition definition) {
        return textOnly("drop table").join(tableName(definition));
    }
}
