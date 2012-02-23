package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.lazyrecords.Record;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;
import static com.googlecode.lazyrecords.sql.expressions.FromClause.fromClause;
import static com.googlecode.lazyrecords.sql.expressions.WhereClause.whereClause;

public class DeleteStatement extends CompoundExpression {
    public DeleteStatement(Definition definition, Option<Predicate<? super Record>> predicate) {
        super(
                textOnly("delete"),
                fromClause(definition),
                whereClause(predicate)
                );
    }

    public static Expression deleteStatement(Definition definition, Predicate<? super Record> predicate) {
        return new DeleteStatement(definition, Option.<Predicate<? super Record>>some(predicate));
    }

    public static Expression deleteStatement(Definition definition) {
        return new DeleteStatement(definition, Option.<Predicate<? super Record>>none());
    }
}
