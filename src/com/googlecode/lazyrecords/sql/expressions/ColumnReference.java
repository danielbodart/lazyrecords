package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Option;

import static com.googlecode.totallylazy.Option.none;

public class ColumnReference extends Reference<ColumnReference> implements ValueExpression {
    protected ColumnReference(String name, Option<String> qualifier) {
        super(name, qualifier);
    }

    public static ColumnReference columnReference(String text) {
        return columnReference(text, none(String.class));
    }

    public static ColumnReference columnReference(String text, Option<String> qualifier) {
        return new ColumnReference(text, qualifier);
    }

    @Override
    protected ColumnReference self(String text, Option<String> qualifier) {
        return columnReference(text, qualifier);
    }
}
