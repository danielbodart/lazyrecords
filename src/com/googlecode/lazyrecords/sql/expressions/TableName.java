package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Option;

import static com.googlecode.totallylazy.Option.none;

public class TableName extends Reference<TableName> {
    protected TableName(String name, Option<String> qualifier) {
        super(name, qualifier);
    }

    public static TableName tableName(String text) {
        return tableName(text, none(String.class));
    }

    public static TableName tableName(String text, Option<String> qualifier) {
        return new TableName(text, qualifier);
    }

    @Override
    protected TableName self(String text, Option<String> qualifier) {
        return tableName(text, qualifier);
    }
}
