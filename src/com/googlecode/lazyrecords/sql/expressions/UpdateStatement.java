package com.googlecode.lazyrecords.sql.expressions;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;

public interface UpdateStatement extends Expression {
    TextOnlyExpression update = textOnly("update");
    TableName tableName();
    SetClause setClause();
    WhereClause whereClause();
}
