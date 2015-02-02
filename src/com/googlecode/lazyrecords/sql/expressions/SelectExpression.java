package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Option;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;

public interface SelectExpression extends Expression {
    TextOnlyExpression select = textOnly("select");
    Option<SetQuantifier> setQuantifier();
    SelectList selectList();
    FromClause fromClause();
    Option<WhereClause> whereClause();
    Option<OrderByClause> orderByClause();
    Option<GroupByClause> groupByClause();
    Option<FetchClause> fetchClause();
}
