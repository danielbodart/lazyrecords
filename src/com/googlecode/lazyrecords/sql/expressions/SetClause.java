package com.googlecode.lazyrecords.sql.expressions;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;

public interface SetClause extends Expression {
    TextOnlyExpression set = textOnly("set");
}
