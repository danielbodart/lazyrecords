package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Strings;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.expression;

public class AnsiSetClause extends CompoundExpression implements SetClause {
    private AnsiSetClause(Sequence<ColumnReference> columnReferences, Sequence<Object> parameters) {
        super(set, expression(columnReferences.map(Strings.format("%s=?")).toString(), parameters));
    }

    public static AnsiSetClause setClause(Sequence<ColumnReference> columnReferences, Sequence<Object> parameters) {
        return new AnsiSetClause(columnReferences, parameters);
    }
}
