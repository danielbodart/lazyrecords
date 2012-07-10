package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Strings;
import com.googlecode.lazyrecords.Record;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.expression;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.name;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;
import static com.googlecode.lazyrecords.sql.expressions.WhereClause.whereClause;

public class UpdateStatement extends CompoundExpression {
    public static final TextOnlyExpression UPDATE = textOnly("update");
    public static final TextOnlyExpression SET = textOnly("set");

    public UpdateStatement(Definition definition, Predicate<? super Record> predicate, Record record) {
        super(
                UPDATE.join(name(definition)),
                setClause(record),
                whereClause(predicate)
                );
    }

    public static CompoundExpression setClause(Record record) {
        return SET.join(expression(record.keywords().map(name()).map(Strings.format("%s=?")).toString(), record.getValuesFor(record.keywords())));
    }

    public static Expression updateStatement(Definition definition, Predicate<? super Record> predicate, Record record) {
        return new UpdateStatement(definition, predicate, record);
    }

}
