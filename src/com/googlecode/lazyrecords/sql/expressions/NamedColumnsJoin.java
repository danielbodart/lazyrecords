package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Sequence;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.quotedText;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;
import static com.googlecode.totallylazy.Sequences.sequence;

public class NamedColumnsJoin extends CompoundExpression implements JoinSpecification {
    public static final TextOnlyExpression using = textOnly("using");
    private final Sequence<String> columnNames;

    private NamedColumnsJoin(final Sequence<String> columnNames) {
        super(columnNames.map(quotedText).cons(using));
        this.columnNames = columnNames;
    }

    public static NamedColumnsJoin namedColumnsJoin(final Sequence<String> columnNames) {
        return new NamedColumnsJoin(columnNames);
    }

    public static NamedColumnsJoin namedColumnsJoin(final String columnNameHead, String... columnNameTail) {
        return new NamedColumnsJoin(sequence(columnNameTail).cons(columnNameHead));
    }

    public Sequence<String> columnNames() {
        return columnNames;
    }
}
