package com.googlecode.lazyrecords.sql.expressions;

public interface SortSpecification extends Expression {
    ValueExpression sortKey();
    OrderingSpecification orderingSpecification();
}
