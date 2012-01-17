package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.RecordName;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;

import static com.googlecode.totallylazy.Sequences.repeat;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.expression;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;

public class InsertStatement extends CompoundExpression {
    public static final TextOnlyExpression INSERT = textOnly("insert");
    public static final TextOnlyExpression VALUES = textOnly("values");

    public InsertStatement(final RecordName recordName, final Record record) {
        super(
                INSERT,
                textOnly("into").join(textOnly(recordName)),
                columns(record),
                VALUES,
                values(record)
        );
    }

    public static TextOnlyExpression columns(Record record) {
        return textOnly(formatList(record.keywords()));
    }

    public static AbstractExpression values(Record record) {
        return expression(formatList(repeat("?").take((Integer) record.fields().size())),
                record.getValuesFor(record.keywords()));
    }

    public static String formatList(final Sequence<?> values) {
        return values.toString("(", ",", ")");
    }

    public static Function1<Record, Expression> insertStatement(final RecordName recordName) {
        return new Function1<Record, Expression>() {
            public Expression call(Record record) throws Exception {
                return insertStatement(recordName, record);
            }
        };
    }

    public static InsertStatement insertStatement(final RecordName recordName, final Record record) {
        return new InsertStatement(recordName, record);
    }
}
