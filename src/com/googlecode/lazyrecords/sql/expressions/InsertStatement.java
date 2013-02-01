package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.expression;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.formatList;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.columnReference;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.names;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;
import static com.googlecode.totallylazy.Sequences.repeat;

public class InsertStatement extends CompoundExpression {
    public static final TextOnlyExpression INSERT = textOnly("insert");
    public static final TextOnlyExpression VALUES = textOnly("values");

    public InsertStatement(final Definition definition, final Record record) {
        super(
                INSERT,
                textOnly("into").join(Expressions.tableName(definition)),
                columns(record),
                VALUES,
                values(record)
        );
    }

    public static TextOnlyExpression columns(Record record) {
        return textOnly(names(record.keywords()));
    }

    public static AbstractExpression values(Record record) {
        return expression(formatList(repeat("?").take(record.fields().size())),
                record.valuesFor(record.keywords()));
    }

    public static InsertStatement insertStatement(final Definition definition, final Record record) {
        return new InsertStatement(definition, record);
    }
}
