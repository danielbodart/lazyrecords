package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Option;

import static com.googlecode.totallylazy.Option.none;

public class ColumnReference extends Reference<ColumnReference> {
    protected ColumnReference(String name, Option<String> qualifier, Option<String> alias) {
        super(name, qualifier, alias);
    }

    public static ColumnReference columnReference(String text) {
        return columnReference(text, none(String.class));
    }

    public static ColumnReference columnReference(String text, Option<String> qualifier) {
        return new ColumnReference(text, qualifier, none(String.class));
    }

    public static ColumnReference columnReference(String text, Option<String> qualifier, Option<String> alias) {
        return new ColumnReference(text, qualifier, alias);
    }

    @Override
    protected ColumnReference self(String text, Option<String> qualifier, Option<String> alias) {
        return columnReference(text, qualifier, alias);
    }
}
