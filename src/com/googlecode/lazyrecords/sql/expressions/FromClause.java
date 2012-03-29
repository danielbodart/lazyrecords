package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Definition;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.name;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;
import static java.lang.String.format;

public class FromClause extends CompoundExpression{
    public FromClause(Definition table) {
        super(textOnly("from"), name(table));
    }

    public static Expression fromClause(Definition table) {
        return new FromClause(table);
    }
}
