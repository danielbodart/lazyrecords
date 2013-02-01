package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Option;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;

public interface DeleteStatement extends Expression {
    TextOnlyExpression delete = textOnly("delete");
    FromClause fromClause();
    Option<WhereClause> whereClause();
}
