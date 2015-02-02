package com.googlecode.lazyrecords.sql.expressions;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;

public class LimitClause extends CompoundExpression implements FetchClause {
    private static final TextOnlyExpression fetch = textOnly("limit");
    private final int number;

    public LimitClause(int number) {
        super(fetch, textOnly(number));
        this.number = number;
    }

    @Override
    public int number() {
        return number;
    }
}
