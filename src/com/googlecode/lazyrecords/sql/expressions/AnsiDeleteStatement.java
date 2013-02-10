package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Option;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.expression;

public class AnsiDeleteStatement extends CompoundExpression implements DeleteStatement {
    private final FromClause fromClause;
    private final Option<WhereClause> whereClause;

    private AnsiDeleteStatement(FromClause fromClause, Option<WhereClause> whereClause) {
        super(
                delete,
                fromClause,
                expression(whereClause)
        );
        this.fromClause = fromClause;
        this.whereClause = whereClause;
    }

    public static DeleteStatement deleteStatement(FromClause fromClause, Option<WhereClause> whereClause) {
        return new AnsiDeleteStatement(fromClause, whereClause);
    }

    @Override
    public FromClause fromClause() {
        return fromClause;
    }

    @Override
    public Option<WhereClause> whereClause() {
        return whereClause;
    }
}
