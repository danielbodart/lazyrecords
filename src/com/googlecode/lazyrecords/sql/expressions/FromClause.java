package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.RecordName;

import static java.lang.String.format;

public class FromClause extends TextOnlyExpression{
    public FromClause(RecordName table) {
        super(format("from %s", table));
    }

    public static Expression fromClause(RecordName table) {
        return new FromClause(table);
    }
}
