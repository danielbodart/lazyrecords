package com.googlecode.lazyrecords.sql.expressions;

public class TableName extends Reference<TableName> {
    protected TableName(String qualifier, String name, String alias) {
        super(qualifier, name, alias);
    }

    public static TableName tableName(String text) {
        return tableName("", text);
    }

    public static TableName tableName(String qualifier, String text) {
        return new TableName(qualifier, text, "");
    }

    public static TableName tableName(String qualifier, String text, String alias) {
        return new TableName(qualifier, text, alias);
    }

    @Override
    protected TableName self(String qualifier, String text, String alias) {
        return tableName(qualifier, text, alias);
    }
}
