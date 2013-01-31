package com.googlecode.lazyrecords.sql.expressions;

public interface PredicateExpression extends Expression {
    ValueExpression predicand();
    Expression predicate();
}
