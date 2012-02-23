package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Strings;
import com.googlecode.lazyrecords.Record;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.expression;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;
import static com.googlecode.lazyrecords.sql.expressions.WhereClause.whereClause;

public class UpdateStatement extends CompoundExpression {
    public static final TextOnlyExpression UPDATE = textOnly("update");
    public static final TextOnlyExpression SET = textOnly("set");

    public UpdateStatement(Definition definition, Predicate<? super Record> predicate, Record record) {
        super(
                UPDATE.join(textOnly(definition)),
                setClause(record),
                whereClause(predicate)
                );
    }

    public static CompoundExpression setClause(Record record) {
        return SET.join(expression(record.keywords().map(Strings.format("%s=?")).toString(), record.getValuesFor(record.keywords())));
    }

    public static Expression updateStatement(Definition definition, Predicate<? super Record> predicate, Record record) {
        return new UpdateStatement(definition, predicate, record);
    }

    public static Function1<Pair<? extends Predicate<? super Record>, Record>, Expression> updateStatement(final Definition definition) {
        return new Function1<Pair<? extends Predicate<? super Record>, Record>, Expression>() {
            public Expression call(Pair<? extends Predicate<? super Record>, Record> recordPair) throws Exception {
                return updateStatement(definition, recordPair.first(), recordPair.second());
            }
        };
    }
}
