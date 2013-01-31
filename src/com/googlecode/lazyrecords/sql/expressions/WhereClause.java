package com.googlecode.lazyrecords.sql.expressions;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;

public interface WhereClause extends Expression {
    TextOnlyExpression where = textOnly("where");

    Expression expression();
}
