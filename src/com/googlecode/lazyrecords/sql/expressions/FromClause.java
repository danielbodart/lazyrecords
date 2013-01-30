package com.googlecode.lazyrecords.sql.expressions;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;

public interface FromClause extends Expression {
    TextOnlyExpression from = textOnly("from");
    TableName tableName();
}
