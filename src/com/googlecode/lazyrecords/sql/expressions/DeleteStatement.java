package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.RecordName;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;
import static com.googlecode.lazyrecords.sql.expressions.FromClause.fromClause;
import static com.googlecode.lazyrecords.sql.expressions.WhereClause.whereClause;

public class DeleteStatement extends CompoundExpression {
    public DeleteStatement(RecordName recordName, Option<Predicate<? super Record>> predicate) {
        super(
                textOnly("delete"),
                fromClause(recordName),
                whereClause(predicate)
                );
    }

    public static Expression deleteStatement(RecordName recordName, Predicate<? super Record> predicate) {
        return new DeleteStatement(recordName, Option.<Predicate<? super Record>>some(predicate));
    }

    public static Expression deleteStatement(RecordName recordName) {
        return new DeleteStatement(recordName, Option.<Predicate<? super Record>>none());
    }
}
