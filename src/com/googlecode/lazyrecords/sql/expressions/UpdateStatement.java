package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
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
                setClause(definition, record),
                whereClause(predicate)
                );
    }

    public static CompoundExpression setClause(Definition definition, Record record) {
        Sequence<Keyword<?>> updatingKeywords = Record.methods.filter(record, definition.fields()).keywords();
        return SET.join(expression(updatingKeywords.map(name()).map(Strings.format("%s=?")).toString(), record.getValuesFor(updatingKeywords)));
    }

    public static Expression updateStatement(Definition definition, Predicate<? super Record> predicate, Record record) {
        return new UpdateStatement(definition, predicate, record);
    }

}
