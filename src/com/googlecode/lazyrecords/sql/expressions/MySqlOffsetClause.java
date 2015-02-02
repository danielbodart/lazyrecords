package com.googlecode.lazyrecords.sql.expressions;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.empty;

public class MySqlOffsetClause extends AnsiOffsetClause{
    public MySqlOffsetClause(int number) {
        super(offset, number, empty());
    }
}
