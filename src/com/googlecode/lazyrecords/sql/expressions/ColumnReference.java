package com.googlecode.lazyrecords.sql.expressions;

public class ColumnReference extends Reference<ColumnReference> {
    protected ColumnReference(String qualifier, String name, String alias) {
        super(qualifier, name, alias);
    }

    public static ColumnReference columnName(String text) {
        return columnReference("", text);
    }

    public static ColumnReference columnReference(String qualifier, String text) {
        return new ColumnReference(qualifier, text, "");
    }

    public static ColumnReference columnReference(String qualifier, String text, String alias) {
        return new ColumnReference(qualifier, text, alias);
    }

    @Override
    protected ColumnReference self(String qualifier, String text, String alias) {
        return columnReference(qualifier, text, alias);
    }
}
