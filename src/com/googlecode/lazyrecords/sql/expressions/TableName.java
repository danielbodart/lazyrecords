package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Option;

import static com.googlecode.totallylazy.Option.none;

public class TableName extends Reference<TableName> {
    protected TableName(String name, Option<String> qualifier, Option<String> alias) {
        super(name, qualifier, alias);
    }

    public static TableName tableName(String text) {
        return tableName(text, none(String.class));
    }

    public static TableName tableName(String text, Option<String> qualifier) {
        return new TableName(text, qualifier, none(String.class));
    }

    public static TableName tableName(String text, Option<String> qualifier, Option<String> alias) {
        return new TableName(text, qualifier, alias);
    }

    @Override
    protected TableName self(String text, Option<String> qualifier, Option<String> alias) {
        return tableName(text, qualifier, alias);
    }
}
