package com.googlecode.lazyrecords.sql.expressions;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;

public interface OrderByClause extends Expression {
    TextOnlyExpression orderBy = textOnly("order by");
}
