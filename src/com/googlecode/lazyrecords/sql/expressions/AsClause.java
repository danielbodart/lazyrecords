package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Option;

public interface AsClause extends Expression {
    TextOnlyExpression as = Expressions.textOnly("as");

    Option<Expression> as();
    String alias();
}
