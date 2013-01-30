package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;

import static com.googlecode.lazyrecords.sql.expressions.AnsiFromClause.fromClause;
import static com.googlecode.lazyrecords.sql.expressions.AnsiWhereClause.whereClause;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.expression;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;

public class DeleteStatement extends CompoundExpression {
    public DeleteStatement(Definition definition, Option<? extends Predicate<? super Record>> predicate) {
        super(
                textOnly("delete"),
                fromClause(definition),
                expression(whereClause(predicate))
        );
    }

    public static DeleteStatement deleteStatement(Definition definition, Option<? extends Predicate<? super Record>> none) {
        return new DeleteStatement(definition, none);
    }
}
