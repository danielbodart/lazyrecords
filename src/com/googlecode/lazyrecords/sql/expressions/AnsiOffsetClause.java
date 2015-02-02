package com.googlecode.lazyrecords.sql.expressions;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;

public class AnsiOffsetClause extends CompoundExpression implements OffsetClause {
    protected static final TextOnlyExpression offset = textOnly("offset");
    protected static final TextOnlyExpression rows = textOnly("rows");
    private final int number;

    public AnsiOffsetClause(int number) {
        this(offset, number, rows);
    }

    protected AnsiOffsetClause(Expression prefix, int number, Expression postfix) {
        super(prefix, textOnly(number), postfix);
        this.number = number;
    }

    @Override
    public int number() {
        return number;
    }
}

