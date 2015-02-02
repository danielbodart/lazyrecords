package com.googlecode.lazyrecords.sql.expressions;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;

public class AnsiFetchClause extends CompoundExpression implements FetchClause {
    private static final TextOnlyExpression fetch = textOnly("fetch next");
    private static final TextOnlyExpression rows = textOnly("rows only");
    private final int number;

    public AnsiFetchClause(int number) {
        super(fetch, textOnly(number), rows);
        this.number = number;
    }

    @Override
    public int number() {
        return number;
    }
}
