package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Definition;

import static java.lang.String.format;

public class FromClause extends TextOnlyExpression{
    public FromClause(Definition table) {
        super(format("from %s", table));
    }

    public static Expression fromClause(Definition table) {
        return new FromClause(table);
    }
}
