package com.googlecode.lazyrecords.sql.expressions;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;

public class AnsiFetchClause extends CompoundExpression implements FetchClause {
    protected static final TextOnlyExpression fetch = textOnly("fetch next");
    protected static final TextOnlyExpression rows = textOnly("rows only");
    private final int number;

    public AnsiFetchClause(int number) {
        this(fetch, number, rows);
    }

    protected AnsiFetchClause(Expression prefix, int number, Expression postfix) {
        super(prefix, textOnly(number), postfix);
        this.number = number;
    }

    @Override
    public int number() {
        return number;
    }
}
