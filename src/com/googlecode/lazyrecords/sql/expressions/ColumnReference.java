package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Option;

import static com.googlecode.totallylazy.Option.none;

public class ColumnReference extends Reference<ColumnReference> implements ValueExpression {
    protected ColumnReference(String name, Option<String> qualifier) {
        super(name, qualifier);
    }

    public static ColumnReference columnReference(String name) {
        return columnReference(name, none(String.class));
    }

    public static ColumnReference columnReference(String name, Option<String> qualifier) {
        return new ColumnReference(name, qualifier);
    }

    @Override
    protected ColumnReference self(String name, Option<String> qualifier) {
        return columnReference(name, qualifier);
    }
}
